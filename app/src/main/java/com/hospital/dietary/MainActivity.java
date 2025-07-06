// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/MainActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class MainActivity extends AppCompatActivity {
    
    // UI Components
    private RadioGroup dayGroup;
    private EditText patientNameInput;
    private Spinner wingSpinner, roomSpinner, dietSpinner, fluidRestrictionSpinner;
    
    // Texture modification checkboxes
    private CheckBox mechanicalGroundCB, mechanicalChoppedCB, biteSizeCB, breadOKCB;
    
    // Additional restrictions
    private CheckBox noDairyCB, paperServiceCB;
    
    // Breakfast section
    private LinearLayout breakfastSection;
    
    // Juice checkboxes and quantities
    private CheckBox orangeJuiceCB, cranberryJuiceCB, appleJuiceCB, pineappleJuiceCB, pruneJuiceCB;
    private LinearLayout orangeJuiceQtyLayout, cranberryJuiceQtyLayout, appleJuiceQtyLayout, pineappleJuiceQtyLayout, pruneJuiceQtyLayout;
    private EditText orangeJuiceQty, cranberryJuiceQty, appleJuiceQty, pineappleJuiceQty, pruneJuiceQty;
    
    // Drink checkboxes
    private CheckBox wholeMilkCB, twoPercentMilkCB, coffeeCB, decafCoffeeCB;
    private CheckBox hotTeaCB, hotChocolateCB, sugarFreeHotChocolateCB, iceTeaCB;
    
    // Cereal checkboxes
    private CheckBox raisinBranCB, cheeriosCB, honeyNutCheeriosCB, cornFlakesCB, riceKrispiesCB;
    private CheckBox oatmealCB, creamOfWheatCB;
    
    // Bread and muffin checkboxes
    private CheckBox biscuitCB, toastCB;
    private CheckBox bananaNutMuffinCB, blueberryMuffinCB;
    
    // Fruit checkboxes
    private CheckBox stewedPrunesCB, mixedFruitCB;
    
    // Main course container
    private LinearLayout breakfastMainCourseContainer;
    
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
    
    // Static diet menus
    private Map<String, Map<String, List<String>>> staticDietMenus = new HashMap<>();
    
    // Default items for each diet
    private Map<String, Map<String, Object>> defaultItems = new HashMap<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        
        // Initialize fluid tracking
        initializeFluidTracking();
        
        // Initialize static diet menus
        initializeStaticDietMenus();
        
        // Initialize default items
        initializeDefaultItems();
        
        // Initialize UI components
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load initial data
        loadInitialData();
    }
    
    private void initializeStaticDietMenus() {
        // Clear Liquid Diet Menu
        Map<String, List<String>> clearLiquidMenu = new HashMap<>();
        clearLiquidMenu.put("breakfast", Arrays.asList("Apple Juice", "Chicken Broth", "Jello", "Coffee", "Sprite"));
        clearLiquidMenu.put("lunch", Arrays.asList("Apple Juice", "Beef Broth", "Jello", "Ice Tea", "Sprite"));
        clearLiquidMenu.put("dinner", Arrays.asList("Cranberry Juice", "Chicken Broth", "Jello", "Ice Tea", "Sprite"));
        staticDietMenus.put("Clear Liquid", clearLiquidMenu);
        
        // Full Liquid Diet Menu
        Map<String, List<String>> fullLiquidMenu = new HashMap<>();
        fullLiquidMenu.put("breakfast", Arrays.asList("Apple Juice", "Jello", "Cream of Wheat", "Coffee", "Whole Milk", "Sprite", "Ensure - Vanilla"));
        fullLiquidMenu.put("lunch", Arrays.asList("Apple Juice", "Jello", "Cream of Mushroom Soup", "Vanilla Pudding", "Whole Milk", "Sprite", "Ensure - Vanilla"));
        fullLiquidMenu.put("dinner", Arrays.asList("Cranberry Juice", "Jello", "Tomato Soup", "Vanilla Pudding", "Whole Milk", "Sprite", "Ensure - Vanilla"));
        staticDietMenus.put("Full Liquid", fullLiquidMenu);
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
        
        // Add Cardiac defaults
        defaultItems.put("Cardiac", createDefaultMap(
            "Apple Juice", "Cheerios", "Oatmeal", "", "", "Fried Eggs", "Mixed Fruit",
            "Decaf Coffee|200", "LS Chicken Noodle Soup", "Baked Potato", "Steamed Broccoli", "Sugar Free Jello",
            "Bottled Water|355", "LS Tomato Soup", "Side Salad", "Green Beans", 
            "Sugar Free Vanilla Pudding", "Ice Tea|240"
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
        
        // Additional restrictions
        noDairyCB = findViewById(R.id.noDairyCB);
        paperServiceCB = findViewById(R.id.paperServiceCB);
        
        // Breakfast section
        breakfastSection = findViewById(R.id.breakfastSection);
        
        // Juice checkboxes and quantities
        orangeJuiceCB = findViewById(R.id.orangeJuiceCB);
        cranberryJuiceCB = findViewById(R.id.cranberryJuiceCB);
        appleJuiceCB = findViewById(R.id.appleJuiceCB);
        pineappleJuiceCB = findViewById(R.id.pineappleJuiceCB);
        pruneJuiceCB = findViewById(R.id.pruneJuiceCB);
        
        orangeJuiceQtyLayout = findViewById(R.id.orangeJuiceQtyLayout);
        cranberryJuiceQtyLayout = findViewById(R.id.cranberryJuiceQtyLayout);
        appleJuiceQtyLayout = findViewById(R.id.appleJuiceQtyLayout);
        pineappleJuiceQtyLayout = findViewById(R.id.pineappleJuiceQtyLayout);
        pruneJuiceQtyLayout = findViewById(R.id.pruneJuiceQtyLayout);
        
        orangeJuiceQty = findViewById(R.id.orangeJuiceQty);
        cranberryJuiceQty = findViewById(R.id.cranberryJuiceQty);
        appleJuiceQty = findViewById(R.id.appleJuiceQty);
        pineappleJuiceQty = findViewById(R.id.pineappleJuiceQty);
        pruneJuiceQty = findViewById(R.id.pruneJuiceQty);
        
        // Drink checkboxes
        wholeMilkCB = findViewById(R.id.wholeMilkCB);
        twoPercentMilkCB = findViewById(R.id.twoPercentMilkCB);
        coffeeCB = findViewById(R.id.coffeeCB);
        decafCoffeeCB = findViewById(R.id.decafCoffeeCB);
        hotTeaCB = findViewById(R.id.hotTeaCB);
        hotChocolateCB = findViewById(R.id.hotChocolateCB);
        sugarFreeHotChocolateCB = findViewById(R.id.sugarFreeHotChocolateCB);
        iceTeaCB = findViewById(R.id.iceTeaCB);
        
        // Cereal checkboxes
        raisinBranCB = findViewById(R.id.raisinBranCB);
        cheeriosCB = findViewById(R.id.cheeriosCB);
        honeyNutCheeriosCB = findViewById(R.id.honeyNutCheeriosCB);
        cornFlakesCB = findViewById(R.id.cornFlakesCB);
        riceKrispiesCB = findViewById(R.id.riceKrispiesCB);
        oatmealCB = findViewById(R.id.oatmealCB);
        creamOfWheatCB = findViewById(R.id.creamOfWheatCB);
        
        // Bread and muffin checkboxes
        biscuitCB = findViewById(R.id.biscuitCB);
        toastCB = findViewById(R.id.toastCB);
        bananaNutMuffinCB = findViewById(R.id.bananaNutMuffinCB);
        blueberryMuffinCB = findViewById(R.id.blueberryMuffinCB);
        
        // Fruit checkboxes
        stewedPrunesCB = findViewById(R.id.stewedPrunesCB);
        mixedFruitCB = findViewById(R.id.mixedFruitCB);
        
        // Main course container
        breakfastMainCourseContainer = findViewById(R.id.breakfastMainCourseContainer);
        
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
                String selectedDiet = (String) dietSpinner.getSelectedItem();
                
                // Handle texture modification visibility
                handleTextureModificationVisibility(selectedDiet);
                
                // Handle breakfast section visibility and functionality
                handleBreakfastSection(selectedDiet);
                
                // Handle static diet menus
                if (isStaticDiet(selectedDiet)) {
                    applyStaticDietMenu(selectedDiet);
                } else {
                    populateMealDropdowns();
                    
                    // Update breakfast restrictions based on diet
                    updateBreakfastRestrictions(selectedDiet);
                    
                    // Ask about applying defaults for non-static diets
                    if (selectedDiet != null && !selectedDiet.equals("Select Diet") && 
                        defaultItems.containsKey(selectedDiet)) {
                        
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Apply Defaults")
                            .setMessage("Would you like to apply default items for " + selectedDiet + " diet?")
                            .setPositiveButton("Yes", (dialog, which) -> applyBreakfastDefaults(selectedDiet))
                            .setNegativeButton("No", null)
                            .show();
                    }
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
        
        // Additional restrictions listener
        noDairyCB.setOnCheckedChangeListener((buttonView, isChecked) -> updateBreakfastRestrictions((String) dietSpinner.getSelectedItem()));
        
        // Texture modification listeners
        CompoundButton.OnCheckedChangeListener textureListener = (buttonView, isChecked) -> {
            String selectedDiet = (String) dietSpinner.getSelectedItem();
            if (!isStaticDiet(selectedDiet)) {
                populateMealDropdowns();
                updateBreakfastRestrictions(selectedDiet);
            }
        };
        mechanicalGroundCB.setOnCheckedChangeListener(textureListener);
        mechanicalChoppedCB.setOnCheckedChangeListener(textureListener);
        biteSizeCB.setOnCheckedChangeListener(textureListener);
        breadOKCB.setOnCheckedChangeListener(textureListener);
        
        // Juice checkbox listeners
        setupJuiceCheckboxListeners();
        
        // Drink checkbox listeners
        setupDrinkCheckboxListeners();
        
        // Juice quantity listeners
        setupJuiceQuantityListeners();
    }
    
    private void setupJuiceCheckboxListeners() {
        orangeJuiceCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            orangeJuiceQtyLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateBreakfastFluid();
        });
        
        cranberryJuiceCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cranberryJuiceQtyLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateBreakfastFluid();
        });
        
        appleJuiceCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appleJuiceQtyLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateBreakfastFluid();
        });
        
        pineappleJuiceCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            pineappleJuiceQtyLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateBreakfastFluid();
        });
        
        pruneJuiceCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            pruneJuiceQtyLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            updateBreakfastFluid();
        });
    }
    
    private void setupDrinkCheckboxListeners() {
        CompoundButton.OnCheckedChangeListener drinkListener = (buttonView, isChecked) -> updateBreakfastFluid();
        
        wholeMilkCB.setOnCheckedChangeListener(drinkListener);
        twoPercentMilkCB.setOnCheckedChangeListener(drinkListener);
        coffeeCB.setOnCheckedChangeListener(drinkListener);
        decafCoffeeCB.setOnCheckedChangeListener(drinkListener);
        hotTeaCB.setOnCheckedChangeListener(drinkListener);
        hotChocolateCB.setOnCheckedChangeListener(drinkListener);
        sugarFreeHotChocolateCB.setOnCheckedChangeListener(drinkListener);
        iceTeaCB.setOnCheckedChangeListener(drinkListener);
    }
    
    private void setupJuiceQuantityListeners() {
        TextWatcher quantityWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateBreakfastFluid();
            }
        };
        
        orangeJuiceQty.addTextChangedListener(quantityWatcher);
        cranberryJuiceQty.addTextChangedListener(quantityWatcher);
        appleJuiceQty.addTextChangedListener(quantityWatcher);
        pineappleJuiceQty.addTextChangedListener(quantityWatcher);
        pruneJuiceQty.addTextChangedListener(quantityWatcher);
    }
    
    private void handleBreakfastSection(String selectedDiet) {
        if (isStaticDiet(selectedDiet)) {
            // Hide breakfast section for static diets
            breakfastSection.setVisibility(View.GONE);
        } else {
            // Show breakfast section for regular diets
            breakfastSection.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateBreakfastRestrictions(String selectedDiet) {
        if (selectedDiet == null || isStaticDiet(selectedDiet)) return;
        
        boolean isADA = "ADA".equals(selectedDiet);
        boolean noDairy = noDairyCB.isChecked();
        boolean mechanicalGround = mechanicalGroundCB.isChecked();
        boolean mechanicalChopped = mechanicalChoppedCB.isChecked();
        boolean breadFiltered = (mechanicalGround || mechanicalChopped) && !breadOKCB.isChecked();
        
        // Gray out Orange Juice for ADA
        orangeJuiceCB.setEnabled(!isADA);
        orangeJuiceCB.setAlpha(isADA ? 0.5f : 1.0f);
        if (isADA && orangeJuiceCB.isChecked()) {
            orangeJuiceCB.setChecked(false);
        }
        
        // Gray out dairy items for ADA or No Dairy restriction
        wholeMilkCB.setEnabled(!isADA && !noDairy);
        wholeMilkCB.setAlpha((!isADA && !noDairy) ? 1.0f : 0.5f);
        if ((isADA || noDairy) && wholeMilkCB.isChecked()) {
            wholeMilkCB.setChecked(false);
        }
        
        twoPercentMilkCB.setEnabled(!noDairy);
        twoPercentMilkCB.setAlpha(!noDairy ? 1.0f : 0.5f);
        if (noDairy && twoPercentMilkCB.isChecked()) {
            twoPercentMilkCB.setChecked(false);
        }
        
        hotChocolateCB.setEnabled(!isADA && !noDairy);
        hotChocolateCB.setAlpha((!isADA && !noDairy) ? 1.0f : 0.5f);
        if ((isADA || noDairy) && hotChocolateCB.isChecked()) {
            hotChocolateCB.setChecked(false);
        }
        
        sugarFreeHotChocolateCB.setEnabled(!noDairy);
        sugarFreeHotChocolateCB.setAlpha(!noDairy ? 1.0f : 0.5f);
        if (noDairy && sugarFreeHotChocolateCB.isChecked()) {
            sugarFreeHotChocolateCB.setChecked(false);
        }
        
        // Gray out breads and muffins for texture modifications
        biscuitCB.setEnabled(!breadFiltered);
        biscuitCB.setAlpha(!breadFiltered ? 1.0f : 0.5f);
        if (breadFiltered && biscuitCB.isChecked()) {
            biscuitCB.setChecked(false);
        }
        
        toastCB.setEnabled(!breadFiltered);
        toastCB.setAlpha(!breadFiltered ? 1.0f : 0.5f);
        if (breadFiltered && toastCB.isChecked()) {
            toastCB.setChecked(false);
        }
        
        bananaNutMuffinCB.setEnabled(!breadFiltered);
        bananaNutMuffinCB.setAlpha(!breadFiltered ? 1.0f : 0.5f);
        if (breadFiltered && bananaNutMuffinCB.isChecked()) {
            bananaNutMuffinCB.setChecked(false);
        }
        
        blueberryMuffinCB.setEnabled(!breadFiltered);
        blueberryMuffinCB.setAlpha(!breadFiltered ? 1.0f : 0.5f);
        if (breadFiltered && blueberryMuffinCB.isChecked()) {
            blueberryMuffinCB.setChecked(false);
        }
    }
    
    private void updateBreakfastFluid() {
        int totalFluid = 0;
        
        // Calculate juice fluids
        if (orangeJuiceCB.isChecked()) {
            int qty = getQuantity(orangeJuiceQty);
            totalFluid += qty * 120; // 120ml per juice
        }
        if (cranberryJuiceCB.isChecked()) {
            int qty = getQuantity(cranberryJuiceQty);
            totalFluid += qty * 120;
        }
        if (appleJuiceCB.isChecked()) {
            int qty = getQuantity(appleJuiceQty);
            totalFluid += qty * 120;
        }
        if (pineappleJuiceCB.isChecked()) {
            int qty = getQuantity(pineappleJuiceQty);
            totalFluid += qty * 120;
        }
        if (pruneJuiceCB.isChecked()) {
            int qty = getQuantity(pruneJuiceQty);
            totalFluid += qty * 120;
        }
        
        // Calculate drink fluids
        if (wholeMilkCB.isChecked()) totalFluid += 240;
        if (twoPercentMilkCB.isChecked()) totalFluid += 240;
        if (coffeeCB.isChecked()) totalFluid += 200;
        if (decafCoffeeCB.isChecked()) totalFluid += 200;
        if (hotTeaCB.isChecked()) totalFluid += 240;
        if (hotChocolateCB.isChecked()) totalFluid += 240;
        if (sugarFreeHotChocolateCB.isChecked()) totalFluid += 240;
        if (iceTeaCB.isChecked()) totalFluid += 240;
        
        // Add main course container fluids (if any drinks are added there)
        totalFluid += calculateContainerFluid(breakfastMainCourseContainer);
        
        fluidUsed.put("breakfast", totalFluid);
        updateFluidTracker("breakfast", breakfastFluidTracker);
    }
    
    private int getQuantity(EditText qtyInput) {
        try {
            String text = qtyInput.getText().toString();
            if (text.isEmpty()) return 1;
            int qty = Integer.parseInt(text);
            return Math.max(1, qty); // Minimum 1
        } catch (NumberFormatException e) {
            return 1;
        }
    }
    
    public void addBreakfastMainCourse(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        
        // Prevent adding for static diets
        if (isStaticDiet(selectedDiet)) {
            Toast.makeText(this, "Cannot modify " + selectedDiet + " diet menu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean isADA = "ADA".equals(selectedDiet);
        boolean hasTextureModification = mechanicalGroundCB.isChecked() || 
                                       mechanicalChoppedCB.isChecked() || 
                                       biteSizeCB.isChecked();
        boolean breadOK = breadOKCB.isChecked();
        boolean filterBread = hasTextureModification && !breadOK;
        
        List<Item> breakfastItems = itemDAO.getFilteredItems(itemDAO.getBreakfastItems(), isADA, filterBread);
        addMainCourseToContainer(breakfastMainCourseContainer, breakfastItems);
    }
    
    private void addMainCourseToContainer(LinearLayout container, List<Item> items) {
        View mainCourseView = getLayoutInflater().inflate(R.layout.main_course_item, container, false);
        
        Spinner mainCourseSpinner = mainCourseView.findViewById(R.id.drinkSpinner);
        Button removeButton = mainCourseView.findViewById(R.id.removeButton);
        
        // Populate spinner
        populateSpinner(mainCourseSpinner, items);
        
        // Remove button listener
        removeButton.setOnClickListener(v -> container.removeView(mainCourseView));
        
        container.addView(mainCourseView);
    }
    
    private void applyBreakfastDefaults(String selectedDiet) {
        if (selectedDiet == null || !defaultItems.containsKey(selectedDiet) || isStaticDiet(selectedDiet)) {
            return;
        }
        
        Map<String, Object> defaults = defaultItems.get(selectedDiet);
        
        // Clear current breakfast selections
        clearBreakfastSelections();
        
        // Apply juice default
        String juiceDefault = (String) defaults.get("breakfastJuice");
        if (juiceDefault != null && !juiceDefault.isEmpty()) {
            switch (juiceDefault) {
                case "Orange Juice":
                    if (!"ADA".equals(selectedDiet)) orangeJuiceCB.setChecked(true);
                    break;
                case "Cranberry Juice":
                    cranberryJuiceCB.setChecked(true);
                    break;
                case "Apple Juice":
                    appleJuiceCB.setChecked(true);
                    break;
                case "Pineapple Juice":
                    pineappleJuiceCB.setChecked(true);
                    break;
                case "Prune Juice":
                    pruneJuiceCB.setChecked(true);
                    break;
            }
        }
        
        // Apply cereal defaults
        String coldCerealDefault = (String) defaults.get("breakfastColdCereal");
        if (coldCerealDefault != null && !coldCerealDefault.isEmpty()) {
            switch (coldCerealDefault) {
                case "Raisin Bran":
                    raisinBranCB.setChecked(true);
                    break;
                case "Cheerios":
                    cheeriosCB.setChecked(true);
                    break;
                case "Honey Nut Cheerios":
                    honeyNutCheeriosCB.setChecked(true);
                    break;
                case "Corn Flakes":
                    cornFlakesCB.setChecked(true);
                    break;
                case "Rice Krispies":
                    riceKrispiesCB.setChecked(true);
                    break;
            }
        }
        
        String hotCerealDefault = (String) defaults.get("breakfastHotCereal");
        if (hotCerealDefault != null && !hotCerealDefault.isEmpty()) {
            switch (hotCerealDefault) {
                case "Oatmeal":
                    oatmealCB.setChecked(true);
                    break;
                case "Cream of Wheat":
                    creamOfWheatCB.setChecked(true);
                    break;
            }
        }
        
        // Apply bread defaults
        String breadDefault = (String) defaults.get("breakfastBread");
        if (breadDefault != null && !breadDefault.isEmpty()) {
            switch (breadDefault) {
                case "Biscuit":
                    biscuitCB.setChecked(true);
                    break;
                case "Toast":
                    toastCB.setChecked(true);
                    break;
            }
        }
        
        // Apply fruit defaults
        String fruitDefault = (String) defaults.get("breakfastFruit");
        if (fruitDefault != null && !fruitDefault.isEmpty()) {
            switch (fruitDefault) {
                case "Stewed Prunes":
                    stewedPrunesCB.setChecked(true);
                    break;
                case "Mixed Fruit":
                    mixedFruitCB.setChecked(true);
                    break;
            }
        }
        
        // Apply drink defaults
        String drinkDefault = (String) defaults.get("breakfastDrink");
        if (drinkDefault != null && !drinkDefault.isEmpty()) {
            String[] parts = drinkDefault.split("\\|");
            String drinkName = parts[0];
            
            switch (drinkName) {
                case "Coffee":
                    coffeeCB.setChecked(true);
                    break;
                case "Decaf Coffee":
                    decafCoffeeCB.setChecked(true);
                    break;
                case "Hot Tea":
                    hotTeaCB.setChecked(true);
                    break;
                case "Ice Tea":
                    iceTeaCB.setChecked(true);
                    break;
                case "Whole Milk":
                    if (!"ADA".equals(selectedDiet) && !noDairyCB.isChecked()) wholeMilkCB.setChecked(true);
                    break;
                case "2% Milk":
                    if (!noDairyCB.isChecked()) twoPercentMilkCB.setChecked(true);
                    break;
                case "Hot Chocolate":
                    if (!"ADA".equals(selectedDiet) && !noDairyCB.isChecked()) hotChocolateCB.setChecked(true);
                    break;
                case "Sugar Free Hot Chocolate":
                    if (!noDairyCB.isChecked()) sugarFreeHotChocolateCB.setChecked(true);
                    break;
            }
        }
        
        // Apply main course default
        String mainDefault = (String) defaults.get("breakfastMain");
        if (mainDefault != null && !mainDefault.isEmpty()) {
            addBreakfastMainCourse(null);
            // Set the added main course
            View lastMain = breakfastMainCourseContainer.getChildAt(breakfastMainCourseContainer.getChildCount() - 1);
            if (lastMain != null) {
                Spinner mainSpinner = lastMain.findViewById(R.id.drinkSpinner);
                setSpinnerByValue(mainSpinner, mainDefault);
            }
        }
        
        // Apply lunch and dinner defaults as before
        setSpinnerByValue(lunchProtein, (String) defaults.get("lunchProtein"));
        setSpinnerByValue(lunchStarch, (String) defaults.get("lunchStarch"));
        setSpinnerByValue(lunchVegetable, (String) defaults.get("lunchVegetable"));
        setSpinnerByValue(lunchDessert, (String) defaults.get("lunchDessert"));
        
        setSpinnerByValue(dinnerProtein, (String) defaults.get("dinnerProtein"));
        setSpinnerByValue(dinnerStarch, (String) defaults.get("dinnerStarch"));
        setSpinnerByValue(dinnerVegetable, (String) defaults.get("dinnerVegetable"));
        setSpinnerByValue(dinnerDessert, (String) defaults.get("dinnerDessert"));
        
        // Apply drink defaults for lunch and dinner
        addDefaultDrink(lunchDrinksContainer, (String) defaults.get("lunchDrink"), "lunch");
        addDefaultDrink(dinnerDrinksContainer, (String) defaults.get("dinnerDrink"), "dinner");
        
        Toast.makeText(this, "Default items applied for " + selectedDiet + " diet", Toast.LENGTH_SHORT).show();
    }
    
    private void clearBreakfastSelections() {
        // Clear juice selections
        orangeJuiceCB.setChecked(false);
        cranberryJuiceCB.setChecked(false);
        appleJuiceCB.setChecked(false);
        pineappleJuiceCB.setChecked(false);
        pruneJuiceCB.setChecked(false);
        
        // Reset quantities
        orangeJuiceQty.setText("1");
        cranberryJuiceQty.setText("1");
        appleJuiceQty.setText("1");
        pineappleJuiceQty.setText("1");
        pruneJuiceQty.setText("1");
        
        // Clear drink selections
        wholeMilkCB.setChecked(false);
        twoPercentMilkCB.setChecked(false);
        coffeeCB.setChecked(false);
        decafCoffeeCB.setChecked(false);
        hotTeaCB.setChecked(false);
        hotChocolateCB.setChecked(false);
        sugarFreeHotChocolateCB.setChecked(false);
        iceTeaCB.setChecked(false);
        
        // Clear cereal selections
        raisinBranCB.setChecked(false);
        cheeriosCB.setChecked(false);
        honeyNutCheeriosCB.setChecked(false);
        cornFlakesCB.setChecked(false);
        riceKrispiesCB.setChecked(false);
        oatmealCB.setChecked(false);
        creamOfWheatCB.setChecked(false);
        
        // Clear bread and muffin selections
        biscuitCB.setChecked(false);
        toastCB.setChecked(false);
        bananaNutMuffinCB.setChecked(false);
        blueberryMuffinCB.setChecked(false);
        
        // Clear fruit selections
        stewedPrunesCB.setChecked(false);
        mixedFruitCB.setChecked(false);
        
        // Clear main course container
        breakfastMainCourseContainer.removeAllViews();
    }
    
    // [Continue with existing methods...]
    
    private void handleTextureModificationVisibility(String selectedDiet) {
        boolean isLiquidDiet = "Full Liquid".equals(selectedDiet) || "Clear Liquid".equals(selectedDiet);
        
        // Disable texture modifications for liquid diets
        mechanicalGroundCB.setEnabled(!isLiquidDiet);
        mechanicalChoppedCB.setEnabled(!isLiquidDiet);
        biteSizeCB.setEnabled(!isLiquidDiet);
        breadOKCB.setEnabled(!isLiquidDiet);
        
        // Clear selections if disabled
        if (isLiquidDiet) {
            mechanicalGroundCB.setChecked(false);
            mechanicalChoppedCB.setChecked(false);
            biteSizeCB.setChecked(false);
            breadOKCB.setChecked(false);
        }
        
        // Update appearance
        float alpha = isLiquidDiet ? 0.5f : 1.0f;
        mechanicalGroundCB.setAlpha(alpha);
        mechanicalChoppedCB.setAlpha(alpha);
        biteSizeCB.setAlpha(alpha);
        breadOKCB.setAlpha(alpha);
    }
    
    private boolean isStaticDiet(String dietName) {
        return "Clear Liquid".equals(dietName) || "Full Liquid".equals(dietName);
    }
    
    private boolean isADADiet(String dietName) {
        return "ADA".equals(dietName);
    }
    
    private void applyStaticDietMenu(String dietName) {
        // Clear existing selections first
        clearMealSelections();
        
        // Get the static menu for this diet
        Map<String, List<String>> menu = staticDietMenus.get(dietName);
        if (menu == null) return;
        
        boolean isADA = isADADiet(dietName);
        
        // Apply breakfast menu
        applyStaticMealItems("breakfast", menu.get("breakfast"), isADA);
        
        // Apply lunch menu
        applyStaticMealItems("lunch", menu.get("lunch"), isADA);
        
        // Apply dinner menu
        applyStaticMealItems("dinner", menu.get("dinner"), isADA);
        
        Toast.makeText(this, "Static menu applied for " + dietName + " diet" + (isADA ? " (ADA)" : ""), Toast.LENGTH_SHORT).show();
    }
    
    private void applyStaticMealItems(String meal, List<String> items, boolean isADA) {
        for (String itemName : items) {
            // Handle ADA substitutions
            String finalItemName = getADASubstitution(itemName, isADA);
            
            // Determine where to place the item based on its type
            if (isJuiceItem(finalItemName)) {
                if (meal.equals("breakfast")) {
                    addStaticJuice(finalItemName);
                } else {
                    addStaticDrink(meal, finalItemName);
                }
            } else if (isDrinkItem(finalItemName)) {
                addStaticDrink(meal, finalItemName);
            } else if (isCerealItem(finalItemName)) {
                setStaticSpinnerItem(null, finalItemName); // Will be handled by static diet logic
            } else if (isSoupItem(finalItemName)) {
                if (meal.equals("lunch")) {
                    setStaticSpinnerItem(lunchProtein, finalItemName);
                } else if (meal.equals("dinner")) {
                    setStaticSpinnerItem(dinnerProtein, finalItemName);
                }
            } else if (isDessertItem(finalItemName)) {
                if (meal.equals("lunch")) {
                    setStaticSpinnerItem(lunchDessert, finalItemName);
                } else if (meal.equals("dinner")) {
                    setStaticSpinnerItem(dinnerDessert, finalItemName);
                }
            } else if (isSupplementItem(finalItemName)) {
                addStaticDrink(meal, finalItemName);
            }
        }
    }
    
    // [Continue with all the existing helper methods from the previous version...]
    
    private String getADASubstitution(String itemName, boolean isADA) {
        if (!isADA) return itemName;
        
        // ADA substitutions
        switch (itemName) {
            case "Jello": return "Sugar Free Jello";
            case "Sprite": return "Sprite Zero";
            case "Whole Milk": return "2% Milk";
            case "Vanilla Pudding": return "Sugar Free Vanilla Pudding";
            case "Chocolate Pudding": return "Sugar Free Chocolate Pudding";
            default: return itemName;
        }
    }
    
    private boolean isJuiceItem(String itemName) {
        return itemName.contains("Juice");
    }
    
    private boolean isDrinkItem(String itemName) {
        return itemName.equals("Coffee") || itemName.equals("Ice Tea") || itemName.contains("Sprite") || 
               itemName.contains("Milk") || itemName.contains("Broth");
    }
    
    private boolean isCerealItem(String itemName) {
        return itemName.contains("Cream of Wheat");
    }
    
    private boolean isSoupItem(String itemName) {
        return itemName.contains("Soup") || itemName.contains("Broth");
    }
    
    private boolean isDessertItem(String itemName) {
        return itemName.contains("Jello") || itemName.contains("Pudding");
    }
    
    private boolean isSupplementItem(String itemName) {
        return itemName.contains("Ensure");
    }
    
    // [Include all remaining methods from previous version...]
    
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
        
        // Skip if this is a static diet
        if (isStaticDiet(selectedDiet)) {
            return;
        }
        
        boolean isADA = "ADA".equals(selectedDiet);
        
        // Check texture modifications
        boolean hasTextureModification = mechanicalGroundCB.isChecked() || 
                                       mechanicalChoppedCB.isChecked() || 
                                       biteSizeCB.isChecked();
        boolean breadOK = breadOKCB.isChecked();
        boolean filterBread = hasTextureModification && !breadOK;
        
        // Get items from database with filtering
        List<Item> proteins = itemDAO.getFilteredItems(itemDAO.getProteinItems(), isADA, filterBread);
        List<Item> starches = itemDAO.getFilteredItems(itemDAO.getStarchItems(), isADA, filterBread);
        List<Item> vegetables = itemDAO.getFilteredItems(itemDAO.getVegetableItems(), isADA, filterBread);
        List<Item> desserts = itemDAO.getFilteredItems(itemDAO.getDessertItems(), isADA, filterBread);
        
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
        
        // Re-enable spinners for non-static diets
        enableSpinnersForNormalDiets();
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
    
    private void enableSpinnersForNormalDiets() {
        lunchProtein.setEnabled(true);
        lunchStarch.setEnabled(true);
        lunchVegetable.setEnabled(true);
        lunchDessert.setEnabled(true);
        
        dinnerProtein.setEnabled(true);
        dinnerStarch.setEnabled(true);
        dinnerVegetable.setEnabled(true);
        dinnerDessert.setEnabled(true);
        
        // Reset alpha
        lunchProtein.setAlpha(1.0f);
        lunchStarch.setAlpha(1.0f);
        lunchVegetable.setAlpha(1.0f);
        lunchDessert.setAlpha(1.0f);
        
        dinnerProtein.setAlpha(1.0f);
        dinnerStarch.setAlpha(1.0f);
        dinnerVegetable.setAlpha(1.0f);
        dinnerDessert.setAlpha(1.0f);
    }
    
    // [Continue with remaining methods - updateFluidLimits, addLunchDrink, addDinnerDrink, etc.]
    // [For brevity, I'll include the essential methods for the new breakfast functionality]
    
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
    
    // [Include remaining methods from previous version - addLunchDrink, addDinnerDrink, etc.]
    
    public void addLunchDrink(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        
        // Prevent adding drinks for static diets
        if (isStaticDiet(selectedDiet)) {
            Toast.makeText(this, "Cannot modify " + selectedDiet + " diet menu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean isADA = "ADA".equals(selectedDiet);
        
        List<Item> drinks = itemDAO.getFilteredItems(itemDAO.getDrinkItems(), isADA, false);
        drinks.addAll(itemDAO.getFilteredItems(itemDAO.getJuiceItems(), isADA, false));
        addDrinkToContainer(lunchDrinksContainer, drinks, "lunch");
    }
    
    public void addDinnerDrink(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        
        // Prevent adding drinks for static diets
        if (isStaticDiet(selectedDiet)) {
            Toast.makeText(this, "Cannot modify " + selectedDiet + " diet menu", Toast.LENGTH_SHORT).show();
            return;
        }
        
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
                updateBreakfastFluid(); // Use the special breakfast fluid calculation
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
    
    // [Include remaining helper methods...]
    
    private void setSpinnerByValue(Spinner spinner, String value) {
        if (value == null || value.isEmpty() || spinner == null) return;
        
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter == null) return;
        
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = (String) adapter.getItem(i);
            if (item != null && item.startsWith(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    
    // [Continue with validation, saving, and other remaining methods...]
    
    public void applyDefaults(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        
        // Prevent applying defaults for static diets
        if (isStaticDiet(selectedDiet)) {
            Toast.makeText(this, "Cannot apply defaults to " + selectedDiet + " diet - menu is predetermined", Toast.LENGTH_SHORT).show();
            return;
        }
        
        applyBreakfastDefaults(selectedDiet);
    }
    
    public void clearForm(View view) {
        clearForm();
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
        noDairyCB.setChecked(false);
        paperServiceCB.setChecked(false);
        
        // Re-enable texture modifications
        mechanicalGroundCB.setEnabled(true);
        mechanicalChoppedCB.setEnabled(true);
        biteSizeCB.setEnabled(true);
        breadOKCB.setEnabled(true);
        mechanicalGroundCB.setAlpha(1.0f);
        mechanicalChoppedCB.setAlpha(1.0f);
        biteSizeCB.setAlpha(1.0f);
        breadOKCB.setAlpha(1.0f);
        
        // Clear breakfast selections
        clearBreakfastSelections();
        
        // Clear lunch and dinner selections
        clearMealSelections();
        
        // Re-enable all spinners
        enableSpinnersForNormalDiets();
        
        // Show breakfast section
        breakfastSection.setVisibility(View.VISIBLE);
    }
    
    private void clearMealSelections() {
        // Clear lunch and dinner spinners
        if (lunchProtein.getAdapter() != null) lunchProtein.setSelection(0);
        if (lunchStarch.getAdapter() != null) lunchStarch.setSelection(0);
        if (lunchVegetable.getAdapter() != null) lunchVegetable.setSelection(0);
        if (lunchDessert.getAdapter() != null) lunchDessert.setSelection(0);
        
        if (dinnerProtein.getAdapter() != null) dinnerProtein.setSelection(0);
        if (dinnerStarch.getAdapter() != null) dinnerStarch.setSelection(0);
        if (dinnerVegetable.getAdapter() != null) dinnerVegetable.setSelection(0);
        if (dinnerDessert.getAdapter() != null) dinnerDessert.setSelection(0);
        
        // Clear drink containers
        lunchDrinksContainer.removeAllViews();
        dinnerDrinksContainer.removeAllViews();
        
        // Reset fluid tracking
        initializeFluidTracking();
        updateFluidTrackers();
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
        if (meal.equals("breakfast")) {
            saveBreakfastItems(orderId);
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
    
    private void saveBreakfastItems(int orderId) {
        // Save juice selections
        if (orangeJuiceCB.isChecked()) {
            int qty = getQuantity(orangeJuiceQty);
            for (int i = 0; i < qty; i++) {
                saveItemByName(orderId, "Orange Juice");
            }
        }
        if (cranberryJuiceCB.isChecked()) {
            int qty = getQuantity(cranberryJuiceQty);
            for (int i = 0; i < qty; i++) {
                saveItemByName(orderId, "Cranberry Juice");
            }
        }
        if (appleJuiceCB.isChecked()) {
            int qty = getQuantity(appleJuiceQty);
            for (int i = 0; i < qty; i++) {
                saveItemByName(orderId, "Apple Juice");
            }
        }
        if (pineappleJuiceCB.isChecked()) {
            int qty = getQuantity(pineappleJuiceQty);
            for (int i = 0; i < qty; i++) {
                saveItemByName(orderId, "Pineapple Juice");
            }
        }
        if (pruneJuiceCB.isChecked()) {
            int qty = getQuantity(pruneJuiceQty);
            for (int i = 0; i < qty; i++) {
                saveItemByName(orderId, "Prune Juice");
            }
        }
        
        // Save drink selections
        if (wholeMilkCB.isChecked()) saveItemByName(orderId, "Whole Milk");
        if (twoPercentMilkCB.isChecked()) saveItemByName(orderId, "2% Milk");
        if (coffeeCB.isChecked()) saveItemByName(orderId, "Coffee");
        if (decafCoffeeCB.isChecked()) saveItemByName(orderId, "Decaf Coffee");
        if (hotTeaCB.isChecked()) saveItemByName(orderId, "Hot Tea");
        if (hotChocolateCB.isChecked()) saveItemByName(orderId, "Hot Chocolate");
        if (sugarFreeHotChocolateCB.isChecked()) saveItemByName(orderId, "Sugar Free Hot Chocolate");
        if (iceTeaCB.isChecked()) saveItemByName(orderId, "Ice Tea");
        
        // Save cereal selections
        if (raisinBranCB.isChecked()) saveItemByName(orderId, "Raisin Bran");
        if (cheeriosCB.isChecked()) saveItemByName(orderId, "Cheerios");
        if (honeyNutCheeriosCB.isChecked()) saveItemByName(orderId, "Honey Nut Cheerios");
        if (cornFlakesCB.isChecked()) saveItemByName(orderId, "Corn Flakes");
        if (riceKrispiesCB.isChecked()) saveItemByName(orderId, "Rice Krispies");
        if (oatmealCB.isChecked()) saveItemByName(orderId, "Oatmeal");
        if (creamOfWheatCB.isChecked()) saveItemByName(orderId, "Cream of Wheat");
        
        // Save bread selections
        if (biscuitCB.isChecked()) saveItemByName(orderId, "Biscuit");
        if (toastCB.isChecked()) saveItemByName(orderId, "Toast");
        
        // Save muffin selections
        if (bananaNutMuffinCB.isChecked()) saveItemByName(orderId, "Banana Nut Muffin");
        if (blueberryMuffinCB.isChecked()) saveItemByName(orderId, "Blueberry Muffin");
        
        // Save fruit selections
        if (stewedPrunesCB.isChecked()) saveItemByName(orderId, "Stewed Prunes");
        if (mixedFruitCB.isChecked()) saveItemByName(orderId, "Mixed Fruit");
        
        // Save main course container items
        saveContainerItems(orderId, breakfastMainCourseContainer);
    }
    
    private void saveItemByName(int orderId, String itemName) {
        Item item = itemDAO.getItemByName(itemName);
        if (item != null) {
            itemDAO.saveMealLine(orderId, item.getItemId());
        }
    }
    
    private void saveSpinnerItem(int orderId, Spinner spinner) {
        if (spinner.getSelectedItemPosition() > 0) {
            String itemName = (String) spinner.getSelectedItem();
            // Remove size information to get clean item name
            if (itemName.contains("(")) {
                itemName = itemName.substring(0, itemName.indexOf("(")).trim();
            }
            saveItemByName(orderId, itemName);
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
                saveItemByName(orderId, itemName);
            }
        }
    }
    
    private void addDefaultDrink(LinearLayout container, String drinkDefault, String meal) {
        if (drinkDefault == null || drinkDefault.isEmpty()) return;
        
        String[] parts = drinkDefault.split("\\|");
        String drinkName = parts[0];
        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 240;
        
        // Add drink container first
        if (meal.equals("lunch")) {
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
    
    private void addStaticJuice(String juiceName) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = isADADiet(selectedDiet);
        
        List<Item> juices = itemDAO.getFilteredItems(itemDAO.getJuiceItems(), isADA, false);
        addStaticDrinkToContainer(lunchDrinksContainer, juices, "lunch", juiceName); // Static diets don't use breakfast section
    }
    
    private void addStaticDrink(String meal, String drinkName) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = isADADiet(selectedDiet);
        
        List<Item> drinks = itemDAO.getFilteredItems(itemDAO.getDrinkItems(), isADA, false);
        drinks.addAll(itemDAO.getFilteredItems(itemDAO.getJuiceItems(), isADA, false));
        drinks.addAll(itemDAO.getDrinkItems()); // Add supplements
        
        LinearLayout container;
        switch (meal) {
            case "lunch":
                container = lunchDrinksContainer;
                break;
            case "dinner":
                container = dinnerDrinksContainer;
                break;
            default:
                return;
        }
        
        addStaticDrinkToContainer(container, drinks, meal, drinkName);
    }
    
    private void addStaticDrinkToContainer(LinearLayout container, List<Item> drinks, String meal, String drinkName) {
        View drinkView = getLayoutInflater().inflate(R.layout.drink_item, container, false);
        
        Spinner drinkSpinner = drinkView.findViewById(R.id.drinkSpinner);
        EditText amountInput = drinkView.findViewById(R.id.amountInput);
        Button removeButton = drinkView.findViewById(R.id.removeButton);
        
        // Populate spinner
        populateSpinner(drinkSpinner, drinks);
        
        // Find and select the specific drink
        setSpinnerByValue(drinkSpinner, drinkName);
        
        // Set amount automatically
        Item selectedItem = findItemByName(drinks, drinkName);
        if (selectedItem != null && selectedItem.getSizeML() != null) {
            int requestedAmount = selectedItem.getSizeML();
            int adjustedAmount = adjustFluidForLimit(meal, requestedAmount);
            amountInput.setText(String.valueOf(adjustedAmount));
            amountInput.setEnabled(true);
        }
        
        // Disable modification for static diets
        drinkSpinner.setEnabled(false);
        removeButton.setEnabled(false);
        removeButton.setAlpha(0.5f);
        
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
        
        container.addView(drinkView);
        updateMealFluid(meal);
    }
    
    private void setStaticSpinnerItem(Spinner spinner, String itemName) {
        if (spinner == null) return; // For static diets, some items don't go in spinners
        
        // Populate spinner first if not already populated
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = isADADiet(selectedDiet);
        
        List<Item> items = new ArrayList<>();
        if (spinner == lunchProtein || spinner == dinnerProtein) {
            items = itemDAO.getFilteredItems(itemDAO.getProteinItems(), isADA, false);
        } else if (spinner == lunchDessert || spinner == dinnerDessert) {
            items = itemDAO.getFilteredItems(itemDAO.getDessertItems(), isADA, false);
        }
        
        populateSpinner(spinner, items);
        setSpinnerByValue(spinner, itemName);
        spinner.setEnabled(false); // Disable modification
        spinner.setAlpha(0.7f); // Visual indication it's disabled
    }
    
    private Item findItemByName(List<Item> items, String name) {
        for (Item item : items) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
