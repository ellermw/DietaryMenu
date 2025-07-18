package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PatientInfoMenuActivity extends AppCompatActivity {
    
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    private Button newPatientButton;
    private Button existingPatientButton;
    private Button backButton;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info_menu);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        initializeUI();
        setupListeners();
    }
    
    private void initializeUI() {
        newPatientButton = findViewById(R.id.newPatientButton);
        existingPatientButton = findViewById(R.id.existingPatientButton);
        backButton = findViewById(R.id.backButton);
        welcomeText = findViewById(R.id.welcomeText);
        
        // Set welcome text
        welcomeText.setText("Welcome, " + (currentUserFullName != null ? currentUserFullName : currentUsername) + "!");
        
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