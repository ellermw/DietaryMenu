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

        // FIXED: Check admin access for both "Admin" and "Administrator" roles (case-insensitive)
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
        try {
            categories.clear();
            categories.add("All Categories");
            // FIXED: Use getAllCategories() method that exists
            List<String> dbCategories = categoryDAO.getAllCategories();
            categories.addAll(dbCategories);

            categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categoryFilterSpinner.setAdapter(categoryAdapter);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            boolean matchesSearch = currentSearchTerm.isEmpty() ||
                    item.getName().toLowerCase().contains(currentSearchTerm.toLowerCase()) ||
                    item.getDescription().toLowerCase().contains(currentSearchTerm.toLowerCase());

            boolean matchesCategory = currentCategoryFilter.equals("All Categories") ||
                    currentCategoryFilter.equals(item.getCategory());

            if (matchesSearch && matchesCategory) {
                filteredItems.add(item);
            }
        }

        updateListView();
    }

    private void updateListView() {
        if (itemsAdapter == null) {
            itemsAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_2,
                    android.R.id.text1, filteredItems) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    Item item = filteredItems.get(position);
                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);

                    text1.setText(item.getName());
                    // FIXED: Remove price display since Item doesn't have price field
                    text2.setText("Category: " + item.getCategory() +
                            " | ADA: " + (item.isAdaFriendly() ? "Yes" : "No"));

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
        }
    }

    private void showAddItemDialog() {
        // Create dialog layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Name field
        TextView nameLabel = new TextView(this);
        nameLabel.setText("Item Name:");
        layout.addView(nameLabel);

        EditText nameEdit = new EditText(this);
        nameEdit.setHint("Enter item name");
        layout.addView(nameEdit);

        // Description field
        TextView descLabel = new TextView(this);
        descLabel.setText("Description:");
        layout.addView(descLabel);

        EditText descEdit = new EditText(this);
        descEdit.setHint("Enter description");
        layout.addView(descEdit);

        // Category spinner
        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category:");
        layout.addView(categoryLabel);

        Spinner categorySpinner = new Spinner(this);
        // FIXED: Use getAllCategories() method that exists
        List<String> categoryNames = categoryDAO.getAllCategories();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
        layout.addView(categorySpinner);

        // ADA Friendly checkbox
        CheckBox adaCheckBox = new CheckBox(this);
        adaCheckBox.setText("ADA Friendly");
        layout.addView(adaCheckBox);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Item")
                .setView(layout)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(v -> {
                String name = nameEdit.getText().toString().trim();
                String description = descEdit.getText().toString().trim();
                String category = categorySpinner.getSelectedItem().toString();
                boolean adaFriendly = adaCheckBox.isChecked();

                if (validateItemInput(name, description)) {
                    Item newItem = new Item();
                    newItem.setName(name);
                    newItem.setDescription(description);
                    newItem.setCategory(category);
                    newItem.setAdaFriendly(adaFriendly);

                    // FIXED: addItem returns long, check if > 0
                    long result = itemDAO.addItem(newItem);
                    if (result > 0) {
                        Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                        loadItems();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Error adding item", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        dialog.show();
    }

    private boolean validateItemInput(String name, String description) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showItemOptionsDialog(Item item) {
        String[] options = {"Edit Item", "Delete Item"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getName())
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
        // Create dialog layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Name field
        TextView nameLabel = new TextView(this);
        nameLabel.setText("Item Name:");
        layout.addView(nameLabel);

        EditText nameEdit = new EditText(this);
        nameEdit.setText(item.getName());
        layout.addView(nameEdit);

        // Description field
        TextView descLabel = new TextView(this);
        descLabel.setText("Description:");
        layout.addView(descLabel);

        EditText descEdit = new EditText(this);
        descEdit.setText(item.getDescription());
        layout.addView(descEdit);

        // Category spinner
        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category:");
        layout.addView(categoryLabel);

        Spinner categorySpinner = new Spinner(this);
        // FIXED: Use getAllCategories() method that exists
        List<String> categoryNames = categoryDAO.getAllCategories();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        // Set current category
        int categoryPosition = categoryNames.indexOf(item.getCategory());
        if (categoryPosition >= 0) {
            categorySpinner.setSelection(categoryPosition);
        }
        layout.addView(categorySpinner);

        // ADA Friendly checkbox
        CheckBox adaCheckBox = new CheckBox(this);
        adaCheckBox.setText("ADA Friendly");
        adaCheckBox.setChecked(item.isAdaFriendly());
        layout.addView(adaCheckBox);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Item")
                .setView(layout)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            updateButton.setOnClickListener(v -> {
                String name = nameEdit.getText().toString().trim();
                String description = descEdit.getText().toString().trim();
                String category = categorySpinner.getSelectedItem().toString();
                boolean adaFriendly = adaCheckBox.isChecked();

                if (validateItemInput(name, description)) {
                    item.setName(name);
                    item.setDescription(description);
                    item.setCategory(category);
                    item.setAdaFriendly(adaFriendly);

                    if (itemDAO.updateItem(item)) {
                        Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                        loadItems();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Error updating item", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        dialog.show();
    }

    private void showDeleteItemConfirmation(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete '" + item.getName() + "'?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // FIXED: Use deleteItem method with itemId
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
        startActivityForResult(intent, 100); // Use request code to refresh categories when returning
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