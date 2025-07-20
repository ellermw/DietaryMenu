package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // FIXED: Added missing import
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
        // FIXED: Don't look for defaultMenuButton if it doesn't exist in layout
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
        // FIXED: Add default menu management as a menu option instead of button
        backToMenuButton.setOnClickListener(v -> handleBackNavigation());

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
        setTitle("Admin Panel - " + (currentUser != null ? currentUser.getFullName() : "Administrator"));
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

    // FIXED: Default menu management opens as separate activity
    private void showDefaultMenuManagement() {
        Intent intent = new Intent(this, DefaultMenuManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void hideAllContainers() {
        mainMenuContainer.setVisibility(View.GONE);
        usersContainer.setVisibility(View.GONE);
        itemsContainer.setVisibility(View.GONE);
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

    private void handleBackNavigation() {
        if (wasLaunchedWithDirectMode) {
            if (mainMenuContainer.getVisibility() == View.VISIBLE) {
                goToMainMenu();
            } else {
                showMainMenu();
            }
        } else {
            showMainMenu();
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
    public void onBackPressed() {
        if (mainMenuContainer.getVisibility() == View.VISIBLE) {
            if (wasLaunchedWithDirectMode) {
                goToMainMenu();
            } else {
                showMainMenu();
            }
        } else {
            goToMainMenu();
        }
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
        // Add default menu management as menu option
        menu.add(0, R.id.action_default_menus, 0, "Default Menus").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
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
            case R.id.action_default_menus:
                showDefaultMenuManagement();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}