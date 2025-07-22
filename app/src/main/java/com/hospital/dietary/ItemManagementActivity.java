package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.CategoryDAO;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.models.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private CategoryDAO categoryDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private ListView itemsListView;
    private TextView itemsCountText;
    private Button addItemButton;
    private Button manageCategoriesButton;
    private EditText searchEditText;
    private Spinner categoryFilterSpinner;

    // Data
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private ArrayAdapter<Item> itemsAdapter;
    private ArrayAdapter<String> categoryAdapter;

    // Filter state
    private String currentSearchTerm = "";
    private String currentCategoryFilter = "All Categories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_management_enhanced);

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
        itemDAO = new ItemDAO(dbHelper);
        categoryDAO = new CategoryDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Item Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI
        initializeViews();
        setupListeners();
        loadCategories();
        loadItems();
    }

    private void initializeViews() {
        itemsListView = findViewById(R.id.itemsListView);
        itemsCountText = findViewById(R.id.itemsCountText);
        addItemButton = findViewById(R.id.addItemButton);
        manageCategoriesButton = findViewById(R.id.manageCategoriesButton);
        searchEditText = findViewById(R.id.itemSearchEditText);
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);
    }

    private void setupListeners() {
        addItemButton.setOnClickListener(v -> showAddItemDialog());
        manageCategoriesButton.setOnClickListener(v -> openCategoryManagement());

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchTerm = s.toString().trim();
                applyFilters();
            }
        });

        // Category filter
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategoryFilter = categories.get(position);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Item list click
        itemsListView.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = filteredItems.get(position);
            showItemOptionsDialog(selectedItem);
        });
    }

    private void loadCategories() {
        categories.clear();
        categories.add("All Categories");

        List<String> dbCategories = categoryDAO.getAllCategories();
        categories.addAll(dbCategories);

        // If no categories exist, add default ones
        if (dbCategories.isEmpty()) {
            createDefaultCategories();
            dbCategories = categoryDAO.getAllCategories();
            categories.addAll(dbCategories);
        }

        // Update category spinner
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);
    }

    private void createDefaultCategories() {
        String[] defaultCategories = {
                "Proteins", "Starches", "Vegetables", "Beverages",
                "Fruits", "Desserts", "Breakfast Items", "Condiments"
        };

        for (String category : defaultCategories) {
            categoryDAO.addCategory(category);
        }
    }

    private void loadItems() {
        try {
            allItems = itemDAO.getAllItems();
            applyFilters();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFilters() {
        filteredItems.clear();

        for (Item item : allItems) {
            // Skip placeholder items
            if (item.getName().startsWith("Category Placeholder - ")) {
                continue;
            }

            boolean matchesSearch = currentSearchTerm.isEmpty() ||
                    item.getName().toLowerCase().contains(currentSearchTerm.toLowerCase()) ||
                    item.getCategory().toLowerCase().contains(currentSearchTerm.toLowerCase());

            boolean matchesCategory = currentCategoryFilter.equals("All Categories") ||
                    item.getCategory().equals(currentCategoryFilter);

            if (matchesSearch && matchesCategory) {
                filteredItems.add(item);
            }
        }

        updateItemsList();
    }

    private void updateItemsList() {
        // Create adapter for filtered items
        itemsAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_2, android.R.id.text1, filteredItems) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                Item item = filteredItems.get(position);
                text1.setText(item.getName());

                String subtitle = "Category: " + item.getCategory();
                if (item.isAdaFriendly()) {
                    subtitle += " • ADA Friendly";
                }
                text2.setText(subtitle);

                return view;
            }
        };

        itemsListView.setAdapter(itemsAdapter);

        // Update count
        String countText = "Items: " + filteredItems.size();
        if (filteredItems.size() != allItems.size() - getPlaceholderItemCount()) {
            countText += " (filtered from " + (allItems.size() - getPlaceholderItemCount()) + ")";
        }
        itemsCountText.setText(countText);
    }

    private int getPlaceholderItemCount() {
        int count = 0;
        for (Item item : allItems) {
            if (item.getName().startsWith("Category Placeholder - ")) {
                count++;
            }
        }
        return count;
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name");
        layout.addView(nameInput);

        final Spinner categorySpinner = new Spinner(this);
        List<String> categoriesForSpinner = new ArrayList<>(categoryDAO.getAllCategories());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesForSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        layout.addView(categorySpinner);

        final CheckBox adaCheckBox = new CheckBox(this);
        adaCheckBox.setText("ADA Friendly");
        layout.addView(adaCheckBox);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            boolean adaFriendly = adaCheckBox.isChecked();

            if (name.isEmpty()) {
                Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            Item newItem = new Item();
            newItem.setName(name);
            newItem.setCategory(category);
            newItem.setAdaFriendly(adaFriendly);

            long result = itemDAO.addItem(newItem);
            if (result > 0) {
                Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                loadItems();
            } else {
                Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showItemOptionsDialog(Item item) {
        String[] options = {"Edit Item", "Delete Item"};

        new AlertDialog.Builder(this)
                .setTitle("Item: " + item.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showEditItemDialog(item);
                            break;
                        case 1:
                            showDeleteItemConfirmation(item);
                            break;
                    }
                })
                .show();
    }

    private void showEditItemDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Item: " + item.getName());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name");
        nameInput.setText(item.getName());
        layout.addView(nameInput);

        final Spinner categorySpinner = new Spinner(this);
        List<String> categoriesForSpinner = new ArrayList<>(categoryDAO.getAllCategories());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesForSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set current category
        for (int i = 0; i < categoriesForSpinner.size(); i++) {
            if (categoriesForSpinner.get(i).equals(item.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }
        layout.addView(categorySpinner);

        final CheckBox adaCheckBox = new CheckBox(this);
        adaCheckBox.setText("ADA Friendly");
        adaCheckBox.setChecked(item.isAdaFriendly());
        layout.addView(adaCheckBox);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            boolean adaFriendly = adaCheckBox.isChecked();

            if (name.isEmpty()) {
                Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            item.setName(name);
            item.setCategory(category);
            item.setAdaFriendly(adaFriendly);

            boolean success = itemDAO.updateItem(item);
            if (success) {
                Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                loadCategories(); // Reload in case category changed
                loadItems();
            } else {
                Toast.makeText(this, "Error updating item", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteItemConfirmation(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete '" + item.getName() + "'?\n\nThis cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = itemDAO.deleteItem(item.getItemId());

                    if (success) {
                        Toast.makeText(this, "Item deleted successfully!", Toast.LENGTH_SHORT).show();
                        loadItems();
                    } else {
                        Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openCategoryManagement() {
        Intent intent = new Intent(this, CategoryManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
        loadItems();
    }
}