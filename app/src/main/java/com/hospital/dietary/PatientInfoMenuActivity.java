package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.List;

public class PatientInfoMenuActivity extends AppCompatActivity {

    private static final String TAG = "PatientInfoMenu";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Button newPatientButton;
    private Button existingPatientsButton;
    private TextView quickStatsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info_menu);

        Log.d(TAG, "PatientInfoMenuActivity onCreate started");

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Patient Information");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupListeners();
        loadQuickStats();

        Log.d(TAG, "PatientInfoMenuActivity onCreate completed");
    }

    private void initializeUI() {
        newPatientButton = findViewById(R.id.newPatientButton);
        existingPatientsButton = findViewById(R.id.existingPatientsButton);
        quickStatsText = findViewById(R.id.quickStatsText);
    }

    private void setupListeners() {
        newPatientButton.setOnClickListener(v -> openNewPatient());
        existingPatientsButton.setOnClickListener(v -> openExistingPatients());
    }

    private void loadQuickStats() {
        try {
            // Get all patients
            List<Patient> allPatients = patientDAO.getAllPatients();
            int totalPatients = allPatients.size();

            // Count various statuses
            int pendingBreakfast = 0;
            int pendingLunch = 0;
            int pendingDinner = 0;
            int completedOrders = 0;
            int adaDietCount = 0;

            for (Patient patient : allPatients) {
                if (!patient.isBreakfastComplete()) {
                    pendingBreakfast++;
                }
                if (!patient.isLunchComplete()) {
                    pendingLunch++;
                }
                if (!patient.isDinnerComplete()) {
                    pendingDinner++;
                }
                if (patient.isBreakfastComplete() && patient.isLunchComplete() && patient.isDinnerComplete()) {
                    completedOrders++;
                }
                if (patient.isAdaDiet()) {
                    adaDietCount++;
                }
            }

            StringBuilder stats = new StringBuilder();
            stats.append("Total Patients: ").append(totalPatients).append("\n");
            stats.append("Pending Breakfast: ").append(pendingBreakfast).append("\n");
            stats.append("Pending Lunch: ").append(pendingLunch).append("\n");
            stats.append("Pending Dinner: ").append(pendingDinner).append("\n");
            stats.append("Completed Orders: ").append(completedOrders).append("\n");
            stats.append("ADA Diet Patients: ").append(adaDietCount);

            quickStatsText.setText(stats.toString());

        } catch (Exception e) {
            Log.e(TAG, "Error loading quick stats", e);
            quickStatsText.setText("Unable to load statistics");
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