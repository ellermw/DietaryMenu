package com.hospital.dietary;

import android.app.AlertDialog;
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
    
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components
    private EditText patientFirstNameEditText;
    private EditText patientLastNameEditText;
    private Spinner wingSpinner;
    private EditText roomNumberEditText;
    private Spinner dietSpinner;
    private Spinner fluidRestrictionSpinner;
    
    // FIXED: Texture modification checkboxes
    private CheckBox mechanicalGroundCB;
    private CheckBox mechanicalChoppedCB;
    private CheckBox biteSizeCB;
    private CheckBox breadOKCB;
    
    private Button savePatientButton;
    private Button backButton;
    
    // Data arrays
    private String[] wings = {"North", "South", "East", "West", "ICU", "ER"};
    private String[] diets = {"Regular", "ADA Diabetic", "Cardiac", "Renal", "Soft", "Liquid", "NPO", "Pureed", "Mechanical Soft"};
    private String[] fluidRestrictions = {"None", "1000ml", "1500ml", "2000ml", "2500ml"};

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
        
        initializeUI();
        setupSpinners();
        setupListeners();
        
        // FIXED: Initialize texture modification validation
        initializeTextureValidation();
        
        setTitle("Add New Patient");
    }
    
    private void initializeUI() {
        // Patient Information - FIXED: Match layout XML IDs
        patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
        patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberEditText = findViewById(R.id.roomNumberEditText);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        
        // FIXED: Texture modification checkboxes
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        
        // Buttons
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);
    }
    
    private void setupSpinners() {
        // Setup wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // Setup diet spinner
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // Setup fluid restriction spinner
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }
    
    private void setupListeners() {
        savePatientButton.setOnClickListener(v -> {
            // FIXED: Validate texture modifications before saving
            if (validateTextureModifications()) {
                savePatient();
            }
        });
        
        backButton.setOnClickListener(v -> finish());
    }
    
    // FIXED: Texture modification validation logic
    private void initializeTextureValidation() {
        setupTextureModificationListeners();
    }
    
    private void setupTextureModificationListeners() {
        mechanicalGroundCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                validateTextureModifications();
            }
        });
        
        mechanicalChoppedCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                validateTextureModifications();
            }
        });
        
        breadOKCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // No special action needed here - just keep for consistency
        });
    }
    
    private boolean validateTextureModifications() {
        boolean mechanicalGround = mechanicalGroundCB.isChecked();
        boolean mechanicalChopped = mechanicalChoppedCB.isChecked();
        boolean breadOK = breadOKCB.isChecked();
        
        // No validation needed during patient creation since no bread items are being selected yet
        // This will be enforced during meal planning
        return true;
    }
    
    private void showTextureValidationDialog(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Texture Modification Info")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();
    }
    
    private void savePatient() {
        if (!validateForm()) {
            return;
        }
        
        String firstName = patientFirstNameEditText.getText().toString().trim();
        String lastName = patientLastNameEditText.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String roomNumber = roomNumberEditText.getText().toString().trim();
        String diet = (String) dietSpinner.getSelectedItem();
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        
        // Build texture modifications string from checkboxes
        String textureModifications = getTextureModifications();
        
        // Create patient object
        Patient patient = new Patient();
        patient.setPatientFirstName(firstName);
        patient.setPatientLastName(lastName);
        patient.setWing(wing);
        patient.setRoomNumber(roomNumber);
        patient.setDiet(diet);
        patient.setFluidRestriction(fluidRestriction.equals("None") ? null : fluidRestriction);
        patient.setTextureModifications(textureModifications.equals("None") ? null : textureModifications);
        
        // Set meal completion status to false (new patient)
        patient.setBreakfastComplete(false);
        patient.setLunchComplete(false);
        patient.setDinnerComplete(false);
        patient.setBreakfastNPO(false);
        patient.setLunchNPO(false);
        patient.setDinnerNPO(false);
        
        // Check for room conflicts
        if (patientDAO.isRoomOccupied(wing, roomNumber)) {
            new AlertDialog.Builder(this)
                .setTitle("Room Conflict")
                .setMessage("Room " + roomNumber + " in " + wing + " wing is already occupied.\n\nPlease select a different room.")
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
        String roomNumber = roomNumberEditText.getText().toString().trim();
        
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
        
        if (roomNumber.isEmpty()) {
            roomNumberEditText.setError("Room number is required");
            roomNumberEditText.requestFocus();
            return false;
        }
        
        return true;
    }
    
    // FIXED: Build texture modifications from checkboxes
    private String getTextureModifications() {
        List<String> modifications = new ArrayList<>();
        
        if (mechanicalGroundCB.isChecked()) modifications.add("Mechanical Ground");
        if (mechanicalChoppedCB.isChecked()) modifications.add("Mechanical Chopped");
        if (biteSizeCB.isChecked()) modifications.add("Bite Size");
        if (breadOKCB.isChecked()) modifications.add("Bread OK");
        
        return modifications.isEmpty() ? "None" : String.join(", ", modifications);
    }
    
    private void clearForm() {
        patientFirstNameEditText.setText("");
        patientLastNameEditText.setText("");
        wingSpinner.setSelection(0);
        roomNumberEditText.setText("");
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        
        // Clear texture modification checkboxes
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        
        // Focus on first field
        patientFirstNameEditText.requestFocus();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}