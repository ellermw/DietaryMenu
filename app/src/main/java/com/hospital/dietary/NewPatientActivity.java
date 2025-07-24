package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Toolbar toolbar;
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

    // Additional Fields
    private EditText allergiesInput;
    private EditText likesInput;
    private EditText dislikesInput;
    private EditText commentsInput;

    // Action Buttons
    private Button savePatientButton;
    private Button cancelButton;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap = new HashMap<>();
    private String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};

    // Edit mode
    private boolean isEditMode = false;
    private int editPatientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Check if in edit mode
        editPatientId = getIntent().getIntExtra("edit_patient_id", -1);
        isEditMode = editPatientId != -1;

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Setup toolbar
        setupToolbar();

        // Setup wing-room mapping
        setupWingRoomMapping();

        // Initialize UI
        initializeUI();

        // Setup spinners
        setupSpinners();

        // Setup listeners
        setupListeners();

        // Load patient data if in edit mode
        if (isEditMode) {
            loadPatientData();
        }
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Patient" : "New Patient");
        }
    }

    private void setupWingRoomMapping() {
        wingRoomMap.put("1 South", generateRoomNumbers(100, 126));
        wingRoomMap.put("2 North", generateRoomNumbers(200, 227));
        wingRoomMap.put("Labor and Delivery", generateRoomNumbers(300, 310));
        wingRoomMap.put("2 West", generateRoomNumbers(228, 250));
        wingRoomMap.put("3 North", generateRoomNumbers(301, 325));
        wingRoomMap.put("ICU", generateRoomNumbers(1, 10));
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

        // Dietary Requirements
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

        // Additional Fields
        allergiesInput = findViewById(R.id.allergiesInput);
        likesInput = findViewById(R.id.likesInput);
        dislikesInput = findViewById(R.id.dislikesInput);
        commentsInput = findViewById(R.id.commentsInput);

        // Action Buttons
        savePatientButton = findViewById(R.id.savePatientButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Update button text if in edit mode
        if (isEditMode && savePatientButton != null) {
            savePatientButton.setText("Update Patient");
        }
    }

    private void setupSpinners() {
        // Wing Spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Diet Spinner - UPDATED DIET TYPES
        String[] dietTypes = {
                "Regular", "Cardiac", "ADA", "Renal", "Puree", "Full Liquid", "Clear Liquid"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid Restrictions
        String[] fluidRestrictions = {
                "No Restriction", "1000ml", "1500ml", "2000ml", "As Ordered"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void updateRoomSpinner(String selectedWing) {
        String[] rooms = wingRoomMap.get(selectedWing);
        if (rooms != null) {
            ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, rooms);
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomSpinner.setAdapter(roomAdapter);
        }
    }

    private void setupListeners() {
        // Wing selection listener - update rooms when wing changes
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                updateRoomSpinner(selectedWing);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Diet selection listener - UPDATED to show ADA checkbox for Puree, Full Liquid, Clear Liquid
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getItemAtPosition(position);
                boolean showAdaOption = "Clear Liquid".equals(selectedDiet) ||
                        "Full Liquid".equals(selectedDiet) ||
                        "Puree".equals(selectedDiet);

                adaDietCheckBox.setVisibility(showAdaOption ? View.VISIBLE : View.GONE);
                adaDietLabel.setVisibility(showAdaOption ? View.VISIBLE : View.GONE);

                if (!showAdaOption) {
                    adaDietCheckBox.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Texture modification listeners
        CompoundButton.OnCheckedChangeListener textureListener = (buttonView, isChecked) -> updateMeatsOnlyVisibility();
        mechanicalGroundCheckBox.setOnCheckedChangeListener(textureListener);
        mechanicalChoppedCheckBox.setOnCheckedChangeListener(textureListener);
        biteSizeCheckBox.setOnCheckedChangeListener(textureListener);

        // Save button
        savePatientButton.setOnClickListener(v -> validateAndSave());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void updateMeatsOnlyVisibility() {
        boolean showMeatsOnly = mechanicalGroundCheckBox.isChecked() ||
                mechanicalChoppedCheckBox.isChecked() ||
                biteSizeCheckBox.isChecked();
        meatsOnlyCheckBox.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);
        if (!showMeatsOnly) {
            meatsOnlyCheckBox.setChecked(false);
        }
    }

    private void loadPatientData() {
        Patient patient = patientDAO.getPatientById(editPatientId);
        if (patient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate fields with patient data
        firstNameInput.setText(patient.getFirstName());
        lastNameInput.setText(patient.getLastName());

        // Set wing
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(patient.getWing())) {
                wingSpinner.setSelection(i);
                break;
            }
        }

        // Wait for room spinner to be populated, then set room
        wingSpinner.post(() -> {
            ArrayAdapter<String> roomAdapter = (ArrayAdapter<String>) roomSpinner.getAdapter();
            if (roomAdapter != null) {
                for (int i = 0; i < roomAdapter.getCount(); i++) {
                    if (roomAdapter.getItem(i).equals(patient.getRoomNumber())) {
                        roomSpinner.setSelection(i);
                        break;
                    }
                }
            }
        });

        // Set diet and other fields...
        // (Implementation continues with all patient fields)
    }

    private void validateAndSave() {
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
            Patient patient = isEditMode ? patientDAO.getPatientById(editPatientId) : new Patient();

            // Basic information
            patient.setFirstName(firstNameInput.getText().toString().trim());
            patient.setLastName(lastNameInput.getText().toString().trim());
            patient.setWing(wingSpinner.getSelectedItem().toString());
            patient.setRoomNumber(roomSpinner.getSelectedItem().toString());

            // Diet information
            patient.setDiet(dietSpinner.getSelectedItem().toString());
            patient.setAdaDiet(adaDietCheckBox.isChecked());
            patient.setFluidRestriction(fluidRestrictionSpinner.getSelectedItem().toString());

            // Texture modifications
            patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
            patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
            patient.setBiteSize(biteSizeCheckBox.isChecked());
            patient.setBreadOK(breadOkCheckBox.isChecked());
            patient.setExtraGravy(extraGravyCheckBox.isChecked());
            patient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

            // Thicken liquids
            patient.setNectarThick(nectarThickCheckBox.isChecked());
            patient.setHoneyThick(honeyThickCheckBox.isChecked());
            patient.setPuddingThick(puddingThickCheckBox.isChecked());

            // Additional fields
            patient.setAllergies(allergiesInput.getText().toString().trim());
            patient.setLikes(likesInput.getText().toString().trim());
            patient.setDislikes(dislikesInput.getText().toString().trim());
            patient.setComments(commentsInput.getText().toString().trim());

            // Set creation date if new patient
            if (!isEditMode) {
                patient.setCreatedAt(System.currentTimeMillis());
            }

            // Save to database
            boolean success;
            if (isEditMode) {
                success = patientDAO.updatePatient(patient);
            } else {
                long newId = patientDAO.insertPatient(patient);
                success = newId != -1;
            }

            if (success) {
                Toast.makeText(this, isEditMode ? "Patient updated successfully" : "Patient added successfully",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error saving patient", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}