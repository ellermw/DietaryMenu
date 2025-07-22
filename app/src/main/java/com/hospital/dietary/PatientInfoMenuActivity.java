package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PatientInfoMenuActivity extends AppCompatActivity {

    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView backArrow;
    private Button newPatientButton;
    private Button existingPatientsButton;
    private Button backToMainMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info_menu);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Hide the default action bar since we have custom header
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        backArrow = findViewById(R.id.backArrow);
        newPatientButton = findViewById(R.id.newPatientButton);
        existingPatientsButton = findViewById(R.id.existingPatientsButton);
        backToMainMenuButton = findViewById(R.id.backToMainMenuButton);
    }

    private void setupListeners() {
        // Back arrow in header
        backArrow.setOnClickListener(v -> goToMainMenu());

        // New Patient button
        newPatientButton.setOnClickListener(v -> openNewPatient());

        // Existing Patients button
        existingPatientsButton.setOnClickListener(v -> openExistingPatients());

        // Back to Main Menu button
        backToMainMenuButton.setOnClickListener(v -> goToMainMenu());
    }

    private void openNewPatient() {
        Intent intent = new Intent(this, NewPatientActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openExistingPatients() {
        Intent intent = new Intent(this, ExistingPatientActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goToMainMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goToMainMenu();
    }
}