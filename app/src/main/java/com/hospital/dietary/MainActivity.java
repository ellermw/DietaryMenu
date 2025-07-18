package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.models.Item;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    private boolean isAdmin = false;
    
    // Patient Information - FIXED: Split patient name
    private EditText patientFirstNameInput;
    private EditText patientLastNameInput;
    private Spinner wingSpinner;
    private Spinner roomSpinner;
    private Spinner dietSpinner;
    private Spinner fluidRestrictionSpinner;
    private CheckBox mechanicalGroundCB;
    private CheckBox mechanicalChoppedCB;
    private CheckBox biteSizeCB;
    private CheckBox breadOKCB;
    
    // Breakfast Items
    private Spinner breakfastColdCereal;
    private Spinner breakfastHotCereal;
    private Spinner breakfastBread;
    private Spinner breakfastMuffin;
    private Spinner breakfastMain;
    private Spinner breakfastFruit;
    
    // Lunch Items
    private Spinner lunchProtein;
    private Spinner lunchStarch;
    private Spinner lunchVegetable;
    private Spinner lunchDessert;
    
    // Dinner Items
    private Spinner dinnerProtein;
    private Spinner dinnerStarch;
    private Spinner dinnerVegetable;
    private Spinner dinnerDessert;
    
    // Drink containers
    private LinearLayout breakfastJuicesContainer;
    private LinearLayout breakfastDrinksContainer;
    private LinearLayout lunchDrinksContainer;
    private LinearLayout dinnerDrinksContainer;
    
    // Buttons
    private Button addBreakfastJuiceButton;
    private Button addBreakfastDrinkButton;
    private Button addLunchDrinkButton;
    private Button addDinnerDrinkButton;
    private Button submitOrderButton;
    private Button clearFormButton;
    private Button backButton;
    
    // Fluid tracking
    private TextView breakfastFluidDisplay;
    private TextView lunchFluidDisplay;
    private TextView dinnerFluidDisplay;
    private Map<String, Integer> fluidUsed = new HashMap<>();
    private Map<String, Integer> fluidLimits = new HashMap<>();
    
    // Data arrays - FIXED: Updated with correct values
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "ADA", "Cardiac", "Renal", 
                                              "Puree", "Full Liquid", "Clear Liquid");
    private List<String> fluidRestrictions = Arrays.asList("None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        isAdmin = "admin".equalsIgnoreCase(currentUserRole);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        
        // Load data
        loadSpinnerData();
        
        // Setup listeners
        setupListeners();
        
        // Initialize fluid tracking
        initializeFluidTracking();
    }
    
    private void initializeUI() {
        // FIXED: Patient information with split names
        patientFirstNameInput = findViewById(R.id.patientFirstNameInput);
        patientLastNameInput = findViewById(R.id.patientLastNameInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomSpinner = findViewById(R.id.roomSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        
        // Breakfast items
        breakfastColdCereal = findViewById(R.id.breakfastColdCereal);
        breakfastHotCereal = findViewById(R.id.breakfastHotCereal);
        breakfastBread = findViewById(R.id.breakfastBread);
        breakfastMuffin = findViewById(R.id.breakfastMuffin);
        breakfastMain = findViewById(R.id.breakfastMain);
        breakfastFruit = findViewById(R.id.breakfastFruit);
        
        // Lunch items
        lunchProtein = findViewById(R.id.lunchProtein);
        lunchStarch = findViewById(R.id.lunchStarch);
        lunchVegetable = findViewById(R.id.lunchVegetable);
        lunchDessert = findViewById(R.id.lunchDessert);
        
        // Dinner items
        dinnerProtein = findViewById(R.id.dinnerProtein);
        dinnerStarch = findViewById(R.id.dinnerStarch);
        dinnerVegetable = findViewById(R.id.dinnerVegetable);
        dinnerDessert = findViewById(R.id.dinnerDessert);
        
        // Drink containers
        breakfastJuicesContainer = findViewById(R.id.breakfastJuicesContainer);
        breakfastDrinksContainer = findViewById(R.id.breakfastDrinksContainer);
        lunchDrinksContainer = findViewById(R.id.lunchDrinksContainer);
        dinnerDrinksContainer = findViewById(R.id.dinnerDrinksContainer);
        
        // Buttons
        addBreakfastJuiceButton = findViewById(R.id.addBreakfastJuiceButton);
        addBreakfastDrinkButton = findViewById(R.id.addBreakfastDrinkButton);
        addLunchDrinkButton = findViewById(R.id.addLunchDrinkButton);
        addDinnerDrinkButton = findViewById(R.id.addDinnerDrinkButton);
        submitOrderButton = findViewById(R.id.submitOrderButton);
        clearFormButton = findViewById(R.id.clearFormButton);
        backButton = findViewById(R.id.backButton);
        
        // Fluid displays
        breakfastFluidDisplay = findViewById(R.id.breakfastFluidDisplay);
        lunchFluidDisplay = findViewById(R.id.lunchFluidDisplay);
        dinnerFluidDisplay = findViewById(R.id.dinnerFluidDisplay);
    }
    
    private void loadSpinnerData() {
        loadWings();
        loadDiets();
        loadFluidRestrictions();
        loadMenuItems();
    }
    
    private void loadWings() {
        List<String> wingList = new ArrayList<>(wings);
        wingList.add(0, "Select Wing");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wingList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(adapter);
    }
    
    private void updateRoomSpinner(String wing) {
        List<String> rooms = new ArrayList<>();
        
        if (!wing.equals("Select Wing")) {
            switch (wing) {
                case "1 South":
                    for (int i = 106; i <= 120; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "2 North":
                    for (int i = 250; i <= 264; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "Labor and Delivery":
                    for (int i = 1; i <= 6; i++) {
                        rooms.add("L&D" + (i - 1));
                    }
                    break;
                case "2 West":
                    for (int i = 225; i <= 248; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "3 North":
                    for (int i = 349; i <= 371; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "ICU":
                    for (int i = 1; i <= 13; i++) {
                        rooms.add("ICU" + i);
                    }
                    break;
            }
        }
        
        rooms.add(0, "Select Room");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomSpinner.setAdapter(adapter);
    }
    
    private void loadDiets() {
        List<String> dietList = new ArrayList<>(diets);
        dietList.add(0, "Select Diet");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(adapter);
    }
    
    // FIXED: Load only the 3 specified fluid restriction values
    private void loadFluidRestrictions() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(adapter);
    }
    
    private void loadMenuItems() {
        // Load all menu spinners based on diet selection
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        
        if (selectedDiet != null && selectedDiet.equals("Clear Liquid")) {
            setClearLiquidSpinners();
        } else {
            loadSpinnerItems(breakfastColdCereal, "Cold Cereals", true);
            loadSpinnerItems(breakfastHotCereal, "Hot Cereals", true);
            loadSpinnerItems(breakfastBread, "Breads", true);
            loadSpinnerItems(breakfastMuffin, "Fresh Muffins", true);
            loadSpinnerItems(breakfastMain, "Breakfast", true);
            loadSpinnerItems(breakfastFruit, "Fruits", true);
            loadSpinnerItems(lunchProtein, "Protein/Entrée", true);
            loadSpinnerItems(lunchStarch, "Starch", true);
            loadSpinnerItems(lunchVegetable, "Vegetable", true);
            loadSpinnerItems(lunchDessert, "Dessert", true);
            loadSpinnerItems(dinnerProtein, "Protein/Entrée", true);
            loadSpinnerItems(dinnerStarch, "Starch", true);
            loadSpinnerItems(dinnerVegetable, "Vegetable", true);
            loadSpinnerItems(dinnerDessert, "Dessert", true);
        }
    }
    
    // FIXED: Set all food spinners to "None" only for Clear Liquid diet
    private void setClearLiquidSpinners() {
        List<String> noneOnly = Arrays.asList("None");
        
        ArrayAdapter<String> noneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, noneOnly);
        noneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        breakfastColdCereal.setAdapter(noneAdapter);
        breakfastColdCereal.setSelection(0);
        breakfastColdCereal.setEnabled(false);
        
        breakfastHotCereal.setAdapter(noneAdapter);
        breakfastHotCereal.setSelection(0);
        breakfastHotCereal.setEnabled(false);
        
        breakfastBread.setAdapter(noneAdapter);
        breakfastBread.setSelection(0);
        breakfastBread.setEnabled(false);
        
        breakfastMuffin.setAdapter(noneAdapter);
        breakfastMuffin.setSelection(0);
        breakfastMuffin.setEnabled(false);
        
        breakfastMain.setAdapter(noneAdapter);
        breakfastMain.setSelection(0);
        breakfastMain.setEnabled(false);
        
        breakfastFruit.setAdapter(noneAdapter);
        breakfastFruit.setSelection(0);
        breakfastFruit.setEnabled(false);
        
        // Lunch food items - all set to "None" only
        lunchProtein.setAdapter(noneAdapter);
        lunchProtein.setSelection(0);
        lunchProtein.setEnabled(false);
        
        lunchStarch.setAdapter(noneAdapter);
        lunchStarch.setSelection(0);
        lunchStarch.setEnabled(false);
        
        lunchVegetable.setAdapter(noneAdapter);
        lunchVegetable.setSelection(0);
        lunchVegetable.setEnabled(false);
        
        lunchDessert.setAdapter(noneAdapter);
        lunchDessert.setSelection(0);
        lunchDessert.setEnabled(false);
        
        // Dinner food items - all set to "None" only
        dinnerProtein.setAdapter(noneAdapter);
        dinnerProtein.setSelection(0);
        dinnerProtein.setEnabled(false);
        
        dinnerStarch.setAdapter(noneAdapter);
        dinnerStarch.setSelection(0);
        dinnerStarch.setEnabled(false);
        
        dinnerVegetable.setAdapter(noneAdapter);
        dinnerVegetable.setSelection(0);
        dinnerVegetable.setEnabled(false);
        
        dinnerDessert.setAdapter(noneAdapter);
        dinnerDessert.setSelection(0);
        dinnerDessert.setEnabled(false);
    }
    
    private void loadSpinnerItems(Spinner spinner, String category, boolean includeNone) {
        List<Item> items = itemDAO.getItemsByCategory(category);
        List<String> itemNames = new ArrayList<>();
        
        if (includeNone) {
            itemNames.add("None");
        }
        
        for (Item item : items) {
            itemNames.add(item.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    private void setupListeners() {
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = (String) parent.getItemAtPosition(position);
                updateRoomSpinner(selectedWing);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getItemAtPosition(position);
                handleDietSelection(selectedDiet);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        fluidRestrictionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFluidLimits();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        addBreakfastJuiceButton.setOnClickListener(v -> addDrinkItem("breakfast", "Juices", breakfastJuicesContainer));
        addBreakfastDrinkButton.setOnClickListener(v -> addDrinkItem("breakfast", "Drink", breakfastDrinksContainer));
        addLunchDrinkButton.setOnClickListener(v -> addDrinkItem("lunch", "Drink", lunchDrinksContainer));
        addDinnerDrinkButton.setOnClickListener(v -> addDrinkItem("dinner", "Drink", dinnerDrinksContainer));
        
        submitOrderButton.setOnClickListener(v -> confirmOrder());
        clearFormButton.setOnClickListener(v -> clearForm());
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
            finish();
        });
    }
    
    private void handleDietSelection(String diet) {
        if (diet != null) {
            loadMenuItems();
            applyDefaults(diet);
        }
    }
    
    // FIXED: Apply defaults including Clear Liquid
    private void applyDefaults(String diet) {
        if (!isAdmin) {
            return;
        }
        
        if (diet.equals("Clear Liquid")) {
            applyClearLiquidDefaults();
        }
    }
    
    // FIXED: Apply Clear Liquid defaults
    private void applyClearLiquidDefaults() {
        // Clear all existing drinks first
        breakfastJuicesContainer.removeAllViews();
        breakfastDrinksContainer.removeAllViews();
        lunchDrinksContainer.removeAllViews();
        dinnerDrinksContainer.removeAllViews();
        
        // Reset fluid tracking
        fluidUsed.put("breakfast", 0);
        fluidUsed.put("lunch", 0);
        fluidUsed.put("dinner", 0);
        
        // Apply Clear Liquid defaults for breakfast
        addClearLiquidBreakfastDefaults();
        
        // Apply Clear Liquid defaults for lunch  
        addClearLiquidLunchDefaults();
        
        // Apply Clear Liquid defaults for dinner
        addClearLiquidDinnerDefaults();
        
        updateFluidDisplay();
    }
    
    // Clear Liquid breakfast defaults
    private void addClearLiquidBreakfastDefaults() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = selectedDiet != null && selectedDiet.contains("ADA");
        
        addDefaultDrinkItem("breakfast", "Coffee", 200, breakfastDrinksContainer, false);
        addDefaultDrinkItem("breakfast", "Chicken Broth", 200, breakfastDrinksContainer, false);
        
        String juiceChoice = isADA ? "Apple Juice" : "Orange Juice";
        addDefaultDrinkItem("breakfast", juiceChoice, 120, breakfastJuicesContainer, false);
        
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addDefaultDrinkItem("breakfast", jelloChoice, 0, breakfastDrinksContainer, false);
        
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addDefaultDrinkItem("breakfast", spriteChoice, 240, breakfastDrinksContainer, false);
    }
    
    // Clear Liquid lunch defaults
    private void addClearLiquidLunchDefaults() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = selectedDiet != null && selectedDiet.contains("ADA");
        
        addDefaultDrinkItem("lunch", "Beef Broth", 200, lunchDrinksContainer, false);
        addDefaultDrinkItem("lunch", "Ice Tea", 200, lunchDrinksContainer, false);
        
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addDefaultDrinkItem("lunch", jelloChoice, 0, lunchDrinksContainer, false);
        
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addDefaultDrinkItem("lunch", spriteChoice, 240, lunchDrinksContainer, false);
    }
    
    // Clear Liquid dinner defaults
    private void addClearLiquidDinnerDefaults() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = selectedDiet != null && selectedDiet.contains("ADA");
        
        addDefaultDrinkItem("dinner", "Chicken Broth", 200, dinnerDrinksContainer, false);
        
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addDefaultDrinkItem("dinner", jelloChoice, 0, dinnerDrinksContainer, false);
        
        addDefaultDrinkItem("dinner", "Ice Tea", 200, dinnerDrinksContainer, false);
        
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addDefaultDrinkItem("dinner", spriteChoice, 240, dinnerDrinksContainer, false);
    }
    
    // Helper method for adding default drink items
    private void addDefaultDrinkItem(String meal, String itemName, int volumeML, LinearLayout container, boolean editable) {
        View drinkItemLayout = getLayoutInflater().inflate(R.layout.drink_item, container, false);
        TextView itemNameView = drinkItemLayout.findViewById(R.id.drinkItemName);
        TextView quantityView = drinkItemLayout.findViewById(R.id.drinkQuantity);
        TextView fluidMLView = drinkItemLayout.findViewById(R.id.drinkFluidML);
        Button removeButton = drinkItemLayout.findViewById(R.id.removeDrinkButton);
        
        itemNameView.setText(itemName);
        quantityView.setText("1");
        if (volumeML > 0) {
            fluidMLView.setText(volumeML + " ml");
        } else {
            fluidMLView.setText("0 ml");
        }
        
        if (!editable) {
            removeButton.setVisibility(View.GONE);
            drinkItemLayout.setBackgroundResource(android.R.drawable.edit_text);
        } else {
            removeButton.setOnClickListener(v -> {
                container.removeView(drinkItemLayout);
                updateFluidUsage(meal, -volumeML);
            });
        }
        
        container.addView(drinkItemLayout);
        updateFluidUsage(meal, volumeML);
    }
    
    private void addDrinkItem(String meal, String category, LinearLayout container) {
        List<Item> drinks = itemDAO.getItemsByCategory(category);
        if (drinks.isEmpty()) {
            Toast.makeText(this, "No " + category.toLowerCase() + " items available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] drinkNames = new String[drinks.size()];
        for (int i = 0; i < drinks.size(); i++) {
            drinkNames[i] = drinks.get(i).getName();
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Select " + category)
            .setItems(drinkNames, (dialog, which) -> {
                Item selectedDrink = drinks.get(which);
                addDrinkToContainer(meal, selectedDrink, container);
            })
            .show();
    }
    
    private void addDrinkToContainer(String meal, Item drink, LinearLayout container) {
        View drinkItemLayout = getLayoutInflater().inflate(R.layout.drink_item, container, false);
        TextView itemNameView = drinkItemLayout.findViewById(R.id.drinkItemName);
        TextView quantityView = drinkItemLayout.findViewById(R.id.drinkQuantity);
        TextView fluidMLView = drinkItemLayout.findViewById(R.id.drinkFluidML);
        Button removeButton = drinkItemLayout.findViewById(R.id.removeDrinkButton);
        
        itemNameView.setText(drink.getName());
        quantityView.setText("1");
        int volumeML = drink.getSizeML() != null ? drink.getSizeML() : 0;
        fluidMLView.setText(volumeML + " ml");
        
        removeButton.setOnClickListener(v -> {
            container.removeView(drinkItemLayout);
            updateFluidUsage(meal, -volumeML);
        });
        
        container.addView(drinkItemLayout);
        updateFluidUsage(meal, volumeML);
    }
    
    private void initializeFluidTracking() {
        fluidUsed.put("breakfast", 0);
        fluidUsed.put("lunch", 0);
        fluidUsed.put("dinner", 0);
        
        fluidLimits.put("breakfast", 9999);
        fluidLimits.put("lunch", 9999);
        fluidLimits.put("dinner", 9999);
        
        updateFluidDisplay();
    }
    
    // FIXED: Update fluid limits based on selected restriction with specific distributions
    private void updateFluidLimits() {
        String restriction = (String) fluidRestrictionSpinner.getSelectedItem();
        
        if (restriction == null || restriction.equals("None")) {
            fluidLimits.put("breakfast", 9999);
            fluidLimits.put("lunch", 9999);
            fluidLimits.put("dinner", 9999);
        } else {
            // FIXED: Use specific fluid distributions as provided
            switch (restriction) {
                case "1000ml":
                    fluidLimits.put("breakfast", 120);
                    fluidLimits.put("lunch", 120);
                    fluidLimits.put("dinner", 160);
                    break;
                case "1200ml":
                    fluidLimits.put("breakfast", 250);
                    fluidLimits.put("lunch", 170);
                    fluidLimits.put("dinner", 180);
                    break;
                case "1500ml":
                    fluidLimits.put("breakfast", 350);
                    fluidLimits.put("lunch", 170);
                    fluidLimits.put("dinner", 180);
                    break;
                case "1800ml":
                    fluidLimits.put("breakfast", 360);
                    fluidLimits.put("lunch", 240);
                    fluidLimits.put("dinner", 240);
                    break;
                case "2000ml":
                    fluidLimits.put("breakfast", 320);
                    fluidLimits.put("lunch", 240);
                    fluidLimits.put("dinner", 240);
                    break;
                case "2500ml":
                    fluidLimits.put("breakfast", 400);
                    fluidLimits.put("lunch", 400);
                    fluidLimits.put("dinner", 400);
                    break;
                default:
                    // Fallback to no limits if unknown restriction
                    fluidLimits.put("breakfast", 9999);
                    fluidLimits.put("lunch", 9999);
                    fluidLimits.put("dinner", 9999);
                    break;
            }
        }
        
        updateFluidDisplay();
    }
    
    private void updateFluidUsage(String meal, int volumeChange) {
        int currentUsage = fluidUsed.get(meal);
        fluidUsed.put(meal, currentUsage + volumeChange);
        updateFluidDisplay();
    }
    
    private void updateFluidDisplay() {
        updateMealFluidDisplay("breakfast", breakfastFluidDisplay);
        updateMealFluidDisplay("lunch", lunchFluidDisplay);
        updateMealFluidDisplay("dinner", dinnerFluidDisplay);
    }
    
    private void updateMealFluidDisplay(String meal, TextView displayView) {
        int used = fluidUsed.get(meal);
        int limit = fluidLimits.get(meal);
        
        String displayText;
        if (limit == 9999) {
            displayText = "Fluid: " + used + " ml (No limit)";
        } else {
            displayText = "Fluid: " + used + " / " + limit + " ml";
            if (used > limit) {
                displayView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                displayView.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
        
        displayView.setText(displayText);
    }
    
    // FIXED: Confirm order with split patient names
    private void confirmOrder() {
        String firstName = patientFirstNameInput.getText().toString().trim();
        String lastName = patientLastNameInput.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String room = (String) roomSpinner.getSelectedItem();
        String diet = (String) dietSpinner.getSelectedItem();
        
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please enter patient first and last name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (wing == null || wing.equals("Select Wing")) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (room == null || room.equals("Select Room")) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (diet == null || diet.equals("Select Diet")) {
            Toast.makeText(this, "Please select a diet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Build order summary
        StringBuilder summary = new StringBuilder();
        summary.append("PATIENT INFORMATION:\n");
        summary.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
        summary.append("Location: ").append(wing).append(" - Room ").append(room).append("\n");
        summary.append("Diet: ").append(diet).append("\n");
        
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        if (fluidRestriction != null && !fluidRestriction.equals("None")) {
            summary.append("Fluid Restriction: ").append(fluidRestriction).append("\n");
        }
        
        String textureModifications = getTextureModifications();
        if (!textureModifications.equals("None")) {
            summary.append("Texture Modifications: ").append(textureModifications).append("\n");
        }
        
        summary.append("\n=== MEAL SELECTIONS ===\n\n");
        
        // Add breakfast items
        summary.append("BREAKFAST:\n");
        addSelectedItem(summary, breakfastColdCereal, "Cold Cereal");
        addSelectedItem(summary, breakfastHotCereal, "Hot Cereal");
        addSelectedItem(summary, breakfastBread, "Bread");
        addSelectedItem(summary, breakfastMuffin, "Muffin");
        addSelectedItem(summary, breakfastMain, "Main");
        addSelectedItem(summary, breakfastFruit, "Fruit");
        addSelectedDrinks(summary, breakfastJuicesContainer);
        addSelectedDrinks(summary, breakfastDrinksContainer);
        
        summary.append("\nLUNCH:\n");
        addSelectedItem(summary, lunchProtein, "Protein");
        addSelectedItem(summary, lunchStarch, "Starch");
        addSelectedItem(summary, lunchVegetable, "Vegetable");
        addSelectedItem(summary, lunchDessert, "Dessert");
        addSelectedDrinks(summary, lunchDrinksContainer);
        
        summary.append("\nDINNER:\n");
        addSelectedItem(summary, dinnerProtein, "Protein");
        addSelectedItem(summary, dinnerStarch, "Starch");
        addSelectedItem(summary, dinnerVegetable, "Vegetable");
        addSelectedItem(summary, dinnerDessert, "Dessert");
        addSelectedDrinks(summary, dinnerDrinksContainer);
        
        new AlertDialog.Builder(this)
            .setTitle("Confirm Order")
            .setMessage(summary.toString())
            .setPositiveButton("Submit", (dialog, which) -> {
                String patientName = firstName + " " + lastName;
                saveOrderToDatabase(firstName, lastName, wing, room, diet);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private String getTextureModifications() {
        List<String> modifications = new ArrayList<>();
        
        if (mechanicalGroundCB.isChecked()) modifications.add("Mechanical Ground");
        if (mechanicalChoppedCB.isChecked()) modifications.add("Mechanical Chopped");
        if (biteSizeCB.isChecked()) modifications.add("Bite Size");
        if (breadOKCB.isChecked()) modifications.add("Bread OK");
        
        return modifications.isEmpty() ? "None" : String.join(", ", modifications);
    }
    
    private void addSelectedItem(StringBuilder summary, Spinner spinner, String itemType) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.equals("None")) {
            summary.append("  ").append(itemType).append(": ").append(selected).append("\n");
        }
    }
    
    private void addSelectedDrinks(StringBuilder summary, LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            TextView itemName = child.findViewById(R.id.drinkItemName);
            TextView quantity = child.findViewById(R.id.drinkQuantity);
            TextView fluidML = child.findViewById(R.id.drinkFluidML);
            
            if (itemName != null) {
                summary.append("  ").append(itemName.getText()).append(" (").append(quantity.getText()).append(") - ").append(fluidML.getText()).append("\n");
            }
        }
    }
    
    // FIXED: Save order with split patient names
    private void saveOrderToDatabase(String firstName, String lastName, String wing, String room, String diet) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("patient_first_name", firstName);
        values.put("patient_last_name", lastName);
        values.put("wing", wing);
        values.put("room", room);
        values.put("order_date", getCurrentTimestamp());
        values.put("diet_type", diet);
        
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        if (fluidRestriction != null && !fluidRestriction.equals("None")) {
            values.put("fluid_restriction", fluidRestriction);
        }
        
        values.put("mechanical_ground", mechanicalGroundCB.isChecked() ? 1 : 0);
        values.put("mechanical_chopped", mechanicalChoppedCB.isChecked() ? 1 : 0);
        values.put("bite_size", biteSizeCB.isChecked() ? 1 : 0);
        values.put("bread_ok", breadOKCB.isChecked() ? 1 : 0);
        
        // Add meal items
        addMealItems(values, "breakfast");
        addMealItems(values, "lunch");
        addMealItems(values, "dinner");
        
        long orderId = db.insert("FinalizedOrder", null, values);
        
        if (orderId > 0) {
            Toast.makeText(this, "Order submitted successfully!", Toast.LENGTH_LONG).show();
            clearForm();
        } else {
            Toast.makeText(this, "Error submitting order. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addMealItems(ContentValues values, String meal) {
        StringBuilder items = new StringBuilder();
        StringBuilder juices = new StringBuilder();
        StringBuilder drinks = new StringBuilder();
        
        // Add food items based on meal
        if (meal.equals("breakfast")) {
            addSpinnerItem(items, breakfastColdCereal);
            addSpinnerItem(items, breakfastHotCereal);
            addSpinnerItem(items, breakfastBread);
            addSpinnerItem(items, breakfastMuffin);
            addSpinnerItem(items, breakfastMain);
            addSpinnerItem(items, breakfastFruit);
            
            addContainerItems(juices, breakfastJuicesContainer);
            addContainerItems(drinks, breakfastDrinksContainer);
        } else if (meal.equals("lunch")) {
            addSpinnerItem(items, lunchProtein);
            addSpinnerItem(items, lunchStarch);
            addSpinnerItem(items, lunchVegetable);
            addSpinnerItem(items, lunchDessert);
            
            addContainerItems(drinks, lunchDrinksContainer);
        } else if (meal.equals("dinner")) {
            addSpinnerItem(items, dinnerProtein);
            addSpinnerItem(items, dinnerStarch);
            addSpinnerItem(items, dinnerVegetable);
            addSpinnerItem(items, dinnerDessert);
            
            addContainerItems(drinks, dinnerDrinksContainer);
        }
        
        values.put(meal + "_items", items.toString());
        values.put(meal + "_juices", juices.toString());
        values.put(meal + "_drinks", drinks.toString());
    }
    
    private void addSpinnerItem(StringBuilder items, Spinner spinner) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.equals("None")) {
            if (items.length() > 0) items.append(", ");
            items.append(selected);
        }
    }
    
    private void addContainerItems(StringBuilder items, LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            TextView itemName = child.findViewById(R.id.drinkItemName);
            TextView quantity = child.findViewById(R.id.drinkQuantity);
            
            if (itemName != null) {
                if (items.length() > 0) items.append(", ");
                items.append(itemName.getText()).append(" (").append(quantity.getText()).append(")");
            }
        }
    }
    
    private void clearForm() {
        // FIXED: Clear split name fields
        patientFirstNameInput.setText("");
        patientLastNameInput.setText("");
        wingSpinner.setSelection(0);
        roomSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        
        // Clear all spinners
        if (breakfastColdCereal.getAdapter() != null) breakfastColdCereal.setSelection(0);
        if (breakfastHotCereal.getAdapter() != null) breakfastHotCereal.setSelection(0);
        if (breakfastBread.getAdapter() != null) breakfastBread.setSelection(0);
        if (breakfastMuffin.getAdapter() != null) breakfastMuffin.setSelection(0);
        if (breakfastMain.getAdapter() != null) breakfastMain.setSelection(0);
        if (breakfastFruit.getAdapter() != null) breakfastFruit.setSelection(0);
        
        if (lunchProtein.getAdapter() != null) lunchProtein.setSelection(0);
        if (lunchStarch.getAdapter() != null) lunchStarch.setSelection(0);
        if (lunchVegetable.getAdapter() != null) lunchVegetable.setSelection(0);
        if (lunchDessert.getAdapter() != null) lunchDessert.setSelection(0);
        
        if (dinnerProtein.getAdapter() != null) dinnerProtein.setSelection(0);
        if (dinnerStarch.getAdapter() != null) dinnerStarch.setSelection(0);
        if (dinnerVegetable.getAdapter() != null) dinnerVegetable.setSelection(0);
        if (dinnerDessert.getAdapter() != null) dinnerDessert.setSelection(0);
        
        // Clear drink containers
        breakfastJuicesContainer.removeAllViews();
        breakfastDrinksContainer.removeAllViews();
        lunchDrinksContainer.removeAllViews();
        dinnerDrinksContainer.removeAllViews();
        
        // Reset fluid tracking
        initializeFluidTracking();
        
        // Focus on first name input
        patientFirstNameInput.requestFocus();
    }
    
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}