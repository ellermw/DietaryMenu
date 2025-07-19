package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    private EditText searchPatientEditText; // FIXED: Added search functionality
    private Spinner dateFilterSpinner;
    private ListView completedOrdersListView;
    private TextView noOrdersText;
    private Button clearSearchButton; // FIXED: Clear search button

    // Date handling
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEEE - MM/dd", Locale.getDefault());

    private List<DateOption> dateOptions = new ArrayList<>();
    private List<Patient> allCompletedPatients = new ArrayList<>(); // FIXED: Store all patients
    private List<Patient> filteredPatients = new ArrayList<>(); // FIXED: Store filtered results
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
        searchPatientEditText = findViewById(R.id.searchPatientEditText); // FIXED: Added search
        clearSearchButton = findViewById(R.id.clearSearchButton); // FIXED: Clear search button
        dateFilterSpinner = findViewById(R.id.dateFilterSpinner);
        completedOrdersListView = findViewById(R.id.completedOrdersListView);
        noOrdersText = findViewById(R.id.noOrdersText);

        // Set up list view click listener
        completedOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient selectedPatient = filteredPatients.get(position);
            showPatientOptionsDialog(selectedPatient);
        });

        // FIXED: Long click for delete option
        completedOrdersListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Patient selectedPatient = filteredPatients.get(position);
            showDeletePatientDialog(selectedPatient);
            return true;
        });
    }

    /**
     * FIXED: Show patient options when clicked
     */
    private void showPatientOptionsDialog(Patient patient) {
        String patientInfo = patient.getPatientFirstName() + " " + patient.getPatientLastName() +
                " - " + patient.getWing() + " Room " + patient.getRoomNumber();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Patient Options")
                .setMessage("What would you like to do with " + patientInfo + "?")
                .setPositiveButton("Edit Patient", (dialog, which) -> {
                    openPatientEdit(patient);
                })
                .setNeutralButton("View Details", (dialog, which) -> {
                    openPatientDetail(patient);
                })
                .setNegativeButton("Delete Patient", (dialog, which) -> {
                    showDeletePatientDialog(patient);
                })
                .show();
    }

    /**
     * FIXED: Show delete confirmation dialog
     */
    private void showDeletePatientDialog(Patient patient) {
        String patientInfo = patient.getPatientFirstName() + " " + patient.getPatientLastName() +
                " - " + patient.getWing() + " Room " + patient.getRoomNumber();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Delete Patient")
                .setMessage("Are you sure you want to permanently delete " + patientInfo +
                        "?\n\nThis will remove:\n• Patient information\n• All meal orders\n• Order history\n\nThis action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePatient(patient);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * FIXED: Delete patient and refresh list
     */
    private void deletePatient(Patient patient) {
        try {
            boolean success = patientDAO.deletePatient(patient.getPatientId());

            if (success) {
                Toast.makeText(this, "Patient " + patient.getPatientFirstName() + " " +
                        patient.getPatientLastName() + " has been deleted.", Toast.LENGTH_LONG).show();

                // Refresh the list
                loadCompletedOrders();
            } else {
                Toast.makeText(this, "Failed to delete patient. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error deleting patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * FIXED: Open patient edit functionality
     */
    private void openPatientEdit(Patient patient) {
        Intent intent = new Intent(this, PatientInfoActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.putExtra("edit_patient_id", patient.getPatientId());
        startActivity(intent);
    }

    private void openPatientDetail(Patient patient) {
        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.putExtra("patient_id", patient.getPatientId());
        startActivity(intent);
    }

    private void setupDateOptions() {
        dateOptions.clear();
        Calendar cal = Calendar.getInstance();

        // Add "Today" option
        String today = dbDateFormat.format(cal.getTime());
        String todayDisplay = "Today - " + displayDateFormat.format(cal.getTime());
        dateOptions.add(new DateOption(todayDisplay, today));
        selectedDate = today; // Default to today

        // Add previous 6 days
        for (int i = 1; i <= 6; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String date = dbDateFormat.format(cal.getTime());
            String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.getTime());
            String dateDisplay = dayName + " - " + displayDateFormat.format(cal.getTime());
            dateOptions.add(new DateOption(dateDisplay, date));
        }

        // FIXED: Setup spinner adapter properly
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
        // FIXED: Date filter spinner listener
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

        // FIXED: Search functionality
        searchPatientEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // FIXED: Clear search button
        clearSearchButton.setOnClickListener(v -> {
            searchPatientEditText.setText("");
            filterPatients("");
        });
    }

    /**
     * FIXED: Filter patients based on search term
     */
    private void filterPatients(String searchTerm) {
        filteredPatients.clear();

        if (searchTerm.trim().isEmpty()) {
            filteredPatients.addAll(allCompletedPatients);
        } else {
            String lowerSearchTerm = searchTerm.toLowerCase().trim();
            for (Patient patient : allCompletedPatients) {
                if (matchesSearchTerm(patient, lowerSearchTerm)) {
                    filteredPatients.add(patient);
                }
            }
        }

        updatePatientsList();
    }

    /**
     * FIXED: Check if patient matches search term
     */
    private boolean matchesSearchTerm(Patient patient, String searchTerm) {
        return patient.getPatientFirstName().toLowerCase().contains(searchTerm) ||
                patient.getPatientLastName().toLowerCase().contains(searchTerm) ||
                patient.getWing().toLowerCase().contains(searchTerm) ||
                patient.getRoomNumber().contains(searchTerm) ||
                (patient.getPatientFirstName() + " " + patient.getPatientLastName()).toLowerCase().contains(searchTerm);
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
                // Archive old orders
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

    /**
     * FIXED: Load completed orders and store in both lists
     */
    private void loadCompletedOrders() {
        try {
            // Get completed orders for selected date
            allCompletedPatients = patientDAO.getCompletedOrdersByDate(selectedDate);

            // Sort by Wing then Room Number
            Collections.sort(allCompletedPatients, (p1, p2) -> {
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
                    return p1.getRoomNumber().compareTo(p2.getRoomNumber());
                }
            });

            // Apply current search filter
            String currentSearch = searchPatientEditText.getText().toString();
            filterPatients(currentSearch);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading completed orders", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * FIXED: Update the patients list view
     */
    private void updatePatientsList() {
        if (filteredPatients.isEmpty()) {
            completedOrdersListView.setVisibility(View.GONE);
            noOrdersText.setVisibility(View.VISIBLE);

            String searchTerm = searchPatientEditText.getText().toString().trim();
            if (!searchTerm.isEmpty()) {
                noOrdersText.setText("No patients found matching \"" + searchTerm + "\"" +
                        "\n\nTry a different search term or clear the search.");
            } else {
                DateOption selectedOption = dateOptions.get(dateFilterSpinner.getSelectedItemPosition());
                noOrdersText.setText("No completed orders found for " + selectedOption.displayName +
                        "\n\nTry selecting a different date or complete some pending orders.");
            }

            // Update title with count
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Completed Orders (0)");
            }
        } else {
            completedOrdersListView.setVisibility(View.VISIBLE);
            noOrdersText.setVisibility(View.GONE);

            // Update title with count
            if (getSupportActionBar() != null) {
                String searchInfo = searchPatientEditText.getText().toString().trim().isEmpty() ?
                        "" : " (filtered)";
                getSupportActionBar().setTitle("Completed Orders (" + filteredPatients.size() + searchInfo + ")");
            }

            // Create adapter
            patientsAdapter = new ArrayAdapter<Patient>(this,
                    android.R.layout.simple_list_item_2, android.R.id.text1, filteredPatients) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
                    }

                    Patient patient = getItem(position);
                    TextView primaryText = convertView.findViewById(android.R.id.text1);
                    TextView secondaryText = convertView.findViewById(android.R.id.text2);

                    String patientName = patient.getPatientFirstName() + " " + patient.getPatientLastName();
                    String locationInfo = patient.getWing() + " - Room " + patient.getRoomNumber() +
                            " | Diet: " + patient.getDiet();

                    primaryText.setText(patientName);
                    secondaryText.setText(locationInfo);

                    return convertView;
                }
            };

            completedOrdersListView.setAdapter(patientsAdapter);
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
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                loadCompletedOrders();
                Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from edit
        loadCompletedOrders();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * Date option helper class
     */
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