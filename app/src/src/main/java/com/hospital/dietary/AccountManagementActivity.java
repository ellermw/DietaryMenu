package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.User;

public class AccountManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private UserDAO userDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    private User currentUser;

    // UI Components
    private TextView welcomeText;
    private TextView usernameText;
    private TextView roleText;
    private TextView lastLoginText;
    private Button changePasswordButton;
    private LinearLayout adminSection;
    private Button manageUsersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Account Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        loadUserData();
        setupListeners();
    }

    private void initializeUI() {
        welcomeText = findViewById(R.id.welcomeText);
        usernameText = findViewById(R.id.usernameText);
        roleText = findViewById(R.id.roleText);
        lastLoginText = findViewById(R.id.lastLoginText);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        adminSection = findViewById(R.id.adminSection);
        manageUsersButton = findViewById(R.id.manageUsersButton);

        // Show admin section only for admin users
        boolean isAdmin = "admin".equals(currentUserRole);
        adminSection.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void loadUserData() {
        try {
            currentUser = userDAO.getUserByUsername(currentUsername);
            if (currentUser == null) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Populate UI with user data
            welcomeText.setText("Welcome, " + currentUser.getFullName() + "!");
            usernameText.setText("Username: " + currentUser.getUsername());
            roleText.setText("Role: " + currentUser.getRoleDisplayName());

            if (currentUser.getLastLogin() != null) {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault());
                lastLoginText.setText("Last login: " + dateFormat.format(currentUser.getLastLogin()));
            } else {
                lastLoginText.setText("Last login: Never");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        if (manageUsersButton != null) {
            manageUsersButton.setOnClickListener(v -> openUserManagement());
        }
    }

    // FEATURE: Allow users to change their own password
    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        EditText currentPasswordEditText = dialogView.findViewById(R.id.currentPasswordEditText);
        EditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.confirmPasswordEditText);
        CheckBox showPasswordsCheckBox = dialogView.findViewById(R.id.showPasswordsCheckBox);

        // Show/hide password toggle
        showPasswordsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked ?
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) :
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            currentPasswordEditText.setInputType(inputType);
            newPasswordEditText.setInputType(inputType);
            confirmPasswordEditText.setInputType(inputType);

            // Maintain cursor positions
            currentPasswordEditText.setSelection(currentPasswordEditText.getText().length());
            newPasswordEditText.setSelection(newPasswordEditText.getText().length());
            confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change Password", null) // Set to null initially
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String currentPassword = currentPasswordEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                // Validate current password
                if (currentPassword.isEmpty()) {
                    currentPasswordEditText.setError("Current password is required");
                    return;
                }

                if (!currentPassword.equals(currentUser.getPassword())) {
                    currentPasswordEditText.setError("Current password is incorrect");
                    return;
                }

                // Validate new password
                if (newPassword.isEmpty()) {
                    newPasswordEditText.setError("New password is required");
                    return;
                }

                if (newPassword.length() < 6) {
                    newPasswordEditText.setError("Password must be at least 6 characters");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordEditText.setError("Passwords do not match");
                    return;
                }

                if (newPassword.equals(currentPassword)) {
                    newPasswordEditText.setError("New password must be different from current password");
                    return;
                }

                // Update password
                currentUser.setPassword(newPassword);
                boolean result = userDAO.updateUser(currentUser);

                if (result) {
                    dialog.dismiss();
                    Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void openUserManagement() {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.putExtra("admin_mode", "users");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_home:
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                showLogoutConfirmation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}