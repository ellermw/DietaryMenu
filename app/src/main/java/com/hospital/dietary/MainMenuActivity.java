package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    private Button patientInfoButton;
    private Button pendingOrdersButton;
    private Button finishedOrdersButton;
    private Button retiredOrdersButton;
    private Button logoutButton;

    // Admin-only buttons
    private Button userManagementButton;
    private Button itemManagementButton;
    private LinearLayout adminSection;

    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Setup action bar with title - NO back button, NO menu for main menu
        setupActionBar();

        initializeUI();
        setupListeners();
        configureAdminAccess();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dietary Management - Main Menu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // No back button
        }
    }

    // NO MENU FOR MAIN MENU - Remove onCreateOptionsMenu and onOptionsItemSelected
    // Logout is handled by the button in the layout

    private void initializeUI() {
        patientInfoButton = findViewById(R.id.patientInfoButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        finishedOrdersButton = findViewById(R.id.finishedOrdersButton);
        retiredOrdersButton = findViewById(R.id.retiredOrdersButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Admin-only UI elements
        adminSection = findViewById(R.id.adminSection);
        userManagementButton = findViewById(R.id.userManagementButton);
        itemManagementButton = findViewById(R.id.itemManagementButton);

        welcomeText = findViewById(R.id.welcomeText);

        // Set welcome text
        welcomeText.setText("Welcome, " + (currentUserFullName != null ? currentUserFullName : currentUsername) + "!");

        setTitle("Dietary Management - Main Menu");
    }

    private void configureAdminAccess() {
        // Show/hide admin options based on user role
        if ("admin".equalsIgnoreCase(currentUserRole)) {
            adminSection.setVisibility(View.VISIBLE);
        } else {
            adminSection.setVisibility(View.GONE);
        }
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

        retiredOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RetiredOrdersActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });

        // Admin-only button listeners
        if (userManagementButton != null) {
            userManagementButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.putExtra("admin_mode", "users");
                startActivity(intent);
            });
        }

        if (itemManagementButton != null) {
            itemManagementButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.putExtra("admin_mode", "items");
                startActivity(intent);
            });
        }

        // Logout button listener
        logoutButton.setOnClickListener(v -> {
            // Return to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}