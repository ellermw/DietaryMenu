package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PatientInfoMenuActivity extends AppCompatActivity {
    
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    private Toolbar toolbar;
    private Button newPatientButton;
    private Button existingPatientButton;
    private Button backButton;
    private Button homeButton;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info_menu);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        // Setup toolbar
        setupToolbar();
        
        initializeUI();
        setupListeners();
    }
    
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Patient Information");
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_home:
                goToMainMenu();
                return true;
            case R.id.action_refresh:
                // No refresh needed for menu, but could reload user info
                Toast.makeText(this, "Menu refreshed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    
    private void initializeUI() {
        newPatientButton = findViewById(R.id.newPatientButton);
        existingPatientButton = findViewById(R.id.existingPatientButton);
        backButton = findViewById(R.id.backButton);
        homeButton = findViewById(R.id.homeButton);
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
        
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }
    }
}