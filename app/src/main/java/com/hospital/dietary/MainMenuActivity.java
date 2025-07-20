package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView welcomeText;
    private Button patientInfoButton;
    private Button pendingOrdersButton;
    private Button retiredOrdersButton;
    private Button userManagementButton;
    private Button itemManagementButton;
    private Button accountManagementButton; // FEATURE: Account management button
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dietary Management");
        }

        initializeUI();
        setupListeners();
        updateWelcomeMessage();
        updateButtonVisibility();
    }

    private void initializeUI() {
        welcomeText = findViewById(R.id.welcomeText);
        patientInfoButton = findViewById(R.id.patientInfoButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        retiredOrdersButton = findViewById(R.id.retiredOrdersButton);
        userManagementButton = findViewById(R.id.userManagementButton);
        itemManagementButton = findViewById(R.id.itemManagementButton);
        accountManagementButton = findViewById(R.id.accountManagementButton); // FEATURE: New button
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupListeners() {
        patientInfoButton.setOnClickListener(v -> openPatientInfo());
        pendingOrdersButton.setOnClickListener(v -> openPendingOrders());
        retiredOrdersButton.setOnClickListener(v -> openRetiredOrders());
        userManagementButton.setOnClickListener(v -> openUserManagement());
        itemManagementButton.setOnClickListener(v -> openItemManagement());
        accountManagementButton.setOnClickListener(v -> openAccountManagement()); // FEATURE: New listener
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void updateWelcomeMessage() {
        if (welcomeText != null && currentUserFullName != null) {
            String welcome = getString(R.string.welcome_message, currentUserFullName);
            if ("admin".equals(currentUserRole)) {
                welcome += "\n(Administrator Access)";
            }
            welcomeText.setText(welcome);
        }
    }

    private void updateButtonVisibility() {
        // Show admin buttons only for admin users
        boolean isAdmin = "admin".equals(currentUserRole);

        if (userManagementButton != null) {
            userManagementButton.setVisibility(isAdmin ? Button.VISIBLE : Button.GONE);
        }
        if (itemManagementButton != null) {
            itemManagementButton.setVisibility(isAdmin ? Button.VISIBLE : Button.GONE);
        }

        // FEATURE: Account management button is always visible for all users
        if (accountManagementButton != null) {
            accountManagementButton.setVisibility(Button.VISIBLE);
        }
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

    /**
     * FIXED: Open AdminActivity for user management
     */
    private void openUserManagement() {
        if (!"admin".equals(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.putExtra("admin_mode", "users"); // Direct to user management
        startActivity(intent);
    }

    /**
     * FIXED: Open AdminActivity for item management
     */
    private void openItemManagement() {
        if (!"admin".equals(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.putExtra("admin_mode", "items"); // Direct to item management
        startActivity(intent);
    }

    /**
     * FEATURE: Open account management for password changes
     */
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
}