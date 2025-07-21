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
    private Button defaultMenuManagementButton;  // NEW
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
        defaultMenuManagementButton = findViewById(R.id.defaultMenuManagementButton);  // NEW
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
                // TODO: Implement UserManagementActivity
                Toast.makeText(this, "User Management - Coming Soon!", Toast.LENGTH_SHORT).show();
            });
        }

        if (itemManagementButton != null) {
            itemManagementButton.setOnClickListener(v -> {
                // TODO: Implement ItemManagementActivity
                Toast.makeText(this, "Item Management - Coming Soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // NEW: Default Menu Management Button
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
                // TODO: Implement AccountManagementActivity
                Toast.makeText(this, "Account Management - Coming Soon!", Toast.LENGTH_SHORT).show();
            });
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}