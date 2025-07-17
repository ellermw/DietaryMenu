package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

public class PendingOrdersActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components
    private ListView pendingOrdersListView;
    private TextView noPendingOrdersText;
    private Button backButton;
    private Button refreshButton;
    
    // Data
    private List<Patient> pendingPatients = new ArrayList<>();
    private PendingOrdersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);
        
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
        
        // Load pending orders
        loadPendingOrders();
    }
    
    private void initializeUI() {
        pendingOrdersListView = findViewById(R.id.pendingOrdersListView);
        noPendingOrdersText = findViewById(R.id.noPendingOrdersText);
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
        
        // Set title
        setTitle("Pending Orders");
        
        // Setup adapter
        adapter = new PendingOrdersAdapter(this, pendingPatients);
        pendingOrdersListView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        refreshButton.setOnClickListener(v -> loadPendingOrders());
        
        pendingOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = pendingPatients.get(position);
            openMealPlanning(patient);
        });
    }
    
    private void loadPendingOrders() {
        try {
            // Get all patients who have incomplete meal orders
            pendingPatients = patientDAO.getPendingPatients();
            
            // Sort by wing then room number (ascending)
            pendingPatients.sort((p1, p2) -> {
                int wingCompare = p1.getWing().compareTo(p2.getWing());
                if (wingCompare != 0) {
                    return wingCompare;
                }
                
                // Try to parse room numbers as integers for proper sorting
                try {
                    int room1 = Integer.parseInt(p1.getRoomNumber());
                    int room2 = Integer.parseInt(p2.getRoomNumber());
                    return Integer.compare(room1, room2);
                } catch (NumberFormatException e) {
                    // If parsing fails, use string comparison
                    return p1.getRoomNumber().compareTo(p2.getRoomNumber());
                }
            });
            
            // Update UI
            if (pendingPatients.isEmpty()) {
                pendingOrdersListView.setVisibility(ListView.GONE);
                noPendingOrdersText.setVisibility(TextView.VISIBLE);
            } else {
                pendingOrdersListView.setVisibility(ListView.VISIBLE);
                noPendingOrdersText.setVisibility(TextView.GONE);
                adapter.notifyDataSetChanged();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading pending orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openMealPlanning(Patient patient) {
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
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadPendingOrders();
    }
    
    // Inner class for the adapter
    private class PendingOrdersAdapter extends BaseAdapter {
        private List<Patient> patients;
        private LayoutInflater inflater;
        
        public PendingOrdersAdapter(PendingOrdersActivity context, List<Patient> patients) {
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
            return patients.get(position).getPatientId();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_pending_order, parent, false);
            }
            
            Patient patient = patients.get(position);
            
            TextView patientNameText = convertView.findViewById(R.id.patientNameText);
            TextView locationText = convertView.findViewById(R.id.locationText);
            TextView dietText = convertView.findViewById(R.id.dietText);
            TextView restrictionsText = convertView.findViewById(R.id.restrictionsText);
            TextView statusText = convertView.findViewById(R.id.statusText);
            
            patientNameText.setText(patient.getName());
            locationText.setText(patient.getWing() + " - Room " + patient.getRoomNumber());
            dietText.setText(patient.getDiet());
            
            // Show restrictions if any
            StringBuilder restrictions = new StringBuilder();
            if (patient.getFluidRestriction() != null && !patient.getFluidRestriction().equals("None")) {
                restrictions.append("Fluid: ").append(patient.getFluidRestriction());
            }
            if (patient.getTextureModifications() != null && !patient.getTextureModifications().isEmpty()) {
                if (restrictions.length() > 0) {
                    restrictions.append(" | ");
                }
                restrictions.append("Texture: ").append(patient.getTextureModifications());
            }
            
            if (restrictions.length() > 0) {
                restrictionsText.setText(restrictions.toString());
                restrictionsText.setVisibility(TextView.VISIBLE);
            } else {
                restrictionsText.setVisibility(TextView.GONE);
            }
            
            // Show meal completion status
            String incompleteCount = patient.getIncompleteMealCount() + " meals pending";
            statusText.setText(incompleteCount);
            
            return convertView;
        }
    }
}