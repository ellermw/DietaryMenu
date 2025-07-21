package com.hospital.dietary;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.dao.DefaultMenuDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class NewPatientActivity extends AppCompatActivity {

    private static final String TAG = "NewPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private DefaultMenuDAO defaultMenuDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private EditText patientFirstNameEditText;
    private EditText patientLastNameEditText;
    private Spinner wingSpinner;
    private Spinner roomNumberSpinner;
    private Spinner dietSpinner;
    private CheckBox adaToggleCheckBox;
    private LinearLayout adaToggleContainer;
    private Spinner fluidRestrictionSpinner;

    // Texture modification checkboxes
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOKCheckBox;
    private CheckBox nectarThickCheckBox;
    private CheckBox puddingThickCheckBox;
    private CheckBox honeyThickCheckBox;
    private CheckBox extraGravyCheckBox;
    private CheckBox meatsOnlyCheckBox;
    private LinearLayout meatsOnlyContainer;

    private Button savePatientButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

        Log.d(TAG, "NewPatientActivity onCreate started");

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        defaultMenuDAO = new DefaultMenuDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("New Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components
        initializeViews();
        setupSpinners();
        setupListeners();
        setupTextureModificationLogic();
    }

    private void initializeViews() {
        // Basic Information
        patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
        patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);

        // Dietary Information
        dietSpinner = findViewById(R.id.dietSpinner);
        adaToggleCheckBox = findViewById(R.id.adaToggleCheckBox);
        adaToggleContainer = findViewById(R.id.adaToggleContainer);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

        // Texture Modifications
        mechanicalChoppedCheckBox = findViewById(R.id.mechanicalChoppedCheckBox);
        mechanicalGroundCheckBox = findViewById(R.id.mechanicalGroundCheckBox);
        biteSizeCheckBox = findViewById(R.id.biteSizeCheckBox);
        breadOKCheckBox = findViewById(R.id.breadOKCheckBox);
        nectarThickCheckBox = findViewById(R.id.nectarThickCheckBox);
        puddingThickCheckBox = findViewById(R.id.puddingThickCheckBox);
        honeyThickCheckBox = findViewById(R.id.honeyThickCheckBox);
        extraGravyCheckBox = findViewById(R.id.extraGravyCheckBox);
        meatsOnlyCheckBox = findViewById(R.id.meatsOnlyCheckBox);
        meatsOnlyContainer = findViewById(R.id.meatsOnlyContainer);

        savePatientButton = findViewById(R.id.savePatientButton);
    }

    private void setupSpinners() {
        // Wing Spinner
        String[] wings = {"Select Wing", "1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Room Number Spinner (dynamic based on wing) - Initialize with empty list
        updateRoomNumbers();

        // Diet Spinner - FIXED: Removed Mechanical Chopped and Mechanical Ground
        String[] diets = {"Select Diet", "Regular", "ADA", "Cardiac", "Renal", "Clear Liquid", "Full Liquid", "Puree"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid Restriction Spinner
        String[] fluidRestrictions = {
                "No Fluid Restriction",
                "1000ml (34oz): 120ml, 120ml, 160ml",
                "1200ml (41oz): 250ml, 170ml, 180ml",
                "1500ml (51oz): 350ml, 170ml, 180ml",
                "1800ml (61oz): 360ml, 240ml, 240ml",
                "2000ml (68oz): 320ml, 240ml, 240ml",
                "2500ml (85oz): 400ml, 400ml, 400ml"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void updateRoomNumbers() {
        // Get selected wing
        String selectedWing = (String) wingSpinner.getSelectedItem();
        String[] roomNumbers;

        // Determine room numbers based on wing
        if ("Select Wing".equals(selectedWing) || selectedWing == null) {
            roomNumbers = new String[]{"Select Room"};
        } else {
            // Create room numbers based on wing
            List<String> rooms = new ArrayList<>();
            rooms.add("Select Room");

            // Different room ranges for different wings
            switch (selectedWing) {
                case "ICU":
                    for (int i = 1; i <= 10; i++) {
                        rooms.add("ICU-" + i);
                    }
                    break;
                case "Labor and Delivery":
                    for (int i = 1; i <= 15; i++) {
                        rooms.add("LD-" + i);
                    }
                    break;
                default:
                    // Standard rooms for other wings
                    for (int i = 101; i <= 120; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
            }
            roomNumbers = rooms.toArray(new String[0]);
        }

        // Update room spinner
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomNumbers);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNumberSpinner.setAdapter(roomAdapter);
    }

    private void setupListeners() {
        // Wing spinner listener - update room numbers when wing changes
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomNumbers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Diet spinner listener - show/hide ADA toggle
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getSelectedItem();
                if ("ADA".equals(selectedDiet)) {
                    adaToggleContainer.setVisibility(View.VISIBLE);
                } else {
                    adaToggleContainer.setVisibility(View.GONE);
                    adaToggleCheckBox.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                adaToggleContainer.setVisibility(View.GONE);
            }
        });

        // Save button listener
        savePatientButton.setOnClickListener(v -> savePatient());

        // Add text watchers for validation
        patientFirstNameEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                patientFirstNameEditText.setError(null);
            }
        });

        patientLastNameEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                patientLastNameEditText.setError(null);
            }
        });
    }

    private void setupTextureModificationLogic() {
        // Show/hide meats only option based on mechanical selections
        CompoundButton.OnCheckedChangeListener mechanicalListener = (buttonView, isChecked) -> {
            boolean showMeatsOnly = mechanicalChoppedCheckBox.isChecked() || mechanicalGroundCheckBox.isChecked();
            meatsOnlyContainer.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);
            if (!showMeatsOnly) {
                meatsOnlyCheckBox.setChecked(false);
            }
        };

        mechanicalChoppedCheckBox.setOnCheckedChangeListener(mechanicalListener);
        mechanicalGroundCheckBox.setOnCheckedChangeListener(mechanicalListener);
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate first name
        String firstName = patientFirstNameEditText.getText().toString().trim();
        if (firstName.isEmpty()) {
            patientFirstNameEditText.setError("First name is required");
            isValid = false;
        }

        // Validate last name
        String lastName = patientLastNameEditText.getText().toString().trim();
        if (lastName.isEmpty()) {
            patientLastNameEditText.setError("Last name is required");
            isValid = false;
        }

        // Validate wing selection
        if (wingSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate room selection
        if (roomNumberSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a room number", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate diet selection
        if (dietSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a diet type", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void savePatient() {
        if (!validateForm()) {
            return;
        }

        // Create new patient object
        Patient patient = new Patient();
        patient.setPatientFirstName(patientFirstNameEditText.getText().toString().trim());
        patient.setPatientLastName(patientLastNameEditText.getText().toString().trim());
        patient.setWing((String) wingSpinner.getSelectedItem());
        patient.setRoomNumber((String) roomNumberSpinner.getSelectedItem());

        // Set diet information
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        patient.setDietType(selectedDiet);
        patient.setDiet(selectedDiet);
        patient.setAdaDiet("ADA".equals(selectedDiet) && adaToggleCheckBox.isChecked());

        // Set fluid restriction
        patient.setFluidRestriction((String) fluidRestrictionSpinner.getSelectedItem());

        // Set texture modifications
        patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        patient.setBiteSize(biteSizeCheckBox.isChecked());
        patient.setBreadOK(breadOKCheckBox.isChecked());
        patient.setNectarThick(nectarThickCheckBox.isChecked());
        patient.setPuddingThick(puddingThickCheckBox.isChecked());
        patient.setHoneyThick(honeyThickCheckBox.isChecked());
        patient.setExtraGravy(extraGravyCheckBox.isChecked());
        patient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

        // Build texture modifications string
        List<String> textureMods = new ArrayList<>();
        if (patient.isMechanicalChopped()) textureMods.add("Mechanical Chopped");
        if (patient.isMechanicalGround()) textureMods.add("Mechanical Ground");
        if (patient.isBiteSize()) textureMods.add("Bite Size");
        if (!patient.isBreadOK()) textureMods.add("No Bread");
        if (patient.isNectarThick()) textureMods.add("Nectar Thick");
        if (patient.isPuddingThick()) textureMods.add("Pudding Thick");
        if (patient.isHoneyThick()) textureMods.add("Honey Thick");
        if (patient.isExtraGravy()) textureMods.add("Extra Gravy");
        if (patient.isMeatsOnly()) textureMods.add("Meats Only");

        patient.setTextureModifications(textureMods.isEmpty() ? "Regular" : String.join(", ", textureMods));

        // Set created date
        patient.setCreatedDate(new Date());

        // Save to database
        try {
            long result = patientDAO.addPatient(patient);
            if (result > 0) {
                Toast.makeText(this, "Patient added successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error adding patient. Please try again.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving patient", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            case R.id.action_clear:
                clearForm();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearForm() {
        patientFirstNameEditText.setText("");
        patientLastNameEditText.setText("");
        wingSpinner.setSelection(0);
        roomNumberSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        adaToggleCheckBox.setChecked(false);
        fluidRestrictionSpinner.setSelection(0);

        // Clear all texture modifications
        mechanicalChoppedCheckBox.setChecked(false);
        mechanicalGroundCheckBox.setChecked(false);
        biteSizeCheckBox.setChecked(false);
        breadOKCheckBox.setChecked(true);
        nectarThickCheckBox.setChecked(false);
        puddingThickCheckBox.setChecked(false);
        honeyThickCheckBox.setChecked(false);
        extraGravyCheckBox.setChecked(false);
        meatsOnlyCheckBox.setChecked(false);

        // Reset focus
        patientFirstNameEditText.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    }
}