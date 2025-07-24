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

            populatePatientInformation();
            populateMealItems();
            loadOrderStatus();

        } catch (Exception e) {
            Log.e(TAG, "Error loading patient data", e);
            Toast.makeText(this, "Error loading patient data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void populatePatientInformation() {
        // Patient basic info
        patientNameText.setText(currentPatient.getFullName());
        locationText.setText("📍 " + currentPatient.getWing() + " - Room " + currentPatient.getRoomNumber());

        String dietInfo = "🍽️ " + currentPatient.getDiet();
        if (currentPatient.isAdaDiet()) {
            dietInfo += " (ADA)";
        }
        dietText.setText(dietInfo);

        // Fluid restriction
        String fluidRestriction = currentPatient.getFluidRestriction();
        if (fluidRestriction != null && !fluidRestriction.trim().isEmpty()) {
            fluidRestrictionText.setText("💧 Fluid Restriction: " + fluidRestriction);
            fluidRestrictionText.setVisibility(View.VISIBLE);
        } else {
            fluidRestrictionText.setVisibility(View.GONE);
        }

        // Texture modifications
        StringBuilder textureInfo = new StringBuilder();
        if (currentPatient.isMechanicalChopped()) textureInfo.append("Mechanical Chopped, ");
        if (currentPatient.isMechanicalGround()) textureInfo.append("Mechanical Ground, ");
        if (currentPatient.isBiteSize()) textureInfo.append("Bite Size, ");
        if (currentPatient.isBreadOK()) textureInfo.append("Bread OK, ");
        if (currentPatient.isNectarThick()) textureInfo.append("Nectar Thick, ");
        if (currentPatient.isPuddingThick()) textureInfo.append("Pudding Thick, ");
        if (currentPatient.isHoneyThick()) textureInfo.append("Honey Thick, ");
        if (currentPatient.isExtraGravy()) textureInfo.append("Extra Gravy, ");
        if (currentPatient.isMeatsOnly()) textureInfo.append("Meats Only, ");

        if (textureInfo.length() > 0) {
            // Remove trailing comma and space
            textureInfo.setLength(textureInfo.length() - 2);
            textureModificationsText.setText("🔧 Texture Modifications: " + textureInfo.toString());
            textureModificationsText.setVisibility(View.VISIBLE);
        } else {
            textureModificationsText.setVisibility(View.GONE);
        }

        // Created date
        if (currentPatient.getCreatedDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' h:mm a", Locale.getDefault());
            createdDateText.setText("📅 Created: " + dateFormat.format(currentPatient.getCreatedDate()));
        }
    }

    private void populateMealItems() {
        // Breakfast items
        String breakfastItems = currentPatient.getBreakfastItems();
        if (breakfastItems != null && !breakfastItems.trim().isEmpty()) {
            breakfastItemsText.setText("• " + breakfastItems.replace("\n", "\n• "));
        } else {
            breakfastItemsText.setText("No items selected");
        }

        // Lunch items
        String lunchItems = currentPatient.getLunchItems();
        if (lunchItems != null && !lunchItems.trim().isEmpty()) {
            lunchItemsText.setText("• " + lunchItems.replace("\n", "\n• "));
        } else {
            lunchItemsText.setText("No items selected");
        }

        // Dinner items
        String dinnerItems = currentPatient.getDinnerItems();
        if (dinnerItems != null && !dinnerItems.trim().isEmpty()) {
            dinnerItemsText.setText("• " + dinnerItems.replace("\n", "\n• "));
        } else {
            dinnerItemsText.setText("No items selected");
        }

        // Breakfast drinks
        populateDrinkInfo(breakfastDrinksText, currentPatient.getBreakfastDrinks(), currentPatient.getBreakfastJuices());

        // Lunch drinks
        populateDrinkInfo(lunchDrinksText, currentPatient.getLunchDrinks(), currentPatient.getLunchJuices());

        // Dinner drinks
        populateDrinkInfo(dinnerDrinksText, currentPatient.getDinnerDrinks(), currentPatient.getDinnerJuices());
    }

    private void populateDrinkInfo(TextView textView, String drinks, String juices) {
        StringBuilder drinkInfo = new StringBuilder();

        if (drinks != null && !drinks.trim().isEmpty()) {
            String[] drinkArray = drinks.split("\n");
            for (String drink : drinkArray) {
                if (!drink.trim().isEmpty()) {
                    if (drinkInfo.length() > 0) drinkInfo.append("\n");
                    drinkInfo.append("• ").append(drink.trim());
                }
            }
        }

        if (juices != null && !juices.trim().isEmpty()) {
            String[] juiceArray = juices.split("\n");
            for (String juice : juiceArray) {
                if (!juice.trim().isEmpty()) {
                    if (drinkInfo.length() > 0) drinkInfo.append("\n");
                    drinkInfo.append("• ").append(juice.trim());
                }
            }
        }

        if (drinkInfo.length() > 0) {
            textView.setText(drinkInfo.toString());
        } else {
            textView.setText("No drinks selected");
        }
    }

    private void loadOrderStatus() {
        if (currentPatient == null) return;

        try {
            // Show current completion status
            breakfastCompleteCheckBox.setChecked(currentPatient.isBreakfastComplete());
            lunchCompleteCheckBox.setChecked(currentPatient.isLunchComplete());
            dinnerCompleteCheckBox.setChecked(currentPatient.isDinnerComplete());

            // Set today's date
            SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
            orderDateText.setText("Status for " + displayFormat.format(new Date()));

            mealStatusSection.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            mealStatusSection.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // Edit patient button
        editPatientButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewPatientActivity.class);  // Or dedicated edit activity
            intent.putExtra("edit_patient_id", patientId);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivityForResult(intent, 1001);
        });

        // Edit meal plan button
        editMealPlanButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MealPlanningActivity.class);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivityForResult(intent, 1002);
        });

        // Delete patient button
        deletePatientButton.setOnClickListener(v -> showDeletePatientDialog());

        // Meal completion checkboxes
        breakfastCompleteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateMealCompletion("breakfast", isChecked));

        lunchCompleteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateMealCompletion("lunch", isChecked));

        dinnerCompleteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateMealCompletion("dinner", isChecked));
    }

    private void updateMealCompletion(String meal, boolean isComplete) {
        if (currentPatient == null) return;

        try {
            switch (meal) {
                case "breakfast":
                    currentPatient.setBreakfastComplete(isComplete);
                    break;
                case "lunch":
                    currentPatient.setLunchComplete(isComplete);
                    break;
                case "dinner":
                    currentPatient.setDinnerComplete(isComplete);
                    break;
            }

            boolean result = patientDAO.updatePatient(currentPatient);
            if (result) {
                String status = isComplete ? "completed" : "not completed";
                Toast.makeText(this, meal.substring(0, 1).toUpperCase() + meal.substring(1) +
                        " marked as " + status, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update meal status", Toast.LENGTH_SHORT).show();
                // Revert checkbox state
                switch (meal) {
                    case "breakfast":
                        breakfastCompleteCheckBox.setChecked(!isComplete);
                        break;
                    case "lunch":
                        lunchCompleteCheckBox.setChecked(!isComplete);
                        break;
                    case "dinner":
                        dinnerCompleteCheckBox.setChecked(!isComplete);
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating meal status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeletePatientDialog() {
        if (currentPatient == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete patient " +
                        currentPatient.getPatientFirstName() + " " +
                        currentPatient.getPatientLastName() + "?\n\n" +
                        "This will also delete all associated meal plans and orders.\n\n" +
                        "This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient() {
        try {
            boolean success = patientDAO.deletePatient(patientId);

            if (success) {
                Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting patient", e);
            Toast.makeText(this, "Error deleting patient: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Reload patient data if it was edited
            loadPatientData();
        }
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
            case R.id.action_home:
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_edit:
                editPatientButton.performClick();
                return true;
            case R.id.action_delete:
                deletePatientButton.performClick();
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