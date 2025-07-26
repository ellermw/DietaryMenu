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

        // Discharge status
        dischargedCheckBox = findViewById(R.id.dischargedCheckBox);

        // Action Buttons
        updatePatientButton = findViewById(R.id.updatePatientButton);
        deletePatientButton = findViewById(R.id.deletePatientButton);
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
                ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(EditPatientActivity.this,
                        R.layout.spinner_item, rooms);
                roomAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
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

    private void loadPatientData() {
        currentPatient = patientDAO.getPatientById(patientId);
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
        if (currentPatient.getAllergies() != null) allergiesInput.setText(currentPatient.getAllergies());
        if (currentPatient.getLikes() != null) likesInput.setText(currentPatient.getLikes());
        if (currentPatient.getDislikes() != null) dislikesInput.setText(currentPatient.getDislikes());
        if (currentPatient.getComments() != null) commentsInput.setText(currentPatient.getComments());

        // Set discharge status
        dischargedCheckBox.setChecked(currentPatient.isDischarged());
    }

    private void setupListeners() {
        // Update button
        updatePatientButton.setOnClickListener(v -> updatePatient());

        // Delete button
        deletePatientButton.setOnClickListener(v -> confirmDeletePatient());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void updatePatient() {
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

        // Update patient object
        currentPatient.setFirstName(firstName);
        currentPatient.setLastName(lastName);
        currentPatient.setWing(wingSpinner.getSelectedItem().toString());
        currentPatient.setRoomNumber(roomSpinner.getSelectedItem().toString());
        currentPatient.setDietType(dietSpinner.getSelectedItem().toString());

        // Set ADA diet if applicable
        if (adaDietCheckBox.getVisibility() == View.VISIBLE && adaDietCheckBox.isChecked()) {
            currentPatient.setAdaDiet(true);
        } else {
            currentPatient.setAdaDiet(false);
        }

        // Set fluid restriction
        String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();
        if (!fluidRestriction.equals("None")) {
            currentPatient.setFluidRestriction(fluidRestriction);
        } else {
            currentPatient.setFluidRestriction("");
        }

        // Set texture modifications
        currentPatient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        currentPatient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        currentPatient.setBiteSize(biteSizeCheckBox.isChecked());
        currentPatient.setBreadOK(breadOkCheckBox.isChecked());
        currentPatient.setExtraGravy(extraGravyCheckBox.isChecked());
        currentPatient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

        // Set thicken liquids
        currentPatient.setNectarThick(nectarThickCheckBox.isChecked());
        currentPatient.setHoneyThick(honeyThickCheckBox.isChecked());
        currentPatient.setPuddingThick(puddingThickCheckBox.isChecked());

        // Set additional info
        currentPatient.setAllergies(allergiesInput.getText().toString().trim());
        currentPatient.setLikes(likesInput.getText().toString().trim());
        currentPatient.setDislikes(dislikesInput.getText().toString().trim());
        currentPatient.setComments(commentsInput.getText().toString().trim());

        // Set discharge status
        currentPatient.setDischarged(dischargedCheckBox.isChecked());


        // Update in database
        boolean success = patientDAO.updatePatient(currentPatient);

        if (success) {
            Toast.makeText(this, "Patient updated successfully", Toast.LENGTH_SHORT).show();

            // Return to patient detail
            Intent intent = new Intent(this, PatientDetailActivity.class);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error updating patient", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeletePatient() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete this patient? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient() {
        boolean success = patientDAO.deletePatient(patientId);

        if (success) {
            Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();

            // Return to patient list
            Intent intent = new Intent(this, ExistingPatientsActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error deleting patient", Toast.LENGTH_SHORT).show();
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
}