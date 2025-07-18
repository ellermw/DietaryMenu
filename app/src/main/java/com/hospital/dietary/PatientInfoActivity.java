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
import java.util.Arrays;
import java.util.List;

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
    private EditText roomNumberInput;
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
    
    // Data lists - FIXED: Updated with correct values
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "ADA", "Cardiac", "Renal", 
                                              "Puree", "Full Liquid", "Clear Liquid");
    private List<String> fluidRestrictions = Arrays.asList("None", "1000ml", "1500ml", "2000ml");
    
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
        
        // Initialize UI
        initializeUI();
        
        // Load data
        loadPatients();
        loadSpinnerData();
        
        // Setup listeners
        setupListeners();
    }
    
    private void initializeUI() {
        // FIXED: Initialize split name inputs
        patientFirstNameInput = findViewById(R.id.patientFirstNameInput);
        patientLastNameInput = findViewById(R.id.patientLastNameInput);
        searchInput = findViewById(R.id.searchInput);
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
        newPatientButton = findViewById(R.id.newPatientButton);
        deletePatientButton = findViewById(R.id.deletePatientButton);
        backButton = findViewById(R.id.backButton);
        patientsListView = findViewById(R.id.patientsListView);
        
        // Initially disable save and delete buttons
        savePatientButton.setEnabled(false);
        deletePatientButton.setEnabled(false);
    }
    
    private void loadPatients() {
        allPatients = patientDAO.getAllPatients();
        filteredPatients = allPatients;
        
        // FIXED: Custom adapter to display full name (first + last)
        patientsAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_1, filteredPatients) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Patient patient = getItem(position);
                TextView textView = (TextView) view;
                if (patient != null) {
                    String displayText = patient.getFullName() + " - " + patient.getLocationString();
                    textView.setText(displayText);
                }
                return view;
            }
        };
        
        patientsListView.setAdapter(patientsAdapter);
    }
    
    private void loadSpinnerData() {
        // Load wings
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // FIXED: Load only the 7 specified diet types
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // FIXED: Load only the 3 specified fluid restriction values (plus None)
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }
    
    private void setupListeners() {
        // FIXED: Enhanced search to work with first name, last name, or full name
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
        
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPatient = filteredPatients.get(position);
            populatePatientDetails(selectedPatient);
            savePatientButton.setEnabled(true);
            deletePatientButton.setEnabled(true);
        });
        
        savePatientButton.setOnClickListener(v -> savePatient());
        
        newPatientButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewPatientActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });
        
        deletePatientButton.setOnClickListener(v -> confirmDeletePatient());
        
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
            finish();
        });
    }
    
    // FIXED: Enhanced search to handle split names
    private void filterPatients(String searchText) {
        filteredPatients.clear();
        
        if (searchText.isEmpty()) {
            filteredPatients.addAll(allPatients);
        } else {
            String searchLower = searchText.toLowerCase();
            for (Patient patient : allPatients) {
                // Search in first name, last name, full name, wing, or room number
                if ((patient.getPatientFirstName() != null && patient.getPatientFirstName().toLowerCase().contains(searchLower)) ||
                    (patient.getPatientLastName() != null && patient.getPatientLastName().toLowerCase().contains(searchLower)) ||
                    patient.getFullName().toLowerCase().contains(searchLower) ||
                    patient.getWing().toLowerCase().contains(searchLower) ||
                    patient.getRoomNumber().toLowerCase().contains(searchLower)) {
                    filteredPatients.add(patient);
                }
            }
        }
        
        patientsAdapter.notifyDataSetChanged();
        
        // Clear selection if current patient is no longer in filtered list
        if (selectedPatient != null && !filteredPatients.contains(selectedPatient)) {
            clearPatientDetails();
        }
    }
    
    // FIXED: Populate form with split names
    private void populatePatientDetails(Patient patient) {
        patientFirstNameInput.setText(patient.getPatientFirstName() != null ? patient.getPatientFirstName() : "");
        patientLastNameInput.setText(patient.getPatientLastName() != null ? patient.getPatientLastName() : "");
        
        // Set wing spinner
        int wingPosition = wings.indexOf(patient.getWing());
        if (wingPosition >= 0) {
            wingSpinner.setSelection(wingPosition);
        }
        
        roomNumberInput.setText(patient.getRoomNumber());
        
        // Set diet spinner
        int dietPosition = diets.indexOf(patient.getDiet());
        if (dietPosition >= 0) {
            dietSpinner.setSelection(dietPosition);
        }
        
        // Set fluid restriction spinner
        String fluidRestriction = patient.getFluidRestriction();
        if (fluidRestriction == null || fluidRestriction.isEmpty()) {
            fluidRestriction = "None";
        }
        int fluidPosition = fluidRestrictions.indexOf(fluidRestriction);
        if (fluidPosition >= 0) {
            fluidRestrictionSpinner.setSelection(fluidPosition);
        } else {
            fluidRestrictionSpinner.setSelection(0); // Default to "None"
        }
        
        // Set texture modifications
        String textureModifications = patient.getTextureModifications();
        mechanicalGroundCB.setChecked(textureModifications != null && textureModifications.contains("Mechanical Ground"));
        mechanicalChoppedCB.setChecked(textureModifications != null && textureModifications.contains("Mechanical Chopped"));
        biteSizeCB.setChecked(textureModifications != null && textureModifications.contains("Bite Size"));
        breadOKCB.setChecked(textureModifications != null && textureModifications.contains("Bread OK"));
        
        // Set ADA friendly (if applicable)
        adaFriendlyCB.setChecked(patient.isADADiet());
    }
    
    private void clearPatientDetails() {
        selectedPatient = null;
        patientFirstNameInput.setText("");
        patientLastNameInput.setText("");
        wingSpinner.setSelection(0);
        roomNumberInput.setText("");
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        adaFriendlyCB.setChecked(false);
        savePatientButton.setEnabled(false);
        deletePatientButton.setEnabled(false);
    }
    
    // FIXED: Save patient with split names
    private void savePatient() {
        if (selectedPatient == null) {
            Toast.makeText(this, "No patient selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String firstName = patientFirstNameInput.getText().toString().trim();
        String lastName = patientLastNameInput.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String roomNumber = roomNumberInput.getText().toString().trim();
        String diet = (String) dietSpinner.getSelectedItem();
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        
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
        
        if (roomNumber.isEmpty()) {
            roomNumberInput.setError("Room number is required");
            roomNumberInput.requestFocus();
            return;
        }
        
        // Check for room conflicts (excluding current patient)
        for (Patient patient : allPatients) {
            if (patient.getPatientId() != selectedPatient.getPatientId() &&
                patient.getWing().equals(wing) && patient.getRoomNumber().equals(roomNumber)) {
                Toast.makeText(this, "Room " + roomNumber + " in " + wing + " is already occupied", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        // FIXED: Update patient with split names
        selectedPatient.setPatientFirstName(firstName);
        selectedPatient.setPatientLastName(lastName);
        selectedPatient.setWing(wing);
        selectedPatient.setRoomNumber(roomNumber);
        selectedPatient.setDiet(diet);
        selectedPatient.setFluidRestriction(fluidRestriction.equals("None") ? null : fluidRestriction);
        
        // Set texture modifications
        StringBuilder textureModifications = new StringBuilder();
        if (mechanicalGroundCB.isChecked()) textureModifications.append("Mechanical Ground, ");
        if (mechanicalChoppedCB.isChecked()) textureModifications.append("Mechanical Chopped, ");
        if (biteSizeCB.isChecked()) textureModifications.append("Bite Size, ");
        if (breadOKCB.isChecked()) textureModifications.append("Bread OK, ");
        
        String textureModsStr = textureModifications.toString();
        if (textureModsStr.endsWith(", ")) {
            textureModsStr = textureModsStr.substring(0, textureModsStr.length() - 2);
        }
        selectedPatient.setTextureModifications(textureModsStr.isEmpty() ? null : textureModsStr);
        
        // Save to database
        boolean success = patientDAO.updatePatient(selectedPatient);
        
        if (success) {
            Toast.makeText(this, "Patient updated successfully", Toast.LENGTH_SHORT).show();
            loadPatients(); // Reload to reflect changes
            clearPatientDetails();
        } else {
            Toast.makeText(this, "Error updating patient", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void confirmDeletePatient() {
        if (selectedPatient == null) {
            Toast.makeText(this, "No patient selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Delete Patient")
            .setMessage("Are you sure you want to delete " + selectedPatient.getFullName() + "?\n\n" +
                       "This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                boolean success = patientDAO.deletePatient(selectedPatient.getPatientId());
                if (success) {
                    Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                    loadPatients(); // Reload to reflect changes
                    clearPatientDetails();
                } else {
                    Toast.makeText(this, "Error deleting patient", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload patients when returning to this activity
        loadPatients();
        clearPatientDetails();
    }
}