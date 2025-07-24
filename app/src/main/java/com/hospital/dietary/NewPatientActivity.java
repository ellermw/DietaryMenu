package com.hospital.dietary;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPatientActivity extends AppCompatActivity {

    private static final String TAG = "NewPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private EditText firstNameInput;
    private EditText lastNameInput;
    private Spinner wingSpinner;
    private Spinner roomSpinner;
    private Spinner dietSpinner;
    private CheckBox adaDietCheckBox;
    private TextView adaDietLabel;
    private Spinner fluidRestrictionSpinner;

    // Texture Modifications
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOkCheckBox;
    private CheckBox extraGravyCheckBox;
    private CheckBox meatsOnlyCheckBox;

    // Thicken Liquids
    private CheckBox nectarThickCheckBox;
    private CheckBox honeyThickCheckBox;
    private CheckBox puddingThickCheckBox;

    // Action Buttons
    private Button savePatientButton;
    private Button cancelButton;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap = new HashMap<>();
    private String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};

    // Fluid restriction mapping (ml amounts for breakfast, lunch, dinner)
    private Map<String, int[]> fluidRestrictionMap = new HashMap<>();

    // Edit mode
    private boolean isEditMode = false;
    private long editPatientId = -1;
    private Patient currentPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Check if in edit mode
        editPatientId = getIntent().getLongExtra("edit_patient_id", -1);
        isEditMode = editPatientId != -1;

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Setup room mappings
        setupRoomMappings();

        // Setup toolbar
        setupToolbar();

        // Initialize UI
        initializeUI();

        // Setup spinners
        setupSpinners();

        // If in edit mode, load patient data
        if (isEditMode) {
            loadPatientData();
        }

        // Setup listeners
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Edit Patient" : "Add New Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRoomMappings() {
        // Hospital wing and room configurations
        wingRoomMap.put("1 South", generateRoomNumbers(106, 122));
        wingRoomMap.put("2 North", generateRoomNumbers(250, 264));
        wingRoomMap.put("Labor and Delivery", new String[]{"LDR1", "LDR2", "LDR3", "LDR4", "LDR5", "LDR6"});
        wingRoomMap.put("2 West", generateRoomNumbers(225, 248));
        wingRoomMap.put("3 North", generateRoomNumbers(349, 371));
        wingRoomMap.put("ICU", new String[]{"ICU1", "ICU2", "ICU3", "ICU4", "ICU5", "ICU6", "ICU7", "ICU8", "ICU9", "ICU10", "ICU11", "ICU12", "ICU13"});

        // Fluid restriction mappings (breakfast, lunch, dinner in ml)
        fluidRestrictionMap.put("1000ml (34oz)", new int[]{120, 120, 160});
        fluidRestrictionMap.put("1200ml (41oz)", new int[]{250, 170, 180});
        fluidRestrictionMap.put("1500ml (51oz)", new int[]{350, 170, 180});
        fluidRestrictionMap.put("1800ml (61oz)", new int[]{360, 240, 240});
        fluidRestrictionMap.put("2000ml (68oz)", new int[]{320, 240, 240});
        fluidRestrictionMap.put("2500ml (85oz)", new int[]{400, 400, 400});
    }

    private String[] generateRoomNumbers(int start, int end) {
        String[] rooms = new String[end - start + 1];
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = String.valueOf(start + i);
        }
        return rooms;
    }

    private void initializeUI() {
        // Basic Information
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomSpinner = findViewById(R.id.roomSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        adaDietCheckBox = findViewById(R.id.adaDietCheckBox);
        adaDietLabel = findViewById(R.id.adaDietLabel);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

        // Texture Modifications
        mechanicalGroundCheckBox = findViewById(R.id.mechanicalGroundCheckBox);
        mechanicalChoppedCheckBox = findViewById(R.id.mechanicalChoppedCheckBox);
        biteSizeCheckBox = findViewById(R.id.biteSizeCheckBox);
        breadOkCheckBox = findViewById(R.id.breadOkCheckBox);
        extraGravyCheckBox = findViewById(R.id.extraGravyCheckBox);
        meatsOnlyCheckBox = findViewById(R.id.meatsOnlyCheckBox);

        // Thicken Liquids
        nectarThickCheckBox = findViewById(R.id.nectarThickCheckBox);
        honeyThickCheckBox = findViewById(R.id.honeyThickCheckBox);
        puddingThickCheckBox = findViewById(R.id.puddingThickCheckBox);

        // Action Buttons
        savePatientButton = findViewById(R.id.savePatientButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupSpinners() {
        // Wing Spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Diet Spinner
        String[] diets = {"Regular", "Diabetic", "Cardiac", "Low Sodium", "Renal",
                "Soft", "Puree", "Mechanical Soft", "Clear Liquid", "NPO"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid Restriction Spinner
        String[] fluidRestrictions = {"None", "1000ml (34oz)", "1200ml (41oz)",
                "1500ml (51oz)", "1800ml (61oz)", "2000ml (68oz)", "2500ml (85oz)"};
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void setupListeners() {
        // Wing selection changes room options
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                updateRoomSpinner(selectedWing);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Save button
        savePatientButton.setOnClickListener(v -> validateAndSavePatient());

        // Cancel button
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void updateRoomSpinner(String wing) {
        String[] rooms = wingRoomMap.get(wing);
        if (rooms != null) {
            ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, rooms);
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomSpinner.setAdapter(roomAdapter);
        }
    }

    private void validateAndSavePatient() {
        // Get input values
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String wing = wingSpinner.getSelectedItem().toString();
        String room = roomSpinner.getSelectedItem().toString();

        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please enter patient's first and last name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if room is already occupied (exclude current patient if editing)
        checkRoomAndSave(wing, room, firstName, lastName);
    }

    private void checkRoomAndSave(String wing, String room, String firstName, String lastName) {
        // Get all active patients
        List<Patient> activePatients = patientDAO.getActivePatients();
        Patient occupyingPatient = null;

        for (Patient patient : activePatients) {
            // Skip the current patient if we're in edit mode
            if (isEditMode && patient.getPatientId() == editPatientId) {
                continue;
            }

            if (patient.getWing().equals(wing) && patient.getRoomNumber().equals(room)) {
                occupyingPatient = patient;
                break;
            }
        }

        if (occupyingPatient != null) {
            // Room is occupied, show dialog
            final Patient patientToDischarge = occupyingPatient;
            new AlertDialog.Builder(this)
                    .setTitle("Room Already Occupied")
                    .setMessage("Room " + wing + "-" + room + " is currently assigned to " +
                            patientToDischarge.getFullName() + ".\n\n" +
                            "Would you like to discharge the existing patient and assign this room to " +
                            firstName + " " + lastName + "?")
                    .setPositiveButton("Discharge & Replace", (dialog, which) -> {
                        // Discharge the existing patient
                        patientToDischarge.setDischarged(true);
                        patientDAO.updatePatient(patientToDischarge);
                        // Save the new patient
                        savePatient();
                    })
                    .setNegativeButton("Choose Different Room", (dialog, which) -> {
                        // User will select a different room
                        Toast.makeText(this, "Please select a different room", Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            // Room is available, save directly
            savePatient();
        }
    }

    private void savePatient() {
        try {
            Patient patient = isEditMode ? currentPatient : new Patient();

            // Basic Information
            patient.setPatientFirstName(firstNameInput.getText().toString().trim());
            patient.setPatientLastName(lastNameInput.getText().toString().trim());
            patient.setWing(wingSpinner.getSelectedItem().toString());
            patient.setRoomNumber(roomSpinner.getSelectedItem().toString());
            patient.setDietType(dietSpinner.getSelectedItem().toString());
            patient.setDiet(dietSpinner.getSelectedItem().toString());
            patient.setAdaDiet(adaDietCheckBox.isChecked());

            // Fluid Restriction
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();
            patient.setFluidRestriction(fluidRestriction);

            // Texture Modifications
            patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
            patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
            patient.setBiteSize(biteSizeCheckBox.isChecked());
            patient.setBreadOK(breadOkCheckBox.isChecked());
            patient.setExtraGravy(extraGravyCheckBox.isChecked());
            patient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

            // Liquid Thickness
            patient.setNectarThick(nectarThickCheckBox.isChecked());
            patient.setHoneyThick(honeyThickCheckBox.isChecked());
            patient.setPuddingThick(puddingThickCheckBox.isChecked());

            // Build texture modifications string
            StringBuilder textureModifications = new StringBuilder();
            if (patient.isMechanicalGround()) textureModifications.append("Mechanical Ground, ");
            if (patient.isMechanicalChopped()) textureModifications.append("Mechanical Chopped, ");
            if (patient.isBiteSize()) textureModifications.append("Bite Size, ");
            if (patient.isBreadOK()) textureModifications.append("Bread OK, ");
            if (patient.isExtraGravy()) textureModifications.append("Extra Gravy, ");
            if (patient.isMeatsOnly()) textureModifications.append("Meats Only, ");
            if (patient.isNectarThick()) textureModifications.append("Nectar Thick, ");
            if (patient.isHoneyThick()) textureModifications.append("Honey Thick, ");
            if (patient.isPuddingThick()) textureModifications.append("Pudding Thick, ");

            String modifications = textureModifications.toString();
            if (modifications.endsWith(", ")) {
                modifications = modifications.substring(0, modifications.length() - 2);
            }
            patient.setTextureModifications(modifications);

            // Save to database
            long result;
            if (isEditMode) {
                result = patientDAO.updatePatient(patient);
            } else {
                result = patientDAO.insertPatient(patient);
            }

            if (result > 0 || (isEditMode && result >= 0)) {
                Toast.makeText(this, isEditMode ? "Patient updated successfully" : "Patient added successfully",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to save patient", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving patient", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadPatientData() {
        if (editPatientId > 0) {
            currentPatient = patientDAO.getPatientById((int) editPatientId);
            if (currentPatient != null) {
                populateFields();
            } else {
                Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void populateFields() {
        // Basic Information
        firstNameInput.setText(currentPatient.getPatientFirstName());
        lastNameInput.setText(currentPatient.getPatientLastName());

        // Wing
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(currentPatient.getWing())) {
                wingSpinner.setSelection(i);
                break;
            }
        }

        // Room will be set after wing spinner updates

        // Diet
        String diet = currentPatient.getDietType();
        ArrayAdapter dietAdapter = (ArrayAdapter) dietSpinner.getAdapter();
        for (int i = 0; i < dietAdapter.getCount(); i++) {
            if (dietAdapter.getItem(i).toString().equals(diet)) {
                dietSpinner.setSelection(i);
                break;
            }
        }

        // ADA Diet
        adaDietCheckBox.setChecked(currentPatient.isAdaDiet());

        // Fluid Restriction
        String fluidRestriction = currentPatient.getFluidRestriction();
        ArrayAdapter fluidAdapter = (ArrayAdapter) fluidRestrictionSpinner.getAdapter();
        for (int i = 0; i < fluidAdapter.getCount(); i++) {
            if (fluidAdapter.getItem(i).toString().equals(fluidRestriction)) {
                fluidRestrictionSpinner.setSelection(i);
                break;
            }
        }

        // Texture Modifications
        mechanicalGroundCheckBox.setChecked(currentPatient.isMechanicalGround());
        mechanicalChoppedCheckBox.setChecked(currentPatient.isMechanicalChopped());
        biteSizeCheckBox.setChecked(currentPatient.isBiteSize());
        breadOkCheckBox.setChecked(currentPatient.isBreadOK());
        extraGravyCheckBox.setChecked(currentPatient.isExtraGravy());
        meatsOnlyCheckBox.setChecked(currentPatient.isMeatsOnly());

        // Liquid Thickness
        nectarThickCheckBox.setChecked(currentPatient.isNectarThick());
        honeyThickCheckBox.setChecked(currentPatient.isHoneyThick());
        puddingThickCheckBox.setChecked(currentPatient.isPuddingThick());

        // Set room after a delay to ensure wing spinner has updated
        wingSpinner.postDelayed(() -> {
            String room = currentPatient.getRoomNumber();
            ArrayAdapter roomAdapter = (ArrayAdapter) roomSpinner.getAdapter();
            if (roomAdapter != null) {
                for (int i = 0; i < roomAdapter.getCount(); i++) {
                    if (roomAdapter.getItem(i).toString().equals(room)) {
                        roomSpinner.setSelection(i);
                        break;
                    }
                }
            }
        }, 100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}