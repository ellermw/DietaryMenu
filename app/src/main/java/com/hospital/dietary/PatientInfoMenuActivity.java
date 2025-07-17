package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PatientInfoMenuActivity extends AppCompatActivity {
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components
    private TextView welcomeText;
    private Button newPatientButton;
    private Button existingPatientButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info_menu);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
    }
    
    private void initializeUI() {
        welcomeText = findViewById(R.id.welcomeText);
        newPatientButton = findViewById(R.id.newPatientButton);
        existingPatientButton = findViewById(R.id.existingPatientButton);
        backButton = findViewById(R.id.backButton);
        
        // Set welcome message
        if (currentUserFullName != null) {
            welcomeText.setText("Welcome, " + currentUserFullName + "!");
        }
        
        // Set title
        setTitle("Patient Information");
    }
    
    private void setupListeners() {
        newPatientButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewPatientActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });
        
        existingPatientButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExistingPatientActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });
        
        backButton.setOnClickListener(v -> finish());
    }
}