package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

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

    // Data
    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private PatientAdapter patientsAdapter;
    private DayFilterAdapter dayFilterAdapter;

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

        // Setup listeners
        setupListeners();

        // Load patients
        loadPatients();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Existing Patients");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        // Setup day filter spinner
        String[] days = {"All Days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        dayFilterAdapter = new DayFilterAdapter(days);
        dayFilterSpinner.setAdapter(dayFilterAdapter);

        // Initialize list view with CHOICE_MODE_NONE for clickable items
        patientsListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
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
                    if (dayFilterAdapter != null) {
                        dayFilterAdapter.updateSelectedPosition(position);
                    }
                    filterPatients();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Patient list item click - Fixed to properly handle clicks
        if (patientsListView != null) {
            patientsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position < filteredPatients.size()) {
                        Patient selectedPatient = filteredPatients.get(position);
                        openPatientDetails(selectedPatient);
                    }
                }
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
                boolean matchesSearch = searchQuery.isEmpty() ||
                        patient.getFullName().toLowerCase().contains(searchQuery) ||
                        patient.getWing().toLowerCase().contains(searchQuery) ||
                        patient.getRoomNumber().toLowerCase().contains(searchQuery) ||
                        patient.getDiet().toLowerCase().contains(searchQuery);

                boolean matchesDay = true; // Show all for now

                if (matchesSearch && matchesDay) {
                    filteredPatients.add(patient);
                }
            }

            // Update adapter
            if (patientsAdapter == null) {
                patientsAdapter = new PatientAdapter(this, filteredPatients);
                patientsListView.setAdapter(patientsAdapter);
            } else {
                patientsAdapter.updateData(filteredPatients);
            }

            // Update count
            if (patientsCountText != null) {
                String selectedDayText = "";
                if (selectedDayIndex > 0 && dayFilterSpinner != null) {
                    selectedDayText = " for " + dayFilterSpinner.getSelectedItem().toString();
                }
                patientsCountText.setText("Showing " + filteredPatients.size() +
                        " of " + allPatients.size() + " patients" + selectedDayText);
            }

            updateBulkOperationVisibility();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error filtering patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateBulkOperationVisibility() {
        if (bulkOperationsContainer == null || patientsAdapter == null) return;

        int selectedCount = patientsAdapter.getSelectedCount();
        boolean hasSelections = selectedCount > 0;

        bulkOperationsContainer.setVisibility(hasSelections ? View.VISIBLE : View.GONE);

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

        // TODO: Implement printing functionality
        Toast.makeText(this, "Printing " + selectedPatients.size() + " menus...",
                Toast.LENGTH_SHORT).show();
    }

    private void deleteSelectedPatients() {
        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Patients")
                .setMessage("Are you sure you want to delete " + selectedPatients.size() +
                        " patient" + (selectedPatients.size() > 1 ? "s" : "") + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        int deletedCount = 0;
                        for (Patient patient : selectedPatients) {
                            int result = patientDAO.deletePatientById(patient.getPatientId());
                            if (result > 0) {
                                deletedCount++;
                            }
                        }

                        if (deletedCount > 0) {
                            Toast.makeText(this, deletedCount + " patient" +
                                            (deletedCount > 1 ? "s" : "") + " deleted successfully",
                                    Toast.LENGTH_SHORT).show();
                            loadPatients();
                        } else {
                            Toast.makeText(this, "Failed to delete patients", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error deleting patients: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatients();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_patient_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add_patient:
                Intent addPatientIntent = new Intent(this, NewPatientActivity.class);
                addPatientIntent.putExtra("current_user", currentUsername);
                addPatientIntent.putExtra("user_role", currentUserRole);
                addPatientIntent.putExtra("user_full_name", currentUserFullName);
                startActivity(addPatientIntent);
                return true;
            case R.id.action_refresh:
                loadPatients();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // Custom adapter for day filter spinner
    private class DayFilterAdapter extends ArrayAdapter<String> {
        private int selectedPosition = 0;

        public DayFilterAdapter(String[] days) {
            super(ExistingPatientsActivity.this, android.R.layout.simple_spinner_item, days);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        public void updateSelectedPosition(int position) {
            selectedPosition = position;
            notifyDataSetChanged();
        }

        @Override
        public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView tv = (TextView) view;

            if (position == selectedPosition) {
                tv.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                tv.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                tv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                tv.setTextColor(getResources().getColor(android.R.color.black));
            }

            return view;
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