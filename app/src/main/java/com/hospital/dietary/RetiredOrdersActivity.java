package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RetiredOrdersActivity extends AppCompatActivity {

    private static final String TAG = "RetiredOrdersActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView retiredOrdersTitle;
    private TextView selectedDateText;
    private Button selectDateButton;
    private ListView retiredOrdersListView;
    private TextView noRetiredOrdersText;
    private Button printAllButton;
    private Button printSelectedButton;
    private Button backButton;
    private Button homeButton;

    // Data
    private List<Patient> retiredPatients = new ArrayList<>();
    private RetiredOrdersAdapter retiredAdapter;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retired_orders);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Retired Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupListeners();
        loadRetiredOrders();
    }

    private void initializeUI() {
        retiredOrdersTitle = findViewById(R.id.retiredOrdersTitle);
        selectedDateText = findViewById(R.id.selectedDateText);
        selectDateButton = findViewById(R.id.selectDateButton);
        retiredOrdersListView = findViewById(R.id.retiredOrdersListView);
        noRetiredOrdersText = findViewById(R.id.noRetiredOrdersText);
        printAllButton = findViewById(R.id.printAllButton);
        printSelectedButton = findViewById(R.id.printSelectedButton);
        backButton = findViewById(R.id.backButton);
        homeButton = findViewById(R.id.homeButton);

        // Initialize the list view
        retiredAdapter = new RetiredOrdersAdapter(this, retiredPatients);
        retiredOrdersListView.setAdapter(retiredAdapter);
        retiredOrdersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        updateSelectedDateDisplay();
    }

    private void setupListeners() {
        if (selectDateButton != null) {
            selectDateButton.setOnClickListener(v -> showDatePicker());
        }

        if (printAllButton != null) {
            printAllButton.setOnClickListener(v -> printAllOrders());
        }

        if (printSelectedButton != null) {
            printSelectedButton.setOnClickListener(v -> printSelectedOrders());
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }

        if (retiredOrdersListView != null) {
            retiredOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < retiredPatients.size()) {
                    Patient patient = retiredPatients.get(position);
                    showOrderDetails(patient);
                }
            });
        }
    }

    private void updateSelectedDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        if (selectedDateText != null) {
            selectedDateText.setText("Showing: " + dateFormat.format(selectedDate.getTime()));
        }
    }

    private void showDatePicker() {
        // Date picker implementation would go here
        // For now, just reload the current data
        loadRetiredOrders();
    }

    private void loadRetiredOrders() {
        try {
            retiredPatients.clear();

            // Get all patients
            List<Patient> allPatients = patientDAO.getAllPatients();

            // Calculate 6 days ago
            Calendar sixDaysAgo = Calendar.getInstance();
            sixDaysAgo.add(Calendar.DAY_OF_YEAR, -6);
            Date sixDaysAgoDate = sixDaysAgo.getTime();

            for (Patient patient : allPatients) {
                boolean shouldInclude = false;

                // Include if discharged
                if (patient.isDischarged()) {
                    shouldInclude = true;
                }
                // Or if created more than 6 days ago
                else if (patient.getCreatedDate() != null && patient.getCreatedDate().before(sixDaysAgoDate)) {
                    shouldInclude = true;
                }

                // Only include if patient has completed meals or is NPO
                if (shouldInclude) {
                    boolean hasCompletedMeals = patient.isBreakfastComplete() || patient.isLunchComplete() || patient.isDinnerComplete();
                    boolean isAllNPO = patient.isBreakfastNPO() && patient.isLunchNPO() && patient.isDinnerNPO();
                    boolean isFullyProcessed = (patient.isBreakfastComplete() || patient.isBreakfastNPO()) &&
                            (patient.isLunchComplete() || patient.isLunchNPO()) &&
                            (patient.isDinnerComplete() || patient.isDinnerNPO());

                    if (hasCompletedMeals || isAllNPO || isFullyProcessed) {
                        retiredPatients.add(patient);
                    }
                }
            }

            // Update UI
            if (retiredPatients.isEmpty()) {
                if (retiredOrdersListView != null) {
                    retiredOrdersListView.setVisibility(View.GONE);
                }
                if (noRetiredOrdersText != null) {
                    noRetiredOrdersText.setVisibility(View.VISIBLE);
                    noRetiredOrdersText.setText("📋 No retired orders found.\n\nRetired orders include:\n• Discharged patients\n• Patients created more than 6 days ago");
                }

                if (printAllButton != null) printAllButton.setEnabled(false);
                if (printSelectedButton != null) printSelectedButton.setEnabled(false);
            } else {
                if (retiredOrdersListView != null) {
                    retiredOrdersListView.setVisibility(View.VISIBLE);
                }
                if (noRetiredOrdersText != null) {
                    noRetiredOrdersText.setVisibility(View.GONE);
                }
                if (retiredAdapter != null) {
                    retiredAdapter.notifyDataSetChanged();
                }

                if (printAllButton != null) printAllButton.setEnabled(true);
                if (printSelectedButton != null) printSelectedButton.setEnabled(true);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading retired orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error loading retired orders", e);
        }
    }

    private void showOrderDetails(Patient patient) {
        StringBuilder details = new StringBuilder();

        details.append("Patient: ").append(patient.getFullName()).append("\n");
        details.append("Location: ").append(patient.getWing()).append(" - Room ").append(patient.getRoomNumber()).append("\n");

        if (patient.isDischarged()) {
            details.append("Status: DISCHARGED\n");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            details.append("Created: ").append(dateFormat.format(patient.getCreatedDate())).append("\n");
        }

        details.append("\n");

        // Show meal details
        details.append("BREAKFAST - ").append(patient.getBreakfastDiet()).append("\n");
        details.append("Status: ").append(patient.isBreakfastNPO() ? "NPO" :
                patient.isBreakfastComplete() ? "Complete" : "Incomplete").append("\n\n");

        details.append("LUNCH - ").append(patient.getLunchDiet()).append("\n");
        details.append("Status: ").append(patient.isLunchNPO() ? "NPO" :
                patient.isLunchComplete() ? "Complete" : "Incomplete").append("\n\n");

        details.append("DINNER - ").append(patient.getDinnerDiet()).append("\n");
        details.append("Status: ").append(patient.isDinnerNPO() ? "NPO" :
                patient.isDinnerComplete() ? "Complete" : "Incomplete").append("\n");

        // Show details dialog
        new android.app.AlertDialog.Builder(this)
                .setTitle("Order Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void printAllOrders() {
        if (retiredPatients.isEmpty()) {
            Toast.makeText(this, "No orders to print", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement print functionality
        Toast.makeText(this, "Printing all " + retiredPatients.size() + " orders...", Toast.LENGTH_SHORT).show();
    }

    private void printSelectedOrders() {
        android.util.SparseBooleanArray checkedItems = retiredOrdersListView.getCheckedItemPositions();
        int selectedCount = 0;

        for (int i = 0; i < checkedItems.size(); i++) {
            if (checkedItems.valueAt(i)) {
                selectedCount++;
            }
        }

        if (selectedCount == 0) {
            Toast.makeText(this, "Please select orders to print", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement print functionality
        Toast.makeText(this, "Printing " + selectedCount + " selected orders...", Toast.LENGTH_SHORT).show();
    }

    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_retired_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                loadRetiredOrders();
                return true;
            case R.id.action_home:
                goToMainMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // Custom adapter for retired orders
    private class RetiredOrdersAdapter extends ArrayAdapter<Patient> {

        public RetiredOrdersAdapter(android.content.Context context, List<Patient> patients) {
            super(context, android.R.layout.simple_list_item_multiple_choice, patients);
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            Patient patient = getItem(position);

            if (patient != null) {
                StringBuilder display = new StringBuilder();
                display.append(patient.getFullName());
                display.append(" - ").append(patient.getLocationInfo());

                if (patient.isDischarged()) {
                    display.append(" [DISCHARGED]");
                } else {
                    // Show days since creation
                    long daysSince = (new Date().getTime() - patient.getCreatedDate().getTime()) / (1000 * 60 * 60 * 24);
                    display.append(" [").append(daysSince).append(" days old]");
                }

                view.setText(display.toString());

                // Style discharged patients differently
                if (patient.isDischarged()) {
                    view.setTextColor(android.graphics.Color.parseColor("#e74c3c"));
                }
            }

            return view;
        }
    }
}