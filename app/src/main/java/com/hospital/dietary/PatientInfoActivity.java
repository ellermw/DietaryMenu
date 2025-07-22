package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

public class PatientInfoActivity extends AppCompatActivity {

    private static final String TAG = "PatientInfoActivity";
    private static final int REQUEST_NEW_PATIENT = 1001;
    private static final int REQUEST_EDIT_PATIENT = 1002;

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components - only reference IDs that actually exist
    private ListView patientsListView; // This is the actual ID in the layout
    private TextView noPatientsText; // We'll create this dynamically
    private EditText searchInput; // This is the actual ID in the layout

    // No search buttons exist in the layout, so we'll rely on text change listener

    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> displayedPatients = new ArrayList<>();
    private ArrayAdapter<Patient> patientsAdapter;
    private Patient selectedPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

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

        initializeViews();
        setupListeners();
        loadPatients();
    }

    private void initializeViews() {
        // FIXED: Use the correct IDs that actually exist in the layout
        patientsListView = findViewById(R.id.patientsListView);
        searchInput = findViewById(R.id.searchInput);

        // The layout doesn't have noPatientsText, so create it dynamically
        createNoPatientsTextView();

        if (patientsListView == null) {
            Toast.makeText(this, "Patient list not found in layout", Toast.LENGTH_LONG).show();
            Log.e(TAG, "ListView with ID patientsListView not found in layout");
        }
    }

    private void createNoPatientsTextView() {
        // Create the missing noPatientsText TextView dynamically
        noPatientsText = new TextView(this);
        noPatientsText.setText("No patients found");
        noPatientsText.setTextSize(16);
        noPatientsText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        noPatientsText.setPadding(20, 40, 20, 40);
        noPatientsText.setGravity(android.view.Gravity.CENTER);
        noPatientsText.setVisibility(View.GONE);

        // Add it to the root layout
        ViewGroup rootLayout = findViewById(android.R.id.content);
        if (rootLayout instanceof ViewGroup) {
            ((ViewGroup) rootLayout).addView(noPatientsText);
        }
    }

    private void setupListeners() {
        // Set up patients list click listeners
        if (patientsListView != null) {
            patientsListView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < displayedPatients.size()) {
                    Patient patient = displayedPatients.get(position);
                    showPatientOptionsDialog(patient);
                }
            });

            patientsListView.setOnItemLongClickListener((parent, view, position, id) -> {
                if (position < displayedPatients.size()) {
                    Patient patient = displayedPatients.get(position);
                    showDeleteConfirmationDialog(patient);
                }
                return true;
            });
        }

        // Set up search functionality - use TextWatcher since no search button exists
        if (searchInput != null) {
            searchInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    performSearch();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void loadPatients() {
        try {
            allPatients.clear();
            allPatients.addAll(patientDAO.getAllPatients());

            updateDisplayedPatients();
            Log.d(TAG, "Loaded " + allPatients.size() + " patients");

        } catch (Exception e) {
            Toast.makeText(this, "Error loading patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error loading patients", e);
        }
    }

    private void updateDisplayedPatients() {
        displayedPatients.clear();

        String searchQuery = "";
        if (searchInput != null) {
            searchQuery = searchInput.getText().toString().toLowerCase().trim();
        }

        if (searchQuery.isEmpty()) {
            displayedPatients.addAll(allPatients);
        } else {
            for (Patient patient : allPatients) {
                if (patient.getFullName().toLowerCase().contains(searchQuery) ||
                        patient.getWing().toLowerCase().contains(searchQuery) ||
                        patient.getRoomNumber().toLowerCase().contains(searchQuery) ||
                        patient.getDiet().toLowerCase().contains(searchQuery)) {
                    displayedPatients.add(patient);
                }
            }
        }

        // Update UI
        if (patientsListView != null) {
            if (patientsAdapter == null) {
                patientsAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_2, android.R.id.text1, displayedPatients) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        Patient patient = getItem(position);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        if (patient != null) {
                            text1.setText(patient.getFullName());

                            String statusText = String.format("%s - Room %s | %s",
                                    patient.getWing(),
                                    patient.getRoomNumber(),
                                    patient.getDiet());

                            text2.setText(statusText);
                            text2.setTextSize(12);
                            text2.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        }

                        return view;
                    }
                };
                patientsListView.setAdapter(patientsAdapter);
            } else {
                patientsAdapter.notifyDataSetChanged();
            }

            // Show/hide empty state
            if (displayedPatients.isEmpty()) {
                patientsListView.setVisibility(View.GONE);
                if (noPatientsText != null) {
                    noPatientsText.setVisibility(View.VISIBLE);
                    if (allPatients.isEmpty()) {
                        noPatientsText.setText("No patients found.\nAdd a new patient to get started.");
                    } else {
                        noPatientsText.setText("No patients match your search criteria.");
                    }
                }
            } else {
                patientsListView.setVisibility(View.VISIBLE);
                if (noPatientsText != null) {
                    noPatientsText.setVisibility(View.GONE);
                }
            }
        }
    }

    private void performSearch() {
        updateDisplayedPatients();
    }

    private void showPatientOptionsDialog(Patient patient) {
        selectedPatient = patient;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Patient Options: " + patient.getFullName());

        String[] options = {"View Details", "Edit Patient", "Plan Meals", "Delete Patient"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // View Details
                    showPatientDetailsDialog(patient);
                    break;
                case 1: // Edit Patient
                    editPatient(patient);
                    break;
                case 2: // Plan Meals
                    planMeals(patient);
                    break;
                case 3: // Delete Patient
                    showDeleteConfirmationDialog(patient);
                    break;
            }
        });

        builder.show();
    }

    private void editPatient(Patient patient) {
        Intent intent = new Intent(this, NewPatientActivity.class);
        intent.putExtra("edit_patient_id", patient.getPatientId());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivityForResult(intent, REQUEST_EDIT_PATIENT);
    }

    private void planMeals(Patient patient) {
        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("patient_id", (long) patient.getPatientId());
        intent.putExtra("diet", patient.getDiet());
        intent.putExtra("is_ada_diet", patient.isAdaDiet());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void showPatientDetailsDialog(Patient patient) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(patient.getFullName()).append("\n");
        details.append("Location: ").append(patient.getLocationInfo()).append("\n");
        details.append("Diet: ").append(patient.getDiet()).append("\n");
        details.append("Fluid Restriction: ").append(patient.getFluidRestriction()).append("\n");
        details.append("Texture Modifications: ").append(patient.getTextureModifications()).append("\n\n");
        details.append("Meal Status:\n");
        details.append("Breakfast: ").append(patient.isBreakfastComplete() ? "Complete" : patient.isBreakfastNPO() ? "NPO" : "Pending").append("\n");
        details.append("Lunch: ").append(patient.isLunchComplete() ? "Complete" : patient.isLunchNPO() ? "NPO" : "Pending").append("\n");
        details.append("Dinner: ").append(patient.isDinnerComplete() ? "Complete" : patient.isDinnerNPO() ? "NPO" : "Pending");

        new AlertDialog.Builder(this)
                .setTitle("Patient Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Patient patient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete " + patient.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        boolean success = patientDAO.deletePatient(patient.getPatientId());
                        if (success) {
                            Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                            loadPatients(); // Refresh the list
                        } else {
                            Toast.makeText(this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error deleting patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting patient", e);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_NEW_PATIENT:
                case REQUEST_EDIT_PATIENT:
                    loadPatients(); // Refresh the patient list
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Skip menu inflation if menu file doesn't exist
        try {
            getMenuInflater().inflate(R.menu.menu_patient_info, menu);
        } catch (Exception e) {
            Log.d(TAG, "Menu file not found, skipping menu inflation");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                // Handle any other menu items that might exist
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}