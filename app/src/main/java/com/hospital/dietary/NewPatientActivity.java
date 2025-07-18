package com.hospital.dietary;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPatientActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private Spinner wingSpinner;
    private Spinner roomSpinner;
    private Spinner dietSpinner;
    private EditText fluidRestrictionEditText;
    private EditText textureModificationsEditText;
    private Button savePatientButton;
    private Button backButton;
    
    // Wing to room mapping
    private Map<String, List<String>> wingRoomMap = new HashMap<>();

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
        setupWingRoomMapping();
        setupListeners();
    }
    
    private void initializeUI() {
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomSpinner = findViewById(R.id.roomSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionEditText = findViewById(R.id.fluidRestrictionEditText);
        textureModificationsEditText = findViewById(R.id.textureModificationsEditText);
        savePatientButton = findViewById(R.id.savePatientButton);
        backButton = findViewById(R.id.backButton);
        
        setTitle("New Patient");
        
        setupSpinners();
    }
    
    private void setupWingRoomMapping() {
        // Define wing to room mappings - customize based on your hospital layout
        wingRoomMap.put("North Wing", Arrays.asList("101", "102", "103", "104", "105", "106", "107", "108", "109", "110"));
        wingRoomMap.put("South Wing", Arrays.asList("201", "202", "203", "204", "205", "206", "207", "208", "209", "210"));
        wingRoomMap.put("East Wing", Arrays.asList("301", "302", "303", "304", "305", "306", "307", "308", "309", "310"));
        wingRoomMap.put("West Wing", Arrays.asList("401", "402", "403", "404", "405", "406", "407", "408", "409", "410"));
        wingRoomMap.put("ICU", Arrays.asList("ICU-1", "ICU-2", "ICU-3", "ICU-4", "ICU-5", "ICU-6", "ICU-7", "ICU-8"));
        wingRoomMap.put("Pediatrics", Arrays.asList("P101", "P102", "P103", "P104", "P105", "P106", "P107", "P108"));
    }
    
    private void setupSpinners() {
        // Wing spinner
        List<String> wings = new ArrayList<>(wingRoomMap.keySet());
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // Diet spinner
        String[] diets = {"Regular", "ADA Diabetic", "Cardiac", "Renal", "Soft", "Liquid", "NPO", "Pureed", "Mechanical Soft"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // Set initial room options
        updateRoomSpinner((String) wingSpinner.getSelectedItem());
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        savePatientButton.setOnClickListener(v -> savePatient());
        
        // Wing selection changes room options
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = (String) parent.getItemAtPosition(position);
                updateRoomSpinner(selectedWing);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void updateRoomSpinner(String selectedWing) {
        if (selectedWing != null && wingRoomMap.containsKey(selectedWing)) {
            List<String> rooms = wingRoomMap.get(selectedWing);
            ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomSpinner.setAdapter(roomAdapter);
        }
    }
    
    private void savePatient() {
        // Validate input
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String room = (String) roomSpinner.getSelectedItem();
        String diet = (String) dietSpinner.getSelectedItem();
        String fluidRestriction = fluidRestrictionEditText.getText().toString().trim();
        String textureModifications = textureModificationsEditText.getText().toString().trim();
        
        if (firstName.isEmpty()) {
            firstNameEditText.setError("First name is required");
            firstNameEditText.requestFocus();
            return;
        }
        
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Last name is required");
            lastNameEditText.requestFocus();
            return;
        }
        
        if (wing == null || room == null || diet == null) {
            Toast.makeText(this, "Please select wing, room, and diet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if patient already exists in this room
        Patient existingPatient = patientDAO.getPatientByLocation(wing, room);
        if (existingPatient != null) {
            Toast.makeText(this, "A patient already exists in " + wing + " Room " + room, Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
            // Create patient object
            Patient patient = new Patient();
            patient.setPatientFirstName(firstName);
            patient.setPatientLastName(lastName);
            patient.setWing(wing);
            patient.setRoomNumber(room);
            patient.setDiet(diet);
            patient.setFluidRestriction(fluidRestriction.isEmpty() ? null : fluidRestriction);
            patient.setTextureModifications(textureModifications.isEmpty() ? null : textureModifications);
            
            // Save patient
            long result = patientDAO.addPatient(patient);
            
            if (result > 0) {
                Toast.makeText(this, "Patient added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error adding patient. Please try again.", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}