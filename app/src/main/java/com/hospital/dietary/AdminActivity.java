package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView welcomeText;
    private Button userManagementButton;
    private Button itemManagementButton;
    private Button defaultMenuManagementButton;
    private Button accountManagementButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Check if user has admin privileges
        if (!"Admin".equals(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Panel");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        welcomeText = findViewById(R.id.welcomeText);
        userManagementButton = findViewById(R.id.userManagementButton);
        itemManagementButton = findViewById(R.id.itemManagementButton);
        defaultMenuManagementButton = findViewById(R.id.defaultMenuManagementButton);
        accountManagementButton = findViewById(R.id.accountManagementButton);
        backButton = findViewById(R.id.backButton);

        // Set welcome message
        if (welcomeText != null) {
            welcomeText.setText("Welcome to Admin Panel, " +
                    (currentUserFullName != null ? currentUserFullName : currentUsername) + "!");
        }
    }

    private void setupListeners() {
        if (userManagementButton != null) {
            userManagementButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, UserManagementActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
            });
        }

        if (itemManagementButton != null) {
            itemManagementButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ItemManagementActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
            });
        }

        if (defaultMenuManagementButton != null) {
            defaultMenuManagementButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, DefaultMenuManagementActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
            });
        }

        if (accountManagementButton != null) {
            accountManagementButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, AccountManagementActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
            });
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_admin, menu);
        } catch (Exception e) {
            // Menu file might not exist
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_home:
                // Go back to main menu
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_default_menus:
                // Open default menu management directly from menu
                Intent defaultMenuIntent = new Intent(this, DefaultMenuManagementActivity.class);
                defaultMenuIntent.putExtra("current_user", currentUsername);
                defaultMenuIntent.putExtra("user_role", currentUserRole);
                defaultMenuIntent.putExtra("user_full_name", currentUserFullName);
                startActivity(defaultMenuIntent);
                return true;
            case R.id.action_refresh:
                // Refresh the current view if needed
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}