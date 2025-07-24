package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

    private static final String TAG = "ItemManagementActivity";

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

        Log.d(TAG, "ItemManagementActivity onCreate started");

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Check admin access for both "Admin" and "Administrator" roles (case-insensitive)
        boolean isAdmin = currentUserRole != null &&
                ("Admin".equalsIgnoreCase(currentUserRole.trim()) ||
                        "Administrator".equalsIgnoreCase(currentUserRole.trim()));

        if (!isAdmin) {
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

        // Log which views were found
        Log.d(TAG, "itemsListView: " + (itemsListView != null ? "Found" : "NULL"));
        Log.d(TAG, "itemsCountText: " + (itemsCountText != null ? "Found" : "NULL"));
        Log.d(TAG, "addItemButton: " + (addItemButton != null ? "Found" : "NULL"));
    }

    private void setupListeners() {
        if (addItemButton != null) {
            addItemButton.setOnClickListener(v -> showAddItemDialog());
        }

        if (manageCategoriesButton != null) {
            manageCategoriesButton.setOnClickListener(v -> openCategoryManagement());
        }

        // Search functionality
        if (searchEditText != null) {
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
        }

        // Category filter
        if (categoryFilterSpinner != null) {
            categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position < categories.size()) {
                        currentCategoryFilter = categories.get(position);
                        applyFilters();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Item list click
        if (itemsListView != null) {
            itemsListView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < filteredItems.size()) {
                    Item selectedItem = filteredItems.get(position);
                    showItemOptionsDialog(selectedItem);
                }
            });
        }
    }

    private void loadCategories() {
        try {
            categories.clear();
            categories.add("All Categories");
            List<String> dbCategories = categoryDAO.getAllCategories();
            if (dbCategories != null) {
                categories.addAll(dbCategories);
            }

            Log.d(TAG, "Loaded " + categories.size() + " categories");

            if (categoryFilterSpinner != null) {
                categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categoryFilterSpinner.setAdapter(categoryAdapter);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading categories: " + e.getMessage());
            Toast.makeText(this, "Error loading categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadItems() {
        try {
            Log.d(TAG, "Loading items...");
            allItems = itemDAO.getAllItems();
            Log.d(TAG, "Loaded " + allItems.size() + " items from database");

            // DEBUG: Log first few items
            for (int i = 0; i < Math.min(5, allItems.size()); i++) {
                Item item = allItems.get(i);
                Log.d(TAG, "Item " + i + ": " + item.getName() + " (" + item.getCategory() + ")");
            }

            applyFilters();

        } catch (Exception e) {
            Log.e(TAG, "Error loading items: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading items: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            // If no items found, show a helpful message
            if (allItems.isEmpty()) {
                Toast.makeText(this, "No items found in database. Database may need to be recreated.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void applyFilters() {
        filteredItems.clear();

        for (Item item : allItems) {
            boolean matchesSearch = currentSearchTerm.isEmpty() ||
                    item.getName().toLowerCase().contains(currentSearchTerm.toLowerCase()) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(currentSearchTerm.toLowerCase()));

            boolean matchesCategory = currentCategoryFilter.equals("All Categories") ||
                    currentCategoryFilter.equals(item.getCategory());

            if (matchesSearch && matchesCategory) {
                filteredItems.add(item);
            }
        }

        Log.d(TAG, "Applied filters: " + filteredItems.size() + " items match");
        updateListView();
    }

    private void updateListView() {
        if (itemsListView == null) {
            Log.e(TAG, "itemsListView is null, cannot update");
            return;
        }

        if (itemsAdapter == null) {
            itemsAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_2,
                    android.R.id.text1, filteredItems) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    if (position < filteredItems.size()) {
                        Item item = filteredItems.get(position);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        text1.setText(item.getName());
                        text2.setText("Category: " + item.getCategory() +
                                " | ADA: " + (item.isAdaFriendly() ? "Yes" : "No"));

                        // Set explicit colors for Samsung compatibility
                        text1.setTextColor(0xFF2c3e50);
                        text2.setTextColor(0xFF7f8c8d);
                    }

                    return view;
                }
            };
            itemsListView.setAdapter(itemsAdapter);
        } else {
            itemsAdapter.notifyDataSetChanged();
        }

        // Update count
        if (itemsCountText != null) {
            String countText = filteredItems.size() + " of " + allItems.size() + " items";
            if (!currentSearchTerm.isEmpty() || !currentCategoryFilter.equals("All Categories")) {
                countText += " (filtered)";
            }
            itemsCountText.setText(countText);
            Log.d(TAG, "Updated count text: " + countText);
        }
    }

    private void showAddItemDialog() {
        // Create dialog layout programmatically for Samsung compatibility
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Name field
        TextView nameLabel = new TextView(this);
        nameLabel.setText("Item Name *");
        nameLabel.setTextColor(0xFF2c3e50);
        layout.addView(nameLabel);

        EditText nameEdit = new EditText(this);
        nameEdit.setHint("Enter item name");
        nameEdit.setTextColor(0xFF2c3e50);
        layout.addView(nameEdit);

        // Description field
        TextView descLabel = new TextView(this);
        descLabel.setText("Description");
        descLabel.setTextColor(0xFF2c3e50);
        layout.addView(descLabel);

        EditText descEdit = new EditText(this);
        descEdit.setHint("Enter description");
        descEdit.setTextColor(0xFF2c3e50);
        layout.addView(descEdit);

        // Category field
        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category *");
        categoryLabel.setTextColor(0xFF2c3e50);
        layout.addView(categoryLabel);

        Spinner categorySpinner = new Spinner(this);
        List<String> categoryOptions = new ArrayList<>(categories);
        if (categoryOptions.contains("All Categories")) {
            categoryOptions.remove("All Categories");
        }
        categoryOptions.add("Breakfast");
        categoryOptions.add("Lunch");
        categoryOptions.add("Dinner");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        layout.addView(categorySpinner);

        // ADA Friendly checkbox
        CheckBox adaCheckBox = new CheckBox(this);
        adaCheckBox.setText("ADA Friendly");
        adaCheckBox.setTextColor(0xFF2c3e50);
        layout.addView(adaCheckBox);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Add New Item")
                .setView(layout)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setTextColor(0xFF2196F3);

            addButton.setOnClickListener(v -> {
                String name = nameEdit.getText().toString().trim();
                String description = descEdit.getText().toString().trim();
                String category = categorySpinner.getSelectedItem().toString();
                boolean isAdaFriendly = adaCheckBox.isChecked();

                if (validateItemInput(name, category)) {
                    Item newItem = new Item();
                    newItem.setName(name);
                    newItem.setDescription(description);
                    newItem.setCategory(category);
                    newItem.setAdaFriendly(isAdaFriendly);

                    long result = itemDAO.addItem(newItem);
                    if (result > 0) {
                        Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadItems();
                    } else {
                        Toast.makeText(this, "Error adding item", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        dialog.show();
    }

    private boolean validateItemInput(String name, String category) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showItemOptionsDialog(Item item) {
        String[] options = {"Edit Item", "Delete Item"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit Item
                            showEditItemDialog(item);
                            break;
                        case 1: // Delete Item
                            showDeleteItemConfirmation(item);
                            break;
                    }
                })
                .show();
    }

    private void showEditItemDialog(Item item) {
        Toast.makeText(this, "Edit item functionality - to be implemented", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteItemConfirmation(Item item) {
        new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete '" + item.getName() + "'?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (itemDAO.deleteItem(item.getItemId())) {
                        Toast.makeText(this, "Item deleted successfully!", Toast.LENGTH_SHORT).show();
                        loadItems();
                    } else {
                        Toast.makeText(this, "Error deleting item", Toast.LENGTH_LONG).show();
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
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            // Refresh categories when returning from CategoryManagementActivity
            loadCategories();
            loadItems();
        }
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}