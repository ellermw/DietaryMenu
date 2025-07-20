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
    private TextView patientsCountText; // FIXED: Changed from "Completed Orders" to "Active Patients"
    private Button printMenusButton; // FEATURE: Print menus button
    private Button deleteSelectedButton; // FEATURE: Bulk delete button
    private CheckBox selectAllCheckBox; // FEATURE: Select all checkbox

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
            getSupportActionBar().setTitle("Active Patients"); // FIXED: Changed title
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

        // FEATURE: Add new buttons for printing and bulk operations
        printMenusButton = findViewById(R.id.printMenusButton);
        deleteSelectedButton = findViewById(R.id.deleteSelectedButton);
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);

        // Setup list adapter with checkbox support
        patientsAdapter = new PatientAdapter(this, filteredPatients);
        patientsListView.setAdapter(patientsAdapter);

        // Initially hide bulk operation controls
        updateBulkOperationVisibility();
    }

    // FIXED: Setup day filter with correct logic
    private void setupDayFilter() {
        List<String> dayOptions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        // Add 6 days prior, today, and tomorrow
        for (int i = -6; i <= 1; i++) {
            Calendar dayCalendar = (Calendar) calendar.clone();
            dayCalendar.add(Calendar.DAY_OF_YEAR, i);

            String dayText;
            if (i == 0) {
                // FIXED: Show current day as "Today - MM/DD"
                dayText = "Today - " + dateFormat.format(dayCalendar.getTime());
            } else if (i == 1) {
                dayText = "Tomorrow - " + dateFormat.format(dayCalendar.getTime());
            } else {
                // FIXED: Format as "Weekday - MM/DD"
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE - MM/dd", Locale.getDefault());
                dayText = dayFormat.format(dayCalendar.getTime());
            }

            dayOptions.add(dayText);
        }

        // FIXED: Always order by Sunday → Saturday
        Collections.sort(dayOptions, (a, b) -> {
            // Extract day names and compare by day of week order
            String dayA = a.split(" - ")[0];
            String dayB = b.split(" - ")[0];

            if ("Today".equals(dayA)) dayA = getCurrentDayName();
            if ("Tomorrow".equals(dayA)) dayA = getTomorrowDayName();
            if ("Today".equals(dayB)) dayB = getCurrentDayName();
            if ("Tomorrow".equals(dayB)) dayB = getTomorrowDayName();

            return getDayOrder(dayA) - getDayOrder(dayB);
        });

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayOptions);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayFilterSpinner.setAdapter(dayAdapter);

        // Select today by default
        for (int i = 0; i < dayOptions.size(); i++) {
            if (dayOptions.get(i).startsWith("Today")) {
                dayFilterSpinner.setSelection(i);
                break;
            }
        }
    }

    private String getCurrentDayName() {
        return new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());
    }

    private String getTomorrowDayName() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        return new SimpleDateFormat("EEEE", Locale.getDefault()).format(tomorrow.getTime());
    }

    private int getDayOrder(String dayName) {
        switch (dayName) {
            case "Sunday": return 0;
            case "Monday": return 1;
            case "Tuesday": return 2;
            case "Wednesday": return 3;
            case "Thursday": return 4;
            case "Friday": return 5;
            case "Saturday": return 6;
            default: return 7;
        }
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

        // FEATURE: Bulk operation listeners
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            patientsAdapter.selectAll(isChecked);
            updateBulkOperationVisibility();
        });

        printMenusButton.setOnClickListener(v -> printSelectedMenus());
        deleteSelectedButton.setOnClickListener(v -> deleteSelectedPatients());
    }

    private void loadPatients() {
        try {
            allPatients.clear();
            allPatients.addAll(patientDAO.getAllPatients());
            filterPatients();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // FIXED: Filter patients to show only those with orders (finished or pending) for selected day
    private void filterPatients() {
        filteredPatients.clear();

        String selectedDay = dayFilterSpinner.getSelectedItem().toString();
        String searchTerm = searchInput.getText().toString().toLowerCase().trim();

        // Extract date from selected day
        String selectedDate = getDateFromDayOption(selectedDay);

        for (Patient patient : allPatients) {
            // FIXED: Show only patients with orders (finished or pending) for the selected day
            boolean hasOrderForDay = hasOrderForDate(patient, selectedDate);

            boolean matchesSearch = searchTerm.isEmpty() ||
                    patient.getPatientFirstName().toLowerCase().contains(searchTerm) ||
                    patient.getPatientLastName().toLowerCase().contains(searchTerm) ||
                    patient.getWing().toLowerCase().contains(searchTerm) ||
                    patient.getRoomNumber().toLowerCase().contains(searchTerm);

            if (hasOrderForDay && matchesSearch) {
                filteredPatients.add(patient);
            }
        }

        if (patientsAdapter != null) {
            patientsAdapter.notifyDataSetChanged();
        }
        updatePatientsCount();
        updateBulkOperationVisibility();
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

    // FIXED: Update count to show active patients with orders for today or tomorrow
    private void updatePatientsCount() {
        // Count patients with orders for today or tomorrow
        int activeCount = 0;
        String today = getTodayDate();
        String tomorrow = getTomorrowDate();

        for (Patient patient : allPatients) {
            if (hasOrderForDate(patient, today) || hasOrderForDate(patient, tomorrow)) {
                activeCount++;
            }
        }

        if (patientsCountText != null) {
            // FIXED: Changed heading text
            patientsCountText.setText("Active Patients (" + activeCount + ")");
        }
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getTomorrowDate() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(tomorrow.getTime());
    }

    // FIXED: Open patient details with proper data passing
    private void openPatientDetails(Patient patient) {
        Intent intent = new Intent(this, PatientDetailActivity.class);
        intent.putExtra("patient_id", patient.getPatientId());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    // FEATURE: Print selected menus
    private void printSelectedMenus() {
        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "Please select patients to print menus for", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedDay = dayFilterSpinner.getSelectedItem().toString();

        new AlertDialog.Builder(this)
                .setTitle("Print Menus")
                .setMessage("Print menus for " + selectedPatients.size() + " selected patients on " + selectedDay + "?")
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

    // FEATURE: Delete selected patients
    private void deleteSelectedPatients() {
        List<Patient> selectedPatients = patientsAdapter.getSelectedPatients();
        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "Please select patients to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Patients")
                .setMessage("Are you sure you want to delete " + selectedPatients.size() + " selected patients?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int deletedCount = 0;
                    for (Patient patient : selectedPatients) {
                        if (patientDAO.deletePatient(patient.getPatientId())) {
                            deletedCount++;
                        }
                    }

                    Toast.makeText(this, "Deleted " + deletedCount + " patients", Toast.LENGTH_SHORT).show();
                    loadPatients(); // Refresh the list
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
            printMenusButton.setVisibility(View.VISIBLE); // Always show print button
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
            case R.id.action_print_all:
                // FEATURE: Print all menus for current day
                String currentDay = dayFilterSpinner.getSelectedItem().toString();
                new AlertDialog.Builder(this)
                        .setTitle("Print All Menus")
                        .setMessage("Print menus for all " + filteredPatients.size() + " patients on " + currentDay + "?")
                        .setPositiveButton("Print", (dialog, which) -> {
                            Toast.makeText(this, "Printing all menus for " + currentDay + "...", Toast.LENGTH_LONG).show();
                            // Implement print all logic here
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
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

    // Patient Adapter with checkbox support
    private class PatientAdapter extends BaseAdapter {
        private android.content.Context context;
        private List<Patient> patients;
        private Set<Integer> selectedPositions = new HashSet<>();

        public PatientAdapter(android.content.Context context, List<Patient> patients) {
            this.context = context;
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
                convertView = getLayoutInflater().inflate(R.layout.item_patient_selectable, parent, false);
            }

            Patient patient = patients.get(position);

            CheckBox selectCheckBox = convertView.findViewById(R.id.selectCheckBox);
            TextView patientNameText = convertView.findViewById(R.id.patientNameText);
            TextView roomInfoText = convertView.findViewById(R.id.roomInfoText);
            TextView dietInfoText = convertView.findViewById(R.id.dietInfoText);

            selectCheckBox.setChecked(selectedPositions.contains(position));
            selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedPositions.add(position);
                } else {
                    selectedPositions.remove(position);
                }
                updateBulkOperationVisibility();
            });

            patientNameText.setText(patient.getPatientFirstName() + " " + patient.getPatientLastName());
            roomInfoText.setText(patient.getWing() + " - Room " + patient.getRoomNumber());

            String dietInfo = patient.getDietType();
            if (patient.isAdaDiet()) {
                dietInfo += " (ADA)";
            }
            dietInfoText.setText(dietInfo);

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

        @Override
        public void notifyDataSetChanged() {
            // Clear selections that are no longer valid
            selectedPositions.removeIf(position -> position >= patients.size());
            super.notifyDataSetChanged();
        }
    }
}