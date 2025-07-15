// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/AdminActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.models.Item;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    
    // UI Components
    private Spinner categoryFilterSpinner;
    private ListView itemsListView;
    private EditText searchEditText;
    private Button addItemButton, backButton;
    
    // Data
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private ArrayAdapter<Item> itemsAdapter;
    
    private List<String> categories = Arrays.asList(
        "All Categories", "Breakfast", "Protein/Entrée", "Starch", "Vegetable", 
        "Grill Item", "Dessert", "Sugar Free Dessert", "Drink", "Supplement", 
        "Soda", "Juices", "Cold Cereals", "Hot Cereals", "Breads", "Fresh Muffins", "Fruits"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load data
        loadAllItems();
    }
    
    private void initializeUI() {
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);
        itemsListView = findViewById(R.id.itemsListView);
        searchEditText = findViewById(R.id.searchEditText);
        addItemButton = findViewById(R.id.addItemButton);
        backButton = findViewById(R.id.backButton);
        
        // Setup category filter spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);
        
        // Setup items list adapter
        itemsAdapter = new ArrayAdapter<Item>(this, R.layout.item_admin_row, filteredItems) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_admin_row, parent, false);
                }
                
                Item item = getItem(position);
                
                TextView itemName = convertView.findViewById(R.id.itemNameText);
                TextView itemDetails = convertView.findViewById(R.id.itemDetailsText);
                Button editButton = convertView.findViewById(R.id.editItemButton);
                Button deleteButton = convertView.findViewById(R.id.deleteItemButton);
                
                itemName.setText(item.getName());
                
                String details = item.getCategoryName();
                if (item.getSizeML() != null) {
                    details += " • " + item.getSizeML() + "ml";
                }
                if (item.isAdaFriendly()) {
                    details += " • ADA Friendly";
                }
                if (item.isSoda()) {
                    details += " • Soda";
                }
                itemDetails.setText(details);
                
                editButton.setOnClickListener(v -> editItem(item));
                deleteButton.setOnClickListener(v -> deleteItem(item));
                
                return convertView;
            }
        };
        
        itemsListView.setAdapter(itemsAdapter);
    }
    
    private void setupListeners() {
        // Category filter listener
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterItems();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Search listener
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // Button listeners
        addItemButton.setOnClickListener(v -> addNewItem());
        backButton.setOnClickListener(v -> finish());
    }
    
    private void loadAllItems() {
        allItems.clear();
        
        // Load items from all categories
        for (String category : categories) {
            if (!category.equals("All Categories")) {
                List<Item> categoryItems = itemDAO.getItemsByCategory(category);
                allItems.addAll(categoryItems);
            }
        }
        
        filterItems();
    }
    
    private void filterItems() {
        filteredItems.clear();
        
        String selectedCategory = (String) categoryFilterSpinner.getSelectedItem();
        String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
        
        for (Item item : allItems) {
            // Category filter
            if (!selectedCategory.equals("All Categories") && 
                !item.getCategoryName().equals(selectedCategory)) {
                continue;
            }
            
            // Search filter
            if (!searchQuery.isEmpty() && 
                !item.getName().toLowerCase().contains(searchQuery)) {
                continue;
            }
            
            filteredItems.add(item);
        }
        
        itemsAdapter.notifyDataSetChanged();
    }
    
    private void addNewItem() {
        showItemDialog(null);
    }
    
    private void editItem(Item item) {
        showItemDialog(item);
    }
    
    private void showItemDialog(Item item) {
        boolean isEdit = (item != null);
        String title = isEdit ? "Edit Item" : "Add New Item";
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_item, null);
        
        EditText nameInput = dialogView.findViewById(R.id.itemNameInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.itemCategorySpinner);
        EditText sizeInput = dialogView.findViewById(R.id.itemSizeInput);
        CheckBox adaCheckBox = dialogView.findViewById(R.id.itemAdaCheckBox);
        CheckBox sodaCheckBox = dialogView.findViewById(R.id.itemSodaCheckBox);
        
        // Setup category spinner
        List<String> dialogCategories = new ArrayList<>(categories);
        dialogCategories.remove("All Categories");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, dialogCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Pre-fill if editing
        if (isEdit) {
            nameInput.setText(item.getName());
            
            // Set category
            for (int i = 0; i < dialogCategories.size(); i++) {
                if (dialogCategories.get(i).equals(item.getCategoryName())) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
            
            if (item.getSizeML() != null) {
                sizeInput.setText(String.valueOf(item.getSizeML()));
            }
            
            adaCheckBox.setChecked(item.isAdaFriendly());
            sodaCheckBox.setChecked(item.isSoda());
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set to null initially
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                if (validateAndSaveItem(nameInput, categorySpinner, sizeInput, 
                                      adaCheckBox, sodaCheckBox, item, isEdit)) {
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
    }
    
    private boolean validateAndSaveItem(EditText nameInput, Spinner categorySpinner, 
                                      EditText sizeInput, CheckBox adaCheckBox, 
                                      CheckBox sodaCheckBox, Item existingItem, boolean isEdit) {
        
        String name = nameInput.getText().toString().trim();
        String category = (String) categorySpinner.getSelectedItem();
        String sizeText = sizeInput.getText().toString().trim();
        
        // Validation
        if (name.isEmpty()) {
            showError("Item name is required");
            return false;
        }
        
        if (category == null) {
            showError("Please select a category");
            return false;
        }
        
        Integer sizeML = null;
        if (!sizeText.isEmpty()) {
            try {
                sizeML = Integer.parseInt(sizeText);
                if (sizeML <= 0) {
                    showError("Size must be a positive number");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid size in ml");
                return false;
            }
        }
        
        // Check for duplicate names (except when editing the same item)
        for (Item item : allItems) {
            if (item.getName().equalsIgnoreCase(name) && 
                (!isEdit || item.getItemId() != existingItem.getItemId())) {
                showError("An item with this name already exists");
                return false;
            }
        }
        
        // Create/update item
        Item item = isEdit ? existingItem : new Item();
        item.setName(name);
        item.setSizeML(sizeML);
        item.setAdaFriendly(adaCheckBox.isChecked());
        item.setSoda(sodaCheckBox.isChecked());
        
        // Get category ID
        int categoryId = getCategoryId(category);
        item.setCategoryId(categoryId);
        item.setCategoryName(category);
        
        // Save to database
        long result;
        if (isEdit) {
            result = itemDAO.updateItem(item);
        } else {
            result = itemDAO.addItem(item);
            if (result > 0) {
                item.setItemId((int) result);
            }
        }
        
        if (result > 0) {
            if (!isEdit) {
                allItems.add(item);
            }
            filterItems();
            
            String message = isEdit ? "Item updated successfully" : "Item added successfully";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            showError("Failed to save item. Please try again.");
            return false;
        }
    }
    
    private int getCategoryId(String categoryName) {
        // Map category names to IDs based on your database structure
        switch (categoryName) {
            case "Breakfast": return 1;
            case "Protein/Entrée": return 2;
            case "Starch": return 3;
            case "Vegetable": return 4;
            case "Grill Item": return 5;
            case "Dessert": return 6;
            case "Sugar Free Dessert": return 7;
            case "Drink": return 8;
            case "Supplement": return 9;
            case "Soda": return 10;
            case "Juices": return 11;
            case "Cold Cereals": return 12;
            case "Hot Cereals": return 13;
            case "Breads": return 14;
            case "Fresh Muffins": return 15;
            case "Fruits": return 16;
            default: return 1;
        }
    }
    
    private void deleteItem(Item item) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '" + item.getName() + "'?\n\n" +
                       "This action cannot be undone and may affect existing meal orders.")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (itemDAO.deleteItem(item.getItemId())) {
                    allItems.remove(item);
                    filterItems();
                    Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Failed to delete item. It may be used in existing orders.");
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showError(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}