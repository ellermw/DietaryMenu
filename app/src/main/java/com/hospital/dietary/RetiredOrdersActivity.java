package com.hospital.dietary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
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
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components - only reference IDs that actually exist in the layout
    private Button datePickerButton; // This exists
    private TextView selectedDateText; // This exists
    private ListView retiredOrdersListView; // This exists
    private TextView noRetiredOrdersText; // This exists
    private Button printAllButton; // May or may not exist
    private Button printSelectedButton; // May or may not exist

    // Date handling
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat dateFormatShort = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());

    private List<Patient> retiredPatients = new ArrayList<>();
    private ArrayAdapter<Patient> retiredAdapter;

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

        // Set title and enable up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Retired Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize selected date to today
        selectedDate = Calendar.getInstance();
        selectedDate.add(Calendar.DAY_OF_MONTH, -1); // Default to yesterday

        initializeUI();
        setupListeners();
        updateSelectedDateDisplay();
        loadRetiredOrdersForDate();
    }

    private void initializeUI() {
        // FIXED: Use only the IDs that actually exist in the layout
        datePickerButton = findViewById(R.id.datePickerButton);
        selectedDateText = findViewById(R.id.selectedDateText);
        retiredOrdersListView = findViewById(R.id.retiredOrdersListView);
        noRetiredOrdersText = findViewById(R.id.noRetiredOrdersText);

        // These may or may not exist - handle gracefully
        printAllButton = findViewById(R.id.printAllButton);
        printSelectedButton = findViewById(R.id.printSelectedButton);

        // Set up list view for multiple selection if it exists
        if (retiredOrdersListView != null) {
            retiredOrdersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            // Set up the adapter
            retiredAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_multiple_choice, retiredPatients) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    Patient patient = getItem(position);
                    CheckedTextView textView = (CheckedTextView) view.findViewById(android.R.id.text1);

                    if (patient != null) {
                        String displayText = String.format("%s\n%s - Room %s | %s\nCompleted: %s",
                                patient.getFullName(),
                                patient.getWing(),
                                patient.getRoomNumber(),
                                patient.getDiet(),
                                getCompletedMealsText(patient));

                        textView.setText(displayText);
                    }

                    return view;
                }
            };

            retiredOrdersListView.setAdapter(retiredAdapter);
            retiredOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < retiredPatients.size()) {
                    Patient patient = retiredPatients.get(position);
                    showOrderDetails(patient);
                }
            });
        } else {
            Log.w(TAG, "retiredOrdersListView not found in layout");
        }
    }

    private String getCompletedMealsText(Patient patient) {
        List<String> completedMeals = new ArrayList<>();

        if (patient.isBreakfastComplete() || patient.isBreakfastNPO()) {
            completedMeals.add("B");
        }
        if (patient.isLunchComplete() || patient.isLunchNPO()) {
            completedMeals.add("L");
        }
        if (patient.isDinnerComplete() || patient.isDinnerNPO()) {
            completedMeals.add("D");
        }

        return String.join(", ", completedMeals);
    }

    private void setupListeners() {
        if (datePickerButton != null) {
            datePickerButton.setOnClickListener(v -> showDatePicker());
        } else {
            Log.w(TAG, "datePickerButton not found in layout");
        }

        if (printAllButton != null) {
            printAllButton.setOnClickListener(v -> printAllOrders());
        }

        if (printSelectedButton != null) {
            printSelectedButton.setOnClickListener(v -> printSelectedOrders());
        }
    }

    private void updateSelectedDateDisplay() {
        if (selectedDateText != null && selectedDate != null) {
            String displayDate = dateFormat.format(selectedDate.getTime());
            selectedDateText.setText("Viewing orders for: " + displayDate);
        }
    }

    private void showDatePicker() {
        Calendar currentDate = selectedDate != null ? selectedDate : Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    updateSelectedDateDisplay();
                    loadRetiredOrdersForDate();
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadRetiredOrdersForDate() {
        try {
            retiredPatients.clear();

            // Get all patients with completed meal orders
            List<Patient> allPatients = patientDAO.getAllPatients();

            for (Patient patient : allPatients) {
                // Check if patient has at least one completed meal or is all NPO
                boolean hasCompletedMeals = patient.isBreakfastComplete() || patient.isLunchComplete() || patient.isDinnerComplete();
                boolean isAllNPO = patient.isBreakfastNPO() && patient.isLunchNPO() && patient.isDinnerNPO();
                boolean isFullyProcessed = (patient.isBreakfastComplete() || patient.isBreakfastNPO()) &&
                        (patient.isLunchComplete() || patient.isLunchNPO()) &&
                        (patient.isDinnerComplete() || patient.isDinnerNPO());

                if (hasCompletedMeals || isAllNPO || isFullyProcessed) {
                    retiredPatients.add(patient);
                }
            }

            // Update UI
            if (retiredPatients.isEmpty()) {
                if (retiredOrdersListView != null) {
                    retiredOrdersListView.setVisibility(View.GONE);
                }
                if (noRetiredOrdersText != null) {
                    noRetiredOrdersText.setVisibility(View.VISIBLE);
                    noRetiredOrdersText.setText("📋 No orders found for selected date.\n\nTry choosing a different date from the calendar.");
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
            Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error loading orders", e);
        }
    }

    private void showOrderDetails(Patient patient) {
        StringBuilder details = new StringBuilder();

        // Add date header with day of week
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(selectedDate.getTime());

        details.append("=== ").append(dayOfWeek).append(" ===\n");
        details.append(dateHeader).append("\n\n");

        details.append("Patient: ").append(patient.getFullName()).append("\n");
        details.append("Location: ").append(patient.getWing()).append(" - Room ").append(patient.getRoomNumber()).append("\n\n");

        // FIXED: Show individual meal diets with proper formatting
        details.append("BREAKFAST - ").append(getCleanDietDisplay(patient.getBreakfastDiet(), patient.isBreakfastAda())).append("\n");
        details.append("Status: ").append(patient.isBreakfastNPO() ? "NPO" : "Complete").append("\n\n");

        details.append("LUNCH - ").append(getCleanDietDisplay(patient.getLunchDiet(), patient.isLunchAda())).append("\n");
        details.append("Status: ").append(patient.isLunchNPO() ? "NPO" : "Complete").append("\n\n");

        details.append("DINNER - ").append(getCleanDietDisplay(patient.getDinnerDiet(), patient.isDinnerAda())).append("\n");
        details.append("Status: ").append(patient.isDinnerNPO() ? "NPO" : "Complete").append("\n\n");

        // Show texture modifications if any
        if (!patient.getTextureModifications().isEmpty() && !patient.getTextureModifications().equals("None")) {
            details.append("Texture Modifications: ").append(patient.getTextureModifications()).append("\n");
        }

        // Show fluid restriction if any
        if (!patient.getFluidRestriction().isEmpty() && !patient.getFluidRestriction().equals("None")) {
            details.append("Fluid Restriction: ").append(patient.getFluidRestriction()).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Order Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private String getCleanDietDisplay(String diet, boolean isAda) {
        if (diet == null || diet.isEmpty()) {
            return "Regular";
        }

        if (isAda && !diet.contains("(ADA)")) {
            return diet + " (ADA)";
        }

        return diet;
    }

    private void printAllOrders() {
        if (retiredPatients.isEmpty()) {
            Toast.makeText(this, "No orders to print", Toast.LENGTH_SHORT).show();
            return;
        }

        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());

        new AlertDialog.Builder(this)
                .setTitle("Print All Orders")
                .setMessage("Print all " + retiredPatients.size() + " orders for " + dayOfWeek + " " + dateHeader + "?")
                .setPositiveButton("Print", (dialog, which) -> {
                    // TODO: Implement actual printing functionality
                    Toast.makeText(this, "Print All Orders - " + retiredPatients.size() + " orders (" + dayOfWeek + " " + dateHeader + ")", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void printSelectedOrders() {
        if (retiredOrdersListView == null) {
            Toast.makeText(this, "List view not available", Toast.LENGTH_SHORT).show();
            return;
        }

        SparseBooleanArray checkedItems = retiredOrdersListView.getCheckedItemPositions();
        List<Patient> selectedPatients = new ArrayList<>();

        for (int i = 0; i < checkedItems.size(); i++) {
            int position = checkedItems.keyAt(i);
            if (checkedItems.valueAt(i)) {
                if (position < retiredPatients.size()) {
                    selectedPatients.add(retiredPatients.get(position));
                }
            }
        }

        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No orders selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());

        // TODO: Implement actual printing functionality
        Toast.makeText(this, "Print Selected Orders - " + selectedPatients.size() + " orders (" + dayOfWeek + " " + dateHeader + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Skip menu inflation if menu file doesn't exist - FIXED
        try {
            getMenuInflater().inflate(R.menu.menu_retired_orders, menu);
        } catch (Exception e) {
            Log.d(TAG, "Menu file not found, skipping menu inflation");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
}