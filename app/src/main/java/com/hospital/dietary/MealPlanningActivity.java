package com.hospital.dietary;

import android.content.DialogInterface;
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

    // UI Components
    private TextView patientInfoText;
    private Button homeButton;
    private Toolbar toolbar;
    private LinearLayout breakfastSection, lunchSection, dinnerSection;
    private Button saveMealPlanButton;

    // Clear Liquid menu items
    private ClearLiquidMenuItem breakfastJuice;
    private ClearLiquidMenuItem breakfastBroth;
    private ClearLiquidMenuItem breakfastJello;
    private ClearLiquidMenuItem breakfastCoffee;
    private ClearLiquidMenuItem breakfastSprite;

    private ClearLiquidMenuItem lunchJuice;
    private ClearLiquidMenuItem lunchBroth;
    private ClearLiquidMenuItem lunchJello;
    private ClearLiquidMenuItem lunchIcedTea;
    private ClearLiquidMenuItem lunchSprite;

    private ClearLiquidMenuItem dinnerJuice;
    private ClearLiquidMenuItem dinnerBroth;
    private ClearLiquidMenuItem dinnerJello;
    private ClearLiquidMenuItem dinnerIcedTea;
    private ClearLiquidMenuItem dinnerSprite;

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
        isAdaDiet = getIntent().getBooleanExtra("is_ada", false);
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        itemDAO = new ItemDAO(dbHelper);

        // Setup toolbar
        setupToolbar();

        // Initialize UI
        initializeUI();

        // Load patient data
        loadPatientData();

        // Setup listeners
        setupListeners();

        // Load meal items based on diet type
        loadMealItems();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Planning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeUI() {
        patientInfoText = findViewById(R.id.patientInfoText);
        breakfastSection = findViewById(R.id.breakfastSection);
        lunchSection = findViewById(R.id.lunchSection);
        dinnerSection = findViewById(R.id.dinnerSection);
        saveMealPlanButton = findViewById(R.id.saveOrderButton);
        homeButton = findViewById(R.id.homeButton);

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
    }

    private void setupListeners() {
        if (saveMealPlanButton != null) {
            saveMealPlanButton.setOnClickListener(v -> saveMealPlan());
        }
    }

    private void loadPatientData() {
        if (patientId > 0) {
            currentPatient = patientDAO.getPatientById((int) patientId);
            if (currentPatient != null) {
                String patientInfo = String.format("%s | %s | Diet: %s%s",
                        currentPatient.getFullName(),
                        currentPatient.getLocationInfo(),
                        currentPatient.getDiet(),
                        (currentPatient.isAdaDiet() || isAdaDiet) ? " (ADA)" : "");
                patientInfoText.setText(patientInfo);

                // Update diet and ADA status from patient data
                diet = currentPatient.getDiet();
                isAdaDiet = currentPatient.isAdaDiet() || isAdaDiet;
            }
        }
    }

    private void loadMealItems() {
        // Clear existing meal sections
        clearMealSection(breakfastSection, "🍳 Breakfast");
        clearMealSection(lunchSection, "🥙 Lunch");
        clearMealSection(dinnerSection, "🍽️ Dinner");

        // Load meals based on diet type
        if (diet != null && diet.contains("Clear Liquid")) {
            loadClearLiquidMealItems();
        } else if (diet != null && (diet.contains("Full Liquid") || diet.contains("Puree"))) {
            loadPredeterminedMealItems();
        } else {
            loadRegularMealItems();
        }
    }

    private void clearMealSection(LinearLayout container, String title) {
        container.removeAllViews();

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(18);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setPadding(0, 16, 0, 8);
        container.addView(titleView);
    }

    private void loadClearLiquidMealItems() {
        // Breakfast items
        breakfastJuice = new ClearLiquidMenuItem("Juice",
                isAdaDiet ? "Apple Juice" : "Orange Juice", false);
        breakfastBroth = new ClearLiquidMenuItem("Broth",
                "Chicken Broth", true, new String[]{"Chicken Broth", "Beef Broth"});
        breakfastJello = new ClearLiquidMenuItem("Jello",
                isAdaDiet ? "Sugar Free Jello" : "Jello", false);
        breakfastCoffee = new ClearLiquidMenuItem("Coffee",
                "Coffee", true, new String[]{"Coffee", "Decaf Coffee"});
        breakfastSprite = new ClearLiquidMenuItem("Soda",
                isAdaDiet ? "Sprite Zero" : "Sprite", false);

        addClearLiquidItemsToSection(breakfastSection,
                breakfastJuice, breakfastBroth, breakfastJello, breakfastCoffee, breakfastSprite);

        // Lunch items
        lunchJuice = new ClearLiquidMenuItem("Juice", "Cranberry Juice", false);
        lunchBroth = new ClearLiquidMenuItem("Broth",
                "Beef Broth", true, new String[]{"Beef Broth", "Chicken Broth"});
        lunchJello = new ClearLiquidMenuItem("Jello",
                isAdaDiet ? "Sugar Free Jello" : "Jello", false);
        lunchIcedTea = new ClearLiquidMenuItem("Tea", "Iced Tea", false);
        lunchSprite = new ClearLiquidMenuItem("Soda",
                isAdaDiet ? "Sprite Zero" : "Sprite", false);

        addClearLiquidItemsToSection(lunchSection,
                lunchJuice, lunchBroth, lunchJello, lunchIcedTea, lunchSprite);

        // Dinner items
        dinnerJuice = new ClearLiquidMenuItem("Juice", "Cranberry Juice", false);
        dinnerBroth = new ClearLiquidMenuItem("Broth",
                "Chicken Broth", true, new String[]{"Chicken Broth", "Beef Broth"});
        dinnerJello = new ClearLiquidMenuItem("Jello",
                isAdaDiet ? "Sugar Free Jello" : "Jello", false);
        dinnerIcedTea = new ClearLiquidMenuItem("Tea", "Iced Tea", false);
        dinnerSprite = new ClearLiquidMenuItem("Soda",
                isAdaDiet ? "Sprite Zero" : "Sprite", false);

        addClearLiquidItemsToSection(dinnerSection,
                dinnerJuice, dinnerBroth, dinnerJello, dinnerIcedTea, dinnerSprite);

        // Enable save button for clear liquid diets
        if (saveMealPlanButton != null) {
            saveMealPlanButton.setEnabled(true);
        }
    }

    private void addClearLiquidItemsToSection(LinearLayout section, ClearLiquidMenuItem... items) {
        for (ClearLiquidMenuItem item : items) {
            View itemView = createClearLiquidItemView(item);
            section.addView(itemView);
        }
    }

    private View createClearLiquidItemView(final ClearLiquidMenuItem item) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemLayout.setPadding(16, 8, 16, 8);
        itemLayout.setBackgroundResource(R.drawable.clear_liquid_item_background);

        TextView itemText = new TextView(this);
        itemText.setText(item.category + ": " + item.currentValue);
        itemText.setTextSize(16);
        itemText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        itemLayout.addView(itemText);

        if (item.isEditable) {
            Button editButton = new Button(this);
            editButton.setText("Edit");
            editButton.setTextSize(12);
            editButton.setOnClickListener(v -> showEditDialog(item, itemText));
            itemLayout.addView(editButton);
        }

        return itemLayout;
    }

    private void showEditDialog(final ClearLiquidMenuItem item, final TextView itemText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select " + item.category);

        builder.setItems(item.options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                item.currentValue = item.options[which];
                itemText.setText(item.category + ": " + item.currentValue);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadPredeterminedMealItems() {
        // Add meal sections with proper diet display
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

    private void addPredeterminedMealSection(LinearLayout container, String mealType, String mealDiet, boolean mealIsAda) {
        TextView title = new TextView(this);

        String cleanDietDisplay;
        if (mealDiet.contains("(ADA)") || mealIsAda) {
            if (mealDiet.contains("(ADA)")) {
                cleanDietDisplay = mealDiet;
            } else {
                cleanDietDisplay = mealDiet + " (ADA)";
            }
        } else {
            cleanDietDisplay = mealDiet;
        }

        title.setText(mealType + " - " + cleanDietDisplay);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 8, 0, 8);
        container.addView(title);

        String itemsList = getPredeterminedItemsForMeal(mealDiet, mealType, mealIsAda);

        TextView itemsText = new TextView(this);
        itemsText.setText(itemsList);
        itemsText.setPadding(16, 8, 16, 8);
        itemsText.setTextSize(14);
        container.addView(itemsText);

        Button editMealButton = new Button(this);
        editMealButton.setText("Edit " + mealType + " Diet");
        editMealButton.setTextSize(12);
        editMealButton.setPadding(16, 8, 16, 8);
        editMealButton.setOnClickListener(v ->
                Toast.makeText(this, "Edit " + mealType + " diet feature coming soon", Toast.LENGTH_SHORT).show());
        container.addView(editMealButton);
    }

    private String getPredeterminedItemsForMeal(String diet, String mealType, boolean isAda) {
        StringBuilder items = new StringBuilder();

        if (diet.contains("Clear Liquid")) {
            switch (mealType) {
                case "Breakfast":
                    items.append("• ").append(isAda ? "Apple Juice" : "Orange Juice").append("\n");
                    items.append("• Chicken Broth\n");
                    items.append("• ").append(isAda ? "Sugar Free Jello" : "Jello").append("\n");
                    items.append("• Coffee\n");
                    items.append("• ").append(isAda ? "Sprite Zero" : "Sprite");
                    break;
                case "Lunch":
                    items.append("• Cranberry Juice\n");
                    items.append("• Beef Broth\n");
                    items.append("• ").append(isAda ? "Sugar Free Jello" : "Jello").append("\n");
                    items.append("• Iced Tea\n");
                    items.append("• ").append(isAda ? "Sprite Zero" : "Sprite");
                    break;
                case "Dinner":
                    items.append("• Cranberry Juice\n");
                    items.append("• Chicken Broth\n");
                    items.append("• ").append(isAda ? "Sugar Free Jello" : "Jello").append("\n");
                    items.append("• Iced Tea\n");
                    items.append("• ").append(isAda ? "Sprite Zero" : "Sprite");
                    break;
            }
        } else if (diet.contains("Full Liquid")) {
            items.append("• Cream soup\n• Milk\n• Juice\n• Ice cream\n• Pudding");
        } else if (diet.contains("Puree")) {
            items.append("• Pureed meat\n• Pureed vegetables\n• Smooth soup\n• Yogurt\n• Applesauce");
        }

        return items.toString();
    }

    private void loadRegularMealItems() {
        // Implementation for regular diet meal planning
        TextView placeholder = new TextView(this);
        placeholder.setText("Regular meal planning interface");
        placeholder.setPadding(16, 16, 16, 16);
        breakfastSection.addView(placeholder);
    }

    private void saveMealPlan() {
        if (currentPatient == null) {
            Toast.makeText(this, "Error: No patient selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // For Clear Liquid diet, save the current selections
        if (diet != null && diet.contains("Clear Liquid")) {
            // Save breakfast items
            currentPatient.setBreakfastItems(buildClearLiquidMealString(
                    breakfastJuice, breakfastBroth, breakfastJello, breakfastCoffee, breakfastSprite));

            // Save lunch items
            currentPatient.setLunchItems(buildClearLiquidMealString(
                    lunchJuice, lunchBroth, lunchJello, lunchIcedTea, lunchSprite));

            // Save dinner items
            currentPatient.setDinnerItems(buildClearLiquidMealString(
                    dinnerJuice, dinnerBroth, dinnerJello, dinnerIcedTea, dinnerSprite));

            // Mark meals as complete
            currentPatient.setBreakfastComplete(true);
            currentPatient.setLunchComplete(true);
            currentPatient.setDinnerComplete(true);
        }

        // Update patient in database
        int result = patientDAO.updatePatient(currentPatient);

        if (result > 0) {
            Toast.makeText(this, "Meal plan saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving meal plan", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildClearLiquidMealString(ClearLiquidMenuItem... items) {
        StringBuilder meal = new StringBuilder();
        for (ClearLiquidMenuItem item : items) {
            if (meal.length() > 0) meal.append(", ");
            meal.append(item.currentValue);
        }
        return meal.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_meal_planning, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                loadMealItems();
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

    // Helper class for Clear Liquid menu items
    private static class ClearLiquidMenuItem {
        String category;
        String currentValue;
        boolean isEditable;
        String[] options;

        ClearLiquidMenuItem(String category, String currentValue, boolean isEditable) {
            this.category = category;
            this.currentValue = currentValue;
            this.isEditable = isEditable;
            this.options = null;
        }

        ClearLiquidMenuItem(String category, String currentValue, boolean isEditable, String[] options) {
            this.category = category;
            this.currentValue = currentValue;
            this.isEditable = isEditable;
            this.options = options;
        }
    }
}