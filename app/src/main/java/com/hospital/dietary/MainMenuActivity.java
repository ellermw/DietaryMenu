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
    private Button finishedOrdersButton;
    private Button pendingOrdersButton;
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
        finishedOrdersButton = findViewById(R.id.finishedOrdersButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        userManagementButton = findViewById(R.id.userManagementButton);
        itemManagementButton = findViewById(R.id.itemManagementButton);
        logoutButton = findViewById(R.id.logoutButton);
        
        // Set welcome message
        if (currentUserFullName != null) {
            welcomeText.setText("Welcome, " + currentUserFullName + "!");
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
        
        finishedOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FinishedOrdersActivity.class);
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
        
        userManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("admin_mode", "users");
            startActivity(intent);
        });
        
        itemManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("admin_mode", "items");
            startActivity(intent);
        });
        
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private void configureMenuForUserRole() {
        if (isAdmin) {
            // Admin can see all options
            userManagementButton.setVisibility(Button.VISIBLE);
            itemManagementButton.setVisibility(Button.VISIBLE);
        } else {
            // Regular users can't see admin options
            userManagementButton.setVisibility(Button.GONE);
            itemManagementButton.setVisibility(Button.GONE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI when returning to this activity
        configureMenuForUserRole();
    }
}