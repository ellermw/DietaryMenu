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
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.util.SparseBooleanArray;
import android.graphics.Typeface;

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
    private DayFilterAdapter dayFilterAdapter;

    // Date formatting
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
    private int todaySelectionIndex = 0; // Track which index is "Today" for default selection

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

        // Setup day filter spinner with proper Sunday-Saturday format
        setupDayFilterSpinner();

        // Initially hide bulk operations
        updateBulkOperationVisibility();
    }

    private void setupDayFilterSpinner() {
        List<String> dayOptions = new ArrayList<>();

        // Add "All Days" first
        dayOptions.add("All Days");

        // Get current day of week
        Calendar today = Calendar.getInstance();
        int currentDayOfWeek = today.get(Calendar.DAY_OF_WEEK); // Sunday = 1, Monday = 2, etc.

        // Calculate the date for each day of the current week starting from Sunday
        Calendar weekStart = Calendar.getInstance();
        // Set to Sunday of current week
        weekStart.add(Calendar.DAY_OF_MONTH, -(currentDayOfWeek - 1));

        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Generate Sunday through Saturday for the current week
        for (int i = 0; i < 7; i++) {
            Calendar dayCalendar = Calendar.getInstance();
            dayCalendar.setTime(weekStart.getTime());
            dayCalendar.add(Calendar.DAY_OF_MONTH, i);

            String dayName = dayNames[i];
            String dateStr = dateFormat.format(dayCalendar.getTime());

            String formattedOption;
            // Check if this day is today
            if (dayCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    dayCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                formattedOption = "Today - " + dateStr;
                todaySelectionIndex = i + 1; // +1 because "All Days" is at index 0
            } else {
                formattedOption = dayName + " - " + dateStr;
            }

            dayOptions.add(formattedOption);
        }

        // Create custom adapter with highlighted selected item
        dayFilterAdapter = new DayFilterAdapter(this, dayOptions, todaySelectionIndex);

        if (dayFilterSpinner != null) {
            dayFilterSpinner.setAdapter(dayFilterAdapter);
            // Set default selection to "Today"
            dayFilterSpinner.setSelection(todaySelectionIndex);
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
                    // Update the adapter to refresh styling
                    if (dayFilterAdapter != null) {
                        dayFilterAdapter.updateSelectedPosition(position);
                    }
                    filterPatients();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Patient list item click
        if (patientsListView != null) {
            patientsListView.setOnItemClickListener((parent, view, position, id) -> {
                Patient selectedPatient = filteredPatients.get(position);
                openPatientDetails(selectedPatient);
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
            int selectedDayIndex = dayFilterSpinner != null ? dayFilterSpinner.getSelectedItemPosition() : 0;

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
            if (patientsAdapter == null) {
                patientsAdapter = new PatientAdapter(this, filteredPatients);
                patientsListView.setAdapter(patientsAdapter);
            } else {
                patientsAdapter.notifyDataSetChanged();
            }

            // Update count
            if (patientsCountText != null) {
                String selectedDayText = "";
                if (selectedDayIndex > 0 && dayFilterSpinner != null) {
                    selectedDayText = " for " + dayFilterSpinner.getSelectedItem().toString();
                }
                patientsCountText.setText("Showing " + filteredPatients.size() + " of " + allPatients.size() + " patients" + selectedDayText);
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
            Toast.makeText(this, "No patients selected for printing", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement printing functionality
        String message = "Printing menus for " + selectedPatients.size() + " patient" +
                (selectedPatients.size() > 1 ? "s" : "") + ":\n\n";

        for (Patient patient : selectedPatients) {
            message += "• " + patient.getFullName() + " (" + patient.getWing() + " " + patient.getRoomNumber() + ")\n";
        }

        new AlertDialog.Builder(this)
                .setTitle("Print Menus")
                .setMessage(message + "\nPrinting functionality coming soon!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void deleteSelectedPatients() {
        if (patientsAdapter == null) return;

        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No patients selected for deletion", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Are you sure you want to delete " + selectedPatients.size() +
                " patient" + (selectedPatients.size() > 1 ? "s" : "") + "?\n\n";

        for (Patient patient : selectedPatients) {
            message += "• " + patient.getFullName() + "\n";
        }

        message += "\nThis action cannot be undone.";

        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        int deletedCount = 0;
                        for (Patient patient : selectedPatients) {
                            if (patientDAO.deletePatient(patient.getPatientId())) {
                                deletedCount++;
                            }
                        }

                        if (deletedCount > 0) {
                            Toast.makeText(this, deletedCount + " patient" + (deletedCount > 1 ? "s" : "") + " deleted successfully", Toast.LENGTH_SHORT).show();
                            loadPatients(); // Reload the list
                        } else {
                            Toast.makeText(this, "Failed to delete patients", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error deleting patients: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh patient list when returning to this activity
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

    // Custom adapter for day filter spinner with highlighted selected item
    private class DayFilterAdapter extends ArrayAdapter<String> {
        private int selectedPosition;
        private List<String> items;

        public DayFilterAdapter(ExistingPatientActivity context, List<String> items, int selectedPosition) {
            super(context, android.R.layout.simple_spinner_item, items);
            this.items = items;
            this.selectedPosition = selectedPosition;
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view;

            // Style the currently selected item in the spinner button
            if (position == dayFilterSpinner.getSelectedItemPosition()) {
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                textView.setTextColor(0xFF2196F3); // Blue color
                textView.setTextSize(16);
            } else {
                textView.setTypeface(Typeface.DEFAULT);
                textView.setTextColor(0xFF2c3e50); // Dark gray
                textView.setTextSize(14);
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView textView = (TextView) view;

            // Style items in the dropdown list
            if (position == dayFilterSpinner.getSelectedItemPosition()) {
                // Highlight selected item
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                textView.setTextColor(0xFF2196F3); // Blue color
                textView.setBackgroundColor(0xFFE3F2FD); // Light blue background
                textView.setPadding(20, 16, 20, 16);
            } else if (items.get(position).startsWith("Today")) {
                // Special styling for "Today" even when not selected
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                textView.setTextColor(0xFF4CAF50); // Green color for today
                textView.setBackgroundColor(0xFFFFFFFF); // White background
                textView.setPadding(20, 16, 20, 16);
            } else {
                // Normal styling for other items
                textView.setTypeface(Typeface.DEFAULT);
                textView.setTextColor(0xFF2c3e50); // Dark gray
                textView.setBackgroundColor(0xFFFFFFFF); // White background
                textView.setPadding(20, 16, 20, 16);
            }

            return view;
        }

        public void updateSelectedPosition(int position) {
            this.selectedPosition = position;
            notifyDataSetChanged();
        }
    }

    // PatientAdapter inner class for managing patient list display and selection
    private class PatientAdapter extends BaseAdapter {
        private List<Patient> patients;
        private SparseBooleanArray selectedItems;

        public PatientAdapter(ExistingPatientActivity context, List<Patient> patients) {
            this.patients = patients;
            this.selectedItems = new SparseBooleanArray();
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
            CheckedTextView checkedTextView = (CheckedTextView) convertView;

            String displayText = patient.getFullName() + "\n" +
                    patient.getWing() + " " + patient.getRoomNumber() + " | " +
                    patient.getDiet();

            checkedTextView.setText(displayText);
            checkedTextView.setChecked(selectedItems.get(position, false));

            checkedTextView.setOnClickListener(v -> {
                boolean isChecked = !selectedItems.get(position, false);
                selectedItems.put(position, isChecked);
                checkedTextView.setChecked(isChecked);
                updateBulkOperationVisibility();

                // Update select all checkbox state
                if (selectAllCheckBox != null) {
                    selectAllCheckBox.setChecked(getSelectedCount() == getCount());
                }
            });

            return convertView;
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

        public int getSelectedCount() {
            int count = 0;
            for (int i = 0; i < selectedItems.size(); i++) {
                if (selectedItems.valueAt(i)) {
                    count++;
                }
            }
            return count;
        }

        public List<Patient> getSelectedPatients() {
            List<Patient> selected = new ArrayList<>();
            for (int i = 0; i < selectedItems.size(); i++) {
                if (selectedItems.valueAt(i)) {
                    int position = selectedItems.keyAt(i);
                    if (position < patients.size()) {
                        selected.add(patients.get(position));
                    }
                }
            }
            return selected;
        }
    }
}