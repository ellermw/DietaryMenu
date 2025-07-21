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

    private DatabaseHelper dbHelper;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView welcomeText;
    private LinearLayout operationsSection;
    private LinearLayout yourAccountSection;
    private LinearLayout adminToolsSection;

    // Operations buttons
    private Button patientInfoButton;
    private Button pendingOrdersButton;
    private Button retiredOrdersButton;

    // Account button
    private Button accountManagementButton;

    // Admin buttons
    private Button userManagementButton;
    private Button itemManagementButton;
    private Button defaultMenuManagementButton;

    // Logout button
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        initializeViews();

        // Update UI based on user
        updateUI();

        // Set up button listeners
        setupListeners();
    }

    private void initializeViews() {
        // Header
        welcomeText = findViewById(R.id.welcomeText);

        // Sections
        operationsSection = findViewById(R.id.operationsSection);
        yourAccountSection = findViewById(R.id.yourAccountSection);
        adminToolsSection = findViewById(R.id.adminToolsSection);

        // Operations buttons
        patientInfoButton = findViewById(R.id.patientInfoButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        retiredOrdersButton = findViewById(R.id.retiredOrdersButton);

        // Account button
        accountManagementButton = findViewById(R.id.accountManagementButton);

        // Admin buttons
        userManagementButton = findViewById(R.id.userManagementButton);
        itemManagementButton = findViewById(R.id.itemManagementButton);
        defaultMenuManagementButton = findViewById(R.id.defaultMenuManagementButton);

        // Logout button
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void updateUI() {
        // Update welcome message
        if (welcomeText != null && currentUserFullName != null) {
            String roleDisplay = "Admin".equalsIgnoreCase(currentUserRole) ? "System Administrator" : currentUserFullName;
            welcomeText.setText("Welcome, " + roleDisplay + "!");
        }

        // Show/hide admin tools section based on role
        // FIXED: Check for "Admin" with capital A
        boolean isAdmin = "Admin".equalsIgnoreCase(currentUserRole);
        if (adminToolsSection != null) {
            adminToolsSection.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    private void setupListeners() {
        // Operations buttons
        patientInfoButton.setOnClickListener(v -> openPatientInfo());
        pendingOrdersButton.setOnClickListener(v -> openPendingOrders());
        retiredOrdersButton.setOnClickListener(v -> openRetiredOrders());

        // Account button
        accountManagementButton.setOnClickListener(v -> openAccountManagement());

        // Admin buttons
        if (userManagementButton != null) {
            userManagementButton.setOnClickListener(v -> openUserManagement());
        }
        if (itemManagementButton != null) {
            itemManagementButton.setOnClickListener(v -> openItemManagement());
        }
        if (defaultMenuManagementButton != null) {
            defaultMenuManagementButton.setOnClickListener(v -> openDefaultMenuManagement());
        }

        // Logout button
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
    }

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

    private void openUserManagement() {
        // FIXED: Navigate directly to UserManagementActivity
        if (!"Admin".equalsIgnoreCase(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, UserManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openItemManagement() {
        // FIXED: Navigate directly to ItemManagementActivity
        if (!"Admin".equalsIgnoreCase(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ItemManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openDefaultMenuManagement() {
        // NEW: Navigate directly to DefaultMenuManagementActivity
        if (!"Admin".equalsIgnoreCase(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DefaultMenuManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openAccountManagement() {
        Intent intent = new Intent(this, AccountManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_account:
                openAccountManagement();
                return true;
            case R.id.action_logout:
                showLogoutConfirmation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Show logout confirmation when back is pressed from main menu
        showLogoutConfirmation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}