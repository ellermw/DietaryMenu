package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.DefaultMenuDAO;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.ArrayList;
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

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        defaultMenuDAO = new DefaultMenuDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Default Menu Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupSpinners();
        setupListeners();
        updateInstructions();
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

        // Setup menu items adapter
        menuAdapter = new DefaultMenuAdapter();
        menuItemsListView.setAdapter(menuAdapter);

        // Initially disable buttons until a valid selection is made
        updateButtonStates();
    }

    private void setupSpinners() {
        // Diet Type Spinner
        String[] dietTypes = {"Select Diet Type", "Regular", "ADA", "Cardiac", "Renal", "Clear Liquid", "Full Liquid", "Puree", "Mechanical Chopped", "Mechanical Ground"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietTypeSpinner.setAdapter(dietAdapter);

        // Meal Type Spinner
        String[] mealTypes = {"Select Meal Type", "Breakfast", "Lunch", "Dinner"};
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mealTypes);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(mealAdapter);

        // Day of Week Spinner (Breakfast uses "All Days", Lunch/Dinner use specific days)
        String[] daysOfWeek = {"Select Day", "All Days", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(dayAdapter);
    }

    private void setupListeners() {
        // Diet type selection
        dietTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDietType = position > 0 ? parent.getItemAtPosition(position).toString() : "";
                updateCurrentSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Meal type selection
        mealTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMealType = position > 0 ? parent.getItemAtPosition(position).toString() : "";
                updateDaySpinnerOptions();
                updateCurrentSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Day of week selection
        dayOfWeekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDayOfWeek = position > 0 ? parent.getItemAtPosition(position).toString() : "";
                updateCurrentSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Button listeners
        addItemButton.setOnClickListener(v -> showAddItemDialog());
        saveChangesButton.setOnClickListener(v -> saveCurrentConfiguration());
        resetToDefaultsButton.setOnClickListener(v -> showResetConfirmationDialog());

        // Menu item long click for deletion
        menuItemsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteItemDialog(position);
            return true;
        });
    }

    private void updateDaySpinnerOptions() {
        // For breakfast, only "All Days" should be available
        // For lunch and dinner, specific days should be available
        if ("Breakfast".equals(selectedMealType)) {
            String[] breakfastDays = {"Select Day", "All Days"};
            ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, breakfastDays);
            dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dayOfWeekSpinner.setAdapter(dayAdapter);
        } else {
            String[] allDays = {"Select Day", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allDays);
            dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dayOfWeekSpinner.setAdapter(dayAdapter);
        }

        // Reset day selection when meal type changes
        selectedDayOfWeek = "";
        dayOfWeekSpinner.setSelection(0);
    }

    private void updateCurrentSelection() {
        boolean hasValidSelection = !selectedDietType.isEmpty() && !selectedMealType.isEmpty() && !selectedDayOfWeek.isEmpty();

        if (hasValidSelection) {
            loadMenuItemsForSelection();
            updateCurrentConfigText();
        } else {
            currentMenuItems.clear();
            menuAdapter.notifyDataSetChanged();
            currentConfigText.setText("Select diet type, meal type, and day to configure default menu items.");
        }

        updateButtonStates();
    }

    private void loadMenuItemsForSelection() {
        try {
            currentMenuItems.clear();
            List<DefaultMenuItem> items = defaultMenuDAO.getDefaultMenuItems(selectedDietType, selectedMealType, selectedDayOfWeek);
            currentMenuItems.addAll(items);
            menuAdapter.notifyDataSetChanged();

            Log.d(TAG, "Loaded " + items.size() + " menu items for " + selectedDietType + " " + selectedMealType + " " + selectedDayOfWeek);
        } catch (Exception e) {
            Log.e(TAG, "Error loading menu items", e);
            Toast.makeText(this, "Error loading menu items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCurrentConfigText() {
        String configText = "Configuring: " + selectedDietType + " Diet - " + selectedMealType + " - " + selectedDayOfWeek;
        currentConfigText.setText(configText);
    }

    private void updateInstructions() {
        String instructions = "Configure default menu items for each diet type, meal, and day combination.\n\n" +
                "• Select diet type, meal type, and day\n" +
                "• Add menu items that will automatically be applied to new patients\n" +
                "• Items can be customized for each patient later in meal planning\n" +
                "• Breakfast items apply to all days, lunch/dinner items are day-specific";
        instructionsText.setText(instructions);
    }

    private void updateButtonStates() {
        boolean hasValidSelection = !selectedDietType.isEmpty() && !selectedMealType.isEmpty() && !selectedDayOfWeek.isEmpty();
        boolean hasItems = !currentMenuItems.isEmpty();

        addItemButton.setEnabled(hasValidSelection);
        saveChangesButton.setEnabled(hasValidSelection && hasItems);
        resetToDefaultsButton.setEnabled(hasValidSelection);

        addItemButton.setAlpha(hasValidSelection ? 1.0f : 0.5f);
        saveChangesButton.setAlpha(hasValidSelection && hasItems ? 1.0f : 0.5f);
        resetToDefaultsButton.setAlpha(hasValidSelection ? 1.0f : 0.5f);
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Menu Item");

        // Create input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter menu item name (e.g., Grilled Chicken)");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemName = input.getText().toString().trim();
            if (!itemName.isEmpty()) {
                addMenuItem(itemName);
            } else {
                Toast.makeText(this, "Please enter a menu item name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addMenuItem(String itemName) {
        // Check for duplicates
        for (DefaultMenuItem item : currentMenuItems) {
            if (itemName.equalsIgnoreCase(item.getItemName())) {
                Toast.makeText(this, "Item already exists in this menu", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create new menu item
        DefaultMenuItem newItem = new DefaultMenuItem();
        newItem.setItemName(itemName);
        newItem.setDietType(selectedDietType);
        newItem.setMealType(selectedMealType);
        newItem.setDayOfWeek(selectedDayOfWeek);
        newItem.setDescription(""); // Can be enhanced later

        currentMenuItems.add(newItem);
        menuAdapter.notifyDataSetChanged();
        updateButtonStates();

        Toast.makeText(this, "Added: " + itemName, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteItemDialog(int position) {
        if (position < 0 || position >= currentMenuItems.size()) return;

        DefaultMenuItem item = currentMenuItems.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Menu Item")
                .setMessage("Remove \"" + item.getItemName() + "\" from this menu?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    currentMenuItems.remove(position);
                    menuAdapter.notifyDataSetChanged();
                    updateButtonStates();
                    Toast.makeText(this, "Removed: " + item.getItemName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveCurrentConfiguration() {
        if (currentMenuItems.isEmpty()) {
            Toast.makeText(this, "No items to save", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            boolean success = defaultMenuDAO.saveDefaultMenuItems(selectedDietType, selectedMealType, selectedDayOfWeek, currentMenuItems);

            if (success) {
                Toast.makeText(this, "Default menu saved successfully!\n" +
                                currentMenuItems.size() + " items saved for " + selectedDietType + " " + selectedMealType + " " + selectedDayOfWeek,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to save default menu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving default menu", e);
            Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset to System Defaults")
                .setMessage("This will replace current items with system defaults for:\n\n" +
                        selectedDietType + " - " + selectedMealType + " - " + selectedDayOfWeek +
                        "\n\nAre you sure?")
                .setPositiveButton("Reset", (dialog, which) -> resetToSystemDefaults())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetToSystemDefaults() {
        try {
            boolean success = defaultMenuDAO.resetToDefaults(selectedDietType, selectedMealType, selectedDayOfWeek);

            if (success) {
                loadMenuItemsForSelection(); // Reload to show system defaults
                Toast.makeText(this, "Reset to system defaults successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to reset to defaults", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resetting to defaults", e);
            Toast.makeText(this, "Error resetting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                finish();
                return true;
            case R.id.action_home:
                finish();
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

    /**
     * Adapter for displaying default menu items
     */
    private class DefaultMenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return currentMenuItems.size();
        }

        @Override
        public Object getItem(int position) {
            return currentMenuItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_default_menu, parent, false);
            }

            DefaultMenuItem item = currentMenuItems.get(position);

            TextView itemNameText = convertView.findViewById(R.id.itemNameText);
            TextView itemInfoText = convertView.findViewById(R.id.itemInfoText);
            ImageView deleteIcon = convertView.findViewById(R.id.deleteIcon);

            itemNameText.setText(item.getItemName());
            itemInfoText.setText(selectedDietType + " • " + selectedMealType + " • " + selectedDayOfWeek);

            // Delete icon click listener
            deleteIcon.setOnClickListener(v -> showDeleteItemDialog(position));

            return convertView;
        }
    }
}