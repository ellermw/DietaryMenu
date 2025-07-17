package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Context;
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
    
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private UserDAO userDAO;
    
    // Current logged-in user
    private User currentUser;
    
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
        
        // Get current user from intent
        String username = getIntent().getStringExtra("current_user");
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);
        
        // FIXED: Get current user details BEFORE initializing UI
        if (username != null) {
            currentUser = userDAO.getUserByUsername(username);
        }
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // FIXED: Ensure admin menu is configured before any navigation
        if (currentUser != null && isUserAdmin()) {
            configureAdminMenu();
        }
        
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
    }
    
    // FIXED: Add onResume to handle state management
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure proper state when returning to activity
        if (currentUser != null && isUserAdmin()) {
            // Restore admin menu if we're on the main screen
            if (mainMenuContainer.getVisibility() == View.VISIBLE) {
                configureAdminMenu();
            }
        }
    }

    // FIXED: Add helper methods for admin state management
    private boolean isUserAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    private void configureAdminMenu() {
        // Ensure admin buttons are visible for admin users
        if (usersMenuButton != null) {
            usersMenuButton.setVisibility(View.VISIBLE);
        }
        if (itemsMenuButton != null) {
            itemsMenuButton.setVisibility(View.VISIBLE);
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
        
        // Users management components
        usersListView = findViewById(R.id.usersListView);
        userSearchEditText = findViewById(R.id.userSearchEditText);
        addUserButton = findViewById(R.id.addUserButton);
        
        // Items management components
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);
        itemsListView = findViewById(R.id.itemsListView);
        itemSearchEditText = findViewById(R.id.itemSearchEditText);
        addItemButton = findViewById(R.id.addItemButton);
        
        // Setup users adapter
        usersAdapter = new UserAdapter(this, filteredUsers);
        usersListView.setAdapter(usersAdapter);
        
        // Setup items adapter
        itemsAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, filteredItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Item item = getItem(position);
                TextView textView = (TextView) view;
                String displayText = item.getName() + " (" + item.getCategoryName() + ")";
                if (item.getSizeML() != null && item.getSizeML() > 0) {
                    displayText += " - " + item.getSizeML() + "ml";
                }
                textView.setText(displayText);
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
        // Menu navigation
        usersMenuButton.setOnClickListener(v -> showUsersManagement());
        itemsMenuButton.setOnClickListener(v -> showItemsManagement());
        backToMenuButton.setOnClickListener(v -> showMainMenu());
        
        // Users management
        addUserButton.setOnClickListener(v -> addNewUser());
        usersListView.setOnItemClickListener((parent, view, position, id) -> {
            User user = filteredUsers.get(position);
            showUserOptions(user);
        });
        
        // Items management
        addItemButton.setOnClickListener(v -> addNewItem());
        itemsListView.setOnItemClickListener((parent, view, position, id) -> {
            Item item = filteredItems.get(position);
            showItemOptions(item);
        });
        
        // Search and filter
        userSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterUsers();
            }
        });
        
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterItems();
            }
        });
    }
    
    // FIXED: Update showMainMenu to properly restore admin menu
    private void showMainMenu() {
        mainMenuContainer.setVisibility(View.VISIBLE);
        usersContainer.setVisibility(View.GONE);
        itemsContainer.setVisibility(View.GONE);
        backToMenuButton.setVisibility(View.GONE);
        
        // CRITICAL: Restore admin menu buttons for admin users
        if (currentUser != null && isUserAdmin()) {
            configureAdminMenu();
        }
        
        updateMenuStats();
    }
    
    private void showUsersManagement() {
        mainMenuContainer.setVisibility(View.GONE);
        usersContainer.setVisibility(View.VISIBLE);
        itemsContainer.setVisibility(View.GONE);
        backToMenuButton.setVisibility(View.VISIBLE);
        
        loadAllUsers();
    }
    
    private void showItemsManagement() {
        mainMenuContainer.setVisibility(View.GONE);
        usersContainer.setVisibility(View.GONE);
        itemsContainer.setVisibility(View.VISIBLE);
        backToMenuButton.setVisibility(View.VISIBLE);
        
        loadAllItems();
    }
    
    private void updateMenuStats() {
        TextView userCountText = findViewById(R.id.userCountText);
        TextView itemCountText = findViewById(R.id.itemCountText);
        
        if (userCountText != null && itemCountText != null) {
            int userCount = userDAO.getUserCount();
            int itemCount = itemDAO.getAllItems().size();
            
            userCountText.setText(userCount + " Active Users");
            itemCountText.setText(itemCount + " Food Items");
        }
    }
    
    // ===== USER MANAGEMENT METHODS =====
    
    private void loadAllUsers() {
        allUsers.clear();
        allUsers.addAll(userDAO.getAllUsers());
        filterUsers();
    }
    
    private void filterUsers() {
        filteredUsers.clear();
        
        String searchQuery = userSearchEditText.getText().toString().toLowerCase().trim();
        
        for (User user : allUsers) {
            boolean searchMatch = searchQuery.isEmpty() || 
                                 user.getFullName().toLowerCase().contains(searchQuery) ||
                                 user.getUsername().toLowerCase().contains(searchQuery) ||
                                 (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchQuery));
            
            if (searchMatch) {
                filteredUsers.add(user);
            }
        }
        
        usersAdapter.notifyDataSetChanged();
    }
    
    private void addNewUser() {
        showUserDialog(null);
    }
    
    private void editUser(User user) {
        showUserDialog(user);
    }
    
    private void showUserOptions(User user) {
        String[] options = {"Edit User", "Delete User"};
        
        new AlertDialog.Builder(this)
            .setTitle(user.getFullName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Edit
                        editUser(user);
                        break;
                    case 1: // Delete
                        confirmDeleteUser(user);
                        break;
                }
            })
            .show();
    }
    
    private void showUserDialog(User user) {
        boolean isEdit = (user != null);
        String title = isEdit ? "Edit User" : "Add New User";
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_form, null);
        
        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        EditText fullNameInput = dialogView.findViewById(R.id.fullNameInput);
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        CheckBox activeCheckBox = dialogView.findViewById(R.id.activeCheckBox);
        
        // Setup role spinner
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        
        // Populate fields if editing
        if (isEdit) {
            usernameInput.setText(user.getUsername());
            passwordInput.setText(user.getPassword());
            fullNameInput.setText(user.getFullName());
            emailInput.setText(user.getEmail() != null ? user.getEmail() : "");
            roleSpinner.setSelection(user.getRole().equals("admin") ? 1 : 0);
            activeCheckBox.setChecked(user.isActive());
        } else {
            activeCheckBox.setChecked(true);
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(dialogView);
        
        builder.setPositiveButton(isEdit ? "Update" : "Add", null);
        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                if (saveUser(usernameInput, passwordInput, fullNameInput, emailInput, 
                            roleSpinner, activeCheckBox, user, isEdit)) {
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
    }
    
    private boolean saveUser(EditText usernameInput, EditText passwordInput, EditText fullNameInput,
                           EditText emailInput, Spinner roleSpinner, CheckBox activeCheckBox,
                           User existingUser, boolean isEdit) {
        
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
        
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }
        
        if (fullName.isEmpty()) {
            fullNameInput.setError("Full name is required");
            return false;
        }
        
        // Check for duplicate usernames
        if (userDAO.usernameExists(username, isEdit ? existingUser.getUserId() : -1)) {
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
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email.isEmpty() ? null : email);
        user.setRole(role);
        user.setActive(isActive);
        
        // Handle long return types properly
        boolean success;
        if (isEdit) {
            long result = userDAO.updateUser(user);
            success = result > 0;
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
                if (userDAO.deleteUser(user.getUserId())) {
                    loadAllUsers();
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Failed to delete user");
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // ===== ITEM MANAGEMENT METHODS =====
    
    private void loadAllItems() {
        allItems.clear();
        allItems.addAll(itemDAO.getAllItems());
        filterItems();
    }
    
    private void filterItems() {
        filteredItems.clear();
        
        String selectedCategory = (String) categoryFilterSpinner.getSelectedItem();
        String searchQuery = itemSearchEditText.getText().toString().toLowerCase().trim();
        
        for (Item item : allItems) {
            boolean categoryMatch = selectedCategory.equals("All Categories") || 
                                   item.getCategoryName().equals(selectedCategory);
            
            boolean searchMatch = searchQuery.isEmpty() || 
                                 item.getName().toLowerCase().contains(searchQuery);
            
            if (categoryMatch && searchMatch) {
                filteredItems.add(item);
            }
        }
        
        itemsAdapter.notifyDataSetChanged();
    }
    
    private void addNewItem() {
        showItemDialog(null);
    }
    
    private void editItem(Item item) {
        showItemDialog(item);
    }
    
    private void showItemOptions(Item item) {
        String[] options = {"Edit Item", "Delete Item"};
        
        new AlertDialog.Builder(this)
            .setTitle(item.getName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Edit
                        editItem(item);
                        break;
                    case 1: // Delete
                        confirmDeleteItem(item);
                        break;
                }
            })
            .show();
    }
    
    private void showItemDialog(Item item) {
        boolean isEdit = (item != null);
        String title = isEdit ? "Edit Item" : "Add New Item";
        
        // Create dialog layout programmatically to avoid layout file issues
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
        
        // ADA friendly checkbox
        CheckBox adaFriendlyCheckBox = new CheckBox(this);
        adaFriendlyCheckBox.setText("ADA Friendly");
        adaFriendlyCheckBox.setChecked(isEdit ? item.isAdaFriendly() : true);
        layout.addView(adaFriendlyCheckBox);
        
        // Soda checkbox
        CheckBox sodaCheckBox = new CheckBox(this);
        sodaCheckBox.setText("Is Soda");
        sodaCheckBox.setChecked(isEdit ? item.isSoda() : false);
        layout.addView(sodaCheckBox);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(layout);
        
        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String category = (String) categorySpinner.getSelectedItem();
            String sizeText = sizeInput.getText().toString().trim();
            boolean adaFriendly = adaFriendlyCheckBox.isChecked();
            boolean isSoda = sodaCheckBox.isChecked();
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Integer sizeML = null;
            if (!sizeText.isEmpty()) {
                try {
                    sizeML = Integer.parseInt(sizeText);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid size value", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Create/update item
            Item newItem = isEdit ? item : new Item();
            newItem.setName(name);
            newItem.setCategoryName(category);
            newItem.setSizeML(sizeML);
            newItem.setAdaFriendly(adaFriendly);
            newItem.setSoda(isSoda);
            
            // Handle long return types properly
            boolean success;
            if (isEdit) {
                long result = itemDAO.updateItem(newItem);
                success = result > 0;
            } else {
                long itemId = itemDAO.insertItem(newItem);
                success = itemId > 0;
                if (success) {
                    newItem.setItemId((int) itemId);
                }
            }
            
            if (success) {
                loadAllItems();
                Toast.makeText(this, isEdit ? "Item updated successfully" : "Item added successfully", 
                              Toast.LENGTH_SHORT).show();
            } else {
                showError("Failed to " + (isEdit ? "update" : "add") + " item");
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void confirmDeleteItem(Item item) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '" + item.getName() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (itemDAO.deleteItem(item.getItemId())) {
                    loadAllItems();
                    Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Failed to delete item. It may be used in existing orders.");
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // ===== UTILITY METHODS =====
    
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
    
    // ===== USER ADAPTER =====
    
    private class UserAdapter extends ArrayAdapter<User> {
        
        public UserAdapter(Context context, List<User> users) {
            super(context, 0, users);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            User user = getItem(position);
            
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user_row, parent, false);
            }
            
            TextView userIcon = convertView.findViewById(R.id.userIcon);
            TextView userFullName = convertView.findViewById(R.id.userFullName);
            TextView userUsername = convertView.findViewById(R.id.userUsername);
            TextView userEmail = convertView.findViewById(R.id.userEmail);
            TextView userRole = convertView.findViewById(R.id.userRole);
            View userStatusIndicator = convertView.findViewById(R.id.userStatusIndicator);
            
            // Set user icon based on role
            userIcon.setText(user.isAdmin() ? "🔐" : "👤");
            
            // Set user information
            userFullName.setText(user.getFullName());
            userUsername.setText("@" + user.getUsername());
            userEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
            userRole.setText(user.getRole().toUpperCase());
            
            // Set status indicator color
            userStatusIndicator.setBackgroundColor(user.isActive() ? 
                getResources().getColor(android.R.color.holo_green_light) : 
                getResources().getColor(android.R.color.holo_red_light));
            
            return convertView;
        }
    }
}