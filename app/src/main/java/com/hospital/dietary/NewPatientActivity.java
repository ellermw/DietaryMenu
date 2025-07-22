package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

public class NewPatientActivity extends AppCompatActivity {

    private static final String TAG = "NewPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private EditText firstNameEditText, lastNameEditText;
    private Spinner wingSpinner, roomNumberSpinner, dietSpinner, fluidRestrictionSpinner;
    private LinearLayout adaToggleLayout;
    private Switch adaSwitch;
    private Button savePatientButton, cancelButton;

    // Texture modification checkboxes
    private CheckBox mechanicalChoppedCheckBox, mechanicalGroundCheckBox, biteSizeCheckBox;
    private CheckBox breadOKCheckBox, nectarThickCheckBox, puddingThickCheckBox, honeyThickCheckBox;
    private CheckBox extraGravyCheckBox, meatsOnlyCheckBox;

    // Conditional layout for Meats Only
    private LinearLayout meatsOnlyLayout;

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
            getSupportActionBar().setTitle("New Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupSpinners();
        setupListeners();
        setupTextureModificationLogic();
    }

    private void initializeViews() {
        // Basic patient info
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

        // ADA toggle for liquid diets
        adaToggleLayout = findViewById(R.id.adaToggleLayout);
        adaSwitch = findViewById(R.id.adaSwitch);

        // Buttons
        savePatientButton = findViewById(R.id.savePatientButton);
        cancelButton = findViewById(R.id.cancelButton);

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

        // Conditional layouts
        meatsOnlyLayout = findViewById(R.id.meatsOnlyLayout);

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
                    adaToggleLayout.setVisibility(View.VISIBLE);
                } else {
                    adaToggleLayout.setVisibility(View.GONE);
                    adaSwitch.setChecked(false); // Reset ADA switch when not applicable
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                adaToggleLayout.setVisibility(View.GONE);
            }
        });

        // Save button listener
        savePatientButton.setOnClickListener(v -> savePatient());

        // Cancel button listener
        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupTextureModificationLogic() {
        // Show/hide "Meats Only" option based on Mechanical Ground or Chopped selection
        View.OnClickListener textureListener = v -> {
            boolean showMeatsOnly = mechanicalChoppedCheckBox.isChecked() || mechanicalGroundCheckBox.isChecked();
            meatsOnlyLayout.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);

            if (!showMeatsOnly) {
                meatsOnlyCheckBox.setChecked(false);
            }
        };

        mechanicalChoppedCheckBox.setOnClickListener(textureListener);
        mechanicalGroundCheckBox.setOnClickListener(textureListener);
    }

    private void savePatient() {
        if (!validateInput()) {
            return;
        }

        try {
            Patient patient = createPatientFromInput();
            long result = patientDAO.addPatient(patient);

            if (result > 0) {
                Toast.makeText(this, "Patient added successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Patient added successfully: " + patient.getFullName());

                // Return to previous activity
                setResult(RESULT_OK);
                finish();
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
        if (firstNameEditText.getText().toString().trim().isEmpty()) {
            firstNameEditText.setError("First name is required");
            firstNameEditText.requestFocus();
            return false;
        }

        if (lastNameEditText.getText().toString().trim().isEmpty()) {
            lastNameEditText.setError("Last name is required");
            lastNameEditText.requestFocus();
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
        patient.setPatientFirstName(firstNameEditText.getText().toString().trim());
        patient.setPatientLastName(lastNameEditText.getText().toString().trim());
        patient.setWing(wingSpinner.getSelectedItem().toString());
        patient.setRoomNumber(roomNumberSpinner.getSelectedItem().toString());

        // Diet info
        String selectedDiet = dietSpinner.getSelectedItem().toString();
        boolean isAdaToggleVisible = adaToggleLayout.getVisibility() == View.VISIBLE;
        boolean isAdaSelected = isAdaToggleVisible && adaSwitch.isChecked();

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
            case R.id.action_save:
                savePatient();
                return true;
            case R.id.action_cancel:
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