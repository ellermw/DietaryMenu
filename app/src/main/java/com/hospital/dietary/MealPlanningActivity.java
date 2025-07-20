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

    // FIXED: Updated setupToolbar to use default action bar
    private void setupToolbar() {
        // Use default action bar instead of custom toolbar to avoid conflicts
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Planning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Alternative approach if you want to use custom toolbar:
        /*
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Meal Planning");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            // Fallback to default action bar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Meal Planning");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        */
    }

    private void initializeUI() {
        // Patient info
        patientInfoText = findViewById(R.id.patientInfoText);
        if (patientInfoText != null) {
            patientInfoText.setText(String.format("Planning meals for %s\n%s - Room %s\nDiet: %s",
                    patientName, wing, room, diet));
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
        // Label
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(14);
        labelView.setPadding(0, 8, 0, 4);
        container.addView(labelView);

        // Spinner
        Spinner spinner = new Spinner(this);
        spinner.setTag(tag);
        container.addView(spinner);

        // Populate spinner and store reference
        String category = getCategoryFromTag(tag);
        populateCategorySpinner(spinner, category);
        storeSpinnerReference(spinner, tag);

        // Add listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSaveButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void storeSpinnerReference(Spinner spinner, String tag) {
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

                            DrinkSelection drink = new DrinkSelection(drinkName, quantity, mlPerUnit);

                            if ("lunch".equals(mealType)) {
                                lunchDrinks.add(drink);
                                updateDrinkDisplay(lunchDrinksContainer, lunchDrinks);
                            } else {
                                dinnerDrinks.add(drink);
                                updateDrinkDisplay(dinnerDrinksContainer, dinnerDrinks);
                            }

                            updateFluidTracking();
                            updateSaveButtonState();

                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Please select a drink and enter quantity", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDrinkDisplay(LinearLayout container, List<DrinkSelection> drinks) {
        container.removeAllViews();

        for (int i = 0; i < drinks.size(); i++) {
            DrinkSelection drink = drinks.get(i);

            LinearLayout drinkRow = new LinearLayout(this);
            drinkRow.setOrientation(LinearLayout.HORIZONTAL);
            drinkRow.setPadding(0, 4, 0, 4);

            TextView drinkText = new TextView(this);
            drinkText.setText(drink.quantity + "x " + drink.drinkName + " (" + drink.getTotalML() + "ml)");
            drinkText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            Button removeButton = new Button(this);
            removeButton.setText("Remove");
            removeButton.setTextSize(12);
            final int index = i;
            removeButton.setOnClickListener(v -> {
                drinks.remove(index);
                updateDrinkDisplay(container, drinks);
                updateFluidTracking();
                updateSaveButtonState();
            });

            drinkRow.addView(drinkText);
            drinkRow.addView(removeButton);
            container.addView(drinkRow);
        }
    }

    private void updateFluidTracking() {
        currentFluidML = 0;

        for (DrinkSelection drink : lunchDrinks) {
            currentFluidML += drink.getTotalML();
        }
        for (DrinkSelection drink : dinnerDrinks) {
            currentFluidML += drink.getTotalML();
        }

        // Update UI to show fluid status if needed
        // You can add a TextView to show current fluid intake vs limit
    }

    private void setupListeners() {
        if (saveOrderButton != null) {
            saveOrderButton.setOnClickListener(v -> saveOrderAndFinish());
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }

        // NPO checkbox listeners
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
            e.printStackTrace();
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

        TextView itemsText = new TextView(this);
        String items = "Predetermined items for " + diet + " diet";
        itemsText.setText(items);
        itemsText.setPadding(16, 8, 16, 8);
        container.addView(itemsText);
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
        if (diet.equals("Clear Liquid") || diet.equals("Full Liquid") || diet.equals("Puree")) {
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