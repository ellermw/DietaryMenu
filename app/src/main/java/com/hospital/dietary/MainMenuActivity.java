package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

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

        // Configure buttons based on user role
        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            // Admin can see all buttons - no changes needed
            userManagementButton.setEnabled(true);
            itemManagementButton.setEnabled(true);
        } else {
            // Non-admin users - hide admin tools
            userManagementButton.setEnabled(false);
            userManagementButton.setAlpha(0.5f);
            itemManagementButton.setEnabled(false);
            itemManagementButton.setAlpha(0.5f);
        }
    }

    private void setupListeners() {
        // Operations Section
        patientInfoButton.setOnClickListener(v -> openPatientInfo());
        pendingOrdersButton.setOnClickListener(v -> openPendingOrders());
        retiredOrdersButton.setOnClickListener(v -> openRetiredOrders());

        // Documents Section
        productionSheetsButton.setOnClickListener(v -> openProductionSheets());
        stockSheetsButton.setOnClickListener(v -> openStockSheets());

        // Admin Tools Section
        userManagementButton.setOnClickListener(v -> {
            if ("Admin".equalsIgnoreCase(currentUserRole)) {
                openUserManagement();
            } else {
                showAccessDeniedMessage();
            }
        });

        itemManagementButton.setOnClickListener(v -> {
            if ("Admin".equalsIgnoreCase(currentUserRole)) {
                openItemManagement();
            } else {
                showAccessDeniedMessage();
            }
        });

        // Logout
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

    private void openProductionSheets() {
        // Placeholder - to be implemented later
        Toast.makeText(this, "Production Sheets - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void openStockSheets() {
        // Placeholder - to be implemented later
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

    private void showAccessDeniedMessage() {
        Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear user session and return to login
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
        int id = item.getItemId();

        if (id == R.id.action_account) {
            openAccountManagement();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openAccountManagement() {
        Intent intent = new Intent(this, AccountManagementActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user interface when returning to this activity
        setupUserInterface();
    }

    @Override
    public void onBackPressed() {
        // Show logout confirmation when back button is pressed from main menu
        showLogoutConfirmation();
    }
}