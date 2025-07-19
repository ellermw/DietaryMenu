package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private EditText patientFirstNameEditText;
    private EditText patientLastNameEditText;
    private Spinner wingSpinner;
    private Spinner roomNumberSpinner;
    private Spinner dietSpinner;
    private Spinner fluidRestrictionSpinner;

    // Texture modification checkboxes
    private CheckBox mechanicalGroundCB;
    private CheckBox mechanicalChoppedCB;
    private CheckBox biteSizeCB;
    private CheckBox breadOKCB;

    private Button savePatientButton;
    private Button backButton;
    private Button homeButton;

    // Data arrays with proper wings and diets
    private String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
    private String[] diets = {"Regular", "Cardiac", "ADA", "Puree", "Renal", "Full Liquid", "Clear Liquid"};
    private String[] fluidRestrictions = {"None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml"};

    // Room mapping for each wing
    private Map<String, String[]> wingRoomMap;
    private ArrayAdapter<String> roomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_new_patient);

            // Get user information from intent
            currentUsername = getIntent().getStringExtra("current_user");
            currentUserRole = getIntent().getStringExtra("user_role");
            currentUserFullName = getIntent().getStringExtra("user_full_name");

            // Initialize database
            dbHelper = new DatabaseHelper(this);
            patientDAO = new PatientDAO(dbHelper);

            // Initialize room mapping
            initializeRoomMapping();

            // Initialize UI
            initializeUI();
            setupSpinners();
            setupListeners();

            setTitle("Add New Patient");

            // Enable the default action bar back button
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Add New Patient");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to initialize activity: " + e.getMessage());
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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

    private void initializeRoomMapping() {
        wingRoomMap = new HashMap<>();
        wingRoomMap.put("1 South", new String[]{"101", "102", "103", "104", "105", "106", "107", "108", "109", "110"});
        wingRoomMap.put("2 North", new String[]{"201", "202", "203", "204", "205", "206", "207", "208", "209", "210"});
        wingRoomMap.put("Labor and Delivery", new String[]{"L1", "L2", "L3", "L4", "L5", "L6"});
        wingRoomMap.put("2 West", new String[]{"221", "222", "223", "224", "225", "226", "227", "228", "229", "230"});
        wingRoomMap.put("3 North", new String[]{"301", "302", "303", "304", "305", "306", "307", "308", "309", "310"});
        wingRoomMap.put("ICU", new String[]{"ICU1", "ICU2", "ICU3", "ICU4", "ICU5", "ICU6", "ICU7", "ICU8"});
    }

    private void initializeUI() {
        try {
            patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
            patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
            wingSpinner = findViewById(R.id.wingSpinner);
            roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
            dietSpinner = findViewById(R.id.dietSpinner);
            fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

            mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
            mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
            biteSizeCB = findViewById(R.id.biteSizeCB);
            breadOKCB = findViewById(R.id.breadOKCB);

            savePatientButton = findViewById(R.id.savePatientButton);
            backButton = findViewById(R.id.backButton);
            homeButton = findViewById(R.id.homeButton);

            // FIXED: Disable autofill for all EditText fields
            if (patientFirstNameEditText != null) {
                patientFirstNameEditText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            }
            if (patientLastNameEditText != null) {
                patientLastNameEditText.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to initialize UI components: " + e.getMessage());
        }
    }

    private void setupSpinners() {
        try {
            // Wing spinner
            ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
            wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            wingSpinner.setAdapter(wingAdapter);

            // Room spinner (initially empty)
            roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomNumberSpinner.setAdapter(roomAdapter);

            // Diet spinner
            ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
            dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dietSpinner.setAdapter(dietAdapter);

            // Fluid restriction spinner
            ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
            fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fluidRestrictionSpinner.setAdapter(fluidAdapter);

            // Set initial room options for first wing
            updateRoomDropdown();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to setup spinners: " + e.getMessage());
        }
    }

    private void setupListeners() {
        try {
            // Wing selection listener - updates room dropdown
            wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateRoomDropdown();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            savePatientButton.setOnClickListener(v -> validateAndSavePatient());
            backButton.setOnClickListener(v -> finish());

            if (homeButton != null) {
                homeButton.setOnClickListener(v -> goToMainMenu());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to setup listeners: " + e.getMessage());
        }
    }

    private void updateRoomDropdown() {
        try {
            String selectedWing = wingSpinner.getSelectedItem().toString();
            String[] rooms = wingRoomMap.get(selectedWing);

            if (rooms != null) {
                roomAdapter.clear();
                roomAdapter.addAll(Arrays.asList(rooms));
                roomAdapter.notifyDataSetChanged();
                if (roomAdapter.getCount() > 0) {
                    roomNumberSpinner.setSelection(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to update room dropdown: " + e.getMessage());
        }
    }

    private void validateAndSavePatient() {
        try {
            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();
            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();

            // Validate required fields
            if (firstName.isEmpty()) {
                patientFirstNameEditText.setError("First name is required");
                patientFirstNameEditText.requestFocus();
                return;
            }

            if (lastName.isEmpty()) {
                patientLastNameEditText.setError("Last name is required");
                patientLastNameEditText.requestFocus();
                return;
            }

            // Check for duplicate patient in same room
            Patient existingPatient = patientDAO.getPatientByRoomToday(wing, roomNumber);
            if (existingPatient != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Room Already Occupied")
                        .setMessage("Patient " + existingPatient.getPatientFirstName() + " " +
                                existingPatient.getPatientLastName() + " is already in room " +
                                wing + " - " + roomNumber + " today.\n\nWhat would you like to do?")
                        .setPositiveButton("Replace Patient", (dialog, which) -> {
                            // Delete existing patient and save new one
                            patientDAO.deletePatient(existingPatient.getPatientId());
                            savePatient();
                        })
                        .setNegativeButton("Cancel", null)
                        .setNeutralButton("Choose Different Room", (dialog, which) -> {
                            // Just close dialog, let user pick different room
                        })
                        .show();
            } else {
                // No duplicate, proceed with save
                savePatient();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Validation error: " + e.getMessage());
        }
    }

    private void savePatient() {
        try {
            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();
            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();
            String diet = dietSpinner.getSelectedItem().toString();
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();

            // Build texture modifications string
            String textureModifications = buildTextureModifications();

            // Create patient object
            Patient patient = new Patient();
            patient.setPatientFirstName(firstName);
            patient.setPatientLastName(lastName);
            patient.setWing(wing);
            patient.setRoomNumber(roomNumber);
            patient.setDiet(diet);
            patient.setFluidRestriction(fluidRestriction.equals("None") ? null : fluidRestriction);
            patient.setTextureModifications(textureModifications);

            // Save to database
            long result = patientDAO.addPatient(patient);

            if (result > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Success")
                        .setMessage("Patient " + firstName + " " + lastName + " has been added successfully!\n\nLocation: " + wing + " - Room " + roomNumber + "\nDiet: " + diet)
                        .setPositiveButton("Add Another", (dialog, which) -> {
                            // Clear form and stay on this activity for adding more patients
                            clearForm();
                            Toast.makeText(this, "Ready to add another patient", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Done", (dialog, which) -> finish())
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Failed to save patient. Please check all information and try again.")
                        .setPositiveButton("OK", null)
                        .show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to save patient: " + e.getMessage());
        }
    }

    private String buildTextureModifications() {
        List<String> modifications = new ArrayList<>();

        if (mechanicalGroundCB.isChecked()) {
            modifications.add("Mechanical Ground");
        }
        if (mechanicalChoppedCB.isChecked()) {
            modifications.add("Mechanical Chopped");
        }
        if (biteSizeCB.isChecked()) {
            modifications.add("Bite Size");
        }
        if (breadOKCB.isChecked()) {
            modifications.add("Bread OK");
        }

        return modifications.isEmpty() ? null : String.join(", ", modifications);
    }

    private void clearForm() {
        try {
            patientFirstNameEditText.setText("");
            patientLastNameEditText.setText("");
            wingSpinner.setSelection(0);
            dietSpinner.setSelection(0);
            fluidRestrictionSpinner.setSelection(0);

            mechanicalGroundCB.setChecked(false);
            mechanicalChoppedCB.setChecked(false);
            biteSizeCB.setChecked(false);
            breadOKCB.setChecked(false);

            // Clear any errors
            patientFirstNameEditText.setError(null);
            patientLastNameEditText.setError(null);

            // Update room dropdown for selected wing
            updateRoomDropdown();

            // Focus on first name field
            patientFirstNameEditText.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to clear form: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}