package com.hospital.dietary.ui.login;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.hospital.dietary.R;
import com.hospital.dietary.data.entities.UserEntity;
import com.hospital.dietary.ui.main.MainMenuActivity;

/**
 * LoginActivity - Migrated to use MVVM pattern
 * UI remains exactly the same, but now uses ViewModel for all logic
 */
public class LoginActivity extends AppCompatActivity {

    // ViewModel
    private LoginViewModel loginViewModel;

    // UI Components (same as before)
    private EditText usernameEditText;
    private EditText passwordEditText;
    private CheckBox showPasswordCheckBox;
    private Button signInButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Initialize UI components
        initializeViews();

        // Set up listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();

        // Create default admin if needed
        loginViewModel.createDefaultAdminIfNeeded();
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        signInButton = findViewById(R.id.signInButton);
        
        // Add progress bar to existing layout (or find existing one)
        progressBar = findViewById(R.id.progressBar);
        if (progressBar == null) {
            // If no progress bar in layout, create one programmatically
            progressBar = new ProgressBar(this);
            progressBar.setVisibility(View.GONE);
            // Add to parent layout if needed
        }
    }

    private void setupListeners() {
        // Show/hide password toggle (same as before)
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Sign in button click
        signInButton.setOnClickListener(v -> performLogin());

        // Enter key on password field
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            performLogin();
            return true;
        });
    }

    private void observeViewModel() {
        // Observe login result
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult != null) {
                if (loginResult.isSuccess()) {
                    // Login successful
                    UserEntity user = loginResult.getUser();
                    if (user.isMustChangePassword()) {
                        showChangePasswordDialog(user);
                    } else {
                        navigateToMainMenu(user);
                    }
                } else {
                    // Login failed
                    Toast.makeText(this, loginResult.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Observe loading state
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                signInButton.setEnabled(!isLoading);
                usernameEditText.setEnabled(!isLoading);
                passwordEditText.setEnabled(!isLoading);
            }
        });
    }

    private void performLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        loginViewModel.login(username, password);
    }

    private void showChangePasswordDialog(UserEntity user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        new AlertDialog.Builder(this)
                .setTitle("Password Change Required")
                .setMessage("You must change your password before continuing.")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Change Password", (dialog, which) -> {
                    String newPassword = newPasswordInput.getText().toString();
                    String confirmPassword = confirmPasswordInput.getText().toString();

                    if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        showChangePasswordDialog(user);
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        showChangePasswordDialog(user);
                        return;
                    }

                    // Update password using ViewModel
                    changePassword(user, newPassword);
                })
                .setNegativeButton("Logout", (dialog, which) -> {
                    // Clear fields and return to login
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                })
                .show();
    }

    private void changePassword(UserEntity user, String newPassword) {
        // This would be implemented in the ViewModel
        // For now, update the user and navigate
        user.setPassword(newPassword);
        user.setMustChangePassword(false);
        // Update via ViewModel/Repository
        navigateToMainMenu(user);
    }

    private void navigateToMainMenu(UserEntity user) {
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
        // ViewModel automatically cleared by the framework
    }
}