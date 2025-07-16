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
    
    // Fluid tracking
    private TextView breakfastFluidTracker, lunchFluidTracker, dinnerFluidTracker;
    private Map<String, Integer> fluidUsed = new HashMap<>();
    private Map<String, Integer> fluidLimits = new HashMap<>();
    
    // Database components
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private UserDAO userDAO;
    
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
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Admin Access")
            .setMessage("Enter your credentials to access the admin panel:")
            .setView(dialogView)
            .setPositiveButton("Login", null) // Set to null initially
            .setNegativeButton("Cancel", null)
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button loginButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            loginButton.setOnClickListener(v -> {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                
                if (username.isEmpty()) {
                    usernameInput.setError("Username is required");
                    return;
                }
                
                if (password.isEmpty()) {
                    passwordInput.setError("Password is required");
                    return;
                }
                
                // Authenticate user with database
                User user = userDAO.authenticateUser(username, password);
                
                if (user != null) {
                    if (user.isAdmin()) {
                        dialog.dismiss();
                        openAdminPanel(user);
                    } else {
                        passwordInput.setError("Admin access required");
                        passwordInput.selectAll();
                    }
                } else {
                    passwordInput.setError("Invalid username or password");
                    passwordInput.selectAll();
                }
            });
        });
        
        dialog.show();
        
        // Auto-focus username input
        usernameInput.requestFocus();
    }

    private void openAdminPanel(User user) {
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
        
        showFluidTrackers();
        updateFluidTrackerDisplays();
    }
    
    private void hideFluidTrackers() {
        breakfastFluidTracker.setVisibility(View.GONE);
        lunchFluidTracker.setVisibility(View.GONE);
        dinnerFluidTracker.setVisibility(View.GONE);
    }
    
    private void showFluidTrackers() {
        breakfastFluidTracker.setVisibility(View.VISIBLE);
        lunchFluidTracker.setVisibility(View.VISIBLE);
        dinnerFluidTracker.setVisibility(View.VISIBLE);
    }
    
    private void updateFluidTrackerDisplays() {
        breakfastFluidTracker.setText("Fluid Used: " + fluidUsed.get("breakfast") + "ml / " + fluidLimits.get("breakfast") + "ml");
        lunchFluidTracker.setText("Fluid Used: " + fluidUsed.get("lunch") + "ml / " + fluidLimits.get("lunch") + "ml");
        dinnerFluidTracker.setText("Fluid Used: " + fluidUsed.get("dinner") + "ml / " + fluidLimits.get("dinner") + "ml");
    }
    
    private void applyDefaults() {
        String selectedDiet = (String) dietSpinner.getSelectedItem();
        Map<String, Object> defaults = defaultItems.get(selectedDiet);
        
        if (defaults == null) return;
        
        // Apply breakfast defaults
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
        
        Toast.makeText(this, "Default items applied for " + selectedDiet + " diet", Toast.LENGTH_SHORT).show();
    }
    
    private void setSpinnerByValue(Spinner spinner, String value) {
        if (value == null || value.isEmpty()) return;
        
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).contains(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }
    
    // ===== ORDER MANAGEMENT METHODS =====
    
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
                Toast.makeText(this, "Order submitted successfully!", Toast.LENGTH_LONG).show();
                clearForm();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void addSelectedItem(StringBuilder summary, Spinner spinner, String category) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.equals("-- Select --")) {
            summary.append("  ").append(category).append(": ").append(selected).append("\n");
        }
    }
    
    private void clearForm() {
        patientNameInput.setText("");
        wingSpinner.setSelection(0);
        roomSpinner.setSelection(0);
        dietSpinner.setSelection(0);
        fluidRestrictionSpinner.setSelection(0);
        
        // Reset checkboxes
        mechanicalGroundCB.setChecked(false);
        mechanicalChoppedCB.setChecked(false);
        biteSizeCB.setChecked(false);
        breadOKCB.setChecked(false);
        
        // Reset all spinners to default
        populateMealDropdowns();
    }
    
    // ===== DEBUG METHODS (REMOVE THESE AFTER TESTING) =====
    
    private void debugDatabase() {
        android.util.Log.d("DEBUG", "=== DEBUGGING DATABASE CONTENT ===");
        
        try {
            // Check if database is accessible
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            android.util.Log.d("DEBUG", "Database opened successfully");
            
            // Check categories
            Cursor categoryCursor = db.rawQuery("SELECT * FROM Category", null);
            android.util.Log.d("DEBUG", "Categories found: " + categoryCursor.getCount());
            if (categoryCursor.moveToFirst()) {
                do {
                    int id = categoryCursor.getInt(0);
                    String name = categoryCursor.getString(1);
                    android.util.Log.d("DEBUG", "Category: " + id + " - " + name);
                } while (categoryCursor.moveToNext());
            }
            categoryCursor.close();
            
            // Check items
            Cursor itemCursor = db.rawQuery("SELECT * FROM Item LIMIT 10", null);
            android.util.Log.d("DEBUG", "Items found (showing first 10): " + itemCursor.getCount());
            if (itemCursor.moveToFirst()) {
                do {
                    int id = itemCursor.getInt(0);
                    int catId = itemCursor.getInt(1);
                    String name = itemCursor.getString(2);
                    android.util.Log.d("DEBUG", "Item: " + id + " - " + name + " (cat: " + catId + ")");
                } while (itemCursor.moveToNext());
            }
            itemCursor.close();
            
            // Check users
            Cursor userCursor = db.rawQuery("SELECT * FROM User", null);
            android.util.Log.d("DEBUG", "Users found: " + userCursor.getCount());
            if (userCursor.moveToFirst()) {
                do {
                    String username = userCursor.getString(1);
                    String role = userCursor.getString(3);
                    android.util.Log.d("DEBUG", "User: " + username + " - " + role);
                } while (userCursor.moveToNext());
            }
            userCursor.close();
            
            // Test ItemDAO methods
            android.util.Log.d("DEBUG", "Testing ItemDAO methods...");
            
            List<Item> coldCereals = itemDAO.getColdCerealItems();
            android.util.Log.d("DEBUG", "Cold Cereals: " + coldCereals.size());
            
            List<Item> proteins = itemDAO.getProteinItems();
            android.util.Log.d("DEBUG", "Proteins: " + proteins.size());
            
            List<Item> allItems = itemDAO.getAllItems();
            android.util.Log.d("DEBUG", "All Items: " + allItems.size());
            
            // Test UserDAO methods
            android.util.Log.d("DEBUG", "Testing UserDAO methods...");
            
            List<User> allUsers = userDAO.getAllUsers();
            android.util.Log.d("DEBUG", "All Users: " + allUsers.size());
            
            int adminCount = userDAO.getAdminCount();
            android.util.Log.d("DEBUG", "Admin Users: " + adminCount);
            
        } catch (Exception e) {
            android.util.Log.e("DEBUG", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
        
        android.util.Log.d("DEBUG", "=== END DATABASE DEBUG ===");
    }
    
    private void recreateDatabase() {
        android.util.Log.d("DEBUG", "Recreating database...");
        try {
            this.deleteDatabase("hospital_dietary.db");
            dbHelper = new DatabaseHelper(this);
            itemDAO = new ItemDAO(dbHelper);
            userDAO = new UserDAO(dbHelper);
            // Force database creation by trying to read from it
            dbHelper.getReadableDatabase();
            android.util.Log.d("DEBUG", "Database recreated successfully");
            
            // Reload dropdowns
            populateMealDropdowns();
        } catch (Exception e) {
            android.util.Log.e("DEBUG", "Error recreating database: " + e.getMessage());
        }
    }
    
    // TEMPORARY DEBUG BUTTON (REMOVE AFTER TESTING)
    private void createDebugButton() {
        Button debugButton = new Button(this);
        debugButton.setText("🔧 DEBUG DB");
        debugButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Debug Options")
                .setMessage("Choose debug action:")
                .setPositiveButton("Check DB", (dialog, which) -> debugDatabase())
                .setNegativeButton("Recreate DB", (dialog, which) -> {
                    recreateDatabase();
                    debugDatabase();
                })
                .setNeutralButton("Cancel", null)
                .show();
        });
        
        // Add to the main layout (you'll need to find a container to add this to)
        // For now, just create it - you can manually position it in your layout
    }
    
    // Placeholder methods for drink/juice functionality
    public void addJuice(View view) {
        // TODO: Implement juice adding functionality
        Toast.makeText(this, "Add Juice functionality - Coming Soon!", Toast.LENGTH_SHORT).show();
    }
    
    public void addBreakfastDrink(View view) {
        // TODO: Implement breakfast drink adding functionality
        Toast.makeText(this, "Add Breakfast Drink functionality - Coming Soon!", Toast.LENGTH_SHORT).show();
    }
    
    public void addLunchDrink(View view) {
        // TODO: Implement lunch drink adding functionality
        Toast.makeText(this, "Add Lunch Drink functionality - Coming Soon!", Toast.LENGTH_SHORT).show();
    }
    
    public void addDinnerDrink(View view) {
        // TODO: Implement dinner drink adding functionality
        Toast.makeText(this, "Add Dinner Drink functionality - Coming Soon!", Toast.LENGTH_SHORT).show();
    }
}