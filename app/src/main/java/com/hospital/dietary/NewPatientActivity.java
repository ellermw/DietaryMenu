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

public class NewPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

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
    private Button savePatientButton;
    private Button cancelButton;

    // Data
    private String[] wings = {"1st", "2nd", "3rd"};
    private Map<String, String[]> wingRoomMap = new HashMap<>();

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

        // Setup room mappings
        setupRoomMappings();

        // Setup toolbar
        setupToolbar();

        // Initialize UI
        initializeUI();

        // Setup spinners
        setupSpinners();

        // Setup listeners
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Patient");
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
        savePatientButton = findViewById(R.id.savePatientButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Initially hide ADA checkbox
        adaDietCheckBox.setVisibility(View.GONE);
        adaDietLabel.setVisibility(View.GONE);
    }

    private void setupSpinners() {
        // Wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Diet types - Updated to include new diet types
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

        // Diet selection listener - show/hide ADA checkbox for specific diets
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

        // Save button
        savePatientButton.setOnClickListener(v -> savePatient());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void savePatient() {
        // Validate input
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please enter both first and last name", Toast.LENGTH_SHORT).show();
            return;
        }

        String wing = (String) wingSpinner.getSelectedItem();
        String room = (String) roomSpinner.getSelectedItem();

        // Get texture modifications
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

        // Create new patient
        Patient patient = new Patient();
        patient.setPatientFirstName(firstName);
        patient.setPatientLastName(lastName);
        patient.setWing(wing);
        patient.setRoomNumber(room);

        // Set diet type
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        patient.setDietType(selectedDiet);
        patient.setDiet(selectedDiet);

        // Set ADA flag if checkbox is checked and visible
        boolean isAda = adaDietCheckBox.getVisibility() == View.VISIBLE && adaDietCheckBox.isChecked();
        patient.setAdaDiet(isAda);

        // Set fluid restriction
        String selectedFluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        patient.setFluidRestriction(selectedFluidRestriction);

        // Parse and store meal-specific fluid limits
        int[] fluidLimits = parseFluidRestriction(selectedFluidRestriction);
        if (fluidLimits[0] > 0) {
            patient.setBreakfastDrinks("FL:" + fluidLimits[1]);
            patient.setLunchDrinks("FL:" + fluidLimits[2]);
            patient.setDinnerDrinks("FL:" + fluidLimits[3]);
        }

        patient.setTextureModifications(textureModsString);

        // Set texture modification flags
        patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        patient.setBiteSize(biteSizeCheckBox.isChecked());
        patient.setBreadOK(breadOkCheckBox.isChecked());
        patient.setExtraGravy(extraGravyCheckBox.isChecked());
        patient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

        // Set thicken liquids flags
        patient.setNectarThick(nectarThickCheckBox.isChecked());
        patient.setHoneyThick(honeyThickCheckBox.isChecked());
        patient.setPuddingThick(puddingThickCheckBox.isChecked());

        // Set created date
        patient.setCreatedDate(new Date());

        // Save to database
        long result = patientDAO.insertPatient(patient);

        if (result > 0) {
            Toast.makeText(this, "Patient added successfully", Toast.LENGTH_SHORT).show();

            // Return to existing patients activity
            Intent intent = new Intent(this, ExistingPatientsActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error adding patient. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private int[] parseFluidRestriction(String restriction) {
        // Returns array: [totalLimit, breakfastLimit, lunchLimit, dinnerLimit]
        if (restriction == null || restriction.equals("No Restriction")) {
            return new int[]{0, 0, 0, 0};
        }

        try {
            if (restriction.contains("ml")) {
                int total = Integer.parseInt(restriction.replace("ml", "").trim());
                // Distribute evenly across meals with breakfast getting any remainder
                int perMeal = total / 3;
                int remainder = total % 3;
                return new int[]{total, perMeal + remainder, perMeal, perMeal};
            }
        } catch (NumberFormatException e) {
            // Handle "As Ordered" or invalid format
        }

        return new int[]{0, 0, 0, 0};
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
            case R.id.action_clear_form:
                clearForm();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearForm() {
        firstNameInput.setText("");
        lastNameInput.setText("");
        wingSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        adaDietCheckBox.setChecked(false);
        fluidRestrictionSpinner.setSelection(0);

        // Clear all checkboxes
        mechanicalGroundCheckBox.setChecked(false);
        mechanicalChoppedCheckBox.setChecked(false);
        biteSizeCheckBox.setChecked(false);
        breadOkCheckBox.setChecked(false);
        extraGravyCheckBox.setChecked(false);
        meatsOnlyCheckBox.setChecked(false);
        nectarThickCheckBox.setChecked(false);
        honeyThickCheckBox.setChecked(false);
        puddingThickCheckBox.setChecked(false);

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