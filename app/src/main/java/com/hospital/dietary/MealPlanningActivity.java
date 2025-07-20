package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.Item;
import android.graphics.Typeface;
import java.util.ArrayList;
import java.util.List;

public class MealPlanningActivity extends AppCompatActivity {

    private static final String TAG = "MealPlanningActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private ItemDAO itemDAO;

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
    private boolean isAdaDiet;

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

    // Lunch and Dinner dropdowns
    private Spinner lunchProteinSpinner;
    private Spinner lunchStarchSpinner;
    private Spinner lunchVegetableSpinner;
    private Spinner lunchDessertSpinner;
    private LinearLayout lunchDrinksContainer;
    private Button addLunchDrinkButton;

    private Spinner dinnerProteinSpinner;
    private Spinner dinnerStarchSpinner;
    private Spinner dinnerVegetableSpinner;
    private Spinner dinnerDessertSpinner;
    private LinearLayout dinnerDrinksContainer;
    private Button addDinnerDrinkButton;

    // Drink tracking
    private List<DrinkSelection> lunchDrinks = new ArrayList<>();
    private List<DrinkSelection> dinnerDrinks = new ArrayList<>();
    private int currentFluidML = 0;
    private int fluidLimitML = -1; // -1 means no limit

    // Completion tracking
    private boolean breakfastComplete = false;
    private boolean lunchComplete = false;
    private boolean dinnerComplete = false;

    // Drink selection class
    private static class DrinkSelection {
        String drinkName;
        int quantity;
        int mlPerUnit;

        DrinkSelection(String name, int qty, int ml) {
            this.drinkName = name;
            this.quantity = qty;
            this.mlPerUnit = ml;
        }

        int getTotalML() {
            return quantity * mlPerUnit;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);

        // Get information from intent
        extractIntentData();

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        itemDAO = new ItemDAO(dbHelper);

        // Setup toolbar
        setupToolbar();

        // Initialize UI
        initializeUI();

        // Setup listeners
        setupListeners();

        // Load patient data and meal items
        loadPatientData();
        loadMealItems();

        // Initial UI state
        updateSaveButtonState();
    }

    private void extractIntentData() {
        // Get intent data with null safety
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

        // Provide safe defaults for null values
        if (diet == null) {
            diet = "Regular";
            Log.w(TAG, "Diet was null, defaulting to Regular");
        }

        if (fluidRestriction == null) {
            fluidRestriction = "No Restriction";
        }

        if (textureModifications == null) {
            textureModifications = "";
        }

        // Check if this is an ADA diet (with null safety)
        isAdaDiet = "ADA".equals(diet) || (diet.contains("ADA"));

        // Parse fluid restriction
        if (fluidRestriction != null && !fluidRestriction.equals("No Restriction")) {
            try {
                // Extract number from fluid restriction string
                String[] parts = fluidRestriction.split(" ");
                for (String part : parts) {
                    if (part.matches("\\d+")) {
                        fluidLimitML = Integer.parseInt(part);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not parse fluid restriction: " + fluidRestriction);
                fluidLimitML = -1; // No limit
            }
        }
    }

    private void setupToolbar() {
        // Use default action bar instead of custom toolbar to avoid conflicts
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Planning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeUI() {
        // Patient info
        patientInfoText = findViewById(R.id.patientInfoText);
        if (patientInfoText != null) {
            String patientInfo = String.format("Planning meals for %s\n%s - Room %s\nDiet: %s",
                    patientName != null ? patientName : "Unknown Patient",
                    wing != null ? wing : "Unknown Wing",
                    room != null ? room : "Unknown Room",
                    diet != null ? diet : "Unknown Diet");
            patientInfoText.setText(patientInfo);
        }

        // Action buttons
        saveOrderButton = findViewById(R.id.saveOrderButton);
        backButton = findViewById(R.id.backButton);
        homeButton = findViewById(R.id.homeButton);

        // Meal sections
        breakfastSection = findViewById(R.id.breakfastSection);
        lunchSection = findViewById(R.id.lunchSection);
        dinnerSection = findViewById(R.id.dinnerSection);

        // NPO checkboxes
        breakfastNPOCheckbox = findViewById(R.id.breakfastNPOCheckbox);
        lunchNPOCheckbox = findViewById(R.id.lunchNPOCheckbox);
        dinnerNPOCheckbox = findViewById(R.id.dinnerNPOCheckbox);

        // Meal containers
        breakfastItemsContainer = findViewById(R.id.breakfastItemsContainer);
        lunchItemsContainer = findViewById(R.id.lunchItemsContainer);
        dinnerItemsContainer = findViewById(R.id.dinnerItemsContainer);

        // Initialize lunch dropdowns and drink system
        initializeLunchMealPlanning();
        initializeDinnerMealPlanning();
    }

    private void initializeLunchMealPlanning() {
        if (lunchItemsContainer == null) return;

        lunchItemsContainer.removeAllViews();

        // Add title
        TextView title = new TextView(this);
        title.setText("Lunch Selection");
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 16, 0, 16);
        lunchItemsContainer.addView(title);

        // Protein dropdown
        addCategoryDropdown(lunchItemsContainer, "Protein:", "lunchProtein");

        // Starch dropdown
        addCategoryDropdown(lunchItemsContainer, "Starch:", "lunchStarch");

        // Vegetable dropdown
        addCategoryDropdown(lunchItemsContainer, "Vegetable:", "lunchVegetable");

        // Dessert dropdown
        addCategoryDropdown(lunchItemsContainer, "Dessert:", "lunchDessert");

        // Drinks section
        TextView drinksTitle = new TextView(this);
        drinksTitle.setText("Drinks:");
        drinksTitle.setTextSize(14);
        drinksTitle.setTypeface(null, Typeface.BOLD);
        drinksTitle.setPadding(0, 16, 0, 8);
        lunchItemsContainer.addView(drinksTitle);

        lunchDrinksContainer = new LinearLayout(this);
        lunchDrinksContainer.setOrientation(LinearLayout.VERTICAL);
        lunchItemsContainer.addView(lunchDrinksContainer);

        addLunchDrinkButton = new Button(this);
        addLunchDrinkButton.setText("+ Add Drink");
        addLunchDrinkButton.setOnClickListener(v -> showAddDrinkDialog("lunch"));
        lunchItemsContainer.addView(addLunchDrinkButton);
    }

    private void initializeDinnerMealPlanning() {
        if (dinnerItemsContainer == null) return;

        dinnerItemsContainer.removeAllViews();

        // Add title
        TextView title = new TextView(this);
        title.setText("Dinner Selection");
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 16, 0, 16);
        dinnerItemsContainer.addView(title);

        // Protein dropdown
        addCategoryDropdown(dinnerItemsContainer, "Protein:", "dinnerProtein");

        // Starch dropdown
        addCategoryDropdown(dinnerItemsContainer, "Starch:", "dinnerStarch");

        // Vegetable dropdown
        addCategoryDropdown(dinnerItemsContainer, "Vegetable:", "dinnerVegetable");

        // Dessert dropdown
        addCategoryDropdown(dinnerItemsContainer, "Dessert:", "dinnerDessert");

        // Drinks section
        TextView drinksTitle = new TextView(this);
        drinksTitle.setText("Drinks:");
        drinksTitle.setTextSize(14);
        drinksTitle.setTypeface(null, Typeface.BOLD);
        drinksTitle.setPadding(0, 16, 0, 8);
        dinnerItemsContainer.addView(drinksTitle);

        dinnerDrinksContainer = new LinearLayout(this);
        dinnerDrinksContainer.setOrientation(LinearLayout.VERTICAL);
        dinnerItemsContainer.addView(dinnerDrinksContainer);

        addDinnerDrinkButton = new Button(this);
        addDinnerDrinkButton.setText("+ Add Drink");
        addDinnerDrinkButton.setOnClickListener(v -> showAddDrinkDialog("dinner"));
        dinnerItemsContainer.addView(addDinnerDrinkButton);
    }

    private void addCategoryDropdown(LinearLayout container, String label, String tag) {
        // Validate inputs
        if (container == null) {
            Log.w(TAG, "addCategoryDropdown called with null container");
            return;
        }

        if (tag == null || tag.trim().isEmpty()) {
            Log.w(TAG, "addCategoryDropdown called with null or empty tag");
            return;
        }

        // Label
        TextView labelView = new TextView(this);
        labelView.setText(label != null ? label : "Category:");
        labelView.setTextSize(14);
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setPadding(0, 8, 0, 4);
        container.addView(labelView);

        // Spinner
        Spinner spinner = new Spinner(this);
        spinner.setTag(tag);

        // Get items for this category
        String category = getCategoryFromTag(tag);
        List<Item> items = itemDAO.getItemsByCategory(category);

        // Create adapter
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

    private String getCategoryFromTag(String tag) {
        if (tag.contains("Protein")) return "Protein";
        if (tag.contains("Starch")) return "Starch";
        if (tag.contains("Vegetable")) return "Vegetable";
        if (tag.contains("Dessert")) return "Dessert";
        return "Unknown";
    }

    private void updateMealCompletion(String tag) {
        if (tag.startsWith("lunch")) {
            // Check if lunch is complete
            boolean hasProtein = lunchProteinSpinner != null && lunchProteinSpinner.getSelectedItemPosition() > 0;
            boolean hasStarch = lunchStarchSpinner != null && lunchStarchSpinner.getSelectedItemPosition() > 0;
            lunchComplete = hasProtein || hasStarch; // At least one selection
        } else if (tag.startsWith("dinner")) {
            // Check if dinner is complete
            boolean hasProtein = dinnerProteinSpinner != null && dinnerProteinSpinner.getSelectedItemPosition() > 0;
            boolean hasStarch = dinnerStarchSpinner != null && dinnerStarchSpinner.getSelectedItemPosition() > 0;
            dinnerComplete = hasProtein || hasStarch; // At least one selection
        }
    }

    private void showAddDrinkDialog(String mealType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Drink for " + mealType);

        // Create dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_drink, null);
        builder.setView(dialogView);

        Spinner drinkSpinner = dialogView.findViewById(R.id.drinkSpinner);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);

        // Populate drink options
        List<Item> drinks = itemDAO.getItemsByCategory("Drinks");
        List<String> drinkNames = new ArrayList<>();
        drinkNames.add("Select drink");
        for (Item drink : drinks) {
            drinkNames.add(drink.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drinkSpinner.setAdapter(adapter);

        builder.setPositiveButton("Add", (dialog, which) -> {
            int selectedPosition = drinkSpinner.getSelectedItemPosition();
            if (selectedPosition > 0) {
                String drinkName = drinkNames.get(selectedPosition);
                String quantityText = quantityInput.getText().toString();

                try {
                    int quantity = Integer.parseInt(quantityText);
                    if (quantity > 0) {
                        // Find drink item to get ml value
                        Item selectedDrink = drinks.get(selectedPosition - 1);
                        DrinkSelection selection = new DrinkSelection(drinkName, quantity, selectedDrink.getSizeML());

                        if ("lunch".equals(mealType)) {
                            lunchDrinks.add(selection);
                            updateDrinkDisplay(lunchDrinksContainer, lunchDrinks);
                        } else {
                            dinnerDrinks.add(selection);
                            updateDrinkDisplay(dinnerDrinksContainer, dinnerDrinks);
                        }

                        updateFluidTotal();
                        updateSaveButtonState();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateDrinkDisplay(LinearLayout container, List<DrinkSelection> drinks) {
        container.removeAllViews();

        for (int i = 0; i < drinks.size(); i++) {
            DrinkSelection drink = drinks.get(i);

            LinearLayout drinkRow = new LinearLayout(this);
            drinkRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView drinkText = new TextView(this);
            drinkText.setText(String.format("%s x%d (%dml)",
                    drink.drinkName, drink.quantity, drink.getTotalML()));
            drinkText.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Button removeButton = new Button(this);
            removeButton.setText("Remove");
            removeButton.setTextSize(12);
            final int index = i;
            removeButton.setOnClickListener(v -> {
                drinks.remove(index);
                updateDrinkDisplay(container, drinks);
                updateFluidTotal();
                updateSaveButtonState();
            });

            drinkRow.addView(drinkText);
            drinkRow.addView(removeButton);
            container.addView(drinkRow);
        }
    }

    private void updateFluidTotal() {
        currentFluidML = 0;
        for (DrinkSelection drink : lunchDrinks) {
            currentFluidML += drink.getTotalML();
        }
        for (DrinkSelection drink : dinnerDrinks) {
            currentFluidML += drink.getTotalML();
        }

        // Update UI if there's a fluid display
        if (fluidLimitML > 0) {
            // Could add fluid total display here
            Log.d(TAG, String.format("Current fluid: %dml / %dml", currentFluidML, fluidLimitML));
        }
    }

    private void setupListeners() {
        // Action buttons
        if (saveOrderButton != null) {
            saveOrderButton.setOnClickListener(v -> saveOrderAndFinish());
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }

        // NPO checkboxes
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
            Patient patient = patientDAO.getPatientById(patientId);
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
        if (diet != null && (diet.equals("Clear Liquid") || diet.equals("Full Liquid") || diet.equals("Puree"))) {
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
        title.setText(mealType + " - " + diet + (isAdaDiet ? " (ADA)" : ""));
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 8, 0, 8);
        container.addView(title);

        // Get specific items based on diet type and meal
        String itemsList = getPredeterminedItemsForMeal(diet, mealType, isAdaDiet);

        TextView itemsText = new TextView(this);
        itemsText.setText(itemsList);
        itemsText.setPadding(16, 8, 16, 8);
        itemsText.setTextSize(14);
        container.addView(itemsText);
    }

    private String getPredeterminedItemsForMeal(String dietType, String mealType, boolean isADA) {
        StringBuilder items = new StringBuilder();

        if ("Clear Liquid".equals(dietType)) {
            // Existing Clear Liquid logic
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

        } else if ("Full Liquid".equals(dietType)) {
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

        } else if ("Puree".equals(dietType)) {
            // Keep existing puree logic or add specific items if needed
            items.append("Predetermined items for ").append(dietType).append(" diet");
        }

        return items.toString();
    }

    private void loadRegularMealItems() {
        // Regular diet - load dropdowns as already implemented
        // Dropdowns are already initialized in initializeUI()
    }

    private void updateSaveButtonState() {
        boolean canSave = false;

        // Check if at least one meal is complete or NPO
        if (breakfastNPOCheckbox != null && breakfastNPOCheckbox.isChecked()) canSave = true;
        if (lunchNPOCheckbox != null && lunchNPOCheckbox.isChecked()) canSave = true;
        if (dinnerNPOCheckbox != null && dinnerNPOCheckbox.isChecked()) canSave = true;

        // For liquid diets, always allow save
        if (diet != null && (diet.equals("Clear Liquid") || diet.equals("Full Liquid") || diet.equals("Puree"))) {
            canSave = true;
        }

        // Check if regular meals have selections
        if (lunchProteinSpinner != null && lunchProteinSpinner.getSelectedItemPosition() > 0) canSave = true;
        if (dinnerProteinSpinner != null && dinnerProteinSpinner.getSelectedItemPosition() > 0) canSave = true;

        if (saveOrderButton != null) {
            saveOrderButton.setEnabled(canSave);
        }
    }

    private void saveOrderAndFinish() {
        try {
            // Create updated patient object
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient == null) {
                Toast.makeText(this, "Error: Patient not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update NPO states
            patient.setBreakfastNPO(breakfastNPOCheckbox != null && breakfastNPOCheckbox.isChecked());
            patient.setLunchNPO(lunchNPOCheckbox != null && lunchNPOCheckbox.isChecked());
            patient.setDinnerNPO(dinnerNPOCheckbox != null && dinnerNPOCheckbox.isChecked());

            // Update completion states
            patient.setBreakfastComplete(breakfastComplete || (breakfastNPOCheckbox != null && breakfastNPOCheckbox.isChecked()));
            patient.setLunchComplete(lunchComplete || (lunchNPOCheckbox != null && lunchNPOCheckbox.isChecked()));
            patient.setDinnerComplete(dinnerComplete || (dinnerNPOCheckbox != null && dinnerNPOCheckbox.isChecked()));

            // Save patient updates
            boolean result = patientDAO.updatePatient(patient);

            if (result) {
                Toast.makeText(this, "Meal plan saved successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error saving meal plan", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving meal plan: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error saving meal plan: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}