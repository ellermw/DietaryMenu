package com.hospital.dietary;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExistingPatientsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Toolbar toolbar;
    private EditText searchInput;
    private Spinner dayFilterSpinner;
    private TextView patientsCountText;
    private ListView patientsListView;
    private LinearLayout bulkOperationsContainer;
    private CheckBox selectAllCheckBox;
    private Button printMenusButton;
    private Button deleteSelectedButton;
    private Button viewPatientDetailsButton;  // NEW

    // Data
    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private PatientAdapter patientsAdapter;
    private DayFilterAdapter dayFilterAdapter;

    // Date tracking
    private int todayIndex = -1;
    private List<String> dayLabels = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_patients);

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

        // Setup dates
        setupDayFilter();

        // Load patients
        loadPatients();

        // Setup listeners
        setupListeners();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Existing Patients");
        }
    }

    private void initializeUI() {
        searchInput = findViewById(R.id.searchInput);
        dayFilterSpinner = findViewById(R.id.dayFilterSpinner);
        patientsCountText = findViewById(R.id.patientsCountText);
        patientsListView = findViewById(R.id.patientsListView);
        bulkOperationsContainer = findViewById(R.id.bulkOperationsContainer);
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);
        printMenusButton = findViewById(R.id.printMenusButton);
        deleteSelectedButton = findViewById(R.id.deleteSelectedButton);
        viewPatientDetailsButton = findViewById(R.id.viewPatientDetailsButton);  // NEW

        // Setup patients adapter
        patientsAdapter = new PatientAdapter();
        patientsListView.setAdapter(patientsAdapter);
        patientsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void setupDayFilter() {
        // Setup 7 days including today
        Calendar calendar = Calendar.getInstance();
        dayLabels.clear();

        for (int i = 0; i < 7; i++) {
            if (i == 0) {
                dayLabels.add("Today (" + dateFormat.format(calendar.getTime()) + ")");
                todayIndex = i;
            } else if (i == 1) {
                dayLabels.add("Tomorrow (" + dateFormat.format(calendar.getTime()) + ")");
            } else {
                dayLabels.add(dateFormat.format(calendar.getTime()));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Add "All Days" option
        dayLabels.add(0, "All Days");

        // Setup adapter
        dayFilterAdapter = new DayFilterAdapter();
        dayFilterSpinner.setAdapter(dayFilterAdapter);
        dayFilterSpinner.setSelection(1); // Default to Today
    }

    private void setupListeners() {
        // Search input
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

        // Day filter
        dayFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterPatients();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Select all checkbox
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (patientsAdapter != null) {
                patientsAdapter.selectAll(isChecked);
                updateBulkOperationVisibility();
            }
        });

        // Patient list item click
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            // Update selection state
            if (patientsAdapter != null) {
                patientsAdapter.toggleSelection(position);
                updateBulkOperationVisibility();

                // Update select all checkbox state
                if (selectAllCheckBox != null) {
                    selectAllCheckBox.setChecked(patientsAdapter.areAllSelected());
                }
            }
        });

        // Long click for patient details
        patientsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Patient patient = filteredPatients.get(position);
            openPatientDetails(patient);
            return true;
        });

        // Bulk operation buttons
        if (printMenusButton != null) {
            printMenusButton.setOnClickListener(v -> printSelectedMenus());
        }

        if (deleteSelectedButton != null) {
            deleteSelectedButton.setOnClickListener(v -> deleteSelectedPatients());
        }

        // NEW: View Patient Details button
        if (viewPatientDetailsButton != null) {
            viewPatientDetailsButton.setOnClickListener(v -> {
                List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
                if (selectedPatients.size() == 1) {
                    openPatientDetails(selectedPatients.get(0));
                }
            });
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

    private void loadPatients() {
        try {
            allPatients.clear();
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
            int selectedDayIndex = dayFilterSpinner != null ?
                    dayFilterSpinner.getSelectedItemPosition() : 0;

            for (Patient patient : allPatients) {
                // Skip discharged patients
                if (patient.isDischarged()) continue;

                // Apply search filter
                boolean matchesSearch = searchQuery.isEmpty() ||
                        patient.getPatientFirstName().toLowerCase().contains(searchQuery) ||
                        patient.getPatientLastName().toLowerCase().contains(searchQuery) ||
                        patient.getWing().toLowerCase().contains(searchQuery) ||
                        patient.getRoomNumber().toLowerCase().contains(searchQuery) ||
                        patient.getDiet().toLowerCase().contains(searchQuery);

                if (matchesSearch) {
                    // Apply day filter (simplified for now - you can enhance this)
                    filteredPatients.add(patient);
                }
            }

            // Update UI
            patientsAdapter.notifyDataSetChanged();
            updatePatientCount();
            updateBulkOperationVisibility();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error filtering patients", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePatientCount() {
        if (patientsCountText != null) {
            int count = filteredPatients.size();
            patientsCountText.setText(count + " patient" + (count != 1 ? "s" : ""));
        }
    }

    private void updateBulkOperationVisibility() {
        int selectedCount = patientsAdapter != null ? patientsAdapter.getSelectedCount() : 0;
        boolean hasSelections = selectedCount > 0;

        if (bulkOperationsContainer != null) {
            bulkOperationsContainer.setVisibility(hasSelections ? View.VISIBLE : View.GONE);
        }

        // NEW: Update View Patient Details button
        if (viewPatientDetailsButton != null) {
            boolean showViewDetails = selectedCount == 1;
            viewPatientDetailsButton.setVisibility(showViewDetails ? View.VISIBLE : View.GONE);
            if (showViewDetails) {
                viewPatientDetailsButton.setText("View Patient Details");
            }
        }

        if (hasSelections) {
            if (printMenusButton != null) {
                printMenusButton.setText("Print " + selectedCount + " Menu" +
                        (selectedCount > 1 ? "s" : ""));
            }

            if (deleteSelectedButton != null) {
                deleteSelectedButton.setText("Delete " + selectedCount + " Patient" +
                        (selectedCount > 1 ? "s" : ""));
            }
        }
    }

    private void printSelectedMenus() {
        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement print functionality when PrintMenuActivity is created
        // For now, show a message
        Toast.makeText(this, "Print functionality coming soon. Selected " +
                selectedPatients.size() + " patient(s).", Toast.LENGTH_LONG).show();

        /* // Uncomment when PrintMenuActivity is implemented
        Intent printIntent = new Intent(this, PrintMenuActivity.class);
        long[] patientIds = new long[selectedPatients.size()];
        for (int i = 0; i < selectedPatients.size(); i++) {
            patientIds[i] = selectedPatients.get(i).getPatientId();
        }
        printIntent.putExtra("patient_ids", patientIds);
        printIntent.putExtra("current_user", currentUsername);
        printIntent.putExtra("user_role", currentUserRole);
        printIntent.putExtra("user_full_name", currentUserFullName);
        startActivity(printIntent);
        */
    }

    private void deleteSelectedPatients() {
        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        String message = selectedPatients.size() == 1 ?
                "Are you sure you want to delete " + selectedPatients.get(0).getFullName() + "?" :
                "Are you sure you want to delete " + selectedPatients.size() + " patients?";

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    performPatientDeletion(selectedPatients);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performPatientDeletion(List<Patient> patients) {
        int successCount = 0;
        int failCount = 0;

        for (Patient patient : patients) {
            try {
                boolean success = patientDAO.deletePatient(patient.getPatientId());
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                e.printStackTrace();
            }
        }

        // Show result
        if (failCount == 0) {
            Toast.makeText(this, successCount + " patient(s) deleted successfully",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, successCount + " deleted, " + failCount + " failed",
                    Toast.LENGTH_LONG).show();
        }

        // Clear selections and reload
        if (selectAllCheckBox != null) {
            selectAllCheckBox.setChecked(false);
        }
        loadPatients();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatients();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_existing_patients, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadPatients();
            return true;
        } else if (itemId == R.id.action_home) {
            Intent homeIntent = new Intent(this, MainMenuActivity.class);
            homeIntent.putExtra("current_user", currentUsername);
            homeIntent.putExtra("user_role", currentUserRole);
            homeIntent.putExtra("user_full_name", currentUserFullName);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Inner classes remain the same
    private class PatientAdapter extends BaseAdapter {
        private SparseBooleanArray selectedItems = new SparseBooleanArray();

        @Override
        public int getCount() {
            return filteredPatients.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredPatients.get(position);
        }

        @Override
        public long getItemId(int position) {
            return filteredPatients.get(position).getPatientId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        android.R.layout.simple_list_item_multiple_choice, parent, false);
            }

            CheckedTextView textView = (CheckedTextView) convertView;
            Patient patient = filteredPatients.get(position);

            String displayText = patient.getFullName() + "\n" +
                    patient.getWing() + " " + patient.getRoomNumber() + " | " +
                    patient.getDiet();

            textView.setText(displayText);
            textView.setChecked(selectedItems.get(position, false));

            return convertView;
        }

        public void toggleSelection(int position) {
            if (selectedItems.get(position, false)) {
                selectedItems.delete(position);
            } else {
                selectedItems.put(position, true);
            }
            notifyDataSetChanged();
        }

        public void selectAll(boolean select) {
            selectedItems.clear();
            if (select) {
                for (int i = 0; i < getCount(); i++) {
                    selectedItems.put(i, true);
                }
            }
            notifyDataSetChanged();
        }

        public boolean areAllSelected() {
            if (getCount() == 0) return false;
            for (int i = 0; i < getCount(); i++) {
                if (!selectedItems.get(i, false)) {
                    return false;
                }
            }
            return true;
        }

        public int getSelectedCount() {
            return selectedItems.size();
        }

        public List<Patient> getSelectedPatients() {
            List<Patient> selected = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                if (selectedItems.get(i, false)) {
                    selected.add(filteredPatients.get(i));
                }
            }
            return selected;
        }
    }

    private class DayFilterAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dayLabels.size();
        }

        @Override
        public Object getItem(int position) {
            return dayLabels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        android.R.layout.simple_spinner_item, parent, false);
            }

            TextView textView = (TextView) convertView;
            textView.setText(dayLabels.get(position));
            textView.setPadding(16, 16, 16, 16);

            // Highlight today
            if (position == todayIndex + 1) { // +1 because of "All Days" at index 0
                textView.setTypeface(null, Typeface.BOLD);
                textView.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // FIX: Call getView instead of super.getView
            View view = getView(position, convertView, parent);
            TextView textView = (TextView) view;
            textView.setPadding(24, 20, 24, 20);

            if (position == todayIndex + 1) {
                textView.setTypeface(null, Typeface.BOLD);
                textView.setTextColor(getResources().getColor(R.color.colorPrimary));
            }

            return view;
        }
    }
}