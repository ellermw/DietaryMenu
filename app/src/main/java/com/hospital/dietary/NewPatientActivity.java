package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewPatientActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components
    private EditText patientFirstNameEditText;
    private EditText patientLastNameEditText;
    private Spinner wingSpinner;
    private Spinner roomNumberSpinner;
    private Spinner dietSpinner;
    private Spinner fluidRestrictionSpinner;
    
    // Texture modification checkboxes
    private CheckBox mechanicalGroundCB;
    private CheckBox mechanicalChoppedCB;
    private CheckBox biteSizeCB;
    private CheckBox breadOKCB;
    
    private Button savePatientButton;
    private Button backButton;
    
    // CORRECTED: Data arrays with proper wings and diets
    private String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
    private String[] diets = {"Regular", "Cardiac", "ADA", "Puree", "Renal", "Full Liquid", "Clear Liquid"};
    private String[] fluidRestrictions = {"None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml"};
    
    // ADDED: Room mapping for each wing
    private Map<String, String[]> wingRoomMap;
    private ArrayAdapter<String> roomAdapter;

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
        
        // Initialize room mapping
        initializeRoomMapping();
        
        // Setup toolbar
        setupToolbar();
        
        // Initialize UI
        initializeUI();
        setupSpinners();
        setupListeners();
        
        setTitle("Add New Patient");
    }
    
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Add New Patient");
            }
        }
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
    
    private void initializeRoomMapping() {
        wingRoomMap = new HashMap<>();
        
        // 1 South - Rooms 106 through 122
        String[] southRooms = new String[17];
        for (int i = 0; i < 17; i++) {
            southRooms[i] = String.valueOf(106 + i);
        }
        wingRoomMap.put("1 South", southRooms);
        
        // 2 North - Rooms 250 through 264
        String[] northRooms = new String[15];
        for (int i = 0; i < 15; i++) {
            northRooms[i] = String.valueOf(250 + i);
        }
        wingRoomMap.put("2 North", northRooms);
        
        // Labor and Delivery - LDR1 through LDR6
        String[] ldrRooms = {"LDR1", "LDR2", "LDR3", "LDR4", "LDR5", "LDR6"};
        wingRoomMap.put("Labor and Delivery", ldrRooms);
        
        // 2 West - Rooms 225 through 248
        String[] westRooms = new String[24];
        for (int i = 0; i < 24; i++) {
            westRooms[i] = String.valueOf(225 + i);
        }
        wingRoomMap.put("2 West", westRooms);
        
        // 3 North - Rooms 348 through 371
        String[] north3Rooms = new String[24];
        for (int i = 0; i < 24; i++) {
            north3Rooms[i] = String.valueOf(348 + i);
        }
        wingRoomMap.put("3 North", north3Rooms);
        
        // ICU - ICU1 through ICU13
        String[] icuRooms = new String[13];
        for (int i = 0; i < 13; i++) {
            icuRooms[i] = "ICU" + (i + 1);
        }
        wingRoomMap.put("ICU", icuRooms);
    }
    
    private void initializeUI() {
        patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
        patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);
        homeButton = findViewById(R.id.homeButton);
    }
    
    private void setupSpinners() {
        // Wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // Room spinner (initially empty)
        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{});
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
    }
    
    private void setupListeners() {
        // Wing selection listener - updates room dropdown
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomDropdown();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        savePatientButton.setOnClickListener(v -> {
            if (validateForm()) {
                checkForDuplicateAndSave();
            }
        });
        
        backButton.setOnClickListener(v -> finish());
        
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }
        
        // Clear error messages when user starts typing
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
    
    private void updateRoomDropdown() {
        String selectedWing = wingSpinner.getSelectedItem().toString();
        String[] rooms = wingRoomMap.get(selectedWing);
        
        if (rooms != null) {
            roomAdapter.clear();
            roomAdapter.addAll(rooms);
            roomAdapter.notifyDataSetChanged();
            roomNumberSpinner.setSelection(0);
        }
    }
    
    private void checkForDuplicateAndSave() {
        String wing = wingSpinner.getSelectedItem().toString();
        String roomNumber = roomNumberSpinner.getSelectedItem() != null ? 
                          roomNumberSpinner.getSelectedItem().toString() : "";
        
        if (roomNumber.isEmpty()) {
            Toast.makeText(this, "Please select a room number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check for existing patient in this room today
        Patient existingPatient = patientDAO.getPatientByRoomToday(wing, roomNumber);
        
        if (existingPatient != null) {
            // Show confirmation dialog for overwrite
            new AlertDialog.Builder(this)
                .setTitle("Room Already Occupied")
                .setMessage("Room " + roomNumber + " in " + wing + " already has a patient:\n\n" +
                           existingPatient.getPatientFirstName() + " " + existingPatient.getPatientLastName() + 
                           "\n\nDo you want to replace this patient with the new patient?")
                .setPositiveButton("Replace Patient", (dialog, which) -> {
                    // Delete existing patient and save new one
                    patientDAO.deletePatient(existingPatient.getPatientId());
                    savePatient();
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Choose Different Room", (dialog, which) -> {
                    // Just close dialog, let user pick different room
                })
                .show();
        } else {
            // No duplicate, proceed with save
            savePatient();
        }
    }
    
    private void savePatient() {
        String firstName = patientFirstNameEditText.getText().toString().trim();
        String lastName = patientLastNameEditText.getText().toString().trim();
        String wing = wingSpinner.getSelectedItem().toString();
        String roomNumber = roomNumberSpinner.getSelectedItem().toString();
        String diet = dietSpinner.getSelectedItem().toString();
        String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();
        
        // Build texture modifications string
        String textureModifications = buildTextureModifications();
        
        // Create patient object
        Patient patient = new Patient();
        patient.setPatientFirstName(firstName);
        patient.setPatientLastName(lastName);
        patient.setWing(wing);
        patient.setRoomNumber(roomNumber);
        patient.setDiet(diet);
        patient.setFluidRestriction(fluidRestriction.equals("None") ? null : fluidRestriction);
        patient.setTextureModifications(textureModifications);
        
        // Validate patient data
        if (patient.getPatientFirstName().isEmpty() || patient.getPatientLastName().isEmpty()) {
            new AlertDialog.Builder(this)
                .setTitle("Validation Error")
                .setMessage("Patient name cannot be empty. Please check all fields.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }
        
        // Save to database
        long result = patientDAO.addPatient(patient);
        
        if (result > 0) {
            new AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("Patient " + firstName + " " + lastName + " has been added successfully!\n\nLocation: " + wing + " - Room " + roomNumber + "\nDiet: " + diet)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Clear form and stay on this activity for adding more patients
                    clearForm();
                    Toast.makeText(this, "Ready to add another patient", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", (dialog, which) -> finish())
                .show();
        } else {
            new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Failed to save patient. Please check all information and try again.")
                .setPositiveButton("OK", null)
                .show();
        }
    }
    
    private boolean validateForm() {
        String firstName = patientFirstNameEditText.getText().toString().trim();
        String lastName = patientLastNameEditText.getText().toString().trim();
        
        if (firstName.isEmpty()) {
            patientFirstNameEditText.setError("First name is required");
            patientFirstNameEditText.requestFocus();
            return false;
        }
        
        if (lastName.isEmpty()) {
            patientLastNameEditText.setError("Last name is required");
            patientLastNameEditText.requestFocus();
            return false;
        }
        
        if (roomNumberSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a wing first, then choose a room", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private String buildTextureModifications() {
        List<String> modifications = new ArrayList<>();
        
        if (mechanicalGroundCB.isChecked()) {
            modifications.add("Mechanical Ground");
        }
        if (mechanicalChoppedCB.isChecked()) {
            modifications.add("Mechanical Chopped");
        }
        if (biteSizeCB.isChecked()) {
            modifications.add("Bite Size");
        }
        if (breadOKCB.isChecked()) {
            modifications.add("Bread OK");
        }
        
        if (modifications.isEmpty()) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < modifications.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(modifications.get(i));
        }
        return result.toString();
    }
    
    private void clearForm() {
        patientFirstNameEditText.setText("");
        patientLastNameEditText.setText("");
        wingSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        
        // Clear any error messages
        patientFirstNameEditText.setError(null);
        patientLastNameEditText.setError(null);
        
        // Reset room dropdown
        updateRoomDropdown();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    // Simple TextWatcher implementation
    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void afterTextChanged(android.text.Editable s) {}
    }
}