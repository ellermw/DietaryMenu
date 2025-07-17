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
    private LinearLayout lunchDrinksContainer;
    
    // Dinner components
    private Spinner dinnerProtein, dinnerStarch, dinnerVegetable, dinnerDessert;
    private LinearLayout dinnerDrinksContainer;
    
    // Fluid tracking components
    private TextView breakfastFluidTracker, lunchFluidTracker, dinnerFluidTracker;
    
    // Database components
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private OrderDAO orderDAO;
    private UserDAO userDAO;
    
    // User info
    private String currentUsername;
    private String currentUserRole;
    private boolean isAdmin = false;
    
    // Fluid tracking
    private Map<String, Integer> fluidLimits = new HashMap<>();
    private Map<String, Integer> fluidUsed = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get user info from intent
        currentUsername = getIntent().getStringExtra("username");
        currentUserRole = getIntent().getStringExtra("role");
        isAdmin = getIntent().getBooleanExtra("is_admin", false);
        
        setTitle("Dietary Menu - " + currentUsername + " (" + currentUserRole + ")");
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        orderDAO = new OrderDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);
        
        // Initialize UI and load data
        initializeUI();
        setupListeners();
        loadStaticData();
        loadSpinnerData();
        initializeFluidTracking();
    }
    
    private void initializeUI() {
        // Main form components
        dayGroup = findViewById(R.id.dayGroup);
        patientNameInput = findViewById(R.id.patientNameInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomSpinner = findViewById(R.id.roomSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        
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
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Fluid restriction listener
        fluidRestrictionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFluidLimits();
                updateAllFluidDisplays();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void loadStaticData() {
        // Load wing options
        List<String> wings = Arrays.asList("Select Wing", "A", "B", "C", "D", "E");
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // Load diet options
        List<String> diets = Arrays.asList("Select Diet", "Regular", "ADA", "Diabetic", "Cardiac", "Renal", "Soft", "Liquid");
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // Load fluid restriction options
        List<String> fluidRestrictions = Arrays.asList("None", "1000ml", "1200ml", "1500ml", "1800ml", "2000ml", "2500ml");
        ArrayAdapter<String> fluidAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidAdapter);
    }
    
    private void updateRoomNumbers() {
        String selectedWing = (String) wingSpinner.getSelectedItem();
        List<String> rooms = new ArrayList<>();
        rooms.add("Select Room");
        
        if (selectedWing != null && !selectedWing.equals("Select Wing")) {
            // Generate room numbers 1-20 for each wing
            for (int i = 1; i <= 20; i++) {
                rooms.add(String.valueOf(i));
            }
        }
        
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomSpinner.setAdapter(roomAdapter);
    }
    
    private void loadSpinnerData() {
        // Breakfast items
        loadSpinnerItems(breakfastColdCereal, "Cold Cereal", true);
        loadSpinnerItems(breakfastHotCereal, "Hot Cereal", true);
        loadSpinnerItems(breakfastBread, "Bread", true);
        loadSpinnerItems(breakfastMuffin, "Muffin", true);
        loadSpinnerItems(breakfastMain, "Breakfast", true);
        loadSpinnerItems(breakfastFruit, "Fruit", true);
        
        // Lunch items
        loadSpinnerItems(lunchProtein, "Protein", true);
        loadSpinnerItems(lunchStarch, "Starch", true);
        loadSpinnerItems(lunchVegetable, "Vegetable", true);
        loadSpinnerItems(lunchDessert, "Dessert", true);
        
        // Dinner items
        loadSpinnerItems(dinnerProtein, "Protein", true);
        loadSpinnerItems(dinnerStarch, "Starch", true);
        loadSpinnerItems(dinnerVegetable, "Vegetable", true);
        loadSpinnerItems(dinnerDessert, "Dessert", true);
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
    
    private void filterItemsByDiet(String selectedDiet) {
        boolean isADA = selectedDiet.equals("ADA") || selectedDiet.equals("Diabetic");
        
        if (isADA) {
            filterSpinnerForADA(lunchDessert);
            filterSpinnerForADA(dinnerDessert);
        } else {
            // Reload all desserts for non-ADA diets
            loadSpinnerItems(lunchDessert, "Dessert", true);
            loadSpinnerItems(dinnerDessert, "Dessert", true);
        }
    }
    
    private void filterSpinnerForADA(Spinner spinner) {
        List<Item> allDesserts = itemDAO.getItemsByCategory("Dessert");
        List<String> adaFriendlyDesserts = new ArrayList<>();
        adaFriendlyDesserts.add("None");
        
        for (Item dessert : allDesserts) {
            if (dessert.isAdaFriendly()) {
                adaFriendlyDesserts.add(dessert.getName());
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, adaFriendlyDesserts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    // ===== FLUID TRACKING SYSTEM =====
    
    private void initializeFluidTracking() {
        fluidUsed.put("breakfast", 0);
        fluidUsed.put("lunch", 0);
        fluidUsed.put("dinner", 0);
        
        fluidLimits.put("breakfast", 0);
        fluidLimits.put("lunch", 0);
        fluidLimits.put("dinner", 0);
    }
    
    private void updateFluidLimits() {
        String restriction = (String) fluidRestrictionSpinner.getSelectedItem();
        if (restriction == null || restriction.equals("None")) {
            // No restrictions - hide fluid trackers
            breakfastFluidTracker.setVisibility(View.GONE);
            lunchFluidTracker.setVisibility(View.GONE);
            dinnerFluidTracker.setVisibility(View.GONE);
            return;
        }
        
        // Show fluid trackers
        breakfastFluidTracker.setVisibility(View.VISIBLE);
        lunchFluidTracker.setVisibility(View.VISIBLE);
        dinnerFluidTracker.setVisibility(View.VISIBLE);
        
        // Get fluid limits from database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT fr.restriction_name, rl.meal, rl.limit_ml " +
                      "FROM FluidRestriction fr " +
                      "JOIN RestrictionLimit rl ON fr.restriction_id = rl.restriction_id " +
                      "WHERE fr.restriction_name = ?";
        
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
    
    private void updateAllFluidDisplays() {
        updateFluidDisplay("breakfast", breakfastFluidTracker);
        updateFluidDisplay("lunch", lunchFluidTracker);
        updateFluidDisplay("dinner", dinnerFluidTracker);
    }
    
    private void updateFluidDisplay(String meal, TextView textView) {
        int used = fluidUsed.get(meal);
        int limit = fluidLimits.get(meal);
        
        if (limit > 0) {
            String text = "Fluid Used: " + used + "ml / " + limit + "ml";
            textView.setText(text);
            
            // Color coding
            if (used > limit) {
                textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                textView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            } else if (used > limit * 0.8) {
                textView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                textView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            } else {
                textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                textView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            }
        } else {
            textView.setText("Fluid Used: " + used + "ml / Unlimited");
            textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }
    
    // ===== DRINK MANAGEMENT METHODS =====
    
    public void addBreakfastJuice(View view) {
        addDrinkToContainer(breakfastJuicesContainer, "Juices", "breakfast");
    }
    
    public void addBreakfastDrink(View view) {
        addDrinkToContainer(breakfastDrinksContainer, "Drink", "breakfast");
    }
    
    public void addLunchDrink(View view) {
        addDrinkToContainer(lunchDrinksContainer, "Drink", "lunch");
    }
    
    public void addDinnerDrink(View view) {
        addDrinkToContainer(dinnerDrinksContainer, "Drink", "dinner");
    }
    
    private void addDrinkToContainer(LinearLayout container, String category, String meal) {
        // Create drink item view
        View drinkView = LayoutInflater.from(this).inflate(R.layout.drink_item, container, false);
        
        Spinner drinkSpinner = drinkView.findViewById(R.id.drinkSpinner);
        TextView drinkSize = drinkView.findViewById(R.id.drinkSize);
        Button removeButton = drinkView.findViewById(R.id.removeButton);
        
        // Load drink options
        List<Item> drinks = itemDAO.getItemsByCategory(category);
        List<String> drinkNames = new ArrayList<>();
        drinkNames.add("Select " + category.substring(0, category.length() - 1)); // Remove 's' from category
        
        for (Item drink : drinks) {
            drinkNames.add(drink.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        drinkSpinner.setAdapter(adapter);
        
        // Set up spinner listener for fluid tracking
        drinkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selectedDrink = (String) parent.getItemAtPosition(position);
                    Item drinkItem = findItemByName(selectedDrink, category);
                    if (drinkItem != null) {
                        int newFluid = fluidUsed.get(meal) + drinkItem.getSizeML();
                        int limit = fluidLimits.get(meal);
                        
                        // Check fluid limit
                        if (limit > 0 && newFluid > limit) {
                            Toast.makeText(MainActivity.this, 
                                "Warning: This would exceed the " + meal + " fluid limit (" + limit + "ml)", 
                                Toast.LENGTH_LONG).show();
                        }
                        
                        fluidUsed.put(meal, newFluid);
                        drinkSize.setText(drinkItem.getSizeML() + "ml");
                        updateAllFluidDisplays();
                    }
                } else {
                    drinkSize.setText("0ml");
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Set up remove button
        removeButton.setOnClickListener(v -> {
            // Remove fluid amount before removing view
            String selectedDrink = (String) drinkSpinner.getSelectedItem();
            if (selectedDrink != null && !selectedDrink.startsWith("Select")) {
                Item drinkItem = findItemByName(selectedDrink, category);
                if (drinkItem != null) {
                    int currentFluid = fluidUsed.get(meal);
                    fluidUsed.put(meal, Math.max(0, currentFluid - drinkItem.getSizeML()));
                    updateAllFluidDisplays();
                }
            }
            container.removeView(drinkView);
        });
        
        container.addView(drinkView);
    }
    
    private Item findItemByName(String name, String category) {
        List<Item> items = itemDAO.getItemsByCategory(category);
        for (Item item : items) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }
    
    // ===== BUTTON CLICK HANDLERS =====
    
    public void clearForm(View view) {
        clearForm();
    }
    
    public void applyDefaults(View view) {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        if (selectedDiet != null && !selectedDiet.equals("Select Diet")) {
            applyDefaults(selectedDiet);
        } else {
            Toast.makeText(this, "Please select a diet first", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void finalizeOrder(View view) {
        finalizeOrder();
    }
    
    // ===== ORDER MANAGEMENT =====
    
    private void finalizeOrder() {
        // Validate required fields
        String patientName = patientNameInput.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String room = (String) roomSpinner.getSelectedItem();
        String diet = (String) dietSpinner.getSelectedItem();
        
        if (patientName.isEmpty()) {
            patientNameInput.setError("Patient name is required");
            patientNameInput.requestFocus();
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
        
        // Check fluid limits
        if (!validateFluidLimits()) {
            return;
        }
        
        // Build order summary
        StringBuilder summary = new StringBuilder();
        buildOrderSummary(summary, patientName, wing, room, diet);
        
        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Confirm Order")
            .setMessage(summary.toString())
            .setPositiveButton("Finalize", (dialog, which) -> {
                saveOrderToDatabase(patientName, wing, room, diet);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private boolean validateFluidLimits() {
        for (String meal : Arrays.asList("breakfast", "lunch", "dinner")) {
            int used = fluidUsed.get(meal);
            int limit = fluidLimits.get(meal);
            
            if (limit > 0 && used > limit) {
                new AlertDialog.Builder(this)
                    .setTitle("Fluid Limit Exceeded")
                    .setMessage("The " + meal + " fluid intake (" + used + "ml) exceeds the limit (" + limit + "ml). Please remove some drinks.")
                    .setPositiveButton("OK", null)
                    .show();
                return false;
            }
        }
        return true;
    }
    
    private void buildOrderSummary(StringBuilder summary, String patientName, String wing, String room, String diet) {
        summary.append("Patient: ").append(patientName).append("\n");
        summary.append("Location: ").append(wing).append(" - ").append(room).append("\n");
        summary.append("Diet: ").append(diet).append("\n");
        
        // Add texture modifications if any
        String modifications = getTextureModifications();
        if (modifications != null && !modifications.isEmpty()) {
            summary.append("Modifications: ").append(modifications).append("\n");
        }
        
        summary.append("\n");
        
        // Breakfast
        summary.append("BREAKFAST:\n");
        addSelectedItem(summary, breakfastColdCereal, "Cold Cereal");
        addSelectedItem(summary, breakfastHotCereal, "Hot Cereal");
        addSelectedItem(summary, breakfastBread, "Bread");
        addSelectedItem(summary, breakfastMuffin, "Muffin");
        addSelectedItem(summary, breakfastMain, "Main");
        addSelectedItem(summary, breakfastFruit, "Fruit");
        addSelectedDrinks(summary, breakfastJuicesContainer);
        addSelectedDrinks(summary, breakfastDrinksContainer);
        
        // Lunch
        summary.append("\nLUNCH:\n");
        addSelectedItem(summary, lunchProtein, "Protein");
        addSelectedItem(summary, lunchStarch, "Starch");
        addSelectedItem(summary, lunchVegetable, "Vegetable");
        addSelectedItem(summary, lunchDessert, "Dessert");
        addSelectedDrinks(summary, lunchDrinksContainer);
        
        // Dinner
        summary.append("\nDINNER:\n");
        addSelectedItem(summary, dinnerProtein, "Protein");
        addSelectedItem(summary, dinnerStarch, "Starch");
        addSelectedItem(summary, dinnerVegetable, "Vegetable");
        addSelectedItem(summary, dinnerDessert, "Dessert");
        addSelectedDrinks(summary, dinnerDrinksContainer);
        
        // Add fluid summary if restrictions exist
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        if (fluidRestriction != null && !fluidRestriction.equals("None")) {
            summary.append("\nFLUID INTAKE:\n");
            for (String meal : Arrays.asList("breakfast", "lunch", "dinner")) {
                int used = fluidUsed.get(meal);
                int limit = fluidLimits.get(meal);
                if (limit > 0) {
                    summary.append("• ").append(meal.toUpperCase()).append(": ").append(used).append("ml / ").append(limit).append("ml\n");
                }
            }
        }
    }
    
    private void addSelectedItem(StringBuilder summary, Spinner spinner, String category) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.equals("None")) {
            summary.append("• ").append(selected).append("\n");
        }
    }
    
    private void addSelectedDrinks(StringBuilder summary, LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View drinkView = container.getChildAt(i);
            Spinner drinkSpinner = drinkView.findViewById(R.id.drinkSpinner);
            TextView drinkSize = drinkView.findViewById(R.id.drinkSize);
            
            String selectedDrink = (String) drinkSpinner.getSelectedItem();
            if (selectedDrink != null && !selectedDrink.startsWith("Select")) {
                summary.append("• ").append(selectedDrink).append(" (").append(drinkSize.getText()).append(")\n");
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
                Toast.makeText(this, "Failed to save order. Please try again.", Toast.LENGTH_LONG).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving order: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private List<String> getSelectedMealItems(String meal) {
        List<String> items = new ArrayList<>();
        
        switch (meal.toLowerCase()) {
            case "breakfast":
                addSelectedSpinnerItem(items, breakfastColdCereal);
                addSelectedSpinnerItem(items, breakfastHotCereal);
                addSelectedSpinnerItem(items, breakfastBread);
                addSelectedSpinnerItem(items, breakfastMuffin);
                addSelectedSpinnerItem(items, breakfastMain);
                addSelectedSpinnerItem(items, breakfastFruit);
                addContainerSelections(items, breakfastJuicesContainer);
                addContainerSelections(items, breakfastDrinksContainer);
                break;
                
            case "lunch":
                addSelectedSpinnerItem(items, lunchProtein);
                addSelectedSpinnerItem(items, lunchStarch);
                addSelectedSpinnerItem(items, lunchVegetable);
                addSelectedSpinnerItem(items, lunchDessert);
                addContainerSelections(items, lunchDrinksContainer);
                break;
                
            case "dinner":
                addSelectedSpinnerItem(items, dinnerProtein);
                addSelectedSpinnerItem(items, dinnerStarch);
                addSelectedSpinnerItem(items, dinnerVegetable);
                addSelectedSpinnerItem(items, dinnerDessert);
                addContainerSelections(items, dinnerDrinksContainer);
                break;
        }
        
        return items;
    }
    
    private void addSelectedSpinnerItem(List<String> items, Spinner spinner) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.equals("None")) {
            items.add(selected);
        }
    }
    
    private void addContainerSelections(List<String> items, LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View drinkView = container.getChildAt(i);
            Spinner drinkSpinner = drinkView.findViewById(R.id.drinkSpinner);
            String selectedDrink = (String) drinkSpinner.getSelectedItem();
            if (selectedDrink != null && !selectedDrink.startsWith("Select")) {
                items.add(selectedDrink);
            }
        }
    }
    
    private String getTextureModifications() {
        List<String> modifications = new ArrayList<>();
        
        if (mechanicalGroundCB.isChecked()) {
            modifications.add("Mechanical Ground");
        }
        if (mechanicalChoppedCB.isChecked()) {
            modifications.add("Mechanical Chopped");
        }
        if (biteSizeCB.isChecked()) {
            modifications.add("Bite Size");
        }
        if (breadOKCB.isChecked()) {
            modifications.add("Bread OK");
        }
        
        return modifications.isEmpty() ? null : String.join(", ", modifications);
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
        
        // Reset all spinners
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
        fluidUsed.put("breakfast", 0);
        fluidUsed.put("lunch", 0);
        fluidUsed.put("dinner", 0);
        updateAllFluidDisplays();
    }
    
    private void applyDefaults(String diet) {
        // This would set default selections based on diet type
        // Implementation depends on business requirements
        Toast.makeText(this, "Default items applied for " + diet + " diet", Toast.LENGTH_SHORT).show();
    }
    
    // ===== MENU FUNCTIONALITY =====
    
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
                    openAdminPanel();
                }
                return true;
            case 2: // View Orders
                openViewOrders();
                return true;
            case 3: // Logout
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAdminPanel() {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("current_user", currentUsername);
        startActivity(intent);
    }
    
    private void openViewOrders() {
        Intent intent = new Intent(this, ViewOrdersActivity.class);
        intent.putExtra("current_username", currentUsername);
        intent.putExtra("is_admin", isAdmin);
        startActivity(intent);
    }
    
    private void logout() {
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
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}