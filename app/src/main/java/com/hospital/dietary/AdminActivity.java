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
            } else {
                Log.e(TAG, "User no longer exists: " + currentUsername);
                redirectToLogin();
                return;
            }
        }
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private boolean isUserAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }
    
    private void configureAdminMenu() {
        // FIXED: Ensure current user info is displayed correctly
        if (currentUser != null) {
            setTitle("Admin Panel - " + currentUser.getFullName());
        } else {
            setTitle("Admin Panel");
        }
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
        
        // FIXED: Back button behavior based on how activity was launched
        backToMenuButton.setOnClickListener(v -> {
            if (wasLaunchedWithDirectMode) {
                // If we came directly to users/items management, go back to MainMenuActivity
                finish(); // This will return to MainMenuActivity
            } else {
                // If we navigated normally, show AdminActivity's main menu
                showMainMenu();
            }
        });
        
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
                                    item.getCategoryName().equals(selectedCategory);
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
    
    private void showUserDialog(User user) {
        // Implementation for user dialog would go here
        Toast.makeText(this, "User dialog not implemented yet", Toast.LENGTH_SHORT).show();
    }
    
    private void showItemDialog(Item item) {
        // Implementation for item dialog would go here
        Toast.makeText(this, "Item dialog not implemented yet", Toast.LENGTH_SHORT).show();
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
}