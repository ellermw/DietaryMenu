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

        // Generate Sunday through Saturday
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(calendar.getTime());
            String dayName = dayNames[i];

            // Check if this is today
            Calendar today = Calendar.getInstance();
            if (calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                dayLabels.add("Today - " + date);
                todayIndex = i;
            } else {
                dayLabels.add(dayName + " - " + date);
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Update adapter
        dayFilterAdapter.notifyDataSetChanged();

        // Set selection to today if found
        if (todayIndex >= 0) {
            dayFilterSpinner.setSelection(todayIndex);
        }
    }

    private void loadPatients() {
        try {
            allPatients = patientDAO.getActivePatients();
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
                // For now, just refresh the view
                // In the future, this could filter by meal dates
                dayFilterAdapter.setSelectedPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Patient click - show details
        patientsListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = filteredPatients.get(position);
            showPatientDetailsDialog(patient);
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

        // Print menus button
        if (printMenusButton != null) {
            printMenusButton.setOnClickListener(v -> printSelectedMenus());
        }

        // Delete selected button
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setOnClickListener(v -> deleteSelectedPatients());
        }
    }

    private void showPatientDetailsDialog(Patient patient) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_patient_details, null);

        // Populate patient information
        TextView nameText = dialogView.findViewById(R.id.patientNameText);
        TextView locationText = dialogView.findViewById(R.id.patientLocationText);
        TextView dietText = dialogView.findViewById(R.id.patientDietText);
        TextView restrictionsText = dialogView.findViewById(R.id.patientRestrictionsText);
        TextView modificationsText = dialogView.findViewById(R.id.patientModificationsText);
        TextView breakfastText = dialogView.findViewById(R.id.patientBreakfastText);
        TextView lunchText = dialogView.findViewById(R.id.patientLunchText);
        TextView dinnerText = dialogView.findViewById(R.id.patientDinnerText);

        nameText.setText(patient.getFullName());
        locationText.setText(patient.getWing() + " - Room " + patient.getRoomNumber());
        dietText.setText(patient.getDietType() + (patient.isAdaDiet() ? " (ADA)" : ""));
        restrictionsText.setText(patient.getFluidRestriction());
        modificationsText.setText(patient.getTextureModifications());

        // Meal information
        breakfastText.setText(getMealStatus("Breakfast", patient.isBreakfastComplete(), patient.isBreakfastNPO()));
        lunchText.setText(getMealStatus("Lunch", patient.isLunchComplete(), patient.isLunchNPO()));
        dinnerText.setText(getMealStatus("Dinner", patient.isDinnerComplete(), patient.isDinnerNPO()));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Patient Details")
                .setView(dialogView)
                .setPositiveButton("Edit Meal Plans", (d, which) -> {
                    Intent intent = new Intent(this, MealPlanningActivity.class);
                    intent.putExtra("patient_id", (long) patient.getPatientId());
                    intent.putExtra("diet", patient.getDietType());
                    intent.putExtra("is_ada_diet", patient.isAdaDiet());
                    intent.putExtra("current_user", currentUsername);
                    intent.putExtra("user_role", currentUserRole);
                    intent.putExtra("user_full_name", currentUserFullName);
                    startActivity(intent);
                })
                .setNegativeButton("Edit Patient", (d, which) -> {
                    Intent intent = new Intent(this, NewPatientActivity.class);
                    intent.putExtra("edit_patient_id", (long) patient.getPatientId());
                    intent.putExtra("current_user", currentUsername);
                    intent.putExtra("user_role", currentUserRole);
                    intent.putExtra("user_full_name", currentUserFullName);
                    startActivity(intent);
                })
                .setNeutralButton("Close", null)
                .create();

        // Add additional buttons for admin users
        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Options", (d, which) -> {
                showPatientOptionsDialog(patient);
            });
        }

        dialog.show();
    }

    private void showPatientOptionsDialog(Patient patient) {
        String[] options = {"Edit Meal Plans", "Edit Patient Info", "Transfer Room", "Discharge Patient"};

        new AlertDialog.Builder(this)
                .setTitle("Patient Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit Meal Plans
                            Intent mealIntent = new Intent(this, MealPlanningActivity.class);
                            mealIntent.putExtra("patient_id", (long) patient.getPatientId());
                            mealIntent.putExtra("diet", patient.getDietType());
                            mealIntent.putExtra("is_ada_diet", patient.isAdaDiet());
                            mealIntent.putExtra("current_user", currentUsername);
                            mealIntent.putExtra("user_role", currentUserRole);
                            mealIntent.putExtra("user_full_name", currentUserFullName);
                            startActivity(mealIntent);
                            break;

                        case 1: // Edit Patient Info
                            Intent editIntent = new Intent(this, NewPatientActivity.class);
                            editIntent.putExtra("edit_patient_id", (long) patient.getPatientId());
                            editIntent.putExtra("current_user", currentUsername);
                            editIntent.putExtra("user_role", currentUserRole);
                            editIntent.putExtra("user_full_name", currentUserFullName);
                            startActivity(editIntent);
                            break;

                        case 2: // Transfer Room
                            showTransferRoomDialog(patient);
                            break;

                        case 3: // Discharge Patient
                            showDischargeConfirmation(patient);
                            break;
                    }
                })
                .show();
    }

    private void showTransferRoomDialog(Patient patient) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transfer_room, null);
        Spinner wingSpinner = dialogView.findViewById(R.id.transferWingSpinner);
        Spinner roomSpinner = dialogView.findViewById(R.id.transferRoomSpinner);

        // Setup wing spinner
        String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Room mappings
        Map<String, String[]> wingRoomMap = new HashMap<>();
        wingRoomMap.put("1 South", generateRoomNumbers(106, 122));
        wingRoomMap.put("2 North", generateRoomNumbers(250, 264));
        wingRoomMap.put("Labor and Delivery", new String[]{"LDR1", "LDR2", "LDR3", "LDR4", "LDR5", "LDR6"});
        wingRoomMap.put("2 West", generateRoomNumbers(225, 248));
        wingRoomMap.put("3 North", generateRoomNumbers(349, 371));
        wingRoomMap.put("ICU", new String[]{"ICU1", "ICU2", "ICU3", "ICU4", "ICU5", "ICU6", "ICU7", "ICU8", "ICU9", "ICU10", "ICU11", "ICU12", "ICU13"});

        // Wing selection listener
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                String[] rooms = wingRoomMap.get(selectedWing);
                ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(ExistingPatientsActivity.this,
                        android.R.layout.simple_spinner_item, rooms);
                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roomSpinner.setAdapter(roomAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Transfer Patient")
                .setMessage("Transfer " + patient.getFullName() + " to a new room")
                .setView(dialogView)
                .setPositiveButton("Transfer", (dialog, which) -> {
                    String newWing = wingSpinner.getSelectedItem().toString();
                    String newRoom = roomSpinner.getSelectedItem().toString();

                    // Check if new room is occupied
                    boolean occupied = false;
                    for (Patient p : allPatients) {
                        if (p.getPatientId() != patient.getPatientId() &&
                                p.getWing().equals(newWing) &&
                                p.getRoomNumber().equals(newRoom)) {
                            occupied = true;
                            break;
                        }
                    }

                    if (occupied) {
                        Toast.makeText(this, "Room " + newWing + "-" + newRoom + " is already occupied",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        patient.setWing(newWing);
                        patient.setRoomNumber(newRoom);
                        patientDAO.updatePatient(patient);
                        Toast.makeText(this, "Patient transferred successfully", Toast.LENGTH_SHORT).show();
                        loadPatients();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDischargeConfirmation(Patient patient) {
        new AlertDialog.Builder(this)
                .setTitle("Discharge Patient")
                .setMessage("Are you sure you want to discharge " + patient.getFullName() + "?\n\n" +
                        "This will move the patient to Retired Orders.")
                .setPositiveButton("Discharge", (dialog, which) -> {
                    patient.setDischarged(true);
                    patientDAO.updatePatient(patient);
                    Toast.makeText(this, "Patient discharged successfully", Toast.LENGTH_SHORT).show();
                    loadPatients();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String[] generateRoomNumbers(int start, int end) {
        String[] rooms = new String[end - start + 1];
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = String.valueOf(start + i);
        }
        return rooms;
    }

    private String getMealStatus(String meal, boolean isComplete, boolean isNPO) {
        if (isNPO) {
            return meal + ": NPO";
        } else if (isComplete) {
            return meal + ": Complete ✓";
        } else {
            return meal + ": Pending";
        }
    }

    private void updateBulkOperationVisibility() {
        int selectedCount = getSelectedPatientCount();
        if (selectedCount > 0) {
            bulkOperationsContainer.setVisibility(View.VISIBLE);
            printMenusButton.setText("Print " + selectedCount + " Menu" + (selectedCount > 1 ? "s" : ""));
            deleteSelectedButton.setText("Delete " + selectedCount + " Patient" + (selectedCount > 1 ? "s" : ""));
        } else {
            bulkOperationsContainer.setVisibility(View.GONE);
        }
    }

    private int getSelectedPatientCount() {
        SparseBooleanArray checkedItems = patientsListView.getCheckedItemPositions();
        int count = 0;
        for (int i = 0; i < checkedItems.size(); i++) {
            if (checkedItems.valueAt(i)) {
                count++;
            }
        }
        return count;
    }

    private void printSelectedMenus() {
        SparseBooleanArray checkedItems = patientsListView.getCheckedItemPositions();
        List<Patient> selectedPatients = new ArrayList<>();

        for (int i = 0; i < checkedItems.size(); i++) {
            int position = checkedItems.keyAt(i);
            if (checkedItems.valueAt(i) && position < filteredPatients.size()) {
                selectedPatients.add(filteredPatients.get(position));
            }
        }

        if (!selectedPatients.isEmpty()) {
            // TODO: Implement print functionality
            Toast.makeText(this, "Printing " + selectedPatients.size() + " menu(s)...",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSelectedPatients() {
        SparseBooleanArray checkedItems = patientsListView.getCheckedItemPositions();
        List<Patient> patientsToDelete = new ArrayList<>();

        for (int i = 0; i < checkedItems.size(); i++) {
            int position = checkedItems.keyAt(i);
            if (checkedItems.valueAt(i) && position < filteredPatients.size()) {
                patientsToDelete.add(filteredPatients.get(position));
            }
        }

        if (!patientsToDelete.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Patients")
                    .setMessage("Are you sure you want to delete " + patientsToDelete.size() +
                            " patient(s)?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        for (Patient patient : patientsToDelete) {
                            patientDAO.deletePatient(patient.getPatientId());
                        }
                        Toast.makeText(this, "Patients deleted successfully", Toast.LENGTH_SHORT).show();
                        loadPatients();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Patient adapter
    private class PatientAdapter extends BaseAdapter {
        private SparseBooleanArray selectedItems = new SparseBooleanArray();

        public void selectAll(boolean select) {
            for (int i = 0; i < filteredPatients.size(); i++) {
                selectedItems.put(i, select);
                patientsListView.setItemChecked(i, select);
            }
            notifyDataSetChanged();
            updateBulkOperationVisibility();
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
                convertView = getLayoutInflater().inflate(R.layout.item_patient, parent, false);
            }

            Patient patient = getItem(position);

            TextView nameText = convertView.findViewById(R.id.patientNameText);
            TextView locationText = convertView.findViewById(R.id.patientLocationText);
            TextView dietText = convertView.findViewById(R.id.patientDietText);
            CheckBox checkBox = convertView.findViewById(R.id.patientCheckBox);

            nameText.setText(patient.getFullName());
            locationText.setText(patient.getWing() + " - " + patient.getRoomNumber());
            dietText.setText(patient.getDietType());

            // Show completion status with color
            boolean allComplete = patient.isBreakfastComplete() &&
                    patient.isLunchComplete() &&
                    patient.isDinnerComplete();

            if (allComplete) {
                nameText.setTextColor(Color.parseColor("#27ae60"));
            } else {
                nameText.setTextColor(Color.BLACK);
            }

            // Handle checkbox visibility
            boolean isAdmin = "Admin".equalsIgnoreCase(currentUserRole);
            checkBox.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            checkBox.setChecked(patientsListView.isItemChecked(position));

            return convertView;
        }
    }

    // Day filter adapter
    private class DayFilterAdapter extends BaseAdapter {
        private int selectedPosition = -1;

        public void setSelectedPosition(int position) {
            selectedPosition = position;
            notifyDataSetChanged();
        }

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

            if (position == todayIndex) {
                textView.setTypeface(null, Typeface.BOLD);
                textView.setTextColor(Color.parseColor("#2196F3"));
            }

            return textView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) getView(position, convertView, parent);
            textView.setBackgroundColor(Color.WHITE);

            if (position == selectedPosition) {
                textView.setBackgroundColor(Color.parseColor("#E3F2FD"));
            }

            return textView;
        }
    }
}