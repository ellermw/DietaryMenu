// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/ItemManagementActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ItemManagementActivity extends AppCompatActivity {
    
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
        
        titleText.setText("🍽️ Item Management");
        statusText.setText("This feature is coming soon!\n\nYou will be able to:\n• Add new food items\n• Edit existing items\n• Manage item categories\n• Set ADA compliance flags\n• Configure meal types");
        
        setTitle("Item Management");
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
    }
}