package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

public class ExistingPatientActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components
    private EditText searchInput;
    private ListView patientsListView;
    private TextView noPatientsText;
    private Button backButton;
    private Button refreshButton;
    
    // Data
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
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load patients
        loadPatients();
    }
    
    private void initializeUI() {
        searchInput = findViewById(R.id.searchInput);
        patientsListView = findViewById(R.id.patientsListView);
        noPatientsText = findViewById(R.id.noPatientsText);
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
        
        // Set title
        setTitle("Existing Patients");
        
        // Setup adapter
        adapter = new ExistingPatientAdapter(this, filteredPatients);
        patientsListView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        refreshButton.setOnClickListener(v -> loadPatients());
        
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
        
        // Patient click listener
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = filteredPatients.get(position);
            showPatientDetails(patient);
        });
    }
    
    private void loadPatients() {
        try {
            allPatients = patientDAO.getAllPatients();
            filteredPatients.clear();
            filteredPatients.addAll(allPatients);
            
            // Update UI
            updateUI();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void filterPatients(String query) {
        filteredPatients.clear();
        
        if (query.isEmpty()) {
            filteredPatients.addAll(allPatients);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Patient patient : allPatients) {
                if (patient.getName().toLowerCase().contains(lowerCaseQuery) ||
                    patient.getWing().toLowerCase().contains(lowerCaseQuery) ||
                    patient.getRoomNumber().toLowerCase().contains(lowerCaseQuery) ||
                    patient.getDiet().toLowerCase().contains(lowerCaseQuery)) {
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
            
            if (searchInput.getText().toString().isEmpty()) {
                noPatientsText.setText("No patients found.\nAdd a new patient to get started!");
            } else {
                noPatientsText.setText("No patients match your search.\nTry a different search term.");
            }
        } else {
            patientsListView.setVisibility(View.VISIBLE);
            noPatientsText.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }
    
    private void showPatientDetails(Patient patient) {
        Intent intent = new Intent(this, PatientDetailActivity.class);
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
    }
    
    // Adapter class for existing patients
    private class ExistingPatientAdapter extends BaseAdapter {
        private List<Patient> patients;
        private LayoutInflater inflater;
        
        public ExistingPatientAdapter(ExistingPatientActivity context, List<Patient> patients) {
            this.patients = patients;
            this.inflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() {
            return patients.size();
        }
        
        @Override
        public Object getItem(int position) {
            return patients.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_existing_patient, parent, false);
            }
            
            Patient patient = patients.get(position);
            
            TextView patientName = convertView.findViewById(R.id.patientNameText);
            TextView locationText = convertView.findViewById(R.id.locationText);
            TextView dietText = convertView.findViewById(R.id.dietText);
            TextView fluidText = convertView.findViewById(R.id.fluidText);
            TextView textureText = convertView.findViewById(R.id.textureText);
            
            patientName.setText(patient.getName());
            locationText.setText(patient.getWing() + " - Room " + patient.getRoomNumber());
            dietText.setText("Diet: " + patient.getDiet());
            fluidText.setText("Fluid: " + (patient.getFluidRestriction() != null ? patient.getFluidRestriction() : "None"));
            
            if (patient.getTextureModifications() != null && !patient.getTextureModifications().isEmpty()) {
                textureText.setText("Texture: " + patient.getTextureModifications());
                textureText.setVisibility(View.VISIBLE);
            } else {
                textureText.setVisibility(View.GONE);
            }
            
            return convertView;
        }
    }
}