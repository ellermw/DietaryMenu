package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.models.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private ListView itemsListView;
    private TextView itemsCountText;
    private Button addItemButton;
    private Button refreshButton;
    private Button backButton;

    // Data
    private List<Item> allItems = new ArrayList<>();
    private ArrayAdapter<Item> itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_management);

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

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Item Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI
        initializeViews();
        setupListeners();
        loadItems();
    }

    private void initializeViews() {
        itemsListView = findViewById(R.id.itemsListView);
        itemsCountText = findViewById(R.id.itemsCountText);
        addItemButton = findViewById(R.id.addItemButton);
        refreshButton = findViewById(R.id.refreshButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        addItemButton.setOnClickListener(v -> showAddItemDialog());
        refreshButton.setOnClickListener(v -> loadItems());

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Item list click
        itemsListView.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = allItems.get(position);
            showItemOptionsDialog(selectedItem);
        });
    }

    private void loadItems() {
        try {
            allItems = itemDAO.getAllItems();

            // Create adapter
            itemsAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_2, android.R.id.text1, allItems) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    Item item = allItems.get(position);
                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);

                    text1.setText(item.getName());
                    String details = "Category: " + item.getCategory();
                    if (item.isAdaFriendly()) {
                        details += " | ADA Friendly";
                    }
                    if (item.getSizeML() != null && item.getSizeML() > 0) {
                        details += " | " + item.getSizeML() + "ml";
                    }
                    text2.setText(details);

                    return view;
                }
            };

            itemsListView.setAdapter(itemsAdapter);
            itemsCountText.setText("Total Items: " + allItems.size());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading items: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddItemDialog() {
        // Create a simple dialog with basic fields
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name");
        layout.addView(nameInput);

        final Spinner categorySpinner = new Spinner(this);
        String[] categories = {"Proteins", "Starches", "Vegetables", "Beverages", "Fruits", "Desserts", "Breakfast Items", "Condiments"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
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
        String[] categories = {"Proteins", "Starches", "Vegetables", "Beverages", "Fruits", "Desserts", "Breakfast Items", "Condiments"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set current category
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(item.getCategory())) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}