package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.HashMap;
import java.util.Map;

public class NewPatientActivity extends AppCompatActivity {

    private static final String TAG = "NewPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private Spinner wingSpinner;
    private Spinner roomSpinner;
    private Spinner dietSpinner;
    private CheckBox adaDietCheckBox;
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

    private Button savePatientButton;
    private Button cancelButton;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap;
    private String[] wings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "NewPatientActivity onCreate started");

        // Initialize room mapping
        initializeRoomMapping();

        // Create the patient form layout
        createPatientForm();

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

        setupSpinners();
        setupListeners();

        Log.d(TAG, "NewPatientActivity onCreate completed successfully");
    }

    private void createPatientForm() {
        // Create a scrollable form layout
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        mainLayout.setBackgroundColor(0xFFf8f9fa);

        // Title
        TextView titleText = new TextView(this);
        titleText.setText("👤 Add New Patient");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFF2c3e50);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 30);
        mainLayout.addView(titleText);

        // Basic Information Section
        TextView basicInfoLabel = new TextView(this);
        basicInfoLabel.setText("Basic Information");
        basicInfoLabel.setTextSize(18);
        basicInfoLabel.setTextColor(0xFF2c3e50);
        basicInfoLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        basicInfoLabel.setPadding(0, 20, 0, 10);
        mainLayout.addView(basicInfoLabel);

        // First Name
        TextView firstNameLabel = new TextView(this);
        firstNameLabel.setText("First Name *");
        firstNameLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(firstNameLabel);

        firstNameEdit = new EditText(this);
        firstNameEdit.setHint("Enter patient's first name");
        firstNameEdit.setTextColor(0xFF2c3e50);
        firstNameEdit.setBackgroundColor(0xFFFFFFFF);
        firstNameEdit.setPadding(15, 15, 15, 15);
        mainLayout.addView(firstNameEdit);

        // Last Name
        TextView lastNameLabel = new TextView(this);
        lastNameLabel.setText("Last Name *");
        lastNameLabel.setTextColor(0xFF2c3e50);
        lastNameLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(lastNameLabel);

        lastNameEdit = new EditText(this);
        lastNameEdit.setHint("Enter patient's last name");
        lastNameEdit.setTextColor(0xFF2c3e50);
        lastNameEdit.setBackgroundColor(0xFFFFFFFF);
        lastNameEdit.setPadding(15, 15, 15, 15);
        mainLayout.addView(lastNameEdit);

        // Location Section
        TextView locationLabel = new TextView(this);
        locationLabel.setText("Location");
        locationLabel.setTextSize(18);
        locationLabel.setTextColor(0xFF2c3e50);
        locationLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        locationLabel.setPadding(0, 30, 0, 10);
        mainLayout.addView(locationLabel);

        // Wing Dropdown
        TextView wingLabel = new TextView(this);
        wingLabel.setText("Wing *");
        wingLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(wingLabel);

        wingSpinner = new Spinner(this);
        wingSpinner.setBackgroundColor(0xFFFFFFFF);
        wingSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(wingSpinner);

        // Room Dropdown (depends on wing selection)
        TextView roomLabel = new TextView(this);
        roomLabel.setText("Room Number *");
        roomLabel.setTextColor(0xFF2c3e50);
        roomLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(roomLabel);

        roomSpinner = new Spinner(this);
        roomSpinner.setBackgroundColor(0xFFFFFFFF);
        roomSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(roomSpinner);

        // Dietary Requirements Section
        TextView dietaryLabel = new TextView(this);
        dietaryLabel.setText("Dietary Requirements");
        dietaryLabel.setTextSize(18);
        dietaryLabel.setTextColor(0xFF2c3e50);
        dietaryLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        dietaryLabel.setPadding(0, 30, 0, 10);
        mainLayout.addView(dietaryLabel);

        // Diet Type
        TextView dietLabel = new TextView(this);
        dietLabel.setText("Diet Type *");
        dietLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(dietLabel);

        dietSpinner = new Spinner(this);
        dietSpinner.setBackgroundColor(0xFFFFFFFF);
        dietSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(dietSpinner);

        // ADA Diet (only shows for specific diet types)
        adaDietCheckBox = new CheckBox(this);
        adaDietCheckBox.setText("ADA Diet (Diabetic)");
        adaDietCheckBox.setTextColor(0xFF2c3e50);
        adaDietCheckBox.setPadding(0, 15, 0, 0);
        adaDietCheckBox.setVisibility(View.GONE); // Hidden by default
        mainLayout.addView(adaDietCheckBox);

        // Fluid Restriction
        TextView fluidLabel = new TextView(this);
        fluidLabel.setText("Fluid Restriction");
        fluidLabel.setTextColor(0xFF2c3e50);
        fluidLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(fluidLabel);

        fluidRestrictionSpinner = new Spinner(this);
        fluidRestrictionSpinner.setBackgroundColor(0xFFFFFFFF);
        fluidRestrictionSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(fluidRestrictionSpinner);

        // Texture Modifications and Thicken Liquids in horizontal layout
        LinearLayout textureThickenLayout = new LinearLayout(this);
        textureThickenLayout.setOrientation(LinearLayout.HORIZONTAL);
        textureThickenLayout.setPadding(0, 30, 0, 10);

        // Texture Modifications Section (Left side)
        LinearLayout textureLayout = new LinearLayout(this);
        textureLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textureParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        textureParams.setMargins(0, 0, 10, 0);
        textureLayout.setLayoutParams(textureParams);

        TextView textureLabel = new TextView(this);
        textureLabel.setText("Texture Modifications");
        textureLabel.setTextSize(16);
        textureLabel.setTextColor(0xFF2c3e50);
        textureLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        textureLabel.setPadding(0, 0, 0, 10);
        textureLayout.addView(textureLabel);

        mechanicalGroundCheckBox = new CheckBox(this);
        mechanicalGroundCheckBox.setText("Mechanical Ground");
        mechanicalGroundCheckBox.setTextColor(0xFF2c3e50);
        textureLayout.addView(mechanicalGroundCheckBox);

        mechanicalChoppedCheckBox = new CheckBox(this);
        mechanicalChoppedCheckBox.setText("Mechanical Chopped");
        mechanicalChoppedCheckBox.setTextColor(0xFF2c3e50);
        textureLayout.addView(mechanicalChoppedCheckBox);

        biteSizeCheckBox = new CheckBox(this);
        biteSizeCheckBox.setText("Bite Size");
        biteSizeCheckBox.setTextColor(0xFF2c3e50);
        textureLayout.addView(biteSizeCheckBox);

        breadOkCheckBox = new CheckBox(this);
        breadOkCheckBox.setText("Bread OK");
        breadOkCheckBox.setTextColor(0xFF2c3e50);
        breadOkCheckBox.setChecked(true); // Default to checked
        textureLayout.addView(breadOkCheckBox);

        extraGravyCheckBox = new CheckBox(this);
        extraGravyCheckBox.setText("Extra Gravy");
        extraGravyCheckBox.setTextColor(0xFF2c3e50);
        textureLayout.addView(extraGravyCheckBox);

        // Meats Only toggle (shows when mechanical modifications are selected)
        meatsOnlyCheckBox = new CheckBox(this);
        meatsOnlyCheckBox.setText("Meats Only");
        meatsOnlyCheckBox.setTextColor(0xFF2c3e50);
        meatsOnlyCheckBox.setVisibility(View.GONE); // Hidden by default
        textureLayout.addView(meatsOnlyCheckBox);

        textureThickenLayout.addView(textureLayout);

        // Thicken Liquids Section (Right side)
        LinearLayout thickenLayout = new LinearLayout(this);
        thickenLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams thickenParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        thickenParams.setMargins(10, 0, 0, 0);
        thickenLayout.setLayoutParams(thickenParams);

        TextView thickenLabel = new TextView(this);
        thickenLabel.setText("Thicken Liquids");
        thickenLabel.setTextSize(16);
        thickenLabel.setTextColor(0xFF2c3e50);
        thickenLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        thickenLabel.setPadding(0, 0, 0, 10);
        thickenLayout.addView(thickenLabel);

        nectarThickCheckBox = new CheckBox(this);
        nectarThickCheckBox.setText("Nectar Thick");
        nectarThickCheckBox.setTextColor(0xFF2c3e50);
        thickenLayout.addView(nectarThickCheckBox);

        honeyThickCheckBox = new CheckBox(this);
        honeyThickCheckBox.setText("Honey Thick");
        honeyThickCheckBox.setTextColor(0xFF2c3e50);
        thickenLayout.addView(honeyThickCheckBox);

        puddingThickCheckBox = new CheckBox(this);
        puddingThickCheckBox.setText("Pudding Thick");
        puddingThickCheckBox.setTextColor(0xFF2c3e50);
        thickenLayout.addView(puddingThickCheckBox);

        textureThickenLayout.addView(thickenLayout);
        mainLayout.addView(textureThickenLayout);

        // Buttons Section
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 40, 0, 20);

        savePatientButton = new Button(this);
        savePatientButton.setText("Save Patient");
        savePatientButton.setTextColor(0xFFFFFFFF);
        savePatientButton.setBackgroundColor(0xFF27ae60);
        savePatientButton.setPadding(20, 15, 20, 15);

        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        saveParams.setMargins(0, 0, 10, 0);
        savePatientButton.setLayoutParams(saveParams);
        buttonLayout.addView(savePatientButton);

        cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setTextColor(0xFFFFFFFF);
        cancelButton.setBackgroundColor(0xFF95a5a6);
        cancelButton.setPadding(20, 15, 20, 15);

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        cancelParams.setMargins(10, 0, 0, 0);
        cancelButton.setLayoutParams(cancelParams);
        buttonLayout.addView(cancelButton);

        mainLayout.addView(buttonLayout);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void initializeRoomMapping() {
        wingRoomMap = new HashMap<>();

        // 1 South - Rooms 101-125
        String[] south1Rooms = new String[25];
        for (int i = 0; i < 25; i++) {
            south1Rooms[i] = String.valueOf(101 + i);
        }
        wingRoomMap.put("1 South", south1Rooms);

        // 2 North - Rooms 201-225
        String[] north2Rooms = new String[25];
        for (int i = 0; i < 25; i++) {
            north2Rooms[i] = String.valueOf(201 + i);
        }
        wingRoomMap.put("2 North", north2Rooms);

        // Labor and Delivery - Rooms LDR1-LDR10
        String[] ldRooms = new String[10];
        for (int i = 0; i < 10; i++) {
            ldRooms[i] = "LDR" + (i + 1);
        }
        wingRoomMap.put("Labor and Delivery", ldRooms);

        // 2 West - Rooms 226-250
        String[] west2Rooms = new String[25];
        for (int i = 0; i < 25; i++) {
            west2Rooms[i] = String.valueOf(226 + i);
        }
        wingRoomMap.put("2 West", west2Rooms);

        // 3 North - Rooms 301-325
        String[] north3Rooms = new String[25];
        for (int i = 0; i < 25; i++) {
            north3Rooms[i] = String.valueOf(301 + i);
        }
        wingRoomMap.put("3 North", north3Rooms);

        // ICU - ICU1 through ICU13
        String[] icuRooms = new String[13];
        for (int i = 0; i < 13; i++) {
            icuRooms[i] = "ICU" + (i + 1);
        }
        wingRoomMap.put("ICU", icuRooms);

        // Create wings array for spinner
        wings = wingRoomMap.keySet().toArray(new String[0]);
    }

    private void setupSpinners() {
        // Wing dropdown using the initialized wings
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Initially populate rooms for first wing
        updateRoomSpinner(wings[0]);

        // Diet Types
        String[] dietTypes = {
                "Regular", "Cardiac", "Diabetic", "Renal", "Low Sodium",
                "Soft", "Clear Liquid", "Full Liquid", "Pureed", "NPO"
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

        // Diet selection listener - show/hide ADA checkbox for specific diets
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getItemAtPosition(position);
                boolean showAdaOption = "Clear Liquid".equals(selectedDiet) ||
                        "Full Liquid".equals(selectedDiet) ||
                        "Pureed".equals(selectedDiet);
                adaDietCheckBox.setVisibility(showAdaOption ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Texture modification listeners - show/hide "Meats Only" option
        CompoundButton.OnCheckedChangeListener textureListener = (buttonView, isChecked) -> {
            boolean showMeatsOnly = mechanicalGroundCheckBox.isChecked() ||
                    mechanicalChoppedCheckBox.isChecked() ||
                    biteSizeCheckBox.isChecked();
            meatsOnlyCheckBox.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);
            if (!showMeatsOnly) {
                meatsOnlyCheckBox.setChecked(false);
            }
        };

        mechanicalGroundCheckBox.setOnCheckedChangeListener(textureListener);
        mechanicalChoppedCheckBox.setOnCheckedChangeListener(textureListener);
        biteSizeCheckBox.setOnCheckedChangeListener(textureListener);

        savePatientButton.setOnClickListener(v -> savePatient());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void savePatient() {
        // Validate required fields
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String room = (String) roomSpinner.getSelectedItem();

        if (firstName.isEmpty()) {
            Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show();
            firstNameEdit.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            Toast.makeText(this, "Last name is required", Toast.LENGTH_SHORT).show();
            lastNameEdit.requestFocus();
            return;
        }

        if (wing == null) {
            Toast.makeText(this, "Wing is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (room == null) {
            Toast.makeText(this, "Room number is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build texture modifications string
        StringBuilder textureModifications = new StringBuilder();
        if (mechanicalGroundCheckBox.isChecked()) {
            textureModifications.append("Mechanical Ground");
            if (meatsOnlyCheckBox.isChecked()) textureModifications.append(" (Meats Only)");
            textureModifications.append(", ");
        }
        if (mechanicalChoppedCheckBox.isChecked()) {
            textureModifications.append("Mechanical Chopped");
            if (meatsOnlyCheckBox.isChecked()) textureModifications.append(" (Meats Only)");
            textureModifications.append(", ");
        }
        if (biteSizeCheckBox.isChecked()) {
            textureModifications.append("Bite Size");
            if (meatsOnlyCheckBox.isChecked()) textureModifications.append(" (Meats Only)");
            textureModifications.append(", ");
        }
        if (breadOkCheckBox.isChecked()) {
            textureModifications.append("Bread OK, ");
        }
        if (extraGravyCheckBox.isChecked()) {
            textureModifications.append("Extra Gravy, ");
        }

        // Build thicken liquids string
        StringBuilder thickenLiquids = new StringBuilder();
        if (nectarThickCheckBox.isChecked()) {
            thickenLiquids.append("Nectar Thick, ");
        }
        if (honeyThickCheckBox.isChecked()) {
            thickenLiquids.append("Honey Thick, ");
        }
        if (puddingThickCheckBox.isChecked()) {
            thickenLiquids.append("Pudding Thick, ");
        }

        // Remove trailing commas
        String textureModsStr = textureModifications.toString();
        if (textureModsStr.endsWith(", ")) {
            textureModsStr = textureModsStr.substring(0, textureModsStr.length() - 2);
        }

        String thickenLiquidsStr = thickenLiquids.toString();
        if (thickenLiquidsStr.endsWith(", ")) {
            thickenLiquidsStr = thickenLiquidsStr.substring(0, thickenLiquidsStr.length() - 2);
        }

        // Create new patient object
        Patient patient = new Patient();
        patient.setPatientFirstName(firstName);
        patient.setPatientLastName(lastName);
        patient.setWing(wing);
        patient.setRoomNumber(room);
        patient.setDietType(dietSpinner.getSelectedItem().toString());
        patient.setAdaDiet(adaDietCheckBox.getVisibility() == View.VISIBLE && adaDietCheckBox.isChecked());
        patient.setFluidRestriction(fluidRestrictionSpinner.getSelectedItem().toString());
        patient.setTextureModifications(textureModsStr);

        // Set individual texture flags for database compatibility
        patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        patient.setBiteSize(biteSizeCheckBox.isChecked());
        patient.setBreadOK(breadOkCheckBox.isChecked());

        // Save patient to database
        try {
            long result = patientDAO.addPatient(patient);
            if (result > 0) {
                Toast.makeText(this, "Patient added successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Patient added with ID: " + result);

                // Return to previous activity
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error adding patient", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to add patient");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception adding patient", e);
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}