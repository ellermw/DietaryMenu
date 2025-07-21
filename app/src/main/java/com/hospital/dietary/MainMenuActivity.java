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

    // User data
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView welcomeText;
    private LinearLayout operationsSection;
    private LinearLayout documentsSection;
    private LinearLayout adminToolsSection;

    // Operation buttons
    private Button patientInfoButton;
    private Button pendingOrdersButton;
    private Button retiredOrdersButton;

    // Documents buttons (renamed from Account Management)
    private Button productionDocumentsButton;
    private Button stockSheetsButton;

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

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize views
        initializeViews();

        // Update UI based on user role
        updateUI();

        // Set up click listeners
        setupListeners();
    }

    private void initializeViews() {
        // Find views
        welcomeText = findViewById(R.id.welcomeText);
        operationsSection = findViewById(R.id.operationsSection);
        documentsSection = findViewById(R.id.documentsSection);
        adminToolsSection = findViewById(R.id.adminToolsSection);

        // Operations buttons
        patientInfoButton = findViewById(R.id.patientInfoButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        retiredOrdersButton = findViewById(R.id.retiredOrdersButton);

        // Documents buttons (renamed from Account Management)
        productionDocumentsButton = findViewById(R.id.productionDocumentsButton);
        stockSheetsButton = findViewById(R.id.stockSheetsButton);

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

        // Documents buttons (renamed from Account Management)
        productionDocumentsButton.setOnClickListener(v -> openProductionDocuments());
        stockSheetsButton.setOnClickListener(v -> openStockSheets());

        // Admin buttons - Navigate directly to management pages
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

    private void openProductionDocuments() {
        // TODO: Implement Production Documents functionality
        Toast.makeText(this, "Production Documents - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void openStockSheets() {
        // TODO: Implement Stock Sheets functionality
        Toast.makeText(this, "Stock Sheets - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void openUserManagement() {
        // Navigate directly to UserManagementActivity
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
        // Navigate directly to ItemManagementActivity
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

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                showLogoutConfirmation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Show logout confirmation when back button is pressed
        showLogoutConfirmation();
    }
}