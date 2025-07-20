package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
    private CheckBox showPasswordCheckBox; // FEATURE: Show password toggle
    private Button signInButton;
    private TextView versionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);

        initializeUI();
        setupListeners();

        // Ensure default admin user exists
        createDefaultAdminIfNeeded();
    }

    private void initializeUI() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox); // FEATURE: Password toggle
        signInButton = findViewById(R.id.signInButton);
        versionText = findViewById(R.id.versionText);

        // Set version text
        versionText.setText(getString(R.string.app_version));
    }

    private void setupListeners() {
        // FEATURE: Show/hide password toggle
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Hide password
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Move cursor to end
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Sign in button
        signInButton.setOnClickListener(v -> attemptLogin());

        // Enter key on password field
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            attemptLogin();
            return true;
        });
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        // Validate input
        if (username.isEmpty()) {
            usernameEditText.setError(getString(R.string.username_required));
            usernameEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.password_required));
            passwordEditText.requestFocus();
            return;
        }

        // Attempt authentication
        try {
            User user = userDAO.authenticateUser(username, password);

            if (user == null) {
                Toast.makeText(this, getString(R.string.invalid_credentials), Toast.LENGTH_LONG).show();
                return;
            }

            if (!user.isActive()) {
                Toast.makeText(this, getString(R.string.account_inactive), Toast.LENGTH_LONG).show();
                return;
            }

            // Update last login
            userDAO.updateLastLogin(user.getUserId());

            // FEATURE: Check if user must change password on first login
            if (user.isMustChangePassword()) {
                showForcePasswordChangeDialog(user);
            } else {
                // Successful login - proceed to main menu
                proceedToMainMenu(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
        }
    }

    // FEATURE: Force password change on first login
    private void showForcePasswordChangeDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_force_password_change, null);

        EditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.confirmPasswordEditText);
        CheckBox showNewPasswordCheckBox = dialogView.findViewById(R.id.showNewPasswordCheckBox);

        // Show/hide password toggle for new password
        showNewPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked ?
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) :
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            newPasswordEditText.setInputType(inputType);
            confirmPasswordEditText.setInputType(inputType);

            newPasswordEditText.setSelection(newPasswordEditText.getText().length());
            confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Password Change Required")
                .setMessage("You must change your password before continuing.")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Change Password", null) // Set to null initially
                .setNegativeButton("Logout", (d, which) -> {
                    // Return to login screen
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                    showPasswordCheckBox.setChecked(false);
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String newPassword = newPasswordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

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

                if (newPassword.equals(user.getPassword())) {
                    newPasswordEditText.setError("New password must be different from current password");
                    return;
                }

                // Update password
                user.setPassword(newPassword);
                user.setMustChangePassword(false);

                boolean result = userDAO.updateUser(user);
                if (result) {
                    dialog.dismiss();
                    Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    proceedToMainMenu(user);
                } else {
                    Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void proceedToMainMenu(User user) {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", user.getUsername());
        intent.putExtra("user_role", user.getRole());
        intent.putExtra("user_full_name", user.getFullName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void createDefaultAdminIfNeeded() {
        try {
            // Check if any admin users exist
            if (userDAO.getActiveAdmins().isEmpty()) {
                // Create default admin user
                User defaultAdmin = new User("admin", "admin123", "System Administrator", "admin");
                defaultAdmin.setActive(true);
                defaultAdmin.setMustChangePassword(true); // Force password change on first login

                long result = userDAO.addUser(defaultAdmin);
                if (result > 0) {
                    Toast.makeText(this, "Default admin account created. Username: admin, Password: admin123",
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}