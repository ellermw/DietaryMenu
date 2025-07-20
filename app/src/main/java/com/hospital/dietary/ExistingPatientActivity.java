package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.dao.FinalizedOrderDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.FinalizedOrder;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExistingPatientActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private FinalizedOrderDAO finalizedOrderDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Spinner dayFilterSpinner;
    private EditText searchInput;
    private ListView patientsListView;
    private TextView patientsCountText;
    private Button printMenusButton;
    private Button deleteSelectedButton;
    private CheckBox selectAllCheckBox;

    // Data
    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private PatientAdapter patientsAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

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
        finalizedOrderDAO = new FinalizedOrderDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Active Patients");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupDayFilter();
        setupListeners();
        loadPatients();
    }

    private void initializeUI() {
        dayFilterSpinner = findViewById(R.id.dayFilterSpinner);
        searchInput = findViewById(R.id.searchInput);
        patientsListView = findViewById(R.id.patientsListView);
        patientsCountText = findViewById(R.id.patientsCountText);
        printMenusButton = findViewById(R.id.printMenusButton);
        deleteSelectedButton = findViewById(R.id.deleteSelectedButton);
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);
    }

    private void setupDayFilter() {
        // Create day options with dates - Today, Tomorrow, and 5 days before today in chronological order
        List<String> dayOptions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // Add Today
        dayOptions.add("Today - " + dateFormat.format(calendar.getTime()));

        // Add Tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        dayOptions.add("Tomorrow - " + dateFormat.format(calendar.getTime()));

        // Add the 5 days BEFORE today in chronological order (oldest to newest)
        // Start from 5 days ago and work forward to yesterday
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -5); // Start 5 days ago

        for (int i = 0; i < 5; i++) {
            String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
            String dateStr = dateFormat.format(calendar.getTime());
            dayOptions.add(dayName + " - " + dateStr);

            calendar.add(Calendar.DAY_OF_MONTH, 1); // Move forward one day
        }

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayOptions);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayFilterSpinner.setAdapter(dayAdapter);
    }

    private void setupListeners() {
        // Day filter listener
        dayFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterPatients();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Search listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Patient list click listener
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient selectedPatient = filteredPatients.get(position);
            openPatientDetails(selectedPatient);
        });

        // Bulk operation listeners
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (patientsAdapter != null) {
                patientsAdapter.selectAll(isChecked);
                updateBulkOperationVisibility();
            }
        });

        printMenusButton.setOnClickListener(v -> printSelectedMenus());
        deleteSelectedButton.setOnClickListener(v -> deleteSelectedPatients());
    }

    private void loadPatients() {
        try {
            allPatients.clear();
            // FIXED: Load ALL patients, not just those with orders
            allPatients.addAll(patientDAO.getAllPatients());

            // Clear select all checkbox when reloading data
            if (selectAllCheckBox != null) {
                selectAllCheckBox.setChecked(false);
            }

            filterPatients();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // FIXED: Show all patients by default, with option to filter by day
    private void filterPatients() {
        filteredPatients.clear();

        String selectedDay = dayFilterSpinner.getSelectedItem().toString();
        String searchTerm = searchInput.getText().toString().toLowerCase().trim();

        // Extract date from selected day
        String selectedDate = getDateFromDayOption(selectedDay);

        for (Patient patient : allPatients) {
            boolean matchesSearch = searchTerm.isEmpty() ||
                    patient.getPatientFirstName().toLowerCase().contains(searchTerm) ||
                    patient.getPatientLastName().toLowerCase().contains(searchTerm) ||
                    patient.getWing().toLowerCase().contains(searchTerm) ||
                    patient.getRoomNumber().toLowerCase().contains(searchTerm);

            // FIXED: Show all patients that match search, don't filter by orders
            if (matchesSearch) {
                filteredPatients.add(patient);
            }
        }

        // FIXED: Update adapter properly
        updatePatientsAdapter();
        updatePatientsCount();
        updateBulkOperationVisibility();
    }

    private void updatePatientsAdapter() {
        if (patientsAdapter == null) {
            patientsAdapter = new PatientAdapter(this, filteredPatients);
            patientsListView.setAdapter(patientsAdapter);
        } else {
            // Clear selections when data changes to avoid out-of-bounds issues
            patientsAdapter.clearSelections();
            patientsAdapter.updatePatients(filteredPatients);
            patientsAdapter.notifyDataSetChanged();
        }
    }

    private String getDateFromDayOption(String dayOption) {
        // Extract date from format like "Today - 01/15" or "Monday - 01/16"
        String[] parts = dayOption.split(" - ");
        if (parts.length == 2) {
            String datePart = parts[1];
            // Convert MM/dd to yyyy-MM-dd format for database comparison
            Calendar cal = Calendar.getInstance();
            String year = String.valueOf(cal.get(Calendar.YEAR));
            return year + "-" + datePart.replace("/", "-");
        }
        return "";
    }

    private boolean hasOrderForDate(Patient patient, String date) {
        // Check if patient has any orders (pending or finished) for the specified date
        try {
            List<FinalizedOrder> orders = finalizedOrderDAO.getOrdersByWingRoomAndDate(
                    patient.getWing(), patient.getRoomNumber(), date);
            return !orders.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void updatePatientsCount() {
        if (patientsCountText != null) {
            patientsCountText.setText("Active Patients: " + filteredPatients.size() + " of " + allPatients.size());
        }
    }

    private void openPatientDetails(Patient patient) {
        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("patient_id", patient.getPatientId());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void printSelectedMenus() {
        if (patientsAdapter == null) return;

        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "Please select patients to print menus", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedDay = dayFilterSpinner.getSelectedItem().toString();

        new AlertDialog.Builder(this)
                .setTitle("Print Menus")
                .setMessage("Print menus for " + selectedPatients.size() + " selected patients for " + selectedDay + "?")
                .setPositiveButton("Print", (dialog, which) -> {
                    // Here you would implement actual printing logic
                    // For now, just show a confirmation
                    Toast.makeText(this, "Printing menus for " + selectedPatients.size() + " patients...", Toast.LENGTH_LONG).show();

                    // You could start a print service or generate PDF here
                    // Example: startPrintService(selectedPatients, selectedDay);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSelectedPatients() {
        if (patientsAdapter == null) return;

        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "Please select patients to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build confirmation message with patient names
        StringBuilder message = new StringBuilder("Are you sure you want to delete the following " +
                selectedPatients.size() + " patient(s)?\n\n");

        for (Patient patient : selectedPatients) {
            message.append("• ").append(patient.getPatientFirstName())
                    .append(" ").append(patient.getPatientLastName())
                    .append(" (").append(patient.getWing())
                    .append(" Room ").append(patient.getRoomNumber()).append(")\n");
        }

        message.append("\nThis action cannot be undone.");

        new AlertDialog.Builder(this)
                .setTitle("Delete Patients")
                .setMessage(message.toString())
                .setPositiveButton("Delete", (dialog, which) -> {
                    int deletedCount = 0;
                    int failedCount = 0;

                    for (Patient patient : selectedPatients) {
                        try {
                            if (patientDAO.deletePatient(patient.getPatientId())) {
                                deletedCount++;
                            } else {
                                failedCount++;
                            }
                        } catch (Exception e) {
                            failedCount++;
                            e.printStackTrace();
                        }
                    }

                    // Show result message
                    String resultMessage;
                    if (failedCount == 0) {
                        resultMessage = "Successfully deleted " + deletedCount + " patient(s)";
                    } else {
                        resultMessage = "Deleted " + deletedCount + " patient(s), failed to delete " + failedCount;
                    }

                    Toast.makeText(this, resultMessage, Toast.LENGTH_LONG).show();

                    // Refresh the list and clear selections
                    loadPatients();
                    selectAllCheckBox.setChecked(false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBulkOperationVisibility() {
        boolean hasSelected = patientsAdapter != null && patientsAdapter.hasSelectedItems();

        if (deleteSelectedButton != null) {
            deleteSelectedButton.setVisibility(hasSelected ? View.VISIBLE : View.GONE);
        }

        if (printMenusButton != null) {
            printMenusButton.setVisibility(hasSelected ? View.VISIBLE : View.GONE);
        }
    }

    // Custom adapter for patient list with selection capability
    private class PatientAdapter extends BaseAdapter {
        private List<Patient> patients;
        private Set<Integer> selectedPositions = new HashSet<>();

        public PatientAdapter(ExistingPatientActivity context, List<Patient> patients) {
            this.patients = patients;
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
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            }

            Patient patient = patients.get(position);
            CheckedTextView textView = (CheckedTextView) convertView;

            String displayText = patient.getPatientFirstName() + " " + patient.getPatientLastName() +
                    " - " + patient.getWing() + " Room " + patient.getRoomNumber() +
                    " (" + patient.getDiet() + ")";

            textView.setText(displayText);
            textView.setChecked(selectedPositions.contains(position));

            textView.setOnClickListener(v -> {
                CheckedTextView ctv = (CheckedTextView) v;
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                    ctv.setChecked(false);
                } else {
                    selectedPositions.add(position);
                    ctv.setChecked(true);
                }
                updateBulkOperationVisibility();
            });

            return convertView;
        }

        public void selectAll(boolean select) {
            selectedPositions.clear();
            if (select) {
                for (int i = 0; i < patients.size(); i++) {
                    selectedPositions.add(i);
                }
            }
            notifyDataSetChanged();
            updateBulkOperationVisibility();
        }

        public void clearSelections() {
            selectedPositions.clear();
            updateBulkOperationVisibility();
        }

        public void updatePatients(List<Patient> newPatients) {
            this.patients = newPatients;
        }

        public boolean hasSelectedItems() {
            return !selectedPositions.isEmpty();
        }

        public List<Patient> getSelectedPatients() {
            List<Patient> selected = new ArrayList<>();
            for (int position : selectedPositions) {
                if (position < patients.size()) {
                    selected.add(patients.get(position));
                }
            }
            return selected;
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
                loadPatients();
                Toast.makeText(this, "Patient list refreshed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // FIXED: Refresh patient list when returning from other activities
        loadPatients();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}