package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);

        // Initialize UI components
        initializeViews();

        // Set up listeners
        setupListeners();

        // Create default admin if needed
        createDefaultAdminIfNeeded();
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        signInButton = findViewById(R.id.signInButton);
    }

    private void setupListeners() {
        // Show/hide password toggle
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Sign in button click
        signInButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        // Reset errors
        usernameEditText.setError(null);
        passwordEditText.setError(null);

        // Get values
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        // Validate inputs
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.password_required));
            focusView = passwordEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError(getString(R.string.field_required));
            focusView = usernameEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        // Attempt login
        try {
            User user = userDAO.validateLogin(username, password);

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

            // FIXED: Check if user must change password on first login
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

    // FIXED: Force password change dialog
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
                .setTitle(getString(R.string.password_change_required))
                .setMessage("You must change your password before continuing.")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.change_password), null)
                .setNegativeButton(getString(R.string.cancel), (d, which) -> {
                    // User cancelled, return to login
                    Toast.makeText(this, "Password change is required to continue.", Toast.LENGTH_SHORT).show();
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String newPassword = newPasswordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                // Validate passwords
                if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(this, getString(R.string.field_required), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.length() < 6) {
                    Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Change password
                if (userDAO.changePassword(user.getUserId(), newPassword)) {
                    dialog.dismiss();
                    Toast.makeText(this, getString(R.string.password_changed_success), Toast.LENGTH_SHORT).show();
                    proceedToMainMenu(user);
                } else {
                    Toast.makeText(this, getString(R.string.password_change_failed), Toast.LENGTH_SHORT).show();
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
                User defaultAdmin = new User("admin", "admin123", "System Administrator", "Admin");
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