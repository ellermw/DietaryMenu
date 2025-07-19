package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
    private CheckBox adaToggleCheckBox; // FIXED: Dynamic ADA toggle
    private Spinner fluidRestrictionSpinner;

    // Texture modification checkboxes
    private CheckBox pureedCheckBox;
    private CheckBox choppedCheckBox;
    private CheckBox softCheckBox;
    private CheckBox regularCheckBox;

    private Button savePatientButton;
    private Button clearFormButton;
    private Button backButton;

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

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupSpinners();
        setupListeners();
    }

    private void initializeUI() {
        patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
        patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        adaToggleCheckBox = findViewById(R.id.adaToggleCheckBox); // FIXED: ADA toggle
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

        // Texture modification checkboxes
        pureedCheckBox = findViewById(R.id.pureedCheckBox);
        choppedCheckBox = findViewById(R.id.choppedCheckBox);
        softCheckBox = findViewById(R.id.softCheckBox);
        regularCheckBox = findViewById(R.id.regularCheckBox);

        savePatientButton = findViewById(R.id.savePatientButton);
        clearFormButton = findViewById(R.id.clearFormButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupSpinners() {
        // Wing options
        String[] wings = {"North Wing", "South Wing", "East Wing", "West Wing", "ICU", "Emergency"};
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Room numbers (100-599)
        List<String> rooms = new ArrayList<>();
        for (int i = 100; i <= 599; i++) {
            rooms.add(String.valueOf(i));
        }
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNumberSpinner.setAdapter(roomAdapter);

        // FIXED: Simplified diet options (removed redundant ADA diets)
        String[] diets = {
                "Regular",
                "Cardiac",
                "ADA Diabetic",
                "Puree",
                "Renal",
                "Full Liquid",
                "Clear Liquid"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid restriction options
        String[] fluidOptions = {"None", "1000ml", "1500ml", "2000ml", "NPO"};
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidOptions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void setupListeners() {
        savePatientButton.setOnClickListener(v -> validateAndSavePatient());
        clearFormButton.setOnClickListener(v -> clearForm());
        backButton.setOnClickListener(v -> finish());

        // FIXED: Diet spinner listener to show/hide ADA toggle
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = dietSpinner.getSelectedItem().toString();

                // FIXED: Only show ADA toggle for Clear Liquid diet
                if ("Clear Liquid".equals(selectedDiet)) {
                    adaToggleCheckBox.setVisibility(View.VISIBLE);
                    adaToggleCheckBox.setText("ADA Friendly (Sugar-free options)");
                } else {
                    adaToggleCheckBox.setVisibility(View.GONE);
                    adaToggleCheckBox.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Texture modification - only allow one selection
        setupTextureModificationListeners();
    }

    private void setupTextureModificationListeners() {
        CompoundButton.OnCheckedChangeListener textureListener = (buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck all other texture modifications
                if (buttonView != pureedCheckBox) pureedCheckBox.setChecked(false);
                if (buttonView != choppedCheckBox) choppedCheckBox.setChecked(false);
                if (buttonView != softCheckBox) softCheckBox.setChecked(false);
                if (buttonView != regularCheckBox) regularCheckBox.setChecked(false);

                // Check the selected one
                buttonView.setChecked(true);
            }
        };

        pureedCheckBox.setOnCheckedChangeListener(textureListener);
        choppedCheckBox.setOnCheckedChangeListener(textureListener);
        softCheckBox.setOnCheckedChangeListener(textureListener);
        regularCheckBox.setOnCheckedChangeListener(textureListener);

        // Default to regular texture
        regularCheckBox.setChecked(true);
    }

    private void validateAndSavePatient() {
        try {
            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();
            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();

            // Input validation
            if (firstName.isEmpty()) {
                showError("First name is required");
                return;
            }
            if (lastName.isEmpty()) {
                showError("Last name is required");
                return;
            }

            // Check for existing patient in same room
            Patient existingPatient = getPatientByWingAndRoom(wing, roomNumber);
            if (existingPatient != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Room Already Occupied")
                        .setMessage("Patient " + existingPatient.getPatientFirstName() + " " +
                                existingPatient.getPatientLastName() + " is already in " +
                                wing + " Room " + roomNumber + ".\n\nWhat would you like to do?")
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
            String baseDiet = dietSpinner.getSelectedItem().toString();
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();

            // FIXED: Build final diet string with ADA consideration
            String finalDiet = baseDiet;
            if ("Clear Liquid".equals(baseDiet) && adaToggleCheckBox.isChecked()) {
                finalDiet = "Clear Liquid ADA";
            }

            // Build texture modifications string
            String textureModifications = buildTextureModifications();

            // Create patient object
            Patient patient = new Patient();
            patient.setPatientFirstName(firstName);
            patient.setPatientLastName(lastName);
            patient.setWing(wing);
            patient.setRoomNumber(roomNumber);
            patient.setDiet(finalDiet);
            patient.setFluidRestriction(fluidRestriction.equals("None") ? null : fluidRestriction);
            patient.setTextureModifications(textureModifications);

            // FIXED: Handle Clear Liquid diets properly
            if (finalDiet.startsWith("Clear Liquid")) {
                // Clear liquid patients get predetermined items
                patient.setBreakfastComplete(true);
                patient.setLunchComplete(true);
                patient.setDinnerComplete(true);
                patient.setBreakfastNPO(false);
                patient.setLunchNPO(false);
                patient.setDinnerNPO(false);
            } else {
                // Regular patients start with incomplete meals
                patient.setBreakfastComplete(false);
                patient.setLunchComplete(false);
                patient.setDinnerComplete(false);
                patient.setBreakfastNPO(false);
                patient.setLunchNPO(false);
                patient.setDinnerNPO(false);
            }

            // Save to database
            long result = patientDAO.addPatient(patient);

            if (result > 0) {
                String message = "Patient " + firstName + " " + lastName + " has been added successfully!" +
                        "\n\nLocation: " + wing + " - Room " + roomNumber +
                        "\nDiet: " + finalDiet;

                // Add special message for Clear Liquid patients
                if (finalDiet.startsWith("Clear Liquid")) {
                    if (finalDiet.contains("ADA")) {
                        message += "\n\n✅ Clear Liquid ADA diet automatically completed - " +
                                "ADA-friendly options (Apple Juice, Sprite Zero, Sugar-free items) will be provided.";
                    } else {
                        message += "\n\n✅ Clear Liquid diet automatically completed - " +
                                "predetermined menu items will be provided.";
                    }
                }

                new AlertDialog.Builder(this)
                        .setTitle("Success")
                        .setMessage(message)
                        .setPositiveButton("Add Another", (dialog, which) -> {
                            clearForm();
                            Toast.makeText(this, "Ready to add another patient", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Done", (dialog, which) -> finish())
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Failed to save patient. Please try again.")
                        .setPositiveButton("OK", null)
                        .show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Save error: " + e.getMessage());
        }
    }

    private String buildTextureModifications() {
        List<String> modifications = new ArrayList<>();

        if (pureedCheckBox.isChecked()) modifications.add("Pureed");
        if (choppedCheckBox.isChecked()) modifications.add("Chopped");
        if (softCheckBox.isChecked()) modifications.add("Soft");
        if (regularCheckBox.isChecked()) modifications.add("Regular");

        return modifications.isEmpty() ? "Regular" : String.join(", ", modifications);
    }

    private Patient getPatientByWingAndRoom(String wing, String roomNumber) {
        try {
            List<Patient> allPatients = patientDAO.getAllPatients();
            for (Patient patient : allPatients) {
                if (wing.equals(patient.getWing()) && roomNumber.equals(patient.getRoomNumber())) {
                    return patient;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearForm() {
        patientFirstNameEditText.setText("");
        patientLastNameEditText.setText("");
        wingSpinner.setSelection(0);
        roomNumberSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        adaToggleCheckBox.setChecked(false);
        adaToggleCheckBox.setVisibility(View.GONE);
        fluidRestrictionSpinner.setSelection(0);

        // Reset texture modifications to regular
        pureedCheckBox.setChecked(false);
        choppedCheckBox.setChecked(false);
        softCheckBox.setChecked(false);
        regularCheckBox.setChecked(true);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
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
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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