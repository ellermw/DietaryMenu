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
    private TextView versionText;

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
        versionText = findViewById(R.id.versionText);

        // Set version text
        if (versionText != null) {
            versionText.setText(getString(R.string.app_version));
        }
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

        // Sign in button
        signInButton.setOnClickListener(v -> attemptLogin());
    }

    private void createDefaultAdminIfNeeded() {
        try {
            // Check if admin user exists
            User adminUser = userDAO.getUserByUsername("admin");

            if (adminUser == null) {
                // Create default admin user
                User newAdmin = new User();
                newAdmin.setUsername("admin");
                newAdmin.setPassword("password123");
                newAdmin.setRole("Admin");
                newAdmin.setFullName("Administrator");
                newAdmin.setActive(true);

                long result = userDAO.addUser(newAdmin);
                if (result > 0) {
                    android.util.Log.d("LoginActivity", "Default admin user created");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error creating default admin", e);
        }
    }

    private void attemptLogin() {
        // Reset errors
        usernameEditText.setError(null);
        passwordEditText.setError(null);

        // Get values
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for valid password
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for valid username
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            focusView = usernameEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Attempt authentication
            performLogin(username, password);
        }
    }

    private void performLogin(String username, String password) {
        try {
            User user = userDAO.getUserByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                if (!user.isActive()) {
                    Toast.makeText(this, "Account is inactive. Please contact administrator.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Check if this is first login (default password)
                if ("password123".equals(password) && "admin".equals(username)) {
                    showPasswordChangeDialog(user);
                } else {
                    // Proceed to main menu
                    navigateToMainMenu(user);
                }
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                passwordEditText.setText("");
                passwordEditText.requestFocus();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showPasswordChangeDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        EditText newPasswordEdit = dialogView.findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEdit = dialogView.findViewById(R.id.confirmPasswordEditText);

        new AlertDialog.Builder(this)
                .setTitle("Password Change Required")
                .setMessage("You must change your password from the default.")
                .setView(dialogView)
                .setPositiveButton("Change Password", (dialog, which) -> {
                    String newPassword = newPasswordEdit.getText().toString();
                    String confirmPassword = confirmPasswordEdit.getText().toString();

                    if (validatePasswordChange(newPassword, confirmPassword)) {
                        changePassword(user, newPassword);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    private boolean validatePasswordChange(String newPassword, String confirmPassword) {
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void changePassword(User user, String newPassword) {
        try {
            user.setPassword(newPassword);

            boolean result = userDAO.updateUser(user);

            if (result) {
                Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
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