package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = "MainMenuActivity";

    // User data
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    private boolean isAdmin = false;

    // UI Components - Operations Section
    private Button patientInfoButton;
    private Button pendingOrdersButton;
    private Button retiredOrdersButton;

    // UI Components - Documents Section
    private Button productionSheetsButton;
    private Button stockSheetsButton;

    // UI Components - Admin Tools Section
    private LinearLayout adminToolsSection;
    private Button userManagementButton;
    private Button itemManagementButton;
    private Button defaultMenuManagementButton;

    // UI Components - Logout
    private Button logoutButton;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Check if user is admin
        isAdmin = currentUserRole != null &&
                ("Admin".equalsIgnoreCase(currentUserRole.trim()) ||
                        "Administrator".equalsIgnoreCase(currentUserRole.trim()));

        Log.d(TAG, "User: " + currentUsername + ", Role: " + currentUserRole + ", IsAdmin: " + isAdmin);

        // Initialize UI
        initializeViews();
        updateUI();
        setupListeners();
    }

    private void initializeViews() {
        // Welcome text
        welcomeText = findViewById(R.id.welcomeText);

        // Operations Section
        patientInfoButton = findViewById(R.id.patientInfoButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        retiredOrdersButton = findViewById(R.id.retiredOrdersButton);

        // Documents Section
        productionSheetsButton = findViewById(R.id.productionSheetsButton);
        stockSheetsButton = findViewById(R.id.stockSheetsButton);

        // Admin Tools Section
        adminToolsSection = findViewById(R.id.adminToolsSection);
        userManagementButton = findViewById(R.id.userManagementButton);
        itemManagementButton = findViewById(R.id.itemManagementButton);
        defaultMenuManagementButton = findViewById(R.id.defaultMenuManagementButton);

        // Logout
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void updateUI() {
        // Update welcome message
        if (welcomeText != null && currentUserFullName != null) {
            welcomeText.setText("Welcome, " + currentUserFullName + "!");
        }

        // Show/hide admin section based on role
        if (adminToolsSection != null) {
            adminToolsSection.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
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
            productionSheetsButton.setOnClickListener(v -> {
                Toast.makeText(this, "Production Sheets - Coming Soon!", Toast.LENGTH_SHORT).show();
            });
        }
        if (stockSheetsButton != null) {
            stockSheetsButton.setOnClickListener(v -> {
                Toast.makeText(this, "Stock Sheets - Coming Soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Admin Tools Section
        if (userManagementButton != null) {
            userManagementButton.setOnClickListener(v -> openUserManagement());
        }
        if (itemManagementButton != null) {
            itemManagementButton.setOnClickListener(v -> openItemManagement());
        }
        if (defaultMenuManagementButton != null) {
            defaultMenuManagementButton.setOnClickListener(v -> openDefaultMenuManagement());
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

    private void openDefaultMenuManagement() {
        Intent intent = new Intent(this, DefaultMenuManagementActivity.class);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showLogoutConfirmation();
    }
}