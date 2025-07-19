package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;

public class MainMenuActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    private TextView welcomeText;
    private Button patientInfoButton;
    private Button pendingOrdersButton;
    private Button retiredOrdersButton;
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

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dietary Management - Main Menu");
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
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void setupListeners() {
        patientInfoButton.setOnClickListener(v -> openPatientInfo());
        pendingOrdersButton.setOnClickListener(v -> openPendingOrders());
        retiredOrdersButton.setOnClickListener(v -> openRetiredOrders());
        userManagementButton.setOnClickListener(v -> openUserManagement());
        itemManagementButton.setOnClickListener(v -> openItemManagement());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void updateWelcomeMessage() {
        if (welcomeText != null && currentUserFullName != null) {
            String welcome = "Welcome, " + currentUserFullName + "!";
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
        if (!"admin".equals(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // For now, show a message since AdminMenuActivity doesn't exist yet
        Toast.makeText(this, "User Management - Coming Soon!\nAdmin features will be available in future updates.", Toast.LENGTH_LONG).show();
    }

    private void openItemManagement() {
        if (!"admin".equals(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // For now, show a message since ItemManagementActivity doesn't exist yet
        Toast.makeText(this, "Item Management - Coming Soon!\nFood item management will be available in future updates.", Toast.LENGTH_LONG).show();
    }

    private void logout() {
        // Clear any stored session data if needed
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Use the existing menu with home instead of missing menu_main
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateStatistics();
                Toast.makeText(this, "Statistics refreshed", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_home:
                // Already on main menu, just show a message
                Toast.makeText(this, "You are already on the Main Menu", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateStatistics() {
        try {
            int patientCount = patientDAO.getPatientCount();
            int pendingCount = patientDAO.getPendingOrdersCount();

            // Update button texts with counts
            if (patientInfoButton != null) {
                patientInfoButton.setText("👤 Patient Information\n(" + patientCount + " patients)");
            }
            if (pendingOrdersButton != null) {
                pendingOrdersButton.setText("⏳ Pending Orders\n(" + pendingCount + " pending)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatistics();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to login screen accidentally
        moveTaskToBack(true);
    }
}