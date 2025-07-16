// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/MainActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.dao.FinalizedOrderDAO;
import com.hospital.dietary.models.Item;
import com.hospital.dietary.models.User;
import com.hospital.dietary.models.FinalizedOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private LinearLayout lunchJuicesContainer, lunchDrinksContainer;
    
    // Dinner components
    private Spinner dinnerProtein, dinnerStarch, dinnerVegetable, dinnerDessert;
    private LinearLayout dinnerJuicesContainer, dinnerDrinksContainer;
    
    // Fluid tracking
    private TextView breakfastFluidTracker, lunchFluidTracker, dinnerFluidTracker;
    private Map<String, Integer> fluidUsed = new HashMap<>();
    private Map<String, Integer> fluidLimits = new HashMap<>();
    
    // Database components
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private UserDAO userDAO;
    private FinalizedOrderDAO finalizedOrderDAO;
    
    // Data lists
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "ADA", "Cardiac", "Renal", 
                                              "Puree", "Full Liquid", "Clear Liquid");
    
    // Default items for each diet
    private Map<String, Map<String, Object>> defaultItems = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);
        finalizedOrderDAO = new FinalizedOrderDAO(dbHelper);
        
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
        
        // DEBUG: Add debug methods (REMOVE THESE LINES AFTER TESTING)
        debugDatabase();
        
        // TEMPORARY DEBUG BUTTON (REMOVE AFTER TESTING)
        createDebugButton();
    }
    
    // ===== ADMIN PANEL FUNCTIONALITY =====
    
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
        EditText usernameInput = dialogView.findViewById(R.id.adminUsernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.adminPasswordInput);

        new AlertDialog.Builder(this)
            .setTitle("Admin Access")
            .setMessage("Please enter your administrator credentials:")
            .setView(dialogView)
            .setPositiveButton("Login", (dialog, which) -> {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Validate credentials
                User user = userDAO.validateUser(username, password);
                if (user != null && user.getRole().equals("admin")) {
                    launchAdminPanel(user);
                } else {
                    Toast.makeText(this, "Invalid credentials or insufficient privileges", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void launchAdminPanel(User user) {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("current_user", user.getUsername());
        startActivity(intent);
    }
    
    // ===== INITIALIZATION METHODS =====
    
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
        lunchJuicesContainer = findViewById(R.id.lunchJuicesContainer);
        lunchDrinksContainer = findViewById(R.id.lunchDrinksContainer);
        
        // Dinner
        dinnerProtein = findViewById(R.id.dinnerProtein);
        dinnerStarch = findViewById(R.id.dinnerStarch);
        dinnerVegetable = findViewById(R.id.dinnerVegetable);
        dinnerDessert = findViewById(R.id.dinnerDessert);
        dinnerJuicesContainer = findViewById(R.id.dinnerJuicesContainer);
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
    
    // ===== ROOM POPULATION (CORRECTED) =====
    
    private void populateRooms() {
        String selectedWing = (String) wingSpinner.getSelectedItem();
        List<String> rooms = new ArrayList<>();
        rooms.add("Select Room");
        
        if (selectedWing != null && !selectedWing.equals("Select Wing")) {
            switch (selectedWing) {
                case "1 South":
                    // Rooms 106 through 122
                    for (int i = 106; i <= 122; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "2 North":
                    // Rooms 250 through 264
                    for (int i = 250; i <= 264; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "Labor and Delivery":
                    // Rooms LDR1 through LDR6
                    for (int i = 1; i <= 6; i++) {
                        rooms.add("LDR" + i);
                    }
                    break;
                case "2 West":
                    // Rooms 225 through 248
                    for (int i = 225; i <= 248; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "3 North":
                    // Rooms 349 through 371
                    for (int i = 349; i <= 371; i++) {
                        rooms.add(String.valueOf(i));
                    }
                    break;
                case "ICU":
                    // Rooms ICU1 through ICU6
                    for (int i = 1; i <= 6; i++) {
                        rooms.add("ICU" + i);
                    }
                    break;
            }
        }
        
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, rooms);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomSpinner.setAdapter(roomAdapter);
    }
    
    // ===== MEAL DROPDOWN POPULATION =====
    
    private void populateMealDropdowns() {
        populateSpinnerWithItems(breakfastColdCereal, "Cold Cereals");
        populateSpinnerWithItems(breakfastHotCereal, "Hot Cereals");
        populateSpinnerWithItems(breakfastBread, "Breads");
        populateSpinnerWithItems(breakfastMuffin, "Fresh Muffins");
        populateSpinnerWithItems(breakfastMain, "Breakfast");
        populateSpinnerWithItems(breakfastFruit, "Fruits");
        
        populateSpinnerWithItems(lunchProtein, "Protein/Entrée");
        populateSpinnerWithItems(lunchStarch, "Starch");
        populateSpinnerWithItems(lunchVegetable, "Vegetable");
        populateSpinnerWithItems(lunchDessert, "Dessert");
        
        populateSpinnerWithItems(dinnerProtein, "Protein/Entrée");
        populateSpinnerWithItems(dinnerStarch, "Starch");
        populateSpinnerWithItems(dinnerVegetable, "Vegetable");
        populateSpinnerWithItems(dinnerDessert, "Dessert");
    }
    
    private void populateSpinnerWithItems(Spinner spinner, String category) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADADiet = selectedDiet != null && selectedDiet.equals("ADA");
        
        List<Item> items = itemDAO.getItemsByCategory(category);
        List<String> itemNames = new ArrayList<>();
        itemNames.add("Select " + category);
        
        boolean applyTextureFilter = mechanicalGroundCB.isChecked() || 
                                   mechanicalChoppedCB.isChecked() || 
                                   biteSizeCB.isChecked();
        
        for (Item item : items) {
            boolean includeItem = true;
            
            // ADA diet filtering
            if (isADADiet && !item.isAdaFriendly()) {
                includeItem = false;
            }
            
            // Texture modification filtering
            if (applyTextureFilter && !breadOKCB.isChecked() && 
                (item.getName().toLowerCase().contains("bread") || 
                 item.getName().toLowerCase().contains("roll") || 
                 item.getName().toLowerCase().contains("toast"))) {
                includeItem = false;
            }
            
            if (includeItem) {
                itemNames.add(item.getName());
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    // ===== JUICE AND DRINK FUNCTIONALITY =====
    
    public void addJuice(View view) {
        String mealType = getMealTypeFromButton(view);
        LinearLayout container = getJuiceContainer(mealType);
        addDynamicItem(container, "Juices", mealType);
    }
    
    public void addDrink(View view) {
        String mealType = getMealTypeFromButton(view);
        LinearLayout container = getDrinkContainer(mealType);
        addDynamicItem(container, "Drink", mealType);
    }
    
    private String getMealTypeFromButton(View button) {
        String buttonId = getResources().getResourceEntryName(button.getId());
        if (buttonId.contains("breakfast")) return "breakfast";
        if (buttonId.contains("lunch")) return "lunch";
        if (buttonId.contains("dinner")) return "dinner";
        return "breakfast"; // default
    }
    
    private LinearLayout getJuiceContainer(String mealType) {
        switch (mealType) {
            case "breakfast": return breakfastJuicesContainer;
            case "lunch": return lunchJuicesContainer;
            case "dinner": return dinnerJuicesContainer;
            default: return breakfastJuicesContainer;
        }
    }
    
    private LinearLayout getDrinkContainer(String mealType) {
        switch (mealType) {
            case "breakfast": return breakfastDrinksContainer;
            case "lunch": return lunchDrinksContainer;
            case "dinner": return dinnerDrinksContainer;
            default: return breakfastDrinksContainer;
        }
    }
    
    private void addDynamicItem(LinearLayout container, String category, String mealType) {
        View itemView = getLayoutInflater().inflate(R.layout.drink_item, container, false);
        
        Spinner itemSpinner = itemView.findViewById(R.id.drinkSpinner);
        Button removeButton = itemView.findViewById(R.id.removeDrinkButton);
        
        // Populate spinner
        populateSpinnerWithItems(itemSpinner, category);
        
        // Set up remove button
        removeButton.setOnClickListener(v -> {
            container.removeView(itemView);
            updateFluidTracking(mealType);
        });
        
        // Add fluid tracking for drinks
        if (category.equals("Drink")) {
            itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateFluidTracking(mealType);
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        
        container.addView(itemView);
    }
    
    // ===== FLUID TRACKING =====
    
    private void updateFluidLimits() {
        String restriction = (String) fluidRestrictionSpinner.getSelectedItem();
        if (restriction == null || restriction.equals("No Restriction")) {
            hideFluidTrackers();
            return;
        }
        
        showFluidTrackers();
        
        int totalLimit = Integer.parseInt(restriction.replace("ml", ""));
        int breakfastLimit = (int) (totalLimit * 0.30);
        int lunchLimit = (int) (totalLimit * 0.35);
        int dinnerLimit = totalLimit - breakfastLimit - lunchLimit;
        
        fluidLimits.put("breakfast", breakfastLimit);
        fluidLimits.put("lunch", lunchLimit);
        fluidLimits.put("dinner", dinnerLimit);
        
        updateFluidTracking("breakfast");
        updateFluidTracking("lunch");
        updateFluidTracking("dinner");
    }
    
    private void showFluidTrackers() {
        breakfastFluidTracker.setVisibility(View.VISIBLE);
        lunchFluidTracker.setVisibility(View.VISIBLE);
        dinnerFluidTracker.setVisibility(View.VISIBLE);
    }
    
    private void hideFluidTrackers() {
        breakfastFluidTracker.setVisibility(View.GONE);
        lunchFluidTracker.setVisibility(View.GONE);
        dinnerFluidTracker.setVisibility(View.GONE);
    }
    
    private void updateFluidTracking(String mealType) {
        LinearLayout container = getDrinkContainer(mealType);
        int totalFluid = 0;
        
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                Spinner spinner = child.findViewById(R.id.drinkSpinner);
                String selectedDrink = (String) spinner.getSelectedItem();
                if (selectedDrink != null && !selectedDrink.startsWith("Select")) {
                    totalFluid += getFluidAmount(selectedDrink);
                }
            }
        }
        
        fluidUsed.put(mealType, totalFluid);
        updateFluidDisplay(mealType);
    }
    
    private int getFluidAmount(String drinkName) {
        List<Item> drinks = itemDAO.searchItems(drinkName);
        for (Item drink : drinks) {
            if (drink.getName().equals(drinkName) && drink.getSizeML() != null) {
                return drink.getSizeML();
            }
        }
        return 240; // default size
    }
    
    private void updateFluidDisplay(String mealType) {
        TextView tracker = getFluidTracker(mealType);
        int used = fluidUsed.get(mealType);
        int limit = fluidLimits.get(mealType);
        
        String text = "Fluid Used: " + used + "ml / " + limit + "ml";
        tracker.setText(text);
        
        if (used > limit) {
            tracker.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        } else {
            tracker.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
    }
    
    private TextView getFluidTracker(String mealType) {
        switch (mealType) {
            case "breakfast": return breakfastFluidTracker;
            case "lunch": return lunchFluidTracker;
            case "dinner": return dinnerFluidTracker;
            default: return breakfastFluidTracker;
        }
    }
    
    // ===== DEFAULT ITEMS APPLICATION =====
    
    public void applyDefaults() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        if (selectedDiet == null || !defaultItems.containsKey(selectedDiet)) {
            Toast.makeText(this, "No defaults available for selected diet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> defaults = defaultItems.get(selectedDiet);
        
        // Apply breakfast defaults
        setSpinnerSelection(breakfastColdCereal, (String) defaults.get("breakfastColdCereal"));
        setSpinnerSelection(breakfastHotCereal, (String) defaults.get("breakfastHotCereal"));
        setSpinnerSelection(breakfastBread, (String) defaults.get("breakfastBread"));
        setSpinnerSelection(breakfastMuffin, (String) defaults.get("breakfastMuffin"));
        setSpinnerSelection(breakfastMain, (String) defaults.get("breakfastMain"));
        setSpinnerSelection(breakfastFruit, (String) defaults.get("breakfastFruit"));
        
        // Apply lunch defaults
        setSpinnerSelection(lunchProtein, (String) defaults.get("lunchProtein"));
        setSpinnerSelection(lunchStarch, (String) defaults.get("lunchStarch"));
        setSpinnerSelection(lunchVegetable, (String) defaults.get("lunchVegetable"));
        setSpinnerSelection(lunchDessert, (String) defaults.get("lunchDessert"));
        
        // Apply dinner defaults
        setSpinnerSelection(dinnerProtein, (String) defaults.get("dinnerProtein"));
        setSpinnerSelection(dinnerStarch, (String) defaults.get("dinnerStarch"));
        setSpinnerSelection(dinnerVegetable, (String) defaults.get("dinnerVegetable"));
        setSpinnerSelection(dinnerDessert, (String) defaults.get("dinnerDessert"));
        
        // Apply juice and drink defaults
        addDefaultJuice("breakfast", (String) defaults.get("breakfastJuice"));
        addDefaultDrink("breakfast", (String) defaults.get("breakfastDrink"));
        addDefaultDrink("lunch", (String) defaults.get("lunchDrink"));
        addDefaultDrink("dinner", (String) defaults.get("dinnerDrink"));
        
        Toast.makeText(this, "Default items applied successfully!", Toast.LENGTH_SHORT).show();
    }
    
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || value.trim().isEmpty()) return;
        
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }
    
    private void addDefaultJuice(String mealType, String juiceName) {
        if (juiceName == null || juiceName.trim().isEmpty()) return;
        
        LinearLayout container = getJuiceContainer(mealType);
        addDynamicItem(container, "Juices", mealType);
        
        // Set the selection on the newly added item
        if (container.getChildCount() > 0) {
            View lastItem = container.getChildAt(container.getChildCount() - 1);
            Spinner spinner = lastItem.findViewById(R.id.drinkSpinner);
            setSpinnerSelection(spinner, juiceName);
        }
    }
    
    private void addDefaultDrink(String mealType, String drinkInfo) {
        if (drinkInfo == null || drinkInfo.trim().isEmpty()) return;
        
        String drinkName = drinkInfo.contains("|") ? drinkInfo.split("\\|")[0] : drinkInfo;
        
        LinearLayout container = getDrinkContainer(mealType);
        addDynamicItem(container, "Drink", mealType);
        
        // Set the selection on the newly added item
        if (container.getChildCount() > 0) {
            View lastItem = container.getChildAt(container.getChildCount() - 1);
            Spinner spinner = lastItem.findViewById(R.id.drinkSpinner);
            setSpinnerSelection(spinner, drinkName);
        }
    }
    
    // ===== FORM MANAGEMENT =====
    
    public void clearForm(View view) {
        // Clear patient info
        patientNameInput.setText("");
        wingSpinner.setSelection(0);
        roomSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        
        // Clear texture modifications
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        
        // Clear meal selections
        clearSpinnerSelections();
        
        // Clear dynamic items
        breakfastJuicesContainer.removeAllViews();
        breakfastDrinksContainer.removeAllViews();
        lunchJuicesContainer.removeAllViews();
        lunchDrinksContainer.removeAllViews();
        dinnerJuicesContainer.removeAllViews();
        dinnerDrinksContainer.removeAllViews();
        
        // Reset fluid tracking
        initializeFluidTracking();
        hideFluidTrackers();
        
        // Clear day selection
        dayGroup.clearCheck();
        
        Toast.makeText(this, "Form cleared successfully!", Toast.LENGTH_SHORT).show();
    }
    
    private void clearSpinnerSelections() {
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
    }
    
    // ===== FINALIZED ORDER FUNCTIONALITY =====
    
    public void finalizeOrder(View view) {
        // Validate required fields first
        String patientName = patientNameInput.getText().toString().trim();
        String selectedWing = (String) wingSpinner.getSelectedItem();
        String selectedRoom = (String) roomSpinner.getSelectedItem();
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        String selectedFluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        
        if (patientName.isEmpty()) {
            patientNameInput.setError("Patient name is required");
            return;
        }
        
        if (selectedWing == null || selectedWing.equals("Select Wing")) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedRoom == null || selectedRoom.equals("Select Room")) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedDiet == null || selectedDiet.equals("Select Diet")) {
            Toast.makeText(this, "Please select a diet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get selected date
        String orderDate = getSelectedDate();
        
        // Check if order already exists for this wing/room/date
        if (finalizedOrderDAO.orderExists(selectedWing, selectedRoom, orderDate)) {
            showOverwriteDialog(patientName, selectedWing, selectedRoom, selectedDiet, selectedFluidRestriction, orderDate);
        } else {
            saveFinalizedOrder(patientName, selectedWing, selectedRoom, selectedDiet, selectedFluidRestriction, orderDate, false);
        }
    }
    
    private void showOverwriteDialog(String patientName, String wing, String room, String diet, String fluidRestriction, String orderDate) {
        new AlertDialog.Builder(this)
            .setTitle("Order Already Exists")
            .setMessage("A menu already exists for " + wing + " - Room " + room + " on " + orderDate + 
                       ".\n\nWould you like to overwrite the existing order?")
            .setPositiveButton("Overwrite", (dialog, which) -> {
                saveFinalizedOrder(patientName, wing, room, diet, fluidRestriction, orderDate, true);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void saveFinalizedOrder(String patientName, String wing, String room, String diet, String fluidRestriction, String orderDate, boolean isOverwrite) {
        try {
            FinalizedOrder order = new FinalizedOrder();
            order.setPatientName(patientName);
            order.setWing(wing);
            order.setRoom(room);
            order.setOrderDate(orderDate);
            order.setDietType(diet);
            order.setFluidRestriction(fluidRestriction);
            
            // Set texture modifications
            order.setMechanicalGround(mechanicalGroundCB.isChecked());
            order.setMechanicalChopped(mechanicalChoppedCB.isChecked());
            order.setBiteSize(biteSizeCB.isChecked());
            order.setBreadOK(breadOKCB.isChecked());
            
            // Collect meal items
            collectMealItems(order);
            
            boolean success;
            if (isOverwrite) {
                success = finalizedOrderDAO.updateFinalizedOrder(order);
            } else {
                long result = finalizedOrderDAO.saveFinalizedOrder(order);
                success = result > 0;
            }
            
            if (success) {
                String message = isOverwrite ? "Order updated successfully!" : "Order finalized and saved successfully!";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                
                // Show success dialog with options
                showSuccessDialog();
            } else {
                Toast.makeText(this, "Failed to save order. Please try again.", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void collectMealItems(FinalizedOrder order) {
        // Breakfast items
        order.getBreakfastItems().addAll(getSelectedSpinnerItems(
            breakfastColdCereal, breakfastHotCereal, breakfastBread, breakfastMuffin, breakfastMain, breakfastFruit));
        
        // Lunch items  
        order.getLunchItems().addAll(getSelectedSpinnerItems(
            lunchProtein, lunchStarch, lunchVegetable, lunchDessert));
        
        // Dinner items
        order.getDinnerItems().addAll(getSelectedSpinnerItems(
            dinnerProtein, dinnerStarch, dinnerVegetable, dinnerDessert));
        
        // Collect juices and drinks from containers
        order.getBreakfastJuices().addAll(getDynamicItems(breakfastJuicesContainer));
        order.getLunchJuices().addAll(getDynamicItems(lunchJuicesContainer));
        order.getDinnerJuices().addAll(getDynamicItems(dinnerJuicesContainer));
        
        order.getBreakfastDrinks().addAll(getDynamicItems(breakfastDrinksContainer));
        order.getLunchDrinks().addAll(getDynamicItems(lunchDrinksContainer));
        order.getDinnerDrinks().addAll(getDynamicItems(dinnerDrinksContainer));
    }
    
    private List<String> getSelectedSpinnerItems(Spinner... spinners) {
        List<String> items = new ArrayList<>();
        for (Spinner spinner : spinners) {
            String selected = (String) spinner.getSelectedItem();
            if (selected != null && !selected.startsWith("Select") && !selected.trim().isEmpty()) {
                items.add(selected);
            }
        }
        return items;
    }
    
    private List<String> getDynamicItems(LinearLayout container) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout itemLayout = (LinearLayout) child;
                for (int j = 0; j < itemLayout.getChildCount(); j++) {
                    View subChild = itemLayout.getChildAt(j);
                    if (subChild instanceof Spinner) {
                        Spinner spinner = (Spinner) subChild;
                        String selected = (String) spinner.getSelectedItem();
                        if (selected != null && !selected.startsWith("Select") && !selected.trim().isEmpty()) {
                            items.add(selected);
                        }
                    }
                }
            }
        }
        return items;
    }
    
    private String getSelectedDate() {
        // Get selected day from radio group
        int selectedRadioId = dayGroup.getCheckedRadioButtonId();
        if (selectedRadioId != -1) {
            RadioButton selectedRadio = findViewById(selectedRadioId);
            return selectedRadio.getText().toString();
        } else {
            // Default to today's date if no day selected
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date());
        }
    }
    
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Order Finalized!")
            .setMessage("The dietary order has been successfully saved.")
            .setPositiveButton("View All Orders", (dialog, which) -> {
                Intent intent = new Intent(MainActivity.this, ViewOrdersActivity.class);
                startActivity(intent);
            })
            .setNegativeButton("Create New Order", (dialog, which) -> {
                clearForm(null);
            })
            .setNeutralButton("Done", null)
            .show();
    }
    
    // Add this method to navigate to View Orders (you can call this from a menu or button)
    public void viewOrders(View view) {
        Intent intent = new Intent(this, ViewOrdersActivity.class);
        startActivity(intent);
    }
    
    // ===== ORDER SUBMISSION (LEGACY - keeping for compatibility) =====
    
    public void submitOrder(View view) {
        // Validate required fields
        String patientName = patientNameInput.getText().toString().trim();
        String selectedWing = (String) wingSpinner.getSelectedItem();
        String selectedRoom = (String) roomSpinner.getSelectedItem();
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        
        if (patientName.isEmpty()) {
            patientNameInput.setError("Patient name is required");
            return;
        }
        
        if (selectedWing == null || selectedWing.equals("Select Wing")) {
            Toast.makeText(this, "Please select a wing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedRoom == null || selectedRoom.equals("Select Room")) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedDiet == null || selectedDiet.equals("Select Diet")) {
            Toast.makeText(this, "Please select a diet", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show order confirmation
        showOrderConfirmation(patientName, selectedWing, selectedRoom, selectedDiet);
    }
    
    private void showOrderConfirmation(String patientName, String wing, String room, String diet) {
        StringBuilder orderSummary = new StringBuilder();
        orderSummary.append("Patient: ").append(patientName).append("\n");
        orderSummary.append("Location: ").append(wing).append(" - Room ").append(room).append("\n");
        orderSummary.append("Diet: ").append(diet).append("\n\n");
        
        // Add selected meal items
        orderSummary.append("BREAKFAST:\n");
        addSelectedItem(orderSummary, breakfastColdCereal, "Cold Cereal");
        addSelectedItem(orderSummary, breakfastHotCereal, "Hot Cereal");
        addSelectedItem(orderSummary, breakfastBread, "Bread");
        addSelectedItem(orderSummary, breakfastMuffin, "Muffin");
        addSelectedItem(orderSummary, breakfastMain, "Main");
        addSelectedItem(orderSummary, breakfastFruit, "Fruit");
        
        orderSummary.append("\nLUNCH:\n");
        addSelectedItem(orderSummary, lunchProtein, "Protein");
        addSelectedItem(orderSummary, lunchStarch, "Starch");
        addSelectedItem(orderSummary, lunchVegetable, "Vegetable");
        addSelectedItem(orderSummary, lunchDessert, "Dessert");
        
        orderSummary.append("\nDINNER:\n");
        addSelectedItem(orderSummary, dinnerProtein, "Protein");
        addSelectedItem(orderSummary, dinnerStarch, "Starch");
        addSelectedItem(orderSummary, dinnerVegetable, "Vegetable");
        addSelectedItem(orderSummary, dinnerDessert, "Dessert");
        
        new AlertDialog.Builder(this)
            .setTitle("Order Summary")
            .setMessage(orderSummary.toString())
            .setPositiveButton("Confirm Order", (dialog, which) -> {
                // Here you would save the order to database
                Toast.makeText(this, "Order submitted successfully!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void addSelectedItem(StringBuilder summary, Spinner spinner, String category) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.startsWith("Select")) {
            summary.append("- ").append(category).append(": ").append(selected).append("\n");
        }
    }
    
    // ===== DEBUG METHODS (REMOVE IN PRODUCTION) =====
    
    private void debugDatabase() {
        // Test database connectivity
        List<Item> items = itemDAO.getAllItems();
        Toast.makeText(this, "Database loaded with " + items.size() + " items", Toast.LENGTH_SHORT).show();
    }
    
    private void createDebugButton() {
        // This is a temporary debug button - remove in production
        Button debugButton = new Button(this);
        debugButton.setText("Debug: Show All Items");
        debugButton.setOnClickListener(v -> {
            List<Item> allItems = itemDAO.getAllItems();
            StringBuilder itemList = new StringBuilder("All Items:\n\n");
            for (Item item : allItems) {
                itemList.append("• ").append(item.getName())
                       .append(" (").append(item.getCategoryName()).append(")")
                       .append(item.isAdaFriendly() ? " [ADA]" : "")
                       .append("\n");
            }
            
            new AlertDialog.Builder(this)
                .setTitle("Debug: Database Items")
                .setMessage(itemList.toString())
                .setPositiveButton("OK", null)
                .show();
        });
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}