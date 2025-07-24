package com.hospital.dietary;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Item;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

public class MealPlanningActivity extends AppCompatActivity {

    private static final String TAG = "MealPlanningActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private ItemDAO itemDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // Patient info
    private Patient currentPatient;
    private long patientId;
    private String diet;
    private boolean isAdaDiet;

    // UI Components that match your layout
    private TextView patientInfoText;
    private Button homeButton;
    private Toolbar toolbar;
    private LinearLayout breakfastSection, lunchSection, dinnerSection;
    private Button saveMealPlanButton;

    // Meal completion status
    private boolean breakfastComplete = false;
    private boolean lunchComplete = false;
    private boolean dinnerComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);

        // Get data from intent
        patientId = getIntent().getLongExtra("patient_id", -1);
        diet = getIntent().getStringExtra("diet");
        isAdaDiet = getIntent().getBooleanExtra("is_ada_diet", false);
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        if (patientId == -1 || diet == null) {
            Toast.makeText(this, "Error: Missing patient information", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        itemDAO = new ItemDAO(dbHelper);

        // Load patient data
        currentPatient = patientDAO.getPatientById((int) patientId);

        if (currentPatient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Planning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        populatePatientInfo();
        loadMealItems();
        setupListeners();
    }

    private void initializeViews() {
        // Initialize UI components that exist in your layout
        patientInfoText = findViewById(R.id.patientInfoText);
        homeButton = findViewById(R.id.homeButton);
        toolbar = findViewById(R.id.toolbar);

        // Meal sections
        breakfastSection = findViewById(R.id.breakfastSection);
        lunchSection = findViewById(R.id.lunchSection);
        dinnerSection = findViewById(R.id.dinnerSection);

        // Save button - FIXED: Use the correct ID from the layout
        saveMealPlanButton = findViewById(R.id.saveOrderButton);
        if (saveMealPlanButton == null) {
            // Create a save button dynamically if it doesn't exist in layout
            saveMealPlanButton = new Button(this);
            saveMealPlanButton.setText("Save Meal Plan");
            saveMealPlanButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            saveMealPlanButton.setTextColor(getResources().getColor(android.R.color.white));

            // Try to add it to a container - look for a suitable parent
            LinearLayout parentContainer = findViewById(R.id.dinnerSection);
            if (parentContainer == null) {
                parentContainer = findViewById(R.id.lunchSection);
            }
            if (parentContainer == null) {
                parentContainer = findViewById(R.id.breakfastSection);
            }

            if (parentContainer != null) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(16, 32, 16, 16);
                saveMealPlanButton.setLayoutParams(params);
                parentContainer.addView(saveMealPlanButton);
            }
        }
    }

    private void populatePatientInfo() {
        if (currentPatient != null && patientInfoText != null) {
            String patientInfo = String.format("Patient: %s | Location: %s | Diet: %s",
                    currentPatient.getFullName(),
                    currentPatient.getLocationInfo(),
                    currentPatient.getDiet());
            patientInfoText.setText(patientInfo);
        }
    }

    private void loadMealItems() {
        // Clear existing meal sections
        if (breakfastSection != null) {
            clearMealSection(breakfastSection, "🍳 Breakfast");
        }
        if (lunchSection != null) {
            clearMealSection(lunchSection, "🥙 Lunch");
        }
        if (dinnerSection != null) {
            clearMealSection(dinnerSection, "🍽️ Dinner");
        }

        // Load meals based on diet type
        if (diet != null) {
            if (diet.contains("Clear Liquid") || diet.contains("Full Liquid") || diet.contains("Puree")) {
                loadPredeterminedMealItems();
            } else {
                loadRegularMealItems();
            }
        }
    }

    private void clearMealSection(LinearLayout container, String title) {
        container.removeAllViews();

        // Add section title
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setPadding(0, 16, 0, 8);
        container.addView(titleView);
    }

    private void loadPredeterminedMealItems() {
        // Add meal sections with proper titles - FIXED ADA display
        if (breakfastSection != null) {
            addPredeterminedMealSection(breakfastSection, "Breakfast",
                    currentPatient.getBreakfastDiet() != null ? currentPatient.getBreakfastDiet() : diet,
                    currentPatient.isBreakfastAda() || isAdaDiet);
        }
        if (lunchSection != null) {
            addPredeterminedMealSection(lunchSection, "Lunch",
                    currentPatient.getLunchDiet() != null ? currentPatient.getLunchDiet() : diet,
                    currentPatient.isLunchAda() || isAdaDiet);
        }
        if (dinnerSection != null) {
            addPredeterminedMealSection(dinnerSection, "Dinner",
                    currentPatient.getDinnerDiet() != null ? currentPatient.getDinnerDiet() : diet,
                    currentPatient.isDinnerAda() || isAdaDiet);
        }
    }

    // FIXED: Remove duplicate (ADA) display issue
    private void addPredeterminedMealSection(LinearLayout container, String mealType, String mealDiet, boolean mealIsAda) {
        // Create meal header
        TextView title = new TextView(this);

        // FIXED: Clean up diet display to avoid duplicate (ADA)
        String cleanDietDisplay;
        if (mealDiet.contains("(ADA)") || mealIsAda) {
            // If diet already contains (ADA) or meal is ADA, format it properly
            if (mealDiet.contains("(ADA)")) {
                // Diet already has (ADA) in it, use as is
                cleanDietDisplay = mealDiet;
            } else {
                // mealIsAda is true but diet doesn't contain (ADA), add it once
                cleanDietDisplay = mealDiet + " (ADA)";
            }
        } else {
            // Not an ADA diet, use diet as is
            cleanDietDisplay = mealDiet;
        }

        title.setText(mealType + " - " + cleanDietDisplay);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 8, 0, 8);
        container.addView(title);

        // Get specific items based on diet type and meal
        String itemsList = getPredeterminedItemsForMeal(mealDiet, mealType, mealIsAda);

        TextView itemsText = new TextView(this);
        itemsText.setText(itemsList);
        itemsText.setPadding(16, 8, 16, 8);
        itemsText.setTextSize(14);
        container.addView(itemsText);

        // Add button for editing individual meal diet (optional feature)
        Button editMealButton = new Button(this);
        editMealButton.setText("Edit " + mealType + " Diet");
        editMealButton.setTextSize(12);
        editMealButton.setPadding(16, 8, 16, 8);
        editMealButton.setOnClickListener(v -> showSimpleEditMealDietDialog(mealType, mealDiet, mealIsAda));
        container.addView(editMealButton);
    }

    private void showSimpleEditMealDietDialog(String mealType, String currentDiet, boolean isAda) {
        // Simple implementation
        Toast.makeText(this, "Edit " + mealType + " diet feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private String getPredeterminedItemsForMeal(String mealDiet, String mealType, boolean isAda) {
        // Simple implementation - return basic items based on meal type
        switch (mealType.toLowerCase()) {
            case "breakfast":
                return "• Standard breakfast items based on " + mealDiet + " diet\n• Items will be provided according to dietary restrictions";
            case "lunch":
                return "• Standard lunch items based on " + mealDiet + " diet\n• Items will be provided according to dietary restrictions";
            case "dinner":
                return "• Standard dinner items based on " + mealDiet + " diet\n• Items will be provided according to dietary restrictions";
            default:
                return "• Items will be provided based on " + mealDiet + " diet";
        }
    }

    private void loadRegularMealItems() {
        // For non-liquid diets, show standard meal planning interface
        if (breakfastSection != null) {
            TextView breakfastNote = new TextView(this);
            breakfastNote.setText("Standard breakfast items will be provided based on diet type.");
            breakfastNote.setPadding(16, 4, 16, 8);
            breakfastNote.setTextSize(14);
            breakfastSection.addView(breakfastNote);
            breakfastComplete = true;
        }

        if (lunchSection != null) {
            TextView lunchNote = new TextView(this);
            lunchNote.setText("Standard lunch items will be provided based on diet type.");
            lunchNote.setPadding(16, 4, 16, 8);
            lunchNote.setTextSize(14);
            lunchSection.addView(lunchNote);
            lunchComplete = true;
        }

        if (dinnerSection != null) {
            TextView dinnerNote = new TextView(this);
            dinnerNote.setText("Standard dinner items will be provided based on diet type.");
            dinnerNote.setPadding(16, 4, 16, 8);
            dinnerNote.setTextSize(14);
            dinnerSection.addView(dinnerNote);
            dinnerComplete = true;
        }
    }

    private void setupListeners() {
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }

        if (saveMealPlanButton != null) {
            saveMealPlanButton.setOnClickListener(v -> saveMealPlan());
        }
    }

    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void saveMealPlan() {
        try {
            // Mark all meals as complete
            currentPatient.setBreakfastComplete(true);
            currentPatient.setLunchComplete(true);
            currentPatient.setDinnerComplete(true);

            boolean success = patientDAO.updatePatient(currentPatient);

            if (success) {
                Toast.makeText(this, "Meal plan saved successfully!", Toast.LENGTH_SHORT).show();

                // Return to previous activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("meal_plan_saved", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save meal plan", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error saving meal plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error saving meal plan", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_meal_planning, menu);
        } catch (Exception e) {
            Log.d(TAG, "Menu file not found, skipping menu inflation");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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