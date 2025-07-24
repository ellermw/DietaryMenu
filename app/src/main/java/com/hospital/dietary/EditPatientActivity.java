package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.*;

public class EditPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // Patient information
    private int patientId;
    private Patient currentPatient;

    // UI Components - Basic Information
    private EditText firstNameInput;
    private EditText lastNameInput;
    private Spinner wingSpinner;
    private Spinner roomSpinner;

    // UI Components - Dietary Requirements
    private Spinner dietSpinner;
    private CheckBox adaDietCheckBox;
    private TextView adaDietLabel;
    private Spinner fluidRestrictionSpinner;

    // UI Components - Texture Modifications
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOkCheckBox;
    private CheckBox extraGravyCheckBox;
    private CheckBox meatsOnlyCheckBox;

    // UI Components - Thicken Liquids
    private CheckBox nectarThickCheckBox;
    private CheckBox honeyThickCheckBox;
    private CheckBox puddingThickCheckBox;

    // Action Buttons
    private Button saveChangesButton;
    private Button cancelButton;

    // Data
    private String[] wings = {"1st", "2nd", "3rd"};
    private Map<String, String[]> wingRoomMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);

        // Get intent data
        patientId = getIntent().getIntExtra("patient_id", -1);
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        if (patientId == -1) {
            Toast.makeText(this, "Error: Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        // Load patient data
        loadPatientData();

        // Setup listeners
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRoomMappings() {
        wingRoomMap.put("1st", new String[]{"101", "102", "103", "104", "105", "106", "107", "108", "109", "110"});
        wingRoomMap.put("2nd", new String[]{"201", "202", "203", "204", "205", "206", "207", "208", "209", "210"});
        wingRoomMap.put("3rd", new String[]{"301", "302", "303", "304", "305", "306", "307", "308", "309", "310"});
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

        // Action Buttons
        saveChangesButton = findViewById(R.id.saveChangesButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupSpinners() {
        // Wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Diet types
        String[] dietTypes = {
                "Regular", "Cardiac", "Renal", "Low Sodium",
                "Soft", "Clear Liquid", "Full Liquid", "Puree", "NPO"
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

    private void loadPatientData() {
        currentPatient = patientDAO.getPatientById(patientId);

        if (currentPatient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load basic information
        firstNameInput.setText(currentPatient.getPatientFirstName());
        lastNameInput.setText(currentPatient.getPatientLastName());

        // Set wing
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(currentPatient.getWing())) {
                wingSpinner.setSelection(i);
                updateRoomSpinner(wings[i]);
                break;
            }
        }

        // Set room after room spinner is populated
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
        String fluidRestriction = currentPatient.getFluidRestriction();
        if (fluidRestriction == null || fluidRestriction.isEmpty()) {
            fluidRestriction = "No Restriction";
        }
        for (int i = 0; i < fluidAdapter.getCount(); i++) {
            if (fluidAdapter.getItem(i).equals(fluidRestriction)) {
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

        // Save button
        saveChangesButton.setOnClickListener(v -> saveChanges());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void saveChanges() {
        // Validate input
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please enter both first and last name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update patient object
        currentPatient.setPatientFirstName(firstName);
        currentPatient.setPatientLastName(lastName);
        currentPatient.setWing((String) wingSpinner.getSelectedItem());
        currentPatient.setRoomNumber((String) roomSpinner.getSelectedItem());

        // Update diet
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        currentPatient.setDietType(selectedDiet);
        currentPatient.setDiet(selectedDiet);

        // Update ADA flag
        boolean isAda = adaDietCheckBox.getVisibility() == View.VISIBLE && adaDietCheckBox.isChecked();
        currentPatient.setAdaDiet(isAda);

        // Update fluid restriction
        String selectedFluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        currentPatient.setFluidRestriction(selectedFluidRestriction);

        // Update texture modifications
        StringBuilder textureModifications = new StringBuilder();
        if (mechanicalGroundCheckBox.isChecked()) textureModifications.append("Mechanical Ground, ");
        if (mechanicalChoppedCheckBox.isChecked()) textureModifications.append("Mechanical Chopped, ");
        if (biteSizeCheckBox.isChecked()) textureModifications.append("Bite Size, ");
        if (breadOkCheckBox.isChecked()) textureModifications.append("Bread OK, ");
        if (extraGravyCheckBox.isChecked()) textureModifications.append("Extra Gravy, ");
        if (meatsOnlyCheckBox.isChecked()) textureModifications.append("Meats Only, ");
        if (nectarThickCheckBox.isChecked()) textureModifications.append("Nectar Thick, ");
        if (honeyThickCheckBox.isChecked()) textureModifications.append("Honey Thick, ");
        if (puddingThickCheckBox.isChecked()) textureModifications.append("Pudding Thick, ");

        String textureModsString = textureModifications.length() > 0 ?
                textureModifications.substring(0, textureModifications.length() - 2) : "";
        currentPatient.setTextureModifications(textureModsString);

        // Update texture modification flags
        currentPatient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        currentPatient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        currentPatient.setBiteSize(biteSizeCheckBox.isChecked());
        currentPatient.setBreadOK(breadOkCheckBox.isChecked());
        currentPatient.setExtraGravy(extraGravyCheckBox.isChecked());
        currentPatient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

        // Update thicken liquids flags
        currentPatient.setNectarThick(nectarThickCheckBox.isChecked());
        currentPatient.setHoneyThick(honeyThickCheckBox.isChecked());
        currentPatient.setPuddingThick(puddingThickCheckBox.isChecked());

        // Save to database
        int result = patientDAO.updatePatient(currentPatient);

        if (result > 0) {
            Toast.makeText(this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error updating patient. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_patient, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                saveChanges();
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