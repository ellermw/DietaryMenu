package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private int patientId;
    private Patient currentPatient;

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

    // Discharge status
    private CheckBox dischargedCheckBox;

    // Action Buttons
    private Button updatePatientButton;
    private Button deletePatientButton;
    private Button cancelButton;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap = new HashMap<>();
    private String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);

        // Get user information and patient ID from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        patientId = getIntent().getIntExtra("patient_id", -1);

        if (patientId == -1) {
            Toast.makeText(this, "Error: Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        // Load patient data
        loadPatientData();

        // Setup listeners
        setupListeners();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Patient");
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

        // Discharge status
        dischargedCheckBox = findViewById(R.id.dischargedCheckBox);

        // Action Buttons
        updatePatientButton = findViewById(R.id.updatePatientButton);
        deletePatientButton = findViewById(R.id.deletePatientButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupSpinners() {
        // Wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Diet types - UPDATED
        String[] dietTypes = {
                "Regular", "Cardiac", "ADA", "Renal", "Puree", "Full Liquid", "Clear Liquid"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid restrictions
        String[] fluidRestrictions = {
                "No Restriction", "1000ml", "1500ml", "2000ml", "As Ordered"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void loadPatientData() {
        currentPatient = patientDAO.getPatientById(patientId);
        if (currentPatient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateFields();
    }

    private void populateFields() {
        // Basic information
        firstNameInput.setText(currentPatient.getFirstName());
        lastNameInput.setText(currentPatient.getLastName());

        // Set wing
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(currentPatient.getWing())) {
                wingSpinner.setSelection(i);
                break;
            }
        }

        // Update room spinner and set room
        updateRoomSpinner(currentPatient.getWing());
        wingSpinner.post(() -> {
            ArrayAdapter<String> roomAdapter = (ArrayAdapter<String>) roomSpinner.getAdapter();
            if (roomAdapter != null) {
                for (int i = 0; i < roomAdapter.getCount(); i++) {
                    if (roomAdapter.getItem(i).equals(currentPatient.getRoomNumber())) {
                        roomSpinner.setSelection(i);
                        break;
                    }
                }
            }
        });

        // Set diet
        ArrayAdapter<String> dietAdapter = (ArrayAdapter<String>) dietSpinner.getAdapter();
        for (int i = 0; i < dietAdapter.getCount(); i++) {
            if (dietAdapter.getItem(i).equals(currentPatient.getDiet())) {
                dietSpinner.setSelection(i);
                break;
            }
        }

        // Set ADA checkbox
        adaDietCheckBox.setChecked(currentPatient.isAdaDiet());
        updateAdaCheckboxVisibility(currentPatient.getDiet());

        // Set fluid restriction
        ArrayAdapter<String> fluidAdapter = (ArrayAdapter<String>) fluidRestrictionSpinner.getAdapter();
        for (int i = 0; i < fluidAdapter.getCount(); i++) {
            if (fluidAdapter.getItem(i).equals(currentPatient.getFluidRestriction())) {
                fluidRestrictionSpinner.setSelection(i);
                break;
            }
        }

        // Set texture modifications
        mechanicalGroundCheckBox.setChecked(currentPatient.isMechanicalGround());
        mechanicalChoppedCheckBox.setChecked(currentPatient.isMechanicalChopped());
        biteSizeCheckBox.setChecked(currentPatient.isBiteSize());
        breadOkCheckBox.setChecked(currentPatient.isBreadOK());
        extraGravyCheckBox.setChecked(currentPatient.isExtraGravy());
        meatsOnlyCheckBox.setChecked(currentPatient.isMeatsOnly());

        // Set thicken liquids
        nectarThickCheckBox.setChecked(currentPatient.isNectarThick());
        honeyThickCheckBox.setChecked(currentPatient.isHoneyThick());
        puddingThickCheckBox.setChecked(currentPatient.isPuddingThick());

        // Set additional fields
        if (currentPatient.getAllergies() != null) {
            allergiesInput.setText(currentPatient.getAllergies());
        }
        if (currentPatient.getLikes() != null) {
            likesInput.setText(currentPatient.getLikes());
        }
        if (currentPatient.getDislikes() != null) {
            dislikesInput.setText(currentPatient.getDislikes());
        }
        if (currentPatient.getComments() != null) {
            commentsInput.setText(currentPatient.getComments());
        }

        // Set discharged status
        dischargedCheckBox.setChecked(currentPatient.isDischarged());

        // Update meats only visibility
        updateMeatsOnlyVisibility();
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

    private void updateAdaCheckboxVisibility(String diet) {
        boolean showAdaOption = "Clear Liquid".equals(diet) ||
                "Full Liquid".equals(diet) ||
                "Puree".equals(diet);
        adaDietCheckBox.setVisibility(showAdaOption ? View.VISIBLE : View.GONE);
        adaDietLabel.setVisibility(showAdaOption ? View.VISIBLE : View.GONE);
        if (!showAdaOption) {
            adaDietCheckBox.setChecked(false);
        }
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

    private void setupListeners() {
        // Wing selection listener
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                updateRoomSpinner(selectedWing);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Diet selection listener
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getItemAtPosition(position);
                updateAdaCheckboxVisibility(selectedDiet);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Texture modification listeners
        CompoundButton.OnCheckedChangeListener textureListener = (buttonView, isChecked) -> updateMeatsOnlyVisibility();
        mechanicalGroundCheckBox.setOnCheckedChangeListener(textureListener);
        mechanicalChoppedCheckBox.setOnCheckedChangeListener(textureListener);
        biteSizeCheckBox.setOnCheckedChangeListener(textureListener);

        // Update button
        updatePatientButton.setOnClickListener(v -> validateAndUpdate());

        // Delete button
        deletePatientButton.setOnClickListener(v -> confirmDelete());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void validateAndUpdate() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String wing = wingSpinner.getSelectedItem().toString();
        String room = roomSpinner.getSelectedItem().toString();

        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please enter patient's first and last name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if room is already occupied by another patient
        checkRoomAndUpdate(wing, room);
    }

    private void checkRoomAndUpdate(String wing, String room) {
        // Get all active patients
        List<Patient> activePatients = patientDAO.getActivePatients();
        Patient occupyingPatient = null;

        for (Patient patient : activePatients) {
            // Skip the current patient
            if (patient.getPatientId() == patientId) {
                continue;
            }

            if (patient.getWing().equals(wing) && patient.getRoomNumber().equals(room)) {
                occupyingPatient = patient;
                break;
            }
        }

        if (occupyingPatient != null) {
            // Room is occupied by another patient
            final Patient patientToDischarge = occupyingPatient;
            new AlertDialog.Builder(this)
                    .setTitle("Room Already Occupied")
                    .setMessage("Room " + wing + "-" + room + " is currently assigned to " +
                            patientToDischarge.getFullName() + ".\n\n" +
                            "Would you like to discharge the existing patient and transfer " +
                            currentPatient.getFullName() + " to this room?")
                    .setPositiveButton("Discharge & Transfer", (dialog, which) -> {
                        // Discharge the existing patient
                        patientToDischarge.setDischarged(true);
                        patientDAO.updatePatient(patientToDischarge);
                        // Update current patient
                        updatePatient();
                    })
                    .setNegativeButton("Choose Different Room", (dialog, which) -> {
                        Toast.makeText(this, "Please select a different room", Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            // Room is available
            updatePatient();
        }
    }

    private void updatePatient() {
        try {
            // Update basic information
            currentPatient.setFirstName(firstNameInput.getText().toString().trim());
            currentPatient.setLastName(lastNameInput.getText().toString().trim());
            currentPatient.setWing(wingSpinner.getSelectedItem().toString());
            currentPatient.setRoomNumber(roomSpinner.getSelectedItem().toString());

            // Update diet information
            currentPatient.setDiet(dietSpinner.getSelectedItem().toString());
            currentPatient.setAdaDiet(adaDietCheckBox.isChecked());
            currentPatient.setFluidRestriction(fluidRestrictionSpinner.getSelectedItem().toString());

            // Update texture modifications
            currentPatient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
            currentPatient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
            currentPatient.setBiteSize(biteSizeCheckBox.isChecked());
            currentPatient.setBreadOK(breadOkCheckBox.isChecked());
            currentPatient.setExtraGravy(extraGravyCheckBox.isChecked());
            currentPatient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

            // Update thicken liquids
            currentPatient.setNectarThick(nectarThickCheckBox.isChecked());
            currentPatient.setHoneyThick(honeyThickCheckBox.isChecked());
            currentPatient.setPuddingThick(puddingThickCheckBox.isChecked());

            // Update additional fields
            currentPatient.setAllergies(allergiesInput.getText().toString().trim());
            currentPatient.setLikes(likesInput.getText().toString().trim());
            currentPatient.setDislikes(dislikesInput.getText().toString().trim());
            currentPatient.setComments(commentsInput.getText().toString().trim());

            // Update discharged status
            currentPatient.setDischarged(dischargedCheckBox.isChecked());

            // Save to database
            boolean success = patientDAO.updatePatient(currentPatient);

            if (success) {
                Toast.makeText(this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error updating patient", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete " + currentPatient.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient() {
        try {
            boolean success = patientDAO.deletePatient(patientId);

            if (success) {
                Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error deleting patient", Toast.LENGTH_SHORT).show();
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