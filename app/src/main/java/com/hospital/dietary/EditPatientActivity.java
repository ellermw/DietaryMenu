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

public class EditPatientActivity extends AppCompatActivity {

    private static final String TAG = "EditPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private Patient currentPatient;
    private long patientId;

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

    // Additional
    private EditText allergiesInput;
    private EditText commentsInput;

    private Button updatePatientButton;
    private Button cancelButton;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap;
    private String[] wings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get patient ID from intent
        patientId = getIntent().getLongExtra("patient_id", -1);
        if (patientId == -1) {
            // Try getting it as int for backward compatibility
            int intPatientId = getIntent().getIntExtra("patient_id", -1);
            if (intPatientId != -1) {
                patientId = intPatientId;
            } else {
                Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        // Initialize room mapping
        initializeRoomMapping();

        // Create the edit form layout programmatically
        createEditForm();

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupSpinners();
        setupListeners();
        loadPatientData();
    }

    private void createEditForm() {
        // Create the same form layout as NewPatientActivity
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(0xFFF8F9FA);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);

        // Patient Information Section
        TextView infoLabel = new TextView(this);
        infoLabel.setText("Patient Information");
        infoLabel.setTextSize(20);
        infoLabel.setTextColor(0xFF2c3e50);
        infoLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        infoLabel.setPadding(0, 0, 0, 20);
        mainLayout.addView(infoLabel);

        // First Name
        TextView firstNameLabel = new TextView(this);
        firstNameLabel.setText("First Name *");
        firstNameLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(firstNameLabel);

        firstNameInput = new EditText(this);
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
        lastNameInput.setBackgroundColor(0xFFFFFFFF);
        lastNameInput.setPadding(15, 15, 15, 15);
        mainLayout.addView(lastNameInput);

        // Location Section
        TextView locationLabel = new TextView(this);
        locationLabel.setText("Location");
        locationLabel.setTextSize(20);
        locationLabel.setTextColor(0xFF2c3e50);
        locationLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        locationLabel.setPadding(0, 30, 0, 20);
        mainLayout.addView(locationLabel);

        // Wing
        TextView wingLabel = new TextView(this);
        wingLabel.setText("Wing *");
        wingLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(wingLabel);

        wingSpinner = new Spinner(this);
        wingSpinner.setBackgroundColor(0xFFFFFFFF);
        wingSpinner.setPadding(15, 15, 15, 15);
        wingSpinner.setMinimumHeight(120);
        mainLayout.addView(wingSpinner);

        // Room Number
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

        // Dietary Information Section
        TextView dietaryLabel = new TextView(this);
        dietaryLabel.setText("Dietary Information");
        dietaryLabel.setTextSize(20);
        dietaryLabel.setTextColor(0xFF2c3e50);
        dietaryLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        dietaryLabel.setPadding(0, 30, 0, 20);
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

        // ADA Diet
        adaDietCheckBox = new CheckBox(this);
        adaDietCheckBox.setText("ADA Diet (Diabetic)");
        adaDietCheckBox.setTextColor(0xFF2c3e50);
        adaDietCheckBox.setPadding(0, 15, 0, 0);
        adaDietCheckBox.setVisibility(View.GONE);
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

        // Texture Modifications and Thicken Liquids Container
        LinearLayout textureThickenContainer = new LinearLayout(this);
        textureThickenContainer.setOrientation(LinearLayout.HORIZONTAL);
        textureThickenContainer.setPadding(0, 30, 0, 10);
        mainLayout.addView(textureThickenContainer);

        // Left column - Texture Modifications
        LinearLayout textureColumn = new LinearLayout(this);
        textureColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textureParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        textureParams.setMargins(0, 0, 20, 0);
        textureColumn.setLayoutParams(textureParams);

        TextView textureLabel = new TextView(this);
        textureLabel.setText("Texture Modifications");
        textureLabel.setTextSize(18);
        textureLabel.setTextColor(0xFF2c3e50);
        textureLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        textureLabel.setPadding(0, 0, 0, 10);
        textureColumn.addView(textureLabel);

        // Texture checkboxes
        mechanicalGroundCheckBox = new CheckBox(this);
        mechanicalGroundCheckBox.setText("Mechanical Ground");
        mechanicalGroundCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(mechanicalGroundCheckBox);

        mechanicalChoppedCheckBox = new CheckBox(this);
        mechanicalChoppedCheckBox.setText("Mechanical Chopped");
        mechanicalChoppedCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(mechanicalChoppedCheckBox);

        biteSizeCheckBox = new CheckBox(this);
        biteSizeCheckBox.setText("Bite Size");
        biteSizeCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(biteSizeCheckBox);

        breadOkCheckBox = new CheckBox(this);
        breadOkCheckBox.setText("Bread OK");
        breadOkCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(breadOkCheckBox);

        extraGravyCheckBox = new CheckBox(this);
        extraGravyCheckBox.setText("Extra Gravy/Sauce");
        extraGravyCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(extraGravyCheckBox);

        meatsOnlyCheckBox = new CheckBox(this);
        meatsOnlyCheckBox.setText("Meats Only");
        meatsOnlyCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(meatsOnlyCheckBox);

        textureThickenContainer.addView(textureColumn);

        // Right column - Thicken Liquids
        LinearLayout thickenColumn = new LinearLayout(this);
        thickenColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams thickenParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        thickenColumn.setLayoutParams(thickenParams);

        TextView thickenLabel = new TextView(this);
        thickenLabel.setText("Thicken Liquids");
        thickenLabel.setTextSize(18);
        thickenLabel.setTextColor(0xFF2c3e50);
        thickenLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        thickenLabel.setPadding(0, 0, 0, 10);
        thickenColumn.addView(thickenLabel);

        nectarThickCheckBox = new CheckBox(this);
        nectarThickCheckBox.setText("Nectar Thick");
        nectarThickCheckBox.setTextColor(0xFF2c3e50);
        thickenColumn.addView(nectarThickCheckBox);

        honeyThickCheckBox = new CheckBox(this);
        honeyThickCheckBox.setText("Honey Thick");
        honeyThickCheckBox.setTextColor(0xFF2c3e50);
        thickenColumn.addView(honeyThickCheckBox);

        puddingThickCheckBox = new CheckBox(this);
        puddingThickCheckBox.setText("Pudding Thick");
        puddingThickCheckBox.setTextColor(0xFF2c3e50);
        thickenColumn.addView(puddingThickCheckBox);

        textureThickenContainer.addView(thickenColumn);

        // Additional Information
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
        allergiesInput.setBackgroundColor(0xFFFFFFFF);
        allergiesInput.setPadding(15, 15, 15, 15);
        allergiesInput.setHint("Enter any allergies...");
        mainLayout.addView(allergiesInput);

        // Comments
        TextView commentsLabel = new TextView(this);
        commentsLabel.setText("Comments");
        commentsLabel.setTextColor(0xFF2c3e50);
        commentsLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(commentsLabel);

        commentsInput = new EditText(this);
        commentsInput.setBackgroundColor(0xFFFFFFFF);
        commentsInput.setPadding(15, 15, 15, 15);
        commentsInput.setHint("Enter any comments...");
        commentsInput.setMinLines(3);
        mainLayout.addView(commentsInput);

        // Buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 40, 0, 0);

        updatePatientButton = new Button(this);
        updatePatientButton.setText("Update");
        updatePatientButton.setBackgroundColor(0xFF2196F3);
        updatePatientButton.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams updateParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        updateParams.setMargins(0, 0, 10, 0);
        updatePatientButton.setLayoutParams(updateParams);
        updatePatientButton.setPadding(15, 15, 15, 15);
        buttonLayout.addView(updatePatientButton);

        cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setBackgroundColor(0xFF757575);
        cancelButton.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        cancelParams.setMargins(10, 0, 0, 0);
        cancelButton.setLayoutParams(cancelParams);
        cancelButton.setPadding(15, 15, 15, 15);
        buttonLayout.addView(cancelButton);

        mainLayout.addView(buttonLayout);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void initializeRoomMapping() {
        wingRoomMap = new HashMap<>();

        // 1 South - Rooms 106 through 122
        String[] south1Rooms = new String[17];
        for (int i = 0; i < 17; i++) {
            south1Rooms[i] = String.valueOf(106 + i);
        }
        wingRoomMap.put("1 South", south1Rooms);

        // 2 North - Rooms 250 through 264
        String[] north2Rooms = new String[15];
        for (int i = 0; i < 15; i++) {
            north2Rooms[i] = String.valueOf(250 + i);
        }
        wingRoomMap.put("2 North", north2Rooms);

        // Labor and Delivery - LDR1 through LDR6
        String[] ldRooms = new String[6];
        for (int i = 0; i < 6; i++) {
            ldRooms[i] = "LDR" + (i + 1);
        }
        wingRoomMap.put("Labor and Delivery", ldRooms);

        // 2 West - Rooms 225 through 248
        String[] west2Rooms = new String[24];
        for (int i = 0; i < 24; i++) {
            west2Rooms[i] = String.valueOf(225 + i);
        }
        wingRoomMap.put("2 West", west2Rooms);

        // 3 North - Rooms 349 through 471
        String[] north3Rooms = new String[123];
        for (int i = 0; i < 123; i++) {
            north3Rooms[i] = String.valueOf(349 + i);
        }
        wingRoomMap.put("3 North", north3Rooms);

        // ICU - ICU1 through ICU13
        String[] icuRooms = new String[13];
        for (int i = 0; i < 13; i++) {
            icuRooms[i] = "ICU" + (i + 1);
        }
        wingRoomMap.put("ICU", icuRooms);

        // Create wings array for spinner in specific order
        wings = new String[]{"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
    }

    private void setupSpinners() {
        // Setup wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Setup room spinner based on selected wing
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                String[] rooms = wingRoomMap.get(selectedWing);
                ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(EditPatientActivity.this,
                        android.R.layout.simple_spinner_item, rooms);
                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roomSpinner.setAdapter(roomAdapter);

                // If loading patient data, restore room selection
                if (currentPatient != null && selectedWing.equals(currentPatient.getWing())) {
                    for (int i = 0; i < rooms.length; i++) {
                        if (rooms[i].equals(currentPatient.getRoomNumber())) {
                            roomSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup diet spinner - Updated diet types
        String[] dietTypes = {
                "Regular",
                "Cardiac",
                "ADA",
                "Renal",
                "Puree",
                "Full Liquid",
                "Clear Liquid"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Setup diet change listener for ADA checkbox
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getItemAtPosition(position);
                boolean showAdaOption = "Clear Liquid".equals(selectedDiet) ||
                        "Full Liquid".equals(selectedDiet) ||
                        "Puree".equals(selectedDiet);
                adaDietCheckBox.setVisibility(showAdaOption ? View.VISIBLE : View.GONE);

                // For ADA diet, automatically check the ADA checkbox
                if ("ADA".equals(selectedDiet)) {
                    adaDietCheckBox.setChecked(true);
                    adaDietCheckBox.setVisibility(View.VISIBLE);
                    adaDietCheckBox.setEnabled(false); // Don't allow unchecking for ADA diet
                } else {
                    adaDietCheckBox.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup fluid restriction spinner
        String[] fluidRestrictions = {
                "No Restriction", "1000ml", "1500ml", "2000ml", "As Ordered"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void setupListeners() {
        updatePatientButton.setOnClickListener(v -> savePatient());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadPatientData() {
        currentPatient = patientDAO.getPatientById((int) patientId);
        if (currentPatient == null) {
            Toast.makeText(this, "Error: Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate basic info
        firstNameInput.setText(currentPatient.getFirstName());
        lastNameInput.setText(currentPatient.getLastName());

        // Set wing
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(currentPatient.getWing())) {
                wingSpinner.setSelection(i);
                break;
            }
        }

        // Set diet type
        ArrayAdapter<String> dietAdapter = (ArrayAdapter<String>) dietSpinner.getAdapter();
        for (int i = 0; i < dietAdapter.getCount(); i++) {
            if (dietAdapter.getItem(i).equals(currentPatient.getDietType())) {
                dietSpinner.setSelection(i);
                break;
            }
        }

        // Set ADA diet
        adaDietCheckBox.setChecked(currentPatient.isAdaDiet());

        // Set fluid restriction
        if (currentPatient.getFluidRestriction() != null && !currentPatient.getFluidRestriction().isEmpty()) {
            ArrayAdapter<String> fluidAdapter = (ArrayAdapter<String>) fluidRestrictionSpinner.getAdapter();
            for (int i = 0; i < fluidAdapter.getCount(); i++) {
                if (fluidAdapter.getItem(i).equals(currentPatient.getFluidRestriction())) {
                    fluidRestrictionSpinner.setSelection(i);
                    break;
                }
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

        // Set additional info
        if (currentPatient.getAllergies() != null) {
            allergiesInput.setText(currentPatient.getAllergies());
        }
        if (currentPatient.getComments() != null) {
            commentsInput.setText(currentPatient.getComments());
        }
    }

    private void savePatient() {
        // Get values from form
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String roomNumber = (String) roomSpinner.getSelectedItem();
        String dietType = (String) dietSpinner.getSelectedItem();
        boolean isAdaDiet = adaDietCheckBox.isChecked() || "ADA".equals(dietType);
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        String allergies = allergiesInput.getText().toString().trim();
        String comments = commentsInput.getText().toString().trim();

        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please enter both first and last name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update patient object
        currentPatient.setFirstName(firstName);
        currentPatient.setLastName(lastName);
        currentPatient.setWing(wing);
        currentPatient.setRoomNumber(roomNumber);
        currentPatient.setDietType(dietType);
        currentPatient.setAdaDiet(isAdaDiet);
        currentPatient.setFluidRestriction(fluidRestriction);
        currentPatient.setAllergies(allergies);
        currentPatient.setComments(comments);

        // Set texture modifications
        currentPatient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        currentPatient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        currentPatient.setBiteSize(biteSizeCheckBox.isChecked());
        currentPatient.setBreadOK(breadOkCheckBox.isChecked());
        currentPatient.setExtraGravy(extraGravyCheckBox.isChecked());
        currentPatient.setMeatsOnly(meatsOnlyCheckBox.isChecked());
        currentPatient.setNectarThick(nectarThickCheckBox.isChecked());
        currentPatient.setHoneyThick(honeyThickCheckBox.isChecked());
        currentPatient.setPuddingThick(puddingThickCheckBox.isChecked());

        // Build texture modifications string
        StringBuilder textureModifications = new StringBuilder();
        if (mechanicalGroundCheckBox.isChecked()) textureModifications.append("Mechanical Ground, ");
        if (mechanicalChoppedCheckBox.isChecked()) textureModifications.append("Mechanical Chopped, ");
        if (biteSizeCheckBox.isChecked()) textureModifications.append("Bite Size, ");
        if (breadOkCheckBox.isChecked()) textureModifications.append("Bread OK, ");
        if (extraGravyCheckBox.isChecked()) textureModifications.append("Extra Gravy/Sauce, ");
        if (meatsOnlyCheckBox.isChecked()) textureModifications.append("Meats Only, ");
        if (nectarThickCheckBox.isChecked()) textureModifications.append("Nectar Thick, ");
        if (honeyThickCheckBox.isChecked()) textureModifications.append("Honey Thick, ");
        if (puddingThickCheckBox.isChecked()) textureModifications.append("Pudding Thick, ");

        // Remove trailing comma
        if (textureModifications.length() > 2) {
            textureModifications.setLength(textureModifications.length() - 2);
        }
        currentPatient.setTextureModifications(textureModifications.toString());

        // Save to database
        boolean success = patientDAO.updatePatient(currentPatient);

        if (success) {
            Toast.makeText(this, "Patient updated successfully", Toast.LENGTH_SHORT).show();

            // Set result and finish
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_patient_id", patientId);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Error updating patient", Toast.LENGTH_SHORT).show();
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