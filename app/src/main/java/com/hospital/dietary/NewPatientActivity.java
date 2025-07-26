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
        // 1 South - Rooms 106 through 122
        wingRoomMap.put("1 South", generateRoomNumbers(106, 122));

        // 2 North - Rooms 250 through 264
        wingRoomMap.put("2 North", generateRoomNumbers(250, 264));

        // Labor and Delivery - Rooms LDR1 through LDR6
        wingRoomMap.put("Labor and Delivery", generateLDRRooms(1, 6));

        // 2 West - Rooms 225 through 248
        wingRoomMap.put("2 West", generateRoomNumbers(225, 248));

        // 3 North - Rooms 349 through 371
        wingRoomMap.put("3 North", generateRoomNumbers(349, 371));

        // ICU - Rooms ICU1 through ICU13
        wingRoomMap.put("ICU", generateICURooms(1, 13));
    }

    private String[] generateRoomNumbers(int start, int end) {
        String[] rooms = new String[end - start + 1];
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = String.valueOf(start + i);
        }
        return rooms;
    }

    private String[] generateLDRRooms(int start, int end) {
        String[] rooms = new String[end - start + 1];
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = "LDR" + (start + i);
        }
        return rooms;
    }

    private String[] generateICURooms(int start, int end) {
        String[] rooms = new String[end - start + 1];
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = "ICU" + (start + i);
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
    }

    private void setupSpinners() {
        // Setup wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, wings);
        wingAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Setup room spinner based on selected wing
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                String[] rooms = wingRoomMap.get(selectedWing);
                ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(NewPatientActivity.this,
                        R.layout.spinner_item, rooms);
                roomAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                roomSpinner.setAdapter(roomAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup diet spinner
        String[] dietTypes = {
                "Regular",
                "Low Fat",
                "Cardiac (Low Fat/Low Sodium 2g)",
                "Low Sodium 2g",
                "Low Sodium 4g",
                "Renal",
                "Gluten Free",
                "Low Fiber/Low Residue",
                "High Fiber",
                "Bland",
                "Vegetarian",
                "Lactose Free",
                "Kosher",
                "Low Potassium",
                "Pureed",
                "Full Liquid",
                "Clear Liquid",
                "NPO"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Setup diet change listener for ADA checkbox
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = dietTypes[position];
                // Show ADA checkbox for specific diets
                if (selectedDiet.equals("Regular") || selectedDiet.equals("Low Fat") ||
                        selectedDiet.equals("Cardiac (Low Fat/Low Sodium 2g)") ||
                        selectedDiet.equals("Low Sodium 2g") || selectedDiet.equals("Low Sodium 4g") ||
                        selectedDiet.equals("Renal") || selectedDiet.equals("Gluten Free") ||
                        selectedDiet.equals("Low Fiber/Low Residue") || selectedDiet.equals("High Fiber") ||
                        selectedDiet.equals("Bland") || selectedDiet.equals("Vegetarian") ||
                        selectedDiet.equals("Lactose Free")) {
                    adaDietCheckBox.setVisibility(View.VISIBLE);
                    adaDietLabel.setVisibility(View.VISIBLE);
                } else {
                    adaDietCheckBox.setVisibility(View.GONE);
                    adaDietLabel.setVisibility(View.GONE);
                    adaDietCheckBox.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                adaDietCheckBox.setVisibility(View.GONE);
                adaDietLabel.setVisibility(View.GONE);
            }
        });

        // Setup fluid restriction spinner
        String[] fluidRestrictions = {
                "None",
                "1000ml (34oz)",
                "1200ml (41oz)",
                "1500ml (51oz)",
                "1800ml (61oz)",
                "2000ml (68oz)",
                "2500ml (85oz)"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void setupListeners() {
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

        if (wingSpinner.getSelectedItem() == null || roomSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select wing and room", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dietSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a diet type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create patient object
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setWing(wingSpinner.getSelectedItem().toString());
        patient.setRoomNumber(roomSpinner.getSelectedItem().toString());
        patient.setDietType(dietSpinner.getSelectedItem().toString());

        // Set ADA diet if applicable
        if (adaDietCheckBox.getVisibility() == View.VISIBLE && adaDietCheckBox.isChecked()) {
            patient.setAdaDiet(true);
        }

        // Set fluid restriction
        String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();
        if (!fluidRestriction.equals("None")) {
            patient.setFluidRestriction(fluidRestriction);
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
        patient.setLikes(likesInput.getText().toString().trim());
        patient.setDislikes(dislikesInput.getText().toString().trim());
        patient.setComments(commentsInput.getText().toString().trim());


        // Save to database
        boolean success;
        if (isEditMode) {
            patient.setPatientId(editPatientId);
            success = patientDAO.updatePatient(patient);
        } else {
            long newId = patientDAO.insertPatient(patient);
            success = newId > 0;
        }

        if (success) {
            Toast.makeText(this, isEditMode ? "Patient updated successfully" : "Patient added successfully",
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
        if (patient.getLikes() != null) likesInput.setText(patient.getLikes());
        if (patient.getDislikes() != null) dislikesInput.setText(patient.getDislikes());
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