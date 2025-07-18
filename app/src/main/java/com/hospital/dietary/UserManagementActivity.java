// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/UserManagementActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UserManagementActivity extends AppCompatActivity {
    
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    private TextView titleText;
    private TextView statusText;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_placeholder);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        initializeUI();
        setupListeners();
    }
    
    private void initializeUI() {
        titleText = findViewById(R.id.titleText);
        statusText = findViewById(R.id.statusText);
        backButton = findViewById(R.id.backButton);
        
        titleText.setText("👥 User Management");
        statusText.setText("This feature is coming soon!\n\nYou will be able to:\n• Add new users\n• Edit user roles\n• Manage user permissions\n• View user activity");
        
        setTitle("User Management");
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }
}