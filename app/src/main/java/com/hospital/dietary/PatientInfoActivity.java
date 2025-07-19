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
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatientInfoActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components - Patient List Section
    private ListView patientsListView;
    private EditText searchInput;
    private TextView patientsCountText;

    // UI Components - Patient Edit Section
    private LinearLayout editPatientSection;
    private EditText editFirstNameInput;
    private EditText editLastNameInput;
    private Spinner editWingSpinner;
    private Spinner editRoomSpinner;
    private Spinner editDietSpinner;
    private CheckBox editAdaToggle; // FIXED: ADA toggle for editing
    private Spinner editFluidRestrictionSpinner;

    // Texture modification checkboxes for editing
    private CheckBox editPureedCheckBox;
    private CheckBox editChoppedCheckBox;
    private CheckBox editSoftCheckBox;
    private CheckBox editRegularCheckBox;

    // Meal completion checkboxes for editing
    private CheckBox editBreakfastCompleteCheckBox;
    private CheckBox editLunchCompleteCheckBox;
    private CheckBox editDinnerCompleteCheckBox;

    // Action buttons
    private Button saveChangesButton;
    private Button cancelEditButton;
    private Button deletePatientButton;
    private Button editMealsButton; // FIXED: Edit meal selections button
    private Button backButton;

    // Data
    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private ArrayAdapter<Patient> patientsAdapter;
    private Patient selectedPatient;

    // FIXED: Simple TextWatcher class
    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Patients");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupSpinners();
        setupListeners();
        loadPatients();

        // FIXED: Check if we should edit a specific patient
        int editPatientId = getIntent().getIntExtra("edit_patient_id", -1);
        if (editPatientId != -1) {
            Patient patientToEdit = patientDAO.getPatientById(editPatientId);
            if (patientToEdit != null) {
                selectedPatient = patientToEdit;
                populatePatientForm(selectedPatient);
                updateButtonStates();
                editPatientSection.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initializeUI() {
        // Patient list section
        patientsListView = findViewById(R.id.patientsListView);
        searchInput = findViewById(R.id.searchInput);
        patientsCountText = findViewById(R.id.patientsCountText);

        // Patient edit section
        editPatientSection = findViewById(R.id.editPatientSection);
        editFirstNameInput = findViewById(R.id.editFirstNameInput);
        editLastNameInput = findViewById(R.id.editLastNameInput);
        editWingSpinner = findViewById(R.id.editWingSpinner);
        editRoomSpinner = findViewById(R.id.editRoomSpinner);
        editDietSpinner = findViewById(R.id.editDietSpinner);
        editAdaToggle = findViewById(R.id.editAdaToggle); // FIXED: ADA toggle
        editFluidRestrictionSpinner = findViewById(R.id.editFluidRestrictionSpinner);

        // Texture modifications
        editPureedCheckBox = findViewById(R.id.editPureedCheckBox);
        editChoppedCheckBox = findViewById(R.id.editChoppedCheckBox);
        editSoftCheckBox = findViewById(R.id.editSoftCheckBox);
        editRegularCheckBox = findViewById(R.id.editRegularCheckBox);

        // Meal completion
        editBreakfastCompleteCheckBox = findViewById(R.id.editBreakfastCompleteCheckBox);
        editLunchCompleteCheckBox = findViewById(R.id.editLunchCompleteCheckBox);
        editDinnerCompleteCheckBox = findViewById(R.id.editDinnerCompleteCheckBox);

        // Action buttons
        saveChangesButton = findViewById(R.id.saveChangesButton);
        cancelEditButton = findViewById(R.id.cancelEditButton);
        deletePatientButton = findViewById(R.id.deletePatientButton);
        editMealsButton = findViewById(R.id.editMealsButton); // FIXED: Edit meals button
        backButton = findViewById(R.id.backButton);

        // Initially hide edit section
        editPatientSection.setVisibility(View.GONE);
    }

    private void setupSpinners() {
        // Wing options
        String[] wings = {"North Wing", "South Wing", "East Wing", "West Wing", "ICU", "Emergency"};
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editWingSpinner.setAdapter(wingAdapter);

        // Room numbers
        List<String> rooms = new ArrayList<>();
        for (int i = 100; i <= 599; i++) {
            rooms.add(String.valueOf(i));
        }
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editRoomSpinner.setAdapter(roomAdapter);

        // FIXED: Diet options (simplified, no redundant ADA)
        String[] diets = {
                "Regular", "Cardiac", "ADA Diabetic", "Puree", "Renal", "Full Liquid", "Clear Liquid"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editDietSpinner.setAdapter(dietAdapter);

        // Fluid restrictions
        String[] fluidOptions = {"None", "1000ml", "1500ml", "2000ml", "NPO"};
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidOptions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editFluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void setupListeners() {
        try {
            // Action button listeners
            saveChangesButton.setOnClickListener(v -> savePatientChanges());
            cancelEditButton.setOnClickListener(v -> cancelEdit());
            deletePatientButton.setOnClickListener(v -> deletePatient());
            editMealsButton.setOnClickListener(v -> editPatientMeals()); // FIXED: Edit meals
            backButton.setOnClickListener(v -> finish());

            // Patient list selection
            patientsListView.setOnItemClickListener((parent, view, position, id) -> {
                selectedPatient = filteredPatients.get(position);
                populatePatientForm(selectedPatient);
                updateButtonStates();
            });

            // Search functionality
            searchInput.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterPatients();
                }
            });

            // FIXED: Diet spinner listener for ADA toggle visibility
            editDietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDiet = editDietSpinner.getSelectedItem().toString();

                    // Only show ADA toggle for Clear Liquid diet
                    if ("Clear Liquid".equals(selectedDiet)) {
                        editAdaToggle.setVisibility(View.VISIBLE);
                    } else {
                        editAdaToggle.setVisibility(View.GONE);
                        editAdaToggle.setChecked(false);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Texture modification - only allow one selection
            setupTextureModificationListeners();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to setup listeners: " + e.getMessage());
        }
    }

    private void setupTextureModificationListeners() {
        CompoundButton.OnCheckedChangeListener textureListener = (buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck all other texture modifications
                if (buttonView != editPureedCheckBox) editPureedCheckBox.setChecked(false);
                if (buttonView != editChoppedCheckBox) editChoppedCheckBox.setChecked(false);
                if (buttonView != editSoftCheckBox) editSoftCheckBox.setChecked(false);
                if (buttonView != editRegularCheckBox) editRegularCheckBox.setChecked(false);

                // Check the selected one
                buttonView.setChecked(true);
            }
        };

        editPureedCheckBox.setOnCheckedChangeListener(textureListener);
        editChoppedCheckBox.setOnCheckedChangeListener(textureListener);
        editSoftCheckBox.setOnCheckedChangeListener(textureListener);
        editRegularCheckBox.setOnCheckedChangeListener(textureListener);
    }

    private void loadPatients() {
        try {
            allPatients = patientDAO.getAllPatients();
            filteredPatients.clear();
            filteredPatients.addAll(allPatients);

            // Create simple adapter for patient list
            patientsAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_1, filteredPatients) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                    }

                    Patient patient = getItem(position);
                    TextView textView = convertView.findViewById(android.R.id.text1);
                    textView.setText(patient.getPatientFirstName() + " " + patient.getPatientLastName() +
                            " - " + patient.getWing() + " Room " + patient.getRoomNumber());

                    return convertView;
                }
            };

            patientsListView.setAdapter(patientsAdapter);
            updatePatientCount();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load patients: " + e.getMessage());
        }
    }

    private void filterPatients() {
        try {
            String searchTerm = searchInput.getText().toString().toLowerCase().trim();
            filteredPatients.clear();

            if (searchTerm.isEmpty()) {
                filteredPatients.addAll(allPatients);
            } else {
                for (Patient patient : allPatients) {
                    if (patient.getPatientFirstName().toLowerCase().contains(searchTerm) ||
                            patient.getPatientLastName().toLowerCase().contains(searchTerm) ||
                            patient.getWing().toLowerCase().contains(searchTerm) ||
                            patient.getRoomNumber().contains(searchTerm)) {
                        filteredPatients.add(patient);
                    }
                }
            }

            patientsAdapter.notifyDataSetChanged();
            updatePatientCount();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePatientCount() {
        if (patientsCountText != null) {
            patientsCountText.setText("Patients: " + filteredPatients.size() + " of " + allPatients.size());
        }
    }

    /**
     * FIXED: Populate form with patient data for editing
     */
    private void populatePatientForm(Patient patient) {
        try {
            editFirstNameInput.setText(patient.getPatientFirstName());
            editLastNameInput.setText(patient.getPatientLastName());

            // Set wing spinner
            ArrayAdapter<String> wingAdapter = (ArrayAdapter<String>) editWingSpinner.getAdapter();
            int wingPosition = wingAdapter.getPosition(patient.getWing());
            if (wingPosition >= 0) {
                editWingSpinner.setSelection(wingPosition);
            }

            // Set room spinner
            ArrayAdapter<String> roomAdapter = (ArrayAdapter<String>) editRoomSpinner.getAdapter();
            int roomPosition = roomAdapter.getPosition(patient.getRoomNumber());
            if (roomPosition >= 0) {
                editRoomSpinner.setSelection(roomPosition);
            }

            // FIXED: Handle diet and ADA toggle
            String baseDiet = patient.getDiet();
            boolean isAda = false;

            if (baseDiet != null && baseDiet.contains("ADA")) {
                if (baseDiet.equals("Clear Liquid ADA")) {
                    baseDiet = "Clear Liquid";
                    isAda = true;
                } else {
                    // For other ADA diets, convert back to base diet
                    baseDiet = baseDiet.replace(" ADA", "");
                }
            }

            ArrayAdapter<String> dietAdapter = (ArrayAdapter<String>) editDietSpinner.getAdapter();
            int dietPosition = dietAdapter.getPosition(baseDiet);
            if (dietPosition >= 0) {
                editDietSpinner.setSelection(dietPosition);
            }

            // Set ADA toggle
            editAdaToggle.setChecked(isAda);
            editAdaToggle.setVisibility("Clear Liquid".equals(baseDiet) ? View.VISIBLE : View.GONE);

            // Set fluid restriction
            String fluidRestriction = patient.getFluidRestriction() != null ? patient.getFluidRestriction() : "None";
            ArrayAdapter<String> fluidAdapter = (ArrayAdapter<String>) editFluidRestrictionSpinner.getAdapter();
            int fluidPosition = fluidAdapter.getPosition(fluidRestriction);
            if (fluidPosition >= 0) {
                editFluidRestrictionSpinner.setSelection(fluidPosition);
            }

            // Set texture modifications
            String texture = patient.getTextureModifications();
            editPureedCheckBox.setChecked("Pureed".equals(texture));
            editChoppedCheckBox.setChecked("Chopped".equals(texture));
            editSoftCheckBox.setChecked("Soft".equals(texture));
            editRegularCheckBox.setChecked(texture == null || "Regular".equals(texture));

            // Set meal completion status
            editBreakfastCompleteCheckBox.setChecked(patient.isBreakfastComplete());
            editLunchCompleteCheckBox.setChecked(patient.isLunchComplete());
            editDinnerCompleteCheckBox.setChecked(patient.isDinnerComplete());

            // Show edit section
            editPatientSection.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to populate form: " + e.getMessage());
        }
    }

    /**
     * FIXED: Save patient changes with full validation
     */
    private void savePatientChanges() {
        try {
            if (selectedPatient == null) {
                showError("No patient selected for editing");
                return;
            }

            // Validate input
            String firstName = editFirstNameInput.getText().toString().trim();
            String lastName = editLastNameInput.getText().toString().trim();

            if (firstName.isEmpty()) {
                showError("First name is required");
                return;
            }
            if (lastName.isEmpty()) {
                showError("Last name is required");
                return;
            }

            // Update patient object
            selectedPatient.setPatientFirstName(firstName);
            selectedPatient.setPatientLastName(lastName);
            selectedPatient.setWing(editWingSpinner.getSelectedItem().toString());
            selectedPatient.setRoomNumber(editRoomSpinner.getSelectedItem().toString());

            // FIXED: Handle diet with ADA toggle
            String baseDiet = editDietSpinner.getSelectedItem().toString();
            String finalDiet = baseDiet;
            if ("Clear Liquid".equals(baseDiet) && editAdaToggle.isChecked()) {
                finalDiet = "Clear Liquid ADA";
            }
            selectedPatient.setDiet(finalDiet);

            String fluidRestriction = editFluidRestrictionSpinner.getSelectedItem().toString();
            selectedPatient.setFluidRestriction("None".equals(fluidRestriction) ? null : fluidRestriction);

            // Update texture modifications
            String textureModification = "Regular";
            if (editPureedCheckBox.isChecked()) textureModification = "Pureed";
            else if (editChoppedCheckBox.isChecked()) textureModification = "Chopped";
            else if (editSoftCheckBox.isChecked()) textureModification = "Soft";
            selectedPatient.setTextureModifications(textureModification);

            // Update meal completion status
            selectedPatient.setBreakfastComplete(editBreakfastCompleteCheckBox.isChecked());
            selectedPatient.setLunchComplete(editLunchCompleteCheckBox.isChecked());
            selectedPatient.setDinnerComplete(editDinnerCompleteCheckBox.isChecked());

            // Save to database
            boolean success = patientDAO.updatePatient(selectedPatient);

            if (success) {
                Toast.makeText(this, "Patient " + firstName + " " + lastName + " updated successfully!",
                        Toast.LENGTH_LONG).show();

                // Refresh patient list and hide edit section
                loadPatients();
                cancelEdit();
            } else {
                showError("Failed to save changes. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Save error: " + e.getMessage());
        }
    }

    /**
     * FIXED: Edit patient meals functionality
     */
    private void editPatientMeals() {
        if (selectedPatient == null) {
            showError("No patient selected");
            return;
        }

        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.putExtra("patient_id", selectedPatient.getPatientId());
        intent.putExtra("patient_name", selectedPatient.getFullName());
        intent.putExtra("wing", selectedPatient.getWing());
        intent.putExtra("room", selectedPatient.getRoomNumber());
        intent.putExtra("diet", selectedPatient.getDiet());
        intent.putExtra("fluid_restriction", selectedPatient.getFluidRestriction());
        intent.putExtra("texture_modifications", selectedPatient.getTextureModifications());
        startActivity(intent);
    }

    private void cancelEdit() {
        editPatientSection.setVisibility(View.GONE);
        selectedPatient = null;
        updateButtonStates();
    }

    private void deletePatient() {
        if (selectedPatient == null) {
            showError("No patient selected for deletion");
            return;
        }

        String patientInfo = selectedPatient.getFullName() + " - " + selectedPatient.getLocationInfo();

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete Patient")
                .setMessage("Are you sure you want to permanently delete " + patientInfo +
                        "?\n\nThis will remove:\n• Patient information\n• All meal orders\n• Order history\n\nThis action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        boolean success = patientDAO.deletePatient(selectedPatient.getPatientId());

                        if (success) {
                            Toast.makeText(this, "Patient " + selectedPatient.getFullName() +
                                    " has been deleted.", Toast.LENGTH_LONG).show();

                            // Refresh list and hide edit section
                            loadPatients();
                            cancelEdit();
                        } else {
                            showError("Failed to delete patient. Please try again.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Delete error: " + e.getMessage());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedPatient != null;

        if (saveChangesButton != null) saveChangesButton.setEnabled(hasSelection);
        if (cancelEditButton != null) cancelEditButton.setEnabled(hasSelection);
        if (deletePatientButton != null) deletePatientButton.setEnabled(hasSelection);
        if (editMealsButton != null) editMealsButton.setEnabled(hasSelection);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
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
        // Refresh patient list when returning from meal planning
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