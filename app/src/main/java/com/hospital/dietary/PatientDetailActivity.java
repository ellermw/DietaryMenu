package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.dao.FinalizedOrderDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.FinalizedOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private FinalizedOrderDAO finalizedOrderDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // Patient data
    private Patient currentPatient;
    private int patientId;

    // UI Components - Patient Info Section
    private TextView patientNameText;
    private TextView roomLocationText;
    private TextView dietTypeText;
    private TextView fluidRestrictionText;
    private TextView textureModsText;
    private TextView adaStatusText;

    // UI Components - Meal Plan Section
    private LinearLayout mealPlanSection;
    private TextView breakfastItemsText;
    private TextView lunchItemsText;
    private TextView dinnerItemsText;
    private TextView breakfastDrinksText;
    private TextView lunchDrinksText;
    private TextView dinnerDrinksText;

    // UI Components - Order Status Section
    private LinearLayout orderStatusSection;
    private TextView orderDateText;
    private CheckBox breakfastCompleteCheckBox;
    private CheckBox lunchCompleteCheckBox;
    private CheckBox dinnerCompleteCheckBox;

    // Action buttons
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

        // Validate patient ID
        if (patientId == -1) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        finalizedOrderDAO = new FinalizedOrderDAO(dbHelper);

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        loadPatientData();
        setupListeners();
    }

    private void initializeUI() {
        // Patient info section
        patientNameText = findViewById(R.id.patientNameText);
        roomLocationText = findViewById(R.id.roomLocationText);
        dietTypeText = findViewById(R.id.dietTypeText);
        fluidRestrictionText = findViewById(R.id.fluidRestrictionText);
        textureModsText = findViewById(R.id.textureModsText);
        adaStatusText = findViewById(R.id.adaStatusText);

        // Meal plan section
        mealPlanSection = findViewById(R.id.mealPlanSection);
        breakfastItemsText = findViewById(R.id.breakfastItemsText);
        lunchItemsText = findViewById(R.id.lunchItemsText);
        dinnerItemsText = findViewById(R.id.dinnerItemsText);
        breakfastDrinksText = findViewById(R.id.breakfastDrinksText);
        lunchDrinksText = findViewById(R.id.lunchDrinksText);
        dinnerDrinksText = findViewById(R.id.dinnerDrinksText);

        // Order status section
        orderStatusSection = findViewById(R.id.orderStatusSection);
        orderDateText = findViewById(R.id.orderDateText);
        breakfastCompleteCheckBox = findViewById(R.id.breakfastCompleteCheckBox);
        lunchCompleteCheckBox = findViewById(R.id.lunchCompleteCheckBox);
        dinnerCompleteCheckBox = findViewById(R.id.dinnerCompleteCheckBox);

        // Action buttons
        editPatientButton = findViewById(R.id.editPatientButton);
        editMealPlanButton = findViewById(R.id.editMealPlanButton);
        deletePatientButton = findViewById(R.id.deletePatientButton);
    }

    // FIXED: Load patient data and populate UI
    private void loadPatientData() {
        try {
            // Load patient from database
            currentPatient = patientDAO.getPatientById(patientId);

            if (currentPatient == null) {
                Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Update action bar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(currentPatient.getPatientFirstName() + " " +
                        currentPatient.getPatientLastName());
            }

            // Populate patient information
            populatePatientInfo();

            // Load and display meal plan
            loadMealPlan();

            // Load order status
            loadOrderStatus();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading patient data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void populatePatientInfo() {
        if (currentPatient == null) return;

        // Patient name
        patientNameText.setText(currentPatient.getPatientFirstName() + " " +
                currentPatient.getPatientLastName());

        // Room location
        roomLocationText.setText(currentPatient.getWing() + " - Room " +
                currentPatient.getRoomNumber());

        // Diet type
        dietTypeText.setText(currentPatient.getDietType());

        // Fluid restriction
        String fluidRestriction = currentPatient.getFluidRestriction();
        if (fluidRestriction == null || fluidRestriction.equals("No Restriction")) {
            fluidRestrictionText.setText("No fluid restrictions");
        } else {
            fluidRestrictionText.setText("Fluid restriction: " + fluidRestriction);
        }

        // FIXED: Texture modifications - show multiple selections
        StringBuilder textureMods = new StringBuilder();
        if (currentPatient.isMechanicalChopped()) textureMods.append("Mechanical Chopped, ");
        if (currentPatient.isMechanicalGround()) textureMods.append("Mechanical Ground, ");
        if (currentPatient.isBiteSize()) textureMods.append("Bite Size, ");
        if (currentPatient.isBreadOK()) textureMods.append("Bread OK, ");

        if (textureMods.length() > 0) {
            // Remove trailing comma and space
            textureMods.setLength(textureMods.length() - 2);
            textureModsText.setText("Texture: " + textureMods.toString());
        } else {
            textureModsText.setText("Texture: Regular");
        }

        // ADA status
        if (currentPatient.isAdaDiet()) {
            adaStatusText.setText("ADA Diet: Yes");
            adaStatusText.setVisibility(View.VISIBLE);
        } else {
            adaStatusText.setVisibility(View.GONE);
        }
    }

    private void loadMealPlan() {
        try {
            // Get today's order for this patient
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = dateFormat.format(new Date());

            FinalizedOrder todayOrder = finalizedOrderDAO.getOrderByWingRoomAndDate(
                    currentPatient.getWing(),
                    currentPatient.getRoomNumber(),
                    today
            );

            if (todayOrder != null) {
                mealPlanSection.setVisibility(View.VISIBLE);
                populateMealPlan(todayOrder);
            } else {
                // Check if there's a meal plan for tomorrow
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
                String tomorrow = dateFormat.format(cal.getTime());

                FinalizedOrder tomorrowOrder = finalizedOrderDAO.getOrderByWingRoomAndDate(
                        currentPatient.getWing(),
                        currentPatient.getRoomNumber(),
                        tomorrow
                );

                if (tomorrowOrder != null) {
                    mealPlanSection.setVisibility(View.VISIBLE);
                    populateMealPlan(tomorrowOrder);
                    orderDateText.setText("Meal Plan for Tomorrow");
                } else {
                    // No meal plan found
                    mealPlanSection.setVisibility(View.GONE);
                    Toast.makeText(this, "No meal plan found for this patient", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mealPlanSection.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading meal plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void populateMealPlan(FinalizedOrder order) {
        // Breakfast items
        List<String> breakfastItems = order.getBreakfastItems();
        if (breakfastItems != null && !breakfastItems.isEmpty()) {
            breakfastItemsText.setText("• " + String.join("\n• ", breakfastItems));
        } else {
            breakfastItemsText.setText("No items selected");
        }

        // Lunch items
        List<String> lunchItems = order.getLunchItems();
        if (lunchItems != null && !lunchItems.isEmpty()) {
            lunchItemsText.setText("• " + String.join("\n• ", lunchItems));
        } else {
            lunchItemsText.setText("No items selected");
        }

        // Dinner items
        List<String> dinnerItems = order.getDinnerItems();
        if (dinnerItems != null && !dinnerItems.isEmpty()) {
            dinnerItemsText.setText("• " + String.join("\n• ", dinnerItems));
        } else {
            dinnerItemsText.setText("No items selected");
        }

        // Breakfast drinks
        List<String> breakfastDrinks = order.getBreakfastDrinks();
        List<String> breakfastJuices = order.getBreakfastJuices();
        populateDrinks(breakfastDrinksText, breakfastDrinks, breakfastJuices);

        // Lunch drinks
        List<String> lunchDrinks = order.getLunchDrinks();
        List<String> lunchJuices = order.getLunchJuices();
        populateDrinks(lunchDrinksText, lunchDrinks, lunchJuices);

        // Dinner drinks
        List<String> dinnerDrinks = order.getDinnerDrinks();
        List<String> dinnerJuices = order.getDinnerJuices();
        populateDrinks(dinnerDrinksText, dinnerDrinks, dinnerJuices);
    }

    private void populateDrinks(TextView textView, List<String> drinks, List<String> juices) {
        StringBuilder drinkText = new StringBuilder();

        if (drinks != null && !drinks.isEmpty()) {
            for (String drink : drinks) {
                if (!drink.trim().isEmpty()) {
                    drinkText.append("• ").append(drink).append("\n");
                }
            }
        }

        if (juices != null && !juices.isEmpty()) {
            for (String juice : juices) {
                if (!juice.trim().isEmpty()) {
                    drinkText.append("• ").append(juice).append("\n");
                }
            }
        }

        if (drinkText.length() > 0) {
            // Remove trailing newline
            drinkText.setLength(drinkText.length() - 1);
            textView.setText(drinkText.toString());
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

            orderStatusSection.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            orderStatusSection.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // Edit patient button
        editPatientButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PatientInfoActivity.class);
            intent.putExtra("edit_patient_id", patientId);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });

        // Edit meal plan button
        editMealPlanButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MealPlanningActivity.class);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
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
                        currentPatient.getPatientLastName() + "?\n\nThis will also delete all associated meal plans and orders.\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean result = patientDAO.deletePatient(patientId);
                    if (result) {
                        Toast.makeText(this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close this activity
                    } else {
                        Toast.makeText(this, "Failed to delete patient", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload patient data when returning from edit screens
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
            case R.id.action_edit_meals:
                editMealPlanButton.performClick();
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