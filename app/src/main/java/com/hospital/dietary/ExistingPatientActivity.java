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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExistingPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Spinner dateFilterSpinner;
    private ListView completedOrdersListView;
    private TextView noOrdersText;

    // Date handling
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEEE - MM/dd", Locale.getDefault());

    private List<DateOption> dateOptions = new ArrayList<>();
    private List<Patient> completedPatients = new ArrayList<>();
    private ArrayAdapter<Patient> patientsAdapter;
    private String selectedDate;

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

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Completed Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupDateOptions();
        setupListeners();
        performAutoCleanup();
        loadCompletedOrders();
    }

    private void initializeUI() {
        dateFilterSpinner = findViewById(R.id.dateFilterSpinner);
        completedOrdersListView = findViewById(R.id.completedOrdersListView);
        noOrdersText = findViewById(R.id.noOrdersText);

        // Set up list view click listener
        completedOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient selectedPatient = completedPatients.get(position);
            openPatientDetail(selectedPatient);
        });
    }

    private void setupDateOptions() {
        dateOptions.clear();
        Calendar cal = Calendar.getInstance();

        // Add "Today" option
        String today = dbDateFormat.format(cal.getTime());
        String todayDisplay = "Today - " + displayDateFormat.format(cal.getTime());
        dateOptions.add(new DateOption(todayDisplay, today));
        selectedDate = today; // Default to today

        // Add previous 6 days (current week + previous few days)
        for (int i = 1; i <= 6; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String date = dbDateFormat.format(cal.getTime());
            String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.getTime());
            String dateDisplay = dayName + " - " + displayDateFormat.format(cal.getTime());
            dateOptions.add(new DateOption(dateDisplay, date));
        }

        // Setup spinner adapter
        ArrayAdapter<DateOption> dateAdapter = new ArrayAdapter<DateOption>(this,
                android.R.layout.simple_spinner_item, dateOptions) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setPadding(16, 12, 16, 12);
                return view;
            }
        };
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateFilterSpinner.setAdapter(dateAdapter);
    }

    private void setupListeners() {
        dateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DateOption selectedOption = dateOptions.get(position);
                selectedDate = selectedOption.dateValue;
                loadCompletedOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void performAutoCleanup() {
        try {
            // Calculate date 6 days ago
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -6);
            String cutoffDate = dbDateFormat.format(cal.getTime());

            // Get old orders and move them to retired
            List<Patient> oldOrders = patientDAO.getOrdersBeforeDate(cutoffDate);

            if (!oldOrders.isEmpty()) {
                // Archive old orders (this would typically move them to a separate retired table)
                int archivedCount = patientDAO.archiveOldOrders(cutoffDate);

                if (archivedCount > 0) {
                    Toast.makeText(this, archivedCount + " old orders archived to Retired Orders",
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCompletedOrders() {
        try {
            // Get completed orders for selected date
            completedPatients = patientDAO.getCompletedOrdersByDate(selectedDate);

            // Sort by Wing then Room Number
            Collections.sort(completedPatients, (p1, p2) -> {
                // First sort by wing
                int wingCompare = p1.getWing().compareTo(p2.getWing());
                if (wingCompare != 0) {
                    return wingCompare;
                }

                // Then sort by room number (numeric)
                try {
                    int room1 = Integer.parseInt(p1.getRoomNumber().replaceAll("\\D", ""));
                    int room2 = Integer.parseInt(p2.getRoomNumber().replaceAll("\\D", ""));
                    return Integer.compare(room1, room2);
                } catch (NumberFormatException e) {
                    // If parsing fails, use string comparison
                    return p1.getRoomNumber().compareTo(p2.getRoomNumber());
                }
            });

            // Update UI
            if (completedPatients.isEmpty()) {
                completedOrdersListView.setVisibility(View.GONE);
                noOrdersText.setVisibility(View.VISIBLE);

                DateOption selectedOption = dateOptions.get(dateFilterSpinner.getSelectedItemPosition());
                noOrdersText.setText("No completed orders found for " + selectedOption.displayName +
                        "\n\nTry selecting a different date or complete some pending orders.");

                // Update title with count
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Completed Orders (0)");
                }
            } else {
                completedOrdersListView.setVisibility(View.VISIBLE);
                noOrdersText.setVisibility(View.GONE);

                // Update title with count
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Completed Orders (" + completedPatients.size() + ")");
                }

                // Create adapter
                patientsAdapter = new ArrayAdapter<Patient>(this,
                        android.R.layout.simple_list_item_2, android.R.id.text1, completedPatients) {
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

                        StringBuilder status = new StringBuilder("Diet: " + patient.getDiet());
                        if (patient.getFluidRestriction() != null) {
                            status.append(" | Fluid: ").append(patient.getFluidRestriction());
                        }
                        status.append(" | ").append(patient.getMealCompletionStatus());

                        text2.setText(status.toString());

                        return convertView;
                    }
                };

                completedOrdersListView.setAdapter(patientsAdapter);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openPatientDetail(Patient patient) {
        // For now, show patient details in a dialog since PatientDetailActivity might not exist
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
                .setTitle("Patient Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
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
                // Go back to Patient Info Menu, not Main Menu
                Intent intent = new Intent(this, PatientInfoMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_home:
                goToMainMenu();
                return true;
            case R.id.action_refresh:
                performAutoCleanup();
                loadCompletedOrders();
                Toast.makeText(this, "Orders refreshed and cleaned up", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh when returning to this activity
        performAutoCleanup();
        loadCompletedOrders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // Helper class for date options
    private static class DateOption {
        String displayName;
        String dateValue;

        DateOption(String displayName, String dateValue) {
            this.displayName = displayName;
            this.dateValue = dateValue;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}