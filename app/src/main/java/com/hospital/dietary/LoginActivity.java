package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
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
                        // Login successful - redirect to MainMenuActivity
                        proceedToMainMenu(user);
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
                    showError("Login failed: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void proceedToMainMenu(User user) {
        // Create intent for MainMenuActivity
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", user.getUsername());
        intent.putExtra("user_role", user.getRole());
        intent.putExtra("user_full_name", user.getFullName());
        
        // Clear login activity from stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        startActivity(intent);
        finish();
    }
    
    private void setLoading(boolean loading) {
        loginProgress.setVisibility(loading ? ProgressBar.VISIBLE : ProgressBar.GONE);
        loginButton.setEnabled(!loading);
        usernameInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}