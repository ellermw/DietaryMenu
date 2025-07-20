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

    // FIXED: Updated texture modification checkboxes with new options
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOKCheckBox;
    private CheckBox nectarThickCheckBox;
    private CheckBox puddingThickCheckBox;
    private CheckBox honeyThickCheckBox;
    private CheckBox extraGravyCheckBox;

    // FIXED: New "Meats Only" toggle for Mechanical Ground/Chopped
    private CheckBox meatsOnlyCheckBox;
    private LinearLayout meatsOnlyContainer;

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

        // Set up toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupSpinners();
        setupListeners();
    }

    private void initializeUI() {
        // Patient basic info
        patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
        patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        adaToggleCheckBox = findViewById(R.id.adaToggleCheckBox);
        adaToggleContainer = findViewById(R.id.adaToggleContainer);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

        // FIXED: Texture modifications with new options
        mechanicalChoppedCheckBox = findViewById(R.id.mechanicalChoppedCheckBox);
        mechanicalGroundCheckBox = findViewById(R.id.mechanicalGroundCheckBox);
        biteSizeCheckBox = findViewById(R.id.biteSizeCheckBox);
        breadOKCheckBox = findViewById(R.id.breadOKCheckBox);
        nectarThickCheckBox = findViewById(R.id.nectarThickCheckBox);
        puddingThickCheckBox = findViewById(R.id.puddingThickCheckBox);
        honeyThickCheckBox = findViewById(R.id.honeyThickCheckBox);
        extraGravyCheckBox = findViewById(R.id.extraGravyCheckBox);

        // FIXED: Meats Only toggle
        meatsOnlyCheckBox = findViewById(R.id.meatsOnlyCheckBox);
        meatsOnlyContainer = findViewById(R.id.meatsOnlyContainer);

        // Buttons
        savePatientButton = findViewById(R.id.savePatientButton);
        clearFormButton = findViewById(R.id.clearFormButton);

        // Initially hide ADA toggle and Meats Only toggle
        adaToggleContainer.setVisibility(View.GONE);
        meatsOnlyContainer.setVisibility(View.GONE);
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

        // FIXED: Updated fluid restrictions with detailed breakdown
        String[] fluidRestrictions = {
                "No Restriction",
                "1000ml (34oz): 120ml, 120ml, 160ml",
                "1200ml (41oz): 250ml, 170ml, 180ml",
                "1500ml (51oz): 350ml, 170ml, 180ml",
                "1800ml (61oz): 360ml, 240ml, 240ml",
                "2000ml (68oz): 320ml, 240ml, 240ml",
                "2500ml (85oz): 400ml, 400ml, 400ml"
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

                // FIXED: Show ADA toggle for Puree, Full Liquid, or Clear Liquid
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

        // FIXED: Mechanical Ground/Chopped checkbox listeners for Meats Only toggle
        mechanicalChoppedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateMeatsOnlyVisibility();
        });

        mechanicalGroundCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateMeatsOnlyVisibility();
        });

        // Save button
        savePatientButton.setOnClickListener(v -> savePatient());

        // Clear form button
        clearFormButton.setOnClickListener(v -> clearForm());

        // Text change listeners for validation
        patientFirstNameEditText.addTextChangedListener(validationWatcher);
        patientLastNameEditText.addTextChangedListener(validationWatcher);
    }

    // FIXED: Update Meats Only toggle visibility
    private void updateMeatsOnlyVisibility() {
        boolean showMeatsOnly = mechanicalChoppedCheckBox.isChecked() || mechanicalGroundCheckBox.isChecked();
        meatsOnlyContainer.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);

        if (!showMeatsOnly) {
            meatsOnlyCheckBox.setChecked(false);
        }
    }

    private void updateRoomNumbers() {
        String selectedWing = wingSpinner.getSelectedItem().toString();
        List<String> roomNumbers = new ArrayList<>();
        roomNumbers.add("Select Room");

        // FIXED: Correct room number mappings for each wing
        switch (selectedWing) {
            case "1 South":
                for (int i = 101; i <= 120; i++) {
                    roomNumbers.add(String.valueOf(i));
                }
                break;
            case "2 North":
                for (int i = 201; i <= 230; i++) {
                    roomNumbers.add(String.valueOf(i));
                }
                break;
            case "Labor and Delivery":
                for (int i = 301; i <= 315; i++) {
                    roomNumbers.add("LD-" + i);
                }
                break;
            case "2 West":
                for (int i = 250; i <= 280; i++) {
                    roomNumbers.add(String.valueOf(i));
                }
                break;
            case "3 North":
                for (int i = 301; i <= 340; i++) {
                    roomNumbers.add(String.valueOf(i));
                }
                break;
            case "ICU":
                for (int i = 401; i <= 420; i++) {
                    roomNumbers.add("ICU-" + i);
                }
                break;
        }

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomNumbers);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNumberSpinner.setAdapter(roomAdapter);
    }

    private void savePatient() {
        try {
            // Get form data
            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();
            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();
            String diet = dietSpinner.getSelectedItem().toString();
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();

            // Validation
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showError("Please enter both first and last name");
                return;
            }

            if (wingSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a wing");
                return;
            }

            if (roomNumberSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a room number");
                return;
            }

            if (dietSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a diet type");
                return;
            }

            // FIXED: Handle ADA toggle for liquid diets
            final String finalDiet;
            if (adaToggleCheckBox.isChecked() &&
                    ("Puree".equals(diet) || "Full Liquid".equals(diet) || "Clear Liquid".equals(diet))) {
                finalDiet = diet + " ADA";
            } else {
                finalDiet = diet;
            }

            // Check if room is already occupied
            Patient existingPatient = patientDAO.getPatientInRoom(wing, roomNumber);
            if (existingPatient != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Room Already Occupied")
                        .setMessage("Room " + roomNumber + " in " + wing + " is already occupied by:\n" +
                                existingPatient.getFullName() + "\n\nDo you want to replace this patient?")
                        .setPositiveButton("Replace Patient", (dialog, which) -> {
                            // Delete existing patient and save new one
                            patientDAO.deletePatient(existingPatient.getPatientId());
                            saveNewPatient(firstName, lastName, wing, roomNumber, finalDiet, fluidRestriction);
                        })
                        .setNegativeButton("Select Different Room", null)
                        .show();
                return;
            }

            // Save new patient
            saveNewPatient(firstName, lastName, wing, roomNumber, finalDiet, fluidRestriction);

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

            // FIXED: Set all texture modifications including new options
            newPatient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
            newPatient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
            newPatient.setBiteSize(biteSizeCheckBox.isChecked());
            newPatient.setBreadOK(breadOKCheckBox.isChecked());

            // Set new texture modification options (you'll need to add these fields to Patient model)
            newPatient.setNectarThick(nectarThickCheckBox.isChecked());
            newPatient.setPuddingThick(puddingThickCheckBox.isChecked());
            newPatient.setHoneyThick(honeyThickCheckBox.isChecked());
            newPatient.setExtraGravy(extraGravyCheckBox.isChecked());
            newPatient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

            // FIXED: Auto-complete liquid diets
            boolean isLiquidDiet = diet.contains("Clear Liquid") || diet.contains("Full Liquid") || diet.contains("Puree");

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
                    String dietName = diet + (adaToggleCheckBox.isChecked() ? " ADA" : "");
                    showSuccess("Patient " + firstName + " " + lastName + " added successfully!\n\n" +
                            "✅ " + dietName + " diet has been automatically configured with predetermined menu items.\n" +
                            "All meals are marked as complete.");
                } else {
                    showSuccess("Patient " + firstName + " " + lastName + " added successfully!");
                }

                clearForm();
            } else {
                showError("Failed to add patient. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error saving patient: " + e.getMessage());
        }
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

        // Clear all texture modification checkboxes
        mechanicalChoppedCheckBox.setChecked(false);
        mechanicalGroundCheckBox.setChecked(false);
        biteSizeCheckBox.setChecked(false);
        breadOKCheckBox.setChecked(false);
        nectarThickCheckBox.setChecked(false);
        puddingThickCheckBox.setChecked(false);
        honeyThickCheckBox.setChecked(false);
        extraGravyCheckBox.setChecked(false);
        meatsOnlyCheckBox.setChecked(false);
        meatsOnlyContainer.setVisibility(View.GONE);

        // Focus on first name field
        patientFirstNameEditText.requestFocus();
    }

    private TextWatcher validationWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSaveButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void updateSaveButtonState() {
        boolean isValid = !patientFirstNameEditText.getText().toString().trim().isEmpty() &&
                !patientLastNameEditText.getText().toString().trim().isEmpty() &&
                wingSpinner.getSelectedItemPosition() > 0 &&
                roomNumberSpinner.getSelectedItemPosition() > 0 &&
                dietSpinner.getSelectedItemPosition() > 0;

        savePatientButton.setEnabled(isValid);
        savePatientButton.setBackgroundColor(isValid ?
                getResources().getColor(android.R.color.holo_green_dark) :
                getResources().getColor(android.R.color.darker_gray));
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuccess(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Success")
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