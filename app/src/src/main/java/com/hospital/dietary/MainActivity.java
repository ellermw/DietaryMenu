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
    private String[] wings = {"North", "South", "East", "West", "ICU", "ER"};
    private String[] rooms = {"101", "102", "103", "104", "105", "106", "107", "108", "109", "110",
                             "201", "202", "203", "204", "205", "206", "207", "208", "209", "210",
                             "301", "302", "303", "304", "305", "306", "307", "308", "309", "310"};
    private String[] diets = {"Regular", "ADA Diabetic", "Cardiac", "Renal", "Soft", "Liquid", "NPO", "Pureed", "Mechanical Soft"};
    private String[] fluidRestrictions = {"None", "1000ml", "1500ml", "2000ml", "2500ml"};
    
    // Meal item arrays
    private String[] breakfastColdCereals = {"None", "Cornflakes", "Rice Krispies", "Cheerios", "Bran Flakes"};
    private String[] breakfastHotCereals = {"None", "Oatmeal", "Cream of Wheat", "Grits"};
    private String[] breakfastBreads = {"None", "White Toast", "Wheat Toast", "English Muffin", "Bagel"};
    private String[] breakfastMuffins = {"None", "Blueberry Muffin", "Bran Muffin", "Banana Muffin"};
    private String[] breakfastMains = {"None", "Scrambled Eggs", "Pancakes", "French Toast", "Bacon", "Sausage"};
    private String[] breakfastFruits = {"None", "Fresh Fruit", "Banana", "Orange", "Apple", "Fruit Cup"};
    
    private String[] lunchProteins = {"None", "Grilled Chicken", "Turkey Sandwich", "Beef Stew", "Fish"};
    private String[] lunchStarches = {"None", "Rice", "Mashed Potatoes", "Pasta", "Bread Roll"};
    private String[] lunchVegetables = {"None", "Steamed Vegetables", "Garden Salad", "Green Beans", "Carrots"};
    private String[] lunchDesserts = {"None", "Ice Cream", "Pudding", "Jello", "Fruit Cup"};
    
    private String[] dinnerProteins = {"None", "Baked Fish", "Grilled Chicken", "Beef", "Pork"};
    private String[] dinnerStarches = {"None", "Mashed Potatoes", "Rice", "Pasta", "Baked Potato"};
    private String[] dinnerVegetables = {"None", "Green Beans", "Broccoli", "Carrots", "Corn"};
    private String[] dinnerDesserts = {"None", "Ice Cream", "Cake", "Pudding", "Pie"};
    
    private String[] juices = {"Orange Juice", "Apple Juice", "Cranberry Juice", "Grape Juice", "Tomato Juice"};
    private String[] drinks = {"Water", "Coffee", "Tea", "Milk", "Soda", "Iced Tea"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        isAdmin = "admin".equals(currentUserRole);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        
        // Initialize UI elements
        initializeUIElements();
        
        // Setup spinners
        setupSpinners();
        
        // Setup button listeners
        setupButtonListeners();
        
        // FIXED: Initialize texture modification validation
        initializeTextureValidation();
        
        // Initialize fluid tracking
        initializeFluidTracking();
        
        // Set initial form state
        clearForm();
    }
    
    private void initializeUIElements() {
        // Patient Information
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
        
        // Breakfast Items
        breakfastColdCereal = findViewById(R.id.breakfastColdCereal);
        breakfastHotCereal = findViewById(R.id.breakfastHotCereal);
        breakfastBread = findViewById(R.id.breakfastBread);
        breakfastMuffin = findViewById(R.id.breakfastMuffin);
        breakfastMain = findViewById(R.id.breakfastMain);
        breakfastFruit = findViewById(R.id.breakfastFruit);
        
        // Lunch Items
        lunchProtein = findViewById(R.id.lunchProtein);
        lunchStarch = findViewById(R.id.lunchStarch);
        lunchVegetable = findViewById(R.id.lunchVegetable);
        lunchDessert = findViewById(R.id.lunchDessert);
        
        // Dinner Items
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
    
    private void setupSpinners() {
        // Setup spinner adapters
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rooms);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomSpinner.setAdapter(roomAdapter);
        
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        ArrayAdapter<String> fluidRestrictionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fluidRestrictions);
        fluidRestrictionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fluidRestrictionSpinner.setAdapter(fluidRestrictionAdapter);
        
        // Breakfast spinners
        setupSpinnerAdapter(breakfastColdCereal, breakfastColdCereals);
        setupSpinnerAdapter(breakfastHotCereal, breakfastHotCereals);
        setupSpinnerAdapter(breakfastBread, breakfastBreads);
        setupSpinnerAdapter(breakfastMuffin, breakfastMuffins);
        setupSpinnerAdapter(breakfastMain, breakfastMains);
        setupSpinnerAdapter(breakfastFruit, breakfastFruits);
        
        // Lunch spinners
        setupSpinnerAdapter(lunchProtein, lunchProteins);
        setupSpinnerAdapter(lunchStarch, lunchStarches);
        setupSpinnerAdapter(lunchVegetable, lunchVegetables);
        setupSpinnerAdapter(lunchDessert, lunchDesserts);
        
        // Dinner spinners
        setupSpinnerAdapter(dinnerProtein, dinnerProteins);
        setupSpinnerAdapter(dinnerStarch, dinnerStarches);
        setupSpinnerAdapter(dinnerVegetable, dinnerVegetables);
        setupSpinnerAdapter(dinnerDessert, dinnerDesserts);
    }
    
    private void setupSpinnerAdapter(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    private void setupButtonListeners() {
        addBreakfastJuiceButton.setOnClickListener(v -> showDrinkSelectionDialog("Breakfast Juice", breakfastJuicesContainer, "breakfast"));
        addBreakfastDrinkButton.setOnClickListener(v -> showDrinkSelectionDialog("Breakfast Drink", breakfastDrinksContainer, "breakfast"));
        addLunchDrinkButton.setOnClickListener(v -> showDrinkSelectionDialog("Lunch Drink", lunchDrinksContainer, "lunch"));
        addDinnerDrinkButton.setOnClickListener(v -> showDrinkSelectionDialog("Dinner Drink", dinnerDrinksContainer, "dinner"));
        
        submitOrderButton.setOnClickListener(v -> {
            // FIXED: Validate texture modifications before submitting
            if (validateTextureModifications()) {
                confirmOrder();
            }
        });
        
        clearFormButton.setOnClickListener(v -> clearForm());
        backButton.setOnClickListener(v -> finish());
    }
    
    // FIXED: Texture modification validation logic
    private void initializeTextureValidation() {
        setupTextureModificationListeners();
    }
    
    private void setupTextureModificationListeners() {
        mechanicalGroundCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                validateTextureModifications();
            }
        });
        
        mechanicalChoppedCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                validateTextureModifications();
            }
        });
        
        breadOKCB.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If bread OK is unchecked while mechanical restrictions are active,
            // clear any bread selections
            if (!isChecked && (mechanicalGroundCB.isChecked() || mechanicalChoppedCB.isChecked())) {
                clearBreadSelections();
            }
        });
        
        // Add listeners to bread-related spinners to validate in real-time
        breakfastBread.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Not "None"
                    validateTextureModifications();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        breakfastMuffin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Not "None"
                    validateTextureModifications();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private boolean validateTextureModifications() {
        boolean mechanicalGround = mechanicalGroundCB.isChecked();
        boolean mechanicalChopped = mechanicalChoppedCB.isChecked();
        boolean biteSize = biteSizeCB.isChecked();
        boolean breadOK = breadOKCB.isChecked();
        
        // Check if user is trying to select bread items when mechanical restrictions apply
        if ((mechanicalGround || mechanicalChopped) && !breadOK) {
            // If mechanical ground or chopped is selected but bread OK is NOT selected,
            // we need to validate that no bread items are selected
            
            if (isBreakfastBreadSelected()) {
                showTextureValidationDialog("Bread items cannot be selected with Mechanical Ground or Mechanical Chopped unless 'Bread OK' is checked.\n\nPlease either:\n1. Check 'Bread OK' to allow bread items, or\n2. Remove bread selections");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isBreakfastBreadSelected() {
        // Check if breakfast bread spinner has bread item selected
        if (breakfastBread.getSelectedItemPosition() > 0) {
            return true;
        }
        
        // Check if breakfast muffin is selected (also bread-like)
        if (breakfastMuffin.getSelectedItemPosition() > 0) {
            return true;
        }
        
        return false;
    }
    
    private void showTextureValidationDialog(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Texture Modification Conflict")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
    
    private void clearBreadSelections() {
        // Clear breakfast bread selections
        breakfastBread.setSelection(0);
        breakfastMuffin.setSelection(0);
        
        Toast.makeText(this, "Bread selections cleared due to texture restrictions", Toast.LENGTH_SHORT).show();
    }
    
    private void initializeFluidTracking() {
        fluidUsed.put("breakfast", 0);
        fluidUsed.put("lunch", 0);
        fluidUsed.put("dinner", 0);
        
        // Set default fluid limits
        fluidLimits.put("breakfast", 500);
        fluidLimits.put("lunch", 500);
        fluidLimits.put("dinner", 500);
        
        updateFluidDisplays();
    }
    
    private void updateFluidDisplays() {
        breakfastFluidDisplay.setText("Fluids: " + fluidUsed.get("breakfast") + "/" + fluidLimits.get("breakfast") + "ml");
        lunchFluidDisplay.setText("Fluids: " + fluidUsed.get("lunch") + "/" + fluidLimits.get("lunch") + "ml");
        dinnerFluidDisplay.setText("Fluids: " + fluidUsed.get("dinner") + "/" + fluidLimits.get("dinner") + "ml");
    }
    
    private void showDrinkSelectionDialog(String title, LinearLayout container, String meal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        String[] drinkOptions = title.contains("Juice") ? juices : drinks;
        
        builder.setItems(drinkOptions, (dialog, which) -> {
            String selectedDrink = drinkOptions[which];
            addDrinkToContainer(selectedDrink, container, meal);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void addDrinkToContainer(String drinkName, LinearLayout container, String meal) {
        View drinkView = getLayoutInflater().inflate(R.layout.drink_item, container, false);
        
        TextView itemName = drinkView.findViewById(R.id.drinkItemName);
        TextView quantity = drinkView.findViewById(R.id.drinkQuantity);
        TextView fluidML = drinkView.findViewById(R.id.drinkFluidML);
        Button removeButton = drinkView.findViewById(R.id.removeDrinkButton);
        
        itemName.setText(drinkName);
        quantity.setText("1");
        
        // Set fluid amount based on drink type
        int fluidAmount = getFluidAmount(drinkName);
        fluidML.setText(fluidAmount + "ml");
        
        // Update fluid tracking
        fluidUsed.put(meal, fluidUsed.get(meal) + fluidAmount);
        updateFluidDisplays();
        
        removeButton.setOnClickListener(v -> {
            container.removeView(drinkView);
            fluidUsed.put(meal, fluidUsed.get(meal) - fluidAmount);
            updateFluidDisplays();
        });
        
        container.addView(drinkView);
    }
    
    private int getFluidAmount(String drinkName) {
        // Default fluid amounts in ml
        switch (drinkName) {
            case "Water":
            case "Coffee":
            case "Tea":
            case "Milk":
            case "Soda":
            case "Iced Tea":
                return 240; // 8 oz
            case "Orange Juice":
            case "Apple Juice":
            case "Cranberry Juice":
            case "Grape Juice":
            case "Tomato Juice":
                return 120; // 4 oz
            default:
                return 240;
        }
    }
    
    private void confirmOrder() {
        if (!validateForm()) {
            return;
        }
        
        String firstName = patientFirstNameInput.getText().toString().trim();
        String lastName = patientLastNameInput.getText().toString().trim();
        String wing = (String) wingSpinner.getSelectedItem();
        String room = (String) roomSpinner.getSelectedItem();
        String diet = (String) dietSpinner.getSelectedItem();
        
        StringBuilder summary = new StringBuilder();
        summary.append("Patient: ").append(firstName).append(" ").append(lastName).append("\n");
        summary.append("Location: ").append(wing).append(" Wing, Room ").append(room).append("\n");
        summary.append("Diet: ").append(diet).append("\n");
        
        String fluidRestriction = (String) fluidRestrictionSpinner.getSelectedItem();
        if (fluidRestriction != null && !fluidRestriction.equals("None")) {
            summary.append("Fluid Restriction: ").append(fluidRestriction).append("\n");
        }
        
        String textureModifications = getTextureModifications();
        if (!textureModifications.equals("None")) {
            summary.append("Texture Modifications: ").append(textureModifications).append("\n");
        }
        
        summary.append("\nBREAKFAST:\n");
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
    
    private boolean validateForm() {
        String firstName = patientFirstNameInput.getText().toString().trim();
        String lastName = patientLastNameInput.getText().toString().trim();
        
        if (firstName.isEmpty()) {
            patientFirstNameInput.setError("First name is required");
            patientFirstNameInput.requestFocus();
            return false;
        }
        
        if (lastName.isEmpty()) {
            patientLastNameInput.setError("Last name is required");
            patientLastNameInput.requestFocus();
            return false;
        }
        
        return true;
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
        
        values.put("patient_name", firstName + " " + lastName);
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
            Toast.makeText(this, "Error submitting order. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void addMealItems(ContentValues values, String meal) {
        List<String> items = new ArrayList<>();
        List<String> mealJuices = new ArrayList<>();
        List<String> mealDrinks = new ArrayList<>();
        
        // Add main meal items
        switch (meal) {
            case "breakfast":
                addItemIfSelected(items, breakfastColdCereal);
                addItemIfSelected(items, breakfastHotCereal);
                addItemIfSelected(items, breakfastBread);
                addItemIfSelected(items, breakfastMuffin);
                addItemIfSelected(items, breakfastMain);
                addItemIfSelected(items, breakfastFruit);
                addDrinksFromContainer(mealJuices, breakfastJuicesContainer);
                addDrinksFromContainer(mealDrinks, breakfastDrinksContainer);
                break;
            case "lunch":
                addItemIfSelected(items, lunchProtein);
                addItemIfSelected(items, lunchStarch);
                addItemIfSelected(items, lunchVegetable);
                addItemIfSelected(items, lunchDessert);
                addDrinksFromContainer(mealDrinks, lunchDrinksContainer);
                break;
            case "dinner":
                addItemIfSelected(items, dinnerProtein);
                addItemIfSelected(items, dinnerStarch);
                addItemIfSelected(items, dinnerVegetable);
                addItemIfSelected(items, dinnerDessert);
                addDrinksFromContainer(mealDrinks, dinnerDrinksContainer);
                break;
        }
        
        // Save to database
        if (!items.isEmpty()) {
            values.put(meal + "_items", String.join(",", items));
        }
        if (!mealJuices.isEmpty()) {
            values.put(meal + "_juices", String.join(",", mealJuices));
        }
        if (!mealDrinks.isEmpty()) {
            values.put(meal + "_drinks", String.join(",", mealDrinks));
        }
    }
    
    private void addItemIfSelected(List<String> items, Spinner spinner) {
        String selected = (String) spinner.getSelectedItem();
        if (selected != null && !selected.equals("None")) {
            items.add(selected);
        }
    }
    
    private void addDrinksFromContainer(List<String> drinks, LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            TextView itemName = child.findViewById(R.id.drinkItemName);
            TextView quantity = child.findViewById(R.id.drinkQuantity);
            
            if (itemName != null) {
                drinks.add(itemName.getText().toString() + " x" + quantity.getText().toString());
            }
        }
    }
    
    private void clearForm() {
        // Clear patient information
        patientFirstNameInput.setText("");
        patientLastNameInput.setText("");
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
    }
    
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}