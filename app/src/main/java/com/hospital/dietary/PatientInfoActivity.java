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

    // UI Components that may or may not exist in your layout
    private Button newPatientButton, pendingOrdersButton, retiredOrdersButton;
    private ListView existingPatientsListView;
    private TextView noPatientsText;
    private EditText searchEditText;
    private Button searchButton, clearSearchButton;

    // Edit patient dialog components will be created dynamically
    private AlertDialog editPatientDialog;

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
        // Main buttons - handle if they don't exist
        newPatientButton = findViewById(R.id.newPatientButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        retiredOrdersButton = findViewById(R.id.retiredOrdersButton);

        // Patient list - handle if they don't exist
        existingPatientsListView = findViewById(R.id.existingPatientsListView);
        if (existingPatientsListView == null) {
            existingPatientsListView = findViewById(R.id.patientsListView);
        }

        noPatientsText = findViewById(R.id.noPatientsText);
        if (noPatientsText == null) {
            noPatientsText = findViewById(R.id.noDataText);
        }
        if (noPatientsText == null) {
            noPatientsText = findViewById(R.id.emptyText);
        }

        // Search functionality - handle if they don't exist
        searchEditText = findViewById(R.id.searchEditText);
        if (searchEditText == null) {
            searchEditText = findViewById(R.id.searchInput);
        }

        searchButton = findViewById(R.id.searchButton);
        if (searchButton == null) {
            searchButton = findViewById(R.id.btnSearch);
        }

        clearSearchButton = findViewById(R.id.clearSearchButton);
        if (clearSearchButton == null) {
            clearSearchButton = findViewById(R.id.btnClear);
        }

        // If we can't find the list view, show an error
        if (existingPatientsListView == null) {
            Toast.makeText(this, "Patient list not found in layout. Please check layout file.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "ListView with ID existingPatientsListView not found in layout");
        }
    }

    private void setupListeners() {
        if (newPatientButton != null) {
            newPatientButton.setOnClickListener(v -> openNewPatient());
        } else {
            Log.w(TAG, "newPatientButton not found in layout");
        }

        if (pendingOrdersButton != null) {
            pendingOrdersButton.setOnClickListener(v -> openPendingOrders());
        } else {
            Log.w(TAG, "pendingOrdersButton not found in layout");
        }

        if (retiredOrdersButton != null) {
            retiredOrdersButton.setOnClickListener(v -> openRetiredOrders());
        } else {
            Log.w(TAG, "retiredOrdersButton not found in layout");
        }

        if (searchButton != null) {
            searchButton.setOnClickListener(v -> performSearch());
        } else {
            Log.w(TAG, "searchButton not found in layout");
        }

        if (clearSearchButton != null) {
            clearSearchButton.setOnClickListener(v -> clearSearch());
        } else {
            Log.w(TAG, "clearSearchButton not found in layout");
        }

        // Set up patients list click listeners
        if (existingPatientsListView != null) {
            existingPatientsListView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < displayedPatients.size()) {
                    Patient patient = displayedPatients.get(position);
                    showPatientOptionsDialog(patient);
                }
            });

            existingPatientsListView.setOnItemLongClickListener((parent, view, position, id) -> {
                if (position < displayedPatients.size()) {
                    Patient patient = displayedPatients.get(position);
                    showDeleteConfirmationDialog(patient);
                }
                return true;
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
        displayedPatients.addAll(allPatients);

        if (existingPatientsListView != null) {
            if (displayedPatients.isEmpty()) {
                existingPatientsListView.setVisibility(View.GONE);
                if (noPatientsText != null) {
                    noPatientsText.setVisibility(View.VISIBLE);
                    noPatientsText.setText("No patients found");
                }
            } else {
                existingPatientsListView.setVisibility(View.VISIBLE);
                if (noPatientsText != null) {
                    noPatientsText.setVisibility(View.GONE);
                }

                // Create custom adapter for patient display
                patientsAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_2, android.R.id.text1, displayedPatients) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        Patient patient = getItem(position);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        if (patient != null) {
                            text1.setText(patient.getFullName());
                            text2.setText(patient.getLocationInfo() + " • " + patient.getDiet());
                        }

                        return view;
                    }
                };

                existingPatientsListView.setAdapter(patientsAdapter);
            }
        } else {
            // If no ListView found, create a simple text display
            if (noPatientsText != null) {
                noPatientsText.setVisibility(View.VISIBLE);
                if (displayedPatients.isEmpty()) {
                    noPatientsText.setText("No patients found");
                } else {
                    StringBuilder patientsText = new StringBuilder("Patients:\n");
                    for (Patient patient : displayedPatients) {
                        patientsText.append("• ").append(patient.getFullName())
                                .append(" (").append(patient.getLocationInfo()).append(")\n");
                    }
                    noPatientsText.setText(patientsText.toString());
                }
            }
        }
    }

    private void performSearch() {
        if (searchEditText == null) {
            Toast.makeText(this, "Search not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String searchTerm = searchEditText.getText().toString().trim();

        if (searchTerm.isEmpty()) {
            clearSearch();
            return;
        }

        try {
            List<Patient> searchResults = patientDAO.searchPatients(searchTerm);
            displayedPatients.clear();
            displayedPatients.addAll(searchResults);

            if (patientsAdapter != null) {
                patientsAdapter.notifyDataSetChanged();
            }

            if (displayedPatients.isEmpty()) {
                if (noPatientsText != null) {
                    noPatientsText.setVisibility(View.VISIBLE);
                    noPatientsText.setText("No patients found matching \"" + searchTerm + "\"");
                }
                if (existingPatientsListView != null) {
                    existingPatientsListView.setVisibility(View.GONE);
                }
            } else {
                if (existingPatientsListView != null) {
                    existingPatientsListView.setVisibility(View.VISIBLE);
                }
                if (noPatientsText != null) {
                    noPatientsText.setVisibility(View.GONE);
                }
            }

            Log.d(TAG, "Search for '" + searchTerm + "' returned " + searchResults.size() + " results");

        } catch (Exception e) {
            Toast.makeText(this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error performing search", e);
        }
    }

    private void clearSearch() {
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        updateDisplayedPatients();
    }

    private void showPatientOptionsDialog(Patient patient) {
        selectedPatient = patient;

        String[] options = {"Edit Patient", "View Details", "Plan Meals"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(patient.getFullName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit Patient
                            showEditPatientDialog(patient);
                            break;
                        case 1: // View Details
                            showPatientDetailsDialog(patient);
                            break;
                        case 2: // Plan Meals
                            openMealPlanning(patient);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditPatientDialog(Patient patient) {
        // Create a simple edit dialog since your layout files have complex nested structures
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Patient: " + patient.getFullName());

        // Create a simple vertical layout for basic edits
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // First Name
        final EditText firstNameInput = new EditText(this);
        firstNameInput.setText(patient.getPatientFirstName());
        firstNameInput.setHint("First Name");
        layout.addView(firstNameInput);

        // Last Name
        final EditText lastNameInput = new EditText(this);
        lastNameInput.setText(patient.getPatientLastName());
        lastNameInput.setHint("Last Name");
        layout.addView(lastNameInput);

        // Wing Spinner
        final Spinner wingSpinner = new Spinner(this);
        String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        // Set current wing
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(patient.getWing())) {
                wingSpinner.setSelection(i);
                break;
            }
        }
        layout.addView(wingSpinner);

        // Room Input (simplified - you could make this dynamic like in NewPatient)
        final EditText roomInput = new EditText(this);
        roomInput.setText(patient.getRoomNumber());
        roomInput.setHint("Room Number");
        layout.addView(roomInput);

        // Diet Spinner
        final Spinner dietSpinner = new Spinner(this);
        String[] diets = {"Regular", "ADA", "Cardiac", "Renal", "Clear Liquid", "Full Liquid", "Puree"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        // Set current diet
        String currentDiet = patient.getDiet().replace(" (ADA)", "");
        for (int i = 0; i < diets.length; i++) {
            if (diets[i].equals(currentDiet)) {
                dietSpinner.setSelection(i);
                break;
            }
        }
        layout.addView(dietSpinner);

        // ADA CheckBox
        final CheckBox adaCheckBox = new CheckBox(this);
        adaCheckBox.setText("ADA Diet");
        adaCheckBox.setChecked(patient.isAdaDiet());
        layout.addView(adaCheckBox);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            // Update patient with new values
            patient.setPatientFirstName(firstNameInput.getText().toString().trim());
            patient.setPatientLastName(lastNameInput.getText().toString().trim());
            patient.setWing(wingSpinner.getSelectedItem().toString());
            patient.setRoomNumber(roomInput.getText().toString().trim());

            // Handle diet and ADA
            String selectedDiet = dietSpinner.getSelectedItem().toString();
            boolean isAdaSelected = adaCheckBox.isChecked();

            if (isAdaSelected && (selectedDiet.equals("Clear Liquid") || selectedDiet.equals("Full Liquid") || selectedDiet.equals("Puree"))) {
                patient.setDiet(selectedDiet + " (ADA)");
                patient.setAdaDiet(true);
            } else {
                patient.setDiet(selectedDiet);
                patient.setAdaDiet(false);
            }

            // Save to database
            try {
                boolean success = patientDAO.updatePatient(patient);

                if (success) {
                    Toast.makeText(this, "Patient updated successfully!", Toast.LENGTH_SHORT).show();
                    loadPatients(); // Refresh the list
                } else {
                    Toast.makeText(this, "Failed to update patient", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error updating patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error updating patient", e);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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
                .setPositiveButton("Delete", (dialog, which) -> deletePatient(patient))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePatient(Patient patient) {
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
    }

    private void openNewPatient() {
        Intent intent = new Intent(this, NewPatientActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivityForResult(intent, REQUEST_NEW_PATIENT);
    }

    private void openPendingOrders() {
        Intent intent = new Intent(this, PendingOrdersActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openRetiredOrders() {
        Intent intent = new Intent(this, RetiredOrdersActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openMealPlanning(Patient patient) {
        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("patient_id", (long) patient.getPatientId());
        intent.putExtra("diet", patient.getDiet());
        intent.putExtra("is_ada_diet", patient.isAdaDiet());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
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
            Log.d(TAG, "Menu file not found, skipping");
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