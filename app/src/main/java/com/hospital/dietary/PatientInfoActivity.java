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

    // UI Components - Split patient name into first and last
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

    // Data lists with proper wings and complete fluid restrictions
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery",
            "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "Cardiac", "ADA", "Puree",
            "Renal", "Full Liquid", "Clear Liquid");
    private List<String> fluidRestrictions = Arrays.asList("None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml");

    // Room mapping and adapter
    private Map<String, String[]> wingRoomMap;
    private ArrayAdapter<String> roomAdapter;

    private List<Patient> allPatients;
    private List<Patient> filteredPatients;
    private ArrayAdapter<Patient> patientsAdapter;
    private Patient selectedPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_patient_info);

            // Get user information from intent
            currentUsername = getIntent().getStringExtra("current_user");
            currentUserRole = getIntent().getStringExtra("user_role");
            currentUserFullName = getIntent().getStringExtra("user_full_name");

            // Initialize database
            dbHelper = new DatabaseHelper(this);
            patientDAO = new PatientDAO(dbHelper);

            // Initialize data
            allPatients = new ArrayList<>();
            filteredPatients = new ArrayList<>();

            // Initialize room mapping
            initializeRoomMapping();

            // Initialize UI
            initializeUI();
            setupSpinners();
            setupListeners();

            // Load patients
            loadPatients();

            setTitle("Patient Information");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to initialize activity: " + e.getMessage());
            finish();
        }
    }

    private void initializeRoomMapping() {
        wingRoomMap = new HashMap<>();

        // 1 South - 101 through 129
        String[] south1Rooms = new String[29];
        for (int i = 0; i < 29; i++) {
            south1Rooms[i] = String.valueOf(101 + i);
        }
        wingRoomMap.put("1 South", south1Rooms);

        // 2 North - 201 through 229
        String[] north2Rooms = new String[29];
        for (int i = 0; i < 29; i++) {
            north2Rooms[i] = String.valueOf(201 + i);
        }
        wingRoomMap.put("2 North", north2Rooms);

        // Labor and Delivery - L1 through L6
        wingRoomMap.put("Labor and Delivery", new String[]{"L1", "L2", "L3", "L4", "L5", "L6"});

        // 2 West - 230 through 258
        String[] west2Rooms = new String[29];
        for (int i = 0; i < 29; i++) {
            west2Rooms[i] = String.valueOf(230 + i);
        }
        wingRoomMap.put("2 West", west2Rooms);

        // 3 North - 301 through 329
        String[] north3Rooms = new String[29];
        for (int i = 0; i < 29; i++) {
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

    private void initializeUI() {
        try {
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

            // Disable autofill for input fields
            if (patientFirstNameInput != null) {
                patientFirstNameInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            }
            if (patientLastNameInput != null) {
                patientLastNameInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            }
            if (searchInput != null) {
                searchInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to initialize UI: " + e.getMessage());
        }
    }

    private void setupSpinners() {
        try {
            // Wing spinner
            ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
            wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            wingSpinner.setAdapter(wingAdapter);

            // Room spinner (initially empty)
            roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
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

            // Set initial room options
            updateRoomDropdown();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to setup spinners: " + e.getMessage());
        }
    }

    private void updateRoomDropdown() {
        try {
            String selectedWing = wingSpinner.getSelectedItem().toString();
            String[] rooms = wingRoomMap.get(selectedWing);

            if (rooms != null) {
                roomAdapter.clear();
                roomAdapter.addAll(Arrays.asList(rooms));
                roomAdapter.notifyDataSetChanged();
                if (roomAdapter.getCount() > 0) {
                    roomNumberSpinner.setSelection(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to update room dropdown: " + e.getMessage());
        }
    }

    private void setupListeners() {
        try {
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
            searchInput.addTextChangedListener(new SimpleTextWatcher(() -> filterPatients()));

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to setup listeners: " + e.getMessage());
        }
    }

    private void loadPatients() {
        try {
            allPatients = patientDAO.getAllPatients();
            filteredPatients.clear();
            filteredPatients.addAll(allPatients);

            // Create simple adapter for patient list
            patientsAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_1, filteredPatients) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                    }

                    Patient patient = getItem(position);
                    TextView textView = convertView.findViewById(android.R.id.text1);
                    textView.setText(patient.getPatientFirstName() + " " + patient.getPatientLastName() +
                            " - " + patient.getWing() + " Room " + patient.getRoomNumber());

                    return convertView;
                }
            };

            patientsListView.setAdapter(patientsAdapter);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load patients: " + e.getMessage());
        }
    }

    private void filterPatients() {
        try {
            String searchTerm = searchInput.getText().toString().toLowerCase().trim();
            filteredPatients.clear();

            if (searchTerm.isEmpty()) {
                filteredPatients.addAll(allPatients);
            } else {
                for (Patient patient : allPatients) {
                    String fullName = (patient.getPatientFirstName() + " " + patient.getPatientLastName()).toLowerCase();
                    String wing = patient.getWing().toLowerCase();
                    String room = patient.getRoomNumber().toLowerCase();

                    if (fullName.contains(searchTerm) || wing.contains(searchTerm) || room.contains(searchTerm)) {
                        filteredPatients.add(patient);
                    }
                }
            }

            patientsAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to filter patients: " + e.getMessage());
        }
    }

    private void populatePatientForm(Patient patient) {
        try {
            patientFirstNameInput.setText(patient.getPatientFirstName());
            patientLastNameInput.setText(patient.getPatientLastName());

            // Set wing spinner
            int wingPosition = wings.indexOf(patient.getWing());
            if (wingPosition >= 0) {
                wingSpinner.setSelection(wingPosition);
                updateRoomDropdown(); // Update rooms for selected wing

                // Set room spinner
                String[] rooms = wingRoomMap.get(patient.getWing());
                if (rooms != null) {
                    for (int i = 0; i < rooms.length; i++) {
                        if (rooms[i].equals(patient.getRoomNumber())) {
                            roomNumberSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }

            // Set diet spinner
            int dietPosition = diets.indexOf(patient.getDiet());
            if (dietPosition >= 0) {
                dietSpinner.setSelection(dietPosition);
            }

            // Set fluid restriction spinner
            String fluidRestriction = patient.getFluidRestriction() != null ? patient.getFluidRestriction() : "None";
            int fluidPosition = fluidRestrictions.indexOf(fluidRestriction);
            if (fluidPosition >= 0) {
                fluidRestrictionSpinner.setSelection(fluidPosition);
            }

            // Set texture modification checkboxes
            String textureModifications = patient.getTextureModifications();
            mechanicalGroundCB.setChecked(textureModifications != null && textureModifications.contains("Mechanical Ground"));
            mechanicalChoppedCB.setChecked(textureModifications != null && textureModifications.contains("Mechanical Chopped"));
            biteSizeCB.setChecked(textureModifications != null && textureModifications.contains("Bite Size"));
            breadOKCB.setChecked(textureModifications != null && textureModifications.contains("Bread OK"));
            adaFriendlyCB.setChecked(textureModifications != null && textureModifications.contains("ADA Friendly"));

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to populate form: " + e.getMessage());
        }
    }

    private void savePatient() {
        try {
            String firstName = patientFirstNameInput.getText().toString().trim();
            String lastName = patientLastNameInput.getText().toString().trim();
            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();
            String diet = dietSpinner.getSelectedItem().toString();
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();

            // Validate required fields
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

            // Build texture modifications
            String textureModifications = buildTextureModifications();

            // Normalize diet field
            if ("ADA".equals(diet)) {
                diet = "ADA";
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

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to save patient: " + e.getMessage());
        }
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
        if (adaFriendlyCB.isChecked()) {
            modifications.add("ADA Friendly");
        }

        return modifications.isEmpty() ? null : String.join(", ", modifications);
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

    private void clearForm() {
        try {
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

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to clear form: " + e.getMessage());
        }
    }

    private void clearSelection() {
        selectedPatient = null;
        updateButtonStates();
    }

    private void updateButtonStates() {
        deletePatientButton.setEnabled(selectedPatient != null);
        savePatientButton.setText(selectedPatient != null ? "Update Patient" : "Add Patient");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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