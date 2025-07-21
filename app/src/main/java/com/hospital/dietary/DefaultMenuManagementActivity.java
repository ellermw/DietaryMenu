package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.dao.DefaultMenuDAO;
import com.hospital.dietary.models.Item;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultMenuManagementActivity extends AppCompatActivity {

    private static final String TAG = "DefaultMenuManagement";

    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
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
    private Button addMenuItemButton;
    private Button saveChangesButton;
    private Button resetToDefaultsButton;
    private TextView helpText;
    private TextView breakfastNote;

    // Data
    private List<String> dietTypes = Arrays.asList("Regular", "ADA", "Cardiac");
    private List<String> daysOfWeek = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private List<String> mealTypes = Arrays.asList("Breakfast", "Lunch", "Dinner");
    private List<DefaultMenuItem> currentMenuItems = new ArrayList<>();
    private DefaultMenuAdapter menuAdapter;
    private List<Item> availableItems = new ArrayList<>();

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
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        defaultMenuDAO = new DefaultMenuDAO(dbHelper);

        // Set up toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Default Menu Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupListeners();
        loadAvailableItems();
        loadCurrentMenuItems();
    }

    private void initializeUI() {
        dietTypeSpinner = findViewById(R.id.dietTypeSpinner);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);
        mealTypeSpinner = findViewById(R.id.mealTypeSpinner);
        menuItemsListView = findViewById(R.id.menuItemsListView);
        addMenuItemButton = findViewById(R.id.addMenuItemButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        resetToDefaultsButton = findViewById(R.id.resetToDefaultsButton);
        helpText = findViewById(R.id.helpText);
        breakfastNote = findViewById(R.id.breakfastNote);

        // Set up spinners
        setupSpinners();

        // Set up list view
        menuAdapter = new DefaultMenuAdapter(this, currentMenuItems);
        menuItemsListView.setAdapter(menuAdapter);

        // Set help text
        helpText.setText(getString(R.string.default_menu_help));
    }

    private void setupSpinners() {
        // Diet Type Spinner
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietTypes);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietTypeSpinner.setAdapter(dietAdapter);

        // Day of Week Spinner
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(dayAdapter);

        // Meal Type Spinner
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mealTypes);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealTypeSpinner.setAdapter(mealAdapter);
    }

    private void setupListeners() {
        // Spinner change listeners
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadCurrentMenuItems();
                updateDaySpinnerVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        dietTypeSpinner.setOnItemSelectedListener(filterListener);
        dayOfWeekSpinner.setOnItemSelectedListener(filterListener);
        mealTypeSpinner.setOnItemSelectedListener(filterListener);

        // Button listeners
        addMenuItemButton.setOnClickListener(v -> showAddMenuItemDialog());
        saveChangesButton.setOnClickListener(v -> saveCurrentChanges());
        resetToDefaultsButton.setOnClickListener(v -> showResetConfirmation());
    }

    private void updateDaySpinnerVisibility() {
        // Hide day spinner for breakfast since it applies to all days
        String selectedMeal = mealTypes.get(mealTypeSpinner.getSelectedItemPosition());
        if ("Breakfast".equals(selectedMeal)) {
            dayOfWeekSpinner.setVisibility(View.GONE);
            findViewById(R.id.dayOfWeekLabel).setVisibility(View.GONE);
            if (breakfastNote != null) {
                breakfastNote.setVisibility(View.VISIBLE);
                breakfastNote.setText(getString(R.string.breakfast_applies_all_days));
            }
        } else {
            dayOfWeekSpinner.setVisibility(View.VISIBLE);
            findViewById(R.id.dayOfWeekLabel).setVisibility(View.VISIBLE);
            if (breakfastNote != null) {
                breakfastNote.setVisibility(View.GONE);
            }
        }
    }

    private void loadAvailableItems() {
        try {
            availableItems.clear();
            availableItems.addAll(itemDAO.getAllItems());
        } catch (Exception e) {
            Toast.makeText(this, "Error loading items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCurrentMenuItems() {
        try {
            String dietType = dietTypes.get(dietTypeSpinner.getSelectedItemPosition());
            String mealType = mealTypes.get(mealTypeSpinner.getSelectedItemPosition());
            String dayOfWeek = "Breakfast".equals(mealType) ? "All Days" :
                    daysOfWeek.get(dayOfWeekSpinner.getSelectedItemPosition());

            currentMenuItems.clear();
            currentMenuItems.addAll(defaultMenuDAO.getDefaultMenuItems(dietType, mealType, dayOfWeek));
            menuAdapter.notifyDataSetChanged();

            // Update title
            String title = dietType + " - " + mealType;
            if (!"Breakfast".equals(mealType)) {
                title += " - " + dayOfWeek;
            }
            setTitle("Default Menu: " + title);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading menu items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddMenuItemDialog() {
        // Filter available items based on current diet type
        String dietType = dietTypes.get(dietTypeSpinner.getSelectedItemPosition());
        List<Item> filteredItems = new ArrayList<>();

        for (Item item : availableItems) {
            // For ADA diet, only show ADA-friendly items
            if ("ADA".equals(dietType) && !item.isAdaFriendly()) {
                continue;
            }
            // Don't show items already in the current menu
            boolean alreadyAdded = false;
            for (DefaultMenuItem menuItem : currentMenuItems) {
                if (menuItem.getItemId() == item.getItemId()) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                filteredItems.add(item);
            }
        }

        if (filteredItems.isEmpty()) {
            Toast.makeText(this, "No available items to add", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog
        String[] itemNames = new String[filteredItems.size()];
        for (int i = 0; i < filteredItems.size(); i++) {
            itemNames[i] = filteredItems.get(i).getName() + " (" + filteredItems.get(i).getCategory() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Add Menu Item")
                .setItems(itemNames, (dialog, which) -> {
                    Item selectedItem = filteredItems.get(which);
                    addMenuItemToList(selectedItem);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addMenuItemToList(Item item) {
        String dietType = dietTypes.get(dietTypeSpinner.getSelectedItemPosition());
        String mealType = mealTypes.get(mealTypeSpinner.getSelectedItemPosition());
        String dayOfWeek = "Breakfast".equals(mealType) ? "All Days" :
                daysOfWeek.get(dayOfWeekSpinner.getSelectedItemPosition());

        DefaultMenuItem menuItem = new DefaultMenuItem();
        menuItem.setItemId(item.getItemId());
        menuItem.setItemName(item.getName());
        menuItem.setDietType(dietType);
        menuItem.setMealType(mealType);
        menuItem.setDayOfWeek(dayOfWeek);

        currentMenuItems.add(menuItem);
        menuAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Added: " + item.getName(), Toast.LENGTH_SHORT).show();
    }

    private void saveCurrentChanges() {
        try {
            String dietType = dietTypes.get(dietTypeSpinner.getSelectedItemPosition());
            String mealType = mealTypes.get(mealTypeSpinner.getSelectedItemPosition());
            String dayOfWeek = "Breakfast".equals(mealType) ? "All Days" :
                    daysOfWeek.get(dayOfWeekSpinner.getSelectedItemPosition());

            // Save current menu items
            boolean success = defaultMenuDAO.saveDefaultMenuItems(dietType, mealType, dayOfWeek, currentMenuItems);

            if (success) {
                Toast.makeText(this, "Default menu saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save default menu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Reset to Defaults")
                .setMessage("Are you sure you want to reset the current menu to system defaults? This will remove all current items.")
                .setPositiveButton("Reset", (dialog, which) -> resetToDefaults())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetToDefaults() {
        try {
            String dietType = dietTypes.get(dietTypeSpinner.getSelectedItemPosition());
            String mealType = mealTypes.get(mealTypeSpinner.getSelectedItemPosition());
            String dayOfWeek = "Breakfast".equals(mealType) ? "All Days" :
                    daysOfWeek.get(dayOfWeekSpinner.getSelectedItemPosition());

            boolean success = defaultMenuDAO.resetToDefaults(dietType, mealType, dayOfWeek);

            if (success) {
                loadCurrentMenuItems(); // Refresh the display
                Toast.makeText(this, "Menu reset to defaults", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to reset menu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error resetting menu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // No options menu needed for this activity
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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

    // Inner class for the adapter
    private class DefaultMenuAdapter extends BaseAdapter {
        private Context context;
        private List<DefaultMenuItem> items;

        public DefaultMenuAdapter(Context context, List<DefaultMenuItem> items) {
            this.context = context;
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
                convertView = LayoutInflater.from(context).inflate(R.layout.item_default_menu, parent, false);
            }

            DefaultMenuItem item = items.get(position);

            TextView itemNameText = convertView.findViewById(R.id.itemNameText);
            TextView itemDescriptionText = convertView.findViewById(R.id.itemDescriptionText);
            Button removeItemButton = convertView.findViewById(R.id.removeItemButton);

            itemNameText.setText(item.getItemName());

            // Show description if available
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                itemDescriptionText.setText(item.getDescription());
                itemDescriptionText.setVisibility(View.VISIBLE);
            } else {
                itemDescriptionText.setVisibility(View.GONE);
            }

            // Remove button click listener
            removeItemButton.setOnClickListener(v -> showRemoveItemConfirmation(position, item));

            return convertView;
        }

        private void showRemoveItemConfirmation(int position, DefaultMenuItem item) {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Item")
                    .setMessage("Remove '" + item.getItemName() + "' from this menu?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        items.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}