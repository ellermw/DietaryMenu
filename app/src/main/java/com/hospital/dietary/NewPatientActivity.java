package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewPatientActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components - FIXED: Split patient name into first and last
    private EditText patientFirstNameInput;
    private EditText patientLastNameInput;
    private Spinner wingSpinner;
    private Spinner roomSpinner;
    private Spinner dietSpinner;
    private Spinner fluidRestrictionSpinner;
    private CheckBox mechanicalGroundCB;
    private CheckBox mechanicalChoppedCB;
    private CheckBox biteSizeCB;
    private CheckBox breadOKCB;
    private CheckBox adaFriendlyCB;
    private Button savePatientButton;
    private Button backButton;
    private Button clearFormButton;
    
    // FIXED: Updated data arrays with correct values
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "ADA", "Cardiac", "Renal", 
                                              "Puree", "Full Liquid", "Clear Liquid");
    private List<String> fluidRestrictions = Arrays.asList("None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml");

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
        
        // Initialize UI
        initializeUI();
        
        // Load spinner data
        loadSpinnerData();
        
        // Setup listeners
        setupListeners();
    }
    
    private void initializeUI() {
        // FIXED: Initialize split name inputs
        patientFirstNameInput = findViewById(R.id.patientFirstNameInput);
        patientLastNameInput = findViewById(R.id.patientLastNameInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomSpinner = findViewById(R.id.roomSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        adaFriendlyCB = findViewById(R.id.adaFriendlyCB);
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);
        clearFormButton = findViewById(R.id.clearFormButton);
    }
    
    private void loadSpinnerData() {
        // Load wings
        List<String> wingList = new ArrayList<>(wings);
        wingList.add(0, "Select Wing");
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wingList);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // FIXED: Load only the 7 specified diet types
        List<String> dietList = new ArrayList<>(diets);
        dietList.add(0, "Select Diet");
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietList);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // FIXED: Load only the 3 specified fluid restriction values
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
        fluidRestrictionSpinner.setSelection(0); // Default to "None"
        
        // Set up wing spinner listener to update room spinner
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = (String) parent.getItemAtPosition(position);
                updateRoomSpinner(selectedWing);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void updateRoomSpinner(String wing) {
        List<String> rooms = new ArrayList<>();
        
        if (!wing.equals("Select Wing")) {
            switch (wing) {
                case "1 South":
                    for (int i = 101; i <= 120; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "2 North":
                    for (int i = 201; i <= 220; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "Labor and Delivery":
                    for (int i = 301; i <= 310; i++) {
                        rooms.add("L&D" + (i - 300));
                    }
                    break;
                case "2 West":
                    for (int i = 251; i <= 270; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "3 North":
                    for (int i = 301; i <= 320; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "ICU":
                    for (int i = 1; i <= 13; i++) {
                        rooms.add("ICU" + i);
                    }
                    break;
            }
        }
        
        rooms.add(0, "Select Room");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomSpinner.setAdapter(adapter);
    }
    
    private void setupListeners() {
        savePatientButton.setOnClickListener(v -> savePatient());
        
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientInfoActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
            finish();
        });
        
        clearFormButton.setOnClickListener(v -> clearForm());
    }
    
    private void savePatient() {
        // FIXED: Validate split first and last names
        String firstName = patientFirstNameInput.getText().toString().trim();
        String lastName = patientLastNameInput.getText().toString().trim();
        String selectedWing = (String) wingSpinner.getSelectedItem();
        String selectedRoom = (String) roomSpinner.getSelectedItem();
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        String selectedFluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        
        // Validation
        if (firstName.isEmpty()) {
            patientFirstNameInput.setError("First name is required");
            patientFirstNameInput.requestFocus();
            return;
        }
        
        if (lastName.isEmpty()) {
            patientLastNameInput.setError("Last name is required");
            patientLastNameInput.requestFocus();
            return;
        }
        
        if (selectedWing == null || selectedWing.equals("Select Wing")) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedRoom == null || selectedRoom.equals("Select Room")) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedDiet == null || selectedDiet.equals("Select Diet")) {
            Toast.makeText(this, "Please select a diet type", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check for duplicate patient in same room
        List<Patient> existingPatients = patientDAO.getAllPatients();
        for (Patient patient : existingPatients) {
            if (patient.getWing().equals(selectedWing) && patient.getRoomNumber().equals(selectedRoom)) {
                Toast.makeText(this, "Room " + selectedRoom + " in " + selectedWing + " is already occupied", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        // FIXED: Create patient with split first and last names
        Patient newPatient = new Patient();
        newPatient.setPatientFirstName(firstName);
        newPatient.setPatientLastName(lastName);
        newPatient.setWing(selectedWing);
        newPatient.setRoomNumber(selectedRoom);
        newPatient.setDiet(selectedDiet);
        newPatient.setFluidRestriction(selectedFluidRestriction);
        
        // Set texture modifications
        List<String> modifications = new ArrayList<>();
        if (mechanicalGroundCB.isChecked()) modifications.add("Mechanical Ground");
        if (mechanicalChoppedCB.isChecked()) modifications.add("Mechanical Chopped");
        if (biteSizeCB.isChecked()) modifications.add("Bite Size");
        if (breadOKCB.isChecked()) modifications.add("Bread OK");
        
        if (!modifications.isEmpty()) {
            newPatient.setTextureModifications(String.join(", ", modifications));
        }
        
        // Save to database
        long patientId = patientDAO.addPatient(newPatient);
        
        if (patientId > 0) {
            String fullName = firstName + " " + lastName;
            Toast.makeText(this, "Patient " + fullName + " added successfully", Toast.LENGTH_SHORT).show();
            
            // Clear form for next patient
            clearForm();
            
            // Optionally go back to patient list
            // finish();
        } else {
            Toast.makeText(this, "Error adding patient. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearForm() {
        // FIXED: Clear split name fields
        patientFirstNameInput.setText("");
        patientLastNameInput.setText("");
        wingSpinner.setSelection(0);
        roomSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        adaFriendlyCB.setChecked(false);
        
        // Reset focus to first name
        patientFirstNameInput.requestFocus();
    }
    
    @Override
    public void onBackPressed() {
        // Check if form has data
        String firstName = patientFirstNameInput.getText().toString().trim();
        String lastName = patientLastNameInput.getText().toString().trim();
        
        if (!firstName.isEmpty() || !lastName.isEmpty() || 
            wingSpinner.getSelectedItemPosition() > 0 || 
            dietSpinner.getSelectedItemPosition() > 0) {
            
            new AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to go back? Any unsaved changes will be lost.")
                .setPositiveButton("Discard", (dialog, which) -> {
                    Intent intent = new Intent(this, PatientInfoActivity.class);
                    intent.putExtra("current_user", currentUsername);
                    intent.putExtra("user_role", currentUserRole);
                    intent.putExtra("user_full_name", currentUserFullName);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
}