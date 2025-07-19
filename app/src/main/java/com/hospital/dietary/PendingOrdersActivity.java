package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private Toolbar toolbar;
    private Spinner dayFilterSpinner;
    private ListView pendingOrdersListView;
    private TextView noPendingOrdersText;
    private TextView orderCountText;
    private Button backButton;
    private Button homeButton;
    private Button refreshButton;
    
    // Data
    private List<Patient> allPendingPatients = new ArrayList<>();
    private List<Patient> filteredPendingPatients = new ArrayList<>();
    private PendingOrdersAdapter adapter;
    
    // Day of week options
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
        
        // Get current day of week
        currentDayOfWeek = getCurrentDayOfWeek();
        
        // Setup toolbar
        setupToolbar();
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load pending orders
        loadPendingOrders();
    }
    
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("Pending Orders");
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_home:
                goToMainMenu();
                return true;
            case R.id.action_refresh:
                loadPendingOrders();
                Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    
    private void initializeUI() {
        dayFilterSpinner = findViewById(R.id.dayFilterSpinner);
        pendingOrdersListView = findViewById(R.id.pendingOrdersListView);
        noPendingOrdersText = findViewById(R.id.noPendingOrdersText);
        orderCountText = findViewById(R.id.orderCountText);
        backButton = findViewById(R.id.backButton);
        homeButton = findViewById(R.id.homeButton);
        refreshButton = findViewById(R.id.refreshButton);
        
        // Set title
        setTitle("Pending Orders");
        
        // Setup day filter spinner
        setupDayFilterSpinner();
        
        // Setup adapter
        adapter = new PendingOrdersAdapter(this, filteredPendingPatients);
        pendingOrdersListView.setAdapter(adapter);
    }
    
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
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }
        
        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> {
                loadPendingOrders();
                Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Day filter listener
        dayFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterPendingOrdersByDay();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // FIXED: Make list items clickable for editing
        pendingOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = filteredPendingPatients.get(position);
            openMealPlanning(patient);
        });
    }
    
    // FIXED: Load only patients that have incomplete meal orders
    private void loadPendingOrders() {
        try {
            // CORRECTED: Get only patients who haven't completed their meals
            allPendingPatients = patientDAO.getPendingPatients();
            
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
            if (orderCountText != null) {
                orderCountText.setVisibility(TextView.GONE);
            }
            
            String selectedDay = (String) dayFilterSpinner.getSelectedItem();
            if ("All Days".equals(selectedDay)) {
                noPendingOrdersText.setText("✅ No pending orders!\nAll patients have completed their meal orders.");
            } else {
                noPendingOrdersText.setText("✅ No pending orders for " + selectedDay + "!\nAll patients have completed their meal orders.");
            }
        } else {
            pendingOrdersListView.setVisibility(ListView.VISIBLE);
            noPendingOrdersText.setVisibility(TextView.GONE);
            if (orderCountText != null) {
                orderCountText.setVisibility(TextView.VISIBLE);
                
                String selectedDay = (String) dayFilterSpinner.getSelectedItem();
                String countText = filteredPendingPatients.size() + " pending order" + 
                                  (filteredPendingPatients.size() == 1 ? "" : "s");
                if (!"All Days".equals(selectedDay)) {
                    countText += " for " + selectedDay;
                }
                orderCountText.setText(countText);
            }
            
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
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
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            
            Patient patient = patients.get(position);
            
            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);
            
            // Main patient info
            String mainText = String.format("%s - %s Room %s", 
                patient.getFullName(), patient.getWing(), patient.getRoomNumber());
            text1.setText(mainText);
            
            // Diet and incomplete meals info
            StringBuilder subText = new StringBuilder();
            subText.append("Diet: ").append(patient.getDiet());
            
            // Show which meals are incomplete
            List<String> incompleteMeals = new ArrayList<>();
            if (!patient.isBreakfastComplete() && !patient.isBreakfastNPO()) {
                incompleteMeals.add("Breakfast");
            }
            if (!patient.isLunchComplete() && !patient.isLunchNPO()) {
                incompleteMeals.add("Lunch");
            }
            if (!patient.isDinnerComplete() && !patient.isDinnerNPO()) {
                incompleteMeals.add("Dinner");
            }
            
            if (!incompleteMeals.isEmpty()) {
                subText.append(" | Pending: ").append(String.join(", ", incompleteMeals));
            }
            
            text2.setText(subText.toString());
            
            // Make it visually clear this item is clickable
            convertView.setBackgroundResource(android.R.drawable.list_selector_background);
            
            return convertView;
        }
    }
}