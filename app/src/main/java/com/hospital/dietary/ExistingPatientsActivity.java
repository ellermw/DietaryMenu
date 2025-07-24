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
    private int todayIndex = 6; // Today's position in the 7-day list
    private List<String> dayLabels = new ArrayList<>();
    private List<Date> dayDates = new ArrayList<>();

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
        setupDates();

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

    private void setupDates() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd", Locale.US);

        // Generate 7 days of dates starting from today
        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            dayDates.add(date);

            String label = dateFormat.format(date);
            if (i == 0) {
                label = "Today - " + label;
            }
            dayLabels.add(label);

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        dayFilterAdapter.notifyDataSetChanged();
        dayFilterSpinner.setSelection(todayIndex);
    }

    private void loadPatients() {
        try {
            allPatients = patientDAO.getActivePatients(); // Use getActivePatients instead
            filterPatients();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading patients: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void filterPatients() {
        filteredPatients.clear();

        String searchText = searchInput.getText().toString().toLowerCase().trim();

        for (Patient patient : allPatients) {
            boolean matchesSearch = searchText.isEmpty() ||
                    patient.getFullName().toLowerCase().contains(searchText) ||
                    patient.getRoomNumber().toLowerCase().contains(searchText) ||
                    patient.getWing().toLowerCase().contains(searchText);

            if (matchesSearch) {
                filteredPatients.add(patient);
            }
        }

        updateUI();
    }

    private void updateUI() {
        patientsAdapter.notifyDataSetChanged();
        patientsCountText.setText(filteredPatients.size() + " patients");

        // Show/hide bulk operations based on admin role
        boolean isAdmin = "Admin".equalsIgnoreCase(currentUserRole);
        bulkOperationsContainer.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        // Search functionality
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
                // Filter by selected day if needed
                filterPatients();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Patient selection
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = filteredPatients.get(position);
            openPatientDetail(patient);
        });

        // Select all checkbox
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int i = 0; i < patientsListView.getCount(); i++) {
                patientsListView.setItemChecked(i, isChecked);
            }
        });

        // Print menus button
        printMenusButton.setOnClickListener(v -> printSelectedMenus());

        // Delete selected button
        deleteSelectedButton.setOnClickListener(v -> deleteSelectedPatients());
    }

    private void openPatientDetail(Patient patient) {
        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("patient_id", patient.getPatientId());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void printSelectedMenus() {
        SparseBooleanArray checkedItems = patientsListView.getCheckedItemPositions();
        List<Patient> selectedPatients = new ArrayList<>();

        for (int i = 0; i < checkedItems.size(); i++) {
            if (checkedItems.valueAt(i)) {
                int position = checkedItems.keyAt(i);
                selectedPatients.add(filteredPatients.get(position));
            }
        }

        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "Please select patients to print", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement print functionality
        Toast.makeText(this, "Printing menus for " + selectedPatients.size() + " patients",
                Toast.LENGTH_SHORT).show();
    }

    private void deleteSelectedPatients() {
        SparseBooleanArray checkedItems = patientsListView.getCheckedItemPositions();
        List<Patient> selectedPatients = new ArrayList<>();

        for (int i = 0; i < checkedItems.size(); i++) {
            if (checkedItems.valueAt(i)) {
                int position = checkedItems.keyAt(i);
                selectedPatients.add(filteredPatients.get(position));
            }
        }

        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "Please select patients to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Patients")
                .setMessage("Are you sure you want to delete " + selectedPatients.size() + " patients?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    performDelete(selectedPatients);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(List<Patient> patients) {
        int successCount = 0;

        for (Patient patient : patients) {
            try {
                if (patientDAO.deletePatientById(patient.getPatientId()) > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                // Continue with other deletions
            }
        }

        Toast.makeText(this, "Deleted " + successCount + " patients", Toast.LENGTH_SHORT).show();
        loadPatients();
        selectAllCheckBox.setChecked(false);
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
            case R.id.action_refresh:
                loadPatients();
                return true;
            case R.id.action_add_patient:
                Intent intent = new Intent(this, NewPatientActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                startActivity(intent);
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

    // Patient adapter
    private class PatientAdapter extends BaseAdapter {
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
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            }

            Patient patient = getItem(position);

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);

            // Format patient info
            String patientInfo = patient.getFullName() + " - " +
                    patient.getWing() + "-" + patient.getRoomNumber() + " - " +
                    patient.getDiet();

            textView.setText(patientInfo);
            textView.setTextSize(16);
            textView.setPadding(16, 16, 16, 16);

            // Show completion status with color
            boolean allComplete = patient.isBreakfastComplete() &&
                    patient.isLunchComplete() &&
                    patient.isDinnerComplete();

            if (allComplete) {
                textView.setTextColor(Color.parseColor("#27ae60"));
            } else {
                textView.setTextColor(Color.BLACK);
            }

            return convertView;
        }
    }

    // Day filter adapter
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
            TextView textView = new TextView(ExistingPatientsActivity.this);
            textView.setText(getItem(position));
            textView.setPadding(16, 16, 16, 16);
            textView.setTextSize(16);

            if (position == 0) {
                textView.setTypeface(null, Typeface.BOLD);
            }

            return textView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) getView(position, convertView, parent);
            textView.setBackgroundColor(Color.WHITE);
            return textView;
        }
    }
}