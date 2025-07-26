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

    // UI Components - Basic Information
    private EditText firstNameInput;
    private EditText lastNameInput;
    private Spinner wingSpinner;
    private Spinner roomSpinner;

    // UI Components - Dietary Requirements
    private Spinner dietSpinner;
    private CheckBox adaDietCheckBox;
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

    // UI Components - Additional Information
    private EditText allergiesInput;
    private EditText commentsInput;

    // Buttons
    private Button saveButton;
    private Button cancelButton;

    // Data
    private String[] wings = {"North", "South", "East", "West"};
    private Map<String, String[]> wingRoomMap = new HashMap<>();
    private boolean isEditMode = false;
    private long editPatientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup room mappings
        setupRoomMappings();

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Get user data and check if edit mode
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        editPatientId = getIntent().getLongExtra("patient_id", -1);
        isEditMode = editPatientId != -1;

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Patient" : "Add New Patient");
        }

        // Create UI programmatically
        createUI();

        // Setup spinners and listeners
        setupSpinners();
        setupListeners();

        // If edit mode, load patient data
        if (isEditMode) {
            loadPatientData();
        }
    }

    private void setupRoomMappings() {
        wingRoomMap.put("North", new String[]{"100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110"});
        wingRoomMap.put("South", new String[]{"200", "201", "202", "203", "204", "205", "206", "207", "208", "209", "210"});
        wingRoomMap.put("East", new String[]{"300", "301", "302", "303", "304", "305", "306", "307", "308", "309", "310"});
        wingRoomMap.put("West", new String[]{"400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410"});
    }

    private void createUI() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFFF8FAFC);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);

        // Basic Information Section
        TextView basicInfoLabel = new TextView(this);
        basicInfoLabel.setText("Basic Information");
        basicInfoLabel.setTextSize(18);
        basicInfoLabel.setTextColor(0xFF2c3e50);
        basicInfoLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        basicInfoLabel.setPadding(0, 0, 0, 10);
        mainLayout.addView(basicInfoLabel);

        // First Name
        TextView firstNameLabel = new TextView(this);
        firstNameLabel.setText("First Name *");
        firstNameLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(firstNameLabel);

        firstNameInput = new EditText(this);
        firstNameInput.setHint("Enter first name");
        firstNameInput.setBackgroundColor(0xFFFFFFFF);
        firstNameInput.setPadding(15, 15, 15, 15);
        mainLayout.addView(firstNameInput);

        // Last Name
        TextView lastNameLabel = new TextView(this);
        lastNameLabel.setText("Last Name *");
        lastNameLabel.setTextColor(0xFF2c3e50);
        lastNameLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(lastNameLabel);

        lastNameInput = new EditText(this);
        lastNameInput.setHint("Enter last name");
        lastNameInput.setBackgroundColor(0xFFFFFFFF);
        lastNameInput.setPadding(15, 15, 15, 15);
        mainLayout.addView(lastNameInput);

        // Wing
        TextView wingLabel = new TextView(this);
        wingLabel.setText("Wing *");
        wingLabel.setTextColor(0xFF2c3e50);
        wingLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(wingLabel);

        wingSpinner = new Spinner(this);
        wingSpinner.setBackgroundColor(0xFFFFFFFF);
        wingSpinner.setPadding(15, 15, 15, 15);
        wingSpinner.setMinimumHeight(120);
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
        roomSpinner.setMinimumHeight(120);
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
        dietSpinner.setMinimumHeight(120);
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
        fluidRestrictionSpinner.setMinimumHeight(120);
        mainLayout.addView(fluidRestrictionSpinner);

        // Texture Modifications
        TextView textureLabel = new TextView(this);
        textureLabel.setText("Texture Modifications");
        textureLabel.setTextSize(18);
        textureLabel.setTextColor(0xFF2c3e50);
        textureLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        textureLabel.setPadding(0, 30, 0, 10);
        mainLayout.addView(textureLabel);

        // Texture checkboxes
        mechanicalGroundCheckBox = new CheckBox(this);
        mechanicalGroundCheckBox.setText("Mechanical Ground");
        mechanicalGroundCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(mechanicalGroundCheckBox);

        mechanicalChoppedCheckBox = new CheckBox(this);
        mechanicalChoppedCheckBox.setText("Mechanical Chopped");
        mechanicalChoppedCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(mechanicalChoppedCheckBox);

        biteSizeCheckBox = new CheckBox(this);
        biteSizeCheckBox.setText("Bite Size");
        biteSizeCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(biteSizeCheckBox);

        breadOkCheckBox = new CheckBox(this);
        breadOkCheckBox.setText("Bread OK");
        breadOkCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(breadOkCheckBox);

        extraGravyCheckBox = new CheckBox(this);
        extraGravyCheckBox.setText("Extra Gravy");
        extraGravyCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(extraGravyCheckBox);

        meatsOnlyCheckBox = new CheckBox(this);
        meatsOnlyCheckBox.setText("Meats Only");
        meatsOnlyCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(meatsOnlyCheckBox);

        // Thicken Liquids
        TextView thickenLabel = new TextView(this);
        thickenLabel.setText("Thicken Liquids");
        thickenLabel.setTextSize(18);
        thickenLabel.setTextColor(0xFF2c3e50);
        thickenLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        thickenLabel.setPadding(0, 30, 0, 10);
        mainLayout.addView(thickenLabel);

        nectarThickCheckBox = new CheckBox(this);
        nectarThickCheckBox.setText("Nectar Thick");
        nectarThickCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(nectarThickCheckBox);

        honeyThickCheckBox = new CheckBox(this);
        honeyThickCheckBox.setText("Honey Thick");
        honeyThickCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(honeyThickCheckBox);

        puddingThickCheckBox = new CheckBox(this);
        puddingThickCheckBox.setText("Pudding Thick");
        puddingThickCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(puddingThickCheckBox);

        // Additional Information (NO LIKES/DISLIKES)
        TextView additionalLabel = new TextView(this);
        additionalLabel.setText("Additional Information");
        additionalLabel.setTextSize(18);
        additionalLabel.setTextColor(0xFF2c3e50);
        additionalLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        additionalLabel.setPadding(0, 30, 0, 10);
        mainLayout.addView(additionalLabel);

        // Allergies
        TextView allergiesLabel = new TextView(this);
        allergiesLabel.setText("Allergies");
        allergiesLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(allergiesLabel);

        allergiesInput = new EditText(this);
        allergiesInput.setHint("Enter any allergies");
        allergiesInput.setBackgroundColor(0xFFFFFFFF);
        allergiesInput.setPadding(15, 15, 15, 15);
        allergiesInput.setMinLines(2);
        mainLayout.addView(allergiesInput);

        // Comments
        TextView commentsLabel = new TextView(this);
        commentsLabel.setText("Comments");
        commentsLabel.setTextColor(0xFF2c3e50);
        commentsLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(commentsLabel);

        commentsInput = new EditText(this);
        commentsInput.setHint("Additional comments");
        commentsInput.setBackgroundColor(0xFFFFFFFF);
        commentsInput.setPadding(15, 15, 15, 15);
        commentsInput.setMinLines(3);
        mainLayout.addView(commentsInput);

        // Buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 30, 0, 0);

        saveButton = new Button(this);
        saveButton.setText(isEditMode ? "Update" : "Save");
        saveButton.setBackgroundColor(0xFF2196F3);
        saveButton.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        saveParams.setMargins(0, 0, 10, 0);
        saveButton.setLayoutParams(saveParams);
        buttonLayout.addView(saveButton);

        cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setBackgroundColor(0xFF757575);
        cancelButton.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        cancelButton.setLayoutParams(cancelParams);
        buttonLayout.addView(cancelButton);

        mainLayout.addView(buttonLayout);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void setupSpinners() {
        // Wing Spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

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

        // Save button
        saveButton.setOnClickListener(v -> savePatient());

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

        if (wingSpinner.getSelectedItem() == null || roomSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select wing and room", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dietSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a diet type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create or update patient
        Patient patient = isEditMode ? patientDAO.getPatientById(editPatientId) : new Patient();

        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setWing(wingSpinner.getSelectedItem().toString());
        patient.setRoomNumber(roomSpinner.getSelectedItem().toString());
        patient.setDietType(dietSpinner.getSelectedItem().toString());

        // Set ADA diet if applicable
        if (adaDietCheckBox.getVisibility() == View.VISIBLE && adaDietCheckBox.isChecked()) {
            patient.setAdaDiet(true);
        } else {
            patient.setAdaDiet(false);
        }

        // Set fluid restriction
        String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();
        if (!fluidRestriction.equals("No Restriction")) {
            patient.setFluidRestriction(fluidRestriction);
        } else {
            patient.setFluidRestriction("");
        }

        // Set texture modifications
        patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        patient.setBiteSize(biteSizeCheckBox.isChecked());
        patient.setBreadOK(breadOkCheckBox.isChecked());
        patient.setExtraGravy(extraGravyCheckBox.isChecked());
        patient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

        // Set thicken liquids
        patient.setNectarThick(nectarThickCheckBox.isChecked());
        patient.setHoneyThick(honeyThickCheckBox.isChecked());
        patient.setPuddingThick(puddingThickCheckBox.isChecked());

        // Set additional info
        patient.setAllergies(allergiesInput.getText().toString().trim());
        patient.setComments(commentsInput.getText().toString().trim());

        // Save to database
        boolean success;
        if (isEditMode) {
            success = patientDAO.updatePatient(patient);
        } else {
            long result = patientDAO.insertPatient(patient);
            success = result > 0;
        }

        if (success) {
            Toast.makeText(this,
                    isEditMode ? "Patient updated successfully" : "Patient added successfully",
                    Toast.LENGTH_SHORT).show();

            // Return to patient list
            Intent intent = new Intent(this, ExistingPatientsActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error saving patient", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPatientData() {
        Patient patient = patientDAO.getPatientById(editPatientId);
        if (patient == null) {
            Toast.makeText(this, "Error: Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate basic info
        firstNameInput.setText(patient.getFirstName());
        lastNameInput.setText(patient.getLastName());

        // Set wing
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(patient.getWing())) {
                wingSpinner.setSelection(i);
                break;
            }
        }

        // Wait for wing spinner to update room spinner, then set room
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

        // Set diet type
        ArrayAdapter<String> dietAdapter = (ArrayAdapter<String>) dietSpinner.getAdapter();
        for (int i = 0; i < dietAdapter.getCount(); i++) {
            if (dietAdapter.getItem(i).equals(patient.getDietType())) {
                dietSpinner.setSelection(i);
                break;
            }
        }

        // Set ADA diet
        adaDietCheckBox.setChecked(patient.isAdaDiet());

        // Set fluid restriction
        if (patient.getFluidRestriction() != null && !patient.getFluidRestriction().isEmpty()) {
            ArrayAdapter<String> fluidAdapter = (ArrayAdapter<String>) fluidRestrictionSpinner.getAdapter();
            for (int i = 0; i < fluidAdapter.getCount(); i++) {
                if (fluidAdapter.getItem(i).equals(patient.getFluidRestriction())) {
                    fluidRestrictionSpinner.setSelection(i);
                    break;
                }
            }
        }

        // Set texture modifications
        mechanicalGroundCheckBox.setChecked(patient.isMechanicalGround());
        mechanicalChoppedCheckBox.setChecked(patient.isMechanicalChopped());
        biteSizeCheckBox.setChecked(patient.isBiteSize());
        breadOkCheckBox.setChecked(patient.isBreadOK());
        extraGravyCheckBox.setChecked(patient.isExtraGravy());
        meatsOnlyCheckBox.setChecked(patient.isMeatsOnly());

        // Set thicken liquids
        nectarThickCheckBox.setChecked(patient.isNectarThick());
        honeyThickCheckBox.setChecked(patient.isHoneyThick());
        puddingThickCheckBox.setChecked(patient.isPuddingThick());

        // Set additional info
        if (patient.getAllergies() != null) allergiesInput.setText(patient.getAllergies());
        if (patient.getComments() != null) commentsInput.setText(patient.getComments());
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