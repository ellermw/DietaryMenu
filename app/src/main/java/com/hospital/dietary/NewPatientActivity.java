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
    
    // UI Components
    private EditText patientNameInput;
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
    
    // Data arrays
    private List<String> wings = Arrays.asList("Labor & Delivery", "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "Diabetic", "Low Sodium", "Renal", 
                                              "Cardiac", "Soft", "Full Liquid", "Clear Liquid");
    private List<String> fluidRestrictions = Arrays.asList("None", "1000ml", "1500ml", "2000ml");

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
        
        // Setup listeners
        setupListeners();
        
        // Load initial data
        loadInitialData();
    }
    
    private void initializeUI() {
        patientNameInput = findViewById(R.id.patientNameInput);
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
        
        // Set title
        setTitle("New Patient");
    }
    
    private void setupListeners() {
        savePatientButton.setOnClickListener(v -> savePatientInfo());
        backButton.setOnClickListener(v -> finish());
        clearFormButton.setOnClickListener(v -> clearForm());
        
        // Wing selection listener to update room dropdown
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomNumbers();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Show/hide ADA checkbox based on diet selection
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getItemAtPosition(position);
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
        loadWings();
        loadDiets();
        loadFluidRestrictions();
        
        // Hide ADA checkbox initially
        adaFriendlyCB.setVisibility(View.GONE);
    }
    
    private void loadWings() {
        List<String> wingList = new ArrayList<>(wings);
        wingList.add(0, "Select Wing");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wingList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(adapter);
    }
    
    private void updateRoomNumbers() {
        List<String> rooms = new ArrayList<>();
        
        if (wingSpinner.getSelectedItemPosition() > 0) {
            String selectedWing = (String) wingSpinner.getSelectedItem();
            
            switch (selectedWing) {
                case "Labor & Delivery":
                    // Rooms LDR1 through LDR5
                    for (int i = 1; i <= 5; i++) {
                        rooms.add("LDR" + i);
                    }
                    break;
                case "2 West":
                    // Rooms 225 through 248
                    for (int i = 225; i <= 248; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "3 North":
                    // Rooms 349 through 371
                    for (int i = 349; i <= 371; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "ICU":
                    // Rooms ICU1 through ICU13
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
    
    private void loadDiets() {
        List<String> dietList = new ArrayList<>(diets);
        dietList.add(0, "Select Diet");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(adapter);
    }
    
    private void loadFluidRestrictions() {
        List<String> restrictionList = new ArrayList<>(fluidRestrictions);
        restrictionList.add(0, "Select Fluid Restriction");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, restrictionList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(adapter);
    }
    
    private void savePatientInfo() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Create patient object
            Patient patient = new Patient();
            patient.setName(patientNameInput.getText().toString().trim());
            patient.setWing((String) wingSpinner.getSelectedItem());
            patient.setRoomNumber((String) roomSpinner.getSelectedItem());
            patient.setDiet((String) dietSpinner.getSelectedItem());
            patient.setFluidRestriction((String) fluidRestrictionSpinner.getSelectedItem());
            
            // Set texture modifications
            StringBuilder textureModifications = new StringBuilder();
            if (mechanicalGroundCB.isChecked()) textureModifications.append("Mechanical Ground, ");
            if (mechanicalChoppedCB.isChecked()) textureModifications.append("Mechanical Chopped, ");
            if (biteSizeCB.isChecked()) textureModifications.append("Bite Size, ");
            if (breadOKCB.isChecked()) textureModifications.append("Bread OK, ");
            if (adaFriendlyCB.isChecked()) textureModifications.append("ADA Friendly, ");
            
            if (textureModifications.length() > 0) {
                // Remove trailing comma and space
                textureModifications.setLength(textureModifications.length() - 2);
            }
            patient.setTextureModifications(textureModifications.toString());
            
            // FIXED: Set meal statuses to false so patient appears in pending orders
            patient.setBreakfastComplete(false);
            patient.setLunchComplete(false);
            patient.setDinnerComplete(false);
            patient.setBreakfastNPO(false);
            patient.setLunchNPO(false);
            patient.setDinnerNPO(false);
            
            // Save patient
            long patientId = patientDAO.addPatient(patient);
            
            if (patientId > 0) {
                patient.setPatientId((int) patientId);
                Toast.makeText(this, "Patient saved successfully!", Toast.LENGTH_SHORT).show();
                
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
        
        // Validate wing selection
        if (wingSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate room selection
        if (roomSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate diet selection
        if (dietSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a diet type", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate fluid restriction selection
        if (fluidRestrictionSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a fluid restriction", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        patientNameInput.setText("");
        wingSpinner.setSelection(0);
        roomSpinner.setSelection(0);
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
                intent.putExtra("fluid_restriction", patient.getFluidRestriction());
                intent.putExtra("texture_modifications", patient.getTextureModifications());
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
            })
            .setNegativeButton("No", null)
            .show();
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}