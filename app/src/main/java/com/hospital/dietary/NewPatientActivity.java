package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewPatientActivity extends AppCompatActivity {

    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // UI Components - only using IDs that exist in layout
    private EditText patientFirstNameEditText;
    private EditText patientLastNameEditText;
    private Spinner wingSpinner;
    private Spinner roomNumberSpinner;
    private Spinner dietSpinner;
    private Spinner fluidRestrictionSpinner;

    // Optional texture modification checkboxes (may not exist in layout)
    private CheckBox mechanicalGroundCB;
    private CheckBox mechanicalChoppedCB;
    private CheckBox biteSizeCB;
    private CheckBox breadOKCB;

    // Optional buttons (may not exist in layout)
    private Button savePatientButton;
    private Button backButton;

    // Data arrays for spinners
    private String[] wings = {"SELECT FLOOR", "1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
    private String[] diets = {"SELECT DIET", "Regular", "Cardiac", "ADA", "Puree", "Renal", "Full Liquid", "Clear Liquid"};
    private String[] fluidRestrictions = {"None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml"};

    // Room mapping for each wing
    private Map<String, String[]> wingRoomMap;
    private ArrayAdapter<String> roomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_new_patient);

            // Get user information from intent
            currentUsername = getIntent().getStringExtra("current_user");
            currentUserRole = getIntent().getStringExtra("user_role");
            currentUserFullName = getIntent().getStringExtra("user_full_name");

            // Initialize database
            dbHelper = new DatabaseHelper(this);
            patientDAO = new PatientDAO(dbHelper);

            // Initialize room mapping
            initializeRoomMapping();

            // Set title and enable up button using default action bar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add New Patient");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // Initialize UI with error handling
            if (initializeUI()) {
                setupSpinners();
                setupListeners();
            } else {
                // If UI initialization fails, show error and return to previous screen
                Toast.makeText(this, "Error loading page. Returning to previous screen.", Toast.LENGTH_LONG).show();
                finish();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading page: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeRoomMapping() {
        wingRoomMap = new HashMap<>();

        // 1 South - Rooms 101-125
        String[] south1Rooms = new String[25];
        for (int i = 5; i < 22; i++) {
            south1Rooms[i] = String.valueOf(101 + i);
        }
        wingRoomMap.put("1 South", south1Rooms);

        // 2 North - Rooms 201-225
        String[] north2Rooms = new String[25];
        for (int i = 49; i < 64; i++) {
            north2Rooms[i] = String.valueOf(201 + i);
        }
        wingRoomMap.put("2 North", north2Rooms);

        // Labor and Delivery - Rooms LD1-LD10
        String[] ldRooms = new String[10];
        for (int i = 0; i < 6; i++) {
            ldRooms[i] = "LDR" + (i + 1);
        }
        wingRoomMap.put("Labor and Delivery", ldRooms);

        // 2 West - Rooms 226-250
        String[] west2Rooms = new String[25];
        for (int i = 24; i < 48; i++) {
            west2Rooms[i] = String.valueOf(226 + i);
        }
        wingRoomMap.put("2 West", west2Rooms);

        // 3 North - Rooms 301-325
        String[] north3Rooms = new String[25];
        for (int i = 48; i < 71; i++) {
            north3Rooms[i] = String.valueOf(301 + i);
        }
        wingRoomMap.put("3 North", north3Rooms);

        // ICU - ICU1 through ICU13
        String[] icuRooms = new String[13];
        for (int i = 0; i < 13; i++) {
            icuRooms[i] = "ICU" + (i + 1);
        }
        wingRoomMap.put("ICU", icuRooms);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_home:
                goToMainMenu();
                return true;
            case R.id.action_refresh:
                clearForm();
                Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private boolean initializeUI() {
        try {
            // Required UI elements (must exist)
            patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
            patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
            wingSpinner = findViewById(R.id.wingSpinner);
            roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
            dietSpinner = findViewById(R.id.dietSpinner);
            fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

            // Check if required elements exist
            if (patientFirstNameEditText == null || patientLastNameEditText == null ||
                    wingSpinner == null || roomNumberSpinner == null ||
                    dietSpinner == null || fluidRestrictionSpinner == null) {
                Toast.makeText(this, "Layout error: Missing required form elements", Toast.LENGTH_LONG).show();
                return false;
            }

            // Optional UI elements (may not exist in layout)
            mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
            mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
            biteSizeCB = findViewById(R.id.biteSizeCB);
            breadOKCB = findViewById(R.id.breadOKCB);
            savePatientButton = findViewById(R.id.savePatientButton);
            backButton = findViewById(R.id.backButton);

            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void setupSpinners() {
        try {
            // Wing spinner
            ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
            wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            wingSpinner.setAdapter(wingAdapter);

            // Room spinner (initially empty)
            roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomNumberSpinner.setAdapter(roomAdapter);

            // Diet spinner
            ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
            dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dietSpinner.setAdapter(dietAdapter);

            // Fluid restriction spinner
            ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
            fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fluidRestrictionSpinner.setAdapter(fluidAdapter);

            // Set initial room options for first wing
            updateRoomDropdown();
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up spinners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        try {
            // Wing selection listener - updates room dropdown
            wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateRoomDropdown();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Save button listener (if button exists)
            if (savePatientButton != null) {
                savePatientButton.setOnClickListener(v -> savePatient());
            } else {
                // If no save button exists, create a simple save method accessible via menu
                Toast.makeText(this, "Use the refresh button in the menu to save", Toast.LENGTH_SHORT).show();
            }

            // Back button listener (if button exists)
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up listeners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRoomDropdown() {
        try {
            String selectedWing = (String) wingSpinner.getSelectedItem();
            if (selectedWing != null && roomAdapter != null) {
                String[] rooms = wingRoomMap.get(selectedWing);

                if (rooms != null) {
                    roomAdapter.clear();
                    roomAdapter.addAll(rooms);
                    roomAdapter.notifyDataSetChanged();
                    if (roomAdapter.getCount() > 0) {
                        roomNumberSpinner.setSelection(0);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating room dropdown: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePatient() {
        try {
            if (!validateForm()) {
                return;
            }

            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();
            String wing = (String) wingSpinner.getSelectedItem();
            String roomNumber = (String) roomNumberSpinner.getSelectedItem();
            String diet = (String) dietSpinner.getSelectedItem();
            String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
            String textureModifications = buildTextureModifications();

            Patient patient = new Patient();
            patient.setPatientFirstName(firstName);
            patient.setPatientLastName(lastName);
            patient.setWing(wing);
            patient.setRoomNumber(roomNumber);
            patient.setDiet(diet);
            patient.setFluidRestriction(fluidRestriction.equals("None") ? null : fluidRestriction);
            patient.setTextureModifications(textureModifications.isEmpty() ? null : textureModifications);
            patient.setCreatedDate(new Date());

            long patientId = patientDAO.addPatient(patient);

            if (patientId > 0) {
                Toast.makeText(this, "Patient " + firstName + " " + lastName + " added successfully!", Toast.LENGTH_LONG).show();
                clearForm();
            } else {
                Toast.makeText(this, "Error adding patient. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving patient: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateForm() {
        if (patientFirstNameEditText.getText().toString().trim().isEmpty()) {
            patientFirstNameEditText.setError("First name is required");
            patientFirstNameEditText.requestFocus();
            return false;
        }

        if (patientLastNameEditText.getText().toString().trim().isEmpty()) {
            patientLastNameEditText.setError("Last name is required");
            patientLastNameEditText.requestFocus();
            return false;
        }

        if (wingSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (roomNumberSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a room number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dietSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a diet type", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String buildTextureModifications() {
        ArrayList<String> modifications = new ArrayList<>();

        // Only check checkboxes if they exist
        if (mechanicalGroundCB != null && mechanicalGroundCB.isChecked()) {
            modifications.add("Mechanical Ground");
        }
        if (mechanicalChoppedCB != null && mechanicalChoppedCB.isChecked()) {
            modifications.add("Mechanical Chopped");
        }
        if (biteSizeCB != null && biteSizeCB.isChecked()) {
            modifications.add("Bite Size");
        }
        if (breadOKCB != null && breadOKCB.isChecked()) {
            modifications.add("Bread OK");
        }

        return String.join(", ", modifications);
    }

    private void clearForm() {
        try {
            patientFirstNameEditText.setText("");
            patientLastNameEditText.setText("");
            wingSpinner.setSelection(0);
            updateRoomDropdown();
            dietSpinner.setSelection(0);
            fluidRestrictionSpinner.setSelection(0);

            // Only clear checkboxes if they exist
            if (mechanicalGroundCB != null) mechanicalGroundCB.setChecked(false);
            if (mechanicalChoppedCB != null) mechanicalChoppedCB.setChecked(false);
            if (biteSizeCB != null) biteSizeCB.setChecked(false);
            if (breadOKCB != null) breadOKCB.setChecked(false);

            // Clear any error messages
            patientFirstNameEditText.setError(null);
            patientLastNameEditText.setError(null);
        } catch (Exception e) {
            Toast.makeText(this, "Error clearing form: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}