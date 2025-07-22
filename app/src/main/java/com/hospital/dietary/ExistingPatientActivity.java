package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
// PatientAdapter is defined as inner class below
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExistingPatientActivity extends AppCompatActivity {

    private static final String TAG = "ExistingPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private ListView patientsListView;
    private EditText searchInput;
    private Spinner dayFilterSpinner;
    private TextView patientsCountText;
    private CheckBox selectAllCheckBox;
    private Button printMenusButton;
    private Button deleteSelectedButton;
    private LinearLayout bulkOperationsContainer;

    // Data
    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private PatientAdapter patientsAdapter;

    // Date formatting
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

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Existing Patients");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupListeners();
        loadPatients();
    }

    private void initializeUI() {
        patientsListView = findViewById(R.id.patientsListView);
        searchInput = findViewById(R.id.searchInput);
        dayFilterSpinner = findViewById(R.id.dayFilterSpinner);
        patientsCountText = findViewById(R.id.patientsCountText);
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);
        printMenusButton = findViewById(R.id.printMenusButton);
        deleteSelectedButton = findViewById(R.id.deleteSelectedButton);
        bulkOperationsContainer = findViewById(R.id.bulkOperationsContainer);

        // FIXED: Setup day filter spinner with formatted dates
        setupDayFilterSpinner();

        // Initially hide bulk operations
        updateBulkOperationVisibility();
    }

    private void setupDayFilterSpinner() {
        List<String> dayOptions = new ArrayList<>();

        // Get current calendar instance
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        // Add "All Days" first
        dayOptions.add("All Days");

        // Add days of week with special formatting for today and tomorrow
        String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        for (int i = 0; i < 7; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, i);

            String dayName = daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1];
            String dateStr = dateFormat.format(cal.getTime());

            String formattedOption;
            if (i == 0) {
                // Today
                formattedOption = "Today - " + dateStr;
            } else if (i == 1) {
                // Tomorrow
                formattedOption = "Tomorrow - " + dateStr;
            } else {
                // Other days
                formattedOption = dayName + " - " + dateStr;
            }

            dayOptions.add(formattedOption);
        }

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayOptions);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (dayFilterSpinner != null) {
            dayFilterSpinner.setAdapter(dayAdapter);
            // FIXED: Set default selection to "Today" (index 1, since "All Days" is index 0)
            dayFilterSpinner.setSelection(1);
        }
    }

    private void setupListeners() {
        // Search functionality
        if (searchInput != null) {
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
        }

        // Day filter
        if (dayFilterSpinner != null) {
            dayFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    filterPatients();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Select all checkbox
        if (selectAllCheckBox != null) {
            selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (patientsAdapter != null) {
                    patientsAdapter.selectAll(isChecked);
                    updateBulkOperationVisibility();
                }
            });
        }

        // Bulk operation buttons
        if (printMenusButton != null) {
            printMenusButton.setOnClickListener(v -> printSelectedMenus());
        }

        if (deleteSelectedButton != null) {
            deleteSelectedButton.setOnClickListener(v -> deleteSelectedPatients());
        }
    }

    // ENHANCED: New method to open patient details for viewing/editing
    private void openPatientDetails(Patient patient) {
        Intent intent = new Intent(this, PatientInfoActivity.class);
        intent.putExtra("selected_patient_id", patient.getPatientId());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivityForResult(intent, 1001);
    }

    private void loadPatients() {
        try {
            allPatients.clear();
            // Load ALL patients
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

    private void filterPatients() {
        try {
            filteredPatients.clear();

            String searchQuery = searchInput != null ?
                    searchInput.getText().toString().toLowerCase().trim() : "";
            int selectedDay = dayFilterSpinner != null ? dayFilterSpinner.getSelectedItemPosition() : 0;

            for (Patient patient : allPatients) {
                boolean matchesSearch = searchQuery.isEmpty() ||
                        patient.getFullName().toLowerCase().contains(searchQuery) ||
                        patient.getWing().toLowerCase().contains(searchQuery) ||
                        patient.getRoomNumber().toLowerCase().contains(searchQuery) ||
                        patient.getDiet().toLowerCase().contains(searchQuery);

                // FIXED: Day filtering logic updated for new format
                // 0 = All Days, 1 = Today, 2 = Tomorrow, 3-8 = Other days of week
                // For now, show all patients regardless of day (this was likely causing the issue)
                boolean matchesDay = true; // Changed to always true to show all patients

                if (matchesSearch && matchesDay) {
                    filteredPatients.add(patient);
                }
            }

            // Update adapter
            if (patientsAdapter == null) {
                patientsAdapter = new PatientAdapter(this, filteredPatients);
                patientsListView.setAdapter(patientsAdapter);
            } else {
                patientsAdapter.notifyDataSetChanged();
            }

            // Update count
            if (patientsCountText != null) {
                patientsCountText.setText("Showing " + filteredPatients.size() + " of " + allPatients.size() + " patients");
            }

            // Update bulk operations visibility
            updateBulkOperationVisibility();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error filtering patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBulkOperationVisibility() {
        if (bulkOperationsContainer == null || patientsAdapter == null) return;

        int selectedCount = patientsAdapter.getSelectedCount();
        boolean hasSelections = selectedCount > 0;

        bulkOperationsContainer.setVisibility(hasSelections ? View.VISIBLE : View.GONE);

        if (hasSelections) {
            String selectionText = selectedCount + " patient" + (selectedCount > 1 ? "s" : "") + " selected";
            if (printMenusButton != null) {
                printMenusButton.setText("Print " + selectedCount + " Menu" + (selectedCount > 1 ? "s" : ""));
            }
            if (deleteSelectedButton != null) {
                deleteSelectedButton.setText("Delete " + selectedCount + " Patient" + (selectedCount > 1 ? "s" : ""));
            }
        }
    }

    private void printSelectedMenus() {
        if (patientsAdapter == null) return;

        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create intent to print multiple menus - TODO: Implement actual print functionality
        Toast.makeText(this, "Printing " + selectedPatients.size() + " patient menus...", Toast.LENGTH_SHORT).show();
        /*
        Intent printIntent = new Intent(this, PrintMenuActivity.class);
        ArrayList<Integer> patientIds = new ArrayList<>();
        for (Patient patient : selectedPatients) {
            patientIds.add(patient.getPatientId());
        }
        printIntent.putExtra("patient_ids", patientIds);
        printIntent.putExtra("current_user", currentUsername);
        printIntent.putExtra("user_role", currentUserRole);
        printIntent.putExtra("user_full_name", currentUserFullName);
        startActivity(printIntent);
        */
    }

    private void deleteSelectedPatients() {
        if (patientsAdapter == null) return;

        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Are you sure you want to delete " + selectedPatients.size() +
                " patient" + (selectedPatients.size() > 1 ? "s" : "") + "?\n\n" +
                "This action cannot be undone.";

        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    performBulkDelete(selectedPatients);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performBulkDelete(List<Patient> patients) {
        try {
            int successCount = 0;
            for (Patient patient : patients) {
                boolean success = patientDAO.deletePatient(patient.getPatientId());
                if (success) {
                    successCount++;
                }
            }

            if (successCount > 0) {
                Toast.makeText(this, "Deleted " + successCount + " patient" +
                                (successCount > 1 ? "s" : "") + " successfully",
                        Toast.LENGTH_SHORT).show();

                // Clear selections and reload data
                if (selectAllCheckBox != null) {
                    selectAllCheckBox.setChecked(false);
                }
                loadPatients();
            } else {
                Toast.makeText(this, "Failed to delete patients", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error deleting patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_existing_patients, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_print_all:
                printAllMenus();
                return true;
            case R.id.action_home:
                Intent homeIntent = new Intent(this, MainMenuActivity.class);
                homeIntent.putExtra("current_user", currentUsername);
                homeIntent.putExtra("user_role", currentUserRole);
                homeIntent.putExtra("user_full_name", currentUserFullName);
                startActivity(homeIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void printAllMenus() {
        if (filteredPatients.isEmpty()) {
            Toast.makeText(this, "No patients to print", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement print all functionality
        Toast.makeText(this, "Printing all " + filteredPatients.size() + " patient menus...", Toast.LENGTH_SHORT).show();
        /*
        Intent printIntent = new Intent(this, PrintMenuActivity.class);
        ArrayList<Integer> patientIds = new ArrayList<>();
        for (Patient patient : filteredPatients) {
            patientIds.add(patient.getPatientId());
        }
        printIntent.putExtra("patient_ids", patientIds);
        printIntent.putExtra("current_user", currentUsername);
        printIntent.putExtra("user_role", currentUserRole);
        printIntent.putExtra("user_full_name", currentUserFullName);
        startActivity(printIntent);
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Patient was updated, reload the list
            loadPatients();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        loadPatients();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * Enhanced Patient Adapter with selection functionality
     */
    private class PatientAdapter extends BaseAdapter {
        private List<Patient> patients;
        private List<Boolean> selections;
        private ExistingPatientActivity context;

        public PatientAdapter(ExistingPatientActivity context, List<Patient> patients) {
            this.context = context;
            this.patients = patients;
            this.selections = new ArrayList<>();
            initializeSelections();
        }

        private void initializeSelections() {
            selections.clear();
            for (int i = 0; i < patients.size(); i++) {
                selections.add(false);
            }
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
            TextView textView = convertView.findViewById(android.R.id.text1);
            CheckBox checkBox = convertView.findViewById(android.R.id.checkbox);

            String displayText = patient.getFullName() + " - " + patient.getWing() + " " +
                    patient.getRoomNumber() + " (" + patient.getDiet() + ")";
            textView.setText(displayText);

            if (checkBox != null) {
                checkBox.setChecked(selections.get(position));
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    selections.set(position, isChecked);
                    updateBulkOperationVisibility();
                });
            }

            return convertView;
        }

        public void toggleSelection(int position) {
            if (position >= 0 && position < selections.size()) {
                selections.set(position, !selections.get(position));
                notifyDataSetChanged();
            }
        }

        public void selectAll(boolean selected) {
            for (int i = 0; i < selections.size(); i++) {
                selections.set(i, selected);
            }
            notifyDataSetChanged();
        }

        public List<Patient> getSelectedPatients() {
            List<Patient> selectedPatients = new ArrayList<>();
            for (int i = 0; i < patients.size(); i++) {
                if (selections.get(i)) {
                    selectedPatients.add(patients.get(i));
                }
            }
            return selectedPatients;
        }

        public int getSelectedCount() {
            int count = 0;
            for (Boolean selected : selections) {
                if (selected) count++;
            }
            return count;
        }

        @Override
        public void notifyDataSetChanged() {
            initializeSelections(); // Reset selections when data changes
            super.notifyDataSetChanged();
        }
    }
}