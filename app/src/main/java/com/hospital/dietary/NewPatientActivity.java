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
    
    // Wing to room mapping with correct hospital layout
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
        // Define wing to room mappings based on hospital layout
        
        // 1 South - 106 through 122
        List<String> oneSouthRooms = new ArrayList<>();
        for (int i = 106; i <= 122; i++) {
            oneSouthRooms.add(String.valueOf(i));
        }
        wingRoomMap.put("1 South", oneSouthRooms);
        
        // 2 North - 250 through 264
        List<String> twoNorthRooms = new ArrayList<>();
        for (int i = 250; i <= 264; i++) {
            twoNorthRooms.add(String.valueOf(i));
        }
        wingRoomMap.put("2 North", twoNorthRooms);
        
        // Labor and Delivery - LDR1 through LDR6
        List<String> laborDeliveryRooms = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            laborDeliveryRooms.add("LDR" + i);
        }
        wingRoomMap.put("Labor and Delivery", laborDeliveryRooms);
        
        // 2 West - 225 through 248
        List<String> twoWestRooms = new ArrayList<>();
        for (int i = 225; i <= 248; i++) {
            twoWestRooms.add(String.valueOf(i));
        }
        wingRoomMap.put("2 West", twoWestRooms);
        
        // 3 North - 349 through 371
        List<String> threeNorthRooms = new ArrayList<>();
        for (int i = 349; i <= 371; i++) {
            threeNorthRooms.add(String.valueOf(i));
        }
        wingRoomMap.put("3 North", threeNorthRooms);
        
        // ICU - ICU1 through ICU13
        List<String> icuRooms = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            icuRooms.add("ICU" + i);
        }
        wingRoomMap.put("ICU", icuRooms);
    }
    
    private void setupSpinners() {
        // Wing spinner - in the specified order
        List<String> wings = Arrays.asList(
            "1 South", 
            "2 North", 
            "Labor and Delivery", 
            "2 West", 
            "3 North", 
            "ICU"
        );
        
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // Diet spinner
        String[] diets = {"Regular", "ADA Diabetic", "Cardiac", "Renal", "Soft", "Liquid", "NPO", "Pureed", "Mechanical Soft"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // Set initial room options for the first wing
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