package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

        // Check if this is an ADA diet
        isAdaDiet = "ADA".equals(diet) || (diet != null && diet.contains("ADA"));

        // Parse fluid restriction
        if (fluidRestriction != null && !fluidRestriction.equals("No Restriction")) {
            try {
                fluidLimitML = Integer.parseInt(fluidRestriction.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                fluidLimitML = -1;
            }
        }
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
        // Patient info
        patientInfoText = findViewById(R.id.patientInfoText);
        patientInfoText.setText(String.format("Planning meals for %s\n%s - Room %s\nDiet: %s",
                patientName, wing, room, diet));

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
        // Label
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(14);
        labelView.setPadding(0, 8, 0, 4);
        container.addView(labelView);

        // Spinner
        Spinner spinner = new Spinner(this);
        spinner.setTag(tag);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        spinner.setLayoutParams(params);

        // Populate spinner based on category
        String category = getCategoryFromTag(tag);
        populateCategorySpinner(spinner, category);

        // Add selection listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMealCompletion();
                updateFluidDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        container.addView(spinner);

        // Store reference for easy access
        if (tag.equals("lunchProtein")) lunchProteinSpinner = spinner;
        else if (tag.equals("lunchStarch")) lunchStarchSpinner = spinner;
        else if (tag.equals("lunchVegetable")) lunchVegetableSpinner = spinner;
        else if (tag.equals("lunchDessert")) lunchDessertSpinner = spinner;
        else if (tag.equals("dinnerProtein")) dinnerProteinSpinner = spinner;
        else if (tag.equals("dinnerStarch")) dinnerStarchSpinner = spinner;
        else if (tag.equals("dinnerVegetable")) dinnerVegetableSpinner = spinner;
        else if (tag.equals("dinnerDessert")) dinnerDessertSpinner = spinner;
    }

    private String getCategoryFromTag(String tag) {
        if (tag.contains("Protein")) return "Proteins";
        if (tag.contains("Starch")) return "Starches";
        if (tag.contains("Vegetable")) return "Vegetables";
        if (tag.contains("Dessert")) return "Desserts";
        return "Other";
    }

    private void populateCategorySpinner(Spinner spinner, String category) {
        List<String> items = new ArrayList<>();
        items.add("Select " + category.substring(0, category.length() - 1)); // Remove 's' from category

        try {
            List<Item> categoryItems;
            if (isAdaDiet) {
                categoryItems = itemDAO.getAdaItemsByCategory(category);
            } else {
                categoryItems = itemDAO.getItemsByCategory(category);
            }

            for (Item item : categoryItems) {
                items.add(item.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showAddDrinkDialog(String mealType) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_drink, null);

        Spinner drinkSpinner = dialogView.findViewById(R.id.drinkSpinner);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        TextView mlInfoText = dialogView.findViewById(R.id.mlInfoText);

        // Populate drink spinner
        List<Item> beverages = isAdaDiet ? itemDAO.getAdaItemsByCategory("Beverages") : itemDAO.getItemsByCategory("Beverages");
        List<Item> juices = isAdaDiet ? itemDAO.getAdaItemsByCategory("Juices") : itemDAO.getItemsByCategory("Juices");

        List<String> drinkOptions = new ArrayList<>();
        drinkOptions.add("Select Drink");

        for (Item item : beverages) {
            drinkOptions.add(item.getName() + " (240ml)");
        }
        for (Item item : juices) {
            drinkOptions.add(item.getName() + " (180ml)");
        }

        ArrayAdapter<String> drinkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkOptions);
        drinkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drinkSpinner.setAdapter(drinkAdapter);

        // Update ML info when drink is selected
        drinkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedDrink = drinkOptions.get(position);
                    int mlPerUnit = selectedDrink.contains("(240ml)") ? 240 : 180;
                    mlInfoText.setText("ML per unit: " + mlPerUnit);
                    mlInfoText.setVisibility(View.VISIBLE);
                } else {
                    mlInfoText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Add " + mealType.substring(0, 1).toUpperCase() + mealType.substring(1) + " Drink")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    int drinkPosition = drinkSpinner.getSelectedItemPosition();
                    String quantityStr = quantityInput.getText().toString().trim();

                    if (drinkPosition > 0 && !quantityStr.isEmpty()) {
                        try {
                            int quantity = Integer.parseInt(quantityStr);
                            String drinkName = drinkOptions.get(drinkPosition);
                            int mlPerUnit = drinkName.contains("(240ml)") ? 240 : 180;

                            DrinkSelection drink = new DrinkSelection(
                                    drinkName.replaceAll(" \\(\\d+ml\\)", ""),
                                    quantity,
                                    mlPerUnit);

                            if ("lunch".equals(mealType)) {
                                lunchDrinks.add(drink);
                                updateDrinkDisplay(lunchDrinksContainer, lunchDrinks, "lunch");
                            } else {
                                dinnerDrinks.add(drink);
                                updateDrinkDisplay(dinnerDrinksContainer, dinnerDrinks, "dinner");
                            }

                            updateMealCompletion();
                            updateFluidDisplay();
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDrinkDisplay(LinearLayout container, List<DrinkSelection> drinks, String mealType) {
        container.removeAllViews();

        for (int i = 0; i < drinks.size(); i++) {
            DrinkSelection drink = drinks.get(i);

            LinearLayout drinkRow = new LinearLayout(this);
            drinkRow.setOrientation(LinearLayout.HORIZONTAL);
            drinkRow.setPadding(16, 8, 16, 8);

            TextView drinkText = new TextView(this);
            drinkText.setText(String.format("%s (x%d) - %dml", drink.drinkName, drink.quantity, drink.getTotalML()));
            drinkText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            Button removeButton = new Button(this);
            removeButton.setText("Remove");
            removeButton.setTextSize(12);
            final int position = i;
            removeButton.setOnClickListener(v -> {
                drinks.remove(position);
                updateDrinkDisplay(container, drinks, mealType);
                updateMealCompletion();
                updateFluidDisplay();
            });

            drinkRow.addView(drinkText);
            drinkRow.addView(removeButton);
            container.addView(drinkRow);
        }
    }

    private void updateFluidDisplay() {
        currentFluidML = 0;
        for (DrinkSelection drink : lunchDrinks) {
            currentFluidML += drink.getTotalML();
        }
        for (DrinkSelection drink : dinnerDrinks) {
            currentFluidML += drink.getTotalML();
        }

        if (fluidLimitML > 0) {
            String fluidInfo = String.format("Fluid intake: %dml / %dml", currentFluidML, fluidLimitML);
            if (currentFluidML > fluidLimitML) {
                fluidInfo += " (EXCEEDED)";
                // You could add warning styling here
            }
            // Update fluid display in UI if you have a TextView for it
        }
    }

    private void setupListeners() {
        // Save button
        saveOrderButton.setOnClickListener(v -> saveOrder());

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Home button
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // NPO checkbox listeners
        breakfastNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                breakfastItemsContainer.setVisibility(View.GONE);
                breakfastComplete = true;
            } else {
                breakfastItemsContainer.setVisibility(View.VISIBLE);
                breakfastComplete = false; // Will be updated by meal items
            }
            updateSaveButtonState();
        });

        lunchNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                lunchItemsContainer.setVisibility(View.GONE);
                lunchComplete = true;
            } else {
                lunchItemsContainer.setVisibility(View.VISIBLE);
                updateMealCompletion();
            }
            updateSaveButtonState();
        });

        dinnerNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dinnerItemsContainer.setVisibility(View.GONE);
                dinnerComplete = true;
            } else {
                dinnerItemsContainer.setVisibility(View.VISIBLE);
                updateMealCompletion();
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
        // Special handling for liquid diets
        if (diet.equals("Clear Liquid") || diet.equals("Full Liquid") || diet.equals("Puree")) {
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
        breakfastNPOCheckbox.setVisibility(View.GONE);
        lunchNPOCheckbox.setVisibility(View.GONE);
        dinnerNPOCheckbox.setVisibility(View.GONE);

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
        container.removeAllViews();

        TextView title = new TextView(this);
        title.setText(mealType + " - " + diet + (isAdaDiet ? " (ADA)" : "") + " (Predetermined)");
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 16, 0, 8);
        container.addView(title);

        // Show liquid diet items
        String[] items;
        if (isAdaDiet) {
            items = new String[]{
                    "• Sugar-free clear broth",
                    "• Apple juice (ADA)",
                    "• Sugar-free jello",
                    "• Water",
                    "• Tea/Coffee (no sugar)"
            };
        } else {
            items = new String[]{
                    "• Clear broth",
                    "• Apple or orange juice",
                    "• Jello",
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
        // Breakfast stays as checkboxes (as requested)
        addBreakfastCheckboxItems();
    }

    private void addBreakfastCheckboxItems() {
        breakfastItemsContainer.removeAllViews();

        TextView title = new TextView(this);
        title.setText("Breakfast Items");
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 16, 0, 8);
        breakfastItemsContainer.addView(title);

        // Get breakfast items
        List<Item> breakfastItems;
        if (isAdaDiet) {
            breakfastItems = itemDAO.getAdaItemsByCategory("Breakfast Items");
        } else {
            breakfastItems = itemDAO.getItemsByCategory("Breakfast Items");
        }

        for (Item item : breakfastItems) {
            CheckBox itemCheckbox = new CheckBox(this);
            itemCheckbox.setText(item.getName());
            itemCheckbox.setPadding(16, 8, 0, 8);

            itemCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateMealCompletion();
            });

            breakfastItemsContainer.addView(itemCheckbox);
        }
    }

    private void updateMealCompletion() {
        // Check breakfast completion (checkboxes)
        if (!breakfastNPOCheckbox.isChecked()) {
            breakfastComplete = checkBreakfastItemsSelected();
        }

        // Check lunch completion (dropdowns + drinks)
        if (!lunchNPOCheckbox.isChecked()) {
            lunchComplete = checkDropdownMealComplete("lunch");
        }

        // Check dinner completion (dropdowns + drinks)
        if (!dinnerNPOCheckbox.isChecked()) {
            dinnerComplete = checkDropdownMealComplete("dinner");
        }

        updateSaveButtonState();
    }

    private boolean checkBreakfastItemsSelected() {
        for (int i = 0; i < breakfastItemsContainer.getChildCount(); i++) {
            View child = breakfastItemsContainer.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkbox = (CheckBox) child;
                if (checkbox.isChecked()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDropdownMealComplete(String mealType) {
        if ("lunch".equals(mealType)) {
            return lunchProteinSpinner.getSelectedItemPosition() > 0 &&
                    lunchStarchSpinner.getSelectedItemPosition() > 0 &&
                    lunchVegetableSpinner.getSelectedItemPosition() > 0 &&
                    lunchDessertSpinner.getSelectedItemPosition() > 0;
        } else {
            return dinnerProteinSpinner.getSelectedItemPosition() > 0 &&
                    dinnerStarchSpinner.getSelectedItemPosition() > 0 &&
                    dinnerVegetableSpinner.getSelectedItemPosition() > 0 &&
                    dinnerDessertSpinner.getSelectedItemPosition() > 0;
        }
    }

    private void updateSaveButtonState() {
        boolean allMealsComplete = breakfastComplete && lunchComplete && dinnerComplete;
        saveOrderButton.setEnabled(allMealsComplete);

        if (allMealsComplete) {
            if (fluidLimitML > 0 && currentFluidML > fluidLimitML) {
                saveOrderButton.setText("Save Order (Fluid Limit Exceeded)");
            } else {
                saveOrderButton.setText("Save Complete Order");
            }
        } else {
            saveOrderButton.setText("Complete All Meals First");
        }
    }

    private void saveOrder() {
        if (patientId != -1) {
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                // Update completion status
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
                    if (diet.equals("Clear Liquid") || diet.equals("Full Liquid") || diet.equals("Puree")) {
                        message += "\n\n" + diet + " order completed with predetermined menu items.";
                    }
                    if (fluidLimitML > 0 && currentFluidML > fluidLimitML) {
                        message += "\n\nWARNING: Fluid limit exceeded (" + currentFluidML + "ml / " + fluidLimitML + "ml)";
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    finish();
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