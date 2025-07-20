package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.DefaultMenuDAO;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.*;

public class DefaultMenuManagementActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private DefaultMenuDAO defaultMenuDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Spinner dietTypeSpinner;
    private Spinner dayOfWeekSpinner;
    private Spinner mealTypeSpinner;
    private ListView menuItemsListView;
    private Button addItemButton;
    private Button saveChangesButton;
    private Button resetToDefaultButton;
    private TextView instructionsText;

    // Data
    private List<DefaultMenuItem> currentMenuItems = new ArrayList<>();
    private DefaultMenuAdapter menuAdapter;

    // Diet types that can have default menus
    private String[] dietTypes = {
            "Regular",
            "Cardiac",
            "ADA"
    };

    // Days of the week
    private String[] daysOfWeek = {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    };

    // Meal types
    private String[] mealTypes = {
            "Breakfast",
            "Lunch",
            "Dinner"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_menu_management);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Validate admin access
        if (!"admin".equals(currentUserRole)) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        defaultMenuDAO = new DefaultMenuDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Default Menu Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupSpinners();
        setupListeners();
        loadMenuItems();
    }

    private void initializeUI() {
        dietTypeSpinner = findViewById(R.id.dietTypeSpinner);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);
        mealTypeSpinner = findViewById(R.id.mealTypeSpinner);
        menuItemsListView = findViewById(R.id.menuItemsListView);
        addItemButton = findViewById(R.id.addItemButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        resetToDefaultButton = findViewById(R.id.resetToDefaultButton);
        instructionsText = findViewById(R.id.instructionsText);

        // Set instructions
        instructionsText.setText("Configure default menu items for Regular, ADA, and Cardiac diets. " +
                "These defaults will be automatically applied when creating new patients with these diet types. " +
                "For Breakfast, you can set one default that applies to all 7 days of the week.");

        // Initialize adapter
        menuAdapter = new DefaultMenuAdapter(this, currentMenuItems);
        menuItemsListView.setAdapter(menuAdapter);
    }

    private void setupSpinners() {
        // Diet type spinner
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietTypeSpinner.setAdapter(dietAdapter);

        // Day of week spinner
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(dayAdapter);

        // Meal type spinner
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mealTypes);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(mealAdapter);
    }

    private void setupListeners() {
        // Spinner listeners to reload menu items when selection changes
        AdapterView.OnItemSelectedListener reloadListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadMenuItems();
                updateBreakfastSpecialHandling();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        dietTypeSpinner.setOnItemSelectedListener(reloadListener);
        dayOfWeekSpinner.setOnItemSelectedListener(reloadListener);
        mealTypeSpinner.setOnItemSelectedListener(reloadListener);

        // Button listeners
        addItemButton.setOnClickListener(v -> showAddItemDialog());
        saveChangesButton.setOnClickListener(v -> saveMenuChanges());
        resetToDefaultButton.setOnClickListener(v -> resetToDefaults());
    }

    private void updateBreakfastSpecialHandling() {
        String selectedMeal = mealTypes[mealTypeSpinner.getSelectedItemPosition()];
        boolean isBreakfast = "Breakfast".equals(selectedMeal);

        if (isBreakfast) {
            dayOfWeekSpinner.setEnabled(false);
            instructionsText.setText("Breakfast defaults apply to ALL days of the week. " +
                    "Set your breakfast items here and they will be used for every day.");
        } else {
            dayOfWeekSpinner.setEnabled(true);
            instructionsText.setText("Configure default menu items for each specific day and meal. " +
                    "These will be automatically applied when creating new patients.");
        }
    }

    private void loadMenuItems() {
        try {
            String dietType = dietTypes[dietTypeSpinner.getSelectedItemPosition()];
            String dayOfWeek = daysOfWeek[dayOfWeekSpinner.getSelectedItemPosition()];
            String mealType = mealTypes[mealTypeSpinner.getSelectedItemPosition()];

            // For breakfast, ignore day of week (applies to all days)
            if ("Breakfast".equals(mealType)) {
                dayOfWeek = "All Days";
            }

            currentMenuItems.clear();
            currentMenuItems.addAll(defaultMenuDAO.getDefaultMenuItems(dietType, dayOfWeek, mealType));

            menuAdapter.notifyDataSetChanged();

            // Update save button state
            saveChangesButton.setEnabled(hasChanges());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading menu items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddItemDialog() {
        // Create dialog for adding new menu item
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Menu Item");

        // Create input layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // Item name input
        final EditText itemNameInput = new EditText(this);
        itemNameInput.setHint("Enter item name (e.g., Grilled Chicken)");
        layout.addView(itemNameInput);

        // Category spinner
        final Spinner categorySpinner = new Spinner(this);
        String[] categories = {
                "Main Dish", "Side Dish", "Vegetable", "Starch", "Protein",
                "Dessert", "Beverage", "Juice", "Dairy", "Fruit", "Soup"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category:");
        categoryLabel.setPadding(0, 20, 0, 10);
        layout.addView(categoryLabel);
        layout.addView(categorySpinner);

        // Description input
        final EditText descriptionInput = new EditText(this);
        descriptionInput.setHint("Optional description");
        TextView descLabel = new TextView(this);
        descLabel.setText("Description:");
        descLabel.setPadding(0, 20, 0, 10);
        layout.addView(descLabel);
        layout.addView(descriptionInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemName = itemNameInput.getText().toString().trim();
            String category = categories[categorySpinner.getSelectedItemPosition()];
            String description = descriptionInput.getText().toString().trim();

            if (itemName.isEmpty()) {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add new item to list
            DefaultMenuItem newItem = new DefaultMenuItem();
            newItem.setItemName(itemName);
            newItem.setCategory(category);
            newItem.setDescription(description);
            newItem.setDietType(dietTypes[dietTypeSpinner.getSelectedItemPosition()]);
            newItem.setDayOfWeek(mealTypes[mealTypeSpinner.getSelectedItemPosition()].equals("Breakfast") ?
                    "All Days" : daysOfWeek[dayOfWeekSpinner.getSelectedItemPosition()]);
            newItem.setMealType(mealTypes[mealTypeSpinner.getSelectedItemPosition()]);

            currentMenuItems.add(newItem);
            menuAdapter.notifyDataSetChanged();
            saveChangesButton.setEnabled(true);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveMenuChanges() {
        try {
            String dietType = dietTypes[dietTypeSpinner.getSelectedItemPosition()];
            String dayOfWeek = daysOfWeek[dayOfWeekSpinner.getSelectedItemPosition()];
            String mealType = mealTypes[mealTypeSpinner.getSelectedItemPosition()];

            // For breakfast, save to all days
            if ("Breakfast".equals(mealType)) {
                for (String day : daysOfWeek) {
                    boolean success = defaultMenuDAO.saveDefaultMenuItems(dietType, day, mealType, currentMenuItems);
                    if (!success) {
                        throw new Exception("Failed to save breakfast items for " + day);
                    }
                }
                Toast.makeText(this, "Breakfast defaults saved for all days!", Toast.LENGTH_LONG).show();
            } else {
                boolean success = defaultMenuDAO.saveDefaultMenuItems(dietType, dayOfWeek, mealType, currentMenuItems);
                if (success) {
                    Toast.makeText(this, "Menu items saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    throw new Exception("Failed to save menu items");
                }
            }

            saveChangesButton.setEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving menu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void resetToDefaults() {
        String dietType = dietTypes[dietTypeSpinner.getSelectedItemPosition()];
        String mealType = mealTypes[mealTypeSpinner.getSelectedItemPosition()];

        new AlertDialog.Builder(this)
                .setTitle("Reset to Defaults")
                .setMessage("Reset " + dietType + " " + mealType + " menu to system defaults?\n\nThis will remove all custom items.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    try {
                        defaultMenuDAO.resetToSystemDefaults(dietType, mealType);
                        loadMenuItems();
                        Toast.makeText(this, "Menu reset to defaults", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error resetting menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean hasChanges() {
        // Simple check - in a real implementation you'd compare with saved state
        return !currentMenuItems.isEmpty();
    }

    // Custom adapter for menu items
    private class DefaultMenuAdapter extends BaseAdapter {
        private List<DefaultMenuItem> items;

        public DefaultMenuAdapter(DefaultMenuManagementActivity context, List<DefaultMenuItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
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

            DefaultMenuItem item = items.get(position);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(item.getItemName());
            text2.setText(item.getCategory() +
                    (item.getDescription() != null && !item.getDescription().isEmpty() ?
                            " - " + item.getDescription() : ""));

            // Add click listener for editing/removing
            convertView.setOnClickListener(v -> showItemOptions(position, item));

            return convertView;
        }

        private void showItemOptions(int position, DefaultMenuItem item) {
            new AlertDialog.Builder(DefaultMenuManagementActivity.this)
                    .setTitle(item.getItemName())
                    .setMessage("What would you like to do with this item?")
                    .setPositiveButton("Edit", (dialog, which) -> showEditItemDialog(position, item))
                    .setNeutralButton("Remove", (dialog, which) -> removeItem(position))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void showEditItemDialog(int position, DefaultMenuItem item) {
            // Similar to add dialog but pre-filled with existing values
            AlertDialog.Builder builder = new AlertDialog.Builder(DefaultMenuManagementActivity.this);
            builder.setTitle("Edit Menu Item");

            LinearLayout layout = new LinearLayout(DefaultMenuManagementActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 40);

            final EditText itemNameInput = new EditText(DefaultMenuManagementActivity.this);
            itemNameInput.setText(item.getItemName());
            layout.addView(itemNameInput);

            final EditText descriptionInput = new EditText(DefaultMenuManagementActivity.this);
            descriptionInput.setText(item.getDescription() != null ? item.getDescription() : "");
            descriptionInput.setHint("Optional description");
            layout.addView(descriptionInput);

            builder.setView(layout);

            builder.setPositiveButton("Save", (dialog, which) -> {
                item.setItemName(itemNameInput.getText().toString().trim());
                item.setDescription(descriptionInput.getText().toString().trim());
                notifyDataSetChanged();
                saveChangesButton.setEnabled(true);
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        private void removeItem(int position) {
            items.remove(position);
            notifyDataSetChanged();
            saveChangesButton.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog();
                } else {
                    finish();
                }
                return true;
            case R.id.action_home:
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.putExtra("current_user", currentUsername);
                intent.putExtra("user_role", currentUserRole);
                intent.putExtra("user_full_name", currentUserFullName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean hasUnsavedChanges() {
        return saveChangesButton.isEnabled();
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Do you want to save them before leaving?")
                .setPositiveButton("Save", (dialog, which) -> {
                    saveMenuChanges();
                    finish();
                })
                .setNeutralButton("Discard", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            super.onBackPressed();
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