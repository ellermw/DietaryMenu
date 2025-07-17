// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/LoginActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.User;

public class LoginActivity extends AppCompatActivity {
    
    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private ProgressBar loginProgress;
    private DatabaseHelper dbHelper;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        setupListeners();
    }
    
    private void initializeUI() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        loginProgress = findViewById(R.id.loginProgress);
        
        // Auto-focus username
        usernameInput.requestFocus();
    }
    
    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        
        // Allow Enter key to trigger login
        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            attemptLogin();
            return true;
        });
    }
    
    private void attemptLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        // Clear previous errors
        usernameInput.setError(null);
        passwordInput.setError(null);
        
        // Validate input
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Authenticate user (simulate background task)
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate network delay
                
                User user = userDAO.authenticateUser(username, password);
                
                runOnUiThread(() -> {
                    setLoading(false);
                    
                    if (user != null && user.isActive()) {
                        // Login successful
                        proceedToMainApp(user);
                    } else if (user != null && !user.isActive()) {
                        // User exists but is inactive
                        showError("Account is inactive. Please contact administrator.");
                    } else {
                        // Invalid credentials
                        showError("Invalid username or password");
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError("Login failed. Please try again.");
                });
            }
        }).start();
    }
    
    private void proceedToMainApp(User user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_id", user.getUserId());
        intent.putExtra("username", user.getUsername());
        intent.putExtra("full_name", user.getFullName());
        intent.putExtra("role", user.getRole());
        intent.putExtra("is_admin", user.isAdmin());
        
        startActivity(intent);
        finish(); // Prevent going back to login
    }
    
    private void setLoading(boolean loading) {
        loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        usernameInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        
        if (loading) {
            loginButton.setText("Logging in...");
        } else {
            loginButton.setText("LOGIN");
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        passwordInput.selectAll();
        passwordInput.requestFocus();
    }
}