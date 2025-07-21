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
    private CheckBox editAdaToggle;
    private Spinner editFluidRestrictionSpinner;

    // Action buttons
    private Button saveChangesButton;
    private Button cancelEditButton;
    private Button deletePatientButton;
    private Button editMealsButton;
    private Button backButton;

    // Data
    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private ArrayAdapter<Patient> patientsAdapter;
    private Patient selectedPatient;

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

        // Check if we should edit a specific patient
        int selectedPatientId = getIntent().getIntExtra("selected_patient_id", -1);
        if (selectedPatientId != -1) {
            Patient patientToEdit = patientDAO.getPatientById(selectedPatientId);
            if (patientToEdit != null) {
                selectedPatient = patientToEdit;
                populatePatientForm(selectedPatient);
                updateButtonStates();
                if (editPatientSection != null) {
                    editPatientSection.setVisibility(View.VISIBLE);
                }
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
        editAdaToggle = findViewById(R.id.editAdaToggle);
        editFluidRestrictionSpinner = findViewById(R.id.editFluidRestrictionSpinner);

        // Action buttons
        saveChangesButton = findViewById(R.id.saveChangesButton);
        cancelEditButton = findViewById(R.id.cancelEditButton);
        deletePatientButton = findViewById(R.id.deletePatientButton);
        editMealsButton = findViewById(R.id.editMealsButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupSpinners() {
        // Wing spinner
        if (editWingSpinner != null) {
            String[] wings = {"Select Wing", "1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
            ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
            wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            editWingSpinner.setAdapter(wingAdapter);

            // Set up wing selection listener to update room numbers
            editWingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateRoomNumbers();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Diet spinner
        if (editDietSpinner != null) {
            String[] dietTypes = {"Select Diet", "Regular", "ADA", "Cardiac", "Renal", "Clear Liquid", "Full Liquid", "Puree", "Mechanical Chopped", "Mechanical Ground"};
            ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietTypes);
            dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            editDietSpinner.setAdapter(dietAdapter);
        }

        // FIXED: Fluid restriction spinner with your correct system
        if (editFluidRestrictionSpinner != null) {
            String[] fluidOptions = {
                    "No Fluid Restriction",
                    "1000ml (34oz): 120ml, 120ml, 160ml",
                    "1200ml (41oz): 250ml, 170ml, 180ml",
                    "1500ml (51oz): 350ml, 170ml, 180ml",
                    "1800ml (61oz): 360ml, 240ml, 240ml",
                    "2000ml (68oz): 320ml, 240ml, 240ml",
                    "2500ml (85oz): 400ml, 400ml, 400ml"
            };
            ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidOptions);
            fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            editFluidRestrictionSpinner.setAdapter(fluidAdapter);
        }

        // Initialize room numbers
        updateRoomNumbers();
    }

    // FIXED: Added your room number system
    private void updateRoomNumbers() {
        if (editWingSpinner == null || editRoomSpinner == null) return;

        String selectedWing = editWingSpinner.getSelectedItem().toString();
        List<String> roomNumbers = new ArrayList<>();
        roomNumbers.add("Select Room");

        switch (selectedWing) {
            case "1 South":
                // Rooms 106–122
                for (int i = 106; i <= 122; i++) {
                    roomNumbers.add(String.valueOf(i));
                }
                break;
            case "2 North":
                // Rooms 250–264
                for (int i = 250; i <= 264; i++) {
                    roomNumbers.add(String.valueOf(i));
                }
                break;
            case "Labor and Delivery":
                // Rooms LDR1–LDR6
                for (int i = 1; i <= 6; i++) {
                    roomNumbers.add("LDR" + i);
                }
                break;
            case "2 West":
                // Rooms 225–248
                for (int i = 225; i <= 248; i++) {
                    roomNumbers.add(String.valueOf(i));
                }
                break;
            case "3 North":
                // Rooms 349–371
                for (int i = 349; i <= 371; i++) {
                    roomNumbers.add(String.valueOf(i));
                }
                break;
            case "ICU":
                // Rooms ICU1–ICU13
                for (int i = 1; i <= 13; i++) {
                    roomNumbers.add("ICU" + i);
                }
                break;
            default:
                // (optional) handle any other cases
                break;
        }

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roomNumbers
        );
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editRoomSpinner.setAdapter(roomAdapter);
    }

    private void setupListeners() {
        try {
            // Action button listeners
            if (saveChangesButton != null) saveChangesButton.setOnClickListener(v -> savePatientChanges());
            if (cancelEditButton != null) cancelEditButton.setOnClickListener(v -> cancelEdit());
            if (deletePatientButton != null) deletePatientButton.setOnClickListener(v -> deletePatient());
            if (editMealsButton != null) editMealsButton.setOnClickListener(v -> editPatientMeals());
            if (backButton != null) backButton.setOnClickListener(v -> finish());

            // Patient list selection
            if (patientsListView != null) {
                patientsListView.setOnItemClickListener((parent, view, position, id) -> {
                    selectedPatient = filteredPatients.get(position);
                    populatePatientForm(selectedPatient);
                    updateButtonStates();
                    if (editPatientSection != null) {
                        editPatientSection.setVisibility(View.VISIBLE);
                    }
                });
            }

            // Search functionality
            if (searchInput != null) {
                searchInput.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterPatients();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error setting up listeners: " + e.getMessage());
        }
    }

    private void loadPatients() {
        try {
            allPatients.clear();
            allPatients.addAll(patientDAO.getAllPatients());
            filterPatients();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading patients: " + e.getMessage());
        }
    }

    private void filterPatients() {
        try {
            filteredPatients.clear();
            String searchQuery = searchInput != null ? searchInput.getText().toString().toLowerCase().trim() : "";

            for (Patient patient : allPatients) {
                if (searchQuery.isEmpty() ||
                        patient.getFullName().toLowerCase().contains(searchQuery) ||
                        patient.getWing().toLowerCase().contains(searchQuery) ||
                        patient.getRoomNumber().toLowerCase().contains(searchQuery) ||
                        patient.getDiet().toLowerCase().contains(searchQuery)) {
                    filteredPatients.add(patient);
                }
            }

            // Update adapter
            if (patientsAdapter == null) {
                patientsAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_2, android.R.id.text1, filteredPatients) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        Patient patient = getItem(position);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        if (patient != null) {
                            text1.setText(patient.getFullName());
                            text2.setText(patient.getWing() + " - Room " + patient.getRoomNumber() + " • " + patient.getDiet());
                        }

                        return view;
                    }
                };
                patientsListView.setAdapter(patientsAdapter);
            } else {
                patientsAdapter.notifyDataSetChanged();
            }

            // Update count
            if (patientsCountText != null) {
                patientsCountText.setText("Showing " + filteredPatients.size() + " of " + allPatients.size() + " patients");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error filtering patients: " + e.getMessage());
        }
    }

    private void populatePatientForm(Patient patient) {
        if (patient == null) return;

        try {
            // Basic info
            if (editFirstNameInput != null) editFirstNameInput.setText(patient.getPatientFirstName());
            if (editLastNameInput != null) editLastNameInput.setText(patient.getPatientLastName());

            // Wing selection
            if (editWingSpinner != null) {
                ArrayAdapter<String> wingAdapter = (ArrayAdapter<String>) editWingSpinner.getAdapter();
                int wingPosition = wingAdapter.getPosition(patient.getWing());
                if (wingPosition >= 0) {
                    editWingSpinner.setSelection(wingPosition);
                }
            }

            // Room selection (after wing is set, rooms should be populated)
            if (editRoomSpinner != null) {
                // Wait for room numbers to be updated, then set selection
                editRoomSpinner.post(() -> {
                    ArrayAdapter<String> roomAdapter = (ArrayAdapter<String>) editRoomSpinner.getAdapter();
                    int roomPosition = roomAdapter.getPosition(patient.getRoomNumber());
                    if (roomPosition >= 0) {
                        editRoomSpinner.setSelection(roomPosition);
                    }
                });
            }

            // Diet selection
            if (editDietSpinner != null && editAdaToggle != null) {
                String baseDiet = patient.getDiet();
                boolean isAda = patient.isAdaDiet();

                ArrayAdapter<String> dietAdapter = (ArrayAdapter<String>) editDietSpinner.getAdapter();
                int dietPosition = dietAdapter.getPosition(baseDiet);
                if (dietPosition >= 0) {
                    editDietSpinner.setSelection(dietPosition);
                }

                editAdaToggle.setChecked(isAda);
            }

            // FIXED: Fluid restriction selection with your system
            if (editFluidRestrictionSpinner != null) {
                String fluidRestriction = patient.getFluidRestriction() != null ? patient.getFluidRestriction() : "No Fluid Restriction";
                ArrayAdapter<String> fluidAdapter = (ArrayAdapter<String>) editFluidRestrictionSpinner.getAdapter();

                // Find matching fluid restriction
                for (int i = 0; i < fluidAdapter.getCount(); i++) {
                    String option = fluidAdapter.getItem(i);
                    if ((fluidRestriction.equals("No Fluid Restriction") && option.equals("No Fluid Restriction")) ||
                            (option.contains(fluidRestriction.split("ml")[0] + "ml") && fluidRestriction.contains("ml"))) {
                        editFluidRestrictionSpinner.setSelection(i);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error populating patient form: " + e.getMessage());
        }
    }

    private void savePatientChanges() {
        try {
            if (selectedPatient == null) {
                showError("No patient selected for editing");
                return;
            }

            // Get form values
            String firstName = editFirstNameInput != null ? editFirstNameInput.getText().toString().trim() : "";
            String lastName = editLastNameInput != null ? editLastNameInput.getText().toString().trim() : "";

            // Validate inputs
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showError("First name and last name are required");
                return;
            }

            // Update patient object
            selectedPatient.setPatientFirstName(firstName);
            selectedPatient.setPatientLastName(lastName);

            if (editWingSpinner != null && editWingSpinner.getSelectedItemPosition() > 0) {
                selectedPatient.setWing(editWingSpinner.getSelectedItem().toString());
            }

            if (editRoomSpinner != null && editRoomSpinner.getSelectedItemPosition() > 0) {
                selectedPatient.setRoomNumber(editRoomSpinner.getSelectedItem().toString());
            }

            if (editDietSpinner != null && editDietSpinner.getSelectedItemPosition() > 0) {
                selectedPatient.setDiet(editDietSpinner.getSelectedItem().toString());
                selectedPatient.setAdaDiet(editAdaToggle != null ? editAdaToggle.isChecked() : false);
            }

            // FIXED: Fluid restriction handling with your system
            if (editFluidRestrictionSpinner != null) {
                String fluidRestriction = editFluidRestrictionSpinner.getSelectedItem().toString();
                selectedPatient.setFluidRestriction(fluidRestriction.equals("No Fluid Restriction") ? "" : fluidRestriction);
            }

            // Save to database
            boolean success = patientDAO.updatePatient(selectedPatient);

            if (success) {
                showMessage("Patient " + firstName + " " + lastName + " updated successfully!");
                loadPatients();
                if (editPatientSection != null) {
                    editPatientSection.setVisibility(View.GONE);
                }
                selectedPatient = null;
                updateButtonStates();
            } else {
                showError("Failed to update patient");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error saving changes: " + e.getMessage());
        }
    }

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
        if (editPatientSection != null) {
            editPatientSection.setVisibility(View.GONE);
        }
        selectedPatient = null;
        updateButtonStates();
    }

    private void deletePatient() {
        if (selectedPatient == null) {
            showError("No patient selected for deletion");
            return;
        }

        String patientInfo = selectedPatient.getFullName() + " - " + selectedPatient.getWing() + " Room " + selectedPatient.getRoomNumber();

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete Patient")
                .setMessage("Are you sure you want to permanently delete " + patientInfo +
                        "?\n\nThis will remove:\n• Patient information\n• All meal orders\n• Order history\n\nThis action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = patientDAO.deletePatient(selectedPatient.getPatientId());
                    if (success) {
                        showMessage("Patient deleted successfully");
                        loadPatients();
                        if (editPatientSection != null) {
                            editPatientSection.setVisibility(View.GONE);
                        }
                        selectedPatient = null;
                        updateButtonStates();
                    } else {
                        showError("Failed to delete patient");
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
        Toast.makeText(this, "❌ " + message, Toast.LENGTH_LONG).show();
    }

    private void showMessage(String message) {
        Toast.makeText(this, "✅ " + message, Toast.LENGTH_SHORT).show();
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
}