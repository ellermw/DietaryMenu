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
    private ArrayAdapter<Item> itemsAdapter;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    
    private List<String> categories = Arrays.asList(
        "All Categories", "Breakfast", "Protein/Entrée", "Starch", "Vegetable", 
        "Grill Item", "Dessert", "Sugar Free Dessert", "Drink", "Supplement", 
        "Soda", "Juices", "Cold Cereals", "Hot Cereals", "Breads", "Fresh Muffins", "Fruits"
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
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);
        
        // FIXED: Get current user details BEFORE initializing UI
        if (currentUsername != null) {
            currentUser = userDAO.getUserByUsername(currentUsername);
            if (currentUser != null) {
                currentUserRole = currentUser.getRole();
                currentUserFullName = currentUser.getFullName();
                Log.d(TAG, "Current user loaded: " + currentUsername + " - Role: " + currentUserRole);
            } else {
                Log.e(TAG, "User not found in database: " + currentUsername);
                redirectToLogin();
                return;
            }
        } else {
            Log.e(TAG, "No username provided in intent");
            redirectToLogin();
            return;
        }
        
        // FIXED: Verify admin access
        if (!isUserAdmin()) {
            Log.w(TAG, "Non-admin user attempting to access admin panel: " + currentUsername);
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // FIXED: Ensure admin menu is configured before any navigation
        configureAdminMenu();
        
        // Handle intent extras for direct navigation
        boolean showUsers = getIntent().getBooleanExtra("show_users", false);
        boolean showItems = getIntent().getBooleanExtra("show_items", false);
        
        if (showUsers) {
            showUsersManagement();
        } else if (showItems) {
            showItemsManagement();
        } else {
            showMainMenu();
        }
        
        Log.d(TAG, "AdminActivity onCreate completed");
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
                
                // Restore admin menu if we're on the main screen
                if (mainMenuContainer.getVisibility() == View.VISIBLE) {
                    configureAdminMenu();
                }
            } else {
                Log.e(TAG, "User no longer exists: " + currentUsername);
                redirectToLogin();
                return;
            }
        }
        
        Log.d(TAG, "AdminActivity onResume completed - User: " + currentUsername);
    }

    // FIXED: Add helper methods for admin state management
    private boolean isUserAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    // FIXED: Enhanced admin menu configuration
    private void configureAdminMenu() {
        Log.d(TAG, "Configuring admin menu for user: " + currentUsername);
        
        // Ensure admin buttons are visible for admin users
        if (usersMenuButton != null) {
            usersMenuButton.setVisibility(View.VISIBLE);
            Log.d(TAG, "Users menu button made visible");
        }
        if (itemsMenuButton != null) {
            itemsMenuButton.setVisibility(View.VISIBLE);
            Log.d(TAG, "Items menu button made visible");
        }
    }
    
    // FIXED: Redirect to login if session is invalid
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeUI() {
        mainMenuContainer = findViewById(R.id.mainMenuContainer);
        usersContainer = findViewById(R.id.usersContainer);
        itemsContainer = findViewById(R.id.itemsContainer);
        
        usersMenuButton = findViewById(R.id.usersMenuButton);
        itemsMenuButton = findViewById(R.id.itemsMenuButton);
        backToMenuButton = findViewById(R.id.backToMenuButton);
        
        // Users Management UI
        usersListView = findViewById(R.id.usersListView);
        userSearchEditText = findViewById(R.id.userSearchEditText);
        addUserButton = findViewById(R.id.addUserButton);
        
        // Items Management UI
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);
        itemsListView = findViewById(R.id.itemsListView);
        itemSearchEditText = findViewById(R.id.itemSearchEditText);
        addItemButton = findViewById(R.id.addItemButton);
    }
    
    private void setupListeners() {
        usersMenuButton.setOnClickListener(v -> showUsersManagement());
        itemsMenuButton.setOnClickListener(v -> showItemsManagement());
        backToMenuButton.setOnClickListener(v -> showMainMenu());
        
        addUserButton.setOnClickListener(v -> showUserDialog(null));
        addItemButton.setOnClickListener(v -> showItemDialog(null));
        
        // User search functionality
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
        
        // Item search functionality
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
        
        // Category filter for items
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterItems();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
            
            itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredItems);
            itemsListView.setAdapter(itemsAdapter);
            
            itemsListView.setOnItemClickListener((parent, view, position, id) -> {
                Item selectedItem = filteredItems.get(position);
                showItemDialog(selectedItem);
            });
            
            Log.d(TAG, "Loaded " + allItems.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error loading items", e);
            showError("Error loading items: " + e.getMessage());
        }
    }
    
    private void filterUsers(String searchText) {
        filteredUsers.clear();
        
        if (searchText.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String searchLower = searchText.toLowerCase();
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(searchLower) ||
                    user.getFullName().toLowerCase().contains(searchLower) ||
                    user.getRole().toLowerCase().contains(searchLower)) {
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
        String searchText = itemSearchEditText.getText().toString();
        
        for (Item item : allItems) {
            // Category filter
            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                if (!selectedCategory.equals(item.getCategoryName())) {
                    continue;
                }
            }
            
            // Search filter
            if (!searchText.isEmpty()) {
                String searchLower = searchText.toLowerCase();
                if (!item.getName().toLowerCase().contains(searchLower) &&
                    !item.getCategoryName().toLowerCase().contains(searchLower)) {
                    continue;
                }
            }
            
            filteredItems.add(item);
        }
        
        if (itemsAdapter != null) {
            itemsAdapter.notifyDataSetChanged();
        }
    }
    
    private void showUserDialog(User user) {
        boolean isEdit = user != null;
        String title = isEdit ? "Edit User" : "Add New User";
        
        // Create dialog layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        // Username input
        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        if (isEdit) {
            usernameInput.setText(user.getUsername());
            usernameInput.setEnabled(false); // Don't allow username changes
        }
        layout.addView(usernameInput);
        
        // Password input
        EditText passwordInput = new EditText(this);
        passwordInput.setHint(isEdit ? "New Password (leave blank to keep current)" : "Password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);
        
        // Full name input
        EditText fullNameInput = new EditText(this);
        fullNameInput.setHint("Full Name");
        if (isEdit) fullNameInput.setText(user.getFullName());
        layout.addView(fullNameInput);
        
        // Email input
        EditText emailInput = new EditText(this);
        emailInput.setHint("Email (optional)");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (isEdit && user.getEmail() != null) emailInput.setText(user.getEmail());
        layout.addView(emailInput);
        
        // Role spinner
        Spinner roleSpinner = new Spinner(this);
        List<String> roles = Arrays.asList("admin", "user");
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        if (isEdit) {
            int rolePosition = roles.indexOf(user.getRole());
            if (rolePosition >= 0) {
                roleSpinner.setSelection(rolePosition);
            }
        }
        layout.addView(roleSpinner);
        
        // Active checkbox
        CheckBox activeCheckBox = new CheckBox(this);
        activeCheckBox.setText("Active User");
        activeCheckBox.setChecked(isEdit ? user.isActive() : true);
        layout.addView(activeCheckBox);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(layout);
        
        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
            if (saveUser(user, usernameInput, passwordInput, fullNameInput, emailInput, roleSpinner, activeCheckBox)) {
                dialog.dismiss();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        if (isEdit) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                confirmDeleteUser(user);
            });
        }
        
        builder.show();
    }
    
    private boolean saveUser(User existingUser, EditText usernameInput, EditText passwordInput, 
                           EditText fullNameInput, EditText emailInput, Spinner roleSpinner, CheckBox activeCheckBox) {
        
        boolean isEdit = existingUser != null;
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String role = (String) roleSpinner.getSelectedItem();
        boolean isActive = activeCheckBox.isChecked();
        
        // Validation
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            return false;
        }
        
        if (!isEdit && password.isEmpty()) {
            passwordInput.setError("Password is required for new users");
            return false;
        }
        
        if (fullName.isEmpty()) {
            fullNameInput.setError("Full name is required");
            return false;
        }
        
        // Check username uniqueness
        if (userDAO.isUsernameExists(username, isEdit ? existingUser.getUserId() : -1)) {
            usernameInput.setError("Username already exists");
            return false;
        }
        
        // Check if this would be the last admin
        if (isEdit && existingUser.isAdmin() && (!role.equals("admin") || !isActive)) {
            if (userDAO.isLastActiveAdmin(existingUser.getUserId())) {
                showError("Cannot deactivate or demote the last admin user");
                return false;
            }
        }
        
        // Create/update user
        User user = isEdit ? existingUser : new User();
        user.setUsername(username);
        if (!password.isEmpty()) {
            user.setPassword(password);
        }
        user.setFullName(fullName);
        user.setEmail(email.isEmpty() ? null : email);
        user.setRole(role);
        user.setActive(isActive);
        
        // Save to database
        boolean success;
        if (isEdit) {
            success = userDAO.updateUser(user) > 0;
        } else {
            long userId = userDAO.addUser(user);
            success = userId > 0;
            if (success) {
                user.setUserId((int) userId);
            }
        }
        
        if (success) {
            loadAllUsers();
            Toast.makeText(this, isEdit ? "User updated successfully" : "User added successfully", 
                          Toast.LENGTH_SHORT).show();
            return true;
        } else {
            showError("Failed to " + (isEdit ? "update" : "add") + " user");
            return false;
        }
    }
    
    private void confirmDeleteUser(User user) {
        if (userDAO.isLastActiveAdmin(user.getUserId())) {
            showError("Cannot delete the last admin user.\n" +
                     "At least one admin must remain active.");
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete '" + user.getFullName() + "'?\n\n" +
                       "This will deactivate the user account.")
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean success = userDAO.deleteUser(user.getUserId());
                if (success) {
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAllUsers();
                } else {
                    showError("Failed to delete user");
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showItemDialog(Item item) {
        boolean isEdit = item != null;
        String title = isEdit ? "Edit Item" : "Add New Item";
        
        // Create dialog layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        // Item name input
        EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name");
        if (isEdit) nameInput.setText(item.getName());
        layout.addView(nameInput);
        
        // Category spinner
        Spinner categorySpinner = new Spinner(this);
        List<String> categoryList = new ArrayList<>(categories);
        categoryList.remove("All Categories");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        if (isEdit) {
            int categoryPosition = categoryList.indexOf(item.getCategoryName());
            if (categoryPosition >= 0) {
                categorySpinner.setSelection(categoryPosition);
            }
        }
        layout.addView(categorySpinner);
        
        // Size input
        EditText sizeInput = new EditText(this);
        sizeInput.setHint("Size (ml) - optional");
        sizeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (isEdit && item.getSizeML() != null && item.getSizeML() > 0) {
            sizeInput.setText(String.valueOf(item.getSizeML()));
        }
        layout.addView(sizeInput);
        
        // FIXED: ADA friendly checkbox
        CheckBox adaFriendlyCheckBox = new CheckBox(this);
        adaFriendlyCheckBox.setText("ADA Friendly");
        adaFriendlyCheckBox.setChecked(isEdit ? item.isAdaFriendly() : true);
        layout.addView(adaFriendlyCheckBox);
        
        // Soda checkbox
        CheckBox sodaCheckBox = new CheckBox(this);
        sodaCheckBox.setText("Is Soda");
        sodaCheckBox.setChecked(isEdit ? item.isSoda() : false);
        layout.addView(sodaCheckBox);
        
        // FIXED: Clear liquid checkbox
        CheckBox clearLiquidCheckBox = new CheckBox(this);
        clearLiquidCheckBox.setText("Clear Liquid");
        clearLiquidCheckBox.setChecked(isEdit ? item.isClearLiquid() : false);
        layout.addView(clearLiquidCheckBox);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(layout);
        
        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
            if (saveItem(item, nameInput, categorySpinner, sizeInput, adaFriendlyCheckBox, sodaCheckBox, clearLiquidCheckBox)) {
                dialog.dismiss();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        if (isEdit) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                confirmDeleteItem(item);
            });
        }
        
        builder.show();
    }
    
    private boolean saveItem(Item existingItem, EditText nameInput, Spinner categorySpinner, 
                           EditText sizeInput, CheckBox adaFriendlyCheckBox, CheckBox sodaCheckBox, CheckBox clearLiquidCheckBox) {
        
        boolean isEdit = existingItem != null;
        String name = nameInput.getText().toString().trim();
        String categoryName = (String) categorySpinner.getSelectedItem();
        String sizeText = sizeInput.getText().toString().trim();
        boolean adaFriendly = adaFriendlyCheckBox.isChecked();
        boolean isSoda = sodaCheckBox.isChecked();
        boolean isClearLiquid = clearLiquidCheckBox.isChecked();
        
        // Validation
        if (name.isEmpty()) {
            nameInput.setError("Item name is required");
            return false;
        }
        
        if (categoryName == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Get category ID
        int categoryId = categories.indexOf(categoryName);
        if (categoryId <= 0) {
            Toast.makeText(this, "Invalid category selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Parse size
        Integer sizeML = null;
        if (!sizeText.isEmpty()) {
            try {
                sizeML = Integer.parseInt(sizeText);
                if (sizeML <= 0) {
                    sizeInput.setError("Size must be a positive number");
                    return false;
                }
            } catch (NumberFormatException e) {
                sizeInput.setError("Invalid size format");
                return false;
            }
        }
        
        // Create/update item
        Item item = isEdit ? existingItem : new Item();
        item.setName(name);
        item.setCategoryId(categoryId);
        item.setSizeML(sizeML);
        item.setAdaFriendly(adaFriendly);
        item.setSoda(isSoda);
        item.setClearLiquid(isClearLiquid);
        item.setCategoryName(categoryName);
        
        // Save to database
        boolean success;
        if (isEdit) {
            success = itemDAO.updateItem(item) > 0;
        } else {
            long itemId = itemDAO.addItem(item);
            success = itemId > 0;
            if (success) {
                item.setItemId((int) itemId);
            }
        }
        
        if (success) {
            loadAllItems();
            Toast.makeText(this, isEdit ? "Item updated successfully" : "Item added successfully", 
                          Toast.LENGTH_SHORT).show();
            return true;
        } else {
            showError("Failed to " + (isEdit ? "update" : "add") + " item");
            return false;
        }
    }
    
    private void confirmDeleteItem(Item item) {
        if (itemDAO.isItemUsedInOrders(item.getItemId())) {
            showError("Cannot delete item '" + item.getName() + "'.\n" +
                     "This item is used in existing orders.");
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '" + item.getName() + "'?\n\n" +
                       "This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean success = itemDAO.deleteItem(item.getItemId());
                if (success) {
                    Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAllItems();
                } else {
                    showError("Failed to delete item");
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
    
    // FIXED: Override onBackPressed to return to main menu instead of exiting
    @Override
    public void onBackPressed() {
        if (usersContainer.getVisibility() == View.VISIBLE || itemsContainer.getVisibility() == View.VISIBLE) {
            showMainMenu();
        } else {
            // FIXED: Return to MainMenuActivity with proper user context
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
            finish();
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
                text2.setText(user.getRole().toUpperCase() + " - " + status);
            }
            
            return convertView;
        }
    }
}