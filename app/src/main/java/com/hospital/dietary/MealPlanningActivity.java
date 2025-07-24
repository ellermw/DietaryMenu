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
    private Button backButton;
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
        // Initialize UI components
        patientInfoText = findViewById(R.id.patientInfoText);
        homeButton = findViewById(R.id.homeButton);
        backButton = findViewById(R.id.backButton);
        toolbar = findViewById(R.id.toolbar);

        // Meal sections
        breakfastSection = findViewById(R.id.breakfastSection);
        lunchSection = findViewById(R.id.lunchSection);
        dinnerSection = findViewById(R.id.dinnerSection);

        // Save button - Use the correct ID from the layout
        saveMealPlanButton = findViewById(R.id.saveOrderButton);
    }

    private void populatePatientInfo() {
        if (currentPatient != null && patientInfoText != null) {
            String patientInfo = String.format("Patient: %s | Location: %s - Room %s | Diet: %s%s",
                    currentPatient.getFullName(),
                    currentPatient.getWing(),
                    currentPatient.getRoomNumber(),
                    currentPatient.getDiet(),
                    (currentPatient.isAdaDiet() || isAdaDiet) ? " (ADA)" : ""
            );
            patientInfoText.setText(patientInfo);
        }
    }

    private void loadMealItems() {
        if ("Clear Liquid".equals(diet)) {
            loadClearLiquidItems();
        } else {
            loadStandardDietItems();
        }
    }

    private void loadClearLiquidItems() {
        // Clear existing views
        if (breakfastSection != null) breakfastSection.removeAllViews();
        if (lunchSection != null) lunchSection.removeAllViews();
        if (dinnerSection != null) dinnerSection.removeAllViews();

        // Add section headers
        addSectionHeader(breakfastSection, "🌅 Breakfast");
        addSectionHeader(lunchSection, "☀️ Lunch");
        addSectionHeader(dinnerSection, "🌙 Dinner");

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
        getMenuInflater().inflate(R.menu.menu_meal_planning, menu);
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

            int rowsUpdated = patientDAO.updatePatient(currentPatient);

            if (rowsUpdated > 0) {
                Toast.makeText(this, "Meal plan saved successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error saving meal plan", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving meal plan", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveClearLiquidSelections() {
        // Save breakfast selections
        if (breakfastJuice != null) {
            String breakfastItems = breakfastJuice.getSelectedOption() + ", " +
                    breakfastBroth.getSelectedOption() + ", " +
                    breakfastJello.getSelectedOption();
            currentPatient.setBreakfastItems(breakfastItems);

            String breakfastDrinks = breakfastCoffee.getSelectedOption() + ", " +
                    breakfastSprite.getSelectedOption();
            currentPatient.setBreakfastDrinks(breakfastDrinks);
        }

        // Save lunch selections
        if (lunchJuice != null) {
            String lunchItems = lunchJuice.getSelectedOption() + ", " +
                    lunchBroth.getSelectedOption() + ", " +
                    lunchJello.getSelectedOption();
            currentPatient.setLunchItems(lunchItems);

            String lunchDrinks = lunchIcedTea.getSelectedOption() + ", " +
                    lunchSprite.getSelectedOption();
            currentPatient.setLunchDrinks(lunchDrinks);
        }

        // Save dinner selections
        if (dinnerJuice != null) {
            String dinnerItems = dinnerJuice.getSelectedOption() + ", " +
                    dinnerBroth.getSelectedOption() + ", " +
                    dinnerJello.getSelectedOption();
            currentPatient.setDinnerItems(dinnerItems);

            String dinnerDrinks = dinnerIcedTea.getSelectedOption() + ", " +
                    dinnerSprite.getSelectedOption();
            currentPatient.setDinnerDrinks(dinnerDrinks);
        }
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
            return optionSpinner.getSelectedItem().toString();
        }
    }
}