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
import androidx.appcompat.app.AlertDialog;
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

    // UI Components that match your existing layout
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
        if (breadOKCheckBox != null) {
            breadOKCheckBox.setChecked(false);
        }
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

        // FIXED: Updated fluid restrictions with your correct system
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

    // FIXED: Updated room numbers to match your requirements
    private void updateRoomNumbers() {
        // Get selected wing
        String selectedWing = wingSpinner.getSelectedItemPosition() > 0 ?
                (String) wingSpinner.getSelectedItem() : "";

        List<String> roomNumbers = new ArrayList<>();
        roomNumbers.add("Select Room");

        if (!selectedWing.isEmpty()) {
            switch (selectedWing) {
                case "1 South":
                    // Rooms 106–122
                    for (int i = 106; i <= 122; i++) {
                        roomNumbers.add(String.valueOf(i));
                    }
                    break;
                case "2 North":
                    // Rooms 250–264
                    for (int i = 250; i <= 264; i++) {
                        roomNumbers.add(String.valueOf(i));
                    }
                    break;
                case "Labor and Delivery":
                    // Rooms LDR1–LDR6
                    for (int i = 1; i <= 6; i++) {
                        roomNumbers.add("LDR" + i);
                    }
                    break;
                case "2 West":
                    // Rooms 225–248
                    for (int i = 225; i <= 248; i++) {
                        roomNumbers.add(String.valueOf(i));
                    }
                    break;
                case "3 North":
                    // Rooms 349–371
                    for (int i = 349; i <= 371; i++) {
                        roomNumbers.add(String.valueOf(i));
                    }
                    break;
                case "ICU":
                    // Rooms ICU1–ICU13
                    for (int i = 1; i <= 13; i++) {
                        roomNumbers.add("ICU" + i);
                    }
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
                String selectedDiet = dietSpinner.getSelectedItem().toString();

                // Show ADA toggle only for Clear Liquid, Full Liquid, and Puree diets
                if (selectedDiet.equals("Clear Liquid") || selectedDiet.equals("Full Liquid") || selectedDiet.equals("Puree")) {
                    if (adaToggleContainer != null) {
                        adaToggleContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (adaToggleContainer != null) {
                        adaToggleContainer.setVisibility(View.GONE);
                    }
                    if (adaToggleCheckBox != null) {
                        adaToggleCheckBox.setChecked(false); // Reset ADA checkbox when not applicable
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (adaToggleContainer != null) {
                    adaToggleContainer.setVisibility(View.GONE);
                }
            }
        });

        // Save button listener
        if (savePatientButton != null) {
            savePatientButton.setOnClickListener(v -> savePatient());
        }

        // Texture modification listeners
        setupTextureModificationListeners();
    }

    private void setupTextureModificationListeners() {
        // Show/hide "Meats Only" option based on Mechanical Ground or Chopped selection
        View.OnClickListener textureListener = v -> {
            boolean showMeatsOnly =
                    (mechanicalChoppedCheckBox != null && mechanicalChoppedCheckBox.isChecked()) ||
                            (mechanicalGroundCheckBox != null && mechanicalGroundCheckBox.isChecked());

            if (meatsOnlyContainer != null) {
                meatsOnlyContainer.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);
            }

            if (!showMeatsOnly && meatsOnlyCheckBox != null) {
                meatsOnlyCheckBox.setChecked(false);
            }
        };

        if (mechanicalChoppedCheckBox != null) {
            mechanicalChoppedCheckBox.setOnClickListener(textureListener);
        }
        if (mechanicalGroundCheckBox != null) {
            mechanicalGroundCheckBox.setOnClickListener(textureListener);
        }
    }

    private void savePatient() {
        if (!validateInput()) {
            return;
        }

        try {
            Patient patient = createPatientFromInput();
            long result = patientDAO.addPatient(patient);

            if (result > 0) {
                Log.d(TAG, "Patient added successfully: " + patient.getFullName());
                showSuccessDialog(patient);
            } else {
                Toast.makeText(this, "Failed to add patient. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to add patient");
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error adding patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error adding patient", e);
        }
    }

    private boolean validateInput() {
        // Validate required fields
        if (patientFirstNameEditText.getText().toString().trim().isEmpty()) {
            patientFirstNameEditText.setError("First name is required");
            patientFirstNameEditText.requestFocus();
            return false;
        }

        if (patientLastNameEditText.getText().toString().trim().isEmpty()) {
            patientLastNameEditText.setError("Last name is required");
            patientLastNameEditText.requestFocus();
            return false;
        }

        if (wingSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (roomNumberSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a room number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dietSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a diet type", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Patient createPatientFromInput() {
        Patient patient = new Patient();

        // Basic info
        patient.setPatientFirstName(patientFirstNameEditText.getText().toString().trim());
        patient.setPatientLastName(patientLastNameEditText.getText().toString().trim());
        patient.setWing(wingSpinner.getSelectedItem().toString());
        patient.setRoomNumber(roomNumberSpinner.getSelectedItem().toString());

        // Diet info
        String selectedDiet = dietSpinner.getSelectedItem().toString();
        boolean isAdaToggleVisible = adaToggleContainer != null && adaToggleContainer.getVisibility() == View.VISIBLE;
        boolean isAdaSelected = isAdaToggleVisible && adaToggleCheckBox != null && adaToggleCheckBox.isChecked();

        // FIXED: Handle ADA diet combination properly
        if (isAdaSelected) {
            patient.setDiet(selectedDiet + " (ADA)");
            patient.setAdaDiet(true);
        } else {
            patient.setDiet(selectedDiet);
            patient.setAdaDiet(false);
        }

        patient.setFluidRestriction(fluidRestrictionSpinner.getSelectedItem().toString());

        // Texture modifications
        patient.setMechanicalChopped(mechanicalChoppedCheckBox != null && mechanicalChoppedCheckBox.isChecked());
        patient.setMechanicalGround(mechanicalGroundCheckBox != null && mechanicalGroundCheckBox.isChecked());
        patient.setBiteSize(biteSizeCheckBox != null && biteSizeCheckBox.isChecked());
        patient.setBreadOK(breadOKCheckBox != null && breadOKCheckBox.isChecked());
        patient.setNectarThick(nectarThickCheckBox != null && nectarThickCheckBox.isChecked());
        patient.setPuddingThick(puddingThickCheckBox != null && puddingThickCheckBox.isChecked());
        patient.setHoneyThick(honeyThickCheckBox != null && honeyThickCheckBox.isChecked());
        patient.setExtraGravy(extraGravyCheckBox != null && extraGravyCheckBox.isChecked());
        patient.setMeatsOnly(meatsOnlyCheckBox != null && meatsOnlyCheckBox.isChecked());

        // Build texture modifications string
        List<String> textureModsList = new ArrayList<>();
        if (patient.isMechanicalChopped()) textureModsList.add("Mechanical Chopped");
        if (patient.isMechanicalGround()) textureModsList.add("Mechanical Ground");
        if (patient.isBiteSize()) textureModsList.add("Bite Size");
        if (!patient.isBreadOK()) textureModsList.add("No Bread");
        if (patient.isNectarThick()) textureModsList.add("Nectar Thick");
        if (patient.isPuddingThick()) textureModsList.add("Pudding Thick");
        if (patient.isHoneyThick()) textureModsList.add("Honey Thick");
        if (patient.isExtraGravy()) textureModsList.add("Extra Gravy");
        if (patient.isMeatsOnly()) textureModsList.add("Meats Only");

        if (textureModsList.isEmpty()) {
            patient.setTextureModifications("Regular");
        } else {
            patient.setTextureModifications(String.join(", ", textureModsList));
        }

        return patient;
    }

    private void showSuccessDialog(Patient patient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Patient Added Successfully!");
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
        fluidRestrictionSpinner.setSelection(0);

        // Clear checkboxes
        if (adaToggleCheckBox != null) adaToggleCheckBox.setChecked(false);
        if (mechanicalChoppedCheckBox != null) mechanicalChoppedCheckBox.setChecked(false);
        if (mechanicalGroundCheckBox != null) mechanicalGroundCheckBox.setChecked(false);
        if (biteSizeCheckBox != null) biteSizeCheckBox.setChecked(false);
        if (breadOKCheckBox != null) breadOKCheckBox.setChecked(false);
        if (nectarThickCheckBox != null) nectarThickCheckBox.setChecked(false);
        if (puddingThickCheckBox != null) puddingThickCheckBox.setChecked(false);
        if (honeyThickCheckBox != null) honeyThickCheckBox.setChecked(false);
        if (extraGravyCheckBox != null) extraGravyCheckBox.setChecked(false);
        if (meatsOnlyCheckBox != null) meatsOnlyCheckBox.setChecked(false);

        // Hide conditional layouts
        if (adaToggleContainer != null) adaToggleContainer.setVisibility(View.GONE);
        if (meatsOnlyContainer != null) meatsOnlyContainer.setVisibility(View.GONE);

        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}