package com.hospital.dietary;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Item;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

public class MealPlanningActivity extends AppCompatActivity {

    private static final String TAG = "MealPlanningActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private ItemDAO itemDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // Patient info
    private Patient currentPatient;
    private String diet;
    private boolean isAdaDiet;

    // UI Components
    private TextView patientNameTextView, locationTextView, dietTextView;
    private LinearLayout breakfastItemsContainer, lunchItemsContainer, dinnerItemsContainer;
    private Button completeOrderButton, saveProgressButton;

    // Meal completion status
    private boolean breakfastComplete = false;
    private boolean lunchComplete = false;
    private boolean dinnerComplete = false;

    // Regular diet spinners
    private Spinner lunchProteinSpinner, lunchStarchSpinner, lunchVegetableSpinner, lunchDessertSpinner;
    private Spinner dinnerProteinSpinner, dinnerStarchSpinner, dinnerVegetableSpinner, dinnerDessertSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);

        // Get data from intent
        long patientId = getIntent().getLongExtra("patient_id", -1);
        diet = getIntent().getStringExtra("diet");
        isAdaDiet = getIntent().getBooleanExtra("is_ada_diet", false);
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        itemDAO = new ItemDAO(dbHelper);

        // Load patient data
        currentPatient = patientDAO.getPatientById((int) patientId);

        if (currentPatient == null) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Meal Planning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        populatePatientInfo();
        loadMealItems();
        setupListeners();
    }

    private void initializeViews() {
        // Patient info display
        patientNameTextView = findViewById(R.id.patientNameTextView);
        locationTextView = findViewById(R.id.locationTextView);
        dietTextView = findViewById(R.id.dietTextView);

        // Meal containers
        breakfastItemsContainer = findViewById(R.id.breakfastItemsContainer);
        lunchItemsContainer = findViewById(R.id.lunchItemsContainer);
        dinnerItemsContainer = findViewById(R.id.dinnerItemsContainer);

        // Buttons
        completeOrderButton = findViewById(R.id.completeOrderButton);
        saveProgressButton = findViewById(R.id.saveProgressButton);
    }

    private void populatePatientInfo() {
        if (currentPatient != null) {
            patientNameTextView.setText(currentPatient.getFullName());
            locationTextView.setText(currentPatient.getLocationInfo());
            dietTextView.setText(currentPatient.getDiet());
        }
    }

    private void loadMealItems() {
        // Clear containers
        breakfastItemsContainer.removeAllViews();
        lunchItemsContainer.removeAllViews();
        dinnerItemsContainer.removeAllViews();

        // Check diet type and load appropriate meal items
        if (diet != null) {
            if (diet.contains("Clear Liquid") || diet.contains("Full Liquid") || diet.contains("Puree")) {
                loadPredeterminedMealItems();
            } else {
                loadRegularMealItems();
            }
        }
    }

    private void loadPredeterminedMealItems() {
        // Add meal sections with proper titles and individual diet management
        addPredeterminedMealSection(breakfastItemsContainer, "Breakfast", currentPatient.getBreakfastDiet(), currentPatient.isBreakfastAda());
        addPredeterminedMealSection(lunchItemsContainer, "Lunch", currentPatient.getLunchDiet(), currentPatient.isLunchAda());
        addPredeterminedMealSection(dinnerItemsContainer, "Dinner", currentPatient.getDinnerDiet(), currentPatient.isDinnerAda());
    }

    // FIXED: Remove duplicate (ADA) display issue and add individual meal diet editing
    private void addPredeterminedMealSection(LinearLayout container, String mealType, String mealDiet, boolean mealIsAda) {
        // Create meal header with edit button
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setPadding(0, 8, 0, 8);

        TextView title = new TextView(this);

        // FIXED: Clean up diet display to avoid duplicate (ADA)
        String cleanDietDisplay;
        if (mealDiet.contains("(ADA)") || mealIsAda) {
            // If diet already contains (ADA) or meal is ADA, format it properly
            if (mealDiet.contains("(ADA)")) {
                // Diet already has (ADA) in it, use as is
                cleanDietDisplay = mealDiet;
            } else {
                // mealIsAda is true but diet doesn't contain (ADA), add it once
                cleanDietDisplay = mealDiet + " (ADA)";
            }
        } else {
            // Not an ADA diet, use diet as is
            cleanDietDisplay = mealDiet;
        }

        title.setText(mealType + " - " + cleanDietDisplay);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        // Add edit button for individual meal diet
        Button editMealButton = new Button(this);
        editMealButton.setText("Edit Diet");
        editMealButton.setTextSize(12);
        editMealButton.setPadding(16, 8, 16, 8);
        editMealButton.setOnClickListener(v -> showEditMealDietDialog(mealType, mealDiet, mealIsAda));

        headerLayout.addView(title);
        headerLayout.addView(editMealButton);
        container.addView(headerLayout);

        // Get specific items based on diet type and meal
        String itemsList = getPredeterminedItemsForMeal(mealDiet, mealType, mealIsAda);

        TextView itemsText = new TextView(this);
        itemsText.setText(itemsList);
        itemsText.setPadding(16, 8, 16, 8);
        itemsText.setTextSize(14);
        container.addView(itemsText);
    }

    private void showEditMealDietDialog(String mealType, String currentMealDiet, boolean currentMealAda) {
        // Create dialog for editing individual meal diet
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_meal_diet, null);

        Spinner mealDietSpinner = dialogView.findViewById(R.id.mealDietSpinner);
        LinearLayout mealAdaToggleLayout = dialogView.findViewById(R.id.mealAdaToggleLayout);
        Switch mealAdaSwitch = dialogView.findViewById(R.id.mealAdaSwitch);

        // Setup diet spinner
        String[] diets = {"Regular", "ADA", "Cardiac", "Renal", "Clear Liquid", "Full Liquid", "Puree"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealDietSpinner.setAdapter(dietAdapter);

        // Set current values
        String baseDiet = currentMealDiet.replace(" (ADA)", "");
        int dietPosition = dietAdapter.getPosition(baseDiet);
        mealDietSpinner.setSelection(dietPosition);
        mealAdaSwitch.setChecked(currentMealAda);

        // Setup ADA toggle visibility
        mealDietSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiet = mealDietSpinner.getSelectedItem().toString();

                // Show ADA toggle only for Clear Liquid, Full Liquid, and Puree diets
                if (selectedDiet.equals("Clear Liquid") || selectedDiet.equals("Full Liquid") || selectedDiet.equals("Puree")) {
                    mealAdaToggleLayout.setVisibility(View.VISIBLE);
                } else {
                    mealAdaToggleLayout.setVisibility(View.GONE);
                    mealAdaSwitch.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Trigger initial ADA toggle visibility
        String initialDiet = mealDietSpinner.getSelectedItem().toString();
        if (initialDiet.equals("Clear Liquid") || initialDiet.equals("Full Liquid") || initialDiet.equals("Puree")) {
            mealAdaToggleLayout.setVisibility(View.VISIBLE);
        } else {
            mealAdaToggleLayout.setVisibility(View.GONE);
        }

        builder.setTitle("Edit " + mealType + " Diet")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newDiet = mealDietSpinner.getSelectedItem().toString();
                    boolean newAda = mealAdaSwitch.isChecked();
                    saveMealDietChange(mealType, newDiet, newAda);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveMealDietChange(String mealType, String newDiet, boolean isAda) {
        try {
            // Update the patient's individual meal diet
            boolean success = patientDAO.updateMealDiet(currentPatient.getPatientId(), mealType, newDiet, isAda);

            if (success) {
                // Update local patient object
                switch (mealType.toLowerCase()) {
                    case "breakfast":
                        currentPatient.setBreakfastDiet(newDiet);
                        currentPatient.setBreakfastAda(isAda);
                        break;
                    case "lunch":
                        currentPatient.setLunchDiet(newDiet);
                        currentPatient.setLunchAda(isAda);
                        break;
                    case "dinner":
                        currentPatient.setDinnerDiet(newDiet);
                        currentPatient.setDinnerAda(isAda);
                        break;
                }

                Toast.makeText(this, mealType + " diet updated successfully!", Toast.LENGTH_SHORT).show();

                // Reload meal items to reflect changes
                loadMealItems();

            } else {
                Toast.makeText(this, "Failed to update " + mealType.toLowerCase() + " diet", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error updating meal diet: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error updating meal diet", e);
        }
    }

    private String getPredeterminedItemsForMeal(String dietType, String mealType, boolean isADA) {
        StringBuilder items = new StringBuilder();

        if (dietType.contains("Clear Liquid")) {
            // Clear Liquid diet items
            if (isADA) {
                items.append("• Apple Juice (ADA)\n");
                items.append("• Sprite Zero (ADA)\n");
                items.append("• Sugar Free Jello (ADA)\n");
            } else {
                items.append("• Orange Juice\n");
                items.append("• Sprite\n");
                items.append("• Jello\n");
            }
            items.append("• Clear Broth\n");
            items.append("• Water\n");
            items.append("• Tea/Coffee\n");

        } else if (dietType.contains("Full Liquid")) {
            // Full Liquid diet items
            switch (mealType) {
                case "Breakfast":
                    items.append("• Apple Juice (120ml)\n");
                    items.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• Cream of Wheat\n");
                    items.append("• Coffee (200ml)\n");
                    items.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    items.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    items.append("• Ensure (240ml)\n");
                    break;

                case "Lunch":
                    items.append("• Cranberry Juice (120ml)\n");
                    items.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• Cream of Chicken Soup\n");
                    items.append("• Pudding").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    items.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    items.append("• Ensure (240ml)\n");
                    break;

                case "Dinner":
                    items.append("• Apple Juice (120ml)\n");
                    items.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• Tomato Soup\n");
                    items.append("• Pudding").append(isADA ? " (Sugar Free)" : "").append("\n");
                    items.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    items.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    items.append("• Ensure (240ml)\n");
                    break;
            }

        } else if (dietType.contains("Puree")) {
            // Puree diet items
            switch (mealType) {
                case "Breakfast":
                    items.append("• Pureed Scrambled Eggs\n");
                    items.append("• Pureed Oatmeal\n");
                    items.append("• Apple Juice\n");
                    items.append("• Coffee\n");
                    break;
                case "Lunch":
                    items.append("• Pureed Chicken\n");
                    items.append("• Pureed Potatoes\n");
                    items.append("• Pureed Vegetables\n");
                    items.append("• Thickened Liquids\n");
                    break;
                case "Dinner":
                    items.append("• Pureed Beef\n");
                    items.append("• Pureed Rice\n");
                    items.append("• Pureed Carrots\n");
                    items.append("• Pudding").append(isADA ? " (Sugar Free)" : "").append("\n");
                    break;
            }
        }

        return items.toString();
    }

    // FIXED: Proper implementation for regular meal items with dropdown spinners
    private void loadRegularMealItems() {
        // Clear containers
        if (breakfastItemsContainer != null) breakfastItemsContainer.removeAllViews();
        if (lunchItemsContainer != null) lunchItemsContainer.removeAllViews();
        if (dinnerItemsContainer != null) dinnerItemsContainer.removeAllViews();

        // Add breakfast header (usually minimal for regular diets)
        addBreakfastItems();

        // Add lunch items with dropdown spinners
        addLunchItems();

        // Add dinner items with dropdown spinners
        addDinnerItems();
    }

    private void addBreakfastItems() {
        if (breakfastItemsContainer == null) return;

        TextView breakfastTitle = new TextView(this);
        breakfastTitle.setText("Breakfast");
        breakfastTitle.setTextSize(16);
        breakfastTitle.setTypeface(null, Typeface.BOLD);
        breakfastTitle.setPadding(0, 8, 0, 8);
        breakfastItemsContainer.addView(breakfastTitle);

        TextView breakfastNote = new TextView(this);
        breakfastNote.setText("Standard breakfast items will be provided based on diet type.");
        breakfastNote.setPadding(16, 4, 16, 8);
        breakfastNote.setTextSize(14);
        breakfastItemsContainer.addView(breakfastNote);

        breakfastComplete = true; // Mark as complete for regular diets
    }

    private void addLunchItems() {
        if (lunchItemsContainer == null) return;

        TextView lunchTitle = new TextView(this);
        lunchTitle.setText("Lunch");
        lunchTitle.setTextSize(16);
        lunchTitle.setTypeface(null, Typeface.BOLD);
        lunchTitle.setPadding(0, 8, 0, 8);
        lunchItemsContainer.addView(lunchTitle);

        // Add spinners for lunch components
        addMealComponentSpinner(lunchItemsContainer, "Protein", "lunchProtein");
        addMealComponentSpinner(lunchItemsContainer, "Starch", "lunchStarch");
        addMealComponentSpinner(lunchItemsContainer, "Vegetable", "lunchVegetable");
        addMealComponentSpinner(lunchItemsContainer, "Dessert", "lunchDessert");
    }

    private void addDinnerItems() {
        if (dinnerItemsContainer == null) return;

        TextView dinnerTitle = new TextView(this);
        dinnerTitle.setText("Dinner");
        dinnerTitle.setTextSize(16);
        dinnerTitle.setTypeface(null, Typeface.BOLD);
        dinnerTitle.setPadding(0, 8, 0, 8);
        dinnerItemsContainer.addView(dinnerTitle);

        // Add spinners for dinner components
        addMealComponentSpinner(dinnerItemsContainer, "Protein", "dinnerProtein");
        addMealComponentSpinner(dinnerItemsContainer, "Starch", "dinnerStarch");
        addMealComponentSpinner(dinnerItemsContainer, "Vegetable", "dinnerVegetable");
        addMealComponentSpinner(dinnerItemsContainer, "Dessert", "dinnerDessert");
    }

    // FIXED: Proper implementation of spinner creation with data loading
    private void addMealComponentSpinner(LinearLayout container, String category, String tag) {
        TextView label = new TextView(this);
        label.setText(category + ":");
        label.setTextSize(14);
        label.setTypeface(null, Typeface.BOLD);
        label.setPadding(16, 8, 16, 4);
        container.addView(label);

        Spinner spinner = new Spinner(this);
        spinner.setTag(tag);

        // Load items for this category
        List<Item> categoryItems = getFilteredItemsByCategory(category);
        List<String> itemNames = new ArrayList<>();
        itemNames.add("Select " + category);

        for (Item item : categoryItems) {
            itemNames.add(item.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMealCompletion(tag);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Store spinner references for later use
        if ("lunchProtein".equals(tag)) lunchProteinSpinner = spinner;
        else if ("lunchStarch".equals(tag)) lunchStarchSpinner = spinner;
        else if ("lunchVegetable".equals(tag)) lunchVegetableSpinner = spinner;
        else if ("lunchDessert".equals(tag)) lunchDessertSpinner = spinner;
        else if ("dinnerProtein".equals(tag)) dinnerProteinSpinner = spinner;
        else if ("dinnerStarch".equals(tag)) dinnerStarchSpinner = spinner;
        else if ("dinnerVegetable".equals(tag)) dinnerVegetableSpinner = spinner;
        else if ("dinnerDessert".equals(tag)) dinnerDessertSpinner = spinner;

        container.addView(spinner);
    }

    // FIXED: Helper method to get filtered items based on diet type
    private List<Item> getFilteredItemsByCategory(String category) {
        try {
            List<Item> allItems = itemDAO.getItemsByCategory(category);
            List<Item> filteredItems = new ArrayList<>();

            for (Item item : allItems) {
                // Filter based on diet type
                boolean includeItem = true;

                // For ADA diets, only include ADA-friendly items
                if (isAdaDiet || diet.contains("ADA")) {
                    includeItem = item.isAdaFriendly();
                }

                if (includeItem) {
                    filteredItems.add(item);
                }
            }

            return filteredItems;

        } catch (Exception e) {
            Log.e(TAG, "Error loading items for category: " + category, e);
            return new ArrayList<>();
        }
    }

    private void updateMealCompletion(String tag) {
        if (tag.startsWith("lunch")) {
            // Check if at least one lunch item is selected
            lunchComplete = (lunchProteinSpinner != null && lunchProteinSpinner.getSelectedItemPosition() > 0) ||
                    (lunchStarchSpinner != null && lunchStarchSpinner.getSelectedItemPosition() > 0) ||
                    (lunchVegetableSpinner != null && lunchVegetableSpinner.getSelectedItemPosition() > 0) ||
                    (lunchDessertSpinner != null && lunchDessertSpinner.getSelectedItemPosition() > 0);
        } else if (tag.startsWith("dinner")) {
            // Check if at least one dinner item is selected
            dinnerComplete = (dinnerProteinSpinner != null && dinnerProteinSpinner.getSelectedItemPosition() > 0) ||
                    (dinnerStarchSpinner != null && dinnerStarchSpinner.getSelectedItemPosition() > 0) ||
                    (dinnerVegetableSpinner != null && dinnerVegetableSpinner.getSelectedItemPosition() > 0) ||
                    (dinnerDessertSpinner != null && dinnerDessertSpinner.getSelectedItemPosition() > 0);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean allMealsComplete = breakfastComplete && lunchComplete && dinnerComplete;
        completeOrderButton.setEnabled(allMealsComplete);

        if (allMealsComplete) {
            completeOrderButton.setText("Complete Order");
        } else {
            completeOrderButton.setText("Complete Order (Missing Selections)");
        }
    }

    private void setupListeners() {
        saveProgressButton.setOnClickListener(v -> saveProgress());
        completeOrderButton.setOnClickListener(v -> completeOrder());
    }

    private void saveProgress() {
        try {
            // Save current selections to patient record
            saveCurrentSelections();
            Toast.makeText(this, "Progress saved successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving progress: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error saving progress", e);
        }
    }

    private void completeOrder() {
        if (!breakfastComplete || !lunchComplete || !dinnerComplete) {
            Toast.makeText(this, "Please complete all meal selections before finishing", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Save selections and mark meals as complete
            saveCurrentSelections();

            // Mark all meals as complete in the database
            currentPatient.setBreakfastComplete(true);
            currentPatient.setLunchComplete(true);
            currentPatient.setDinnerComplete(true);

            boolean success = patientDAO.updatePatient(currentPatient);

            if (success) {
                Toast.makeText(this, "Order completed successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error completing order", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error completing order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error completing order", e);
        }
    }

    private void saveCurrentSelections() {
        // Save spinner selections for regular diets
        if (diet != null && !diet.contains("Clear Liquid") && !diet.contains("Full Liquid") && !diet.contains("Puree")) {
            saveRegularMealSelections();
        }
        // For predetermined diets, items are already defined, no need to save selections
    }

    private void saveRegularMealSelections() {
        StringBuilder breakfastItems = new StringBuilder("Standard breakfast items");
        StringBuilder lunchItems = new StringBuilder();
        StringBuilder dinnerItems = new StringBuilder();

        // Build lunch selections
        if (lunchProteinSpinner != null && lunchProteinSpinner.getSelectedItemPosition() > 0) {
            lunchItems.append(lunchProteinSpinner.getSelectedItem().toString());
        }
        if (lunchStarchSpinner != null && lunchStarchSpinner.getSelectedItemPosition() > 0) {
            if (lunchItems.length() > 0) lunchItems.append(", ");
            lunchItems.append(lunchStarchSpinner.getSelectedItem().toString());
        }
        if (lunchVegetableSpinner != null && lunchVegetableSpinner.getSelectedItemPosition() > 0) {
            if (lunchItems.length() > 0) lunchItems.append(", ");
            lunchItems.append(lunchVegetableSpinner.getSelectedItem().toString());
        }
        if (lunchDessertSpinner != null && lunchDessertSpinner.getSelectedItemPosition() > 0) {
            if (lunchItems.length() > 0) lunchItems.append(", ");
            lunchItems.append(lunchDessertSpinner.getSelectedItem().toString());
        }

        // Build dinner selections
        if (dinnerProteinSpinner != null && dinnerProteinSpinner.getSelectedItemPosition() > 0) {
            dinnerItems.append(dinnerProteinSpinner.getSelectedItem().toString());
        }
        if (dinnerStarchSpinner != null && dinnerStarchSpinner.getSelectedItemPosition() > 0) {
            if (dinnerItems.length() > 0) dinnerItems.append(", ");
            dinnerItems.append(dinnerStarchSpinner.getSelectedItem().toString());
        }
        if (dinnerVegetableSpinner != null && dinnerVegetableSpinner.getSelectedItemPosition() > 0) {
            if (dinnerItems.length() > 0) dinnerItems.append(", ");
            dinnerItems.append(dinnerVegetableSpinner.getSelectedItem().toString());
        }
        if (dinnerDessertSpinner != null && dinnerDessertSpinner.getSelectedItemPosition() > 0) {
            if (dinnerItems.length() > 0) dinnerItems.append(", ");
            dinnerItems.append(dinnerDessertSpinner.getSelectedItem().toString());
        }

        // Update patient with selections
        currentPatient.setBreakfastItems(breakfastItems.toString());
        currentPatient.setLunchItems(lunchItems.toString());
        currentPatient.setDinnerItems(dinnerItems.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_meal_planning, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                saveProgress();
                return true;
            case R.id.action_complete:
                completeOrder();
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