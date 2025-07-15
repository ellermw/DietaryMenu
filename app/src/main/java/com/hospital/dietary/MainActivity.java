// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/MainActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.models.Item;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    
    // UI Components
    private RadioGroup dayGroup;
    private EditText patientNameInput;
    private Spinner wingSpinner, roomSpinner, dietSpinner, fluidRestrictionSpinner;
    
    // Texture modification checkboxes
    private CheckBox mechanicalGroundCB, mechanicalChoppedCB, biteSizeCB, breadOKCB;
    
    // Breakfast components
    private Spinner breakfastColdCereal, breakfastHotCereal, breakfastBread;
    private Spinner breakfastMuffin, breakfastMain, breakfastFruit;
    private LinearLayout breakfastJuicesContainer, breakfastDrinksContainer;
    
    // Lunch components
    private Spinner lunchProtein, lunchStarch, lunchVegetable, lunchDessert;
    private LinearLayout lunchDrinksContainer;
    
    // Dinner components
    private Spinner dinnerProtein, dinnerStarch, dinnerVegetable, dinnerDessert;
    private LinearLayout dinnerDrinksContainer;
    
    // Fluid tracking
    private TextView breakfastFluidTracker, lunchFluidTracker, dinnerFluidTracker;
    private Map<String, Integer> fluidUsed = new HashMap<>();
    private Map<String, Integer> fluidLimits = new HashMap<>();
    
    // Database components
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    
    // Data lists
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "ADA", "Cardiac", "Renal", 
                                              "Puree", "Full Liquid", "Clear Liquid");
    
    // Default items for each diet
    private Map<String, Map<String, Object>> defaultItems = new HashMap<>();
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Admin Panel")
            .setIcon(android.R.drawable.ic_menu_manage)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Admin Panel
                showAdminAccessDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAdminAccessDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_access, null);
        EditText passwordInput = dialogView.findViewById(R.id.adminPasswordInput);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Admin Access")
            .setMessage("Enter admin password to access the admin panel:")
            .setView(dialogView)
            .setPositiveButton("Access", null) // Set to null initially
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button accessButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            accessButton.setOnClickListener(v -> {
                String password = passwordInput.getText().toString();
                
                // Simple password check - you can make this more secure
                if (password.equals("admin123")) {
                    dialog.dismiss();
                    openAdminPanel();
                } else {
                    passwordInput.setError("Incorrect password");
                    passwordInput.selectAll();
                }
            });
        });
        
        dialog.show();
        
        // Auto-focus password input
        passwordInput.requestFocus();
    }

    private void openAdminPanel() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        
        // Initialize fluid tracking
        initializeFluidTracking();
        
        // Initialize default items
        initializeDefaultItems();
        
        // Initialize UI components
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load initial data
        loadInitialData();
    }
    
    private void initializeFluidTracking() {
        fluidUsed.put("breakfast", 0);
        fluidUsed.put("lunch", 0);
        fluidUsed.put("dinner", 0);
        fluidLimits.put("breakfast", 0);
        fluidLimits.put("lunch", 0);
        fluidLimits.put("dinner", 0);
    }
    
    private void initializeDefaultItems() {
        // Initialize default items map
        defaultItems.put("Regular", createDefaultMap(
            "Orange Juice", "Cheerios", "", "Toast", "", "Fried Eggs", "Mixed Fruit",
            "Coffee|200", "Chicken Noodle Soup", "Baked Potato", "Green Beans", "Jello",
            "Bottled Water|355", "Turkey Sandwich", "Fruit Cup", "Steamed Broccoli", 
            "Vanilla Pudding", "Ice Tea|240"
        ));
        
        defaultItems.put("ADA", createDefaultMap(
            "Apple Juice", "Cheerios", "", "", "", "Fried Eggs", "Mixed Fruit",
            "Decaf Coffee|200", "Grilled Cheese", "Side Salad", "Steamed Broccoli", 
            "Sugar Free Jello", "Diet Coke|355", "Chicken Strips", "Baked Potato", 
            "Green Beans", "Sugar Free Vanilla Pudding", "Sugar Free Hot Chocolate|240"
        ));
        
        // Add other diets...
    }
    
    private Map<String, Object> createDefaultMap(String... values) {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("breakfastJuice", values[0]);
        defaults.put("breakfastColdCereal", values[1]);
        defaults.put("breakfastHotCereal", values[2]);
        defaults.put("breakfastBread", values[3]);
        defaults.put("breakfastMuffin", values[4]);
        defaults.put("breakfastMain", values[5]);
        defaults.put("breakfastFruit", values[6]);
        defaults.put("breakfastDrink", values[7]);
        defaults.put("lunchProtein", values[8]);
        defaults.put("lunchStarch", values[9]);
        defaults.put("lunchVegetable", values[10]);
        defaults.put("lunchDessert", values[11]);
        defaults.put("lunchDrink", values[12]);
        defaults.put("dinnerProtein", values[13]);
        defaults.put("dinnerStarch", values[14]);
        defaults.put("dinnerVegetable", values[15]);
        defaults.put("dinnerDessert", values[16]);
        defaults.put("dinnerDrink", values[17]);
        return defaults;
    }
    
    private void initializeUI() {
        // Day selection
        dayGroup = findViewById(R.id.dayGroup);
        
        // Patient info
        patientNameInput = findViewById(R.id.patientNameInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomSpinner = findViewById(R.id.roomSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        
        // Texture modifications
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        
        // Breakfast
        breakfastColdCereal = findViewById(R.id.breakfastColdCereal);
        breakfastHotCereal = findViewById(R.id.breakfastHotCereal);
        breakfastBread = findViewById(R.id.breakfastBread);
        breakfastMuffin = findViewById(R.id.breakfastMuffin);
        breakfastMain = findViewById(R.id.breakfastMain);
        breakfastFruit = findViewById(R.id.breakfastFruit);
        breakfastJuicesContainer = findViewById(R.id.breakfastJuicesContainer);
        breakfastDrinksContainer = findViewById(R.id.breakfastDrinksContainer);
        
        // Lunch
        lunchProtein = findViewById(R.id.lunchProtein);
        lunchStarch = findViewById(R.id.lunchStarch);
        lunchVegetable = findViewById(R.id.lunchVegetable);
        lunchDessert = findViewById(R.id.lunchDessert);
        lunchDrinksContainer = findViewById(R.id.lunchDrinksContainer);
        
        // Dinner
        dinnerProtein = findViewById(R.id.dinnerProtein);
        dinnerStarch = findViewById(R.id.dinnerStarch);
        dinnerVegetable = findViewById(R.id.dinnerVegetable);
        dinnerDessert = findViewById(R.id.dinnerDessert);
        dinnerDrinksContainer = findViewById(R.id.dinnerDrinksContainer);
        
        // Fluid trackers
        breakfastFluidTracker = findViewById(R.id.breakfastFluidTracker);
        lunchFluidTracker = findViewById(R.id.lunchFluidTracker);
        dinnerFluidTracker = findViewById(R.id.dinnerFluidTracker);
    }
    
    private void setupListeners() {
        // Wing selection listener
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateRooms();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Diet selection listener
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateMealDropdowns();
                
                // Ask about applying defaults
                String selectedDiet = (String) dietSpinner.getSelectedItem();
                if (selectedDiet != null && !selectedDiet.equals("Select Diet") && 
                    defaultItems.containsKey(selectedDiet)) {
                    
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Apply Defaults")
                        .setMessage("Would you like to apply default items for " + selectedDiet + " diet?")
                        .setPositiveButton("Yes", (dialog, which) -> applyDefaults())
                        .setNegativeButton("No", null)
                        .show();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Fluid restriction listener
        fluidRestrictionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFluidLimits();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Texture modification listeners
        CompoundButton.OnCheckedChangeListener textureListener = (buttonView, isChecked) -> populateMealDropdowns();
        mechanicalGroundCB.setOnCheckedChangeListener(textureListener);
        mechanicalChoppedCB.setOnCheckedChangeListener(textureListener);
        biteSizeCB.setOnCheckedChangeListener(textureListener);
        breadOKCB.setOnCheckedChangeListener(textureListener);
    }
    
    private void loadInitialData() {
        // Setup wing spinner
        List<String> wingOptions = new ArrayList<>();
        wingOptions.add("Select Wing");
        wingOptions.addAll(wings);
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, wingOptions);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // Setup diet spinner
        List<String> dietOptions = new ArrayList<>();
        dietOptions.add("Select Diet");
        dietOptions.addAll(diets);
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, dietOptions);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // Setup fluid restriction spinner
        List<String> fluidRestrictions = Arrays.asList("No Restriction", "1000ml", "1200ml", 
                                                       "1500ml", "1800ml", "2000ml", "2500ml");
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
        
        // Load meal dropdowns
        populateMealDropdowns();
    }
    
    private void populateRooms() {
        String selectedWing = (String) wingSpinner.getSelectedItem();
        List<String> rooms = new ArrayList<>();
        rooms.add("Select Room");
        
        if (selectedWing != null && !selectedWing.equals("Select Wing")) {
            switch (selectedWing) {
                case "1 South":
                    for (int i = 101; i <= 110; i++) rooms.add(String.valueOf(i));
                    break;
                case "2 North":
                    for (int i = 201; i <= 210; i++) rooms.add(String.valueOf(i));
                    break;
                case "Labor and Delivery":
                    for (int i = 1; i <= 6; i++) rooms.add("L" + i);
                    break;
                case "2 West":
                    for (int i = 1; i <= 8; i++) rooms.add("2W0" + i);
                    break;
                case "3 North":
                    for (int i = 301; i <= 310; i++) rooms.add(String.valueOf(i));
                    break;
                case "ICU":
                    for (int i = 1; i <= 8; i++) rooms.add("ICU" + i);
                    break;
            }
        }
        
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, rooms);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomSpinner.setAdapter(roomAdapter);
    }
    
    private void populateMealDropdowns() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = "ADA".equals(selectedDiet);
        
        // Check texture modifications
        boolean hasTextureModification = mechanicalGroundCB.isChecked() || 
                                       mechanicalChoppedCB.isChecked() || 
                                       biteSizeCB.isChecked();
        boolean breadOK = breadOKCB.isChecked();
        boolean filterBread = hasTextureModification && !breadOK;
        
        // Get items from database with filtering
        List<Item> coldCereals = itemDAO.getFilteredItems(itemDAO.getColdCerealItems(), isADA, filterBread);
        List<Item> hotCereals = itemDAO.getFilteredItems(itemDAO.getHotCerealItems(), isADA, filterBread);
        List<Item> breads = itemDAO.getFilteredItems(itemDAO.getBreadItems(), isADA, filterBread);
        List<Item> muffins = itemDAO.getFilteredItems(itemDAO.getMuffinItems(), isADA, filterBread);
        List<Item> breakfastMains = itemDAO.getFilteredItems(itemDAO.getBreakfastItems(), isADA, filterBread);
        List<Item> fruits = itemDAO.getFilteredItems(itemDAO.getFruitItems(), isADA, filterBread);
        
        List<Item> proteins = itemDAO.getFilteredItems(itemDAO.getProteinItems(), isADA, filterBread);
        List<Item> starches = itemDAO.getFilteredItems(itemDAO.getStarchItems(), isADA, filterBread);
        List<Item> vegetables = itemDAO.getFilteredItems(itemDAO.getVegetableItems(), isADA, filterBread);
        List<Item> desserts = itemDAO.getFilteredItems(itemDAO.getDessertItems(), isADA, filterBread);
        
        // Populate breakfast spinners
        populateSpinner(breakfastColdCereal, coldCereals);
        populateSpinner(breakfastHotCereal, hotCereals);
        populateSpinner(breakfastBread, breads);
        populateSpinner(breakfastMuffin, muffins);
        populateSpinner(breakfastMain, breakfastMains);
        populateSpinner(breakfastFruit, fruits);
        
        // Populate lunch spinners
        populateSpinner(lunchProtein, proteins);
        populateSpinner(lunchStarch, starches);
        populateSpinner(lunchVegetable, vegetables);
        populateSpinner(lunchDessert, desserts);
        
        // Populate dinner spinners
        populateSpinner(dinnerProtein, proteins);
        populateSpinner(dinnerStarch, starches);
        populateSpinner(dinnerVegetable, vegetables);
        populateSpinner(dinnerDessert, desserts);
    }
    
    private void populateSpinner(Spinner spinner, List<Item> items) {
        List<String> itemNames = new ArrayList<>();
        itemNames.add("-- Select --");
        
        for (Item item : items) {
            itemNames.add(item.toString());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    private void updateFluidLimits() {
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        
        if (fluidRestriction == null || fluidRestriction.equals("No Restriction")) {
            fluidLimits.put("breakfast", 0);
            fluidLimits.put("lunch", 0);
            fluidLimits.put("dinner", 0);
            hideFluidTrackers();
            return;
        }
        
        // Get limits from database
        Integer breakfastLimit = itemDAO.getFluidLimit(fluidRestriction, "breakfast");
        Integer lunchLimit = itemDAO.getFluidLimit(fluidRestriction, "lunch");
        Integer dinnerLimit = itemDAO.getFluidLimit(fluidRestriction, "dinner");
        
        fluidLimits.put("breakfast", breakfastLimit != null ? breakfastLimit : 0);
        fluidLimits.put("lunch", lunchLimit != null ? lunchLimit : 0);
        fluidLimits.put("dinner", dinnerLimit != null ? dinnerLimit : 0);
        
        updateFluidTrackers();
    }
    
    private void updateFluidTrackers() {
        updateFluidTracker("breakfast", breakfastFluidTracker);
        updateFluidTracker("lunch", lunchFluidTracker);
        updateFluidTracker("dinner", dinnerFluidTracker);
    }
    
    private void updateFluidTracker(String meal, TextView tracker) {
        int used = fluidUsed.get(meal);
        int limit = fluidLimits.get(meal);
        
        if (limit > 0) {
            tracker.setVisibility(View.VISIBLE);
            tracker.setText(String.format("Fluid Used: %dml / %dml", used, limit));
            
            // Update color based on usage
            float percentage = (float) used / limit;
            if (percentage >= 1.0f) {
                tracker.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (percentage >= 0.8f) {
                tracker.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                tracker.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        } else {
            tracker.setVisibility(View.GONE);
        }
    }
    
    private void hideFluidTrackers() {
        breakfastFluidTracker.setVisibility(View.GONE);
        lunchFluidTracker.setVisibility(View.GONE);
        dinnerFluidTracker.setVisibility(View.GONE);
    }
    
    public void addJuice(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = "ADA".equals(selectedDiet);
        
        List<Item> juices = itemDAO.getFilteredItems(itemDAO.getJuiceItems(), isADA, false);
        addDrinkToContainer(breakfastJuicesContainer, juices, "breakfast");
    }
    
    public void addBreakfastDrink(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = "ADA".equals(selectedDiet);
        
        List<Item> drinks = itemDAO.getFilteredItems(itemDAO.getDrinkItems(), isADA, false);
        addDrinkToContainer(breakfastDrinksContainer, drinks, "breakfast");
    }
    
    public void addLunchDrink(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = "ADA".equals(selectedDiet);
        
        List<Item> drinks = itemDAO.getFilteredItems(itemDAO.getDrinkItems(), isADA, false);
        drinks.addAll(itemDAO.getFilteredItems(itemDAO.getJuiceItems(), isADA, false));
        addDrinkToContainer(lunchDrinksContainer, drinks, "lunch");
    }
    
    public void addDinnerDrink(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = "ADA".equals(selectedDiet);
        
        List<Item> drinks = itemDAO.getFilteredItems(itemDAO.getDrinkItems(), isADA, false);
        drinks.addAll(itemDAO.getFilteredItems(itemDAO.getJuiceItems(), isADA, false));
        addDrinkToContainer(dinnerDrinksContainer, drinks, "dinner");
    }
    
    private void addDrinkToContainer(LinearLayout container, List<Item> drinks, String meal) {
        View drinkView = getLayoutInflater().inflate(R.layout.drink_item, container, false);
        
        Spinner drinkSpinner = drinkView.findViewById(R.id.drinkSpinner);
        EditText amountInput = drinkView.findViewById(R.id.amountInput);
        Button removeButton = drinkView.findViewById(R.id.removeButton);
        
        // Populate spinner
        populateSpinner(drinkSpinner, drinks);
        
        // Set up amount input when drink is selected
        drinkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Not "-- Select --"
                    Item selectedItem = drinks.get(position - 1);
                    if (selectedItem.getSizeML() != null) {
                        int requestedAmount = selectedItem.getSizeML();
                        int adjustedAmount = adjustFluidForLimit(meal, requestedAmount);
                        amountInput.setText(String.valueOf(adjustedAmount));
                        
                        if (adjustedAmount < requestedAmount) {
                            Toast.makeText(MainActivity.this, 
                                String.format("Drink size adjusted from %dml to %dml due to fluid limit", 
                                requestedAmount, adjustedAmount), Toast.LENGTH_SHORT).show();
                        }
                    }
                    amountInput.setEnabled(true);
                    updateMealFluid(meal);
                } else {
                    amountInput.setEnabled(false);
                    amountInput.setText("");
                    updateMealFluid(meal);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Amount input listener
        amountInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateMealFluid(meal);
            }
        });
        
        // Remove button listener
        removeButton.setOnClickListener(v -> {
            container.removeView(drinkView);
            updateMealFluid(meal);
        });
        
        container.addView(drinkView);
    }
    
    private int adjustFluidForLimit(String meal, int requestedAmount) {
        int limit = fluidLimits.get(meal);
        if (limit <= 0) return requestedAmount; // No restriction
        
        int used = fluidUsed.get(meal);
        int remaining = limit - used;
        
        return Math.min(requestedAmount, Math.max(0, remaining));
    }
    
    private void updateMealFluid(String meal) {
        LinearLayout container;
        switch (meal) {
            case "breakfast":
                container = breakfastDrinksContainer;
                // Also check juices container
                LinearLayout juicesContainer = breakfastJuicesContainer;
                int totalUsed = calculateContainerFluid(container) + calculateContainerFluid(juicesContainer);
                fluidUsed.put(meal, totalUsed);
                break;
            case "lunch":
                container = lunchDrinksContainer;
                fluidUsed.put(meal, calculateContainerFluid(container));
                break;
            case "dinner":
                container = dinnerDrinksContainer;
                fluidUsed.put(meal, calculateContainerFluid(container));
                break;
        }
        
        updateFluidTrackers();
    }
    
    private int calculateContainerFluid(LinearLayout container) {
        int total = 0;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            EditText amountInput = child.findViewById(R.id.amountInput);
            if (amountInput != null && !amountInput.getText().toString().isEmpty()) {
                try {
                    total += Integer.parseInt(amountInput.getText().toString());
                } catch (NumberFormatException e) {
                    // Ignore invalid numbers
                }
            }
        }
        return total;
    }
    
    public void applyDefaults(View view) {
        applyDefaults();
    }
    
    public void clearForm(View view) {
        clearForm();
    }
    
    public void applyDefaults() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        if (selectedDiet == null || !defaultItems.containsKey(selectedDiet)) {
            return;
        }
        
        Map<String, Object> defaults = defaultItems.get(selectedDiet);
        
        // Apply spinner defaults
        setSpinnerByValue(breakfastColdCereal, (String) defaults.get("breakfastColdCereal"));
        setSpinnerByValue(breakfastHotCereal, (String) defaults.get("breakfastHotCereal"));
        setSpinnerByValue(breakfastBread, (String) defaults.get("breakfastBread"));
        setSpinnerByValue(breakfastMuffin, (String) defaults.get("breakfastMuffin"));
        setSpinnerByValue(breakfastMain, (String) defaults.get("breakfastMain"));
        setSpinnerByValue(breakfastFruit, (String) defaults.get("breakfastFruit"));
        
        setSpinnerByValue(lunchProtein, (String) defaults.get("lunchProtein"));
        setSpinnerByValue(lunchStarch, (String) defaults.get("lunchStarch"));
        setSpinnerByValue(lunchVegetable, (String) defaults.get("lunchVegetable"));
        setSpinnerByValue(lunchDessert, (String) defaults.get("lunchDessert"));
        
        setSpinnerByValue(dinnerProtein, (String) defaults.get("dinnerProtein"));
        setSpinnerByValue(dinnerStarch, (String) defaults.get("dinnerStarch"));
        setSpinnerByValue(dinnerVegetable, (String) defaults.get("dinnerVegetable"));
        setSpinnerByValue(dinnerDessert, (String) defaults.get("dinnerDessert"));
        
        // Apply juice default
        String juiceDefault = (String) defaults.get("breakfastJuice");
        if (juiceDefault != null && !juiceDefault.isEmpty()) {
            addJuice(null);
            // Set the added juice
            View lastJuice = breakfastJuicesContainer.getChildAt(breakfastJuicesContainer.getChildCount() - 1);
            if (lastJuice != null) {
                Spinner juiceSpinner = lastJuice.findViewById(R.id.drinkSpinner);
                setSpinnerByValue(juiceSpinner, juiceDefault);
            }
        }
        
        // Apply drink defaults
        addDefaultDrink(breakfastDrinksContainer, (String) defaults.get("breakfastDrink"), "breakfast");
        addDefaultDrink(lunchDrinksContainer, (String) defaults.get("lunchDrink"), "lunch");
        addDefaultDrink(dinnerDrinksContainer, (String) defaults.get("dinnerDrink"), "dinner");
        
        Toast.makeText(this, "Default items applied for " + selectedDiet + " diet", Toast.LENGTH_SHORT).show();
    }
    
    private void addDefaultDrink(LinearLayout container, String drinkDefault, String meal) {
        if (drinkDefault == null || drinkDefault.isEmpty()) return;
        
        String[] parts = drinkDefault.split("\\|");
        String drinkName = parts[0];
        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 240;
        
        // Add drink container first
        if (meal.equals("breakfast")) {
            addBreakfastDrink(null);
        } else if (meal.equals("lunch")) {
            addLunchDrink(null);
        } else {
            addDinnerDrink(null);
        }
        
        // Set the added drink
        View lastDrink = container.getChildAt(container.getChildCount() - 1);
        if (lastDrink != null) {
            Spinner drinkSpinner = lastDrink.findViewById(R.id.drinkSpinner);
            EditText amountInput = lastDrink.findViewById(R.id.amountInput);
            setSpinnerByValue(drinkSpinner, drinkName);
            amountInput.setText(String.valueOf(amount));
        }
    }
    
    private void setSpinnerByValue(Spinner spinner, String value) {
        if (value == null || value.isEmpty()) return;
        
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = (String) adapter.getItem(i);
            if (item != null && item.startsWith(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    
    public void finalizeOrder(View view) {
        if (validateForm()) {
            saveOrder();
            Toast.makeText(this, "Order finalized successfully!", Toast.LENGTH_LONG).show();
            clearForm();
        }
    }
    
    private boolean validateForm() {
        if (patientNameInput.getText().toString().trim().isEmpty()) {
            showError("Patient name is required");
            return false;
        }
        
        if (wingSpinner.getSelectedItemPosition() == 0) {
            showError("Please select a wing");
            return false;
        }
        
        if (roomSpinner.getSelectedItemPosition() == 0) {
            showError("Please select a room");
            return false;
        }
        
        if (dietSpinner.getSelectedItemPosition() == 0) {
            showError("Please select a diet");
            return false;
        }
        
        return true;
    }
    
    private void saveOrder() {
        String patientName = patientNameInput.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String room = (String) roomSpinner.getSelectedItem();
        String diet = (String) dietSpinner.getSelectedItem();
        
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        // Save patient
        long patientId = itemDAO.savePatient(patientName, wing, room, diet);
        
        // Save meal orders
        saveMealOrder((int) patientId, "breakfast", timestamp);
        saveMealOrder((int) patientId, "lunch", timestamp);
        saveMealOrder((int) patientId, "dinner", timestamp);
    }
    
    private void saveMealOrder(int patientId, String meal, String timestamp) {
        long orderId = itemDAO.saveMealOrder(patientId, meal, timestamp);
        
        // Save meal items
        saveMealItems((int) orderId, meal);
    }
    
    private void saveMealItems(int orderId, String meal) {
        // Save selected spinner items
        if (meal.equals("breakfast")) {
            saveSpinnerItem(orderId, breakfastColdCereal);
            saveSpinnerItem(orderId, breakfastHotCereal);
            saveSpinnerItem(orderId, breakfastBread);
            saveSpinnerItem(orderId, breakfastMuffin);
            saveSpinnerItem(orderId, breakfastMain);
            saveSpinnerItem(orderId, breakfastFruit);
            saveContainerItems(orderId, breakfastJuicesContainer);
            saveContainerItems(orderId, breakfastDrinksContainer);
        } else if (meal.equals("lunch")) {
            saveSpinnerItem(orderId, lunchProtein);
            saveSpinnerItem(orderId, lunchStarch);
            saveSpinnerItem(orderId, lunchVegetable);
            saveSpinnerItem(orderId, lunchDessert);
            saveContainerItems(orderId, lunchDrinksContainer);
        } else if (meal.equals("dinner")) {
            saveSpinnerItem(orderId, dinnerProtein);
            saveSpinnerItem(orderId, dinnerStarch);
            saveSpinnerItem(orderId, dinnerVegetable);
            saveSpinnerItem(orderId, dinnerDessert);
            saveContainerItems(orderId, dinnerDrinksContainer);
        }
    }
    
    private void saveSpinnerItem(int orderId, Spinner spinner) {
        if (spinner.getSelectedItemPosition() > 0) {
            String itemName = (String) spinner.getSelectedItem();
            // Remove size information to get clean item name
            if (itemName.contains("(")) {
                itemName = itemName.substring(0, itemName.indexOf("(")).trim();
            }
            Item item = itemDAO.getItemByName(itemName);
            if (item != null) {
                itemDAO.saveMealLine(orderId, item.getItemId());
            }
        }
    }
    
    private void saveContainerItems(int orderId, LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            Spinner drinkSpinner = child.findViewById(R.id.drinkSpinner);
            if (drinkSpinner != null && drinkSpinner.getSelectedItemPosition() > 0) {
                String itemName = (String) drinkSpinner.getSelectedItem();
                // Remove size information to get clean item name
                if (itemName.contains("(")) {
                    itemName = itemName.substring(0, itemName.indexOf("(")).trim();
                }
                Item item = itemDAO.getItemByName(itemName);
                if (item != null) {
                    itemDAO.saveMealLine(orderId, item.getItemId());
                }
            }
        }
    }
    
    private void showError(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Validation Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void clearForm() {
        patientNameInput.setText("");
        wingSpinner.setSelection(0);
        roomSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        
        // Clear checkboxes
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        
        // Clear meal selections
        breakfastColdCereal.setSelection(0);
        breakfastHotCereal.setSelection(0);
        breakfastBread.setSelection(0);
        breakfastMuffin.setSelection(0);
        breakfastMain.setSelection(0);
        breakfastFruit.setSelection(0);
        
        lunchProtein.setSelection(0);
        lunchStarch.setSelection(0);
        lunchVegetable.setSelection(0);
        lunchDessert.setSelection(0);
        
        dinnerProtein.setSelection(0);
        dinnerStarch.setSelection(0);
        dinnerVegetable.setSelection(0);
        dinnerDessert.setSelection(0);
        
        // Clear drink containers
        breakfastJuicesContainer.removeAllViews();
        breakfastDrinksContainer.removeAllViews();
        lunchDrinksContainer.removeAllViews();
        dinnerDrinksContainer.removeAllViews();
        
        // Reset fluid tracking
        initializeFluidTracking();
        hideFluidTrackers();
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
