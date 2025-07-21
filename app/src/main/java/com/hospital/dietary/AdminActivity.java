package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

    // Current user state
    private User currentUser;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // Track launch mode
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
    private TextView usersCountText;
    private UserAdapter usersAdapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();

    // Items Management UI
    private Spinner categoryFilterSpinner;
    private ListView itemsListView;
    private EditText itemSearchEditText;
    private Button addItemButton;
    private TextView itemsCountText;
    private ItemAdapter itemsAdapter;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();

    // Categories for filtering
    private List<String> categories = Arrays.asList(
            "All Categories", "Breakfast Items", "Proteins", "Starches", "Vegetables",
            "Beverages", "Juices", "Desserts", "Fruits", "Dairy"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        Log.d(TAG, "AdminActivity onCreate started");

        // Get current user from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);

        // Validate admin access
        if (currentUsername == null || !"admin".equals(currentUserRole)) {
            Log.w(TAG, "Non-admin user attempting to access admin panel");
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get current user object
        currentUser = userDAO.getUserByUsername(currentUsername);
        if (currentUser == null || !isUserAdmin()) {
            Log.w(TAG, "User not found or not admin: " + currentUsername);
            Toast.makeText(this, "Admin user not found or invalid.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize UI
        initializeUI();
        setupListeners();
        configureAdminMenu();

        // Handle intent extras for direct navigation
        String adminMode = getIntent().getStringExtra("admin_mode");
        boolean showUsers = getIntent().getBooleanExtra("show_users", false);
        boolean showItems = getIntent().getBooleanExtra("show_items", false);
        boolean showDefaultMenus = getIntent().getBooleanExtra("show_default_menus", false);

        wasLaunchedWithDirectMode = ("users".equals(adminMode) || showUsers ||
                "items".equals(adminMode) || showItems ||
                "default_menus".equals(adminMode) || showDefaultMenus);

        if ("users".equals(adminMode) || showUsers) {
            showUsersManagement();
        } else if ("items".equals(adminMode) || showItems) {
            showItemsManagement();
        } else if ("default_menus".equals(adminMode) || showDefaultMenus) {
            showDefaultMenuManagement();
        } else {
            showMainMenu();
        }

        Log.d(TAG, "AdminActivity onCreate completed");
    }

    private boolean isUserAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }

    private void configureAdminMenu() {
        if (currentUser != null) {
            setTitle("Admin Panel - " + currentUser.getFullName());
        } else {
            setTitle("Admin Panel");
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
        usersCountText = findViewById(R.id.usersCountText);

        // Items management UI
        categoryFilterSpinner = findViewById(R.id.categoryFilterSpinner);
        itemsListView = findViewById(R.id.itemsListView);
        itemSearchEditText = findViewById(R.id.itemSearchEditText);
        addItemButton = findViewById(R.id.addItemButton);
        itemsCountText = findViewById(R.id.itemsCountText);

        // Initialize adapters
        usersAdapter = new UserAdapter(this, filteredUsers);
        usersListView.setAdapter(usersAdapter);

        itemsAdapter = new ItemAdapter(this, filteredItems);
        itemsListView.setAdapter(itemsAdapter);

        // Set up category filter spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);
    }

    private void setupListeners() {
        // Menu button listeners
        usersMenuButton.setOnClickListener(v -> showUsersManagement());
        itemsMenuButton.setOnClickListener(v -> showItemsManagement());

        // NEW: Default menu button listener
        Button defaultMenuButton = findViewById(R.id.defaultMenuButton);
        if (defaultMenuButton != null) {
            defaultMenuButton.setOnClickListener(v -> showDefaultMenuManagement());
        }

        backToMenuButton.setOnClickListener(v -> handleBackNavigation());

        // FIXED: Users list click listener
        usersListView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = filteredUsers.get(position);
            showUserEditDialog(selectedUser);
        });

        // FIXED: Items list click listener
        itemsListView.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = filteredItems.get(position);
            showItemEditDialog(selectedItem);
        });

        // Users management listeners
        addUserButton.setOnClickListener(v -> showAddUserDialog());
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

        // Items management listeners
        addItemButton.setOnClickListener(v -> showAddItemDialog());
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
        hideAllContainers();
        mainMenuContainer.setVisibility(View.VISIBLE);
        setTitle("Admin Panel - " + (currentUser != null ? currentUser.getFullName() : currentUsername));
    }

    // FIXED: Updated navigation methods
    private void hideAllContainers() {
        if (mainMenuContainer != null) mainMenuContainer.setVisibility(View.GONE);
        if (usersContainer != null) usersContainer.setVisibility(View.GONE);
        if (itemsContainer != null) itemsContainer.setVisibility(View.GONE);
    }

    private void showUsersManagement() {
        hideAllContainers();
        usersContainer.setVisibility(View.VISIBLE);
        setTitle("User Management");
        loadUsers();
    }

    private void showItemsManagement() {
        hideAllContainers();
        itemsContainer.setVisibility(View.VISIBLE);
        setTitle("Item Management");
        loadItems();
    }

    // FIXED: User edit dialog
    private void showUserEditDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit User: " + user.getUsername());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // Full Name
        final EditText fullNameInput = new EditText(this);
        fullNameInput.setHint("Full Name");
        fullNameInput.setText(user.getFullName());
        layout.addView(fullNameInput);

        // Role Spinner
        final Spinner roleSpinner = new Spinner(this);
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        roleSpinner.setSelection(user.getRole().equals("admin") ? 1 : 0);
        layout.addView(roleSpinner);

        // Active Status
        final CheckBox activeCheckBox = new CheckBox(this);
        activeCheckBox.setText("User Active");
        activeCheckBox.setChecked(user.isActive());
        layout.addView(activeCheckBox);

        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            try {
                user.setFullName(fullNameInput.getText().toString().trim());
                user.setRole(roleSpinner.getSelectedItem().toString());
                user.setActive(activeCheckBox.isChecked());

                boolean success = userDAO.updateUser(user);
                if (success) {
                    Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                    loadUsers(); // Refresh the list
                } else {
                    Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Change Password", (dialog, which) -> {
            showChangePasswordDialog(user);
        });

        builder.setNegativeButton("Delete User", (dialog, which) -> {
            showDeleteUserConfirmation(user);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangePasswordDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password for " + user.getUsername());

        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("New Password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("Change Password", (dialog, which) -> {
            String newPassword = passwordInput.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                user.setPassword(newPassword); // Note: In production, hash this password
                boolean success = userDAO.updateUser(user);
                if (success) {
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteUserConfirmation(User user) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete User")
                .setMessage("Are you sure you want to delete user '" + user.getUsername() + "'?\n\nThis action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = userDAO.deleteUser(user.getUserId());
                    if (success) {
                        Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Refresh the list
                    } else {
                        Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showItemEditDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Item: " + item.getName());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // Item Name
        final EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name");
        nameInput.setText(item.getName());
        layout.addView(nameInput);

        // Category Spinner
        final Spinner categorySpinner = new Spinner(this);
        String[] itemCategories = {"Breakfast Items", "Proteins", "Starches", "Vegetables", "Beverages", "Juices", "Desserts", "Fruits", "Dairy"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        // Set current selection
        for (int i = 0; i < itemCategories.length; i++) {
            if (itemCategories[i].equals(item.getCategory())) {
                categorySpinner.setSelection(i);
                break;
            }
        }
        layout.addView(categorySpinner);

        // ADA Friendly
        final CheckBox adaCheckBox = new CheckBox(this);
        adaCheckBox.setText("ADA Friendly");
        adaCheckBox.setChecked(item.isAdaFriendly());
        layout.addView(adaCheckBox);

        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            try {
                item.setName(nameInput.getText().toString().trim());
                item.setCategory(categorySpinner.getSelectedItem().toString());
                item.setAdaFriendly(adaCheckBox.isChecked());

                boolean success = itemDAO.updateItem(item);
                if (success) {
                    Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                    loadItems(); // Refresh the list
                } else {
                    Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Delete Item", (dialog, which) -> {
            showDeleteItemConfirmation(item);
        });

        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void showDeleteItemConfirmation(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete Item")
                .setMessage("Are you sure you want to delete item '" + item.getName() + "'?\n\nThis action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = itemDAO.deleteItem(item.getItemId());
                    if (success) {
                        Toast.makeText(this, "Item deleted successfully!", Toast.LENGTH_SHORT).show();
                        loadItems(); // Refresh the list
                    } else {
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadUsers() {
        try {
            allUsers.clear();
            allUsers.addAll(userDAO.getAllUsers());
            filterUsers();
        } catch (Exception e) {
            Log.e(TAG, "Error loading users", e);
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterUsers() {
        try {
            filteredUsers.clear();
            String searchQuery = userSearchEditText.getText().toString().toLowerCase().trim();

            for (User user : allUsers) {
                if (searchQuery.isEmpty() ||
                        user.getUsername().toLowerCase().contains(searchQuery) ||
                        user.getFullName().toLowerCase().contains(searchQuery) ||
                        user.getRole().toLowerCase().contains(searchQuery)) {
                    filteredUsers.add(user);
                }
            }

            usersAdapter.notifyDataSetChanged();
            usersCountText.setText("Users: " + filteredUsers.size());

        } catch (Exception e) {
            Log.e(TAG, "Error filtering users", e);
        }
    }

    private void loadItems() {
        try {
            allItems.clear();
            allItems.addAll(itemDAO.getAllItems());
            filterItems();
        } catch (Exception e) {
            Log.e(TAG, "Error loading items", e);
            Toast.makeText(this, "Error loading items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterItems() {
        try {
            filteredItems.clear();
            String searchQuery = itemSearchEditText.getText().toString().toLowerCase().trim();
            String selectedCategory = categories.get(categoryFilterSpinner.getSelectedItemPosition());

            for (Item item : allItems) {
                boolean matchesSearch = searchQuery.isEmpty() ||
                        item.getName().toLowerCase().contains(searchQuery) ||
                        item.getCategory().toLowerCase().contains(searchQuery);

                boolean matchesCategory = "All Categories".equals(selectedCategory) ||
                        item.getCategory().equals(selectedCategory);

                if (matchesSearch && matchesCategory) {
                    filteredItems.add(item);
                }
            }

            itemsAdapter.notifyDataSetChanged();
            itemsCountText.setText("Items: " + filteredItems.size());

        } catch (Exception e) {
            Log.e(TAG, "Error filtering items", e);
        }
    }

    private void showAddUserDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Add User")
                .setMessage("User management functionality - Add new user dialog would be implemented here")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAddItemDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setMessage("Item management functionality - Add new item dialog would be implemented here")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDefaultMenuManagement() {
        Intent intent = new Intent(this, DefaultMenuManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    // FIXED: Updated back navigation
    private void handleBackNavigation() {
        // Always go back to the main menu when back is pressed
        goToMainMenu();
    }

    @Override
    public void onBackPressed() {
        // Check which container is currently visible
        if (usersContainer.getVisibility() == View.VISIBLE ||
                itemsContainer.getVisibility() == View.VISIBLE) {
            // If we're in a sub-menu (users or items), go back to admin main menu
            showMainMenu();
        } else {
            // If we're in the admin main menu, go back to main app menu
            goToMainMenu();
        }
    }

    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // User Adapter Class
    private class UserAdapter extends BaseAdapter {
        private android.content.Context context;
        private List<User> users;

        public UserAdapter(android.content.Context context, List<User> users) {
            this.context = context;
            this.users = users;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return users.get(position).getUserId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
            }

            User user = users.get(position);

            TextView usernameText = convertView.findViewById(R.id.usernameText);
            TextView fullNameText = convertView.findViewById(R.id.fullNameText);
            TextView roleText = convertView.findViewById(R.id.roleText);
            TextView statusText = convertView.findViewById(R.id.statusText);

            usernameText.setText(user.getUsername());
            fullNameText.setText(user.getFullName());
            roleText.setText(user.getRoleDisplayName());
            statusText.setText(user.isActive() ? "Active" : "Inactive");

            // Set color based on status
            int textColor = user.isActive() ?
                    context.getResources().getColor(android.R.color.black) :
                    context.getResources().getColor(android.R.color.darker_gray);

            usernameText.setTextColor(textColor);
            fullNameText.setTextColor(textColor);

            return convertView;
        }
    }

    // Item Adapter Class
    private class ItemAdapter extends BaseAdapter {
        private android.content.Context context;
        private List<Item> items;

        public ItemAdapter(android.content.Context context, List<Item> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).getItemId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Item item = items.get(position);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(item.getName());
            text2.setText(item.getCategory() +
                    (item.isAdaFriendly() ? " (ADA Friendly)" : ""));

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); // Use the same logic as back button
                return true;
            case R.id.action_home:
                goToMainMenu(); // Always go to main menu for home action
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}