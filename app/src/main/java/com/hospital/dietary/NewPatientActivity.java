package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;

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
    private EditText wingEdit;
    private EditText roomEdit;
    private Spinner dietSpinner;
    private CheckBox adaDietCheckBox;
    private Spinner fluidRestrictionSpinner;
    private Spinner textureSpinner;
    private CheckBox mechanicalChoppedCheckBox;
    private CheckBox mechanicalGroundCheckBox;
    private CheckBox biteSizeCheckBox;
    private CheckBox breadOkCheckBox;
    private Button savePatientButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "NewPatientActivity onCreate started");

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
        // Create a scrollable form layout
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        mainLayout.setBackgroundColor(0xFFf8f9fa);

        // Title
        TextView titleText = new TextView(this);
        titleText.setText("👤 Add New Patient");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFF2c3e50);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 30);
        mainLayout.addView(titleText);

        // Basic Information Section
        TextView basicInfoLabel = new TextView(this);
        basicInfoLabel.setText("Basic Information");
        basicInfoLabel.setTextSize(18);
        basicInfoLabel.setTextColor(0xFF2c3e50);
        basicInfoLabel.setTextStyle(android.graphics.Typeface.BOLD);
        basicInfoLabel.setPadding(0, 20, 0, 10);
        mainLayout.addView(basicInfoLabel);

        // First Name
        TextView firstNameLabel = new TextView(this);
        firstNameLabel.setText("First Name *");
        firstNameLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(firstNameLabel);

        firstNameEdit = new EditText(this);
        firstNameEdit.setHint("Enter patient's first name");
        firstNameEdit.setTextColor(0xFF2c3e50);
        firstNameEdit.setBackgroundColor(0xFFFFFFFF);
        firstNameEdit.setPadding(15, 15, 15, 15);
        mainLayout.addView(firstNameEdit);

        // Last Name
        TextView lastNameLabel = new TextView(this);
        lastNameLabel.setText("Last Name *");
        lastNameLabel.setTextColor(0xFF2c3e50);
        lastNameLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(lastNameLabel);

        lastNameEdit = new EditText(this);
        lastNameEdit.setHint("Enter patient's last name");
        lastNameEdit.setTextColor(0xFF2c3e50);
        lastNameEdit.setBackgroundColor(0xFFFFFFFF);
        lastNameEdit.setPadding(15, 15, 15, 15);
        mainLayout.addView(lastNameEdit);

        // Location Section
        TextView locationLabel = new TextView(this);
        locationLabel.setText("Location");
        locationLabel.setTextSize(18);
        locationLabel.setTextColor(0xFF2c3e50);
        locationLabel.setTextStyle(android.graphics.Typeface.BOLD);
        locationLabel.setPadding(0, 30, 0, 10);
        mainLayout.addView(locationLabel);

        // Wing
        TextView wingLabel = new TextView(this);
        wingLabel.setText("Wing *");
        wingLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(wingLabel);

        wingEdit = new EditText(this);
        wingEdit.setHint("Enter wing (e.g., A, B, C)");
        wingEdit.setTextColor(0xFF2c3e50);
        wingEdit.setBackgroundColor(0xFFFFFFFF);
        wingEdit.setPadding(15, 15, 15, 15);
        mainLayout.addView(wingEdit);

        // Room
        TextView roomLabel = new TextView(this);
        roomLabel.setText("Room Number *");
        roomLabel.setTextColor(0xFF2c3e50);
        roomLabel.setPadding(0, 15, 0, 0);
        mainLayout.addView(roomLabel);

        roomEdit = new EditText(this);
        roomEdit.setHint("Enter room number");
        roomEdit.setTextColor(0xFF2c3e50);
        roomEdit.setBackgroundColor(0xFFFFFFFF);
        roomEdit.setPadding(15, 15, 15, 15);
        roomEdit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        mainLayout.addView(roomEdit);

        // Dietary Requirements Section
        TextView dietaryLabel = new TextView(this);
        dietaryLabel.setText("Dietary Requirements");
        dietaryLabel.setTextSize(18);
        dietaryLabel.setTextColor(0xFF2c3e50);
        dietaryLabel.setTextStyle(android.graphics.Typeface.BOLD);
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

        // ADA Diet
        adaDietCheckBox = new CheckBox(this);
        adaDietCheckBox.setText("ADA Diet (Diabetic)");
        adaDietCheckBox.setTextColor(0xFF2c3e50);
        adaDietCheckBox.setPadding(0, 15, 0, 0);
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

        // Texture Modifications Section
        TextView textureLabel = new TextView(this);
        textureLabel.setText("Texture Modifications");
        textureLabel.setTextSize(18);
        textureLabel.setTextColor(0xFF2c3e50);
        textureLabel.setTextStyle(android.graphics.Typeface.BOLD);
        textureLabel.setPadding(0, 30, 0, 10);
        mainLayout.addView(textureLabel);

        // Texture Type
        TextView textureTypeLabel = new TextView(this);
        textureTypeLabel.setText("Texture Type");
        textureTypeLabel.setTextColor(0xFF2c3e50);
        mainLayout.addView(textureTypeLabel);

        textureSpinner = new Spinner(this);
        textureSpinner.setBackgroundColor(0xFFFFFFFF);
        textureSpinner.setPadding(15, 15, 15, 15);
        mainLayout.addView(textureSpinner);

        // Mechanical Modifications
        mechanicalChoppedCheckBox = new CheckBox(this);
        mechanicalChoppedCheckBox.setText("Mechanical Chopped");
        mechanicalChoppedCheckBox.setTextColor(0xFF2c3e50);
        mechanicalChoppedCheckBox.setPadding(0, 15, 0, 0);
        mainLayout.addView(mechanicalChoppedCheckBox);

        mechanicalGroundCheckBox = new CheckBox(this);
        mechanicalGroundCheckBox.setText("Mechanical Ground");
        mechanicalGroundCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(mechanicalGroundCheckBox);

        biteSizeCheckBox = new CheckBox(this);
        biteSizeCheckBox.setText("Bite Size");
        biteSizeCheckBox.setTextColor(0xFF2c3e50);
        mainLayout.addView(biteSizeCheckBox);

        breadOkCheckBox = new CheckBox(this);
        breadOkCheckBox.setText("Bread OK");
        breadOkCheckBox.setTextColor(0xFF2c3e50);
        breadOkCheckBox.setChecked(true); // Default to checked
        mainLayout.addView(breadOkCheckBox);

        // Buttons Section
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 40, 0, 20);

        savePatientButton = new Button(this);
        savePatientButton.setText("Save Patient");
        savePatientButton.setTextColor(0xFFFFFFFF);
        savePatientButton.setBackgroundColor(0xFF27ae60);
        savePatientButton.setPadding(20, 15, 20, 15);

        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        saveParams.setMargins(0, 0, 10, 0);
        savePatientButton.setLayoutParams(saveParams);
        buttonLayout.addView(savePatientButton);

        cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setTextColor(0xFFFFFFFF);
        cancelButton.setBackgroundColor(0xFF95a5a6);
        cancelButton.setPadding(20, 15, 20, 15);

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        cancelParams.setMargins(10, 0, 0, 0);
        cancelButton.setLayoutParams(cancelParams);
        buttonLayout.addView(cancelButton);

        mainLayout.addView(buttonLayout);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void setupSpinners() {
        // Diet Types
        String[] dietTypes = {
                "Regular", "Cardiac", "Diabetic", "Renal", "Low Sodium",
                "Soft", "Clear Liquid", "Full Liquid", "NPO"
        };
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);

        // Fluid Restrictions
        String[] fluidRestrictions = {
                "No Restriction", "1000ml", "1500ml", "2000ml", "As Ordered"
        };
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);

        // Texture Types
        String[] textureTypes = {
                "Regular", "Soft", "Minced", "Pureed", "Liquid"
        };
        ArrayAdapter<String> textureAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, textureTypes);
        textureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        textureSpinner.setAdapter(textureAdapter);
    }

    private void setupListeners() {
        savePatientButton.setOnClickListener(v -> savePatient());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void savePatient() {
        // Validate required fields
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String wing = wingEdit.getText().toString().trim();
        String room = roomEdit.getText().toString().trim();

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

        if (wing.isEmpty()) {
            Toast.makeText(this, "Wing is required", Toast.LENGTH_SHORT).show();
            wingEdit.requestFocus();
            return;
        }

        if (room.isEmpty()) {
            Toast.makeText(this, "Room number is required", Toast.LENGTH_SHORT).show();
            roomEdit.requestFocus();
            return;
        }

        // Create new patient object
        Patient patient = new Patient();
        patient.setPatientFirstName(firstName);
        patient.setPatientLastName(lastName);
        patient.setWing(wing);
        patient.setRoomNumber(room);
        patient.setDietType(dietSpinner.getSelectedItem().toString());
        patient.setAdaDiet(adaDietCheckBox.isChecked());
        patient.setFluidRestriction(fluidRestrictionSpinner.getSelectedItem().toString());
        patient.setTextureModifications(textureSpinner.getSelectedItem().toString());
        patient.setMechanicalChopped(mechanicalChoppedCheckBox.isChecked());
        patient.setMechanicalGround(mechanicalGroundCheckBox.isChecked());
        patient.setBiteSize(biteSizeCheckBox.isChecked());
        patient.setBreadOK(breadOkCheckBox.isChecked());

        // Save patient to database
        try {
            long result = patientDAO.addPatient(patient);
            if (result > 0) {
                Toast.makeText(this, "Patient added successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Patient added with ID: " + result);

                // Return to previous activity
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error adding patient", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to add patient");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Exception adding patient", e);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}