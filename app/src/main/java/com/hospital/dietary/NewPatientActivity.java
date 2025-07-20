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
    private Spinner fluidRestrictionSpinner;

    // FIXED: Texture modification checkboxes (multiple selections allowed)
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOKCheckBox;

    private Button savePatientButton;
    private Button clearFormButton;

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
        // Patient information fields
        patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
        patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        adaToggleCheckBox = findViewById(R.id.adaToggleCheckBox);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

        // FIXED: Texture modification checkboxes
        mechanicalChoppedCheckBox = findViewById(R.id.mechanicalChoppedCheckBox);
        mechanicalGroundCheckBox = findViewById(R.id.mechanicalGroundCheckBox);
        biteSizeCheckBox = findViewById(R.id.biteSizeCheckBox);
        breadOKCheckBox = findViewById(R.id.breadOKCheckBox);

        // Action buttons
        savePatientButton = findViewById(R.id.savePatientButton);
        clearFormButton = findViewById(R.id.clearFormButton);

        // Initially hide ADA toggle
        adaToggleCheckBox.setVisibility(View.GONE);
    }

    private void setupSpinners() {
        // Wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Diet spinner
        String[] diets = {
                "Select Diet Type",
                "Regular",
                "Diabetic",
                "Low Sodium",
                "Heart Healthy",
                "Renal",
                "Soft",
                "Clear Liquid",
                "Full Liquid",
                "Pureed",
                "NPO"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid restriction spinner
        String[] fluidRestrictions = {
                "No Restriction",
                "1000ml",
                "1200ml",
                "1500ml",
                "2000ml"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);

        // Setup room spinner (initially empty)
        roomNumberSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Wing First"}));
    }

    private void setupListeners() {
        // Wing selection listener to update rooms
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomOptions(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Diet selection listener to show/hide ADA toggle
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = parent.getItemAtPosition(position).toString();
                // Show ADA toggle only for Diabetic diet
                adaToggleCheckBox.setVisibility("Diabetic".equals(selectedDiet) ? View.VISIBLE : View.GONE);
                if (!"Diabetic".equals(selectedDiet)) {
                    adaToggleCheckBox.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // FIXED: Texture modification logic - multiple selections allowed
        // If Mechanical Chopped or Ground is selected without Bread OK, warn about bread items
        mechanicalChoppedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                validateTextureModifications();
            }
        });

        mechanicalGroundCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                validateTextureModifications();
            }
        });

        // Button listeners
        savePatientButton.setOnClickListener(v -> validateAndSavePatient());
        clearFormButton.setOnClickListener(v -> clearForm());
    }

    // FIXED: Update room options based on correct wing/room mappings
    private void updateRoomOptions(int wingPosition) {
        List<String> rooms = new ArrayList<>();
        rooms.add("Select Room");

        switch (wingPosition) {
            case 1: // 1 South
                for (int i = 106; i <= 122; i++) {
                    rooms.add(String.valueOf(i));
                }
                break;
            case 2: // 2 North
                for (int i = 250; i <= 264; i++) {
                    rooms.add(String.valueOf(i));
                }
                break;
            case 3: // Labor and Delivery
                for (int i = 1; i <= 6; i++) {
                    rooms.add("LDR" + i);
                }
                break;
            case 4: // 2 West
                for (int i = 225; i <= 248; i++) {
                    rooms.add(String.valueOf(i));
                }
                break;
            case 5: // 3 North
                for (int i = 349; i <= 371; i++) {
                    rooms.add(String.valueOf(i));
                }
                break;
            case 6: // ICU
                for (int i = 1; i <= 13; i++) {
                    rooms.add("ICU" + i);
                }
                break;
            default:
                rooms = Arrays.asList("Select Wing First");
                break;
        }

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNumberSpinner.setAdapter(roomAdapter);
    }

    // FIXED: Validate texture modification logic
    private void validateTextureModifications() {
        boolean mechanicalChopped = mechanicalChoppedCheckBox.isChecked();
        boolean mechanicalGround = mechanicalGroundCheckBox.isChecked();
        boolean breadOK = breadOKCheckBox.isChecked();

        // If mechanical restrictions are selected but bread OK is not checked,
        // warn that bread items will be hidden
        if ((mechanicalChopped || mechanicalGround) && !breadOK) {
            Toast.makeText(this,
                    "Note: Bread items will be hidden unless 'Bread OK' is also selected",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void validateAndSavePatient() {
        try {
            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();
            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();
            String diet = dietSpinner.getSelectedItem().toString();
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();

            // Input validation
            if (firstName.isEmpty()) {
                showError("First name is required");
                return;
            }
            if (lastName.isEmpty()) {
                showError("Last name is required");
                return;
            }
            if ("Select Wing".equals(wing)) {
                showError("Please select a wing");
                return;
            }
            if ("Select Room".equals(roomNumber) || "Select Wing First".equals(roomNumber)) {
                showError("Please select a room number");
                return;
            }
            if ("Select Diet Type".equals(diet)) {
                showError("Please select a diet type");
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
                            saveNewPatient(firstName, lastName, wing, roomNumber, diet, fluidRestriction);
                        })
                        .setNegativeButton("Select Different Room", null)
                        .show();
                return;
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
            newPatient.setAdaDiet(adaToggleCheckBox.isChecked());
            newPatient.setFluidRestriction(fluidRestriction);

            // FIXED: Set texture modifications (multiple selections allowed)
            newPatient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
            newPatient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
            newPatient.setBiteSize(biteSizeCheckBox.isChecked());
            newPatient.setBreadOK(breadOKCheckBox.isChecked());

            // Set default completion status
            newPatient.setBreakfastComplete(false);
            newPatient.setLunchComplete(false);
            newPatient.setDinnerComplete(false);

            long result = patientDAO.addPatient(newPatient);

            if (result > 0) {
                Toast.makeText(this, "Patient added successfully!", Toast.LENGTH_SHORT).show();

                // Ask if user wants to create meal plan immediately
                new AlertDialog.Builder(this)
                        .setTitle("Patient Added")
                        .setMessage("Patient " + firstName + " " + lastName + " has been added successfully.\n\nWould you like to create their meal plan now?")
                        .setPositiveButton("Create Meal Plan", (dialog, which) -> {
                            Intent intent = new Intent(this, MealPlanningActivity.class);
                            intent.putExtra("patient_id", (int) result);
                            intent.putExtra("current_user", currentUsername);
                            intent.putExtra("user_role", currentUserRole);
                            intent.putExtra("user_full_name", currentUserFullName);
                            startActivity(intent);
                        })
                        .setNegativeButton("Add Another Patient", (dialog, which) -> clearForm())
                        .setNeutralButton("Return to Menu", (dialog, which) -> finish())
                        .show();
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

        // Reset texture modifications
        mechanicalChoppedCheckBox.setChecked(false);
        mechanicalGroundCheckBox.setChecked(false);
        biteSizeCheckBox.setChecked(false);
        breadOKCheckBox.setChecked(false);
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