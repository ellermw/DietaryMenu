package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.CategoryDAO;
import com.hospital.dietary.dao.ItemDAO;
import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private CategoryDAO categoryDAO;
    private ItemDAO itemDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private ListView categoriesListView;
    private TextView categoryCountText;
    private Button addCategoryButton;

    // Data
    private List<CategoryDAO.CategoryInfo> categories = new ArrayList<>();
    private ArrayAdapter<CategoryDAO.CategoryInfo> categoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Check admin access
        boolean isAdmin = currentUserRole != null &&
                ("Admin".equalsIgnoreCase(currentUserRole.trim()) ||
                        "Administrator".equalsIgnoreCase(currentUserRole.trim()));

        if (!isAdmin) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        categoryDAO = new CategoryDAO(dbHelper);
        itemDAO = new ItemDAO(dbHelper);

        // Initialize UI
        initializeUI();
        setupListeners();

        // Load data
        loadCategories();
    }

    private void initializeUI() {
        categoriesListView = findViewById(R.id.categoriesListView);
        categoryCountText = findViewById(R.id.categoryCountText);
        addCategoryButton = findViewById(R.id.addCategoryButton);

        // Setup list adapter
        categoriesAdapter = new ArrayAdapter<CategoryDAO.CategoryInfo>(this, android.R.layout.simple_list_item_2, android.R.id.text1, categories) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                CategoryDAO.CategoryInfo category = categories.get(position);
                text1.setText(category.getName());
                text2.setText(category.getItemCount() + " items");

                return view;
            }
        };
        categoriesListView.setAdapter(categoriesAdapter);
    }

    private void setupListeners() {
        // Add category button
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());

        // Category click listener
        categoriesListView.setOnItemClickListener((parent, view, position, id) -> {
            CategoryDAO.CategoryInfo selectedCategory = categories.get(position);
            showCategoryOptionsDialog(selectedCategory);
        });
    }

    private void loadCategories() {
        categories.clear();
        categories.addAll(categoryDAO.getAllCategoriesWithCounts());
        categoriesAdapter.notifyDataSetChanged();
        updateCategoryCount();
    }

    private void updateCategoryCount() {
        categoryCountText.setText("Categories: " + categories.size());
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");

        final EditText input = new EditText(this);
        input.setHint("Category name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String categoryName = input.getText().toString().trim();
            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryDAO.categoryExists(categoryName)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            long result = categoryDAO.addCategory(categoryName);
            if (result > 0) {
                Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCategoryOptionsDialog(CategoryDAO.CategoryInfo category) {
        String[] options = category.getItemCount() > 0 ?
                new String[]{"Edit", "View Items"} :
                new String[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(category.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditCategoryDialog(category);
                    } else if (which == 1 && category.getItemCount() > 0) {
                        showCategoryItems(category);
                    } else {
                        showDeleteCategoryConfirmation(category);
                    }
                });
        builder.show();
    }

    private void showEditCategoryDialog(CategoryDAO.CategoryInfo category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Category");

        final EditText input = new EditText(this);
        input.setText(category.getName());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newName.equals(category.getName()) && categoryDAO.categoryExists(newName)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            int result = categoryDAO.updateCategory(category.getName(), newName);
            if (result > 0) {
                Toast.makeText(this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCategoryItems(CategoryDAO.CategoryInfo category) {
        // Show items in this category
        List<com.hospital.dietary.models.Item> items = itemDAO.getItemsByCategory(category.getName());

        String[] itemNames = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            itemNames[i] = items.get(i).getItemName();
        }

        new AlertDialog.Builder(this)
                .setTitle(category.getName() + " Items")
                .setItems(itemNames, null)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteCategoryConfirmation(CategoryDAO.CategoryInfo category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.getName() + "'?\n\nThis will delete all items in this category and cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = categoryDAO.deleteCategory(category.getName());
                    if (result > 0) {
                        Toast.makeText(this, "Category and its items deleted successfully", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}