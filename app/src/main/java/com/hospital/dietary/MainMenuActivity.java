package com.hospital.dietary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.User;

public class MainMenuActivity extends AppCompatActivity {
    
    private static final String TAG = "MainMenuActivity";
    private static final String PREFS_NAME = "UserSession";
    private static final String KEY_USERNAME = "current_username";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_FULL_NAME = "user_full_name";
    
    private DatabaseHelper dbHelper;
    private UserDAO userDAO;
    
    // Current user info - FIXED: Enhanced state management
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    private boolean isAdmin = false;
    private User currentUser;
    
    // UI Components
    private TextView welcomeText;
    private Button patientInfoButton;
    private Button pendingOrdersButton;
    private Button finishedOrdersButton;
    private Button adminButton;
    private Button userManagementButton;
    private Button itemManagementButton;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        
        Log.d(TAG, "MainActivity onCreate started");
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);
        
        // FIXED: Load user information with persistence
        loadUserInformation();
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // FIXED: Configure menu based on current user state
        configureMenuForUserRole();
        
        // Show welcome message
        if (currentUserFullName != null) {
            Toast.makeText(this, "Welcome, " + currentUserFullName + "!", Toast.LENGTH_SHORT).show();
        }
        
        Log.d(TAG, "MainActivity onCreate completed - User: " + currentUsername + ", Role: " + currentUserRole + ", IsAdmin: " + isAdmin);
    }
    
    // FIXED: Enhanced user information loading with persistence
    private void loadUserInformation() {
        // First try to get from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        // If not in intent, try to load from SharedPreferences
        if (currentUsername == null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUsername = prefs.getString(KEY_USERNAME, null);
            currentUserRole = prefs.getString(KEY_USER_ROLE, null);
            currentUserFullName = prefs.getString(KEY_USER_FULL_NAME, null);
            Log.d(TAG, "Loaded user from SharedPreferences: " + currentUsername);
        } else {
            // Save to SharedPreferences for persistence
            saveUserToPreferences();
            Log.d(TAG, "Loaded user from intent and saved to preferences: " + currentUsername);
        }
        
        // Validate and refresh user information from database
        if (currentUsername != null) {
            currentUser = userDAO.getUserByUsername(currentUsername);
            if (currentUser != null) {
                currentUserRole = currentUser.getRole();
                currentUserFullName = currentUser.getFullName();
                isAdmin = "admin".equalsIgnoreCase(currentUserRole);
                
                // Update SharedPreferences with latest info
                saveUserToPreferences();
                
                Log.d(TAG, "User validated from database: " + currentUsername + " - Role: " + currentUserRole);
            } else {
                Log.w(TAG, "User not found in database: " + currentUsername);
                // Clear invalid session
                clearUserSession();
            }
        }
        
        // If still no valid user, redirect to login
        if (currentUsername == null || currentUser == null) {
            Log.w(TAG, "No valid user session found, redirecting to login");
            redirectToLogin();
        }
    }
    
    // FIXED: Save user information to SharedPreferences
    private void saveUserToPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, currentUsername);
        editor.putString(KEY_USER_ROLE, currentUserRole);
        editor.putString(KEY_USER_FULL_NAME, currentUserFullName);
        editor.apply();
        Log.d(TAG, "User session saved to preferences");
    }
    
    // FIXED: Clear user session
    private void clearUserSession() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();
        currentUsername = null;
        currentUserRole = null;
        currentUserFullName = null;
        currentUser = null;
        isAdmin = false;
        Log.d(TAG, "User session cleared");
    }
    
    // FIXED: Redirect to login activity
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void initializeUI() {
        welcomeText = findViewById(R.id.welcomeText);
        patientInfoButton = findViewById(R.id.patientInfoButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        finishedOrdersButton = findViewById(R.id.finishedOrdersButton);
        adminButton = findViewById(R.id.adminButton);
        userManagementButton = findViewById(R.id.userManagementButton);
        itemManagementButton = findViewById(R.id.itemManagementButton);
        logoutButton = findViewById(R.id.logoutButton);
        
        // Set welcome message
        if (currentUserFullName != null) {
            welcomeText.setText("Welcome, " + currentUserFullName + "!");
        } else {
            welcomeText.setText("Welcome!");
        }
    }
    
    private void setupListeners() {
        patientInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientInfoActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });
        
        pendingOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PendingOrdersActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });
        
        finishedOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FinishedOrdersActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });
        
        // FIXED: Admin panel navigation with proper user context
        adminButton.setOnClickListener(v -> {
            if (isAdmin) {
                Intent intent = new Intent(this, AdminActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            }
        });
        
        // FIXED: User management navigation with proper user context
        userManagementButton.setOnClickListener(v -> {
            if (isAdmin) {
                Intent intent = new Intent(this, AdminActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.putExtra("show_users", true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            }
        });
        
        // FIXED: Item management navigation with proper user context
        itemManagementButton.setOnClickListener(v -> {
            if (isAdmin) {
                Intent intent = new Intent(this, AdminActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.putExtra("show_items", true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            }
        });
        
        // FIXED: Enhanced logout with session cleanup
        logoutButton.setOnClickListener(v -> {
            Log.d(TAG, "User logout initiated");
            
            // Clear user session data
            clearUserSession();
            
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Go back to login screen
            redirectToLogin();
        });
    }
    
    // FIXED: Enhanced role-based menu configuration
    private void configureMenuForUserRole() {
        Log.d(TAG, "Configuring menu for role: " + currentUserRole + ", isAdmin: " + isAdmin);
        
        if (isAdmin) {
            // Show admin-specific buttons
            adminButton.setVisibility(Button.VISIBLE);
            userManagementButton.setVisibility(Button.VISIBLE);
            itemManagementButton.setVisibility(Button.VISIBLE);
            Log.d(TAG, "Admin menu buttons made visible");
        } else {
            // Hide admin-specific buttons for regular users
            adminButton.setVisibility(Button.GONE);
            userManagementButton.setVisibility(Button.GONE);
            itemManagementButton.setVisibility(Button.GONE);
            Log.d(TAG, "Admin menu buttons hidden for regular user");
        }
    }
    
    // FIXED: Enhanced onResume to handle state restoration
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume called");
        
        // Refresh user information and validate session
        loadUserInformation();
        
        // Refresh welcome message when returning to main menu
        if (currentUserFullName != null) {
            welcomeText.setText("Welcome, " + currentUserFullName + "!");
        }
        
        // FIXED: Ensure proper menu configuration is maintained
        configureMenuForUserRole();
        
        Log.d(TAG, "MainActivity onResume completed - User: " + currentUsername + ", Role: " + currentUserRole);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity onPause called");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy called");
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    // FIXED: Method to check admin status
    private boolean isUserAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }
    
    // FIXED: Method to refresh user state (can be called from other activities)
    public void refreshUserState() {
        Log.d(TAG, "Refreshing user state");
        loadUserInformation();
        configureMenuForUserRole();
        
        if (currentUserFullName != null) {
            welcomeText.setText("Welcome, " + currentUserFullName + "!");
        }
    }
}