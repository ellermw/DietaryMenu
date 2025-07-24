package com.hospital.dietary;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

public class MealPlanningActivity extends AppCompatActivity {

    private static final String TAG = "MealPlanningActivity";

    // Database
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // Patient information
    private long patientId;
    private Patient currentPatient;
    private String diet;
    private boolean isAdaDiet;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView patientInfoText;
    private Button backButton;
    private Button homeButton;
    private Button saveMealPlanButton;
    private LinearLayout breakfastSection;
    private LinearLayout lunchSection;
    private LinearLayout dinnerSection;

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

    // Track completion status
    private boolean breakfastComplete = false;
    private boolean lunchComplete = false;
    private boolean dinnerComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);

        // Get data from intent
        patientId = getIntent().getLongExtra("patient_id", 0);
        diet = getIntent().getStringExtra("diet");
        isAdaDiet = getIntent().getBooleanExtra("is_ada_diet", false);
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Load patient
        currentPatient = patientDAO.getPatientById((int) patientId);
        if (currentPatient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI
        initializeUI();

        // Load appropriate menu based on diet type
        loadMenuForDiet();

        // Setup listeners
        setupListeners();
    }

    private void initializeUI() {
        patientInfoText = findViewById(R.id.patientInfoText);
        backButton = findViewById(R.id.backButton);
        homeButton = findViewById(R.id.homeButton);
        saveMealPlanButton = findViewById(R.id.saveOrderButton);  // FIXED: Use correct ID from layout
        breakfastSection = findViewById(R.id.breakfastSection);
        lunchSection = findViewById(R.id.lunchSection);
        dinnerSection = findViewById(R.id.dinnerSection);

        // Set patient info
        String patientInfo = currentPatient.getFullName() + " - " +
                currentPatient.getWing() + " " + currentPatient.getRoomNumber() +
                "\nDiet: " + diet;
        if (isAdaDiet) {
            patientInfo += " (ADA)";
        }
        patientInfoText.setText(patientInfo);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Planning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadMenuForDiet() {
        // Clear existing content
        breakfastSection.removeAllViews();
        lunchSection.removeAllViews();
        dinnerSection.removeAllViews();

        // Add section headers
        addSectionHeader(breakfastSection, "Breakfast");
        addSectionHeader(lunchSection, "Lunch");
        addSectionHeader(dinnerSection, "Dinner");

        if ("Clear Liquid".equals(diet)) {
            loadClearLiquidItems();
        } else {
            // For other diets, show standard message
            loadStandardDietItems();
        }
    }

    private void loadClearLiquidItems() {
        // Breakfast items
        breakfastJuice = addClearLiquidItem(breakfastSection, "Juice", new String[]{"Apple", "Cranberry", "Grape"});
        breakfastBroth = addClearLiquidItem(breakfastSection, "Broth", new String[]{"Chicken", "Beef", "Vegetable"});
        breakfastJello = addClearLiquidItem(breakfastSection, "Jello", new String[]{"Red", "Orange", "Yellow"});
        breakfastCoffee = addClearLiquidItem(breakfastSection, "Coffee/Tea", new String[]{"Coffee", "Tea", "Decaf Coffee"});
        breakfastSprite = addClearLiquidItem(breakfastSection, "Clear Soda", new String[]{"Sprite", "7-Up", "Ginger Ale"});

        // Lunch items
        lunchJuice = addClearLiquidItem(lunchSection, "Juice", new String[]{"Apple", "Cranberry", "Grape"});
        lunchBroth = addClearLiquidItem(lunchSection, "Broth", new String[]{"Chicken", "Beef", "Vegetable"});
        lunchJello = addClearLiquidItem(lunchSection, "Jello", new String[]{"Red", "Orange", "Yellow"});
        lunchIcedTea = addClearLiquidItem(lunchSection, "Iced Tea", new String[]{"Regular", "Decaf", "Sweetened"});
        lunchSprite = addClearLiquidItem(lunchSection, "Clear Soda", new String[]{"Sprite", "7-Up", "Ginger Ale"});

        // Dinner items
        dinnerJuice = addClearLiquidItem(dinnerSection, "Juice", new String[]{"Apple", "Cranberry", "Grape"});
        dinnerBroth = addClearLiquidItem(dinnerSection, "Broth", new String[]{"Chicken", "Beef", "Vegetable"});
        dinnerJello = addClearLiquidItem(dinnerSection, "Jello", new String[]{"Red", "Orange", "Yellow"});
        dinnerIcedTea = addClearLiquidItem(dinnerSection, "Iced Tea", new String[]{"Regular", "Decaf", "Sweetened"});
        dinnerSprite = addClearLiquidItem(dinnerSection, "Clear Soda", new String[]{"Sprite", "7-Up", "Ginger Ale"});

        breakfastComplete = true;
        lunchComplete = true;
        dinnerComplete = true;
        updateSaveButtonState();
    }

    private void loadStandardDietItems() {
        // For other diet types, show a message that standard items will be provided
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

        updateSaveButtonState();
    }

    private void setupListeners() {
        // Back button listener
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Home button listener
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        }

        // Save button listener
        if (saveMealPlanButton != null) {
            saveMealPlanButton.setOnClickListener(v -> saveMealPlan());
        }
    }

    private void addSectionHeader(LinearLayout section, String title) {
        if (section == null) return;

        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(18);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(0, 16, 0, 8);
        section.addView(header);
    }

    private ClearLiquidMenuItem addClearLiquidItem(LinearLayout section, String itemName, String[] options) {
        if (section == null) return null;

        ClearLiquidMenuItem menuItem = new ClearLiquidMenuItem(this);
        menuItem.setItemName(itemName);
        menuItem.setOptions(options);
        section.addView(menuItem);

        return menuItem;
    }

    private void updateSaveButtonState() {
        if (saveMealPlanButton != null) {
            boolean allComplete = breakfastComplete && lunchComplete && dinnerComplete;
            saveMealPlanButton.setEnabled(allComplete);
            saveMealPlanButton.setBackgroundColor(allComplete ?
                    getResources().getColor(android.R.color.holo_green_dark) :
                    getResources().getColor(android.R.color.darker_gray));
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
            case R.id.action_home:
                // Navigate to MainMenuActivity
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            case R.id.action_save:
                saveMealPlan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveMealPlan() {
        try {
            // Mark all meals as complete
            currentPatient.setBreakfastComplete(true);
            currentPatient.setLunchComplete(true);
            currentPatient.setDinnerComplete(true);

            // Save Clear Liquid selections if applicable
            if ("Clear Liquid".equals(diet)) {
                saveClearLiquidSelections();
            }

            // FIX: Changed from int to boolean
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

    private void saveClearLiquidSelections() {
        // Collect breakfast selections
        List<String> breakfastItems = new ArrayList<>();
        if (breakfastJuice != null && breakfastJuice.hasSelection()) {
            breakfastItems.add(breakfastJuice.getSelectedItem());
        }
        if (breakfastBroth != null && breakfastBroth.hasSelection()) {
            breakfastItems.add(breakfastBroth.getSelectedItem());
        }
        if (breakfastJello != null && breakfastJello.hasSelection()) {
            breakfastItems.add(breakfastJello.getSelectedItem());
        }
        if (breakfastCoffee != null && breakfastCoffee.hasSelection()) {
            breakfastItems.add(breakfastCoffee.getSelectedItem());
        }
        if (breakfastSprite != null && breakfastSprite.hasSelection()) {
            breakfastItems.add(breakfastSprite.getSelectedItem());
        }
        currentPatient.setBreakfastItems(String.join(", ", breakfastItems));

        // Collect lunch selections
        List<String> lunchItems = new ArrayList<>();
        if (lunchJuice != null && lunchJuice.hasSelection()) {
            lunchItems.add(lunchJuice.getSelectedItem());
        }
        if (lunchBroth != null && lunchBroth.hasSelection()) {
            lunchItems.add(lunchBroth.getSelectedItem());
        }
        if (lunchJello != null && lunchJello.hasSelection()) {
            lunchItems.add(lunchJello.getSelectedItem());
        }
        if (lunchIcedTea != null && lunchIcedTea.hasSelection()) {
            lunchItems.add(lunchIcedTea.getSelectedItem());
        }
        if (lunchSprite != null && lunchSprite.hasSelection()) {
            lunchItems.add(lunchSprite.getSelectedItem());
        }
        currentPatient.setLunchItems(String.join(", ", lunchItems));

        // Collect dinner selections
        List<String> dinnerItems = new ArrayList<>();
        if (dinnerJuice != null && dinnerJuice.hasSelection()) {
            dinnerItems.add(dinnerJuice.getSelectedItem());
        }
        if (dinnerBroth != null && dinnerBroth.hasSelection()) {
            dinnerItems.add(dinnerBroth.getSelectedItem());
        }
        if (dinnerJello != null && dinnerJello.hasSelection()) {
            dinnerItems.add(dinnerJello.getSelectedItem());
        }
        if (dinnerIcedTea != null && dinnerIcedTea.hasSelection()) {
            dinnerItems.add(dinnerIcedTea.getSelectedItem());
        }
        if (dinnerSprite != null && dinnerSprite.hasSelection()) {
            dinnerItems.add(dinnerSprite.getSelectedItem());
        }
        currentPatient.setDinnerItems(String.join(", ", dinnerItems));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // Inner class for Clear Liquid menu items
    private class ClearLiquidMenuItem extends LinearLayout {
        private TextView itemNameText;
        private Spinner optionSpinner;
        private String[] options;

        public ClearLiquidMenuItem(android.content.Context context) {
            super(context);
            setOrientation(HORIZONTAL);
            setPadding(16, 8, 16, 8);

            itemNameText = new TextView(context);
            itemNameText.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            itemNameText.setTextSize(16);
            addView(itemNameText);

            optionSpinner = new Spinner(context);
            optionSpinner.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            addView(optionSpinner);
        }

        public void setItemName(String name) {
            itemNameText.setText(name);
        }

        public void setOptions(String[] options) {
            this.options = options;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            optionSpinner.setAdapter(adapter);
        }

        public String getSelectedOption() {
            return optionSpinner.getSelectedItem() != null ?
                    optionSpinner.getSelectedItem().toString() : "";
        }

        public String getSelectedItem() {
            return getSelectedOption();
        }

        public boolean hasSelection() {
            return optionSpinner.getSelectedItem() != null;
        }
    }
}