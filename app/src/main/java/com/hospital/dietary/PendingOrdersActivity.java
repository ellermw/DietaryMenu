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
            // Get patients who have incomplete meal orders
            pendingPatients = patientDAO.getPendingPatients();

            if (pendingPatients.isEmpty()) {
                pendingOrdersListView.setVisibility(View.GONE);
                noPendingOrdersText.setVisibility(View.VISIBLE);
                noPendingOrdersText.setText("✅ No pending orders!\nAll patients have completed meal orders.");

                // Update title with count
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Pending Orders (0)");
                }
            } else {
                pendingOrdersListView.setVisibility(View.VISIBLE);
                noPendingOrdersText.setVisibility(View.GONE);

                // Update title with count
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Pending Orders (" + pendingPatients.size() + ")");
                }

                // Create adapter for pending orders list
                pendingAdapter = new ArrayAdapter<Patient>(this,
                        android.R.layout.simple_list_item_2, android.R.id.text1, pendingPatients) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
                        }

                        Patient patient = getItem(position);
                        TextView text1 = convertView.findViewById(android.R.id.text1);
                        TextView text2 = convertView.findViewById(android.R.id.text2);

                        text1.setText(patient.getPatientFirstName() + " " + patient.getPatientLastName() +
                                " - " + patient.getWing() + " Room " + patient.getRoomNumber());

                        // Show which meals are pending
                        StringBuilder status = new StringBuilder("Pending: ");
                        if (!patient.isBreakfastComplete()) status.append("Breakfast ");
                        if (!patient.isLunchComplete()) status.append("Lunch ");
                        if (!patient.isDinnerComplete()) status.append("Dinner ");

                        status.append("| Diet: ").append(patient.getDiet());

                        text2.setText(status.toString().trim());

                        return convertView;
                    }
                };

                pendingOrdersListView.setAdapter(pendingAdapter);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading pending orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openMealPlanning(Patient patient) {
        Intent intent = new Intent(this, MealPlanningActivity.class);
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

    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}