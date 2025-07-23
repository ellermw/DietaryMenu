package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView welcomeText;
    private Button patientInfoButton;
    private Button pendingOrdersButton;
    private Button retiredOrdersButton;
    private Button productionSheetsButton;
    private Button stockSheetsButton;
    private Button userManagementButton;
    private Button itemManagementButton;
    private Button logoutButton;

    // Admin tools section container
    private LinearLayout adminToolsSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Ensure we have at least basic user info
        if (currentUsername == null) currentUsername = "User";
        if (currentUserRole == null) currentUserRole = "Staff";

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hospital Dietary Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // Initialize UI
        initializeViews();
        setupUserInterface();
        setupListeners();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        patientInfoButton = findViewById(R.id.patientInfoButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        retiredOrdersButton = findViewById(R.id.retiredOrdersButton);
        productionSheetsButton = findViewById(R.id.productionSheetsButton);
        stockSheetsButton = findViewById(R.id.stockSheetsButton);
        userManagementButton = findViewById(R.id.userManagementButton);
        itemManagementButton = findViewById(R.id.itemManagementButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Get reference to admin tools section container
        adminToolsSection = findViewById(R.id.adminToolsSection);
    }

    private void setupUserInterface() {
        // Set welcome message
        String welcomeMessage = "Welcome, ";
        if (currentUserFullName != null && !currentUserFullName.trim().isEmpty()) {
            welcomeMessage += currentUserFullName + "!";
        } else {
            welcomeMessage += currentUsername + "!";
        }
        welcomeText.setText(welcomeMessage);

        // Configure admin tools section based on user role
        configureAdminAccess();
    }

    private void configureAdminAccess() {
        // Check for both "Admin" and "Administrator" role values (case-insensitive)
        boolean isAdmin = currentUserRole != null &&
                ("Admin".equalsIgnoreCase(currentUserRole.trim()) ||
                        "Administrator".equalsIgnoreCase(currentUserRole.trim()));

        // Debug logging to see what role we're getting
        android.util.Log.d("MainMenu", "Current user role: '" + currentUserRole + "', isAdmin: " + isAdmin);

        if (adminToolsSection != null) {
            if (isAdmin) {
                // Show admin tools section for admin users
                adminToolsSection.setVisibility(View.VISIBLE);
                android.util.Log.d("MainMenu", "Admin tools section made visible");
            } else {
                // Hide admin tools section for non-admin users
                adminToolsSection.setVisibility(View.GONE);
                android.util.Log.d("MainMenu", "Admin tools section hidden for non-admin user");
            }
        } else {
            android.util.Log.e("MainMenu", "Admin tools section is null!");
        }
    }

    private void setupListeners() {
        // Operations Section
        if (patientInfoButton != null) {
            patientInfoButton.setOnClickListener(v -> openPatientInfo());
        }
        if (pendingOrdersButton != null) {
            pendingOrdersButton.setOnClickListener(v -> openPendingOrders());
        }
        if (retiredOrdersButton != null) {
            retiredOrdersButton.setOnClickListener(v -> openRetiredOrders());
        }

        // Documents Section - FIXED: Show "Coming Soon" instead of navigating
        if (productionSheetsButton != null) {
            productionSheetsButton.setOnClickListener(v -> openProductionSheets());
        }
        if (stockSheetsButton != null) {
            stockSheetsButton.setOnClickListener(v -> openStockSheets());
        }

        // Admin Tools Section - FIXED: Removed role checking since section is hidden for non-admins
        if (userManagementButton != null) {
            userManagementButton.setOnClickListener(v -> openUserManagement());
        }

        if (itemManagementButton != null) {
            itemManagementButton.setOnClickListener(v -> openItemManagement());
        }

        // Logout
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        }
    }

    // Navigation methods
    private void openPatientInfo() {
        Intent intent = new Intent(this, PatientInfoMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openPendingOrders() {
        Intent intent = new Intent(this, PendingOrdersActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openRetiredOrders() {
        Intent intent = new Intent(this, RetiredOrdersActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openProductionSheets() {
        // FIXED: Show "coming soon" message instead of navigating to DocumentsActivity
        Toast.makeText(this, "Production Sheets - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void openStockSheets() {
        // FIXED: Show "coming soon" message instead of navigating to DocumentsActivity
        Toast.makeText(this, "Stock Sheets - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void openUserManagement() {
        Intent intent = new Intent(this, UserManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openItemManagement() {
        Intent intent = new Intent(this, ItemManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // For main menu, we don't need a complex menu - just keep it simple
        // Most functionality is accessible through the main interface
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle any basic menu items here if needed
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh admin access configuration when returning to main menu
        configureAdminAccess();
    }
}