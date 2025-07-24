package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.ArrayList;
import java.util.List;

/**
 * DefaultMenuDAO class for managing default menu configurations
 */
public class DefaultMenuDAO {

    private DatabaseHelper dbHelper;

    public DefaultMenuDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Get default menu items by diet type, meal type, and day of week
     */
    public List<DefaultMenuItem> getDefaultMenuItems(String dietType, String mealType, String dayOfWeek) {
        List<DefaultMenuItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM default_menu_items " +
                "WHERE diet_type = ? AND meal_type = ? AND day_of_week = ? " +
                "ORDER BY item_name";

        Cursor cursor = db.rawQuery(query, new String[]{dietType, mealType, dayOfWeek});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(cursorToDefaultMenuItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return items;
    }

    /**
     * Save default menu items for a specific configuration
     */
    public boolean saveDefaultMenuItems(String dietType, String mealType, String dayOfWeek,
                                        List<DefaultMenuItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = true;

        db.beginTransaction();
        try {
            // First, delete existing items for this configuration
            db.delete("default_menu_items",
                    "diet_type = ? AND meal_type = ? AND day_of_week = ?",
                    new String[]{dietType, mealType, dayOfWeek});

            // Then insert new items
            for (DefaultMenuItem item : items) {
                ContentValues values = new ContentValues();
                values.put("diet_type", dietType);
                values.put("meal_type", mealType);
                values.put("day_of_week", dayOfWeek);
                values.put("item_name", item.getItemName());
                values.put("item_category", item.getCategory());
                values.put("description", item.getDescription());
                values.put("is_active", item.isActive() ? 1 : 0);

                long result = db.insert("default_menu_items", null, values);
                if (result == -1) {
                    success = false;
                    break;
                }
            }

            if (success) {
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return success;
    }

    /**
     * Reset menu to defaults for a specific configuration
     */
    public boolean resetToDefaults(String dietType, String mealType, String dayOfWeek) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = true;

        db.beginTransaction();
        try {
            // Delete current items for this configuration
            db.delete("default_menu_items",
                    "diet_type = ? AND meal_type = ? AND day_of_week = ?",
                    new String[]{dietType, mealType, dayOfWeek});

            // Insert default items based on diet and meal type
            List<DefaultMenuItem> defaultItems = createDefaultItems(dietType, mealType, dayOfWeek);

            for (DefaultMenuItem item : defaultItems) {
                ContentValues values = new ContentValues();
                values.put("diet_type", dietType);
                values.put("meal_type", mealType);
                values.put("day_of_week", dayOfWeek);
                values.put("item_name", item.getItemName());
                values.put("item_category", item.getCategory());
                values.put("description", item.getDescription());
                values.put("is_active", 1);

                long result = db.insert("default_menu_items", null, values);
                if (result == -1) {
                    success = false;
                    break;
                }
            }

            if (success) {
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return success;
    }

    /**
     * Add a default menu item
     */
    public long addDefaultMenuItem(DefaultMenuItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("diet_type", item.getDietType());
        values.put("meal_type", item.getMealType());
        values.put("day_of_week", item.getDayOfWeek());
        values.put("item_name", item.getItemName());
        values.put("item_category", item.getCategory());
        values.put("description", item.getDescription());
        values.put("is_active", item.isActive() ? 1 : 0);

        return db.insert("default_menu_items", null, values);
    }

    /**
     * Update a default menu item
     */
    public int updateDefaultMenuItem(DefaultMenuItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("diet_type", item.getDietType());
        values.put("meal_type", item.getMealType());
        values.put("day_of_week", item.getDayOfWeek());
        values.put("item_name", item.getItemName());
        values.put("item_category", item.getCategory());
        values.put("description", item.getDescription());
        values.put("is_active", item.isActive() ? 1 : 0);

        return db.update("default_menu_items", values, "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /**
     * Delete a default menu item
     */
    public int deleteDefaultMenuItem(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("default_menu_items", "id = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Get all default menu items
     */
    public List<DefaultMenuItem> getAllDefaultMenuItems() {
        List<DefaultMenuItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM default_menu_items ORDER BY diet_type, meal_type, day_of_week, item_name";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(cursorToDefaultMenuItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return items;
    }

    /**
     * Create default items for a specific configuration
     */
    private List<DefaultMenuItem> createDefaultItems(String dietType, String mealType, String dayOfWeek) {
        List<DefaultMenuItem> items = new ArrayList<>();

        // Add default items based on diet and meal type
        if ("Breakfast".equals(mealType)) {
            DefaultMenuItem eggs = new DefaultMenuItem("Scrambled Eggs", dietType, mealType, dayOfWeek);
            eggs.setCategory("Main");
            items.add(eggs);

            DefaultMenuItem toast = new DefaultMenuItem("Toast", dietType, mealType, dayOfWeek);
            toast.setCategory("Bread");
            items.add(toast);

            DefaultMenuItem juice = new DefaultMenuItem("Orange Juice", dietType, mealType, dayOfWeek);
            juice.setCategory("Beverage");
            items.add(juice);

            if (!"ADA Diabetic".equals(dietType)) {
                DefaultMenuItem pancakes = new DefaultMenuItem("Pancakes", dietType, mealType, dayOfWeek);
                pancakes.setCategory("Main");
                items.add(pancakes);
            }
        } else if ("Lunch".equals(mealType)) {
            DefaultMenuItem chicken = new DefaultMenuItem("Grilled Chicken", dietType, mealType, dayOfWeek);
            chicken.setCategory("Main");
            items.add(chicken);

            DefaultMenuItem rice = new DefaultMenuItem("Rice", dietType, mealType, dayOfWeek);
            rice.setCategory("Side");
            items.add(rice);

            DefaultMenuItem beans = new DefaultMenuItem("Green Beans", dietType, mealType, dayOfWeek);
            beans.setCategory("Vegetable");
            items.add(beans);

            DefaultMenuItem salad = new DefaultMenuItem("Garden Salad", dietType, mealType, dayOfWeek);
            salad.setCategory("Salad");
            items.add(salad);
        } else if ("Dinner".equals(mealType)) {
            DefaultMenuItem fish = new DefaultMenuItem("Baked Fish", dietType, mealType, dayOfWeek);
            fish.setCategory("Main");
            items.add(fish);

            DefaultMenuItem potatoes = new DefaultMenuItem("Mashed Potatoes", dietType, mealType, dayOfWeek);
            potatoes.setCategory("Side");
            items.add(potatoes);

            DefaultMenuItem broccoli = new DefaultMenuItem("Steamed Broccoli", dietType, mealType, dayOfWeek);
            broccoli.setCategory("Vegetable");
            items.add(broccoli);

            DefaultMenuItem roll = new DefaultMenuItem("Dinner Roll", dietType, mealType, dayOfWeek);
            roll.setCategory("Bread");
            items.add(roll);
        }

        return items;
    }

    /**
     * Convert cursor to DefaultMenuItem object
     */
    private DefaultMenuItem cursorToDefaultMenuItem(Cursor cursor) {
        DefaultMenuItem item = new DefaultMenuItem();

        item.setId(cursor.getInt(cursor.getColumnIndex("id")));
        item.setDietType(cursor.getString(cursor.getColumnIndex("diet_type")));
        item.setMealType(cursor.getString(cursor.getColumnIndex("meal_type")));
        item.setDayOfWeek(cursor.getString(cursor.getColumnIndex("day_of_week")));
        item.setItemName(cursor.getString(cursor.getColumnIndex("item_name")));
        item.setCategory(cursor.getString(cursor.getColumnIndex("item_category")));
        item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        item.setActive(cursor.getInt(cursor.getColumnIndex("is_active")) == 1);

        return item;
    }
}