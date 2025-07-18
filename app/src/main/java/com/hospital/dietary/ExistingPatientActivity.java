package com.hospital.dietary;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ExistingPatientActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    private EditText searchEditText;
    private ListView patientsListView;
    private TextView noPatientsText;
    private TextView patientCountText;
    private Button backButton;
    private Button clearSearchButton;
    
    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private ExistingPatientAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_patient);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        
        initializeUI();
        setupListeners();
        loadPatients();
    }
    
    private void initializeUI() {
        searchEditText = findViewById(R.id.searchEditText);
        patientsListView = findViewById(R.id.patientsListView);
        noPatientsText = findViewById(R.id.noPatientsText);
        patientCountText = findViewById(R.id.patientCountText);
        backButton = findViewById(R.id.backButton);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        
        setTitle("Existing Patients");
        
        // Setup adapter
        adapter = new ExistingPatientAdapter(this, filteredPatients);
        patientsListView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            searchEditText.clearFocus();
        });
        
        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Patient selection
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient selectedPatient = filteredPatients.get(position);
            openPatientDetail(selectedPatient);
        });
    }
    
    private void loadPatients() {
        try {
            allPatients = patientDAO.getAllPatients();
            sortPatients();
            filteredPatients.clear();
            filteredPatients.addAll(allPatients);
            updateUI();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void sortPatients() {
        // Sort by date (created_date), then wing, then room number (ascending)
        Collections.sort(allPatients, new Comparator<Patient>() {
            @Override
            public int compare(Patient p1, Patient p2) {
                // First sort by created date (most recent first)
                if (p1.getCreatedDate() != null && p2.getCreatedDate() != null) {
                    int dateCompare = p2.getCreatedDate().compareTo(p1.getCreatedDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                }
                
                // Then by wing
                int wingCompare = p1.getWing().compareTo(p2.getWing());
                if (wingCompare != 0) {
                    return wingCompare;
                }
                
                // Finally by room number (ascending)
                try {
                    int room1 = Integer.parseInt(p1.getRoomNumber());
                    int room2 = Integer.parseInt(p2.getRoomNumber());
                    return Integer.compare(room1, room2);
                } catch (NumberFormatException e) {
                    return p1.getRoomNumber().compareTo(p2.getRoomNumber());
                }
            }
        });
    }
    
    private void filterPatients(String searchTerm) {
        filteredPatients.clear();
        
        if (searchTerm.trim().isEmpty()) {
            filteredPatients.addAll(allPatients);
        } else {
            String lowerCaseFilter = searchTerm.toLowerCase().trim();
            
            for (Patient patient : allPatients) {
                String firstName = patient.getPatientFirstName().toLowerCase();
                String lastName = patient.getPatientLastName().toLowerCase();
                String fullName = firstName + " " + lastName;
                
                if (firstName.contains(lowerCaseFilter) || 
                    lastName.contains(lowerCaseFilter) || 
                    fullName.contains(lowerCaseFilter)) {
                    filteredPatients.add(patient);
                }
            }
        }
        
        updateUI();
    }
    
    private void updateUI() {
        if (filteredPatients.isEmpty()) {
            patientsListView.setVisibility(View.GONE);
            noPatientsText.setVisibility(View.VISIBLE);
            patientCountText.setVisibility(View.GONE);
            noPatientsText.setText(searchEditText.getText().toString().trim().isEmpty() ? 
                "No patients found" : "No patients match your search");
        } else {
            patientsListView.setVisibility(View.VISIBLE);
            noPatientsText.setVisibility(View.GONE);
            patientCountText.setVisibility(View.VISIBLE);
            patientCountText.setText(filteredPatients.size() + " patient(s) found");
        }
        
        adapter.notifyDataSetChanged();
    }
    
    private void openPatientDetail(Patient patient) {
        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.putExtra("patient_id", patient.getPatientId());
        intent.putExtra("patient_name", patient.getPatientFirstName() + " " + patient.getPatientLastName());
        intent.putExtra("wing", patient.getWing());
        intent.putExtra("room", patient.getRoomNumber());
        intent.putExtra("diet", patient.getDiet());
        intent.putExtra("fluid_restriction", patient.getFluidRestriction());
        intent.putExtra("texture_modifications", patient.getTextureModifications());
        startActivity(intent);
    }
}