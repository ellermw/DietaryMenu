package com.hospital.dietary;

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
import com.hospital.dietary.dao.DefaultMenuDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class NewPatientActivity extends AppCompatActivity {

    private static final String TAG = "NewPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private DefaultMenuDAO defaultMenuDAO;
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

    // Texture modification checkboxes
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOKCheckBox;
    private CheckBox nectarThickCheckBox;
    private CheckBox puddingThickCheckBox;
    private CheckBox honeyThickCheckBox;
    private CheckBox extraGravyCheckBox;
    private CheckBox meatsOnlyCheckBox;
    private LinearLayout meatsOnlyContainer;

    private Button savePatientButton;

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
        defaultMenuDAO = new DefaultMenuDAO(dbHelper);

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
        meatsOnlyCheckBox = findViewById(R.id.meatsOnlyCheckBox);
        meatsOnlyContainer = findViewById(R.id.meatsOnlyContainer);

        savePatientButton = findViewById(R.id.savePatientButton);

        // FIXED: Set breadOK unchecked by default
        breadOKCheckBox.setChecked(false);
    }

    private void setupSpinners() {
        // Wing Spinner
        String[] wings = {"Select Wing", "1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Room Number Spinner (dynamic based on wing) - Initialize with empty list
        updateRoomNumbers();

        // Diet Spinner - FIXED: Removed Mechanical Chopped and Mechanical Ground
        String[] diets = {"Select Diet", "Regular", "ADA", "Cardiac", "Renal", "Clear Liquid", "Full Liquid", "Puree"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid Restriction Spinner
        String[] fluidRestrictions = {
                "No Fluid Restriction",
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
    }

    private void updateRoomNumbers() {
        // Get selected wing
        String selectedWing = wingSpinner.getSelectedItemPosition() > 0 ?
                (String) wingSpinner.getSelectedItem() : "";

        List<String> roomNumbers = new ArrayList<>();
        roomNumbers.add("Select Room");

        if (!selectedWing.isEmpty()) {
            switch (selectedWing) {
                case "1 South":
                    for (int i = 101; i <= 120; i++) roomNumbers.add(String.valueOf(i));
                    break;
                case "2 North":
                    for (int i = 201; i <= 220; i++) roomNumbers.add(String.valueOf(i));
                    break;
                case "Labor and Delivery":
                    for (int i = 301; i <= 310; i++) roomNumbers.add("LD-" + i);
                    break;
                case "2 West":
                    for (int i = 221; i <= 240; i++) roomNumbers.add(String.valueOf(i));
                    break;
                case "3 North":
                    for (int i = 301; i <= 320; i++) roomNumbers.add(String.valueOf(i));
                    break;
                case "ICU":
                    for (int i = 401; i <= 410; i++) roomNumbers.add("ICU-" + i);
                    break;
            }
        }

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomNumbers);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNumberSpinner.setAdapter(roomAdapter);
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

        // FIXED: Diet selection listener to show ADA toggle for liquid diets
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAdaToggleVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Save button listener
        savePatientButton.setOnClickListener(v -> savePatient());

        // Setup texture modification listeners
        setupTextureModificationListeners();
    }

    // FIXED: Updated method to show ADA toggle for liquid diets as well
    private void updateAdaToggleVisibility() {
        if (dietSpinner.getSelectedItemPosition() > 0) {
            String selectedDiet = (String) dietSpinner.getSelectedItem();

            // Show ADA toggle for ADA diet or liquid diets (Clear Liquid, Full Liquid, Puree)
            boolean showAdaToggle = "ADA".equals(selectedDiet) ||
                    "Clear Liquid".equals(selectedDiet) ||
                    "Full Liquid".equals(selectedDiet) ||
                    "Puree".equals(selectedDiet);

            if (adaToggleContainer != null) {
                adaToggleContainer.setVisibility(showAdaToggle ? View.VISIBLE : View.GONE);
            }

            // For ADA diet, check the toggle by default; for liquid diets, leave unchecked
            if (adaToggleCheckBox != null) {
                adaToggleCheckBox.setChecked("ADA".equals(selectedDiet));
            }
        } else {
            if (adaToggleContainer != null) {
                adaToggleContainer.setVisibility(View.GONE);
            }
        }
    }

    private void setupTextureModificationListeners() {
        // Set up listeners for mechanical modifications to control "Meats Only" visibility
        CompoundButton.OnCheckedChangeListener mechanicalListener = (buttonView, isChecked) -> {
            boolean showMeatsOnly = mechanicalChoppedCheckBox.isChecked() || mechanicalGroundCheckBox.isChecked();
            meatsOnlyContainer.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);
            if (!showMeatsOnly) {
                meatsOnlyCheckBox.setChecked(false);
            }
        };

        mechanicalChoppedCheckBox.setOnCheckedChangeListener(mechanicalListener);
        mechanicalGroundCheckBox.setOnCheckedChangeListener(mechanicalListener);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate first name
        String firstName = patientFirstNameEditText.getText().toString().trim();
        if (firstName.isEmpty()) {
            patientFirstNameEditText.setError("First name is required");
            isValid = false;
        }

        // Validate last name
        String lastName = patientLastNameEditText.getText().toString().trim();
        if (lastName.isEmpty()) {
            patientLastNameEditText.setError("Last name is required");
            isValid = false;
        }

        // Validate wing selection
        if (wingSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate room selection
        if (roomNumberSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a room number", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate diet selection
        if (dietSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a diet type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void savePatient() {
        if (!validateForm()) {
            return;
        }

        // Create new patient object
        Patient patient = new Patient();
        patient.setPatientFirstName(patientFirstNameEditText.getText().toString().trim());
        patient.setPatientLastName(patientLastNameEditText.getText().toString().trim());
        patient.setWing((String) wingSpinner.getSelectedItem());
        patient.setRoomNumber((String) roomNumberSpinner.getSelectedItem());

        // FIXED: Set diet information with proper ADA handling for liquid diets
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        patient.setDietType(selectedDiet);
        patient.setDiet(selectedDiet);

        // For liquid diets, append " (ADA)" if ADA toggle is checked
        boolean isAdaChecked = adaToggleCheckBox.isChecked();
        if (isAdaChecked && ("Clear Liquid".equals(selectedDiet) || "Full Liquid".equals(selectedDiet) || "Puree".equals(selectedDiet))) {
            patient.setDiet(selectedDiet + " (ADA)");
        }
        patient.setAdaDiet(isAdaChecked);

        // Set fluid restriction
        patient.setFluidRestriction((String) fluidRestrictionSpinner.getSelectedItem());

        // Set texture modifications
        patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        patient.setBiteSize(biteSizeCheckBox.isChecked());
        patient.setBreadOK(breadOKCheckBox.isChecked());
        patient.setNectarThick(nectarThickCheckBox.isChecked());
        patient.setPuddingThick(puddingThickCheckBox.isChecked());
        patient.setHoneyThick(honeyThickCheckBox.isChecked());
        patient.setExtraGravy(extraGravyCheckBox.isChecked());
        patient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

        // Build texture modifications string
        List<String> textureMods = new ArrayList<>();
        if (patient.isMechanicalChopped()) textureMods.add("Mechanical Chopped");
        if (patient.isMechanicalGround()) textureMods.add("Mechanical Ground");
        if (patient.isBiteSize()) textureMods.add("Bite Size");
        if (!patient.isBreadOK()) textureMods.add("No Bread");
        if (patient.isNectarThick()) textureMods.add("Nectar Thick");
        if (patient.isPuddingThick()) textureMods.add("Pudding Thick");
        if (patient.isHoneyThick()) textureMods.add("Honey Thick");
        if (patient.isExtraGravy()) textureMods.add("Extra Gravy");
        if (patient.isMeatsOnly()) textureMods.add("Meats Only");

        patient.setTextureModifications(textureMods.isEmpty() ? "Regular" : String.join(", ", textureMods));

        // Set created date
        patient.setCreatedDate(new Date());

        // Save to database
        try {
            long result = patientDAO.addPatient(patient);
            if (result > 0) {
                patient.setPatientId((int) result); // Set the patient ID
                // FIXED: Show success dialog with options instead of just finishing
                showPatientCreatedDialog(patient);
            } else {
                Toast.makeText(this, "Error adding patient. Please try again.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving patient", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // FIXED: New method to show success dialog with options
    private void showPatientCreatedDialog(Patient patient) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("✅ Patient Created Successfully!");
        builder.setMessage("Patient " + patient.getFullName() + " has been added to the system.\n\nWhat would you like to do next?");

        // Edit Meal Plan button
        builder.setPositiveButton("📋 Edit Meal Plan", (dialog, which) -> {
            openMealPlanning(patient);
        });

        // Add Another Patient button
        builder.setNeutralButton("➕ Add Another Patient", (dialog, which) -> {
            clearForm();
            patientFirstNameEditText.requestFocus();
        });

        // Go to Main Menu button
        builder.setNegativeButton("🏠 Main Menu", (dialog, which) -> {
            goToMainMenu();
        });

        builder.setCancelable(false); // Prevent dismissing without choosing
        builder.show();
    }

    // Helper method to open meal planning
    private void openMealPlanning(Patient patient) {
        try {
            Intent intent = new Intent(this, MealPlanningActivity.class);
            intent.putExtra("patient_id", (long) patient.getPatientId());
            intent.putExtra("diet", patient.getDiet());
            intent.putExtra("is_ada_diet", patient.isAdaDiet());
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
            finish(); // Close this activity
        } catch (Exception e) {
            Log.e(TAG, "Error opening meal planning", e);
            Toast.makeText(this, "Error opening meal planning: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to go to main menu
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_patient, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_clear:
                clearForm();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearForm() {
        patientFirstNameEditText.setText("");
        patientLastNameEditText.setText("");
        wingSpinner.setSelection(0);
        roomNumberSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        adaToggleCheckBox.setChecked(false);
        fluidRestrictionSpinner.setSelection(0);

        // FIXED: Clear all texture modifications with breadOK unchecked by default
        mechanicalChoppedCheckBox.setChecked(false);
        mechanicalGroundCheckBox.setChecked(false);
        biteSizeCheckBox.setChecked(false);
        breadOKCheckBox.setChecked(false); // Changed from true to false
        nectarThickCheckBox.setChecked(false);
        puddingThickCheckBox.setChecked(false);
        honeyThickCheckBox.setChecked(false);
        extraGravyCheckBox.setChecked(false);
        meatsOnlyCheckBox.setChecked(false);

        // Hide ADA toggle and meats only container
        if (adaToggleContainer != null) {
            adaToggleContainer.setVisibility(View.GONE);
        }
        meatsOnlyContainer.setVisibility(View.GONE);

        // Reset focus
        patientFirstNameEditText.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    }
}