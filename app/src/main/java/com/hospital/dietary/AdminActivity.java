// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/AdminActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
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
    private ArrayAdapter<User> usersAdapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    
    // Items Management UI (existing)
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
        
        // Get current user from intent (we'll pass this from MainActivity)
        String username = getIntent().getStringExtra("current_user");
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);
        
        // Get current user details
        if (username != null) {
            currentUser = userDAO.getUserByUsername(username);
        }
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Show main menu
        showMainMenu();
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
        usersAdapter = new ArrayAdapter<User>(this, R.layout.item_user_row, filteredUsers) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user_row, parent, false);
                }
                
                User user = getItem(position);
                
                TextView userName = convertView.findViewById(R.id.userNameText);
                TextView userDetails = convertView.findViewById(R.id.userDetailsText);
                Button editButton = convertView.findViewById(R.id.editUserButton);
                Button deleteButton = convertView.findViewById(R.id.deleteUserButton);
                
                userName.setText(user.getFullName());
                
                String details = user.getUsername() + " • " + user.getRole().toUpperCase();
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    details += " • " + user.getEmail();
                }
                if (!user.isActive()) {
                    details += " • INACTIVE";
                }
                
                userDetails.setText(details);
                
                editButton.setOnClickListener(v -> editUser(user));
                deleteButton.setOnClickListener(v -> deleteUser(user));
                
                // Hide delete button for current user
                if (currentUser != null && user.getUserId() == currentUser.getUserId()) {
                    deleteButton.setVisibility(View.GONE);
                }
                
                return convertView;
            }
        };
        usersListView.setAdapter(usersAdapter);
        
        // Setup items adapter (existing code)
        setupItemsAdapter();
        
        // Setup category filter spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);
    }
    
    private void setupItemsAdapter() {
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
        // Menu buttons
        usersMenuButton.setOnClickListener(v -> showUsersManagement());
        itemsMenuButton.setOnClickListener(v -> showItemsManagement());
        backToMenuButton.setOnClickListener(v -> showMainMenu());
        
        // Users management listeners
        userSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        addUserButton.setOnClickListener(v -> addNewUser());
        
        // Items management listeners
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
        
        addItemButton.setOnClickListener(v -> addNewItem());
    }
    
    // ===== NAVIGATION METHODS =====
    
    private void showMainMenu() {
        mainMenuContainer.setVisibility(View.VISIBLE);
        usersContainer.setVisibility(View.GONE);
        itemsContainer.setVisibility(View.GONE);
        backToMenuButton.setVisibility(View.GONE);
        
        // Update stats
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
        
        int userCount = userDAO.getUserCount();
        int itemCount = itemDAO.getAllItems().size();
        
        userCountText.setText(userCount + " Active Users");
        itemCountText.setText(itemCount + " Food Items");
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
            if (searchQuery.isEmpty() || 
                user.getFullName().toLowerCase().contains(searchQuery) ||
                user.getUsername().toLowerCase().contains(searchQuery) ||
                user.getRole().toLowerCase().contains(searchQuery)) {
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
    
    private void showUserDialog(User user) {
        boolean isEdit = (user != null);
        String title = isEdit ? "Edit User" : "Add New User";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        // Create input layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        
        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        if (isEdit) usernameInput.setText(user.getUsername());
        layout.addView(usernameInput);
        
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if (isEdit) passwordInput.setText(user.getPassword());
        layout.addView(passwordInput);
        
        EditText fullNameInput = new EditText(this);
        fullNameInput.setHint("Full Name");
        if (isEdit) fullNameInput.setText(user.getFullName());
        layout.addView(fullNameInput);
        
        EditText emailInput = new EditText(this);
        emailInput.setHint("Email (optional)");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (isEdit && user.getEmail() != null) emailInput.setText(user.getEmail());
        layout.addView(emailInput);
        
        Spinner roleSpinner = new Spinner(this);
        List<String> roles = Arrays.asList("user", "admin");
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        
        if (isEdit) {
            int rolePosition = roles.indexOf(user.getRole());
            if (rolePosition >= 0) {
                roleSpinner.setSelection(rolePosition);
            }
        }
        layout.addView(roleSpinner);
        
        CheckBox activeCheckBox = new CheckBox(this);
        activeCheckBox.setText("Active");
        activeCheckBox.setChecked(isEdit ? user.isActive() : true);
        layout.addView(activeCheckBox);
        
        builder.setView(layout);
        
        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
            if (saveUser(usernameInput, passwordInput, fullNameInput, emailInput, 
                        roleSpinner, activeCheckBox, user, isEdit)) {
                dialog.dismiss();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
            showError("Username is required");
            return false;
        }
        
        if (password.isEmpty()) {
            showError("Password is required");
            return false;
        }
        
        if (fullName.isEmpty()) {
            showError("Full name is required");
            return false;
        }
        
        // Check for duplicate usernames
        if (userDAO.usernameExists(username, isEdit ? existingUser.getUserId() : -1)) {
            showError("Username already exists");
            return false;
        }
        
        // Create/update user
        User user = isEdit ? existingUser : new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email.isEmpty() ? null : email);
        user.setRole(role);
        user.setActive(isActive);
        
        // Save to database
        long result;
        if (isEdit) {
            result = userDAO.updateUser(user);
        } else {
            result = userDAO.addUser(user);
            if (result > 0) {
                user.setUserId((int) result);
            }
        }
        
        if (result > 0) {
            if (!isEdit) {
                allUsers.add(user);
            }
            filterUsers();
            
            String message = isEdit ? "User updated successfully" : "User added successfully";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            showError("Failed to save user. Please try again.");
            return false;
        }
    }
    
    private void deleteUser(User user) {
        // Check if this is the only admin
        if (user.isAdmin() && userDAO.getAdminCount() <= 1) {
            showError("Cannot delete the last admin user.");
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete user '" + user.getFullName() + "'?\n\n" +
                       "This will deactivate the user account.")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (userDAO.deleteUser(user.getUserId())) {
                    user.setActive(false);
                    filterUsers();
                    Toast.makeText(this, "User deactivated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Failed to delete user.");
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // ===== ITEMS MANAGEMENT METHODS (existing code adapted) =====
    
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
    
    private void addNewItem() {
        showItemDialog(null);
    }
    
    private void editItem(Item item) {
        showItemDialog(item);
    }
    
    // Item dialog and related methods (same as before)
    private void showItemDialog(Item item) {
        boolean isEdit = (item != null);
        String title = isEdit ? "Edit Item" : "Add New Item";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        
        EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name");
        if (isEdit) nameInput.setText(item.getName());
        layout.addView(nameInput);
        
        Spinner categorySpinner = new Spinner(this);
        List<String> editCategories = new ArrayList<>(categories);
        editCategories.remove("All Categories");
        
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, editCategories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(catAdapter);
        
        if (isEdit && item.getCategoryName() != null) {
            int catPosition = editCategories.indexOf(item.getCategoryName());
            if (catPosition >= 0) {
                categorySpinner.setSelection(catPosition);
            }
        }
        layout.addView(categorySpinner);
        
        EditText sizeInput = new EditText(this);
        sizeInput.setHint("Size (ml) - Optional");
        sizeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (isEdit && item.getSizeML() != null) {
            sizeInput.setText(String.valueOf(item.getSizeML()));
        }
        layout.addView(sizeInput);
        
        CheckBox adaCheckBox = new CheckBox(this);
        adaCheckBox.setText("ADA Friendly");
        if (isEdit) adaCheckBox.setChecked(item.isAdaFriendly());
        layout.addView(adaCheckBox);
        
        CheckBox sodaCheckBox = new CheckBox(this);
        sodaCheckBox.setText("Is Soda");
        if (isEdit) sodaCheckBox.setChecked(item.isSoda());
        layout.addView(sodaCheckBox);
        
        builder.setView(layout);
        
        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
            if (saveItem(nameInput, categorySpinner, sizeInput, adaCheckBox, sodaCheckBox, item, isEdit)) {
                dialog.dismiss();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private boolean saveItem(EditText nameInput, Spinner categorySpinner, EditText sizeInput, 
                           CheckBox adaCheckBox, CheckBox sodaCheckBox, Item existingItem, boolean isEdit) {
        
        String name = nameInput.getText().toString().trim();
        String category = (String) categorySpinner.getSelectedItem();
        String sizeText = sizeInput.getText().toString().trim();
        
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
        
        Item item = isEdit ? existingItem : new Item();
        item.setName(name);
        item.setSizeML(sizeML);
        item.setAdaFriendly(adaCheckBox.isChecked());
        item.setSoda(sodaCheckBox.isChecked());
        
        int categoryId = getCategoryId(category);
        item.setCategoryId(categoryId);
        item.setCategoryName(category);
        
        long result;
        if (isEdit) {
            result = itemDAO.updateItem(item);
        } else {
            result = itemDAO.insertItem(item);
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
}