package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class PatientInfoActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components - FIXED: Split patient name into first and last
    private EditText patientFirstNameInput;
    private EditText patientLastNameInput;
    private EditText searchInput;
    private Spinner wingSpinner;
    private Spinner roomNumberSpinner;
    private Spinner dietSpinner;
    private Spinner fluidRestrictionSpinner;
    private CheckBox mechanicalGroundCB;
    private CheckBox mechanicalChoppedCB;
    private CheckBox biteSizeCB;
    private CheckBox breadOKCB;
    private CheckBox adaFriendlyCB;
    private Button savePatientButton;
    private Button newPatientButton;
    private Button deletePatientButton;
    private Button backButton;
    private ListView patientsListView;
    
    // CORRECTED: Data lists with proper wings and complete fluid restrictions
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "Cardiac", "ADA", "Puree", 
                                              "Renal", "Full Liquid", "Clear Liquid");
    private List<String> fluidRestrictions = Arrays.asList("None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml");
    
    // ADDED: Room mapping and adapter
    private Map<String, String[]> wingRoomMap;
    private ArrayAdapter<String> roomAdapter;
    
    private List<Patient> allPatients;
    private List<Patient> filteredPatients;
    private ArrayAdapter<Patient> patientsAdapter;
    private Patient selectedPatient = null;

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
        
        // Initialize room mapping
        initializeRoomMapping();
        
        // Initialize UI
        initializeUI();
        setupSpinners();
        setupListeners();
        loadPatients();
        
        setTitle("Patient Information");
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
        patientFirstNameInput = findViewById(R.id.patientFirstNameInput);
        patientLastNameInput = findViewById(R.id.patientLastNameInput);
        searchInput = findViewById(R.id.searchInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        adaFriendlyCB = findViewById(R.id.adaFriendlyCB);
        savePatientButton = findViewById(R.id.savePatientButton);
        newPatientButton = findViewById(R.id.newPatientButton);
        deletePatientButton = findViewById(R.id.deletePatientButton);
        backButton = findViewById(R.id.backButton);
        patientsListView = findViewById(R.id.patientsListView);
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
        
        // Wing selection listener - updates room dropdown
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomDropdown();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Diet spinner
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // Fluid restriction spinner
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }
    
    private void updateRoomDropdown() {
        String selectedWing = wingSpinner.getSelectedItem().toString();
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
    
    private void setupListeners() {
        savePatientButton.setOnClickListener(v -> savePatient());
        newPatientButton.setOnClickListener(v -> addNewPatient());
        deletePatientButton.setOnClickListener(v -> deletePatient());
        backButton.setOnClickListener(v -> finish());
        
        // Patient list selection
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPatient = filteredPatients.get(position);
            populatePatientForm(selectedPatient);
            updateButtonStates();
        });
        
        // Search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Clear selection when form is modified
        patientFirstNameInput.addTextChangedListener(new SimpleTextWatcher(() -> clearSelection()));
        patientLastNameInput.addTextChangedListener(new SimpleTextWatcher(() -> clearSelection()));
        
        // Wing change also clears selection
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomDropdown();
                clearSelection();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void loadPatients() {
        allPatients = patientDAO.getAllPatients();
        filteredPatients = new ArrayList<>(allPatients);
        
        patientsAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_1, filteredPatients) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                
                Patient patient = filteredPatients.get(position);
                TextView textView = convertView.findViewById(android.R.id.text1);
                
                String displayText = String.format(java.util.Locale.getDefault(),
                    "%s, %s - %s Room %s (%s)", 
                    patient.getPatientLastName(),
                    patient.getPatientFirstName(),
                    patient.getWing(),
                    patient.getRoomNumber(),
                    patient.getDiet());
                    
                textView.setText(displayText);
                
                return convertView;
            }
        };
        
        patientsListView.setAdapter(patientsAdapter);
        updateButtonStates();
    }
    
    private void filterPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredPatients = new ArrayList<>(allPatients);
        } else {
            filteredPatients = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (Patient patient : allPatients) {
                if (patient.getPatientFirstName().toLowerCase().contains(lowerQuery) ||
                    patient.getPatientLastName().toLowerCase().contains(lowerQuery) ||
                    patient.getWing().toLowerCase().contains(lowerQuery) ||
                    patient.getRoomNumber().toLowerCase().contains(lowerQuery) ||
                    patient.getDiet().toLowerCase().contains(lowerQuery)) {
                    filteredPatients.add(patient);
                }
            }
        }
        
        patientsAdapter.notifyDataSetChanged();
    }
    
    private void populatePatientForm(Patient patient) {
        patientFirstNameInput.setText(patient.getPatientFirstName());
        patientLastNameInput.setText(patient.getPatientLastName());
        
        // Set wing first, which will populate room dropdown
        setSpinnerSelection(wingSpinner, patient.getWing());
        
        // Wait for room dropdown to populate, then set room
        wingSpinner.post(() -> {
            updateRoomDropdown();
            setSpinnerSelection(roomNumberSpinner, patient.getRoomNumber());
        });
        
        // Set other spinners
        setSpinnerSelection(dietSpinner, patient.getDiet());
        setSpinnerSelection(fluidRestrictionSpinner, 
            patient.getFluidRestriction() != null ? patient.getFluidRestriction() : "None");
        
        // Set texture modification checkboxes
        String textureModifications = patient.getTextureModifications();
        mechanicalGroundCB.setChecked(textureModifications != null && textureModifications.contains("Mechanical Ground"));
        mechanicalChoppedCB.setChecked(textureModifications != null && textureModifications.contains("Mechanical Chopped"));
        biteSizeCB.setChecked(textureModifications != null && textureModifications.contains("Bite Size"));
        breadOKCB.setChecked(textureModifications != null && textureModifications.contains("Bread OK"));
        
        // Set ADA checkbox
        adaFriendlyCB.setChecked(patient.getDiet().contains("ADA"));
    }
    
    private void setSpinnerSelection(Spinner spinner, String value) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }
    
    private void savePatient() {
        if (!validateForm()) {
            return;
        }
        
        String firstName = patientFirstNameInput.getText().toString().trim();
        String lastName = patientLastNameInput.getText().toString().trim();
        String wing = wingSpinner.getSelectedItem().toString();
        String roomNumber = roomNumberSpinner.getSelectedItem() != null ? 
                          roomNumberSpinner.getSelectedItem().toString() : "";
        String diet = dietSpinner.getSelectedItem().toString();
        String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();
        String textureModifications = buildTextureModifications();
        
        // Handle ADA diet type
        if (adaFriendlyCB.isChecked() && !diet.contains("ADA")) {
            if ("Clear Liquid".equals(diet)) {
                diet = "Clear Liquid ADA";
            } else if (!"ADA".equals(diet)) {
                diet = "ADA";
            }
        }
        
        if (selectedPatient != null) {
            // Update existing patient
            selectedPatient.setPatientFirstName(firstName);
            selectedPatient.setPatientLastName(lastName);
            selectedPatient.setWing(wing);
            selectedPatient.setRoomNumber(roomNumber);
            selectedPatient.setDiet(diet);
            selectedPatient.setFluidRestriction(fluidRestriction.equals("None") ? null : fluidRestriction);
            selectedPatient.setTextureModifications(textureModifications);
            
            boolean success = patientDAO.updatePatient(selectedPatient);
            
            if (success) {
                Toast.makeText(this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
                loadPatients();
                clearForm();
            } else {
                Toast.makeText(this, "Failed to update patient", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add new patient
            Patient patient = new Patient();
            patient.setPatientFirstName(firstName);
            patient.setPatientLastName(lastName);
            patient.setWing(wing);
            patient.setRoomNumber(roomNumber);
            patient.setDiet(diet);
            patient.setFluidRestriction(fluidRestriction.equals("None") ? null : fluidRestriction);
            patient.setTextureModifications(textureModifications);
            
            long result = patientDAO.addPatient(patient);
            
            if (result > 0) {
                Toast.makeText(this, "Patient added successfully", Toast.LENGTH_SHORT).show();
                loadPatients();
                clearForm();
            } else {
                Toast.makeText(this, "Failed to add patient", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void addNewPatient() {
        Intent intent = new Intent(this, NewPatientActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }
    
    private void deletePatient() {
        if (selectedPatient == null) {
            Toast.makeText(this, "Please select a patient to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete Patient")
            .setMessage("Are you sure you want to delete " + selectedPatient.getPatientFirstName() + 
                       " " + selectedPatient.getPatientLastName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean success = patientDAO.deletePatient(selectedPatient.getPatientId());
                if (success) {
                    Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                    loadPatients();
                    clearForm();
                } else {
                    Toast.makeText(this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private boolean validateForm() {
        String firstName = patientFirstNameInput.getText().toString().trim();
        String lastName = patientLastNameInput.getText().toString().trim();
        
        if (firstName.isEmpty()) {
            patientFirstNameInput.setError("First name is required");
            patientFirstNameInput.requestFocus();
            return false;
        }
        
        if (lastName.isEmpty()) {
            patientLastNameInput.setError("Last name is required");
            patientLastNameInput.requestFocus();
            return false;
        }
        
        if (roomNumberSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a wing first, then choose a room", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private String buildTextureModifications() {
        java.util.List<String> modifications = new java.util.ArrayList<>();
        
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
        patientFirstNameInput.setText("");
        patientLastNameInput.setText("");
        wingSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        adaFriendlyCB.setChecked(false);
        
        // Clear any error messages
        patientFirstNameInput.setError(null);
        patientLastNameInput.setError(null);
        
        // Reset room dropdown
        updateRoomDropdown();
        
        clearSelection();
    }
    
    private void clearSelection() {
        selectedPatient = null;
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        deletePatientButton.setEnabled(selectedPatient != null);
        savePatientButton.setText(selectedPatient != null ? "Update Patient" : "Add Patient");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadPatients(); // Refresh the list when returning to this activity
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    // Simple TextWatcher implementation
    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable action;
        
        public SimpleTextWatcher(Runnable action) {
            this.action = action;
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            action.run();
        }
        
        @Override
        public void afterTextChanged(Editable s) {}
    }
}