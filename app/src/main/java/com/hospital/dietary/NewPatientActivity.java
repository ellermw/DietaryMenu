package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

    private static final String TAG = "NewPatientActivity";

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

        Log.d(TAG, "NewPatientActivity onCreate started");

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
        updateSaveButtonState(); // Initial state

        Log.d(TAG, "NewPatientActivity onCreate completed");
    }

    private void initializeUI() {
        // Basic patient information
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
        nectarThickCheckBox = findViewById(R.id.nectarThickCheckBox);
        puddingThickCheckBox = findViewById(R.id.puddingThickCheckBox);
        honeyThickCheckBox = findViewById(R.id.honeyThickCheckBox);
        extraGravyCheckBox = findViewById(R.id.extraGravyCheckBox);

        // Meats Only toggle
        meatsOnlyCheckBox = findViewById(R.id.meatsOnlyCheckBox);
        meatsOnlyContainer = findViewById(R.id.meatsOnlyContainer);

        // Action buttons
        savePatientButton = findViewById(R.id.savePatientButton);
        clearFormButton = findViewById(R.id.clearFormButton);

        // Initially hide ADA toggle and meats only container
        adaToggleContainer.setVisibility(View.GONE);
        meatsOnlyContainer.setVisibility(View.GONE);
    }

    private void setupSpinners() {
        // Wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Diet type spinner
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid restriction spinner
        String[] fluidRestrictions = {
                "No Restriction",
                "1500ml",
                "2000ml",
                "2500ml",
                "Other"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);

        // Room number spinner - initially empty, populated when wing is selected
        updateRoomNumbers();
    }

    private void updateRoomNumbers() {
        String selectedWing = wingSpinner.getSelectedItem().toString();
        List<String> roomNumbers = new ArrayList<>();
        roomNumbers.add("Select Room");

        // Generate room numbers based on selected wing
        if ("1 South".equals(selectedWing)) {
            for (int i = 101; i <= 120; i++) {
                roomNumbers.add(String.valueOf(i));
            }
        } else if ("2 North".equals(selectedWing)) {
            for (int i = 201; i <= 220; i++) {
                roomNumbers.add(String.valueOf(i));
            }
        } else if ("Labor and Delivery".equals(selectedWing)) {
            for (int i = 301; i <= 310; i++) {
                roomNumbers.add("LD" + (i - 300));
            }
        } else if ("2 West".equals(selectedWing)) {
            for (int i = 221; i <= 240; i++) {
                roomNumbers.add(String.valueOf(i));
            }
        } else if ("3 North".equals(selectedWing)) {
            for (int i = 301; i <= 320; i++) {
                roomNumbers.add(String.valueOf(i));
            }
        } else if ("ICU".equals(selectedWing)) {
            for (int i = 1; i <= 12; i++) {
                roomNumbers.add("ICU" + i);
            }
        }

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomNumbers);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNumberSpinner.setAdapter(roomAdapter);
        roomNumberSpinner.setSelection(0);
    }

    private void setupListeners() {
        // Wing selection listener
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomNumbers();
                updateSaveButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Room selection listener
        roomNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSaveButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // FIXED: Diet type selection listener for ADA toggle visibility
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = dietTypes[position];
                updateSaveButtonState();

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

        // FIXED: Save button with proper logging
        savePatientButton.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            savePatient();
        });

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

    // FIXED: Enhanced save patient method with proper error handling
    private void savePatient() {
        Log.d(TAG, "savePatient() method called");

        if (!savePatientButton.isEnabled()) {
            Log.w(TAG, "Save button is disabled, cannot save");
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Show saving indicator
            savePatientButton.setEnabled(false);
            savePatientButton.setText("Saving...");

            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();

            // Validate inputs
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showError("First name and last name are required");
                resetSaveButton();
                return;
            }

            if (wingSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a wing");
                resetSaveButton();
                return;
            }

            if (roomNumberSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a room number");
                resetSaveButton();
                return;
            }

            if (dietSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a diet type");
                resetSaveButton();
                return;
            }

            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();
            String diet = dietSpinner.getSelectedItem().toString();
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();

            // Create Patient object
            Patient newPatient = new Patient();
            newPatient.setPatientFirstName(firstName);
            newPatient.setPatientLastName(lastName);
            newPatient.setWing(wing);
            newPatient.setRoomNumber(roomNumber);
            newPatient.setDiet(diet);
            newPatient.setAdaDiet(adaToggleCheckBox.isChecked());
            newPatient.setFluidRestriction(fluidRestriction.startsWith("No") ? "No Restriction" : fluidRestriction);

            // Build texture modifications string
            StringBuilder textureModifications = new StringBuilder();
            if (mechanicalChoppedCheckBox.isChecked()) textureModifications.append("Mechanical Chopped, ");
            if (mechanicalGroundCheckBox.isChecked()) textureModifications.append("Mechanical Ground, ");
            if (biteSizeCheckBox.isChecked()) textureModifications.append("Bite Size, ");
            if (breadOKCheckBox.isChecked()) textureModifications.append("Bread OK, ");
            if (nectarThickCheckBox.isChecked()) textureModifications.append("Nectar Thick, ");
            if (puddingThickCheckBox.isChecked()) textureModifications.append("Pudding Thick, ");
            if (honeyThickCheckBox.isChecked()) textureModifications.append("Honey Thick, ");
            if (extraGravyCheckBox.isChecked()) textureModifications.append("Extra Gravy, ");
            if (meatsOnlyCheckBox.isChecked()) textureModifications.append("Meats Only, ");

            if (textureModifications.length() > 0) {
                textureModifications.setLength(textureModifications.length() - 2); // Remove last comma and space
            }
            newPatient.setTextureModifications(textureModifications.toString());

            Log.d(TAG, "Attempting to save patient: " + firstName + " " + lastName);

            // Save to database
            long patientId = patientDAO.addPatient(newPatient);

            if (patientId > 0) {
                Log.d(TAG, "Patient saved successfully with ID: " + patientId);

                String dietName = diet + (adaToggleCheckBox.isChecked() && !diet.contains("ADA") ? " ADA" : "");
                showSuccess("Patient " + firstName + " " + lastName + " added successfully!\n\n" +
                        "✅ " + dietName + " diet has been automatically configured with predetermined menu items.\n" +
                        "All meals are marked as complete.");

                clearForm();
            } else {
                Log.e(TAG, "Failed to save patient - patientDAO.addPatient returned: " + patientId);
                showError("Failed to add patient. Please try again.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving patient", e);
            showError("Error saving patient: " + e.getMessage());
        } finally {
            resetSaveButton();
        }
    }

    private void resetSaveButton() {
        savePatientButton.setText("💾 Save Patient");
        updateSaveButtonState(); // This will re-enable if form is valid
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

    // FIXED: Enhanced button state management
    private void updateSaveButtonState() {
        boolean isValid = !patientFirstNameEditText.getText().toString().trim().isEmpty() &&
                !patientLastNameEditText.getText().toString().trim().isEmpty() &&
                wingSpinner.getSelectedItemPosition() > 0 &&
                roomNumberSpinner.getSelectedItemPosition() > 0 &&
                dietSpinner.getSelectedItemPosition() > 0;

        savePatientButton.setEnabled(isValid);

        // Update button appearance based on state
        if (isValid) {
            savePatientButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            savePatientButton.setAlpha(1.0f);
        } else {
            savePatientButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            savePatientButton.setAlpha(0.5f);
        }

        Log.d(TAG, "Save button enabled: " + isValid);
    }

    // FIXED: Enhanced error and success dialogs
    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuccess(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // No options menu for this activity
        return false;
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