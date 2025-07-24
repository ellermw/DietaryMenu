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

        // Initialize adapters
        patientsAdapter = new PatientAdapter();
        patientsListView.setAdapter(patientsAdapter);
        patientsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        dayFilterAdapter = new DayFilterAdapter();
        dayFilterSpinner.setAdapter(dayFilterAdapter);
    }

    private void setupDayFilter() {
        // Clear previous data
        dayLabels.clear();

        // Get current calendar
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Calculate Sunday of current week
        calendar.add(Calendar.DAY_OF_MONTH, -(currentDayOfWeek - Calendar.SUNDAY));

        // Days of week
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Add "All Days" option first
        dayLabels.add("All Days");

        // Generate Sunday through Saturday
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(calendar.getTime());
            String dayName = dayNames[i];

            // Check if this is today
            Calendar today = Calendar.getInstance();
            boolean isToday = (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR));

            if (isToday) {
                dayLabels.add(dayName + " (Today) - " + date);
                todayIndex = dayLabels.size() - 1;
            } else {
                dayLabels.add(dayName + " - " + date);
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Update adapter
        dayFilterAdapter.notifyDataSetChanged();

        // Set default to today if found
        if (todayIndex > 0) {
            dayFilterSpinner.setSelection(todayIndex);
        }
    }

    private void setupListeners() {
        // Search input listener
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

        // Day filter listener
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

        // Select all checkbox listener
        if (selectAllCheckBox != null) {
            selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (patientsAdapter != null) {
                    patientsAdapter.selectAll(isChecked);
                    // Always update bulk operation visibility
                    updateBulkOperationVisibility();
                }
            });
        }

        // ListView item click listener for selection
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
                boolean matchesSearch = searchQuery.isEmpty() ||
                        patient.getFullName().toLowerCase().contains(searchQuery) ||
                        patient.getWing().toLowerCase().contains(searchQuery) ||
                        patient.getRoomNumber().toLowerCase().contains(searchQuery) ||
                        patient.getDiet().toLowerCase().contains(searchQuery);

                // Day filtering logic
                boolean matchesDay = true; // Default to show all patients

                if (selectedDayIndex > 0) { // 0 = "All Days", so only filter if specific day selected
                    // For now, we'll show all patients regardless of selected day
                    // In the future, this could be enhanced to filter by actual meal planning dates
                    // or patient admission dates if that functionality is needed
                    matchesDay = true;
                }

                if (matchesSearch && matchesDay) {
                    filteredPatients.add(patient);
                }
            }

            // Update adapter
            patientsAdapter.notifyDataSetChanged();

            // Update count
            if (patientsCountText != null) {
                String selectedDayText = "";
                if (selectedDayIndex > 0 && dayFilterSpinner != null) {
                    selectedDayText = " for " + dayFilterSpinner.getSelectedItem().toString();
                }
                patientsCountText.setText("Showing " + filteredPatients.size() + " of " +
                        allPatients.size() + " patients" + selectedDayText);
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
                printMenusButton.setText("Print " + selectedCount + " Menu" +
                        (selectedCount > 1 ? "s" : ""));
            }
            if (deleteSelectedButton != null) {
                deleteSelectedButton.setText("Delete " + selectedCount + " Selected");
            }
        }
    }

    private void printSelectedMenus() {
        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();

        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement print functionality
        Toast.makeText(this, "Printing " + selectedPatients.size() + " menus...", Toast.LENGTH_SHORT).show();
    }

    private void deleteSelectedPatients() {
        List<Patient> patientsToDelete = patientsAdapter.getSelectedPatients();

        if (patientsToDelete.isEmpty()) {
            Toast.makeText(this, "No patients selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Patients")
                .setMessage("Are you sure you want to delete " + patientsToDelete.size() + " patient(s)?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete patients
                    int deletedCount = 0;
                    for (Patient patient : patientsToDelete) {
                        if (patientDAO.deletePatient(patient.getPatientId())) {
                            deletedCount++;
                        }
                    }
                    Toast.makeText(this, deletedCount + " patient(s) deleted successfully",
                            Toast.LENGTH_SHORT).show();

                    // Clear selections and reload
                    if (selectAllCheckBox != null) {
                        selectAllCheckBox.setChecked(false);
                    }
                    loadPatients();
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
        getMenuInflater().inflate(R.menu.menu_existing_patients, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add_patient:
                Intent intent = new Intent(this, NewPatientActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                loadPatients();
                return true;
            case R.id.action_home:
                Intent homeIntent = new Intent(this, MainMenuActivity.class);
                homeIntent.putExtra("current_user", currentUsername);
                homeIntent.putExtra("user_role", currentUserRole);
                homeIntent.putExtra("user_full_name", currentUserFullName);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
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

    // Patient adapter class
    private class PatientAdapter extends BaseAdapter {
        private SparseBooleanArray selectedItems = new SparseBooleanArray();

        public void selectAll(boolean select) {
            selectedItems.clear();
            if (select) {
                for (int i = 0; i < filteredPatients.size(); i++) {
                    selectedItems.put(i, true);
                }
            }
            notifyDataSetChanged();
        }

        public void toggleSelection(int position) {
            if (selectedItems.get(position, false)) {
                selectedItems.delete(position);
            } else {
                selectedItems.put(position, true);
            }
            notifyDataSetChanged();
        }

        public boolean areAllSelected() {
            if (filteredPatients.isEmpty()) return false;
            for (int i = 0; i < filteredPatients.size(); i++) {
                if (!selectedItems.get(i, false)) {
                    return false;
                }
            }
            return true;
        }

        public List<Patient> getSelectedPatients() {
            List<Patient> selected = new ArrayList<>();
            for (int i = 0; i < filteredPatients.size(); i++) {
                if (selectedItems.get(i, false)) {
                    selected.add(filteredPatients.get(i));
                }
            }
            return selected;
        }

        public int getSelectedCount() {
            return selectedItems.size();
        }

        @Override
        public int getCount() {
            return filteredPatients.size();
        }

        @Override
        public Patient getItem(int position) {
            return filteredPatients.get(position);
        }

        @Override
        public long getItemId(int position) {
            return filteredPatients.get(position).getPatientId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_existing_patient, parent, false);
            }

            Patient patient = getItem(position);

            TextView nameText = convertView.findViewById(R.id.patientNameText);
            TextView locationText = convertView.findViewById(R.id.locationText);
            TextView dietText = convertView.findViewById(R.id.dietText);
            TextView createdDateText = convertView.findViewById(R.id.createdDateText);

            nameText.setText(patient.getFullName());
            locationText.setText("📍 " + patient.getWing() + " - Room " + patient.getRoomNumber());
            dietText.setText("🍽️ " + patient.getDiet());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            createdDateText.setText("📅 " + sdf.format(patient.getCreatedDate()));

            // Highlight if selected
            convertView.setBackgroundColor(selectedItems.get(position, false) ?
                    Color.parseColor("#E3F2FD") : Color.TRANSPARENT);

            return convertView;
        }
    }

    // Day filter adapter class
    private class DayFilterAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dayLabels.size();
        }

        @Override
        public String getItem(int position) {
            return dayLabels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(getItem(position));
            textView.setMinHeight(48); // Ensure minimum height
            textView.setPadding(16, 12, 16, 12); // Add padding

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(getItem(position));
            textView.setMinHeight(56); // Ensure dropdown items have proper height
            textView.setPadding(16, 16, 16, 16); // Add proper padding
            textView.setTextSize(16); // Ensure readable text size

            return convertView;
        }
    }
}