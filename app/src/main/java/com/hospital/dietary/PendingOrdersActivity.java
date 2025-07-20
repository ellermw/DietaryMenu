package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private ListView pendingOrdersListView;
    private TextView noPendingOrdersText;

    private List<Patient> pendingPatients = new ArrayList<>();
    private ArrayAdapter<Patient> pendingAdapter;

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

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pending Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupListeners();
        loadPendingOrders();
    }

    private void initializeUI() {
        pendingOrdersListView = findViewById(R.id.pendingOrdersListView);
        noPendingOrdersText = findViewById(R.id.noPendingOrdersText);

        // Set up click listener for meal planning
        pendingOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient selectedPatient = pendingPatients.get(position);
            openMealPlanning(selectedPatient);
        });
    }

    private void setupListeners() {
        // No additional listeners needed for this activity
    }

    private void loadPendingOrders() {
        try {
            // Get patients who have incomplete meal orders (not all meals complete)
            pendingPatients = patientDAO.getPendingPatients();

            if (pendingPatients.isEmpty()) {
                pendingOrdersListView.setVisibility(View.GONE);
                noPendingOrdersText.setVisibility(View.VISIBLE);
                noPendingOrdersText.setText("✅ No pending orders!\nAll patients have completed meal orders.");
            } else {
                pendingOrdersListView.setVisibility(View.VISIBLE);
                noPendingOrdersText.setVisibility(View.GONE);

                // Create adapter for pending patients
                pendingAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_2, android.R.id.text1, pendingPatients) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        Patient patient = getItem(position);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        if (patient != null) {
                            text1.setText(patient.getFullName());

                            // Show meal completion status instead of just location
                            String locationInfo = patient.getWing() + " - Room " + patient.getRoomNumber();
                            String mealStatus = getMealCompletionStatus(patient);
                            text2.setText(locationInfo + " • " + patient.getDiet() + "\n" + mealStatus);
                        }

                        return view;
                    }
                };

                pendingOrdersListView.setAdapter(pendingAdapter);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading pending orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to show meal completion status
    private String getMealCompletionStatus(Patient patient) {
        List<String> completed = new ArrayList<>();
        List<String> pending = new ArrayList<>();

        if (patient.isBreakfastComplete() || patient.isBreakfastNPO()) {
            completed.add("Breakfast");
        } else {
            pending.add("Breakfast");
        }

        if (patient.isLunchComplete() || patient.isLunchNPO()) {
            completed.add("Lunch");
        } else {
            pending.add("Lunch");
        }

        if (patient.isDinnerComplete() || patient.isDinnerNPO()) {
            completed.add("Dinner");
        } else {
            pending.add("Dinner");
        }

        if (pending.isEmpty()) {
            return "✅ All meals complete";
        } else {
            return "⏳ Pending: " + String.join(", ", pending);
        }
    }

    private void openMealPlanning(Patient patient) {
        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("patient_id", patient.getPatientId());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
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
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                loadPendingOrders();
                Toast.makeText(this, "Pending orders refreshed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always refresh when returning to this activity
        // This ensures the list updates when patients complete meals
        loadPendingOrders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}