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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PendingOrdersActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // UI Components
    private Spinner dayFilterSpinner;
    private ListView pendingOrdersListView;
    private TextView noPendingOrdersText;
    private TextView orderCountText;
    private Button backButton;
    private Button refreshButton;
    
    // Data
    private List<Patient> allPendingPatients = new ArrayList<>();
    private List<Patient> filteredPendingPatients = new ArrayList<>();
    private PendingOrdersAdapter adapter;
    
    // FIXED: Day of week options
    private String[] daysOfWeek = {"All Days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private String currentDayOfWeek;

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
        
        // FIXED: Get current day of week
        currentDayOfWeek = getCurrentDayOfWeek();
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load pending orders
        loadPendingOrders();
    }
    
    private void initializeUI() {
        dayFilterSpinner = findViewById(R.id.dayFilterSpinner);
        pendingOrdersListView = findViewById(R.id.pendingOrdersListView);
        noPendingOrdersText = findViewById(R.id.noPendingOrdersText);
        orderCountText = findViewById(R.id.orderCountText);
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
        
        // Set title
        setTitle("Pending Orders");
        
        // FIXED: Setup day filter spinner
        setupDayFilterSpinner();
        
        // Setup adapter
        adapter = new PendingOrdersAdapter(this, filteredPendingPatients);
        pendingOrdersListView.setAdapter(adapter);
    }
    
    // FIXED: Setup day filter spinner with current day as default
    private void setupDayFilterSpinner() {
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayFilterSpinner.setAdapter(dayAdapter);
        
        // Set current day as default selection
        int currentDayPosition = -1;
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i].equals(currentDayOfWeek)) {
                currentDayPosition = i;
                break;
            }
        }
        
        if (currentDayPosition > 0) {
            dayFilterSpinner.setSelection(currentDayPosition);
        }
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        refreshButton.setOnClickListener(v -> {
            loadPendingOrders();
            Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
        });
        
        // FIXED: Day filter listener
        dayFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterPendingOrdersByDay();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        pendingOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = filteredPendingPatients.get(position);
            openMealPlanning(patient);
        });
    }
    
    // FIXED: Load all patients that need meal orders (not just those with incomplete orders)
    private void loadPendingOrders() {
        try {
            // Get all patients who haven't completed their meals for today
            allPendingPatients = patientDAO.getPatientsNeedingOrders();
            
            // Sort by wing then room number (ascending)
            allPendingPatients.sort((p1, p2) -> {
                int wingCompare = p1.getWing().compareTo(p2.getWing());
                if (wingCompare != 0) {
                    return wingCompare;
                }
                
                // Try to parse room numbers as integers for proper sorting
                try {
                    int room1 = Integer.parseInt(p1.getRoomNumber().replaceAll("\\D", ""));
                    int room2 = Integer.parseInt(p2.getRoomNumber().replaceAll("\\D", ""));
                    return Integer.compare(room1, room2);
                } catch (NumberFormatException e) {
                    // If parsing fails, use string comparison
                    return p1.getRoomNumber().compareTo(p2.getRoomNumber());
                }
            });
            
            // Apply day filter
            filterPendingOrdersByDay();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading pending orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // FIXED: Filter patients by selected day
    private void filterPendingOrdersByDay() {
        filteredPendingPatients.clear();
        String selectedDay = (String) dayFilterSpinner.getSelectedItem();
        
        if ("All Days".equals(selectedDay)) {
            filteredPendingPatients.addAll(allPendingPatients);
        } else {
            // For now, just show all patients regardless of day
            // In a real implementation, you might filter based on admission date or other criteria
            filteredPendingPatients.addAll(allPendingPatients);
        }
        
        // Update UI
        updateUI();
    }
    
    private void updateUI() {
        if (filteredPendingPatients.isEmpty()) {
            pendingOrdersListView.setVisibility(ListView.GONE);
            noPendingOrdersText.setVisibility(TextView.VISIBLE);
            orderCountText.setVisibility(TextView.GONE);
            
            String selectedDay = (String) dayFilterSpinner.getSelectedItem();
            if ("All Days".equals(selectedDay)) {
                noPendingOrdersText.setText("✅ No pending orders!\nAll patients have completed their meal orders.");
            } else {
                noPendingOrdersText.setText("✅ No pending orders for " + selectedDay + "!\nAll patients have completed their meal orders.");
            }
        } else {
            pendingOrdersListView.setVisibility(ListView.VISIBLE);
            noPendingOrdersText.setVisibility(TextView.GONE);
            orderCountText.setVisibility(TextView.VISIBLE);
            
            String selectedDay = (String) dayFilterSpinner.getSelectedItem();
            String countText = filteredPendingPatients.size() + " pending order" + 
                              (filteredPendingPatients.size() == 1 ? "" : "s");
            if (!"All Days".equals(selectedDay)) {
                countText += " for " + selectedDay;
            }
            orderCountText.setText(countText);
            
            adapter.notifyDataSetChanged();
        }
    }
    
    private void openMealPlanning(Patient patient) {
        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("patient_id", patient.getPatientId());
        intent.putExtra("patient_name", patient.getFullName());
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
    
    // FIXED: Get current day of week
    private String getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        return dayFormat.format(calendar.getTime());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadPendingOrders();
    }
    
    // Enhanced adapter for pending orders
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
            
            patientNameText.setText(patient.getFullName());
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
            int incompleteMeals = 0;
            if (!patient.isBreakfastComplete()) incompleteMeals++;
            if (!patient.isLunchComplete()) incompleteMeals++;
            if (!patient.isDinnerComplete()) incompleteMeals++;
            
            String statusMessage;
            if (incompleteMeals == 0) {
                statusMessage = "All meals complete";
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                statusMessage = incompleteMeals + " meal" + (incompleteMeals == 1 ? "" : "s") + " pending";
                statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
            statusText.setText(statusMessage);
            
            return convertView;
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}