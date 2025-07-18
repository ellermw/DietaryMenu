package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    
    private static final String TAG = "AdminActivity";
    
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private UserDAO userDAO;
    
    // FIXED: Enhanced current user state management
    private User currentUser;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // FIXED: Track if launched with direct admin mode
    private boolean wasLaunchedWithDirectMode = false;
    
    // Main UI Components
    private LinearLayout mainMenuContainer;
    private LinearLayout usersContainer;
    private LinearLayout itemsContainer;
    
    // Menu Buttons
    private Button usersMenuButton, itemsMenuButton, backToMenuButton;
    
    // Users Management UI
    private ListView usersListView;
    private EditText userSearchEditText;
    private Button addUserButton;
    private UserAdapter usersAdapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    
    // Items Management UI
    private Spinner categoryFilterSpinner;
    private ListView itemsListView;
    private EditText itemSearchEditText;
    private Button addItemButton;
    private ItemAdapter itemsAdapter;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    
    // FIXED: Updated categories to match database schema
    private List<String> categories = Arrays.asList(
        "All Categories", "Breakfast Items", "Proteins", "Starches", "Vegetables", 
        "Beverages", "Juices", "Desserts", "Fruits", "Dairy"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
        
        Log.d(TAG, "AdminActivity onCreate started");
        
        // FIXED: Get and validate current user from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        Log.d(TAG, "Received user info: " + currentUsername + ", role: " + currentUserRole);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);
        itemDAO = new ItemDAO(dbHelper);
        
        // FIXED: Validate user and admin access
        if (currentUsername == null) {
            Log.e(TAG, "No username provided");
            Toast.makeText(this, "Authentication error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        currentUser = userDAO.getUserByUsername(currentUsername);
        if (currentUser == null) {
            Log.e(TAG, "User not found: " + currentUsername);
            Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        if (!isUserAdmin()) {
            Log.w(TAG, "Non-admin user attempted access: " + currentUsername);
            Toast.makeText(this, "Admin privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // FIXED: Ensure admin menu is configured before any navigation
        configureAdminMenu();
        
        // FIXED: Handle intent extras for direct navigation
        String adminMode = getIntent().getStringExtra("admin_mode");
        boolean showUsers = getIntent().getBooleanExtra("show_users", false);
        boolean showItems = getIntent().getBooleanExtra("show_items", false);
        
        // Track if we were launched directly to a specific mode
        wasLaunchedWithDirectMode = ("users".equals(adminMode) || showUsers || "items".equals(adminMode) || showItems);
        
        if ("users".equals(adminMode) || showUsers) {
            showUsersManagement();
        } else if ("items".equals(adminMode) || showItems) {
            showItemsManagement();
        } else {
            showMainMenu();
        }
        
        Log.d(TAG, "AdminActivity onCreate completed");
    }
    
    private boolean isUserAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }
    
    private void configureAdminMenu() {
        // Set title based on user
        if (currentUser != null) {
            setTitle("Admin Menu - " + currentUser.getFullName());
        } else {
            setTitle("Admin Menu");
        }
    }
    
    private void initializeUI() {
        // Main containers
        mainMenuContainer = findViewById(R.id.mainMenuContainer);
        usersContainer = findViewById(R.id.usersContainer);
        itemsContainer = findViewById(R.id.itemsContainer);
        
        // Menu buttons
        usersMenuButton = findViewById(R.id.usersMenuButton);
        itemsMenuButton = findViewById(R.id.itemsMenuButton);
        backToMenuButton = findViewById(R.id.backToMenuButton);
        
        // Users management UI
        usersListView = findViewById(R.id.usersListView);
        userSearchEditText = findViewById(R.id.userSearchEditText);
        addUserButton = findViewById(R.id.addUserButton);
        
        // Items management UI
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);
        itemsListView = findViewById(R.id.itemsListView);
        itemSearchEditText = findViewById(R.id.itemSearchEditText);
        addItemButton = findViewById(R.id.addItemButton);
        
        // Setup category filter spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);
    }
    
    private void setupListeners() {
        // Menu navigation
        usersMenuButton.setOnClickListener(v -> showUsersManagement());
        itemsMenuButton.setOnClickListener(v -> showItemsManagement());
        backToMenuButton.setOnClickListener(v -> {
            if (wasLaunchedWithDirectMode) {
                finish(); // Go back to MainMenuActivity
            } else {
                showMainMenu(); // Show AdminActivity's main menu
            }
        });
        
        // Users management listeners
        addUserButton.setOnClickListener(v -> showAddUserDialog());
        
        userSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Items management listeners
        addItemButton.setOnClickListener(v -> showAddItemDialog());
        
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterItems();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        itemSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    // FIXED: Add onResume to handle state management
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "AdminActivity onResume called");
        
        // FIXED: Refresh user state and ensure proper access
        if (currentUsername != null) {
            currentUser = userDAO.getUserByUsername(currentUsername);
            if (currentUser != null) {
                currentUserRole = currentUser.getRole();
                currentUserFullName = currentUser.getFullName();
                
                // Verify admin access is still valid
                if (!isUserAdmin()) {
                    Log.w(TAG, "User no longer has admin privileges: " + currentUsername);
                    Toast.makeText(this, "Admin privileges have been revoked.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                
                // Update title
                configureAdminMenu();
            }
        }
    }
    
    private void showMainMenu() {
        Log.d(TAG, "Showing main menu");
        mainMenuContainer.setVisibility(View.VISIBLE);
        usersContainer.setVisibility(View.GONE);
        itemsContainer.setVisibility(View.GONE);
        backToMenuButton.setVisibility(View.GONE);
        
        // FIXED: Ensure admin menu is properly configured when returning to main menu
        configureAdminMenu();
    }
    
    private void showUsersManagement() {
        Log.d(TAG, "Showing users management");
        mainMenuContainer.setVisibility(View.GONE);
        usersContainer.setVisibility(View.VISIBLE);
        itemsContainer.setVisibility(View.GONE);
        backToMenuButton.setVisibility(View.VISIBLE);
        
        loadAllUsers();
    }
    
    private void showItemsManagement() {
        Log.d(TAG, "Showing items management");
        mainMenuContainer.setVisibility(View.GONE);
        usersContainer.setVisibility(View.GONE);
        itemsContainer.setVisibility(View.VISIBLE);
        backToMenuButton.setVisibility(View.VISIBLE);
        
        loadAllItems();
    }
    
    private void loadAllUsers() {
        try {
            allUsers = userDAO.getAllUsers();
            filteredUsers = new ArrayList<>(allUsers);
            
            usersAdapter = new UserAdapter(this, filteredUsers);
            usersListView.setAdapter(usersAdapter);
            
            usersListView.setOnItemClickListener((parent, view, position, id) -> {
                User selectedUser = filteredUsers.get(position);
                showUserDialog(selectedUser);
            });
            
            Log.d(TAG, "Loaded " + allUsers.size() + " users");
        } catch (Exception e) {
            Log.e(TAG, "Error loading users", e);
            showError("Error loading users: " + e.getMessage());
        }
    }
    
    private void loadAllItems() {
        try {
            allItems = itemDAO.getAllItems();
            filteredItems = new ArrayList<>(allItems);
            
            itemsAdapter = new ItemAdapter(this, filteredItems);
            itemsListView.setAdapter(itemsAdapter);
            
            itemsListView.setOnItemClickListener((parent, view, position, id) -> {
                Item selectedItem = filteredItems.get(position);
                showItemDialog(selectedItem);
            });
            
            Log.d(TAG, "Loaded " + allItems.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error loading items", e);
            showError("Error loading items: " + e.getMessage());
            
            // FIXED: Show helpful message if no items found
            if (allItems.isEmpty()) {
                Toast.makeText(this, "No items found. Database may need to be rebuilt.", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void filterUsers(String query) {
        filteredUsers.clear();
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : allUsers) {
                if (user.getFullName().toLowerCase().contains(lowerQuery) || 
                    user.getUsername().toLowerCase().contains(lowerQuery) ||
                    user.getRole().toLowerCase().contains(lowerQuery)) {
                    filteredUsers.add(user);
                }
            }
        }
        if (usersAdapter != null) {
            usersAdapter.notifyDataSetChanged();
        }
    }
    
    private void filterItems() {
        filteredItems.clear();
        String selectedCategory = (String) categoryFilterSpinner.getSelectedItem();
        String searchQuery = itemSearchEditText.getText().toString().toLowerCase();
        
        for (Item item : allItems) {
            boolean matchesCategory = "All Categories".equals(selectedCategory) || 
                                    (item.getCategoryName() != null && item.getCategoryName().equals(selectedCategory));
            boolean matchesSearch = searchQuery.isEmpty() || 
                                  item.getName().toLowerCase().contains(searchQuery);
            
            if (matchesCategory && matchesSearch) {
                filteredItems.add(item);
            }
        }
        
        if (itemsAdapter != null) {
            itemsAdapter.notifyDataSetChanged();
        }
    }
    
    private void showAddUserDialog() {
        showUserDialog(null); // null means add new user
    }
    
    private void showAddItemDialog() {
        showItemDialog(null); // null means add new item
    }
    
    private int getCategoryId(String categoryName) {
        // Map category names to IDs based on database schema
        switch (categoryName) {
            case "Breakfast Items": return 1;
            case "Proteins": return 2;
            case "Starches": return 3;
            case "Vegetables": return 4;
            case "Beverages": return 5;
            case "Juices": return 6;
            case "Desserts": return 7;
            case "Fruits": return 8;
            case "Dairy": return 9;
            default: return 1; // Default to Breakfast Items
        }
    }
    
    // FIXED: Complete implementation of showUserDialog for both add and edit
    private void showUserDialog(User user) {
        boolean isEdit = (user != null);
        String title = isEdit ? "Edit User" : "Add New User";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        builder.setView(dialogView);
        
        EditText usernameET = dialogView.findViewById(R.id.usernameEditText);
        EditText passwordET = dialogView.findViewById(R.id.passwordEditText);
        EditText fullNameET = dialogView.findViewById(R.id.fullNameEditText);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        
        // Setup role spinner
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        
        // If editing, populate existing data
        if (isEdit) {
            usernameET.setText(user.getUsername());
            usernameET.setEnabled(false); // Don't allow username changes
            passwordET.setHint("Leave blank to keep current password");
            fullNameET.setText(user.getFullName());
            
            // Set role spinner
            int rolePosition = Arrays.asList(roles).indexOf(user.getRole());
            if (rolePosition >= 0) {
                roleSpinner.setSelection(rolePosition);
            }
        }
        
        builder.setPositiveButton(isEdit ? "Update User" : "Add User", (dialog, which) -> {
            String username = usernameET.getText().toString().trim();
            String password = passwordET.getText().toString().trim();
            String fullName = fullNameET.getText().toString().trim();
            String role = (String) roleSpinner.getSelectedItem();
            
            // Validation
            if (username.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Username and full name are required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!isEdit && password.isEmpty()) {
                Toast.makeText(this, "Password is required for new users", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if we're trying to demote the last admin
            if (isEdit && "admin".equals(user.getRole()) && !"admin".equals(role)) {
                long adminCount = userDAO.getAdminUserCount();
                if (adminCount <= 1) {
                    Toast.makeText(this, "Cannot remove admin role - at least one admin must remain", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            
            long result;
            if (isEdit) {
                // Update existing user
                user.setFullName(fullName);
                user.setRole(role);
                if (!password.isEmpty()) {
                    user.setPassword(password); // Only update password if provided
                }
                result = userDAO.updateUser(user);
            } else {
                // Create new user
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                newUser.setFullName(fullName);
                newUser.setRole(role);
                newUser.setActive(true);
                result = userDAO.addUser(newUser);
            }
            
            if (result > 0) {
                Toast.makeText(this, isEdit ? "User updated successfully" : "User added successfully", Toast.LENGTH_SHORT).show();
                loadAllUsers(); // Refresh the list
            } else {
                Toast.makeText(this, isEdit ? "Error updating user" : "Error adding user (username may already exist)", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        // Add delete button for existing users
        if (isEdit) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                confirmDeleteUser(user);
            });
        }
        
        builder.show();
    }
    
    private void showItemDialog(Item item) {
        boolean isEdit = (item != null);
        String title = isEdit ? "Edit Item" : "Add New Item";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);
        
        EditText nameET = dialogView.findViewById(R.id.itemNameEditText);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText descriptionET = dialogView.findViewById(R.id.descriptionEditText);
        EditText sizeMlET = dialogView.findViewById(R.id.sizeMlEditText);
        CheckBox adaFriendlyCB = dialogView.findViewById(R.id.adaFriendlyCheckBox);
        CheckBox isSodaCB = dialogView.findViewById(R.id.isSodaCheckBox);
        CheckBox isClearLiquidCB = dialogView.findViewById(R.id.isClearLiquidCheckBox);
        CheckBox isDefaultCB = dialogView.findViewById(R.id.isDefaultCheckBox);
        
        // Setup category spinner (excluding "All Categories")
        List<String> itemCategories = new ArrayList<>(categories);
        itemCategories.remove("All Categories");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // If editing, populate existing data
        if (isEdit) {
            nameET.setText(item.getName());
            
            if (item.getCategoryName() != null) {
                int categoryPosition = itemCategories.indexOf(item.getCategoryName());
                if (categoryPosition >= 0) {
                    categorySpinner.setSelection(categoryPosition);
                }
            }
            
            if (item.getDescription() != null) {
                descriptionET.setText(item.getDescription());
            }
            
            if (item.getSizeML() != null && item.getSizeML() > 0) {
                sizeMlET.setText(String.valueOf(item.getSizeML()));
            }
            
            adaFriendlyCB.setChecked(item.isAdaFriendly());
            isSodaCB.setChecked(item.isSoda());
            isClearLiquidCB.setChecked(item.isClearLiquid());
            isDefaultCB.setChecked(item.isDefault());
        }
        
        builder.setPositiveButton(isEdit ? "Update Item" : "Add Item", (dialog, which) -> {
            String name = nameET.getText().toString().trim();
            String category = (String) categorySpinner.getSelectedItem();
            String description = descriptionET.getText().toString().trim();
            String sizeStr = sizeMlET.getText().toString().trim();
            boolean adaFriendly = adaFriendlyCB.isChecked();
            boolean isSoda = isSodaCB.isChecked();
            boolean isClearLiquid = isClearLiquidCB.isChecked();
            boolean isDefault = isDefaultCB.isChecked();
            
            if (name.isEmpty() || category == null) {
                Toast.makeText(this, "Name and category are required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            long result;
            if (isEdit) {
                // Update existing item
                item.setName(name);
                item.setCategoryId(getCategoryId(category));
                item.setDescription(description.isEmpty() ? null : description);
                item.setAdaFriendly(adaFriendly);
                item.setSoda(isSoda);
                item.setClearLiquid(isClearLiquid);
                item.setDefault(isDefault);
                
                if (!sizeStr.isEmpty()) {
                    try {
                        item.setSizeML(Integer.parseInt(sizeStr));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid size value", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    item.setSizeML(null);
                }
                
                result = itemDAO.updateItem(item);
            } else {
                // Create new item
                Item newItem = new Item();
                newItem.setName(name);
                newItem.setCategoryId(getCategoryId(category));
                newItem.setDescription(description.isEmpty() ? null : description);
                newItem.setAdaFriendly(adaFriendly);
                newItem.setSoda(isSoda);
                newItem.setClearLiquid(isClearLiquid);
                newItem.setDefault(isDefault);
                newItem.setMealType("General");
                
                if (!sizeStr.isEmpty()) {
                    try {
                        newItem.setSizeML(Integer.parseInt(sizeStr));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid size value", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                
                result = itemDAO.addItem(newItem);
            }
            
            if (result > 0) {
                Toast.makeText(this, isEdit ? "Item updated successfully" : "Item added successfully", Toast.LENGTH_SHORT).show();
                loadAllItems(); // Refresh the list
            } else {
                Toast.makeText(this, isEdit ? "Error updating item" : "Error adding item", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        // Add delete button for existing items
        if (isEdit) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                confirmDeleteItem(item);
            });
        }
        
        builder.show();
    }
    
    private void confirmDeleteUser(User user) {
        // Prevent deleting the last admin
        if ("admin".equals(user.getRole())) {
            long adminCount = userDAO.getAdminUserCount();
            if (adminCount <= 1) {
                Toast.makeText(this, "Cannot delete the last admin user", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        // Prevent users from deleting themselves
        if (user.getUsername().equals(currentUsername)) {
            Toast.makeText(this, "You cannot delete your own account", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete user '" + user.getUsername() + "'?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean success = userDAO.deleteUser(user.getUserId());
                if (success) {
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAllUsers(); // Refresh the list
                } else {
                    Toast.makeText(this, "Error deleting user", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void confirmDeleteItem(Item item) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete item '" + item.getName() + "'?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean success = itemDAO.deleteItem(item.getItemId());
                if (success) {
                    Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAllItems(); // Refresh the list
                } else {
                    Toast.makeText(this, "Error deleting item (may be in use)", Toast.LENGTH_SHORT).show();
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
    
    // FIXED: Override onBackPressed to handle proper navigation
    @Override
    public void onBackPressed() {
        // Check if we're currently showing users or items management
        if (usersContainer.getVisibility() == View.VISIBLE || itemsContainer.getVisibility() == View.VISIBLE) {
            if (wasLaunchedWithDirectMode) {
                // Go back to MainMenuActivity
                finish();
            } else {
                // Show AdminActivity's main menu
                showMainMenu();
            }
        } else {
            // We're on the main admin menu, so go back to MainMenuActivity
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        Log.d(TAG, "AdminActivity destroyed");
    }
    
    // Custom adapter for users list
    private class UserAdapter extends ArrayAdapter<User> {
        public UserAdapter(Context context, List<User> users) {
            super(context, android.R.layout.simple_list_item_2, users);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            
            User user = getItem(position);
            if (user != null) {
                TextView text1 = convertView.findViewById(android.R.id.text1);
                TextView text2 = convertView.findViewById(android.R.id.text2);
                
                text1.setText(user.getFullName() + " (" + user.getUsername() + ")");
                String status = user.isActive() ? "Active" : "Inactive";
                text2.setText(user.getRole() + " - " + status);
            }
            
            return convertView;
        }
    }
    
    // Custom adapter for items list - FIXED: Handle 0ml display properly
    private class ItemAdapter extends ArrayAdapter<Item> {
        public ItemAdapter(Context context, List<Item> items) {
            super(context, android.R.layout.simple_list_item_2, items);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            
            Item item = getItem(position);
            if (item != null) {
                TextView text1 = convertView.findViewById(android.R.id.text1);
                TextView text2 = convertView.findViewById(android.R.id.text2);
                
                text1.setText(item.getName());
                String details = (item.getCategoryName() != null ? item.getCategoryName() : "Unknown Category");
                if (item.isAdaFriendly()) {
                    details += " • ADA Friendly";
                }
                // FIXED: Only show ml if it's greater than 0
                if (item.getSizeML() != null && item.getSizeML() > 0) {
                    details += " • " + item.getSizeML() + "ml";
                }
                text2.setText(details);
            }
            
            return convertView;
        }
    }
}