package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import android.graphics.Typeface;

public class MealPlanningActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // Patient information
    private int patientId;
    private String patientName;
    private String wing;
    private String room;
    private String diet;
    private String fluidRestriction;
    private String textureModifications;

    // UI Components
    private Toolbar toolbar;
    private TextView patientInfoText;
    private Button saveOrderButton;
    private Button backButton;
    private Button homeButton;

    // Meal sections
    private LinearLayout breakfastSection;
    private LinearLayout lunchSection;
    private LinearLayout dinnerSection;

    // NPO checkboxes
    private CheckBox breakfastNPOCheckbox;
    private CheckBox lunchNPOCheckbox;
    private CheckBox dinnerNPOCheckbox;

    // Meal content containers
    private LinearLayout breakfastItemsContainer;
    private LinearLayout lunchItemsContainer;
    private LinearLayout dinnerItemsContainer;

    // FIXED: Better completion tracking
    private boolean breakfastComplete = false;
    private boolean lunchComplete = false;
    private boolean dinnerComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);

        // Get information from intent
        extractIntentData();

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Setup toolbar
        setupToolbar();

        // Initialize UI
        initializeUI();

        // Setup listeners
        setupListeners();

        // Load existing patient data
        loadPatientData();

        // Load meal items
        loadMealItems();

        // Initial button state update
        updateSaveButtonState();
    }

    private void extractIntentData() {
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        patientId = getIntent().getIntExtra("patient_id", -1);
        patientName = getIntent().getStringExtra("patient_name");
        wing = getIntent().getStringExtra("wing");
        room = getIntent().getStringExtra("room");
        diet = getIntent().getStringExtra("diet");
        fluidRestriction = getIntent().getStringExtra("fluid_restriction");
        textureModifications = getIntent().getStringExtra("texture_modifications");
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Planning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
                goToMainMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private void initializeUI() {
        patientInfoText = findViewById(R.id.patientInfoText);
        saveOrderButton = findViewById(R.id.saveOrderButton);
        backButton = findViewById(R.id.backButton);
    //  homeButton = findViewById(R.id.homeButton);

        // Meal sections
        breakfastSection = findViewById(R.id.breakfastSection);
        lunchSection = findViewById(R.id.lunchSection);
        dinnerSection = findViewById(R.id.dinnerSection);

        // NPO checkboxes
        breakfastNPOCheckbox = findViewById(R.id.breakfastNPOCheckbox);
        lunchNPOCheckbox = findViewById(R.id.lunchNPOCheckbox);
        dinnerNPOCheckbox = findViewById(R.id.dinnerNPOCheckbox);

        // Meal content containers
        breakfastItemsContainer = findViewById(R.id.breakfastItemsContainer);
        lunchItemsContainer = findViewById(R.id.lunchItemsContainer);
        dinnerItemsContainer = findViewById(R.id.dinnerItemsContainer);

        // Display patient info
        displayPatientInfo();
    }

    private void displayPatientInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Patient: ").append(patientName).append("\n");
        info.append("Location: ").append(wing).append(" - Room ").append(room).append("\n");
        info.append("Diet: ").append(diet);

        if (fluidRestriction != null && !fluidRestriction.equals("None")) {
            info.append("\nFluid Restriction: ").append(fluidRestriction);
        }

        if (textureModifications != null && !textureModifications.isEmpty()) {
            info.append("\nTexture Modifications: ").append(textureModifications);
        }

        patientInfoText.setText(info.toString());
    }

    private void setupListeners() {
        saveOrderButton.setOnClickListener(v -> saveOrder());
        backButton.setOnClickListener(v -> finish());

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }

        // FIXED: NPO checkbox listeners with proper completion logic
        breakfastNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                breakfastItemsContainer.setVisibility(View.GONE);
                breakfastComplete = true; // NPO counts as complete
            } else {
                breakfastItemsContainer.setVisibility(View.VISIBLE);
                breakfastComplete = checkMealItemsSelected(breakfastItemsContainer);
            }
            updateSaveButtonState();
        });

        lunchNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                lunchItemsContainer.setVisibility(View.GONE);
                lunchComplete = true; // NPO counts as complete
            } else {
                lunchItemsContainer.setVisibility(View.VISIBLE);
                lunchComplete = checkMealItemsSelected(lunchItemsContainer);
            }
            updateSaveButtonState();
        });

        dinnerNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dinnerItemsContainer.setVisibility(View.GONE);
                dinnerComplete = true; // NPO counts as complete
            } else {
                dinnerItemsContainer.setVisibility(View.VISIBLE);
                dinnerComplete = checkMealItemsSelected(dinnerItemsContainer);
            }
            updateSaveButtonState();
        });
    }

    private void loadPatientData() {
        if (patientId != -1) {
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                // Update completion status
                breakfastComplete = patient.isBreakfastComplete();
                lunchComplete = patient.isLunchComplete();
                dinnerComplete = patient.isDinnerComplete();

                // Update NPO status
                breakfastNPOCheckbox.setChecked(patient.isBreakfastNPO());
                lunchNPOCheckbox.setChecked(patient.isLunchNPO());
                dinnerNPOCheckbox.setChecked(patient.isDinnerNPO());

                // Hide containers if NPO is checked
                if (patient.isBreakfastNPO()) {
                    breakfastItemsContainer.setVisibility(View.GONE);
                }
                if (patient.isLunchNPO()) {
                    lunchItemsContainer.setVisibility(View.GONE);
                }
                if (patient.isDinnerNPO()) {
                    dinnerItemsContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    private void loadMealItems() {
        // FIXED: Special handling for Clear Liquid diets
        if (diet.startsWith("Clear Liquid")) {
            loadClearLiquidItems();
        } else {
            loadRegularMealItems();
        }
    }

    private void loadClearLiquidItems() {
        // Clear liquid patients get predetermined items - no selection needed
        breakfastComplete = true;
        lunchComplete = true;
        dinnerComplete = true;

        // Hide NPO checkboxes for clear liquid (predetermined menu)
        breakfastNPOCheckbox.setVisibility(View.GONE);
        lunchNPOCheckbox.setVisibility(View.GONE);
        dinnerNPOCheckbox.setVisibility(View.GONE);

        // Show predetermined clear liquid items
        addClearLiquidMenuItems();

        // Auto-save button should be enabled
        updateSaveButtonState();
    }

    private void addClearLiquidMenuItems() {
        // Add clear liquid breakfast items
        addClearLiquidMealItems(breakfastItemsContainer, "Breakfast", diet.contains("ADA"));

        // Add clear liquid lunch items
        addClearLiquidMealItems(lunchItemsContainer, "Lunch", diet.contains("ADA"));

        // Add clear liquid dinner items
        addClearLiquidMealItems(dinnerItemsContainer, "Dinner", diet.contains("ADA"));
    }

    private void addClearLiquidMealItems(LinearLayout container, String mealType, boolean isADA) {
        container.removeAllViews();

        TextView title = new TextView(this);
        title.setText(mealType + " - Clear Liquid" + (isADA ? " ADA" : "") + " (Predetermined)");
        title.setTextSize(16);
        // FIXED: Use setTypeface instead of setTextStyle
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 16, 0, 8);
        container.addView(title);

        // ADA substitutions for clear liquids
        String[] items;
        if (isADA) {
            items = new String[]{
                    "• Apple Juice (instead of Orange Juice)",
                    "• Sprite Zero (instead of Sprite)",
                    "• Sugar Free Jello (instead of regular Jello)",
                    "• Clear Broth",
                    "• Water",
                    "• Tea/Coffee (no sugar)"
            };
        } else {
            items = new String[]{
                    "• Orange Juice",
                    "• Sprite",
                    "• Jello",
                    "• Clear Broth",
                    "• Water",
                    "• Tea/Coffee"
            };
        }

        for (String item : items) {
            TextView itemView = new TextView(this);
            itemView.setText(item);
            itemView.setPadding(16, 4, 0, 4);
            container.addView(itemView);
        }
    }

    private void loadRegularMealItems() {
        // TODO: Load actual meal items from database for regular diets
        // For now, add placeholder items with selection listeners
        addSelectableMealItems(breakfastItemsContainer, "Breakfast");
        addSelectableMealItems(lunchItemsContainer, "Lunch");
        addSelectableMealItems(dinnerItemsContainer, "Dinner");
    }

    private void addSelectableMealItems(LinearLayout container, String mealType) {
        container.removeAllViews();

        TextView title = new TextView(this);
        title.setText(mealType + " Items");
        title.setTextSize(16);
        // FIXED: Use setTypeface instead of setTextStyle
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 16, 0, 8);
        container.addView(title);

        // Add some sample checkboxes for meal items
        String[] sampleItems = {"Main Course", "Side Dish", "Beverage", "Dessert"};

        for (String item : sampleItems) {
            CheckBox itemCheckbox = new CheckBox(this);
            itemCheckbox.setText(item);
            itemCheckbox.setPadding(16, 8, 0, 8);

            // FIXED: Add listener to track meal completion
            itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateMealCompletion(container);
            });

            container.addView(itemCheckbox);
        }
    }

    // FIXED: Method to check if meal items are selected
    private boolean checkMealItemsSelected(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkbox = (CheckBox) child;
                if (checkbox.isChecked()) {
                    return true; // At least one item selected
                }
            }
        }
        return false; // No items selected
    }

    // FIXED: Update meal completion when items are selected/deselected
    private void updateMealCompletion(LinearLayout container) {
        if (container == breakfastItemsContainer && !breakfastNPOCheckbox.isChecked()) {
            breakfastComplete = checkMealItemsSelected(container);
        } else if (container == lunchItemsContainer && !lunchNPOCheckbox.isChecked()) {
            lunchComplete = checkMealItemsSelected(container);
        } else if (container == dinnerItemsContainer && !dinnerNPOCheckbox.isChecked()) {
            dinnerComplete = checkMealItemsSelected(container);
        }
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        boolean allMealsComplete = breakfastComplete && lunchComplete && dinnerComplete;
        saveOrderButton.setEnabled(allMealsComplete);
        saveOrderButton.setText(allMealsComplete ? "Save Complete Order" : "Complete All Meals First");
    }

    private void saveOrder() {
        if (patientId != -1) {
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                // FIXED: Properly update completion status
                patient.setBreakfastComplete(breakfastComplete);
                patient.setLunchComplete(lunchComplete);
                patient.setDinnerComplete(dinnerComplete);

                // Update NPO status
                patient.setBreakfastNPO(breakfastNPOCheckbox.isChecked());
                patient.setLunchNPO(lunchNPOCheckbox.isChecked());
                patient.setDinnerNPO(dinnerNPOCheckbox.isChecked());

                boolean success = patientDAO.updatePatient(patient);

                if (success) {
                    String message = "Order saved successfully!";
                    if (diet.startsWith("Clear Liquid")) {
                        message += "\n\nClear Liquid order completed with predetermined menu items.";
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    finish(); // Return to previous activity
                } else {
                    Toast.makeText(this, "Failed to save order", Toast.LENGTH_SHORT).show();
                }
            }
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