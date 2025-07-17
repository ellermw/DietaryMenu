package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
        
        // FIXED: Patient click listener with improved debugging
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Log.d("ExistingPatient", "Item clicked at position: " + position);
                if (position >= 0 && position < filteredPatients.size()) {
                    Patient patient = filteredPatients.get(position);
                    Log.d("ExistingPatient", "Patient selected: " + patient.getName());
                    showPatientDetails(patient);
                } else {
                    Log.e("ExistingPatient", "Invalid position: " + position + ", list size: " + filteredPatients.size());
                    Toast.makeText(this, "Error selecting patient", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("ExistingPatient", "Error in click listener", e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadPatients() {
        try {
            allPatients = patientDAO.getAllPatients();
            filteredPatients.clear();
            filteredPatients.addAll(allPatients);
            
            Log.d("ExistingPatient", "Loaded " + allPatients.size() + " patients");
            
            // Update UI
            updateUI();
            
        } catch (Exception e) {
            Log.e("ExistingPatient", "Error loading patients", e);
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
    
    // FIXED: Enhanced showPatientDetails with debugging and fallback
    private void showPatientDetails(Patient patient) {
        try {
            Log.d("ExistingPatient", "Attempting to show details for patient: " + patient.getName());
            
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
            
            Log.d("ExistingPatient", "Starting PatientDetailActivity...");
            startActivity(intent);
            
        } catch (Exception e) {
            Log.e("ExistingPatient", "Error launching PatientDetailActivity", e);
            // Fallback: Show details in a dialog
            showPatientDetailsDialog(patient);
        }
    }
    
    // Fallback method: Show patient details in a dialog
    private void showPatientDetailsDialog(Patient patient) {
        StringBuilder details = new StringBuilder();
        details.append("Patient: ").append(patient.getName()).append("\n");
        details.append("Location: ").append(patient.getWing()).append(" - Room ").append(patient.getRoomNumber()).append("\n");
        details.append("Diet: ").append(patient.getDiet()).append("\n");
        details.append("Fluid: ").append(patient.getFluidRestriction() != null ? patient.getFluidRestriction() : "None").append("\n");
        
        if (patient.getTextureModifications() != null && !patient.getTextureModifications().isEmpty()) {
            details.append("Texture: ").append(patient.getTextureModifications()).append("\n");
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Patient Details")
            .setMessage(details.toString())
            .setPositiveButton("Plan Meals", (dialog, which) -> openMealPlanning(patient))
            .setNeutralButton("Try Full View", (dialog, which) -> showPatientDetails(patient))
            .setNegativeButton("Close", null)
            .show();
    }
    
    private void openMealPlanning(Patient patient) {
        try {
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
        } catch (Exception e) {
            Log.e("ExistingPatient", "Error launching MealPlanningActivity", e);
            Toast.makeText(this, "Error opening meal planning: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadPatients();
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
    
    // FIXED: Improved adapter with ViewHolder pattern and better performance
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
            // FIXED: Use actual patient ID instead of position
            return patients.get(position).getPatientId();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_existing_patient, parent, false);
                holder = new ViewHolder();
                holder.patientName = convertView.findViewById(R.id.patientNameText);
                holder.locationText = convertView.findViewById(R.id.locationText);
                holder.dietText = convertView.findViewById(R.id.dietText);
                holder.fluidText = convertView.findViewById(R.id.fluidText);
                holder.textureText = convertView.findViewById(R.id.textureText);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Patient patient = patients.get(position);
            
            holder.patientName.setText(patient.getName());
            holder.locationText.setText(patient.getWing() + " - Room " + patient.getRoomNumber());
            holder.dietText.setText("Diet: " + patient.getDiet());
            holder.fluidText.setText("Fluid: " + (patient.getFluidRestriction() != null ? 
                patient.getFluidRestriction() : "None"));
            
            if (patient.getTextureModifications() != null && !patient.getTextureModifications().isEmpty()) {
                holder.textureText.setText("Texture: " + patient.getTextureModifications());
                holder.textureText.setVisibility(View.VISIBLE);
            } else {
                holder.textureText.setVisibility(View.GONE);
            }
            
            // FIXED: Ensure the row is clickable
            convertView.setClickable(true);
            convertView.setFocusable(true);
            convertView.setBackgroundResource(android.R.drawable.list_selector_background);
            
            return convertView;
        }
        
        // ViewHolder pattern for better performance
        private class ViewHolder {
            TextView patientName;
            TextView locationText;
            TextView dietText;
            TextView fluidText;
            TextView textureText;
        }
    }
}