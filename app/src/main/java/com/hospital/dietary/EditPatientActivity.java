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
import java.util.Map;

public class EditPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private long patientId;
    private Patient currentPatient;

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

    // Discharge status
    private CheckBox dischargedCheckBox;

    // Action Buttons
    private Button updatePatientButton;
    private Button deletePatientButton;
    private Button cancelButton;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap = new HashMap<>();
    private String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};

    // Fluid restriction mapping (ml amounts for breakfast, lunch, dinner)
    private Map<String, int[]> fluidRestrictionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);

        // Get user information and patient ID from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        patientId = getIntent().getLongExtra("patient_id", -1);

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

        // Diet types
        String[] dietTypes = {
                "Regular", "Cardiac", "Diabetic", "Renal", "Low Sodium",
                "Soft", "Clear Liquid", "Full Liquid", "Pureed", "NPO"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid Restrictions with oz equivalents
        String[] fluidRestrictions = {
                "No Restriction",
                "1000ml (34oz)",
                "1200ml (41oz)",
                "1500ml (51oz)",
                "1800ml (61oz)",
                "2000ml (68oz)",
                "2500ml (85oz)",
                "As Ordered"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void loadPatientData() {
        currentPatient = patientDAO.getPatientById((int) patientId);

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
                "Pureed".equals(diet);
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

        // Button listeners
        updatePatientButton.setOnClickListener(v -> updatePatient());
        deletePatientButton.setOnClickListener(v -> confirmDeletePatient());
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

        // Update patient information
        currentPatient.setPatientFirstName(firstName);
        currentPatient.setPatientLastName(lastName);
        currentPatient.setWing((String) wingSpinner.getSelectedItem());
        currentPatient.setRoomNumber((String) roomSpinner.getSelectedItem());

        // Update diet
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        currentPatient.setDietType(selectedDiet);
        currentPatient.setDiet(selectedDiet);
        currentPatient.setAdaDiet(adaDietCheckBox.getVisibility() == View.VISIBLE && adaDietCheckBox.isChecked());

        // Update fluid restriction
        String selectedFluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        currentPatient.setFluidRestriction(selectedFluidRestriction);

        // Set meal-specific fluid limits based on restriction
        if (fluidRestrictionMap.containsKey(selectedFluidRestriction)) {
            int[] limits = fluidRestrictionMap.get(selectedFluidRestriction);
            currentPatient.setBreakfastDrinks("FL:" + limits[0] + "ml");
            currentPatient.setLunchDrinks("FL:" + limits[1] + "ml");
            currentPatient.setDinnerDrinks("FL:" + limits[2] + "ml");
        }

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

        // Update discharged status
        currentPatient.setDischarged(dischargedCheckBox.isChecked());

        // Save to database
        int result = patientDAO.updatePatient(currentPatient);

        if (result > 0) {
            Toast.makeText(this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error updating patient. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeletePatient() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete " +
                        currentPatient.getPatientFirstName() + " " +
                        currentPatient.getPatientLastName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient() {
        int result = patientDAO.deletePatientById((int) patientId);
        if (result > 0) {
            Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}