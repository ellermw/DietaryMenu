package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.User;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private UserDAO userDAO;

    // UI Components
    private EditText usernameEditText;
    private EditText passwordEditText;
    private CheckBox showPasswordCheckBox;
    private Button signInButton;
    private TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);

        // Schedule automatic order creation at 4:00 AM if not already scheduled
        scheduleAutoOrderCreationIfNeeded();

        // Initialize UI components
        initializeViews();

        // Set up listeners
        setupListeners();

        // Create default admin if needed
        createDefaultAdminIfNeeded();
    }

    private void scheduleAutoOrderCreationIfNeeded() {
        SharedPreferences sharedPreferences = getSharedPreferences("DietaryAppPrefs", MODE_PRIVATE);
        boolean isScheduled = sharedPreferences.getBoolean("auto_order_scheduled", false);

        if (!isScheduled) {
            // Schedule the automatic order creation service
            AutoOrderCreationService.scheduleAutoOrderCreation(this);

            // Mark as scheduled
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("auto_order_scheduled", true);
            editor.apply();
        }
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        signInButton = findViewById(R.id.signInButton);
        versionText = findViewById(R.id.versionText);

        // Set version text
        if (versionText != null) {
            versionText.setText("Version 0.1.20");
        }
    }

    private void setupListeners() {
        // Show/hide password toggle
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Sign in button
        signInButton.setOnClickListener(v -> attemptLogin());
    }

    private void createDefaultAdminIfNeeded() {
        try {
            userDAO.createDefaultAdminIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        // Validate login
        User user = userDAO.validateLogin(username, password);

        if (user != null) {
            // Update last login
            userDAO.updateLastLogin(user.getUserId());

            // Check if password change is required
            if (user.isMustChangePassword()) {
                showChangePasswordDialog(user);
            } else {
                navigateToMainMenu(user);
            }
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password Change Required");
        builder.setMessage("You must change your password before continuing.");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter new password");
        builder.setView(input);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT).show();
                showChangePasswordDialog(user);
            } else {
                changePassword(user, newPassword);
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void changePassword(User user, String newPassword) {
        try {
            boolean success = userDAO.changePassword(user.getUserId(), newPassword);
            if (success) {
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                navigateToMainMenu(user);
            } else {
                Toast.makeText(this, "Password change failed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error changing password: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToMainMenu(User user) {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", user.getUsername());
        intent.putExtra("user_role", user.getRole());
        intent.putExtra("user_full_name", user.getFullName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}