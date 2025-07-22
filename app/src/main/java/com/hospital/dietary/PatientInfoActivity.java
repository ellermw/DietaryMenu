package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class PatientInfoActivity extends AppCompatActivity {

    private static final String TAG = "PatientInfoActivity";
    private static final int REQUEST_NEW_PATIENT = 1001;
    private static final int REQUEST_EDIT_PATIENT = 1002;

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Button newPatientButton, pendingOrdersButton, retiredOrdersButton;
    private ListView existingPatientsListView;
    private TextView noPatientsText;
    private EditText searchEditText;
    private Button searchButton, clearSearchButton;

    // Edit patient dialog components
    private AlertDialog editPatientDialog;
    private EditText editFirstNameEditText, editLastNameEditText;
    private Spinner editWingSpinner, editRoomSpinner, editDietSpinner, editFluidRestrictionSpinner;
    private LinearLayout editAdaToggleLayout;
    private Switch editAdaSwitch;

    // Texture modification checkboxes for edit dialog
    private CheckBox editMechanicalChoppedCheckBox, editMechanicalGroundCheckBox, editBiteSizeCheckBox;
    private CheckBox editBreadOKCheckBox, editNectarThickCheckBox, editPuddingThickCheckBox, editHoneyThickCheckBox;
    private CheckBox editExtraGravyCheckBox, editMeatsOnlyCheckBox;
    private LinearLayout editMeatsOnlyLayout;

    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> displayedPatients = new ArrayList<>();
    private ArrayAdapter<Patient> patientsAdapter;
    private Patient selectedPatient;

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
            getSupportActionBar().setTitle("Patient Information");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupListeners();
        loadPatients();
    }

    private void initializeViews() {
        // Main buttons
        newPatientButton = findViewById(R.id.newPatientButton);
        pendingOrdersButton = findViewById(R.id.pendingOrdersButton);
        retiredOrdersButton = findViewById(R.id.retiredOrdersButton);

        // Patient list
        existingPatientsListView = findViewById(R.id.existingPatientsListView);
        noPatientsText = findViewById(R.id.noPatientsText);

        // Search functionality
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        clearSearchButton = findViewById(R.id.clearSearchButton);
    }

    private void setupListeners() {
        newPatientButton.setOnClickListener(v -> openNewPatient());
        pendingOrdersButton.setOnClickListener(v -> openPendingOrders());
        retiredOrdersButton.setOnClickListener(v -> openRetiredOrders());

        searchButton.setOnClickListener(v -> performSearch());
        clearSearchButton.setOnClickListener(v -> clearSearch());

        // Set up patients list click listeners
        existingPatientsListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = displayedPatients.get(position);
            showPatientOptionsDialog(patient);
        });

        existingPatientsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Patient patient = displayedPatients.get(position);
            showDeleteConfirmationDialog(patient);
            return true;
        });
    }

    private void loadPatients() {
        try {
            allPatients.clear();
            allPatients.addAll(patientDAO.getAllPatients());

            updateDisplayedPatients();
            Log.d(TAG, "Loaded " + allPatients.size() + " patients");

        } catch (Exception e) {
            Toast.makeText(this, "Error loading patients: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error loading patients", e);
        }
    }

    private void updateDisplayedPatients() {
        displayedPatients.clear();
        displayedPatients.addAll(allPatients);

        if (displayedPatients.isEmpty()) {
            existingPatientsListView.setVisibility(View.GONE);
            noPatientsText.setVisibility(View.VISIBLE);
            noPatientsText.setText("No patients found");
        } else {
            existingPatientsListView.setVisibility(View.VISIBLE);
            noPatientsText.setVisibility(View.GONE);

            // Create custom adapter for patient display
            patientsAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_2, android.R.id.text1, displayedPatients) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    Patient patient = getItem(position);
                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);

                    if (patient != null) {
                        text1.setText(patient.getFullName());
                        text2.setText(patient.getLocationInfo() + " • " + patient.getDiet());
                    }

                    return view;
                }
            };

            existingPatientsListView.setAdapter(patientsAdapter);
        }
    }

    private void performSearch() {
        String searchTerm = searchEditText.getText().toString().trim();

        if (searchTerm.isEmpty()) {
            clearSearch();
            return;
        }

        try {
            List<Patient> searchResults = patientDAO.searchPatients(searchTerm);
            displayedPatients.clear();
            displayedPatients.addAll(searchResults);

            if (patientsAdapter != null) {
                patientsAdapter.notifyDataSetChanged();
            }

            if (displayedPatients.isEmpty()) {
                noPatientsText.setVisibility(View.VISIBLE);
                noPatientsText.setText("No patients found matching \"" + searchTerm + "\"");
                existingPatientsListView.setVisibility(View.GONE);
            } else {
                existingPatientsListView.setVisibility(View.VISIBLE);
                noPatientsText.setVisibility(View.GONE);
            }

            Log.d(TAG, "Search for '" + searchTerm + "' returned " + searchResults.size() + " results");

        } catch (Exception e) {
            Toast.makeText(this, "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error performing search", e);
        }
    }

    private void clearSearch() {
        searchEditText.setText("");
        updateDisplayedPatients();
    }

    private void showPatientOptionsDialog(Patient patient) {
        selectedPatient = patient;

        String[] options = {"Edit Patient", "View Details", "Plan Meals"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(patient.getFullName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit Patient
                            showEditPatientDialog(patient);
                            break;
                        case 1: // View Details
                            showPatientDetailsDialog(patient);
                            break;
                        case 2: // Plan Meals
                            openMealPlanning(patient);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditPatientDialog(Patient patient) {
        // Inflate the edit patient dialog layout
        View editView = getLayoutInflater().inflate(R.layout.dialog_edit_patient, null);

        // Initialize edit dialog components
        initializeEditDialogViews(editView);
        setupEditDialogSpinners();
        populateEditDialog(patient);
        setupEditDialogListeners();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Patient: " + patient.getFullName())
                .setView(editView)
                .setPositiveButton("Save", (dialog, which) -> savePatientChanges(patient))
                .setNegativeButton("Cancel", null);

        editPatientDialog = builder.create();
        editPatientDialog.show();
    }

    private void initializeEditDialogViews(View editView) {
        editFirstNameEditText = editView.findViewById(R.id.editFirstNameEditText);
        editLastNameEditText = editView.findViewById(R.id.editLastNameEditText);
        editWingSpinner = editView.findViewById(R.id.editWingSpinner);
        editRoomSpinner = editView.findViewById(R.id.editRoomSpinner);
        editDietSpinner = editView.findViewById(R.id.editDietSpinner);
        editFluidRestrictionSpinner = editView.findViewById(R.id.editFluidRestrictionSpinner);
        editAdaToggleLayout = editView.findViewById(R.id.editAdaToggleLayout);
        editAdaSwitch = editView.findViewById(R.id.editAdaSwitch);

        // Texture modification checkboxes
        editMechanicalChoppedCheckBox = editView.findViewById(R.id.editMechanicalChoppedCheckBox);
        editMechanicalGroundCheckBox = editView.findViewById(R.id.editMechanicalGroundCheckBox);
        editBiteSizeCheckBox = editView.findViewById(R.id.editBiteSizeCheckBox);
        editBreadOKCheckBox = editView.findViewById(R.id.editBreadOKCheckBox);
        editNectarThickCheckBox = editView.findViewById(R.id.editNectarThickCheckBox);
        editPuddingThickCheckBox = editView.findViewById(R.id.editPuddingThickCheckBox);
        editHoneyThickCheckBox = editView.findViewById(R.id.editHoneyThickCheckBox);
        editExtraGravyCheckBox = editView.findViewById(R.id.editExtraGravyCheckBox);
        editMeatsOnlyCheckBox = editView.findViewById(R.id.editMeatsOnlyCheckBox);
        editMeatsOnlyLayout = editView.findViewById(R.id.editMeatsOnlyLayout);
    }

    private void setupEditDialogSpinners() {
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

    private void populateEditDialog(Patient patient) {
        if (editFirstNameEditText != null) {
            editFirstNameEditText.setText(patient.getPatientFirstName());
        }
        if (editLastNameEditText != null) {
            editLastNameEditText.setText(patient.getPatientLastName());
        }

        // Set wing selection
        if (editWingSpinner != null) {
            ArrayAdapter<String> wingAdapter = (ArrayAdapter<String>) editWingSpinner.getAdapter();
            int wingPosition = wingAdapter.getPosition(patient.getWing());
            editWingSpinner.setSelection(wingPosition);
        }

        // Set room selection (after wing is set, updateRoomNumbers will be called)
        editWingSpinner.post(() -> {
            if (editRoomSpinner != null) {
                ArrayAdapter<String> roomAdapter = (ArrayAdapter<String>) editRoomSpinner.getAdapter();
                int roomPosition = roomAdapter.getPosition(patient.getRoomNumber());
                editRoomSpinner.setSelection(roomPosition);
            }
        });

        // Set diet selection
        if (editDietSpinner != null) {
            ArrayAdapter<String> dietAdapter = (ArrayAdapter<String>) editDietSpinner.getAdapter();
            String dietToSelect = patient.getDiet().replace(" (ADA)", ""); // Remove ADA suffix for spinner
            int dietPosition = dietAdapter.getPosition(dietToSelect);
            editDietSpinner.setSelection(dietPosition);
        }

        // Set ADA toggle
        if (editAdaSwitch != null) {
            editAdaSwitch.setChecked(patient.isAdaDiet());
        }

        // Set fluid restriction
        if (editFluidRestrictionSpinner != null) {
            ArrayAdapter<String> fluidAdapter = (ArrayAdapter<String>) editFluidRestrictionSpinner.getAdapter();
            int fluidPosition = fluidAdapter.getPosition(patient.getFluidRestriction());
            editFluidRestrictionSpinner.setSelection(fluidPosition);
        }

        // Set texture modification checkboxes
        if (editMechanicalChoppedCheckBox != null) {
            editMechanicalChoppedCheckBox.setChecked(patient.isMechanicalChopped());
        }
        if (editMechanicalGroundCheckBox != null) {
            editMechanicalGroundCheckBox.setChecked(patient.isMechanicalGround());
        }
        if (editBiteSizeCheckBox != null) {
            editBiteSizeCheckBox.setChecked(patient.isBiteSize());
        }
        if (editBreadOKCheckBox != null) {
            editBreadOKCheckBox.setChecked(patient.isBreadOK());
        }
        if (editNectarThickCheckBox != null) {
            editNectarThickCheckBox.setChecked(patient.isNectarThick());
        }
        if (editPuddingThickCheckBox != null) {
            editPuddingThickCheckBox.setChecked(patient.isPuddingThick());
        }
        if (editHoneyThickCheckBox != null) {
            editHoneyThickCheckBox.setChecked(patient.isHoneyThick());
        }
        if (editExtraGravyCheckBox != null) {
            editExtraGravyCheckBox.setChecked(patient.isExtraGravy());
        }
        if (editMeatsOnlyCheckBox != null) {
            editMeatsOnlyCheckBox.setChecked(patient.isMeatsOnly());
        }

        // Show/hide meats only layout based on mechanical modifications
        updateMeatsOnlyVisibility();
    }

    private void setupEditDialogListeners() {
        // Diet selection listener to show/hide ADA toggle
        if (editDietSpinner != null) {
            editDietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDiet = editDietSpinner.getSelectedItem().toString();

                    // Show ADA toggle only for Clear Liquid, Full Liquid, and Puree diets
                    if (selectedDiet.equals("Clear Liquid") || selectedDiet.equals("Full Liquid") || selectedDiet.equals("Puree")) {
                        editAdaToggleLayout.setVisibility(View.VISIBLE);
                    } else {
                        editAdaToggleLayout.setVisibility(View.GONE);
                        editAdaSwitch.setChecked(false);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        // Texture modification listeners
        View.OnClickListener textureListener = v -> updateMeatsOnlyVisibility();

        if (editMechanicalChoppedCheckBox != null) {
            editMechanicalChoppedCheckBox.setOnClickListener(textureListener);
        }
        if (editMechanicalGroundCheckBox != null) {
            editMechanicalGroundCheckBox.setOnClickListener(textureListener);
        }
    }

    private void updateMeatsOnlyVisibility() {
        if (editMeatsOnlyLayout != null && editMechanicalChoppedCheckBox != null && editMechanicalGroundCheckBox != null) {
            boolean showMeatsOnly = editMechanicalChoppedCheckBox.isChecked() || editMechanicalGroundCheckBox.isChecked();
            editMeatsOnlyLayout.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);

            if (!showMeatsOnly && editMeatsOnlyCheckBox != null) {
                editMeatsOnlyCheckBox.setChecked(false);
            }
        }
    }

    private void savePatientChanges(Patient patient) {
        try {
            // Update patient with new values
            if (editFirstNameEditText != null) {
                patient.setPatientFirstName(editFirstNameEditText.getText().toString().trim());
            }
            if (editLastNameEditText != null) {
                patient.setPatientLastName(editLastNameEditText.getText().toString().trim());
            }
            if (editWingSpinner != null) {
                patient.setWing(editWingSpinner.getSelectedItem().toString());
            }
            if (editRoomSpinner != null) {
                patient.setRoomNumber(editRoomSpinner.getSelectedItem().toString());
            }

            // Handle diet and ADA
            if (editDietSpinner != null) {
                String selectedDiet = editDietSpinner.getSelectedItem().toString();
                boolean isAdaSelected = editAdaSwitch != null && editAdaSwitch.isChecked();

                if (isAdaSelected) {
                    patient.setDiet(selectedDiet + " (ADA)");
                    patient.setAdaDiet(true);
                } else {
                    patient.setDiet(selectedDiet);
                    patient.setAdaDiet(false);
                }
            }

            if (editFluidRestrictionSpinner != null) {
                patient.setFluidRestriction(editFluidRestrictionSpinner.getSelectedItem().toString());
            }

            // Update texture modifications
            if (editMechanicalChoppedCheckBox != null) {
                patient.setMechanicalChopped(editMechanicalChoppedCheckBox.isChecked());
            }
            if (editMechanicalGroundCheckBox != null) {
                patient.setMechanicalGround(editMechanicalGroundCheckBox.isChecked());
            }
            if (editBiteSizeCheckBox != null) {
                patient.setBiteSize(editBiteSizeCheckBox.isChecked());
            }
            if (editBreadOKCheckBox != null) {
                patient.setBreadOK(editBreadOKCheckBox.isChecked());
            }
            if (editNectarThickCheckBox != null) {
                patient.setNectarThick(editNectarThickCheckBox.isChecked());
            }
            if (editPuddingThickCheckBox != null) {
                patient.setPuddingThick(editPuddingThickCheckBox.isChecked());
            }
            if (editHoneyThickCheckBox != null) {
                patient.setHoneyThick(editHoneyThickCheckBox.isChecked());
            }
            if (editExtraGravyCheckBox != null) {
                patient.setExtraGravy(editExtraGravyCheckBox.isChecked());
            }
            if (editMeatsOnlyCheckBox != null) {
                patient.setMeatsOnly(editMeatsOnlyCheckBox.isChecked());
            }

            // Update texture modifications string
            List<String> textureModsList = new ArrayList<>();
            if (patient.isMechanicalChopped()) textureModsList.add("Mechanical Chopped");
            if (patient.isMechanicalGround()) textureModsList.add("Mechanical Ground");
            if (patient.isBiteSize()) textureModsList.add("Bite Size");
            if (!patient.isBreadOK()) textureModsList.add("No Bread");
            if (patient.isNectarThick()) textureModsList.add("Nectar Thick");
            if (patient.isPuddingThick()) textureModsList.add("Pudding Thick");
            if (patient.isHoneyThick()) textureModsList.add("Honey Thick");
            if (patient.isExtraGravy()) textureModsList.add("Extra Gravy");
            if (patient.isMeatsOnly()) textureModsList.add("Meats Only");

            if (textureModsList.isEmpty()) {
                patient.setTextureModifications("Regular");
            } else {
                patient.setTextureModifications(String.join(", ", textureModsList));
            }

            // Save to database
            boolean success = patientDAO.updatePatient(patient);

            if (success) {
                Toast.makeText(this, "Patient updated successfully!", Toast.LENGTH_SHORT).show();
                loadPatients(); // Refresh the list
            } else {
                Toast.makeText(this, "Failed to update patient", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error updating patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error updating patient", e);
        }
    }

    private void showPatientDetailsDialog(Patient patient) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(patient.getFullName()).append("\n");
        details.append("Location: ").append(patient.getLocationInfo()).append("\n");
        details.append("Diet: ").append(patient.getDiet()).append("\n");
        details.append("Fluid Restriction: ").append(patient.getFluidRestriction()).append("\n");
        details.append("Texture Modifications: ").append(patient.getTextureModifications()).append("\n\n");
        details.append("Meal Status:\n");
        details.append("Breakfast: ").append(patient.isBreakfastComplete() ? "Complete" : patient.isBreakfastNPO() ? "NPO" : "Pending").append("\n");
        details.append("Lunch: ").append(patient.isLunchComplete() ? "Complete" : patient.isLunchNPO() ? "NPO" : "Pending").append("\n");
        details.append("Dinner: ").append(patient.isDinnerComplete() ? "Complete" : patient.isDinnerNPO() ? "NPO" : "Pending");

        new AlertDialog.Builder(this)
                .setTitle("Patient Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Patient patient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete " + patient.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient(patient))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePatient(Patient patient) {
        try {
            boolean success = patientDAO.deletePatient(patient.getPatientId());

            if (success) {
                Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                loadPatients(); // Refresh the list
            } else {
                Toast.makeText(this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error deleting patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error deleting patient", e);
        }
    }

    private void openNewPatient() {
        Intent intent = new Intent(this, NewPatientActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivityForResult(intent, REQUEST_NEW_PATIENT);
    }

    private void openPendingOrders() {
        Intent intent = new Intent(this, PendingOrdersActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openRetiredOrders() {
        Intent intent = new Intent(this, RetiredOrdersActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void openMealPlanning(Patient patient) {
        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("patient_id", (long) patient.getPatientId());
        intent.putExtra("diet", patient.getDiet());
        intent.putExtra("is_ada_diet", patient.isAdaDiet());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_NEW_PATIENT:
                case REQUEST_EDIT_PATIENT:
                    loadPatients(); // Refresh the patient list
                    break;
            }
        }
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
            case R.id.action_refresh:
                loadPatients();
                return true;
            case R.id.action_search:
                searchEditText.requestFocus();
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