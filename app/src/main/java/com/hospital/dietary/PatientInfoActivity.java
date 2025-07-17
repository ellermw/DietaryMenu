package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.Arrays;
import java.util.List;

public class PatientInfoActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components
    private EditText patientNameInput;
    private Spinner wingSpinner;
    private EditText roomNumberInput;
    private Spinner dietSpinner;
    private Spinner fluidRestrictionSpinner;
    private CheckBox mechanicalGroundCB;
    private CheckBox mechanicalChoppedCB;
    private CheckBox biteSizeCB;
    private CheckBox breadOKCB;
    private CheckBox adaFriendlyCB;
    private Button savePatientButton;
    private Button backButton;
    
    // Data lists - Updated diet types as requested
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "ADA", "Cardiac", "Renal", 
                                              "Puree", "Full Liquid", "Clear Liquid");
    private List<String> fluidRestrictions = Arrays.asList("None", "1000ml", "1200ml", 
                                                          "1500ml", "2000ml", "2500ml");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load initial data
        loadInitialData();
    }
    
    private void initializeUI() {
        patientNameInput = findViewById(R.id.patientNameInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberInput = findViewById(R.id.roomNumberInput);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        adaFriendlyCB = findViewById(R.id.adaFriendlyCB);
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);
        
        // Set title
        setTitle("Patient Information");
    }
    
    private void setupListeners() {
        savePatientButton.setOnClickListener(v -> savePatientInfo());
        
        backButton.setOnClickListener(v -> finish());
        
        // Show/hide ADA checkbox based on diet selection
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = diets.get(position);
                if ("Full Liquid".equals(selectedDiet) || "Clear Liquid".equals(selectedDiet)) {
                    adaFriendlyCB.setVisibility(View.VISIBLE);
                } else {
                    adaFriendlyCB.setVisibility(View.GONE);
                    adaFriendlyCB.setChecked(false);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void loadInitialData() {
        // Setup Wing Spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // Setup Diet Spinner
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // Setup Fluid Restriction Spinner
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
        
        // Hide ADA checkbox initially
        adaFriendlyCB.setVisibility(View.GONE);
    }
    
    private void savePatientInfo() {
        // Validate input
        if (!validateInput()) {
            return;
        }
        
        // Get form data
        String patientName = patientNameInput.getText().toString().trim();
        String wing = wings.get(wingSpinner.getSelectedItemPosition());
        String roomNumber = roomNumberInput.getText().toString().trim();
        String diet = diets.get(dietSpinner.getSelectedItemPosition());
        String fluidRestriction = fluidRestrictions.get(fluidRestrictionSpinner.getSelectedItemPosition());
        
        // Build texture modifications string
        StringBuilder textureModifications = new StringBuilder();
        if (mechanicalGroundCB.isChecked()) {
            textureModifications.append("Mechanical Ground, ");
        }
        if (mechanicalChoppedCB.isChecked()) {
            textureModifications.append("Mechanical Chopped, ");
        }
        if (biteSizeCB.isChecked()) {
            textureModifications.append("Bite Size, ");
        }
        if (breadOKCB.isChecked()) {
            textureModifications.append("Bread OK, ");
        }
        
        // Remove trailing comma and space
        String textureModificationsStr = textureModifications.toString();
        if (textureModificationsStr.endsWith(", ")) {
            textureModificationsStr = textureModificationsStr.substring(0, textureModificationsStr.length() - 2);
        }
        
        // Handle ADA-friendly for Full Liquid and Clear Liquid
        if (adaFriendlyCB.isChecked() && adaFriendlyCB.getVisibility() == View.VISIBLE) {
            diet = diet + " (ADA)";
        }
        
        // Create patient object
        Patient patient = new Patient();
        patient.setName(patientName);
        patient.setWing(wing);
        patient.setRoomNumber(roomNumber);
        patient.setDiet(diet);
        patient.setFluidRestriction(fluidRestriction);
        patient.setTextureModifications(textureModificationsStr);
        
        // Save to database
        try {
            long result = patientDAO.addPatient(patient);
            if (result > 0) {
                Toast.makeText(this, "Patient information saved successfully!", Toast.LENGTH_SHORT).show();
                
                // Clear form for next patient
                clearForm();
                
                // Option to go to meal planning
                showMealPlanningOption(patient);
            } else {
                Toast.makeText(this, "Failed to save patient information", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validateInput() {
        // Validate patient name
        if (patientNameInput.getText().toString().trim().isEmpty()) {
            patientNameInput.setError("Patient name is required");
            patientNameInput.requestFocus();
            return false;
        }
        
        // Validate room number
        if (roomNumberInput.getText().toString().trim().isEmpty()) {
            roomNumberInput.setError("Room number is required");
            roomNumberInput.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        patientNameInput.setText("");
        roomNumberInput.setText("");
        wingSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        adaFriendlyCB.setChecked(false);
        adaFriendlyCB.setVisibility(View.GONE);
    }
    
    private void showMealPlanningOption(Patient patient) {
        new AlertDialog.Builder(this)
            .setTitle("Patient Saved")
            .setMessage("Would you like to plan meals for " + patient.getName() + " now?")
            .setPositiveButton("Yes", (dialog, which) -> {
                // Go to meal planning activity
                Intent intent = new Intent(this, MealPlanningActivity.class);
                intent.putExtra("patient_id", patient.getPatientId());
                intent.putExtra("patient_name", patient.getName());
                intent.putExtra("wing", patient.getWing());
                intent.putExtra("room", patient.getRoomNumber());
                intent.putExtra("diet", patient.getDiet());
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
            })
            .setNegativeButton("No", null)
            .show();
    }
}