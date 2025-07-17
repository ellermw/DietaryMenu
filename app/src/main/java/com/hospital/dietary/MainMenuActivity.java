package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.User;

public class MainMenuActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private UserDAO userDAO;
    
    // Current user info
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    private boolean isAdmin = false;
    
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
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        isAdmin = "admin".equalsIgnoreCase(currentUserRole);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Configure menu based on user role
        configureMenuForUserRole();
        
        // Show welcome message
        if (currentUserFullName != null) {
            Toast.makeText(this, "Welcome, " + currentUserFullName + "!", Toast.LENGTH_SHORT).show();
        }
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
        }
        
        // Set title
        setTitle("Main Menu");
    }
    
    private void setupListeners() {
        patientInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientInfoMenuActivity.class);
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
        
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });
        
        userManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.putExtra("show_users", true);
            startActivity(intent);
        });
        
        itemManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.putExtra("show_items", true);
            startActivity(intent);
        });
        
        logoutButton.setOnClickListener(v -> {
            // Clear any stored user session data if needed
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Go back to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private void configureMenuForUserRole() {
        if (isAdmin) {
            // Show admin-specific buttons
            adminButton.setVisibility(Button.VISIBLE);
            userManagementButton.setVisibility(Button.VISIBLE);
            itemManagementButton.setVisibility(Button.VISIBLE);
        } else {
            // Hide admin-specific buttons for regular users
            adminButton.setVisibility(Button.GONE);
            userManagementButton.setVisibility(Button.GONE);
            itemManagementButton.setVisibility(Button.GONE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh welcome message when returning to main menu
        if (currentUserFullName != null) {
            welcomeText.setText("Welcome, " + currentUserFullName + "!");
        }
    }
}