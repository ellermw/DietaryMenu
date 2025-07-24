package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PatientDetailActivity extends AppCompatActivity {

    private static final String TAG = "PatientDetailActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    private int patientId;
    private Patient currentPatient;

    // UI Components - Patient Information
    private TextView patientNameText;
    private TextView locationText;
    private TextView dietText;
    private TextView fluidRestrictionText;
    private TextView textureModificationsText;
    private TextView createdDateText;

    // UI Components - Meal Status
    private LinearLayout mealStatusSection;
    private CheckBox breakfastCompleteCheckBox;
    private CheckBox lunchCompleteCheckBox;
    private CheckBox dinnerCompleteCheckBox;
    private TextView orderDateText;

    // UI Components - Meal Items
    private LinearLayout mealItemsSection;
    private TextView breakfastItemsText;
    private TextView lunchItemsText;
    private TextView dinnerItemsText;
    private TextView breakfastDrinksText;
    private TextView lunchDrinksText;
    private TextView dinnerDrinksText;

    // UI Components - Action Buttons
    private Button editPatientButton;
    private Button editMealPlanButton;
    private Button deletePatientButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        // Get intent data
        patientId = getIntent().getIntExtra("patient_id", -1);
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        if (patientId == -1) {
            Toast.makeText(this, "Error: Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Patient Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupListeners();
        loadPatientData();
    }

    private void initializeUI() {
        // Patient Information
        patientNameText = findViewById(R.id.patientNameText);
        locationText = findViewById(R.id.locationText);
        dietText = findViewById(R.id.dietText);
        fluidRestrictionText = findViewById(R.id.fluidRestrictionText);
        textureModificationsText = findViewById(R.id.textureModificationsText);
        createdDateText = findViewById(R.id.createdDateText);

        // Meal Status Section
        mealStatusSection = findViewById(R.id.mealStatusSection);
        breakfastCompleteCheckBox = findViewById(R.id.breakfastCompleteCheckBox);
        lunchCompleteCheckBox = findViewById(R.id.lunchCompleteCheckBox);
        dinnerCompleteCheckBox = findViewById(R.id.dinnerCompleteCheckBox);
        orderDateText = findViewById(R.id.orderDateText);

        // Meal Items Section
        mealItemsSection = findViewById(R.id.mealItemsSection);
        breakfastItemsText = findViewById(R.id.breakfastItemsText);
        lunchItemsText = findViewById(R.id.lunchItemsText);
        dinnerItemsText = findViewById(R.id.dinnerItemsText);
        breakfastDrinksText = findViewById(R.id.breakfastDrinksText);
        lunchDrinksText = findViewById(R.id.lunchDrinksText);
        dinnerDrinksText = findViewById(R.id.dinnerDrinksText);

        // Action Buttons
        editPatientButton = findViewById(R.id.editPatientButton);
        editMealPlanButton = findViewById(R.id.editMealPlanButton);
        deletePatientButton = findViewById(R.id.deletePatientButton);
    }

    private void loadPatientData() {
        try {
            currentPatient = patientDAO.getPatientById(patientId);

            if (currentPatient == null) {
                Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Display patient information
            patientNameText.setText(currentPatient.getFullName());
            locationText.setText(currentPatient.getWing() + " - Room " + currentPatient.getRoomNumber());

            // Display diet with ADA indicator
            String dietDisplay = currentPatient.getDiet();
            if (currentPatient.isAdaDiet() && !dietDisplay.contains("ADA")) {
                dietDisplay += " (ADA)";
            }
            dietText.setText("Diet: " + dietDisplay);

            // Display fluid restriction
            String fluidRestriction = currentPatient.getFluidRestriction();
            if (fluidRestriction == null || fluidRestriction.isEmpty() || fluidRestriction.equals("None")) {
                fluidRestrictionText.setText("Fluid Restriction: None");
            } else {
                fluidRestrictionText.setText("Fluid Restriction: " + fluidRestriction);
            }

            // Display texture modifications
            String textureMods = currentPatient.getTextureModifications();
            if (textureMods == null || textureMods.isEmpty() || textureMods.equals("None")) {
                textureModificationsText.setText("Texture Modifications: None");
            } else {
                textureModificationsText.setText("Texture Modifications: " + textureMods);
            }

            // Display created date
            Date createdDate = currentPatient.getCreatedDate();
            if (createdDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                createdDateText.setText("Added: " + sdf.format(createdDate));
            }

            // Update meal status checkboxes
            breakfastCompleteCheckBox.setChecked(currentPatient.isBreakfastComplete() || currentPatient.isBreakfastNPO());
            lunchCompleteCheckBox.setChecked(currentPatient.isLunchComplete() || currentPatient.isLunchNPO());
            dinnerCompleteCheckBox.setChecked(currentPatient.isDinnerComplete() || currentPatient.isDinnerNPO());

            // Update order date
            Date orderDate = currentPatient.getOrderDate();
            if (orderDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                orderDateText.setText("Order Date: " + sdf.format(orderDate));
            } else {
                orderDateText.setText("Order Date: Not ordered yet");
            }

            // Update meal items
            updateMealItemsDisplay();

        } catch (Exception e) {
            Log.e(TAG, "Error loading patient data", e);
            Toast.makeText(this, "Error loading patient data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMealItemsDisplay() {
        // Show meal items if they exist
        boolean hasMealPlan = false;

        // Breakfast items
        String breakfastItems = currentPatient.getBreakfastItems();
        if (breakfastItems != null && !breakfastItems.isEmpty()) {
            breakfastItemsText.setText("Items: " + breakfastItems);
            breakfastItemsText.setVisibility(View.VISIBLE);
            hasMealPlan = true;
        } else {
            breakfastItemsText.setVisibility(View.GONE);
        }

        String breakfastDrinks = currentPatient.getBreakfastDrinks();
        if (breakfastDrinks != null && !breakfastDrinks.isEmpty() && !breakfastDrinks.startsWith("FL:")) {
            breakfastDrinksText.setText("Drinks: " + breakfastDrinks);
            breakfastDrinksText.setVisibility(View.VISIBLE);
            hasMealPlan = true;
        } else {
            breakfastDrinksText.setVisibility(View.GONE);
        }

        // Lunch items
        String lunchItems = currentPatient.getLunchItems();
        if (lunchItems != null && !lunchItems.isEmpty()) {
            lunchItemsText.setText("Items: " + lunchItems);
            lunchItemsText.setVisibility(View.VISIBLE);
            hasMealPlan = true;
        } else {
            lunchItemsText.setVisibility(View.GONE);
        }

        String lunchDrinks = currentPatient.getLunchDrinks();
        if (lunchDrinks != null && !lunchDrinks.isEmpty() && !lunchDrinks.startsWith("FL:")) {
            lunchDrinksText.setText("Drinks: " + lunchDrinks);
            lunchDrinksText.setVisibility(View.VISIBLE);
            hasMealPlan = true;
        } else {
            lunchDrinksText.setVisibility(View.GONE);
        }

        // Dinner items
        String dinnerItems = currentPatient.getDinnerItems();
        if (dinnerItems != null && !dinnerItems.isEmpty()) {
            dinnerItemsText.setText("Items: " + dinnerItems);
            dinnerItemsText.setVisibility(View.VISIBLE);
            hasMealPlan = true;
        } else {
            dinnerItemsText.setVisibility(View.GONE);
        }

        String dinnerDrinks = currentPatient.getDinnerDrinks();
        if (dinnerDrinks != null && !dinnerDrinks.isEmpty() && !dinnerDrinks.startsWith("FL:")) {
            dinnerDrinksText.setText("Drinks: " + dinnerDrinks);
            dinnerDrinksText.setVisibility(View.VISIBLE);
            hasMealPlan = true;
        } else {
            dinnerDrinksText.setVisibility(View.GONE);
        }

        // Show/hide meal items section based on whether there's a meal plan
        mealItemsSection.setVisibility(hasMealPlan ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        // Edit Patient Button
        if (editPatientButton != null) {
            editPatientButton.setOnClickListener(v -> editPatientInfo());
        }

        // Edit Meal Plan Button
        if (editMealPlanButton != null) {
            editMealPlanButton.setOnClickListener(v -> editMealPlan());
        }

        // Delete Patient Button
        if (deletePatientButton != null) {
            deletePatientButton.setOnClickListener(v -> confirmDeletePatient());
        }

        // Meal status checkboxes are read-only in this view
        breakfastCompleteCheckBox.setEnabled(false);
        lunchCompleteCheckBox.setEnabled(false);
        dinnerCompleteCheckBox.setEnabled(false);
    }

    private void editPatientInfo() {
        Intent intent = new Intent(this, EditPatientActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void editMealPlan() {
        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("patient_id", (long) patientId);
        intent.putExtra("diet", currentPatient.getDiet());
        intent.putExtra("is_ada", currentPatient.isAdaDiet());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void confirmDeletePatient() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete " + currentPatient.getFullName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient() {
        try {
            int result = patientDAO.deletePatientById(patientId);
            if (result > 0) {
                Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                // Return to existing patients list
                Intent intent = new Intent(this, ExistingPatientsActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error deleting patient", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting patient", e);
            Toast.makeText(this, "Error deleting patient: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload patient data when returning to this activity
        loadPatientData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_patient_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                loadPatientData();
                return true;
            case R.id.action_edit:
                editPatientInfo();
                return true;
            case R.id.action_delete:
                confirmDeletePatient();
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