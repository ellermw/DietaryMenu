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
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        categoryDAO = new CategoryDAO(dbHelper);

        // Initialize UI
        initializeUI();
        setupListeners();

        // Load data
        loadCategories();
        loadItems();
    }

    private void initializeUI() {
        itemsListView = findViewById(R.id.itemsListView);
        itemsCountText = findViewById(R.id.itemsCountText);
        addItemButton = findViewById(R.id.addItemButton);
        manageCategoriesButton = findViewById(R.id.manageCategoriesButton);
        searchEditText = findViewById(R.id.searchEditText);
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);

        // Setup list adapter
        itemsAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_2, android.R.id.text1, filteredItems) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Item item = getItem(position);

                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                if (item != null) {
                    text1.setText(item.getItemName());
                    String subtitle = item.getCategory();
                    if (item.isAdaFriendly()) {
                        subtitle += " • ADA Friendly";
                    }
                    text2.setText(subtitle);
                }

                return view;
            }
        };
        itemsListView.setAdapter(itemsAdapter);

        // Setup category filter adapter
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        categoryFilterSpinner.setAdapter(categoryAdapter);
    }

    private void setupListeners() {
        // Add item button
        addItemButton.setOnClickListener(v -> showAddItemDialog());

        // Manage categories button
        manageCategoriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(ItemManagementActivity.this, CategoryManagementActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchTerm = s.toString();
                filterItems();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Category filter
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategoryFilter = categories.get(position);
                filterItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Item click listener
        itemsListView.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = filteredItems.get(position);
            showItemOptionsDialog(selectedItem);
        });
    }

    private void loadCategories() {
        categories.clear();
        categories.add("All Categories");
        categories.addAll(categoryDAO.getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }

    private void loadItems() {
        allItems = itemDAO.getAllItems();
        Log.d(TAG, "Loaded " + allItems.size() + " items from database");

        // Debug log to show items
        for (int i = 0; i < Math.min(5, allItems.size()); i++) {
            Item item = allItems.get(i);
            Log.d(TAG, "Item " + i + ": " + item.getName() + " (" + item.getCategory() + ")");
        }

        filterItems();
    }

    private void filterItems() {
        filteredItems.clear();

        for (Item item : allItems) {
            boolean matchesCategory = currentCategoryFilter.equals("All Categories") ||
                    item.getCategory().equals(currentCategoryFilter);

            boolean matchesSearch = currentSearchTerm.isEmpty() ||
                    item.getName().toLowerCase().contains(currentSearchTerm.toLowerCase()) ||
                    item.getCategory().toLowerCase().contains(currentSearchTerm.toLowerCase());

            if (matchesCategory && matchesSearch) {
                filteredItems.add(item);
            }
        }

        itemsAdapter.notifyDataSetChanged();
        updateItemCount();
    }

    private void updateItemCount() {
        itemsCountText.setText("Items: " + filteredItems.size() + " / " + allItems.size());
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.itemNameEditText);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);
        CheckBox adaCheckBox = dialogView.findViewById(R.id.adaFriendlyCheckBox);

        // Setup category spinner (exclude "All Categories")
        List<String> itemCategories = new ArrayList<>(categories);
        itemCategories.remove(0); // Remove "All Categories"
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, itemCategories);
        categorySpinner.setAdapter(spinnerAdapter);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String description = descriptionEditText.getText().toString().trim();
            boolean isAda = adaCheckBox.isChecked();

            if (name.isEmpty()) {
                Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if item already exists
            if (itemDAO.itemExists(name, category)) {
                Toast.makeText(this, "Item already exists in this category", Toast.LENGTH_SHORT).show();
                return;
            }

            Item newItem = new Item();
            newItem.setName(name);
            newItem.setCategory(category);
            newItem.setDescription(description);
            newItem.setIsAdaFriendly(isAda ? 1 : 0);

            long result = itemDAO.insertItem(newItem);
            if (result > 0) {
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                loadItems();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showItemOptionsDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getName())
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditItemDialog(item);
                    } else {
                        showDeleteConfirmation(item);
                    }
                });
        builder.show();
    }

    private void showEditItemDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Item");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.itemNameEditText);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);
        CheckBox adaCheckBox = dialogView.findViewById(R.id.adaFriendlyCheckBox);

        // Set current values
        nameEditText.setText(item.getItemName());
        descriptionEditText.setText(item.getDescription());
        adaCheckBox.setChecked(item.isAdaFriendly());

        // Setup category spinner
        List<String> itemCategories = new ArrayList<>(categories);
        itemCategories.remove(0); // Remove "All Categories"
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, itemCategories);
        categorySpinner.setAdapter(spinnerAdapter);

        // Set current category
        int categoryPosition = itemCategories.indexOf(item.getCategory());
        if (categoryPosition >= 0) {
            categorySpinner.setSelection(categoryPosition);
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String description = descriptionEditText.getText().toString().trim();
            boolean isAda = adaCheckBox.isChecked();

            if (name.isEmpty()) {
                Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            item.setItemName(name);
            item.setCategory(category);
            item.setDescription(description);
            item.setIsAdaFriendly(isAda ? 1 : 0);

            int result = itemDAO.updateItem(item);
            if (result > 0) {
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                loadItems();
            } else {
                Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmation(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete '" + item.getName() + "'?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (itemDAO.deleteItem(item.getItemId())) {
                        Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                        loadItems();
                    } else {
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload categories and items in case they were changed in CategoryManagementActivity
        loadCategories();
        loadItems();
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