package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
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

        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);

        // Setup users list adapter
        usersAdapter = new UserAdapter(this, filteredUsers);
        usersListView.setAdapter(usersAdapter);

        // Setup items list adapter
        itemsAdapter = new ItemAdapter(this, filteredItems);
        itemsListView.setAdapter(itemsAdapter);
    }

    private void setupListeners() {
        // Menu navigation listeners
        usersMenuButton.setOnClickListener(v -> showUsersManagement());
        itemsMenuButton.setOnClickListener(v -> showItemsManagement());
        backToMenuButton.setOnClickListener(v -> showMainMenu());

        // Users management listeners
        addUserButton.setOnClickListener(v -> showAddUserDialog());

        // FIXED: Add user selection listener for editing
        usersListView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = filteredUsers.get(position);
            showEditUserDialog(selectedUser);
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "AdminActivity onResume called");

        // Refresh user state and ensure proper access
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
            } else {
                Log.w(TAG, "Current user no longer exists: " + currentUsername);
                Toast.makeText(this, "User account no longer exists.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        // Refresh data
        if (usersContainer.getVisibility() == View.VISIBLE) {
            loadAllUsers();
        }
        if (itemsContainer.getVisibility() == View.VISIBLE) {
            loadAllItems();
        }
    }

    private void showMainMenu() {
        Log.d(TAG, "Showing main menu");
        mainMenuContainer.setVisibility(View.VISIBLE);
        usersContainer.setVisibility(View.GONE);
        itemsContainer.setVisibility(View.GONE);
    }

    private void showUsersManagement() {
        Log.d(TAG, "Showing users management");
        mainMenuContainer.setVisibility(View.GONE);
        usersContainer.setVisibility(View.VISIBLE);
        itemsContainer.setVisibility(View.GONE);
        loadAllUsers();
    }

    private void showItemsManagement() {
        Log.d(TAG, "Showing items management");
        mainMenuContainer.setVisibility(View.GONE);
        usersContainer.setVisibility(View.GONE);
        itemsContainer.setVisibility(View.VISIBLE);
        loadAllItems();
    }

    private void loadAllUsers() {
        Log.d(TAG, "Loading all users");
        try {
            allUsers.clear();
            allUsers.addAll(userDAO.getAllUsers());

            filteredUsers.clear();
            filteredUsers.addAll(allUsers);

            if (usersAdapter != null) {
                usersAdapter.notifyDataSetChanged();
            }

            updateUsersCount();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterUsers(String searchTerm) {
        filteredUsers.clear();

        if (searchTerm.trim().isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerSearch = searchTerm.toLowerCase();
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(lowerSearch) ||
                        user.getFullName().toLowerCase().contains(lowerSearch) ||
                        user.getRole().toLowerCase().contains(lowerSearch)) {
                    filteredUsers.add(user);
                }
            }
        }

        if (usersAdapter != null) {
            usersAdapter.notifyDataSetChanged();
        }
        updateUsersCount();
    }

    private void updateUsersCount() {
        if (usersCountText != null) {
            usersCountText.setText("Users: " + filteredUsers.size() + " of " + allUsers.size());
        }
    }

    private void showAddUserDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_user, null);

        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        EditText fullNameInput = dialogView.findViewById(R.id.fullNameInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);

        // Setup role spinner
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Add User", (dialog, which) -> {
                    String username = usernameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    String fullName = fullNameInput.getText().toString().trim();
                    String role = roleSpinner.getSelectedItem().toString();

                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User newUser = new User(username, password, fullName, role);
                    newUser.setActive(true);
                    // FEATURE: Force password change on first login
                    newUser.setMustChangePassword(true);

                    long result = userDAO.addUser(newUser);
                    if (result > 0) {
                        Toast.makeText(this, "User " + username + " added successfully! They must change their password on first login.", Toast.LENGTH_LONG).show();
                        loadAllUsers();
                    } else {
                        Toast.makeText(this, "Failed to add user. Username may already exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // FIXED: Add edit user dialog functionality
    private void showEditUserDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_user, null);

        EditText usernameEditText = dialogView.findViewById(R.id.usernameEditText);
        EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);
        EditText fullNameEditText = dialogView.findViewById(R.id.fullNameEditText);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        CheckBox activeCheckBox = dialogView.findViewById(R.id.activeCheckBox);

        // Setup role spinner
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Populate with current user data
        usernameEditText.setText(user.getUsername());
        passwordEditText.setText(user.getPassword());
        fullNameEditText.setText(user.getFullName());

        // Set role selection
        int rolePosition = "admin".equals(user.getRole()) ? 1 : 0;
        roleSpinner.setSelection(rolePosition);

        activeCheckBox.setChecked(user.isActive());

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Edit User: " + user.getUsername())
                .setView(dialogView)
                .setPositiveButton("Save Changes", (dialog, which) -> {
                    String username = usernameEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    String fullName = fullNameEditText.getText().toString().trim();
                    String role = roleSpinner.getSelectedItem().toString();
                    boolean isActive = activeCheckBox.isChecked();

                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if trying to deactivate the last admin
                    if (!isActive && "admin".equals(user.getRole())) {
                        List<User> activeAdmins = userDAO.getActiveAdmins();
                        if (activeAdmins.size() <= 1) {
                            Toast.makeText(this, "Cannot deactivate the last admin user", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Update user object
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setFullName(fullName);
                    user.setRole(role);
                    user.setActive(isActive);

                    boolean result = userDAO.updateUser(user);
                    if (result) {
                        Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        loadAllUsers();
                    } else {
                        Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        // Add delete option if not the current user
        if (user.getUserId() != currentUser.getUserId()) {
            builder.setNeutralButton("Delete User", (dialog, which) -> {
                showDeleteUserConfirmation(user);
            });
        }

        builder.show();
    }

    private void showDeleteUserConfirmation(User user) {
        // Check if trying to delete the last admin
        if ("admin".equals(user.getRole())) {
            List<User> activeAdmins = userDAO.getActiveAdmins();
            if (activeAdmins.size() <= 1) {
                Toast.makeText(this, "Cannot delete the last admin user", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user '" + user.getUsername() + "'?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean result = userDAO.deleteUser(user.getUserId());
                    if (result) {
                        Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                        loadAllUsers();
                    } else {
                        Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadAllItems() {
        Log.d(TAG, "Loading all items");
        try {
            allItems.clear();
            allItems.addAll(itemDAO.getAllItems());

            filterItems();
            updateItemsCount();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterItems() {
        filteredItems.clear();

        String selectedCategory = categoryFilterSpinner.getSelectedItem().toString();
        String searchTerm = itemSearchEditText.getText().toString().toLowerCase().trim();

        for (Item item : allItems) {
            boolean matchesCategory = "All Categories".equals(selectedCategory) ||
                    selectedCategory.equals(item.getCategory());

            boolean matchesSearch = searchTerm.isEmpty() ||
                    item.getName().toLowerCase().contains(searchTerm) ||
                    item.getCategory().toLowerCase().contains(searchTerm);

            if (matchesCategory && matchesSearch) {
                filteredItems.add(item);
            }
        }

        if (itemsAdapter != null) {
            itemsAdapter.notifyDataSetChanged();
        }
        updateItemsCount();
    }

    private void updateItemsCount() {
        if (itemsCountText != null) {
            itemsCountText.setText("Items: " + filteredItems.size() + " of " + allItems.size());
        }
    }

    private void showAddItemDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);

        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        CheckBox adaFriendlyCheckBox = dialogView.findViewById(R.id.adaFriendlyCheckBox);

        // Setup category spinner (exclude "All Categories")
        List<String> itemCategories = categories.subList(1, categories.size());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, itemCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add New Item")
                .setView(dialogView)
                .setPositiveButton("Add Item", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String category = categorySpinner.getSelectedItem().toString();
                    boolean isAdaFriendly = adaFriendlyCheckBox.isChecked();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Item newItem = new Item(name, category, isAdaFriendly);
                    long result = itemDAO.addItem(newItem);
                    if (result > 0) {
                        Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                        loadAllItems();
                    } else {
                        Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
                handleBackNavigation();
                return true;
            case R.id.action_home:
                goToMainMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        handleBackNavigation();
    }

    private void handleBackNavigation() {
        if (usersContainer.getVisibility() == View.VISIBLE ||
                itemsContainer.getVisibility() == View.VISIBLE) {
            if (wasLaunchedWithDirectMode) {
                // If launched directly to users/items, go back to main menu
                goToMainMenu();
            } else {
                // Otherwise, show the admin main menu
                showMainMenu();
            }
        } else {
            // We're on the main menu, go back to main app
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
        private Context context;
        private List<User> users;

        public UserAdapter(Context context, List<User> users) {
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
            roleText.setTextColor(textColor);

            return convertView;
        }
    }

    // Item Adapter Class
    private class ItemAdapter extends BaseAdapter {
        private Context context;
        private List<Item> items;

        public ItemAdapter(Context context, List<Item> items) {
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
                convertView = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
            }

            Item item = items.get(position);

            TextView nameText = convertView.findViewById(R.id.nameText);
            TextView categoryText = convertView.findViewById(R.id.categoryText);
            TextView adaText = convertView.findViewById(R.id.adaText);

            nameText.setText(item.getName());
            categoryText.setText(item.getCategory());
            adaText.setText(item.isAdaFriendly() ? "ADA Friendly" : "Regular");

            return convertView;
        }
    }
}