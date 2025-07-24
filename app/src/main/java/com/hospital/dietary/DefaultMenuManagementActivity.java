package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.DefaultMenuDAO;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultMenuManagementActivity extends AppCompatActivity {

    private static final String TAG = "DefaultMenuManagement";

    private DatabaseHelper dbHelper;
    private DefaultMenuDAO defaultMenuDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Spinner dietTypeSpinner;
    private Spinner mealTypeSpinner;
    private Spinner dayOfWeekSpinner;
    private ListView menuItemsListView;
    private Button addItemButton;
    private Button saveChangesButton;
    private Button resetToDefaultsButton;
    private TextView instructionsText;
    private TextView currentConfigText;

    // Data
    private List<DefaultMenuItem> currentMenuItems = new ArrayList<>();
    private DefaultMenuAdapter menuAdapter;

    // Current selection
    private String selectedDietType = "";
    private String selectedMealType = "";
    private String selectedDayOfWeek = "";

    // Options arrays
    private String[] dietTypes = {"Regular", "Cardiac", "ADA Diabetic"};
    private String[] mealTypes = {"Breakfast", "Lunch", "Dinner"};
    private String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_menu_management);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Check if user has admin privileges
        if (!"Admin".equals(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Default Menu Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        defaultMenuDAO = new DefaultMenuDAO(dbHelper);

        // Initialize UI
        initializeUI();
        setupListeners();

        // Set initial selection
        if (dietTypeSpinner.getCount() > 0) {
            dietTypeSpinner.setSelection(0);
        }
    }

    private void initializeUI() {
        dietTypeSpinner = findViewById(R.id.dietTypeSpinner);
        mealTypeSpinner = findViewById(R.id.mealTypeSpinner);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);
        menuItemsListView = findViewById(R.id.menuItemsListView);
        addItemButton = findViewById(R.id.addItemButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        resetToDefaultsButton = findViewById(R.id.resetToDefaultsButton);
        instructionsText = findViewById(R.id.instructionsText);
        currentConfigText = findViewById(R.id.currentConfigText);

        // Setup spinners
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietTypeSpinner.setAdapter(dietAdapter);

        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mealTypes);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(mealAdapter);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, daysOfWeek);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(dayAdapter);

        // Setup list view
        menuAdapter = new DefaultMenuAdapter();
        menuItemsListView.setAdapter(menuAdapter);

        // Set instructions
        instructionsText.setText("Configure default menu selections for each diet type, meal, and day of the week. " +
                "These defaults will be automatically applied to new patient orders until modified.");
    }

    private void setupListeners() {
        // Spinner listeners
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSelection();
                loadMenuItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        dietTypeSpinner.setOnItemSelectedListener(spinnerListener);
        mealTypeSpinner.setOnItemSelectedListener(spinnerListener);
        dayOfWeekSpinner.setOnItemSelectedListener(spinnerListener);

        // Add item button
        addItemButton.setOnClickListener(v -> showAddItemDialog());

        // Save changes button
        saveChangesButton.setOnClickListener(v -> saveMenuChanges());

        // Reset to defaults button
        resetToDefaultsButton.setOnClickListener(v -> confirmResetToDefaults());
    }

    private void updateSelection() {
        selectedDietType = dietTypeSpinner.getSelectedItem().toString();
        selectedMealType = mealTypeSpinner.getSelectedItem().toString();
        selectedDayOfWeek = dayOfWeekSpinner.getSelectedItem().toString();

        currentConfigText.setText(String.format("Configuring: %s - %s - %s",
                selectedDietType, selectedMealType, selectedDayOfWeek));
    }

    private void loadMenuItems() {
        try {
            currentMenuItems.clear();
            currentMenuItems = defaultMenuDAO.getDefaultMenuItems(
                    selectedDietType, selectedMealType, selectedDayOfWeek);

            if (currentMenuItems == null) {
                currentMenuItems = new ArrayList<>();
            }

            menuAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error loading menu items", e);
            Toast.makeText(this, "Error loading menu items", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddItemDialog() {
        // Create a simple dialog with EditText and Spinner
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        EditText itemNameEdit = new EditText(this);
        itemNameEdit.setHint("Item Name");
        layout.addView(itemNameEdit);

        Spinner categorySpinner = new Spinner(this);
        String[] categories = getCategoriesForMeal(selectedMealType);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        layout.addView(categorySpinner);

        new AlertDialog.Builder(this)
                .setTitle("Add Menu Item")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String itemName = itemNameEdit.getText().toString().trim();
                    String category = categorySpinner.getSelectedItem().toString();

                    if (!itemName.isEmpty()) {
                        addMenuItem(itemName, category);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String[] getCategoriesForMeal(String mealType) {
        switch (mealType) {
            case "Breakfast":
                return new String[]{"Hot Cereal", "Cold Cereal", "Bread", "Main", "Fruit", "Juice", "Drink"};
            case "Lunch":
            case "Dinner":
                return new String[]{"Protein", "Starch", "Vegetable", "Dessert", "Juice", "Drink"};
            default:
                return new String[]{"Other"};
        }
    }

    private void addMenuItem(String itemName, String category) {
        DefaultMenuItem newItem = new DefaultMenuItem();
        newItem.setDietType(selectedDietType);
        newItem.setMealType(selectedMealType);
        newItem.setDayOfWeek(selectedDayOfWeek);
        newItem.setCategory(category);
        newItem.setItemName(itemName);
        newItem.setActive(true);

        currentMenuItems.add(newItem);
        menuAdapter.notifyDataSetChanged();
    }

    private void saveMenuChanges() {
        try {
            // Save all items for current selection
            boolean success = defaultMenuDAO.saveDefaultMenuItems(
                    selectedDietType, selectedMealType, selectedDayOfWeek, currentMenuItems);

            if (success) {
                Toast.makeText(this, "Menu changes saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving menu changes", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving menu changes", e);
            Toast.makeText(this, "Error saving changes: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmResetToDefaults() {
        new AlertDialog.Builder(this)
                .setTitle("Reset to Defaults")
                .setMessage("This will reset ALL default menus to system defaults. Continue?")
                .setPositiveButton("Reset", (dialog, which) -> resetToDefaults())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetToDefaults() {
        try {
            // Clear all existing default menus
            defaultMenuDAO.clearAllDefaultMenus();

            // Add basic defaults
            createBasicDefaults();

            loadMenuItems();
            Toast.makeText(this, "Reset to defaults completed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error resetting to defaults", e);
            Toast.makeText(this, "Error resetting: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void createBasicDefaults() {
        // Create some basic default items for each diet type
        // This is a simplified version - you can expand this as needed

        // Regular Diet - Breakfast - Sunday
        List<DefaultMenuItem> regularBreakfastItems = new ArrayList<>();
        regularBreakfastItems.add(createDefaultItem("Regular", "Breakfast", "Sunday", "Hot Cereal", "Oatmeal"));
        regularBreakfastItems.add(createDefaultItem("Regular", "Breakfast", "Sunday", "Bread", "White Toast"));
        regularBreakfastItems.add(createDefaultItem("Regular", "Breakfast", "Sunday", "Juice", "Orange Juice"));
        defaultMenuDAO.saveDefaultMenuItems("Regular", "Breakfast", "Sunday", regularBreakfastItems);
    }

    private DefaultMenuItem createDefaultItem(String diet, String meal, String day, String category, String item) {
        DefaultMenuItem menuItem = new DefaultMenuItem();
        menuItem.setDietType(diet);
        menuItem.setMealType(meal);
        menuItem.setDayOfWeek(day);
        menuItem.setCategory(category);
        menuItem.setItemName(item);
        menuItem.setActive(true);
        return menuItem;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Use the existing menu_main if specific menu doesn't exist
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                loadMenuItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Default Menu Management Help")
                .setMessage("This feature allows you to configure default menu selections for each diet type:\n\n" +
                        "• Regular Diet\n" +
                        "• Cardiac Diet\n" +
                        "• ADA Diabetic Diet\n\n" +
                        "Configure defaults for each meal (Breakfast, Lunch, Dinner) and each day of the week. " +
                        "These defaults will automatically populate new patient orders.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // Adapter for menu items
    private class DefaultMenuAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return currentMenuItems.size();
        }

        @Override
        public DefaultMenuItem getItem(int position) {
            return currentMenuItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            DefaultMenuItem item = getItem(position);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(item.getItemName());
            text2.setText("Category: " + item.getCategory());

            // Add delete functionality with long press
            convertView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(DefaultMenuManagementActivity.this)
                        .setTitle("Delete Item")
                        .setMessage("Delete " + item.getItemName() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            currentMenuItems.remove(position);
                            notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });

            return convertView;
        }
    }
}