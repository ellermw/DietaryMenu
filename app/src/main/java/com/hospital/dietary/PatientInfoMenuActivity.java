package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;

public class PatientInfoMenuActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Button newPatientButton;
    private Button existingPatientsButton;
    private TextView totalPatientsCount;
    private TextView completedOrdersCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info_menu);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title and back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Patient Information");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupListeners();
        loadQuickStats();
    }

    private void initializeViews() {
        newPatientButton = findViewById(R.id.newPatientButton);
        existingPatientsButton = findViewById(R.id.existingPatientsButton);
        totalPatientsCount = findViewById(R.id.totalPatientsCount);
        completedOrdersCountText = findViewById(R.id.completedOrdersCountText);
    }

    private void setupListeners() {
        // New Patient button
        if (newPatientButton != null) {
            newPatientButton.setOnClickListener(v -> openNewPatient());
        }

        // Existing Patients button
        if (existingPatientsButton != null) {
            existingPatientsButton.setOnClickListener(v -> openExistingPatients());
        }
    }

    private void loadQuickStats() {
        try {
            // Get total patients count
            int totalPatients = patientDAO.getAllPatients().size();
            if (totalPatientsCount != null) {
                totalPatientsCount.setText(String.valueOf(totalPatients));
            }

            // Get completed orders count (patients with all meals complete)
            int completedOrders = 0;
            for (com.hospital.dietary.models.Patient patient : patientDAO.getAllPatients()) {
                if (patient.isBreakfastComplete() && patient.isLunchComplete() && patient.isDinnerComplete()) {
                    completedOrders++;
                }
            }
            if (completedOrdersCountText != null) {
                completedOrdersCountText.setText(String.valueOf(completedOrders));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // If there's an error, set defaults
            if (totalPatientsCount != null) totalPatientsCount.setText("0");
            if (completedOrdersCountText != null) completedOrdersCountText.setText("0");
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