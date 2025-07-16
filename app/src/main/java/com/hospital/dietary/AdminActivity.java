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
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.Item;
import com.hospital.dietary.models.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private UserDAO userDAO;
    
    // Current user
    private int currentUserId;
    
    // UI Components
    private TabHost tabHost;
    
    // Users tab components
    private ListView usersListView;
    private Button addUserButton;
    private EditText userSearchEditText;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private ArrayAdapter<User> usersAdapter;
    
    // Items tab components
    private Spinner categoryFilterSpinner;
    private ListView itemsListView;
    private EditText itemSearchEditText;
    private Button addItemButton;
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
        setContentView(R.layout.activity_admin_new);
        
        // Get current user ID
        currentUserId = getIntent().getIntExtra("current_user_id", -1);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load data
        loadAllUsers();
        loadAllItems();
    }
    
    private void initializeUI() {
        // Setup TabHost
        tabHost = findViewById(android.R.id.tabhost);
        tabHost.setup();
        
        // Users Tab
        TabHost.TabSpec usersTab = tabHost.newTabSpec("users");
        usersTab.setContent(R.id.tab_users);
        usersTab.setIndicator("👥 Users");
        tabHost.addTab(usersTab);
        
        // Items Tab
        TabHost.TabSpec itemsTab = tabHost.newTabSpec("items");
        itemsTab.setContent(R.id.tab_items);
        itemsTab.setIndicator("🍽️ Items");
        tabHost.addTab(itemsTab);
        
        // Initialize Users tab components
        usersListView = findViewById(R.id.usersListView);
        addUserButton = findViewById(R.id.addUserButton);
        userSearchEditText = findViewById(R.id.userSearchEditText);
        
        // Initialize Items tab components
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);
        itemsListView = findViewById(R.id.itemsListView);
        itemSearchEditText = findViewById(R.id.itemSearchEditText);
        addItemButton = findViewById(R.id.addItemButton);
        
        // Setup adapters
        usersAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_2, 
                android.R.id.text1, filteredUsers) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                User user = filteredUsers.get(position);
                
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                
                text1.setText(user.getUsername() + " (" + user.getRole() + ")");
                String status = user.isActive() ? "Active" : "Inactive";
                String lastLogin = user.getLastLogin() != null ? user.getLastLogin() : "Never";
                text2.setText("Status: " + status + " | Last Login: " + lastLogin);
                
                return view;
            }
        };
        usersListView.setAdapter(usersAdapter);
        
        itemsAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_2, 
                android.R.id.text1, filteredItems) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Item item = filteredItems.get(position);
                
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                
                text1.setText(item.getName());
                String details = "Category: " + item.getCategoryName();
                if (item.getSizeML() != null) {
                    details += " | Size: " + item.getSizeML() + "ml";
                }
                if (!item.isAdaFriendly()) {
                    details += " | Non-ADA";
                }
                text2.setText(details);
                
                return view;
            }
        };
        itemsListView.setAdapter(itemsAdapter);
        
        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);
    }
    
    private void setupListeners() {
        // Users tab listeners
        addUserButton.setOnClickListener(v -> showUserDialog(null));
        
        userSearchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        usersListView.setOnItemClickListener((parent, view, position, id) -> {
            User user = filteredUsers.get(position);
            showUserOptionsDialog(user);
        });
        
        // Items tab listeners
        addItemButton.setOnClickListener(v -> showItemDialog(null));
        
        itemSearchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterItems();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        itemsListView.setOnItemClickListener((parent, view, position, id) -> {
            Item item = filteredItems.get(position);
            showItemOptionsDialog(item);
        });
    }
    
    // User Management Methods
    
    private void loadAllUsers() {
        allUsers.clear();
        allUsers.addAll(userDAO.getAllUsers());
        filterUsers();
    }
    
    private void filterUsers() {
        filteredUsers.clear();
        
        String searchQuery = userSearchEditText.getText().toString().toLowerCase().trim();
        
        for (User user : allUsers) {
            if (searchQuery.isEmpty() || 
                user.getUsername().toLowerCase().contains(searchQuery) ||
                user.getRole().toLowerCase().contains(searchQuery)) {
                filteredUsers.add(user);
            }
        }
        
        usersAdapter.notifyDataSetChanged();
    }
    
    private void showUserDialog(User user) {
        boolean isEdit = (user != null);
        String title = isEdit ? "Edit User" : "Add New User";
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_form, null);
        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        CheckBox activeCheckBox = dialogView.findViewById(R.id.activeCheckBox);
        TextView passwordLabel = dialogView.findViewById(R.id.passwordLabel);
        
        // Setup role spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, Arrays.asList("User", "Admin"));
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        
        if (isEdit) {
            usernameInput.setText(user.getUsername());
            passwordLabel.setText("New Password (leave blank to keep current):");
            roleSpinner.setSelection(user.getRole().equals("Admin") ? 1 : 0);
            activeCheckBox.setChecked(user.isActive());
        } else {
            activeCheckBox.setChecked(true);
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                if (saveUser(user, usernameInput, passwordInput, roleSpinner, activeCheckBox)) {
                    dialog.dismiss();
                    loadAllUsers();
                }
            });
        });
        
        dialog.show();
    }
    
    private boolean saveUser(User existingUser, EditText usernameInput, EditText passwordInput, 
                           Spinner roleSpinner, CheckBox activeCheckBox) {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String role = (String) roleSpinner.getSelectedItem();
        boolean isActive = activeCheckBox.isChecked();
        
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            return false;
        }
        
        boolean isEdit = (existingUser != null);
        
        if (!isEdit && password.isEmpty()) {
            passwordInput.setError("Password is required for new users");
            return false;
        }
        
        boolean success;
        
        if (isEdit) {
            existingUser.setUsername(username);
            existingUser.setRole(role);
            existingUser.setActive(isActive);
            
            if (!password.isEmpty()) {
                success = userDAO.updateUserPassword(existingUser.getUserId(), password);
                if (success) {
                    success = userDAO.updateUser(existingUser);
                }
            } else {
                success = userDAO.updateUser(existingUser);
            }
        } else {
            success = userDAO.createUser(username, password, role);
        }
        
        if (success) {
            String message = isEdit ? "User updated successfully" : "User created successfully";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            String errorMessage = isEdit ? "Failed to update user" : "Failed to create user (username may already exist)";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    private void showUserOptionsDialog(User user) {
        String[] options = {"Edit", "Delete"};
        
        new AlertDialog.Builder(this)
            .setTitle("User: " + user.getUsername())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Edit
                        showUserDialog(user);
                        break;
                    case 1: // Delete
                        confirmDeleteUser(user);
                        break;
                }
            })
            .show();
    }
    
    private void confirmDeleteUser(User user) {
        if (user.getUserId() == currentUserId) {
            Toast.makeText(this, "Cannot delete your own account", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete user '" + user.getUsername() + "'?\n\n" +
                       "This will deactivate the user account.")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (userDAO.deleteUser(user.getUserId())) {
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAllUsers();
                } else {
                    Toast.makeText(this, "Cannot delete user (may be the last admin)", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // Item Management Methods (similar to existing code)
    
    private void loadAllItems() {
        allItems.clear();
        
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
        String searchQuery = itemSearchEditText.getText().toString().toLowerCase().trim();
        
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
    
    private void showItemDialog(Item item) {
        boolean isEdit = (item != null);
        String title = isEdit ? "Edit Item" : "Add New Item";
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_item_form, null);
        EditText nameInput = dialogView.findViewById(R.id.itemNameInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.itemCategorySpinner);
        EditText sizeInput = dialogView.findViewById(R.id.itemSizeInput);
        CheckBox adaCheckBox = dialogView.findViewById(R.id.adaFriendlyCheckBox);
        CheckBox sodaCheckBox = dialogView.findViewById(R.id.sodaCheckBox);
        
        // Setup category spinner (excluding "All Categories")
        List<String> itemCategories = new ArrayList<>(categories);
        itemCategories.remove("All Categories");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, itemCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        if (isEdit) {
            nameInput.setText(item.getName());
            if (item.getSizeML() != null) {
                sizeInput.setText(String.valueOf(item.getSizeML()));
            }
            adaCheckBox.setChecked(item.isAdaFriendly());
            sodaCheckBox.setChecked(item.isSoda());
            
            int categoryPosition = itemCategories.indexOf(item.getCategoryName());
            if (categoryPosition >= 0) {
                categorySpinner.setSelection(categoryPosition);
            }
        } else {
            adaCheckBox.setChecked(true);
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                if (saveItem(item, nameInput, categorySpinner, sizeInput, adaCheckBox, sodaCheckBox)) {
                    dialog.dismiss();
                    loadAllItems();
                }
            });
        });
        
        dialog.show();
    }
    
    private boolean saveItem(Item existingItem, EditText nameInput, Spinner categorySpinner, 
                           EditText sizeInput, CheckBox adaCheckBox, CheckBox sodaCheckBox) {
        String name = nameInput.getText().toString().trim();
        String category = (String) categorySpinner.getSelectedItem();
        String sizeText = sizeInput.getText().toString().trim();
        boolean isAdaFriendly = adaCheckBox.isChecked();
        boolean isSoda = sodaCheckBox.isChecked();
        
        if (name.isEmpty()) {
            nameInput.setError("Item name is required");
            return false;
        }
        
        Integer sizeML = null;
        if (!sizeText.isEmpty()) {
            try {
                sizeML = Integer.parseInt(sizeText);
            } catch (NumberFormatException e) {
                sizeInput.setError("Size must be a valid number");
                return false;
            }
        }
        
        boolean success;
        boolean isEdit = (existingItem != null);
        
        if (isEdit) {
            existingItem.setName(name);
            existingItem.setCategoryId(getCategoryId(category));
            existingItem.setCategoryName(category);
            existingItem.setSizeML(sizeML);
            existingItem.setAdaFriendly(isAdaFriendly);
            existingItem.setSoda(isSoda);
            
            success = itemDAO.updateItem(existingItem) > 0;
        } else {
            Item newItem = new Item();
            newItem.setName(name);
            newItem.setCategoryId(getCategoryId(category));
            newItem.setCategoryName(category);
            newItem.setSizeML(sizeML);
            newItem.setAdaFriendly(isAdaFriendly);
            newItem.setSoda(isSoda);
            
            success = itemDAO.insertItem(newItem) > 0;
        }
        
        if (success) {
            String message = isEdit ? "Item updated successfully" : "Item added successfully";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    private int getCategoryId(String categoryName) {
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
    
    private void showItemOptionsDialog(Item item) {
        String[] options = {"Edit", "Delete"};
        
        new AlertDialog.Builder(this)
            .setTitle("Item: " + item.getName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Edit
                        showItemDialog(item);
                        break;
                    case 1: // Delete
                        confirmDeleteItem(item);
                        break;
                }
            })
            .show();
    }
    
    private void confirmDeleteItem(Item item) {
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
                    Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
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