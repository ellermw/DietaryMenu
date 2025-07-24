package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.CategoryDAO;
import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private CategoryDAO categoryDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView backArrow;
    private Button addCategoryButton;
    private ListView categoriesListView;
    private TextView categoriesCountText;

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
        if (!"Admin".equalsIgnoreCase(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        categoryDAO = new CategoryDAO(dbHelper);

        // Hide default action bar since we have custom header
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI
        initializeViews();
        setupListeners();
        loadCategories();
    }

    private void initializeViews() {
        backArrow = findViewById(R.id.backArrow);
        addCategoryButton = findViewById(R.id.addCategoryButton);
        categoriesListView = findViewById(R.id.categoriesListView);
        categoriesCountText = findViewById(R.id.categoriesCountText);
    }

    private void setupListeners() {
        // Back arrow
        backArrow.setOnClickListener(v -> finish());

        // Add category button
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());

        // Category list click
        categoriesListView.setOnItemClickListener((parent, view, position, id) -> {
            CategoryDAO.CategoryInfo selectedCategory = categories.get(position);
            showCategoryOptionsDialog(selectedCategory);
        });
    }

    private void loadCategories() {
        try {
            categories = categoryDAO.getCategoriesWithCounts();
            updateCategoriesList();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCategoriesList() {
        // Create adapter for categories
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

        // Update count
        categoriesCountText.setText("Categories: " + categories.size());
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");

        // Create input layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText input = new EditText(this);
        input.setHint("Category Name");
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String categoryName = input.getText().toString().trim();

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryDAO.categoryExists(categoryName)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = categoryDAO.addCategory(categoryName);
            if (success) {
                Toast.makeText(this, "Category added successfully!", Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "Error adding category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCategoryOptionsDialog(CategoryDAO.CategoryInfo category) {
        String[] options = {"Edit Category", "Delete Category"};

        new AlertDialog.Builder(this)
                .setTitle("Category: " + category.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showEditCategoryDialog(category);
                            break;
                        case 1:
                            showDeleteCategoryConfirmation(category);
                            break;
                    }
                })
                .show();
    }

    private void showEditCategoryDialog(CategoryDAO.CategoryInfo category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Category: " + category.getName());

        // Create input layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText input = new EditText(this);
        input.setHint("Category Name");
        input.setText(category.getName());
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newCategoryName = input.getText().toString().trim();

            if (newCategoryName.isEmpty()) {
                Toast.makeText(this, "Category name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newCategoryName.equals(category.getName())) {
                Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryDAO.categoryExists(newCategoryName)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = categoryDAO.renameCategory(category.getName(), newCategoryName);
            if (success) {
                Toast.makeText(this, "Category updated successfully!", Toast.LENGTH_SHORT).show();
                loadCategories();
            } else {
                Toast.makeText(this, "Error updating category", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteCategoryConfirmation(CategoryDAO.CategoryInfo category) {
        if (category.getItemCount() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Cannot Delete Category")
                    .setMessage("Cannot delete '" + category.getName() + "' because it contains " +
                            category.getItemCount() + " items.\n\nMove or delete the items first.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete the category '" + category.getName() + "'?\n\nThis cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = categoryDAO.deleteCategory(category.getName());
                    if (success) {
                        Toast.makeText(this, "Category deleted successfully!", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(this, "Error deleting category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }
}