package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class NewPatientActivity extends AppCompatActivity {

    private static final String TAG = "NewPatientActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private EditText firstNameEdit;
    private EditText lastNameEdit;
    private Spinner wingSpinner;
    private Spinner roomSpinner;
    private Spinner dietSpinner;
    private CheckBox adaDietCheckBox;
    private Spinner fluidRestrictionSpinner;

    // Texture Modifications
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOkCheckBox;
    private CheckBox extraGravyCheckBox;
    private CheckBox meatsOnlyCheckBox;

    // Thicken Liquids
    private CheckBox nectarThickCheckBox;
    private CheckBox honeyThickCheckBox;
    private CheckBox puddingThickCheckBox;

    private Button savePatientButton;
    private Button cancelButton;

    // Fluid restriction info label
    private TextView fluidInfoLabel;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap;
    private String[] wings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "NewPatientActivity onCreate started");

        // Initialize room mapping
        initializeRoomMapping();

        // Create the patient form layout
        createPatientForm();

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Patient");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupSpinners();
        setupListeners();

        Log.d(TAG, "NewPatientActivity onCreate completed successfully");
    }

    private void createPatientForm() {
        // Create a scrollable layout
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFFF5F5F5);
        scrollView.setFillViewport(true);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);

        // Title
        TextView titleText = new TextView(this);
        titleText.setText("New Patient Registration");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFF2c3e50);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setPadding(0, 0, 0, 20);
        mainLayout.addView(titleText);

        // Patient Information Section
        TextView patientInfoLabel = new TextView(this);
        patientInfoLabel.setText("Patient Information");
        patientInfoLabel.setTextSize(18);
        patientInfoLabel.setTextColor(0xFF2c3e50);
        patientInfoLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        patientInfoLabel.setPadding(0, 10, 0, 10);
        mainLayout.addView(patientInfoLabel);

        // First Name
        TextView firstNameLabel = new TextView(this);
        firstNameLabel.setText("First Name *");
        firstNameLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(firstNameLabel);

        firstNameEdit = new EditText(this);
        firstNameEdit.setBackgroundColor(0xFFFFFFFF);
        firstNameEdit.setPadding(15, 15, 15, 15);
        firstNameEdit.setHint("Enter first name");
        mainLayout.addView(firstNameEdit);

        // Last Name
        TextView lastNameLabel = new TextView(this);
        lastNameLabel.setText("Last Name *");
        lastNameLabel.setTextColor(0xFF2c3e50);
        lastNameLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(lastNameLabel);

        lastNameEdit = new EditText(this);
        lastNameEdit.setBackgroundColor(0xFFFFFFFF);
        lastNameEdit.setPadding(15, 15, 15, 15);
        lastNameEdit.setHint("Enter last name");
        mainLayout.addView(lastNameEdit);

        // Wing Dropdown
        TextView wingLabel = new TextView(this);
        wingLabel.setText("Wing *");
        wingLabel.setTextColor(0xFF2c3e50);
        wingLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(wingLabel);

        wingSpinner = new Spinner(this);
        wingSpinner.setBackgroundColor(0xFFFFFFFF);
        wingSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(wingSpinner);

        // Room Dropdown (depends on wing selection)
        TextView roomLabel = new TextView(this);
        roomLabel.setText("Room Number *");
        roomLabel.setTextColor(0xFF2c3e50);
        roomLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(roomLabel);

        roomSpinner = new Spinner(this);
        roomSpinner.setBackgroundColor(0xFFFFFFFF);
        roomSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(roomSpinner);

        // Dietary Requirements Section
        TextView dietaryLabel = new TextView(this);
        dietaryLabel.setText("Dietary Requirements");
        dietaryLabel.setTextSize(18);
        dietaryLabel.setTextColor(0xFF2c3e50);
        dietaryLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        dietaryLabel.setPadding(0, 30, 0, 10);
        mainLayout.addView(dietaryLabel);

        // Diet Type
        TextView dietLabel = new TextView(this);
        dietLabel.setText("Diet Type *");
        dietLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(dietLabel);

        dietSpinner = new Spinner(this);
        dietSpinner.setBackgroundColor(0xFFFFFFFF);
        dietSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(dietSpinner);

        // ADA Diet (only shows for specific diet types)
        adaDietCheckBox = new CheckBox(this);
        adaDietCheckBox.setText("ADA Diet (Diabetic)");
        adaDietCheckBox.setTextColor(0xFF2c3e50);
        adaDietCheckBox.setPadding(0, 15, 0, 0);
        adaDietCheckBox.setVisibility(View.GONE); // Hidden by default
        mainLayout.addView(adaDietCheckBox);

        // Fluid Restriction
        TextView fluidLabel = new TextView(this);
        fluidLabel.setText("Fluid Restriction");
        fluidLabel.setTextColor(0xFF2c3e50);
        fluidLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(fluidLabel);

        fluidRestrictionSpinner = new Spinner(this);
        fluidRestrictionSpinner.setBackgroundColor(0xFFFFFFFF);
        fluidRestrictionSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(fluidRestrictionSpinner);

        // Fluid restriction info label
        fluidInfoLabel = new TextView(this);
        fluidInfoLabel.setTextColor(0xFF27ae60);
        fluidInfoLabel.setTextSize(12);
        fluidInfoLabel.setPadding(15, 5, 15, 0);
        fluidInfoLabel.setVisibility(View.GONE);
        mainLayout.addView(fluidInfoLabel);

        // Texture Modifications and Thicken Liquids in horizontal layout
        LinearLayout modificationLayout = new LinearLayout(this);
        modificationLayout.setOrientation(LinearLayout.HORIZONTAL);
        modificationLayout.setPadding(0, 20, 0, 0);

        // Texture Modifications Column
        LinearLayout textureColumn = new LinearLayout(this);
        textureColumn.setOrientation(LinearLayout.VERTICAL);
        textureColumn.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView textureLabel = new TextView(this);
        textureLabel.setText("Texture Modifications");
        textureLabel.setTextSize(16);
        textureLabel.setTextColor(0xFF2c3e50);
        textureLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        textureColumn.addView(textureLabel);

        mechanicalGroundCheckBox = new CheckBox(this);
        mechanicalGroundCheckBox.setText("Mechanical Ground");
        mechanicalGroundCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(mechanicalGroundCheckBox);

        mechanicalChoppedCheckBox = new CheckBox(this);
        mechanicalChoppedCheckBox.setText("Mechanical Chopped");
        mechanicalChoppedCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(mechanicalChoppedCheckBox);

        biteSizeCheckBox = new CheckBox(this);
        biteSizeCheckBox.setText("Bite Size");
        biteSizeCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(biteSizeCheckBox);

        breadOkCheckBox = new CheckBox(this);
        breadOkCheckBox.setText("Bread OK");
        breadOkCheckBox.setTextColor(0xFF2c3e50);
        breadOkCheckBox.setChecked(false); // Set unchecked by default
        textureColumn.addView(breadOkCheckBox);

        extraGravyCheckBox = new CheckBox(this);
        extraGravyCheckBox.setText("Extra Gravy");
        extraGravyCheckBox.setTextColor(0xFF2c3e50);
        textureColumn.addView(extraGravyCheckBox);

        meatsOnlyCheckBox = new CheckBox(this);
        meatsOnlyCheckBox.setText("Meats Only");
        meatsOnlyCheckBox.setTextColor(0xFF2c3e50);
        meatsOnlyCheckBox.setVisibility(View.GONE); // Hidden by default
        textureColumn.addView(meatsOnlyCheckBox);

        modificationLayout.addView(textureColumn);

        // Thicken Liquids Column
        LinearLayout thickenColumn = new LinearLayout(this);
        thickenColumn.setOrientation(LinearLayout.VERTICAL);
        thickenColumn.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView thickenLabel = new TextView(this);
        thickenLabel.setText("Thicken Liquids");
        thickenLabel.setTextSize(16);
        thickenLabel.setTextColor(0xFF2c3e50);
        thickenLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        thickenColumn.addView(thickenLabel);

        nectarThickCheckBox = new CheckBox(this);
        nectarThickCheckBox.setText("Nectar Thick");
        nectarThickCheckBox.setTextColor(0xFF2c3e50);
        thickenColumn.addView(nectarThickCheckBox);

        honeyThickCheckBox = new CheckBox(this);
        honeyThickCheckBox.setText("Honey Thick");
        honeyThickCheckBox.setTextColor(0xFF2c3e50);
        thickenColumn.addView(honeyThickCheckBox);

        puddingThickCheckBox = new CheckBox(this);
        puddingThickCheckBox.setText("Pudding Thick");
        puddingThickCheckBox.setTextColor(0xFF2c3e50);
        thickenColumn.addView(puddingThickCheckBox);

        modificationLayout.addView(thickenColumn);

        mainLayout.addView(modificationLayout);

        // Buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 30, 0, 20);

        savePatientButton = new Button(this);
        savePatientButton.setText("Save Patient");
        savePatientButton.setBackgroundColor(0xFF3498db);
        savePatientButton.setTextColor(0xFFFFFFFF);
        savePatientButton.setPadding(30, 15, 30, 15);

        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        saveParams.setMargins(0, 0, 10, 0);
        savePatientButton.setLayoutParams(saveParams);
        buttonLayout.addView(savePatientButton);

        cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setBackgroundColor(0xFF95a5a6);
        cancelButton.setTextColor(0xFFFFFFFF);
        cancelButton.setPadding(30, 15, 30, 15);

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        cancelParams.setMargins(10, 0, 0, 0);
        cancelButton.setLayoutParams(cancelParams);
        buttonLayout.addView(cancelButton);

        mainLayout.addView(buttonLayout);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void initializeRoomMapping() {
        // Use LinkedHashMap to maintain insertion order
        wingRoomMap = new LinkedHashMap<>();

        // 1 South - Rooms 106 through 122
        String[] south1Rooms = new String[17];
        for (int i = 0; i < 17; i++) {
            south1Rooms[i] = String.valueOf(106 + i);
        }
        wingRoomMap.put("1 South", south1Rooms);

        // 2 North - Rooms 250 through 264
        String[] north2Rooms = new String[15];
        for (int i = 0; i < 15; i++) {
            north2Rooms[i] = String.valueOf(250 + i);
        }
        wingRoomMap.put("2 North", north2Rooms);

        // Labor and Delivery - Rooms LDR1 through LDR6
        String[] ldRooms = new String[6];
        for (int i = 0; i < 6; i++) {
            ldRooms[i] = "LDR" + (i + 1);
        }
        wingRoomMap.put("Labor and Delivery", ldRooms);

        // 2 West - Rooms 225 through 248
        String[] west2Rooms = new String[24];
        for (int i = 0; i < 24; i++) {
            west2Rooms[i] = String.valueOf(225 + i);
        }
        wingRoomMap.put("2 West", west2Rooms);

        // 3 North - Rooms 349 through 371
        String[] north3Rooms = new String[23];
        for (int i = 0; i < 23; i++) {
            north3Rooms[i] = String.valueOf(349 + i);
        }
        wingRoomMap.put("3 North", north3Rooms);

        // ICU - ICU1 through ICU13
        String[] icuRooms = new String[13];
        for (int i = 0; i < 13; i++) {
            icuRooms[i] = "ICU" + (i + 1);
        }
        wingRoomMap.put("ICU", icuRooms);

        // Create wings array in the correct order
        wings = new String[]{"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};
    }

    private void setupSpinners() {
        // Wing dropdown using the initialized wings
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Initially populate rooms for first wing
        updateRoomSpinner(wings[0]);

        // Diet Types
        String[] dietTypes = {
                "Regular", "Cardiac", "ADA", "Renal", "Puree",
                "Full Liquid", "Clear Liquid"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid Restrictions with meal-specific limits
        String[] fluidRestrictions = {
                "No Restriction",
                "1000ml (34oz) - B:120ml, L:120ml, D:160ml",
                "1200ml (41oz) - B:250ml, L:170ml, D:180ml",
                "1500ml (51oz) - B:350ml, L:170ml, D:180ml",
                "1800ml (61oz) - B:360ml, L:240ml, D:240ml",
                "2000ml (68oz) - B:320ml, L:240ml, D:240ml",
                "2500ml (85oz) - B:400ml, L:400ml, D:400ml"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }

    private void updateRoomSpinner(String selectedWing) {
        String[] rooms = wingRoomMap.get(selectedWing);
        if (rooms != null) {
            ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, rooms);
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomSpinner.setAdapter(roomAdapter);
        }
    }

    private void setupListeners() {
        // Wing selection listener - update rooms when wing changes
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                updateRoomSpinner(selectedWing);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Diet selection listener - show/hide ADA checkbox for specific diets
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getItemAtPosition(position);
                // Hide ADA checkbox since ADA is now its own diet type
                adaDietCheckBox.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Texture modification listeners - show/hide "Meats Only" option
        CompoundButton.OnCheckedChangeListener textureListener = (buttonView, isChecked) -> {
            boolean showMeatsOnly = mechanicalGroundCheckBox.isChecked() ||
                    mechanicalChoppedCheckBox.isChecked() ||
                    biteSizeCheckBox.isChecked();
            meatsOnlyCheckBox.setVisibility(showMeatsOnly ? View.VISIBLE : View.GONE);
            if (!showMeatsOnly) {
                meatsOnlyCheckBox.setChecked(false);
            }
        };

        mechanicalGroundCheckBox.setOnCheckedChangeListener(textureListener);
        mechanicalChoppedCheckBox.setOnCheckedChangeListener(textureListener);
        biteSizeCheckBox.setOnCheckedChangeListener(textureListener);

        // Fluid restriction selection listener
        fluidRestrictionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRestriction = (String) parent.getItemAtPosition(position);
                if (selectedRestriction != null && !selectedRestriction.equals("No Restriction")) {
                    // Extract and display meal limits
                    if (selectedRestriction.contains("B:")) {
                        String info = "Meal fluid limits will be tracked:\n";
                        String[] parts = selectedRestriction.split(" - ");
                        if (parts.length > 1) {
                            info += parts[1].replace("B:", "Breakfast: ")
                                    .replace("L:", "Lunch: ")
                                    .replace("D:", "Dinner: ");
                        }
                        fluidInfoLabel.setText(info);
                        fluidInfoLabel.setVisibility(View.VISIBLE);
                    }
                } else {
                    fluidInfoLabel.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        savePatientButton.setOnClickListener(v -> savePatient());
        cancelButton.setOnClickListener(v -> finish());
    }

    /**
     * Parse fluid restriction to extract meal limits
     * Format: "1000ml (34oz) - B:120ml, L:120ml, D:160ml"
     * Returns: [total, breakfast, lunch, dinner] in ml
     */
    private int[] parseFluidRestriction(String fluidRestriction) {
        int[] limits = new int[]{0, 0, 0, 0}; // total, breakfast, lunch, dinner

        if (fluidRestriction == null || fluidRestriction.equals("No Restriction")) {
            return limits;
        }

        try {
            // Extract total from beginning (e.g., "1000ml")
            String totalStr = fluidRestriction.split("ml")[0];
            limits[0] = Integer.parseInt(totalStr);

            // Extract meal limits
            if (fluidRestriction.contains("B:")) {
                String bStr = fluidRestriction.split("B:")[1].split("ml")[0];
                limits[1] = Integer.parseInt(bStr);
            }
            if (fluidRestriction.contains("L:")) {
                String lStr = fluidRestriction.split("L:")[1].split("ml")[0];
                limits[2] = Integer.parseInt(lStr);
            }
            if (fluidRestriction.contains("D:")) {
                String dStr = fluidRestriction.split("D:")[1].split("ml")[0];
                limits[3] = Integer.parseInt(dStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing fluid restriction: " + fluidRestriction, e);
        }

        return limits;
    }

    private void savePatient() {
        // Validate required fields
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String room = (String) roomSpinner.getSelectedItem();

        if (firstName.isEmpty()) {
            Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show();
            firstNameEdit.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            Toast.makeText(this, "Last name is required", Toast.LENGTH_SHORT).show();
            lastNameEdit.requestFocus();
            return;
        }

        if (wing == null) {
            Toast.makeText(this, "Wing is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (room == null) {
            Toast.makeText(this, "Room number is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build texture modifications string
        StringBuilder textureModifications = new StringBuilder();
        if (mechanicalGroundCheckBox.isChecked()) textureModifications.append("Mechanical Ground, ");
        if (mechanicalChoppedCheckBox.isChecked()) textureModifications.append("Mechanical Chopped, ");
        if (biteSizeCheckBox.isChecked()) textureModifications.append("Bite Size, ");
        if (breadOkCheckBox.isChecked()) textureModifications.append("Bread OK, ");
        if (extraGravyCheckBox.isChecked()) textureModifications.append("Extra Gravy, ");
        if (meatsOnlyCheckBox.isChecked()) textureModifications.append("Meats Only, ");

        String textureModsString = textureModifications.length() > 0 ?
                textureModifications.substring(0, textureModifications.length() - 2) : "";

        // Create new patient
        Patient patient = new Patient();
        patient.setPatientFirstName(firstName);
        patient.setPatientLastName(lastName);
        patient.setWing(wing);
        patient.setRoomNumber(room);

        // Set diet type
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        patient.setDietType(selectedDiet);
        patient.setDiet(selectedDiet);

        // Set ADA flag if ADA diet is selected
        patient.setAdaDiet("ADA".equals(selectedDiet));

        // Set fluid restriction
        String selectedFluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        patient.setFluidRestriction(selectedFluidRestriction);

        // Parse and store meal-specific fluid limits in the drinks fields as metadata
        int[] fluidLimits = parseFluidRestriction(selectedFluidRestriction);
        if (fluidLimits[0] > 0) {
            // Store limits as metadata in the drinks fields (to be parsed by ordering system)
            patient.setBreakfastDrinks("FL:" + fluidLimits[1]);
            patient.setLunchDrinks("FL:" + fluidLimits[2]);
            patient.setDinnerDrinks("FL:" + fluidLimits[3]);
        }

        patient.setTextureModifications(textureModsString);

        // Set texture modification flags
        patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        patient.setBiteSize(biteSizeCheckBox.isChecked());
        patient.setBreadOK(breadOkCheckBox.isChecked());
        patient.setExtraGravy(extraGravyCheckBox.isChecked());
        patient.setMeatsOnly(meatsOnlyCheckBox.isChecked());

        // Set thicken liquids flags
        patient.setNectarThick(nectarThickCheckBox.isChecked());
        patient.setHoneyThick(honeyThickCheckBox.isChecked());
        patient.setPuddingThick(puddingThickCheckBox.isChecked());

        // Set created date - use Date object, not long
        patient.setCreatedDate(new Date());

        // Save to database
        long result = patientDAO.insertPatient(patient);

        if (result > 0) {
            Toast.makeText(this, "Patient added successfully", Toast.LENGTH_SHORT).show();

            // Return to patient info activity
            Intent intent = new Intent(this, PatientInfoActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error adding patient. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}