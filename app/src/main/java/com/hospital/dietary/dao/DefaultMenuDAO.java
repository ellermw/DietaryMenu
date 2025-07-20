package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.ArrayList;
import java.util.List;

public class DefaultMenuDAO {
    private static final String TAG = "DefaultMenuDAO";
    private DatabaseHelper dbHelper;

    public DefaultMenuDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Get default menu items for a specific diet, day, and meal
     */
    public List<DefaultMenuItem> getDefaultMenuItems(String dietType, String dayOfWeek, String mealType) {
        List<DefaultMenuItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "diet_type = ? AND day_of_week = ? AND meal_type = ? AND is_active = 1";
        String[] selectionArgs = {dietType, dayOfWeek, mealType};

        try (Cursor cursor = db.query("DefaultMenuItems", null, selection, selectionArgs,
                null, null, "sort_order ASC, item_name ASC")) {

            while (cursor.moveToNext()) {
                items.add(createDefaultMenuItemFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting default menu items", e);
        }

        return items;
    }

    /**
     * Save default menu items for a specific diet, day, and meal
     */
    public boolean saveDefaultMenuItems(String dietType, String dayOfWeek, String mealType, List<DefaultMenuItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            // Delete existing items for this combination
            String whereClause = "diet_type = ? AND day_of_week = ? AND meal_type = ?";
            String[] whereArgs = {dietType, dayOfWeek, mealType};
            db.delete("DefaultMenuItems", whereClause, whereArgs);

            // Insert new items
            for (int i = 0; i < items.size(); i++) {
                DefaultMenuItem item = items.get(i);
                ContentValues values = new ContentValues();

                values.put("diet_type", dietType);
                values.put("day_of_week", dayOfWeek);
                values.put("meal_type", mealType);
                values.put("item_name", item.getItemName());
                values.put("category", item.getCategory());
                values.put("description", item.getDescription());
                values.put("is_active", 1);
                values.put("sort_order", i);

                long result = db.insert("DefaultMenuItems", null, values);
                if (result == -1) {
                    throw new Exception("Failed to insert item: " + item.getItemName());
                }
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Successfully saved " + items.size() + " menu items for " + dietType + " " + mealType + " on " + dayOfWeek);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error saving default menu items", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Reset menu items to system defaults
     */
    public boolean resetToSystemDefaults(String dietType, String mealType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            // Delete all existing items for this diet and meal type
            String whereClause = "diet_type = ? AND meal_type = ?";
            String[] whereArgs = {dietType, mealType};
            db.delete("DefaultMenuItems", whereClause, whereArgs);

            // Insert system defaults based on diet type and meal type
            insertSystemDefaults(db, dietType, mealType);

            db.setTransactionSuccessful();
            Log.d(TAG, "Successfully reset " + dietType + " " + mealType + " to system defaults");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error resetting to system defaults", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Insert system default menu items
     */
    private void insertSystemDefaults(SQLiteDatabase db, String dietType, String mealType) {
        List<DefaultMenuItem> defaults = new ArrayList<>();

        if ("Breakfast".equals(mealType)) {
            // Default breakfast items (same for all diet types with some variations)
            defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Scrambled Eggs", "Protein"));
            defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Toast", "Starch"));
            defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Orange Juice", "Beverage"));
            defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Coffee", "Beverage"));

            if ("ADA".equals(dietType)) {
                defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Sugar-Free Syrup", "Condiment"));
                defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "2% Milk", "Dairy"));
            } else if ("Cardiac".equals(dietType)) {
                defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Low-Sodium Butter", "Condiment"));
                defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Fresh Fruit", "Fruit"));
            } else { // Regular
                defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Butter", "Condiment"));
                defaults.add(new DefaultMenuItem(dietType, "All Days", mealType, "Whole Milk", "Dairy"));
            }
        } else {
            // For lunch and dinner, create defaults for each day of the week
            String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

            for (String day : daysOfWeek) {
                if ("Lunch".equals(mealType)) {
                    addLunchDefaults(defaults, dietType, day);
                } else if ("Dinner".equals(mealType)) {
                    addDinnerDefaults(defaults, dietType, day);
                }
            }
        }

        // Insert all defaults
        for (int i = 0; i < defaults.size(); i++) {
            DefaultMenuItem item = defaults.get(i);
            ContentValues values = new ContentValues();

            values.put("diet_type", item.getDietType());
            values.put("day_of_week", item.getDayOfWeek());
            values.put("meal_type", item.getMealType());
            values.put("item_name", item.getItemName());
            values.put("category", item.getCategory());
            values.put("description", item.getDescription());
            values.put("is_active", 1);
            values.put("sort_order", i);

            db.insert("DefaultMenuItems", null, values);
        }
    }

    private void addLunchDefaults(List<DefaultMenuItem> defaults, String dietType, String day) {
        // Rotate main dishes by day
        String[] mainDishes = {"Grilled Chicken", "Turkey Sandwich", "Beef Stew", "Baked Fish", "Pasta", "Soup & Salad", "Chicken Salad"};
        String[] starches = {"Rice", "Mashed Potatoes", "Pasta", "Baked Potato", "Bread Roll", "Quinoa", "Sweet Potato"};
        String[] vegetables = {"Green Beans", "Broccoli", "Carrots", "Mixed Vegetables", "Corn", "Peas", "Spinach"};

        int dayIndex = getDayIndex(day);

        defaults.add(new DefaultMenuItem(dietType, day, "Lunch", mainDishes[dayIndex], "Main Dish"));
        defaults.add(new DefaultMenuItem(dietType, day, "Lunch", starches[dayIndex], "Starch"));
        defaults.add(new DefaultMenuItem(dietType, day, "Lunch", vegetables[dayIndex], "Vegetable"));

        // Add diet-specific items
        if ("ADA".equals(dietType)) {
            defaults.add(new DefaultMenuItem(dietType, day, "Lunch", "Sugar-Free Pudding", "Dessert"));
            defaults.add(new DefaultMenuItem(dietType, day, "Lunch", "Diet Soda", "Beverage"));
        } else if ("Cardiac".equals(dietType)) {
            defaults.add(new DefaultMenuItem(dietType, day, "Lunch", "Fresh Fruit", "Dessert"));
            defaults.add(new DefaultMenuItem(dietType, day, "Lunch", "Herbal Tea", "Beverage"));
        } else {
            defaults.add(new DefaultMenuItem(dietType, day, "Lunch", "Ice Cream", "Dessert"));
            defaults.add(new DefaultMenuItem(dietType, day, "Lunch", "Iced Tea", "Beverage"));
        }
    }

    private void addDinnerDefaults(List<DefaultMenuItem> defaults, String dietType, String day) {
        // Rotate main dishes by day
        String[] mainDishes = {"Baked Chicken", "Meat Loaf", "Grilled Salmon", "Pork Chops", "Beef Roast", "Fish & Chips", "BBQ Chicken"};
        String[] starches = {"Mashed Potatoes", "Rice Pilaf", "Baked Potato", "Pasta", "Dinner Roll", "Wild Rice", "Garlic Bread"};
        String[] vegetables = {"Steamed Broccoli", "Glazed Carrots", "Green Bean Almondine", "Corn", "Mixed Vegetables", "Asparagus", "Brussels Sprouts"};

        int dayIndex = getDayIndex(day);

        defaults.add(new DefaultMenuItem(dietType, day, "Dinner", mainDishes[dayIndex], "Main Dish"));
        defaults.add(new DefaultMenuItem(dietType, day, "Dinner", starches[dayIndex], "Starch"));
        defaults.add(new DefaultMenuItem(dietType, day, "Dinner", vegetables[dayIndex], "Vegetable"));

        // Add diet-specific items
        if ("ADA".equals(dietType)) {
            defaults.add(new DefaultMenuItem(dietType, day, "Dinner", "Sugar-Free Jello", "Dessert"));
            defaults.add(new DefaultMenuItem(dietType, day, "Dinner", "2% Milk", "Beverage"));
        } else if ("Cardiac".equals(dietType)) {
            defaults.add(new DefaultMenuItem(dietType, day, "Dinner", "Sorbet", "Dessert"));
            defaults.add(new DefaultMenuItem(dietType, day, "Dinner", "Low-Sodium Broth", "Beverage"));
        } else {
            defaults.add(new DefaultMenuItem(dietType, day, "Dinner", "Chocolate Cake", "Dessert"));
            defaults.add(new DefaultMenuItem(dietType, day, "Dinner", "Coffee", "Beverage"));
        }
    }

    private int getDayIndex(String day) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < days.length; i++) {
            if (days[i].equals(day)) {
                return i;
            }
        }
        return 0; // Default to Monday
    }

    /**
     * Get all available diet types that can have default menus
     */
    public List<String> getAvailableDietTypes() {
        List<String> dietTypes = new ArrayList<>();
        dietTypes.add("Regular");
        dietTypes.add("ADA");
        dietTypes.add("Cardiac");
        return dietTypes;
    }

    /**
     * Check if default menu items exist for a specific combination
     */
    public boolean hasDefaultMenuItems(String dietType, String dayOfWeek, String mealType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "diet_type = ? AND day_of_week = ? AND meal_type = ? AND is_active = 1";
        String[] selectionArgs = {dietType, dayOfWeek, mealType};

        try (Cursor cursor = db.query("DefaultMenuItems", new String[]{"COUNT(*)"},
                selection, selectionArgs, null, null, null)) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for default menu items", e);
        }

        return false;
    }

    /**
     * Apply default menu items to a patient based on their diet type
     */
    public List<DefaultMenuItem> getDefaultsForPatientDiet(String dietType, String dayOfWeek, String mealType) {
        // If it's a breakfast request, always use "All Days"
        if ("Breakfast".equalsIgnoreCase(mealType)) {
            dayOfWeek = "All Days";
        }

        return getDefaultMenuItems(dietType, dayOfWeek, mealType);
    }

    private DefaultMenuItem createDefaultMenuItemFromCursor(Cursor cursor) {
        DefaultMenuItem item = new DefaultMenuItem();

        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        item.setDietType(cursor.getString(cursor.getColumnIndexOrThrow("diet_type")));
        item.setDayOfWeek(cursor.getString(cursor.getColumnIndexOrThrow("day_of_week")));
        item.setMealType(cursor.getString(cursor.getColumnIndexOrThrow("meal_type")));
        item.setItemName(cursor.getString(cursor.getColumnIndexOrThrow("item_name")));
        item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));

        int descIndex = cursor.getColumnIndex("description");
        if (descIndex >= 0) {
            item.setDescription(cursor.getString(descIndex));
        }

        int activeIndex = cursor.getColumnIndex("is_active");
        if (activeIndex >= 0) {
            item.setActive(cursor.getInt(activeIndex) == 1);
        }

        int sortIndex = cursor.getColumnIndex("sort_order");
        if (sortIndex >= 0) {
            item.setSortOrder(cursor.getInt(sortIndex));
        }

        return item;
    }
}