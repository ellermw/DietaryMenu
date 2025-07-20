package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private EditText patientFirstNameEditText;
    private EditText patientLastNameEditText;
    private Spinner wingSpinner;
    private Spinner roomNumberSpinner;
    private Spinner dietSpinner;
    private CheckBox adaToggleCheckBox;
    private LinearLayout adaToggleContainer;
    private Spinner fluidRestrictionSpinner;

    // Texture modification checkboxes (multiple selections allowed)
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOKCheckBox;

    private Button savePatientButton;
    private Button clearFormButton;

    // FIXED: Corrected diet types to match requirements
    private String[] dietTypes = {
            "Select Diet Type",
            "Regular",
            "Cardiac",
            "ADA",
            "Renal",
            "Puree",
            "Full Liquid",
            "Clear Liquid"
    };

    // FIXED: Corrected wing/room mappings
    private String[] wings = {
            "Select Wing",
            "1 South",
            "2 North",
            "Labor and Delivery",
            "2 West",
            "3 North",
            "ICU"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupSpinners();
        setupListeners();
    }

    private void initializeUI() {
        // Patient information inputs
        patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
        patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        adaToggleCheckBox = findViewById(R.id.adaToggleCheckBox);
        adaToggleContainer = findViewById(R.id.adaToggleContainer);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

        // Texture modification checkboxes
        mechanicalChoppedCheckBox = findViewById(R.id.mechanicalChoppedCheckBox);
        mechanicalGroundCheckBox = findViewById(R.id.mechanicalGroundCheckBox);
        biteSizeCheckBox = findViewById(R.id.biteSizeCheckBox);
        breadOKCheckBox = findViewById(R.id.breadOKCheckBox);

        // Action buttons
        savePatientButton = findViewById(R.id.savePatientButton);
        clearFormButton = findViewById(R.id.clearFormButton);

        // FIXED: Initially hide ADA toggle - only show for specific diet types
        adaToggleContainer.setVisibility(View.GONE);
    }

    private void setupSpinners() {
        // Wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Diet type spinner - FIXED: Updated with correct diet types
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid restriction spinner
        String[] fluidRestrictions = {
                "No Restriction",
                "1000 mL",
                "1500 mL",
                "2000 mL",
                "2500 mL"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);

        // Room number will be populated when wing is selected
        updateRoomNumbers();
    }

    private void setupListeners() {
        // Wing selection listener
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomNumbers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // FIXED: Diet type selection listener for ADA toggle visibility
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = dietTypes[position];

                // FIXED: Only show ADA toggle for Puree, Full Liquid, or Clear Liquid
                if ("Puree".equals(selectedDiet) || "Full Liquid".equals(selectedDiet) || "Clear Liquid".equals(selectedDiet)) {
                    adaToggleContainer.setVisibility(View.VISIBLE);
                } else {
                    adaToggleContainer.setVisibility(View.GONE);
                    adaToggleCheckBox.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Save patient button
        savePatientButton.setOnClickListener(v -> validateAndSavePatient());

        // Clear form button
        clearFormButton.setOnClickListener(v -> clearForm());
    }

    private void updateRoomNumbers() {
        String selectedWing = wingSpinner.getSelectedItem().toString();
        List<String> rooms = new ArrayList<>();
        rooms.add("Select Room");

        // Generate room numbers based on wing
        switch (selectedWing) {
            case "1 South":
                for (int i = 101; i <= 150; i++) {
                    rooms.add(String.valueOf(i));
                }
                break;
            case "2 North":
                for (int i = 201; i <= 250; i++) {
                    rooms.add(String.valueOf(i));
                }
                break;
            case "Labor and Delivery":
                for (int i = 301; i <= 320; i++) {
                    rooms.add("L&D " + i);
                }
                break;
            case "2 West":
                for (int i = 251; i <= 280; i++) {
                    rooms.add(String.valueOf(i));
                }
                break;
            case "3 North":
                for (int i = 351; i <= 380; i++) {
                    rooms.add(String.valueOf(i));
                }
                break;
            case "ICU":
                for (int i = 401; i <= 420; i++) {
                    rooms.add("ICU " + i);
                }
                break;
        }

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNumberSpinner.setAdapter(roomAdapter);
    }

    private void validateAndSavePatient() {
        try {
            // Validate inputs
            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();
            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();
            String diet = dietSpinner.getSelectedItem().toString();
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();

            if (firstName.isEmpty()) {
                showError("Please enter patient's first name");
                return;
            }

            if (lastName.isEmpty()) {
                showError("Please enter patient's last name");
                return;
            }

            if ("Select Wing".equals(wing)) {
                showError("Please select a wing");
                return;
            }

            if ("Select Room".equals(roomNumber)) {
                showError("Please select a room number");
                return;
            }

            if ("Select Diet Type".equals(diet)) {
                showError("Please select a diet type");
                return;
            }

            // Check if room is already occupied
            List<Patient> existingPatients = patientDAO.getAllPatients();
            for (Patient existingPatient : existingPatients) {
                if (wing.equals(existingPatient.getWing()) && roomNumber.equals(existingPatient.getRoomNumber())) {
                    new AlertDialog.Builder(this)
                            .setTitle("Room Already Occupied")
                            .setMessage("Room " + roomNumber + " in " + wing + " is already occupied by " +
                                    existingPatient.getPatientFirstName() + " " + existingPatient.getPatientLastName() +
                                    ".\n\nWould you like to replace this patient?")
                            .setPositiveButton("Replace Patient", (dialog, which) -> {
                                // Delete existing patient and save new one
                                patientDAO.deletePatient(existingPatient.getPatientId());
                                saveNewPatient(firstName, lastName, wing, roomNumber, diet, fluidRestriction);
                            })
                            .setNegativeButton("Select Different Room", null)
                            .show();
                    return;
                }
            }

            // Save new patient
            saveNewPatient(firstName, lastName, wing, roomNumber, diet, fluidRestriction);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error saving patient: " + e.getMessage());
        }
    }

    private void saveNewPatient(String firstName, String lastName, String wing, String roomNumber, String diet, String fluidRestriction) {
        try {
            Patient newPatient = new Patient();
            newPatient.setPatientFirstName(firstName);
            newPatient.setPatientLastName(lastName);
            newPatient.setWing(wing);
            newPatient.setRoomNumber(roomNumber);
            newPatient.setDietType(diet);
            newPatient.setDiet(diet); // Keep both fields in sync
            newPatient.setAdaDiet(adaToggleCheckBox.isChecked());
            newPatient.setFluidRestriction(fluidRestriction);

            // Set texture modifications (multiple selections allowed)
            newPatient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
            newPatient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
            newPatient.setBiteSize(biteSizeCheckBox.isChecked());
            newPatient.setBreadOK(breadOKCheckBox.isChecked());

            // FIXED: Auto-complete liquid diets
            boolean isLiquidDiet = diet.equals("Clear Liquid") || diet.equals("Full Liquid") || diet.equals("Puree");

            if (isLiquidDiet) {
                // Auto-complete all meals for liquid diets
                newPatient.setBreakfastComplete(true);
                newPatient.setLunchComplete(true);
                newPatient.setDinnerComplete(true);
            } else {
                // Set default completion status for regular diets
                newPatient.setBreakfastComplete(false);
                newPatient.setLunchComplete(false);
                newPatient.setDinnerComplete(false);
            }

            long result = patientDAO.addPatient(newPatient);

            if (result > 0) {
                if (isLiquidDiet) {
                    // FIXED: Special notification for liquid diets
                    String dietName = diet + (adaToggleCheckBox.isChecked() ? " (ADA)" : "");
                    new AlertDialog.Builder(this)
                            .setTitle("Patient Added - Diet Auto-Completed")
                            .setMessage("Patient " + firstName + " " + lastName + " has been added successfully.\n\n" +
                                    "The " + dietName + " diet has been auto-completed with predetermined menu items.\n\n" +
                                    "What would you like to do next?")
                            .setPositiveButton("View Patient Details", (dialog, which) -> {
                                Intent intent = new Intent(this, PatientDetailActivity.class);
                                intent.putExtra("patient_id", (int) result);
                                intent.putExtra("current_user", currentUsername);
                                intent.putExtra("user_role", currentUserRole);
                                intent.putExtra("user_full_name", currentUserFullName);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton("Add Another Patient", (dialog, which) -> clearForm())
                            .setNeutralButton("Return to Menu", (dialog, which) -> finish())
                            .show();
                } else {
                    // Regular diet - offer meal planning
                    new AlertDialog.Builder(this)
                            .setTitle("Patient Added")
                            .setMessage("Patient " + firstName + " " + lastName + " has been added successfully.\n\n" +
                                    "Would you like to create their meal plan now?")
                            .setPositiveButton("Create Meal Plan", (dialog, which) -> {
                                Intent intent = new Intent(this, MealPlanningActivity.class);
                                intent.putExtra("patient_id", (int) result);
                                intent.putExtra("current_user", currentUsername);
                                intent.putExtra("user_role", currentUserRole);
                                intent.putExtra("user_full_name", currentUserFullName);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton("Add Another Patient", (dialog, which) -> clearForm())
                            .setNeutralButton("Return to Menu", (dialog, which) -> finish())
                            .show();
                }
            } else {
                showError("Failed to add patient. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error saving patient: " + e.getMessage());
        }
    }

    private String getTextureModifications() {
        List<String> modifications = new ArrayList<>();

        if (mechanicalChoppedCheckBox.isChecked()) modifications.add("Mechanical Chopped");
        if (mechanicalGroundCheckBox.isChecked()) modifications.add("Mechanical Ground");
        if (biteSizeCheckBox.isChecked()) modifications.add("Bite Size");
        if (breadOKCheckBox.isChecked()) modifications.add("Bread OK");

        return modifications.isEmpty() ? "None" : String.join(", ", modifications);
    }

    private void clearForm() {
        patientFirstNameEditText.setText("");
        patientLastNameEditText.setText("");
        wingSpinner.setSelection(0);
        roomNumberSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        adaToggleCheckBox.setChecked(false);
        adaToggleContainer.setVisibility(View.GONE);
        fluidRestrictionSpinner.setSelection(0);

        mechanicalChoppedCheckBox.setChecked(false);
        mechanicalGroundCheckBox.setChecked(false);
        biteSizeCheckBox.setChecked(false);
        breadOKCheckBox.setChecked(false);

        patientFirstNameEditText.requestFocus();
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Input Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}