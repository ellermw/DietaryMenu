package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.Item;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Typeface;

public class MealPlanningActivity extends AppCompatActivity {

    private static final String TAG = "MealPlanningActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private ItemDAO itemDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // Patient info
    private long patientId;
    private String diet;
    private boolean isAdaDiet;
    private Patient patient;

    // UI Components
    private TextView patientInfoText;
    private ScrollView mainScrollView;

    // Meal containers
    private LinearLayout breakfastItemsContainer;
    private LinearLayout lunchItemsContainer;
    private LinearLayout dinnerItemsContainer;

    // NPO checkboxes
    private CheckBox breakfastNPOCheckbox;
    private CheckBox lunchNPOCheckbox;
    private CheckBox dinnerNPOCheckbox;

    // Meal completion flags
    private boolean breakfastComplete = false;
    private boolean lunchComplete = false;
    private boolean dinnerComplete = false;

    // Spinner references for regular diets
    private Spinner lunchProteinSpinner;
    private Spinner lunchStarchSpinner;
    private Spinner lunchVegetableSpinner;
    private Spinner lunchDessertSpinner;
    private Spinner dinnerProteinSpinner;
    private Spinner dinnerStarchSpinner;
    private Spinner dinnerVegetableSpinner;
    private Spinner dinnerDessertSpinner;

    // Save button
    private Button saveMealPlanButton;

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

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Planning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        loadPatientData();
        loadMealItems();
        setupNPOListeners();
        updateSaveButtonState();
    }

    private void initializeUI() {
        patientInfoText = findViewById(R.id.patientInfoText);
        // mainScrollView = findViewById(R.id.mainScrollView); // Not needed, using ScrollView as root

        breakfastItemsContainer = findViewById(R.id.breakfastItemsContainer);
        lunchItemsContainer = findViewById(R.id.lunchItemsContainer);
        dinnerItemsContainer = findViewById(R.id.dinnerItemsContainer);

        breakfastNPOCheckbox = findViewById(R.id.breakfastNPOCheckbox);
        lunchNPOCheckbox = findViewById(R.id.lunchNPOCheckbox);
        dinnerNPOCheckbox = findViewById(R.id.dinnerNPOCheckbox);

        saveMealPlanButton = findViewById(R.id.saveOrderButton); // Using existing saveOrderButton

        // Setup save button listener
        if (saveMealPlanButton != null) {
            saveMealPlanButton.setOnClickListener(v -> saveMealPlan());
            saveMealPlanButton.setText("💾 Save Meal Plan"); // Update button text
        }

        // Load and display patient information
        try {
            patient = patientDAO.getPatientById((int) patientId); // Cast long to int
            if (patient != null && patientInfoText != null) {
                String patientInfo = patient.getFullName() + " - " + patient.getWing() + " " +
                        patient.getRoomNumber() + "\nDiet: " + patient.getDiet();
                patientInfoText.setText(patientInfo);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading patient info: " + e.getMessage());
        }
    }

    private void setupNPOListeners() {
        if (breakfastNPOCheckbox != null) {
            breakfastNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (breakfastItemsContainer != null) {
                    breakfastItemsContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                }
                updateSaveButtonState();
            });
        }

        if (lunchNPOCheckbox != null) {
            lunchNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (lunchItemsContainer != null) {
                    lunchItemsContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                }
                updateSaveButtonState();
            });
        }

        if (dinnerNPOCheckbox != null) {
            dinnerNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (dinnerItemsContainer != null) {
                    dinnerItemsContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                }
                updateSaveButtonState();
            });
        }
    }

    private void loadPatientData() {
        try {
            Patient patient = patientDAO.getPatientById((int) patientId); // Cast long to int
            if (patient != null) {
                // Set NPO states
                if (breakfastNPOCheckbox != null) breakfastNPOCheckbox.setChecked(patient.isBreakfastNPO());
                if (lunchNPOCheckbox != null) lunchNPOCheckbox.setChecked(patient.isLunchNPO());
                if (dinnerNPOCheckbox != null) dinnerNPOCheckbox.setChecked(patient.isDinnerNPO());

                // Hide containers if NPO is checked
                if (patient.isBreakfastNPO() && breakfastItemsContainer != null) {
                    breakfastItemsContainer.setVisibility(View.GONE);
                }
                if (patient.isLunchNPO() && lunchItemsContainer != null) {
                    lunchItemsContainer.setVisibility(View.GONE);
                }
                if (patient.isDinnerNPO() && dinnerItemsContainer != null) {
                    dinnerItemsContainer.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading patient data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMealItems() {
        // Special handling for liquid diets
        if (diet != null && (diet.contains("Clear Liquid") || diet.contains("Full Liquid") || diet.contains("Puree"))) {
            loadLiquidDietItems();
        } else {
            loadRegularMealItems();
        }
    }

    private void loadLiquidDietItems() {
        // Liquid diets are predetermined - mark as complete
        breakfastComplete = true;
        lunchComplete = true;
        dinnerComplete = true;

        // Hide NPO checkboxes and show predetermined items
        if (breakfastNPOCheckbox != null) breakfastNPOCheckbox.setVisibility(View.GONE);
        if (lunchNPOCheckbox != null) lunchNPOCheckbox.setVisibility(View.GONE);
        if (dinnerNPOCheckbox != null) dinnerNPOCheckbox.setVisibility(View.GONE);

        // Show predetermined liquid diet items
        showPredeterminedLiquidItems();
        updateSaveButtonState();
    }

    private void showPredeterminedLiquidItems() {
        // Show predetermined items for each meal
        addPredeterminedMealItems(breakfastItemsContainer, "Breakfast");
        addPredeterminedMealItems(lunchItemsContainer, "Lunch");
        addPredeterminedMealItems(dinnerItemsContainer, "Dinner");
    }

    private void addPredeterminedMealItems(LinearLayout container, String mealType) {
        if (container == null) return;

        container.removeAllViews();

        TextView title = new TextView(this);
        title.setText(mealType + " - " + diet + (isAdaDiet || diet.contains("ADA") ? " (ADA)" : ""));
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 8, 0, 8);
        container.addView(title);

        // Get specific items based on diet type and meal
        String itemsList = getPredeterminedItemsForMeal(diet, mealType, isAdaDiet || diet.contains("ADA"));

        TextView itemsText = new TextView(this);
        itemsText.setText(itemsList);
        itemsText.setPadding(16, 8, 16, 8);
        itemsText.setTextSize(14);
        container.addView(itemsText);
    }

    private String getPredeterminedItemsForMeal(String dietType, String mealType, boolean isADA) {
        StringBuilder items = new StringBuilder();

        if (dietType.contains("Clear Liquid")) {
            // Clear Liquid diet items
            if (isADA) {
                items.append("• Apple Juice (ADA)\n");
                items.append("• Sprite Zero (ADA)\n");
                items.append("• Sugar Free Jello (ADA)\n");
            } else {
                items.append("• Orange Juice\n");
                items.append("• Sprite\n");
                items.append("• Jello\n");
            }
            items.append("• Clear Broth\n");
            items.append("• Water\n");
            items.append("• Tea/Coffee\n");

        } else if (dietType.contains("Full Liquid")) {
            // Full Liquid diet items
            switch (mealType) {
                case "Breakfast":
                    items.append("• Apple Juice (120ml)\n");
                    items.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• Cream of Wheat\n");
                    items.append("• Coffee (200ml)\n");
                    items.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    items.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    items.append("• Ensure (240ml)\n");
                    break;

                case "Lunch":
                    items.append("• Cranberry Juice (120ml)\n");
                    items.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• Cream of Chicken Soup\n");
                    items.append("• Pudding").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    items.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    items.append("• Ensure (240ml)\n");
                    break;

                case "Dinner":
                    items.append("• Apple Juice (120ml)\n");
                    items.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• Tomato Soup\n");
                    items.append("• Pudding").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    items.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    items.append("• Ensure (240ml)\n");
                    break;
            }

        } else if (dietType.contains("Puree")) {
            // Puree diet items
            items.append("Predetermined items for ").append(dietType).append(" diet");
        }

        return items.toString();
    }

    // FIXED: Proper implementation for regular meal items with dropdown spinners
    private void loadRegularMealItems() {
        // Clear containers
        if (breakfastItemsContainer != null) breakfastItemsContainer.removeAllViews();
        if (lunchItemsContainer != null) lunchItemsContainer.removeAllViews();
        if (dinnerItemsContainer != null) dinnerItemsContainer.removeAllViews();

        // Add breakfast header (usually minimal for regular diets)
        addBreakfastItems();

        // Add lunch items with dropdown spinners
        addLunchItems();

        // Add dinner items with dropdown spinners
        addDinnerItems();
    }

    private void addBreakfastItems() {
        if (breakfastItemsContainer == null) return;

        TextView breakfastTitle = new TextView(this);
        breakfastTitle.setText("Breakfast");
        breakfastTitle.setTextSize(16);
        breakfastTitle.setTypeface(null, Typeface.BOLD);
        breakfastTitle.setPadding(0, 8, 0, 8);
        breakfastItemsContainer.addView(breakfastTitle);

        TextView breakfastNote = new TextView(this);
        breakfastNote.setText("Standard breakfast items will be provided based on diet type.");
        breakfastNote.setPadding(16, 4, 16, 8);
        breakfastNote.setTextSize(14);
        breakfastItemsContainer.addView(breakfastNote);

        breakfastComplete = true; // Mark as complete for regular diets
    }

    private void addLunchItems() {
        if (lunchItemsContainer == null) return;

        TextView lunchTitle = new TextView(this);
        lunchTitle.setText("Lunch");
        lunchTitle.setTextSize(16);
        lunchTitle.setTypeface(null, Typeface.BOLD);
        lunchTitle.setPadding(0, 8, 0, 8);
        lunchItemsContainer.addView(lunchTitle);

        // Add spinners for lunch components
        addMealComponentSpinner(lunchItemsContainer, "Protein", "lunchProtein");
        addMealComponentSpinner(lunchItemsContainer, "Starch", "lunchStarch");
        addMealComponentSpinner(lunchItemsContainer, "Vegetable", "lunchVegetable");
        addMealComponentSpinner(lunchItemsContainer, "Dessert", "lunchDessert");
    }

    private void addDinnerItems() {
        if (dinnerItemsContainer == null) return;

        TextView dinnerTitle = new TextView(this);
        dinnerTitle.setText("Dinner");
        dinnerTitle.setTextSize(16);
        dinnerTitle.setTypeface(null, Typeface.BOLD);
        dinnerTitle.setPadding(0, 8, 0, 8);
        dinnerItemsContainer.addView(dinnerTitle);

        // Add spinners for dinner components
        addMealComponentSpinner(dinnerItemsContainer, "Protein", "dinnerProtein");
        addMealComponentSpinner(dinnerItemsContainer, "Starch", "dinnerStarch");
        addMealComponentSpinner(dinnerItemsContainer, "Vegetable", "dinnerVegetable");
        addMealComponentSpinner(dinnerItemsContainer, "Dessert", "dinnerDessert");
    }

    // FIXED: Proper implementation of spinner creation with data loading
    private void addMealComponentSpinner(LinearLayout container, String label, String tag) {
        // Label
        TextView labelView = new TextView(this);
        labelView.setText(label + ":");
        labelView.setTextSize(14);
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setPadding(0, 8, 0, 4);
        container.addView(labelView);

        // Spinner
        Spinner spinner = new Spinner(this);
        spinner.setTag(tag);

        // FIXED: Get items for this category based on diet restrictions
        String category = getCategoryFromTag(tag);
        List<Item> items = getFilteredItemsByCategory(category);

        // Create adapter with proper items
        List<String> itemNames = new ArrayList<>();
        itemNames.add("Select " + category.toLowerCase());
        for (Item item : items) {
            itemNames.add(item.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMealCompletion(tag);
                updateSaveButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Store reference for later use
        if ("lunchProtein".equals(tag)) lunchProteinSpinner = spinner;
        else if ("lunchStarch".equals(tag)) lunchStarchSpinner = spinner;
        else if ("lunchVegetable".equals(tag)) lunchVegetableSpinner = spinner;
        else if ("lunchDessert".equals(tag)) lunchDessertSpinner = spinner;
        else if ("dinnerProtein".equals(tag)) dinnerProteinSpinner = spinner;
        else if ("dinnerStarch".equals(tag)) dinnerStarchSpinner = spinner;
        else if ("dinnerVegetable".equals(tag)) dinnerVegetableSpinner = spinner;
        else if ("dinnerDessert".equals(tag)) dinnerDessertSpinner = spinner;

        container.addView(spinner);
    }

    // FIXED: Helper method to get filtered items based on diet type
    private List<Item> getFilteredItemsByCategory(String category) {
        try {
            List<Item> allItems = itemDAO.getItemsByCategory(category);
            List<Item> filteredItems = new ArrayList<>();

            for (Item item : allItems) {
                // Filter based on diet type
                boolean includeItem = true;

                // For ADA diets, only include ADA-friendly items
                if (isAdaDiet || diet.contains("ADA")) {
                    includeItem = item.isAdaFriendly();
                }

                if (includeItem) {
                    filteredItems.add(item);
                }
            }

            return filteredItems;

        } catch (Exception e) {
            Log.e(TAG, "Error loading items for category: " + category, e);
            return new ArrayList<>();
        }
    }

    private String getCategoryFromTag(String tag) {
        if (tag.contains("Protein")) return "Proteins";
        if (tag.contains("Starch")) return "Starches";
        if (tag.contains("Vegetable")) return "Vegetables";
        if (tag.contains("Dessert")) return "Desserts";
        return "Proteins"; // Default
    }

    private void updateMealCompletion(String tag) {
        if (tag.startsWith("lunch")) {
            // Check if at least one lunch item is selected
            lunchComplete = (lunchProteinSpinner != null && lunchProteinSpinner.getSelectedItemPosition() > 0) ||
                    (lunchStarchSpinner != null && lunchStarchSpinner.getSelectedItemPosition() > 0) ||
                    (lunchVegetableSpinner != null && lunchVegetableSpinner.getSelectedItemPosition() > 0) ||
                    (lunchDessertSpinner != null && lunchDessertSpinner.getSelectedItemPosition() > 0);
        } else if (tag.startsWith("dinner")) {
            // Check if at least one dinner item is selected
            dinnerComplete = (dinnerProteinSpinner != null && dinnerProteinSpinner.getSelectedItemPosition() > 0) ||
                    (dinnerStarchSpinner != null && dinnerStarchSpinner.getSelectedItemPosition() > 0) ||
                    (dinnerVegetableSpinner != null && dinnerVegetableSpinner.getSelectedItemPosition() > 0) ||
                    (dinnerDessertSpinner != null && dinnerDessertSpinner.getSelectedItemPosition() > 0);
        }
    }

    private void updateSaveButtonState() {
        boolean canSave = false;

        // Check if at least one meal is complete or NPO
        if (breakfastNPOCheckbox != null && breakfastNPOCheckbox.isChecked()) canSave = true;
        if (lunchNPOCheckbox != null && lunchNPOCheckbox.isChecked()) canSave = true;
        if (dinnerNPOCheckbox != null && dinnerNPOCheckbox.isChecked()) canSave = true;

        // For liquid diets, always allow save
        if (diet != null && (diet.contains("Clear Liquid") || diet.contains("Full Liquid") || diet.contains("Puree"))) {
            canSave = true;
        }

        // Check if regular meals have selections
        if (lunchComplete || dinnerComplete || breakfastComplete) {
            canSave = true;
        }

        if (saveMealPlanButton != null) {
            saveMealPlanButton.setEnabled(canSave);
            saveMealPlanButton.setAlpha(canSave ? 1.0f : 0.5f);
        }
    }

    private void saveMealPlan() {
        try {
            // Update patient meal completion status
            if (patient != null) {
                patient.setBreakfastComplete(breakfastComplete || (breakfastNPOCheckbox != null && breakfastNPOCheckbox.isChecked()));
                patient.setLunchComplete(lunchComplete || (lunchNPOCheckbox != null && lunchNPOCheckbox.isChecked()));
                patient.setDinnerComplete(dinnerComplete || (dinnerNPOCheckbox != null && dinnerNPOCheckbox.isChecked()));

                // Update NPO status
                patient.setBreakfastNPO(breakfastNPOCheckbox != null && breakfastNPOCheckbox.isChecked());
                patient.setLunchNPO(lunchNPOCheckbox != null && lunchNPOCheckbox.isChecked());
                patient.setDinnerNPO(dinnerNPOCheckbox != null && dinnerNPOCheckbox.isChecked());

                // Save to database
                boolean success = patientDAO.updatePatient(patient);
                if (success) {
                    Toast.makeText(this, "Meal plan saved successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error saving meal plan. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving meal plan", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                Intent homeIntent = new Intent(this, MainMenuActivity.class);
                homeIntent.putExtra("current_user", currentUsername);
                homeIntent.putExtra("user_role", currentUserRole);
                homeIntent.putExtra("user_full_name", currentUserFullName);
                startActivity(homeIntent);
                finish();
                return true;
            case R.id.action_save:
                saveMealPlan();
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