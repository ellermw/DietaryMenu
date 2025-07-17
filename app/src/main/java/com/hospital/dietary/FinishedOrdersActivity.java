package com.hospital.dietary;

import android.app.AlertDialog;
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

public class FinishedOrdersActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components
    private ListView finishedOrdersListView;
    private TextView noFinishedOrdersText;
    private Button backButton;
    private Button refreshButton;
    
    // Data
    private List<Patient> finishedPatients = new ArrayList<>();
    private FinishedOrdersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_orders);
        
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
        
        // Load finished orders
        loadFinishedOrders();
    }
    
    private void initializeUI() {
        finishedOrdersListView = findViewById(R.id.finishedOrdersListView);
        noFinishedOrdersText = findViewById(R.id.noFinishedOrdersText);
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
        
        // Set title
        setTitle("Finished Orders");
        
        // Setup adapter
        adapter = new FinishedOrdersAdapter(this, finishedPatients);
        finishedOrdersListView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        refreshButton.setOnClickListener(v -> loadFinishedOrders());
        
        // Make list view non-clickable since orders are finalized
        finishedOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = finishedPatients.get(position);
            showOrderDetails(patient);
        });
    }
    
    private void loadFinishedOrders() {
        try {
            // Get all patients who have complete meal orders
            finishedPatients = patientDAO.getCompletedPatients();
            
            // Sort by wing then room number (descending as specified in requirements)
            finishedPatients.sort((p1, p2) -> {
                int wingCompare = p1.getWing().compareTo(p2.getWing());
                if (wingCompare != 0) {
                    return wingCompare;
                }
                
                // Try to parse room numbers as integers for proper sorting (descending)
                try {
                    int room1 = Integer.parseInt(p1.getRoomNumber());
                    int room2 = Integer.parseInt(p2.getRoomNumber());
                    return Integer.compare(room2, room1); // Descending order
                } catch (NumberFormatException e) {
                    // If parsing fails, use string comparison (descending)
                    return p2.getRoomNumber().compareTo(p1.getRoomNumber());
                }
            });
            
            // Update UI
            if (finishedPatients.isEmpty()) {
                finishedOrdersListView.setVisibility(ListView.GONE);
                noFinishedOrdersText.setVisibility(TextView.VISIBLE);
            } else {
                finishedOrdersListView.setVisibility(ListView.VISIBLE);
                noFinishedOrdersText.setVisibility(TextView.GONE);
                adapter.notifyDataSetChanged();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading finished orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showOrderDetails(Patient patient) {
        // Show detailed view of the order (read-only)
        StringBuilder details = new StringBuilder();
        details.append("Patient: ").append(patient.getName()).append("\n");
        details.append("Location: ").append(patient.getLocationString()).append("\n");
        details.append("Diet: ").append(patient.getDiet()).append("\n");
        
        if (patient.getFluidRestriction() != null && !patient.getFluidRestriction().equals("None")) {
            details.append("Fluid Restriction: ").append(patient.getFluidRestriction()).append("\n");
        }
        
        if (patient.getTextureModifications() != null && !patient.getTextureModifications().isEmpty()) {
            details.append("Texture Modifications: ").append(patient.getTextureModifications()).append("\n");
        }
        
        details.append("\nMeal Status:\n");
        details.append("• Breakfast: ").append(getMealStatus(patient.isBreakfastComplete(), patient.isBreakfastNPO())).append("\n");
        details.append("• Lunch: ").append(getMealStatus(patient.isLunchComplete(), patient.isLunchNPO())).append("\n");
        details.append("• Dinner: ").append(getMealStatus(patient.isDinnerComplete(), patient.isDinnerNPO())).append("\n");
        
        new AlertDialog.Builder(this)
            .setTitle("Order Details")
            .setMessage(details.toString())
            .setPositiveButton("Close", null)
            .show();
    }
    
    private String getMealStatus(boolean complete, boolean npo) {
        if (npo) {
            return "NPO";
        } else if (complete) {
            return "Complete";
        } else {
            return "Incomplete";
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadFinishedOrders();
    }
    
    // Inner class for the adapter
    private class FinishedOrdersAdapter extends BaseAdapter {
        private List<Patient> patients;
        private LayoutInflater inflater;
        
        public FinishedOrdersAdapter(FinishedOrdersActivity context, List<Patient> patients) {
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
                convertView = inflater.inflate(R.layout.item_finished_order, parent, false);
            }
            
            Patient patient = patients.get(position);
            
            TextView patientNameText = convertView.findViewById(R.id.patientNameText);
            TextView locationText = convertView.findViewById(R.id.locationText);
            TextView dietText = convertView.findViewById(R.id.dietText);
            TextView restrictionsText = convertView.findViewById(R.id.restrictionsText);
            TextView statusText = convertView.findViewById(R.id.statusText);
            TextView completedDateText = convertView.findViewById(R.id.completedDateText);
            
            patientNameText.setText(patient.getName());
            locationText.setText(patient.getWing() + " - Room " + patient.getRoomNumber());
            dietText.setText(patient.getDiet());
            
            // Show restrictions if any
            String restrictions = patient.getRestrictionsString();
            if (!restrictions.isEmpty()) {
                restrictionsText.setText(restrictions);
                restrictionsText.setVisibility(TextView.VISIBLE);
            } else {
                restrictionsText.setVisibility(TextView.GONE);
            }
            
            // Show completion status
            statusText.setText("✅ Complete");
            statusText.setTextColor(0xFF27ae60); // Green color
            
            // Show completion date
            if (patient.getCreatedDate() != null) {
                completedDateText.setText("Completed: " + patient.getCreatedDate().substring(0, 10));
                completedDateText.setVisibility(TextView.VISIBLE);
            } else {
                completedDateText.setVisibility(TextView.GONE);
            }
            
            return convertView;
        }
    }
}