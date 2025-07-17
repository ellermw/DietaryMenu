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
import com.hospital.dietary.dao.OrderDAO;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.Item;
import com.hospital.dietary.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    
    // UI Components
    private RadioGroup dayGroup;
    private RadioButton radioSunday, radioMonday, radioTuesday, radioWednesday, radioThursday, radioFriday, radioSaturday;
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
    private UserDAO userDAO;
    private OrderDAO orderDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    private boolean isAdmin = false;
    
    // Data lists
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "ADA", "Diabetic", "Low Sodium", 
                                              "Cardiac", "Renal", 
                                              "Puree", "Full Liquid", "Clear Liquid");
    
    // Default items for each diet
    private Map<String, Map<String, Object>> defaultItems = new HashMap<>();

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
        userDAO = new UserDAO(dbHelper);
        orderDAO = new OrderDAO(dbHelper);
        
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
        
        // Show welcome message
        if (currentUserFullName != null) {
            Toast.makeText(this, "Welcome, " + currentUserFullName + "!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show admin panel for admin users
        if (isAdmin) {
            menu.add(0, 1, 0, "Admin Panel")
                .setIcon(android.R.drawable.ic_menu_manage)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        
        // View Orders option for all users
        menu.add(0, 2, 0, "View Orders")
            .setIcon(android.R.drawable.ic_menu_view)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            
        // Logout option for all users
        menu.add(0, 3, 0, "Logout")
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Admin Panel
                if (isAdmin) {
                    Intent intent = new Intent(this, AdminActivity.class);
                    intent.putExtra("current_user", currentUsername);
                    startActivity(intent);
                }
                return true;
            case 2: // View Orders
                Intent intent = new Intent(this, ViewOrdersActivity.class);
                intent.putExtra("current_username", currentUsername);
                intent.putExtra("is_admin", isAdmin);
                startActivity(intent);
                return true;
            case 3: // Logout
                confirmLogout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void confirmLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // ===== UI INITIALIZATION =====
    
    private void initializeUI() {
        // Main form components
        dayGroup = findViewById(R.id.dayGroup);
        patientNameInput = findViewById(R.id.patientNameInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomSpinner = findViewById(R.id.roomSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        
        // Initialize day selection with current day as default
        initializeDaySelection();
        
        // Texture modification checkboxes
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        
        // Breakfast components
        breakfastColdCereal = findViewById(R.id.breakfastColdCereal);
        breakfastHotCereal = findViewById(R.id.breakfastHotCereal);
        breakfastBread = findViewById(R.id.breakfastBread);
        breakfastMuffin = findViewById(R.id.breakfastMuffin);
        breakfastMain = findViewById(R.id.breakfastMain);
        breakfastFruit = findViewById(R.id.breakfastFruit);
        breakfastJuicesContainer = findViewById(R.id.breakfastJuicesContainer);
        breakfastDrinksContainer = findViewById(R.id.breakfastDrinksContainer);
        
        // Lunch components
        lunchProtein = findViewById(R.id.lunchProtein);
        lunchStarch = findViewById(R.id.lunchStarch);
        lunchVegetable = findViewById(R.id.lunchVegetable);
        lunchDessert = findViewById(R.id.lunchDessert);
        lunchDrinksContainer = findViewById(R.id.lunchDrinksContainer);
        
        // Dinner components
        dinnerProtein = findViewById(R.id.dinnerProtein);
        dinnerStarch = findViewById(R.id.dinnerStarch);
        dinnerVegetable = findViewById(R.id.dinnerVegetable);
        dinnerDessert = findViewById(R.id.dinnerDessert);
        dinnerDrinksContainer = findViewById(R.id.dinnerDrinksContainer);
        
        // Fluid tracking
        breakfastFluidTracker = findViewById(R.id.breakfastFluidTracker);
        lunchFluidTracker = findViewById(R.id.lunchFluidTracker);
        dinnerFluidTracker = findViewById(R.id.dinnerFluidTracker);
    }
    
    // ===== DAY SELECTION METHODS =====
    
    private void initializeDaySelection() {
        // Initialize individual RadioButton references
        radioSunday = findViewById(R.id.radioSunday);
        radioMonday = findViewById(R.id.radioMonday);
        radioTuesday = findViewById(R.id.radioTuesday);
        radioWednesday = findViewById(R.id.radioWednesday);
        radioThursday = findViewById(R.id.radioThursday);
        radioFriday = findViewById(R.id.radioFriday);
        radioSaturday = findViewById(R.id.radioSaturday);
        
        // Set default selection to current day
        setCurrentDayAsDefault();
    }
    
    private void setCurrentDayAsDefault() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        // Calendar.DAY_OF_WEEK: Sunday = 1, Monday = 2, ... Saturday = 7
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                radioSunday.setChecked(true);
                break;
            case Calendar.MONDAY:
                radioMonday.setChecked(true);
                break;
            case Calendar.TUESDAY:
                radioTuesday.setChecked(true);
                break;
            case Calendar.WEDNESDAY:
                radioWednesday.setChecked(true);
                break;
            case Calendar.THURSDAY:
                radioThursday.setChecked(true);
                break;
            case Calendar.FRIDAY:
                radioFriday.setChecked(true);
                break;
            case Calendar.SATURDAY:
                radioSaturday.setChecked(true);
                break;
            default:
                // Default to Sunday if something goes wrong
                radioSunday.setChecked(true);
                break;
        }
    }
    
    private String getSelectedDay() {
        int selectedId = dayGroup.getCheckedRadioButtonId();
        
        if (selectedId == R.id.radioSunday) return "Sunday";
        else if (selectedId == R.id.radioMonday) return "Monday";
        else if (selectedId == R.id.radioTuesday) return "Tuesday";
        else if (selectedId == R.id.radioWednesday) return "Wednesday";
        else if (selectedId == R.id.radioThursday) return "Thursday";
        else if (selectedId == R.id.radioFriday) return "Friday";
        else if (selectedId == R.id.radioSaturday) return "Saturday";
        else return "Unknown";
    }
    
    private void setupListeners() {
        // Wing selection listener
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRoomNumbers();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Diet selection listener
        dietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = (String) parent.getItemAtPosition(position);
                filterItemsByDiet(selectedDiet);
                
                // Apply defaults only for admin users
                if (isAdmin) {
                    applyDefaults(selectedDiet);
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
        
        // Setup fluid tracking for drink containers
        setupFluidTracking();
    }
    
    // ===== INITIALIZATION METHODS =====
    
    private void initializeFluidTracking() {
        fluidUsed.put("breakfast", 0);
        fluidUsed.put("lunch", 0);
        fluidUsed.put("dinner", 0);
        
        fluidLimits.put("breakfast", 9999); // No limit by default
        fluidLimits.put("lunch", 9999);
        fluidLimits.put("dinner", 9999);
    }
    
    private void initializeDefaultItems() {
        // Initialize default items for each diet type
        // (This would be populated from database or preferences)
        // Only admins can modify defaults
    }
    
    private void loadInitialData() {
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
    
    // UPDATED: Room numbers with correct ranges
    private void updateRoomNumbers() {
        String selectedWing = (String) wingSpinner.getSelectedItem();
        List<String> rooms = new ArrayList<>();
        
        if (selectedWing != null && !selectedWing.equals("Select Wing")) {
            // Generate room numbers based on wing with correct ranges
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
                    // Rooms ICU1 through ICU13
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
    
    private void loadFluidRestrictions() {
        List<String> restrictions = Arrays.asList("None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, restrictions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(adapter);
    }
    
    private void loadMenuItems() {
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
        
        loadDrinkItems();
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
    
    private void loadDrinkItems() {
        // Don't pre-load any drinks - containers start empty
        // Only the "+ Add" buttons will be visible initially
    }
    
    private void loadDrinksForMeal(LinearLayout container, String category) {
        // Keep containers empty initially - user will add drinks via buttons
        container.removeAllViews();
    }
    
    // ===== DIET FILTERING METHODS =====
    
    // UPDATED: Handle Clear Liquid diet filtering
    private void filterItemsByDiet(String selectedDiet) {
        boolean isADA = selectedDiet.equals("ADA") || selectedDiet.equals("Diabetic");
        boolean isClearLiquid = selectedDiet.equals("Clear Liquid");
        
        // First, always re-enable all spinners
        enableAllSpinners();
        
        if (isADA) {
            filterSpinnerForADA(lunchDessert);
            filterSpinnerForADA(dinnerDessert);
            // Reload other spinners normally for ADA
            loadSpinnerItems(breakfastColdCereal, "Cold Cereals", true);
            loadSpinnerItems(breakfastHotCereal, "Hot Cereals", true);
            loadSpinnerItems(breakfastBread, "Breads", true);
            loadSpinnerItems(breakfastMuffin, "Fresh Muffins", true);
            loadSpinnerItems(breakfastMain, "Breakfast", true);
            loadSpinnerItems(breakfastFruit, "Fruits", true);
            loadSpinnerItems(lunchProtein, "Protein/Entrée", true);
            loadSpinnerItems(lunchStarch, "Starch", true);
            loadSpinnerItems(lunchVegetable, "Vegetable", true);
            loadSpinnerItems(dinnerProtein, "Protein/Entrée", true);
            loadSpinnerItems(dinnerStarch, "Starch", true);
            loadSpinnerItems(dinnerVegetable, "Vegetable", true);
        } else if (isClearLiquid) {
            // For Clear Liquid diet, disable most food spinners
            setClearLiquidSpinners();
        } else {
            // Reload all items for non-restricted diets
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
    
    // NEW: Set all food spinners to "None" only for Clear Liquid diet
    private void setClearLiquidSpinners() {
        // For Clear Liquid diet, most food items are not allowed
        // Set all food spinners to only show "None" and disable them
        List<String> noneOnly = Arrays.asList("None");
        
        // Breakfast food items - all set to "None" only
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
    
    // NEW: Re-enable all spinners when switching away from Clear Liquid
    private void enableAllSpinners() {
        // Re-enable all spinners when switching away from Clear Liquid diet
        breakfastColdCereal.setEnabled(true);
        breakfastHotCereal.setEnabled(true);
        breakfastBread.setEnabled(true);
        breakfastMuffin.setEnabled(true);
        breakfastMain.setEnabled(true);
        breakfastFruit.setEnabled(true);
        
        lunchProtein.setEnabled(true);
        lunchStarch.setEnabled(true);
        lunchVegetable.setEnabled(true);
        lunchDessert.setEnabled(true);
        
        dinnerProtein.setEnabled(true);
        dinnerStarch.setEnabled(true);
        dinnerVegetable.setEnabled(true);
        dinnerDessert.setEnabled(true);
    }
    
    private void filterSpinnerForADA(Spinner spinner) {
        List<Item> desserts = itemDAO.getItemsByCategory("Dessert");
        List<String> adaFriendlyDesserts = new ArrayList<>();
        adaFriendlyDesserts.add("None");
        
        for (Item item : desserts) {
            if (item.isAdaFriendly()) {
                adaFriendlyDesserts.add(item.getName());
            }
        }
        
        // Add sugar-free desserts
        List<Item> sugarFreeDesserts = itemDAO.getItemsByCategory("Sugar Free Dessert");
        for (Item item : sugarFreeDesserts) {
            adaFriendlyDesserts.add(item.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, adaFriendlyDesserts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    // UPDATED: Apply defaults including Clear Liquid
    private void applyDefaults(String diet) {
        // Only admins can apply defaults
        if (!isAdmin) {
            return;
        }
        
        // Apply default selections based on diet
        if (diet.equals("Clear Liquid")) {
            applyClearLiquidDefaults();
        }
        // Add other diet defaults as needed
    }
    
    // NEW: Apply Clear Liquid defaults
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
    
    // NEW: Clear Liquid breakfast defaults
    private void addClearLiquidBreakfastDefaults() {
        // Check if ADA is selected for alternative items
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = selectedDiet != null && selectedDiet.contains("ADA");
        
        // Coffee or Decaf Coffee (200ml)
        addDefaultDrinkItem("breakfast", "Coffee", 200, breakfastDrinksContainer, false);
        
        // Chicken Broth (200ml)  
        addDefaultDrinkItem("breakfast", "Chicken Broth", 200, breakfastDrinksContainer, false);
        
        // Orange Juice (120ml) or Apple Juice (120ml) if ADA
        String juiceChoice = isADA ? "Apple Juice" : "Orange Juice";
        addDefaultDrinkItem("breakfast", juiceChoice, 120, breakfastJuicesContainer, false);
        
        // Jello or Sugar Free Jello if ADA
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addDefaultDrinkItem("breakfast", jelloChoice, 0, breakfastDrinksContainer, false);
        
        // Sprite or Sprite Zero if ADA
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addDefaultDrinkItem("breakfast", spriteChoice, 240, breakfastDrinksContainer, false);
    }
    
    // NEW: Clear Liquid lunch defaults
    private void addClearLiquidLunchDefaults() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = selectedDiet != null && selectedDiet.contains("ADA");
        
        // Cranberry Juice (120ml)
        addDefaultDrinkItem("lunch", "Cranberry Juice", 120, lunchDrinksContainer, false);
        
        // Beef Broth (200ml)
        addDefaultDrinkItem("lunch", "Beef Broth", 200, lunchDrinksContainer, false);
        
        // Jello or Sugar Free Jello if ADA
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addDefaultDrinkItem("lunch", jelloChoice, 0, lunchDrinksContainer, false);
        
        // Ice Tea (240ml)
        addDefaultDrinkItem("lunch", "Ice Tea", 240, lunchDrinksContainer, false);
        
        // Sprite or Sprite Zero if ADA
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addDefaultDrinkItem("lunch", spriteChoice, 240, lunchDrinksContainer, false);
    }
    
    // NEW: Clear Liquid dinner defaults
    private void addClearLiquidDinnerDefaults() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        boolean isADA = selectedDiet != null && selectedDiet.contains("ADA");
        
        // Apple Juice (120ml)
        addDefaultDrinkItem("dinner", "Apple Juice", 120, dinnerDrinksContainer, false);
        
        // Chicken Broth (200ml)
        addDefaultDrinkItem("dinner", "Chicken Broth", 200, dinnerDrinksContainer, false);
        
        // Jello or Sugar Free Jello if ADA
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addDefaultDrinkItem("dinner", jelloChoice, 0, dinnerDrinksContainer, false);
        
        // Ice Tea (200ml)
        addDefaultDrinkItem("dinner", "Ice Tea", 200, dinnerDrinksContainer, false);
        
        // Sprite or Sprite Zero if ADA
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addDefaultDrinkItem("dinner", spriteChoice, 240, dinnerDrinksContainer, false);
    }
    
    // NEW: Helper method for adding default drink items
    private void addDefaultDrinkItem(String meal, String itemName, int volumeML, LinearLayout container, boolean editable) {
        // Create the drink item layout
        View drinkItemLayout = getLayoutInflater().inflate(R.layout.drink_item_layout, container, false);
        TextView itemNameView = drinkItemLayout.findViewById(R.id.drinkItemName);
        TextView quantityView = drinkItemLayout.findViewById(R.id.drinkQuantity);
        TextView fluidMLView = drinkItemLayout.findViewById(R.id.drinkFluidML);
        Button removeButton = drinkItemLayout.findViewById(R.id.removeDrinkButton);
        
        // Set the values
        itemNameView.setText(itemName);
        quantityView.setText("1");
        if (volumeML > 0) {
            fluidMLView.setText(volumeML + " ml");
        } else {
            fluidMLView.setText("0 ml");
        }
        
        // For Clear Liquid defaults, make items non-editable (hide remove button)
        if (!editable) {
            removeButton.setVisibility(View.GONE);
            // Change background color to indicate it's a default item
            drinkItemLayout.setBackgroundResource(R.drawable.clear_liquid_item_background);
        } else {
            // Set up remove button functionality for editable items
            removeButton.setOnClickListener(v -> {
                container.removeView(drinkItemLayout);
                updateFluidUsage(meal, -volumeML);
            });
        }
        
        // Add to container
        container.addView(drinkItemLayout);
        
        // Update fluid tracking
        updateFluidUsage(meal, volumeML);
    }
    
    // ===== FLUID TRACKING METHODS =====
    
    private void updateFluidLimits() {
        String restriction = (String) fluidRestrictionSpinner.getSelectedItem();
        
        if (restriction == null || restriction.equals("None")) {
            fluidLimits.put("breakfast", 9999);
            fluidLimits.put("lunch", 9999);
            fluidLimits.put("dinner", 9999);
        } else {
            // Load limits from database based on restriction
            loadFluidLimitsFromDatabase(restriction);
        }
        
        updateFluidDisplay();
    }
    
    private void loadFluidLimitsFromDatabase(String restriction) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT rl.meal, rl.limit_ml " +
                      "FROM RestrictionLimit rl " +
                      "JOIN FluidRestriction fr ON rl.fluid_id = fr.fluid_id " +
                      "WHERE fr.name = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{restriction});
        
        if (cursor.moveToFirst()) {
            int idxMeal = cursor.getColumnIndexOrThrow("meal");
            int idxLimit = cursor.getColumnIndexOrThrow("limit_ml");
            
            do {
                String meal = cursor.getString(idxMeal);
                int limit = cursor.getInt(idxLimit);
                fluidLimits.put(meal, limit);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
    }
    
    private void setupFluidTracking() {
        // Fluid tracking is now handled in the addDrinkToContainer method
        // No need for separate setup since we're not using checkboxes
    }
    
    private void updateFluidDisplay() {
        updateMealFluidDisplay("breakfast", breakfastFluidTracker);
        updateMealFluidDisplay("lunch", lunchFluidTracker);
        updateMealFluidDisplay("dinner", dinnerFluidTracker);
    }
    
    private void updateMealFluidDisplay(String meal, TextView textView) {
        int used = fluidUsed.get(meal);
        int limit = fluidLimits.get(meal);
        
        if (limit == 9999) {
            textView.setText("Fluid: " + used + "ml");
        } else {
            textView.setText("Fluid: " + used + "ml / " + limit + "ml");
            if (used > limit) {
                textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                textView.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }
    
    private void updateFluidUsage(String meal, int changeML) {
        int currentFluid = fluidUsed.get(meal);
        int newFluid = Math.max(0, currentFluid + changeML);
        fluidUsed.put(meal, newFluid);
        updateFluidDisplay();
    }
    
    // ===== PUBLIC ONCLICK METHODS (called from layout XML) =====
    
    public void addJuice(View view) {
        showDrinkSelectionDialog(breakfastJuicesContainer, "Juices", "breakfast");
    }
    
    public void addBreakfastDrink(View view) {
        showDrinkSelectionDialog(breakfastDrinksContainer, "Drink", "breakfast");
    }
    
    public void addLunchDrink(View view) {
        showDrinkSelectionDialog(lunchDrinksContainer, "Drink", "lunch");
    }
    
    public void addDinnerDrink(View view) {
        showDrinkSelectionDialog(dinnerDrinksContainer, "Drink", "dinner");
    }
    
    public void clearForm(View view) {
        clearForm();
    }
    
    public void applyDefaults(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        if (selectedDiet != null && !selectedDiet.equals("Select Diet")) {
            applyDefaults(selectedDiet);
        }
    }
    
    public void finalizeOrder(View view) {
        submitOrder(view);
    }
    
    // ===== DRINK SELECTION DIALOG =====
    
    private void showDrinkSelectionDialog(LinearLayout container, String category, String meal) {
        List<Item> drinks = itemDAO.getItemsByCategory(category);
        if (drinks.isEmpty()) {
            Toast.makeText(this, "No drinks available in this category", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select " + category);
        
        String[] drinkNames = new String[drinks.size()];
        for (int i = 0; i < drinks.size(); i++) {
            drinkNames[i] = drinks.get(i).getName();
        }
        
        builder.setItems(drinkNames, (dialog, which) -> {
            Item selectedDrink = drinks.get(which);
            showQuantityDialog(selectedDrink, container, meal);
        });
        
        builder.show();
    }
    
    private void showQuantityDialog(Item drink, LinearLayout container, String meal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Quantity for " + drink.getName());
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.quantity_dialog, null);
        Spinner quantitySpinner = dialogView.findViewById(R.id.quantitySpinner);
        
        List<String> quantities = Arrays.asList("1", "2", "3", "4", "5");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quantities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quantitySpinner.setAdapter(adapter);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            int quantity = Integer.parseInt((String) quantitySpinner.getSelectedItem());
            addDrinkToContainer(drink, quantity, container, meal);
        });
        builder.setNegativeButton("Cancel", null);
        
        builder.show();
    }
    
    private void addDrinkToContainer(Item drink, int quantity, LinearLayout container, String meal) {
        // Create the drink item layout
        View drinkItemLayout = getLayoutInflater().inflate(R.layout.drink_item_layout, container, false);
        TextView itemNameView = drinkItemLayout.findViewById(R.id.drinkItemName);
        TextView quantityView = drinkItemLayout.findViewById(R.id.drinkQuantity);
        TextView fluidMLView = drinkItemLayout.findViewById(R.id.drinkFluidML);
        Button removeButton = drinkItemLayout.findViewById(R.id.removeDrinkButton);
        
        // Set the values
        itemNameView.setText(drink.getName());
        quantityView.setText(String.valueOf(quantity));
        
        int totalFluidML = drink.getSizeML() * quantity;
        if (totalFluidML > 0) {
            fluidMLView.setText(totalFluidML + " ml");
        } else {
            fluidMLView.setText("0 ml");
        }
        
        // Set up remove button
        removeButton.setOnClickListener(v -> {
            container.removeView(drinkItemLayout);
            updateFluidUsage(meal, -totalFluidML);
        });
        
        // Add to container
        container.addView(drinkItemLayout);
        
        // Add fluid tracking
        updateFluidUsage(meal, totalFluidML);
        
        Toast.makeText(this, "Added " + quantity + " " + drink.getName(), Toast.LENGTH_SHORT).show();
    }
    
    // ===== ORDER SUBMISSION =====
    
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
        StringBuilder summary = new StringBuilder();
        summary.append("Patient: ").append(patientName).append("\n");
        summary.append("Location: ").append(wing).append(" - Room ").append(room).append("\n");
        summary.append("Diet: ").append(diet).append("\n");
        summary.append("Day: ").append(getSelectedDay()).append("\n\n");
        
        // Add fluid restriction if any
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        if (fluidRestriction != null && !fluidRestriction.equals("None")) {
            summary.append("Fluid Restriction: ").append(fluidRestriction).append("\n");
        }
        
        // Add texture modifications if any
        String textureModifications = getTextureModifications();
        if (textureModifications != null) {
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
                saveOrderToDatabase(patientName, wing, room, diet);
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
        
        return modifications.isEmpty() ? null : String.join(", ", modifications);
    }
    
    private void addSelectedItem(StringBuilder summary, Spinner spinner, String category) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.equals("None")) {
            summary.append("• ").append(selected).append("\n");
        }
    }
    
    private void addSelectedDrinks(StringBuilder summary, LinearLayout container) {
        // Updated to work with the new drink layout structure
        for (int i = 0; i < container.getChildCount(); i++) {
            View childView = container.getChildAt(i);
            if (childView instanceof LinearLayout) {
                TextView itemNameView = childView.findViewById(R.id.drinkItemName);
                TextView quantityView = childView.findViewById(R.id.drinkQuantity);
                TextView fluidMLView = childView.findViewById(R.id.drinkFluidML);
                
                if (itemNameView != null && quantityView != null && fluidMLView != null) {
                    String itemText = itemNameView.getText().toString();
                    String quantityText = quantityView.getText().toString();
                    String fluidText = fluidMLView.getText().toString();
                    
                    summary.append("• ").append(itemText)
                           .append(" (x").append(quantityText).append(") - ")
                           .append(fluidText).append("\n");
                }
            }
        }
    }
    
    private void saveOrderToDatabase(String patientName, String wing, String room, String diet) {
        try {
            // Collect all selected items
            List<String> breakfastItems = getSelectedMealItems("breakfast");
            List<String> lunchItems = getSelectedMealItems("lunch");
            List<String> dinnerItems = getSelectedMealItems("dinner");
            
            // Get restrictions and modifications
            String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
            if ("None".equals(fluidRestriction)) {
                fluidRestriction = null;
            }
            
            String textureModifications = getTextureModifications();
            
            // Save to database
            long result = orderDAO.savePatientOrder(
                patientName, wing, room, diet,
                fluidRestriction, textureModifications,
                breakfastItems, lunchItems, dinnerItems
            );
            
            if (result > 0) {
                Toast.makeText(this, "Order submitted successfully!", Toast.LENGTH_SHORT).show();
                clearForm();
            } else {
                Toast.makeText(this, "Failed to save order. Please try again.", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private List<String> getSelectedMealItems(String meal) {
        List<String> items = new ArrayList<>();
        
        switch (meal) {
            case "breakfast":
                addSpinnerSelection(items, breakfastColdCereal);
                addSpinnerSelection(items, breakfastHotCereal);
                addSpinnerSelection(items, breakfastBread);
                addSpinnerSelection(items, breakfastMuffin);
                addSpinnerSelection(items, breakfastMain);
                addSpinnerSelection(items, breakfastFruit);
                addContainerSelections(items, breakfastJuicesContainer);
                addContainerSelections(items, breakfastDrinksContainer);
                break;
            case "lunch":
                addSpinnerSelection(items, lunchProtein);
                addSpinnerSelection(items, lunchStarch);
                addSpinnerSelection(items, lunchVegetable);
                addSpinnerSelection(items, lunchDessert);
                addContainerSelections(items, lunchDrinksContainer);
                break;
            case "dinner":
                addSpinnerSelection(items, dinnerProtein);
                addSpinnerSelection(items, dinnerStarch);
                addSpinnerSelection(items, dinnerVegetable);
                addSpinnerSelection(items, dinnerDessert);
                addContainerSelections(items, dinnerDrinksContainer);
                break;
        }
        
        return items;
    }
    
    private void addSpinnerSelection(List<String> items, Spinner spinner) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.equals("None")) {
            items.add(selected);
        }
    }
    
    private void addContainerSelections(List<String> items, LinearLayout container) {
        // Updated to work with the new drink layout structure
        for (int i = 0; i < container.getChildCount(); i++) {
            View childView = container.getChildAt(i);
            if (childView instanceof LinearLayout) {
                TextView itemNameView = childView.findViewById(R.id.drinkItemName);
                TextView quantityView = childView.findViewById(R.id.drinkQuantity);
                
                if (itemNameView != null && quantityView != null) {
                    String itemText = itemNameView.getText().toString();
                    String quantityText = quantityView.getText().toString();
                    
                    items.add(itemText + " (x" + quantityText + ")");
                }
            }
        }
    }
    
    private void clearForm() {
        patientNameInput.setText("");
        wingSpinner.setSelection(0);
        roomSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        
        // Reset day selection to current day
        setCurrentDayAsDefault();
        
        // Clear checkboxes
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        
        // Reset spinners
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
        
        // Clear drink selections
        clearDrinkSelections(breakfastJuicesContainer);
        clearDrinkSelections(breakfastDrinksContainer);
        clearDrinkSelections(lunchDrinksContainer);
        clearDrinkSelections(dinnerDrinksContainer);
        
        // Reset fluid tracking
        fluidUsed.put("breakfast", 0);
        fluidUsed.put("lunch", 0);
        fluidUsed.put("dinner", 0);
        updateFluidDisplay();
    }
    
    private void clearDrinkSelections(LinearLayout container) {
        // Simply remove all child views since we're not using checkboxes anymore
        container.removeAllViews();
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}