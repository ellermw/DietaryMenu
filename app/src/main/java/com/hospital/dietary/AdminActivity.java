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

    // FIXED: User Management Methods

    private void loadAllUsers() {
        try {
            allUsers = userDAO.getAllUsers();
            filteredUsers.clear();
            filteredUsers.addAll(allUsers);

            usersAdapter = new UserAdapter(this, filteredUsers);
            usersListView.setAdapter(usersAdapter);

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

                    long result = userDAO.addUser(newUser);
                    if (result > 0) {
                        Toast.makeText(this, "User " + username + " added successfully!", Toast.LENGTH_SHORT).show();
                        loadAllUsers();
                    } else {
                        Toast.makeText(this, "Failed to add user. Username may already exist.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // FIXED: Item Management Methods

    private void loadAllItems() {
        try {
            allItems = itemDAO.getAllItems();
            filteredItems.clear();
            filteredItems.addAll(allItems);

            itemsAdapter = new ItemAdapter(this, filteredItems);
            itemsListView.setAdapter(itemsAdapter);

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
                    item.getCategoryName().equals(selectedCategory);

            boolean matchesSearch = searchTerm.isEmpty() ||
                    item.getName().toLowerCase().contains(searchTerm) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchTerm));

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
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        Spinner mealTypeSpinner = dialogView.findViewById(R.id.mealTypeSpinner);
        EditText sizeInput = dialogView.findViewById(R.id.sizeInput);
        CheckBox adaFriendlyCheckBox = dialogView.findViewById(R.id.adaFriendlyCheckBox);
        CheckBox isSodaCheckBox = dialogView.findViewById(R.id.isSodaCheckBox);
        CheckBox isClearLiquidCheckBox = dialogView.findViewById(R.id.isClearLiquidCheckBox);
        CheckBox isDefaultCheckBox = dialogView.findViewById(R.id.isDefaultCheckBox);

        // Setup category spinner (exclude "All Categories")
        List<String> itemCategories = categories.subList(1, categories.size());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Setup meal type spinner
        String[] mealTypes = {"All", "Breakfast", "Lunch", "Dinner"};
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mealTypes);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(mealAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add New Food Item")
                .setView(dialogView)
                .setPositiveButton("Add Item", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    String category = categorySpinner.getSelectedItem().toString();
                    String mealType = mealTypeSpinner.getSelectedItem().toString();
                    String sizeText = sizeInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get category ID
                    int categoryId = categories.indexOf(category); // This will be 0-based, but our categories are 1-based
                    if (categoryId <= 0) categoryId = 1; // Default to first category

                    Item newItem = new Item();
                    newItem.setName(name);
                    newItem.setDescription(description);
                    newItem.setCategoryId(categoryId);
                    newItem.setMealType(mealType);
                    newItem.setAdaFriendly(adaFriendlyCheckBox.isChecked());
                    newItem.setSoda(isSodaCheckBox.isChecked());
                    newItem.setClearLiquid(isClearLiquidCheckBox.isChecked());
                    newItem.setDefault(isDefaultCheckBox.isChecked());

                    // Parse size if provided
                    if (!sizeText.isEmpty()) {
                        try {
                            int size = Integer.parseInt(sizeText);
                            newItem.setSizeML(size);
                        } catch (NumberFormatException e) {
                            // Invalid size, continue without it
                        }
                    }

                    long result = itemDAO.addItem(newItem);
                    if (result > 0) {
                        Toast.makeText(this, "Item " + name + " added successfully!", Toast.LENGTH_SHORT).show();
                        loadAllItems();
                    } else {
                        Toast.makeText(this, "Failed to add item. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // FIXED: Custom Adapters

    private class UserAdapter extends BaseAdapter {
        private Context context;
        private List<User> users;
        private LayoutInflater inflater;

        public UserAdapter(Context context, List<User> users) {
            this.context = context;
            this.users = users;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public User getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return users.get(position).getUserId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            User user = getItem(position);
            TextView primaryText = convertView.findViewById(android.R.id.text1);
            TextView secondaryText = convertView.findViewById(android.R.id.text2);

            primaryText.setText(user.getFullName() + " (" + user.getUsername() + ")");
            secondaryText.setText("Role: " + user.getRole() + " | Status: " + (user.isActive() ? "Active" : "Inactive"));

            return convertView;
        }
    }

    private class ItemAdapter extends BaseAdapter {
        private Context context;
        private List<Item> items;
        private LayoutInflater inflater;

        public ItemAdapter(Context context, List<Item> items) {
            this.context = context;
            this.items = items;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).getItemId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Item item = getItem(position);
            TextView primaryText = convertView.findViewById(android.R.id.text1);
            TextView secondaryText = convertView.findViewById(android.R.id.text2);

            primaryText.setText(item.getName());

            String details = item.getCategoryName() + " | " + item.getMealType();
            if (item.isAdaFriendly()) details += " | ADA";
            if (item.isClearLiquid()) details += " | Clear Liquid";
            if (item.getSizeML() != null) details += " | " + item.getSizeML() + "ml";

            secondaryText.setText(details);

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_home:
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                if (usersContainer.getVisibility() == View.VISIBLE) {
                    loadAllUsers();
                    Toast.makeText(this, "Users refreshed", Toast.LENGTH_SHORT).show();
                } else if (itemsContainer.getVisibility() == View.VISIBLE) {
                    loadAllItems();
                    Toast.makeText(this, "Items refreshed", Toast.LENGTH_SHORT).show();
                }
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