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
    private Toolbar toolbar;
    private ListView finishedOrdersListView;
    private TextView noFinishedOrdersText;
    private Button backButton;
    private Button homeButton;
    private Button printAllButton;
    private Button printSelectedButton;
    
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
        
        // Setup toolbar
        setupToolbar();
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load finished orders
        loadFinishedOrders();
    }

    private void setupToolbar() {
        // toolbar = findViewById(R.id.toolbar);  // Comment this out
        // if (toolbar != null) {
        //     setSupportActionBar(toolbar);
        //     if (getSupportActionBar() != null) {
        //         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //         getSupportActionBar().setTitle("Finished Orders");
        //     }
        // }

        // Use default action bar instead:
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Finished Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                loadFinishedOrders();
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
        finishedOrdersListView = findViewById(R.id.finishedOrdersListView);
        noFinishedOrdersText = findViewById(R.id.noFinishedOrdersText);
        backButton = findViewById(R.id.backButton);
     // homeButton = findViewById(R.id.homeButton);
        printAllButton = findViewById(R.id.printAllButton);
        printSelectedButton = findViewById(R.id.printSelectedButton);
        
        // Set title
        setTitle("Finished Orders");
        
        // Setup adapter
        adapter = new FinishedOrdersAdapter(this, finishedPatients);
        finishedOrdersListView.setAdapter(adapter);
        finishedOrdersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
    
    private void setupListeners() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }
        
        if (printAllButton != null) {
            printAllButton.setOnClickListener(v -> printAllOrders());
        }
        
        if (printSelectedButton != null) {
            printSelectedButton.setOnClickListener(v -> printSelectedOrders());
        }
        
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
                    int room1 = Integer.parseInt(p1.getRoomNumber().replaceAll("\\D", ""));
                    int room2 = Integer.parseInt(p2.getRoomNumber().replaceAll("\\D", ""));
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
                noFinishedOrdersText.setText("No finished orders found.\nComplete some pending orders first.");
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
        details.append("Patient: ").append(patient.getFullName()).append("\n");
        details.append("Location: ").append(patient.getWing()).append(" - Room ").append(patient.getRoomNumber()).append("\n");
        details.append("Diet: ").append(patient.getDiet()).append("\n");
        
        if (patient.getFluidRestriction() != null) {
            details.append("Fluid Restriction: ").append(patient.getFluidRestriction()).append("\n");
        }
        
        if (patient.getTextureModifications() != null) {
            details.append("Texture Modifications: ").append(patient.getTextureModifications()).append("\n");
        }
        
        details.append("\nMeal Status:\n");
        details.append("Breakfast: ").append(patient.isBreakfastNPO() ? "NPO" : "Complete").append("\n");
        details.append("Lunch: ").append(patient.isLunchNPO() ? "NPO" : "Complete").append("\n");
        details.append("Dinner: ").append(patient.isDinnerNPO() ? "NPO" : "Complete");
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Order Details")
            .setMessage(details.toString())
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void printAllOrders() {
        if (finishedPatients.isEmpty()) {
            Toast.makeText(this, "No orders to print", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement printing functionality
        Toast.makeText(this, "Print All Orders - " + finishedPatients.size() + " orders", Toast.LENGTH_SHORT).show();
    }
    
    private void printSelectedOrders() {
        android.util.SparseBooleanArray selectedItems = finishedOrdersListView.getCheckedItemPositions();
        List<Patient> selectedPatients = new ArrayList<>();
        
        for (int i = 0; i < selectedItems.size(); i++) {
            int position = selectedItems.keyAt(i);
            if (selectedItems.valueAt(i)) {
                selectedPatients.add(finishedPatients.get(position));
            }
        }
        
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No orders selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Implement printing functionality
        Toast.makeText(this, "Print Selected Orders - " + selectedPatients.size() + " orders", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadFinishedOrders();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    // Enhanced adapter for finished orders
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
                convertView = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            }
            
            Patient patient = patients.get(position);
            
            TextView textView = convertView.findViewById(android.R.id.text1);
            
            // Format: "John Doe - 1 South Room 106 (Regular Diet) ✅"
            String displayText = String.format("%s - %s Room %s (%s) ✅", 
                patient.getFullName(), 
                patient.getWing(), 
                patient.getRoomNumber(),
                patient.getDiet());
            
            textView.setText(displayText);
            
            return convertView;
        }
    }
}