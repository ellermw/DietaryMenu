package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.List;

public class PatientInfoMenuActivity extends AppCompatActivity {

    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // UI Components
    private TextView welcomeText;
    private TextView statsText;
    private Button newPatientButton;
    private Button existingPatientsButton;
    private Button backToMainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info_menu);

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Patient Information");
        }

        // Initialize views
        initializeViews();

        // Update UI
        updateUI();

        // Set up listeners
        setupListeners();

        // Load statistics
        loadQuickStats();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        statsText = findViewById(R.id.statsText);
        newPatientButton = findViewById(R.id.newPatientButton);
        existingPatientsButton = findViewById(R.id.existingPatientsButton);
        backToMainButton = findViewById(R.id.backToMainButton);
    }

    private void updateUI() {
        if (welcomeText != null && currentUserFullName != null) {
            welcomeText.setText("Patient Management - " + currentUserFullName);
        }
    }

    private void setupListeners() {
        if (newPatientButton != null) {
            newPatientButton.setOnClickListener(v -> openNewPatient());
        }

        if (existingPatientsButton != null) {
            existingPatientsButton.setOnClickListener(v -> openExistingPatients());
        }

        if (backToMainButton != null) {
            backToMainButton.setOnClickListener(v -> goToMainMenu());
        }
    }

    private void loadQuickStats() {
        try {
            // Get all patients and calculate stats
            List<Patient> allPatients = patientDAO.getAllPatients();
            int totalPatients = allPatients.size();

            // Count ADA diet patients
            int adaPatients = 0;
            for (Patient patient : allPatients) {
                if (patient.isAdaDiet()) {
                    adaPatients++;
                }
            }

            String stats = String.format("Total Patients: %d\nADA Diet Patients: %d",
                    totalPatients, adaPatients);

            if (statsText != null) {
                statsText.setText(stats);
                statsText.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            // If stats fail to load, just hide the stats view
            if (statsText != null) {
                statsText.setVisibility(View.GONE);
            }
        }
    }

    private void openNewPatient() {
        Intent intent = new Intent(this, NewPatientActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openExistingPatients() {
        // FIXED: Changed to ExistingPatientsActivity (with 's')
        Intent intent = new Intent(this, ExistingPatientsActivity.class);
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
    protected void onResume() {
        super.onResume();
        // Reload stats when returning to this activity
        loadQuickStats();
    }

    @Override
    public void onBackPressed() {
        goToMainMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}