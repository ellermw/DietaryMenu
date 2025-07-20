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

        // Set initial visibility
        updateBulkOperationVisibility();
    }

    private void setupDayFilter() {
        // Days of the week for filtering
        String[] dayOptions = {
                "All Days",
                "Monday (" + dateFormat.format(getDateForDay(Calendar.MONDAY)) + ")",
                "Tuesday (" + dateFormat.format(getDateForDay(Calendar.TUESDAY)) + ")",
                "Wednesday (" + dateFormat.format(getDateForDay(Calendar.WEDNESDAY)) + ")",
                "Thursday (" + dateFormat.format(getDateForDay(Calendar.THURSDAY)) + ")",
                "Friday (" + dateFormat.format(getDateForDay(Calendar.FRIDAY)) + ")",
                "Saturday (" + dateFormat.format(getDateForDay(Calendar.SATURDAY)) + ")",
                "Sunday (" + dateFormat.format(getDateForDay(Calendar.SUNDAY)) + ")"
        };

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayOptions);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayFilterSpinner.setAdapter(dayAdapter);
    }

    private Date getDateForDay(int dayOfWeek) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return cal.getTime();
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

        // FIXED: Patient list click listener - Opens patient details for editing
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

    // FIXED: New method to open patient details for editing
    private void openPatientDetails(Patient patient) {
        Intent intent = new Intent(this, PatientInfoActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.putExtra("selected_patient_id", patient.getPatientId());
        startActivity(intent);
    }

    private void loadPatients() {
        try {
            allPatients.clear();
            // Load ALL patients, not just those with orders
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
                patientsCountText.setText("Patients: " + filteredPatients.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error filtering patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void printSelectedMenus() {
        if (patientsAdapter == null) return;

        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "Please select patients to print menus for", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create print content
        StringBuilder printContent = new StringBuilder();
        printContent.append("DAILY MENU REPORT\n");
        printContent.append("Generated on: ").append(new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(new Date())).append("\n");
        printContent.append("Total Patients: ").append(selectedPatients.size()).append("\n\n");

        for (Patient patient : selectedPatients) {
            printContent.append("══════════════════════════════════════\n");
            printContent.append("PATIENT: ").append(patient.getFullName().toUpperCase()).append("\n");
            printContent.append("LOCATION: ").append(patient.getWing()).append(" - Room ").append(patient.getRoomNumber()).append("\n");
            printContent.append("DIET: ").append(patient.getDiet()).append("\n");

            if (patient.getFluidRestriction() != null && !patient.getFluidRestriction().equals("No Restriction")) {
                printContent.append("FLUID RESTRICTION: ").append(patient.getFluidRestriction()).append("\n");
            }

            if (patient.hasTextureModifications()) {
                printContent.append("TEXTURE MODIFICATIONS: ").append(patient.getTextureModificationsDescription()).append("\n");
            }

            printContent.append("──────────────────────────────────────\n");
            printContent.append("MEAL STATUS: ").append(patient.getMealCompletionStatus()).append("\n");
            printContent.append("══════════════════════════════════════\n\n");
        }

        // Show print dialog
        new AlertDialog.Builder(this)
                .setTitle("Print Menu Report")
                .setMessage("Print report for " + selectedPatients.size() + " patient(s)?")
                .setPositiveButton("Print", (dialog, which) -> {
                    // Here you would implement actual printing functionality
                    Toast.makeText(this, "Menu report generated for " + selectedPatients.size() + " patients", Toast.LENGTH_LONG).show();
                })
                .setNeutralButton("Preview", (dialog, which) -> {
                    // Show preview dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Print Preview")
                            .setMessage(printContent.toString())
                            .setPositiveButton("OK", null)
                            .show();
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

        // Create confirmation message
        StringBuilder message = new StringBuilder();
        message.append("Are you sure you want to delete the following ").append(selectedPatients.size()).append(" patient(s)?\n\n");

        for (Patient patient : selectedPatients) {
            message.append("• ").append(patient.getFullName())
                    .append(" (").append(patient.getWing()).append(" - Room ").append(patient.getRoomNumber()).append(")\n");
        }

        message.append("\nThis action cannot be undone!");

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

            // Handle selection
            convertView.setOnClickListener(v -> {
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                } else {
                    selectedPositions.add(position);
                }
                textView.setChecked(selectedPositions.contains(position));
                updateBulkOperationVisibility();
            });

            return convertView;
        }

        public void selectAll(boolean selectAll) {
            selectedPositions.clear();
            if (selectAll) {
                for (int i = 0; i < patients.size(); i++) {
                    selectedPositions.add(i);
                }
            }
            notifyDataSetChanged();
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
        getMenuInflater().inflate(R.menu.menu_existing_patients, menu);
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
        // Refresh patient list when returning from other activities
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