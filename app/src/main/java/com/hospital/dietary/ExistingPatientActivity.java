package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

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

        // Setup day filter spinner
        String[] dayOptions = {"All Days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayOptions);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (dayFilterSpinner != null) {
            dayFilterSpinner.setAdapter(dayAdapter);
        }

        // Initially hide bulk operations
        updateBulkOperationVisibility();
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

        // Patient list item clicks - ENHANCED: Open patient details on click
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient selectedPatient = filteredPatients.get(position);
            openPatientDetails(selectedPatient);
        });

        // Patient list item long clicks - Select for bulk operations
        patientsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (patientsAdapter != null) {
                patientsAdapter.toggleSelection(position);
                updateBulkOperationVisibility();
            }
            return true;
        });

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
        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("patient_id", patient.getPatientId());
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

            String searchQuery = searchInput != null ? searchInput.getText().toString().toLowerCase().trim() : "";
            int selectedDay = dayFilterSpinner != null ? dayFilterSpinner.getSelectedItemPosition() : 0;

            for (Patient patient : allPatients) {
                boolean matchesSearch = searchQuery.isEmpty() ||
                        patient.getFullName().toLowerCase().contains(searchQuery) ||
                        patient.getWing().toLowerCase().contains(searchQuery) ||
                        patient.getRoomNumber().toLowerCase().contains(searchQuery) ||
                        patient.getDiet().toLowerCase().contains(searchQuery);

                // Day filtering (0 = All Days, 1-7 = Monday-Sunday)
                boolean matchesDay = selectedDay == 0; // All days

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
            if (printMenusButton != null) {
                printMenusButton.setText("Print " + selectedCount + " Menu(s)");
            }
            if (deleteSelectedButton != null) {
                deleteSelectedButton.setText("Delete " + selectedCount + " Patient(s)");
            }
        }

        // Update select all checkbox state
        if (selectAllCheckBox != null && !filteredPatients.isEmpty()) {
            selectAllCheckBox.setChecked(selectedCount == filteredPatients.size());
        }
    }

    private void printSelectedMenus() {
        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected for printing", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement printing functionality
        Toast.makeText(this, "Printing " + selectedPatients.size() + " menus...", Toast.LENGTH_SHORT).show();

        // Clear selections after printing
        patientsAdapter.clearSelections();
        updateBulkOperationVisibility();
    }

    private void deleteSelectedPatients() {
        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected for deletion", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Are you sure you want to delete " + selectedPatients.size() + " patient(s)?\n\n" +
                "This will also delete all associated meal plans and orders.\n\n" +
                "This action cannot be undone.";

        new AlertDialog.Builder(this)
                .setTitle("Delete Patients")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    performBulkDelete(selectedPatients);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performBulkDelete(List<Patient> patientsToDelete) {
        int deletedCount = 0;
        int failedCount = 0;

        for (Patient patient : patientsToDelete) {
            boolean success = patientDAO.deletePatient(patient.getPatientId());
            if (success) {
                deletedCount++;
            } else {
                failedCount++;
            }
        }

        String message = deletedCount + " patient(s) deleted successfully";
        if (failedCount > 0) {
            message += ", " + failedCount + " failed to delete";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Reload data
        loadPatients();
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
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Refresh data when returning from patient details
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadPatients();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
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
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_patient_selectable, parent, false);
            }

            Patient patient = patients.get(position);
            boolean isSelected = position < selections.size() && selections.get(position);

            // Set patient information
            TextView nameText = convertView.findViewById(R.id.patientNameText);
            TextView locationText = convertView.findViewById(R.id.locationText);
            TextView dietText = convertView.findViewById(R.id.dietText);
            TextView statusText = convertView.findViewById(R.id.statusText);
            CheckBox selectionCheckBox = convertView.findViewById(R.id.selectionCheckBox);

            nameText.setText(patient.getFullName());
            locationText.setText("📍 " + patient.getWing() + " - Room " + patient.getRoomNumber());
            dietText.setText("🍽️ " + patient.getDiet());

            // Show meal completion status
            String status = getMealCompletionStatus(patient);
            statusText.setText(status);

            // Selection state
            selectionCheckBox.setChecked(isSelected);
            selectionCheckBox.setOnCheckedChangeListener((buttonView, checked) -> {
                if (position < selections.size()) {
                    selections.set(position, checked);
                    updateBulkOperationVisibility();
                }
            });

            // Highlight selected items
            convertView.setBackgroundColor(isSelected ?
                    getResources().getColor(android.R.color.holo_blue_light, null) :
                    getResources().getColor(android.R.color.transparent, null));

            return convertView;
        }

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

        public void toggleSelection(int position) {
            if (position < selections.size()) {
                selections.set(position, !selections.get(position));
                notifyDataSetChanged();
            }
        }

        public void selectAll(boolean select) {
            for (int i = 0; i < selections.size(); i++) {
                selections.set(i, select);
            }
            notifyDataSetChanged();
        }

        public void clearSelections() {
            for (int i = 0; i < selections.size(); i++) {
                selections.set(i, false);
            }
            notifyDataSetChanged();
        }

        public int getSelectedCount() {
            int count = 0;
            for (Boolean selected : selections) {
                if (selected) count++;
            }
            return count;
        }

        public List<Patient> getSelectedPatients() {
            List<Patient> selectedPatients = new ArrayList<>();
            for (int i = 0; i < patients.size() && i < selections.size(); i++) {
                if (selections.get(i)) {
                    selectedPatients.add(patients.get(i));
                }
            }
            return selectedPatients;
        }

        @Override
        public void notifyDataSetChanged() {
            // Ensure selections list matches patients list size
            while (selections.size() < patients.size()) {
                selections.add(false);
            }
            while (selections.size() > patients.size()) {
                selections.remove(selections.size() - 1);
            }
            super.notifyDataSetChanged();
        }
    }
}