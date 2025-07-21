package com.hospital.dietary;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.dao.DefaultMenuDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class NewPatientActivity extends AppCompatActivity {

    private static final String TAG = "NewPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private DefaultMenuDAO defaultMenuDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private EditText patientFirstNameEditText;
    private EditText patientLastNameEditText;
    private Spinner wingSpinner;
    private Spinner roomNumberSpinner;
    private Spinner dietSpinner;
    private CheckBox adaToggleCheckBox;
    private LinearLayout adaToggleContainer;
    private Spinner fluidRestrictionSpinner;

    // Texture modification checkboxes
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOKCheckBox;
    private CheckBox nectarThickCheckBox;
    private CheckBox puddingThickCheckBox;
    private CheckBox honeyThickCheckBox;
    private CheckBox extraGravyCheckBox;
    private CheckBox meatsOnlyCheckBox;
    private LinearLayout meatsOnlyContainer;

    private Button savePatientButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_patient);

        Log.d(TAG, "NewPatientActivity onCreate started");

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        defaultMenuDAO = new DefaultMenuDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupSpinners();
        setupListeners();
        updateSaveButtonState(); // Initial state

        Log.d(TAG, "NewPatientActivity onCreate completed");
    }

    private void initializeUI() {
        // Basic patient information
        patientFirstNameEditText = findViewById(R.id.patientFirstNameEditText);
        patientLastNameEditText = findViewById(R.id.patientLastNameEditText);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomNumberSpinner = findViewById(R.id.roomNumberSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        adaToggleCheckBox = findViewById(R.id.adaToggleCheckBox);
        adaToggleContainer = findViewById(R.id.adaToggleContainer);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);

        // Texture modification checkboxes
        mechanicalChoppedCheckBox = findViewById(R.id.mechanicalChoppedCheckBox);
        mechanicalGroundCheckBox = findViewById(R.id.mechanicalGroundCheckBox);
        biteSizeCheckBox = findViewById(R.id.biteSizeCheckBox);
        breadOKCheckBox = findViewById(R.id.breadOKCheckBox);
        nectarThickCheckBox = findViewById(R.id.nectarThickCheckBox);
        puddingThickCheckBox = findViewById(R.id.puddingThickCheckBox);
        honeyThickCheckBox = findViewById(R.id.honeyThickCheckBox);
        extraGravyCheckBox = findViewById(R.id.extraGravyCheckBox);
        meatsOnlyCheckBox = findViewById(R.id.meatsOnlyCheckBox);
        meatsOnlyContainer = findViewById(R.id.meatsOnlyContainer);

        savePatientButton = findViewById(R.id.savePatientButton);
    }

    private void setupSpinners() {
        // Wing Spinner
        String[] wings = {"Select Wing", "1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Room Number Spinner (dynamic based on wing) - Initialize with empty list
        updateRoomNumbers();

        // Diet Spinner
        String[] diets = {"Select Diet", "Regular", "ADA", "Cardiac", "Renal", "Clear Liquid", "Full Liquid", "Puree", "Mechanical Chopped", "Mechanical Ground"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid Restriction Spinner
        String[] fluidRestrictions = {
                "No Fluid Restriction",
                "1000ml (34oz): 120ml, 120ml, 160ml",
                "1200ml (41oz): 250ml, 170ml, 180ml",
                "1500ml (51oz): 350ml, 170ml, 180ml",
                "1800ml (61oz): 360ml, 240ml, 240ml",
                "2000ml (68oz): 320ml, 240ml, 240ml",
                "2500ml (85oz): 400ml, 400ml, 400ml"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void updateRoomNumbers() {
        String selectedWing = wingSpinner.getSelectedItem().toString();
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
        roomNumberSpinner.setAdapter(roomAdapter);
    }

    private void setupListeners() {
        // Text change listeners for validation
        patientFirstNameEditText.addTextChangedListener(validationWatcher);
        patientLastNameEditText.addTextChangedListener(validationWatcher);

        // Spinner change listeners
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomNumbers(); // Update room numbers when wing changes
                updateSaveButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        roomNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSaveButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = parent.getItemAtPosition(position).toString();
                updateDietSpecificOptions(selectedDiet);
                updateSaveButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Save patient button
        savePatientButton.setOnClickListener(v -> savePatient());
    }

    private void updateDietSpecificOptions(String selectedDiet) {
        // Show/hide ADA toggle for specific diets
        boolean showAdaToggle = "Regular".equals(selectedDiet) || "Cardiac".equals(selectedDiet);
        adaToggleContainer.setVisibility(showAdaToggle ? View.VISIBLE : View.GONE);
        if (!showAdaToggle) {
            adaToggleCheckBox.setChecked(false);
        }

        // Show/hide meats only option for specific diets
        boolean showMeatsOnly = "Mechanical Chopped".equals(selectedDiet) || "Mechanical Ground".equals(selectedDiet);
        meatsOnlyContainer.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);
        if (!showMeatsOnly) {
            meatsOnlyCheckBox.setChecked(false);
        }
    }

    // FIXED: Enhanced save patient method with proper meal completion states
    private void savePatient() {
        Log.d(TAG, "savePatient() method called");

        if (!savePatientButton.isEnabled()) {
            Log.w(TAG, "Save button is disabled, cannot save");
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Show saving indicator
            savePatientButton.setEnabled(false);
            savePatientButton.setText("Saving...");

            String firstName = patientFirstNameEditText.getText().toString().trim();
            String lastName = patientLastNameEditText.getText().toString().trim();

            // Validate inputs
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showError("First name and last name are required");
                resetSaveButton();
                return;
            }

            if (wingSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a wing");
                resetSaveButton();
                return;
            }

            if (roomNumberSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a room number");
                resetSaveButton();
                return;
            }

            if (dietSpinner.getSelectedItemPosition() == 0) {
                showError("Please select a diet type");
                resetSaveButton();
                return;
            }

            String wing = wingSpinner.getSelectedItem().toString();
            String roomNumber = roomNumberSpinner.getSelectedItem().toString();
            String diet = dietSpinner.getSelectedItem().toString();
            String fluidRestriction = fluidRestrictionSpinner.getSelectedItem().toString();

            // Create Patient object
            Patient newPatient = new Patient();
            newPatient.setPatientFirstName(firstName);
            newPatient.setPatientLastName(lastName);
            newPatient.setWing(wing);
            newPatient.setRoomNumber(roomNumber);
            newPatient.setDiet(diet);
            newPatient.setAdaDiet(adaToggleCheckBox.isChecked());
            newPatient.setFluidRestriction(fluidRestriction.equals("No Fluid Restriction") ? "" : fluidRestriction);

            // Set texture modifications
            newPatient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
            newPatient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
            newPatient.setBiteSize(biteSizeCheckBox.isChecked());
            newPatient.setBreadOK(breadOKCheckBox.isChecked());
            newPatient.setNectarThick(nectarThickCheckBox.isChecked());
            newPatient.setPuddingThick(puddingThickCheckBox.isChecked());
            newPatient.setHoneyThick(honeyThickCheckBox.isChecked());
            newPatient.setExtraGravy(extraGravyCheckBox.isChecked());
            newPatient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

            // FIXED: Ensure meals are NOT completed when creating new patient
            newPatient.setBreakfastComplete(false);
            newPatient.setLunchComplete(false);
            newPatient.setDinnerComplete(false);

            // Set NPO states to false
            newPatient.setBreakfastNPO(false);
            newPatient.setLunchNPO(false);
            newPatient.setDinnerNPO(false);

            // Set created date
            newPatient.setCreatedDate(new Date());

            // Apply default menu items based on diet type and current day
            applyDefaultMenuItems(newPatient);

            // Save patient to database
            long patientId = patientDAO.addPatient(newPatient);

            if (patientId > 0) {
                Log.d(TAG, "Patient saved successfully with ID: " + patientId);

                String dietName = diet + (adaToggleCheckBox.isChecked() ? " + ADA" : "");
                showSuccess("Patient " + firstName + " " + lastName + " added successfully!\n\n" +
                        "✅ " + dietName + " diet has been configured with default menu items.\n" +
                        "Patient will appear in Pending Orders until meals are marked complete.");

                clearForm();
            } else {
                Log.e(TAG, "Failed to save patient - patientDAO.addPatient returned: " + patientId);
                showError("Failed to add patient. Please try again.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving patient", e);
            showError("Error saving patient: " + e.getMessage());
        } finally {
            resetSaveButton();
        }
    }

    private void applyDefaultMenuItems(Patient patient) {
        try {
            // Get current day of week
            Calendar calendar = Calendar.getInstance();
            String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            String currentDay = daysOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];

            String dietType = patient.getDiet();

            // Get default menu items for breakfast (same for all days)
            List<DefaultMenuItem> breakfastItems = defaultMenuDAO.getDefaultMenuItems(dietType, "Breakfast", "All Days");
            if (!breakfastItems.isEmpty()) {
                StringBuilder breakfastString = new StringBuilder();
                for (DefaultMenuItem item : breakfastItems) {
                    if (breakfastString.length() > 0) breakfastString.append("\n");
                    breakfastString.append(item.getItemName());
                }
                patient.setBreakfastItems(breakfastString.toString());
            }

            // Get default menu items for lunch (specific to day)
            List<DefaultMenuItem> lunchItems = defaultMenuDAO.getDefaultMenuItems(dietType, "Lunch", currentDay);
            if (!lunchItems.isEmpty()) {
                StringBuilder lunchString = new StringBuilder();
                for (DefaultMenuItem item : lunchItems) {
                    if (lunchString.length() > 0) lunchString.append("\n");
                    lunchString.append(item.getItemName());
                }
                patient.setLunchItems(lunchString.toString());
            }

            // Get default menu items for dinner (specific to day)
            List<DefaultMenuItem> dinnerItems = defaultMenuDAO.getDefaultMenuItems(dietType, "Dinner", currentDay);
            if (!dinnerItems.isEmpty()) {
                StringBuilder dinnerString = new StringBuilder();
                for (DefaultMenuItem item : dinnerItems) {
                    if (dinnerString.length() > 0) dinnerString.append("\n");
                    dinnerString.append(item.getItemName());
                }
                patient.setDinnerItems(dinnerString.toString());
            }

            Log.d(TAG, "Applied default menu items for " + dietType + " diet on " + currentDay);

        } catch (Exception e) {
            Log.e(TAG, "Error applying default menu items", e);
            // Continue without default items if there's an error
        }
    }

    private void resetSaveButton() {
        savePatientButton.setText("💾 Save Patient");
        updateSaveButtonState(); // This will re-enable if form is valid
    }

    private void clearForm() {
        patientFirstNameEditText.setText("");
        patientLastNameEditText.setText("");
        wingSpinner.setSelection(0);
        roomNumberSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        adaToggleCheckBox.setChecked(false);
        adaToggleContainer.setVisibility(View.GONE);
        fluidRestrictionSpinner.setSelection(0);

        // Clear all texture modification checkboxes
        mechanicalChoppedCheckBox.setChecked(false);
        mechanicalGroundCheckBox.setChecked(false);
        biteSizeCheckBox.setChecked(false);
        breadOKCheckBox.setChecked(false);
        nectarThickCheckBox.setChecked(false);
        puddingThickCheckBox.setChecked(false);
        honeyThickCheckBox.setChecked(false);
        extraGravyCheckBox.setChecked(false);
        meatsOnlyCheckBox.setChecked(false);
        meatsOnlyContainer.setVisibility(View.GONE);

        // Focus on first name field
        patientFirstNameEditText.requestFocus();
    }

    private TextWatcher validationWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateSaveButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    // FIXED: Enhanced button state management
    private void updateSaveButtonState() {
        boolean isValid = !patientFirstNameEditText.getText().toString().trim().isEmpty() &&
                !patientLastNameEditText.getText().toString().trim().isEmpty() &&
                wingSpinner.getSelectedItemPosition() > 0 &&
                roomNumberSpinner.getSelectedItemPosition() > 0 &&
                dietSpinner.getSelectedItemPosition() > 0;

        savePatientButton.setEnabled(isValid);
        savePatientButton.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void showError(String message) {
        Toast.makeText(this, "❌ " + message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
                // Go to main menu
                finish();
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