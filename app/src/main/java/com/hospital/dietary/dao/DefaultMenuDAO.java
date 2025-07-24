package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.ArrayList;
import java.util.List;

public class DefaultMenuDAO {
    private DatabaseHelper dbHelper;

    public DefaultMenuDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Get default menu items for a specific diet, meal, and day
    public List<DefaultMenuItem> getDefaultMenuItems(String dietType, String mealType, String dayOfWeek) {
        List<DefaultMenuItem> menuItems = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM default_menu WHERE diet_type = ? AND meal_type = ? AND day_of_week = ?";
        Cursor cursor = db.rawQuery(query, new String[]{dietType, mealType, dayOfWeek});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                DefaultMenuItem item = new DefaultMenuItem();
                item.setId(cursor.getInt(cursor.getColumnIndex("menu_id")));
                item.setDietType(cursor.getString(cursor.getColumnIndex("diet_type")));
                item.setMealType(cursor.getString(cursor.getColumnIndex("meal_type")));
                item.setDayOfWeek(cursor.getString(cursor.getColumnIndex("day_of_week")));
                item.setCategory(cursor.getString(cursor.getColumnIndex("item_category")));
                item.setItemName(cursor.getString(cursor.getColumnIndex("item_name")));
                item.setActive(cursor.getInt(cursor.getColumnIndex("is_active")) == 1);
                menuItems.add(item);
            }
            cursor.close();
        }

        return menuItems;
    }

    // Save default menu items for a specific configuration
    public boolean saveDefaultMenuItems(String dietType, String mealType, String dayOfWeek, List<DefaultMenuItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();
        try {
            // First delete existing items for this configuration
            db.delete("default_menu",
                    "diet_type = ? AND meal_type = ? AND day_of_week = ?",
                    new String[]{dietType, mealType, dayOfWeek});

            // Insert new items
            for (DefaultMenuItem item : items) {
                ContentValues values = new ContentValues();
                values.put("diet_type", item.getDietType());
                values.put("meal_type", item.getMealType());
                values.put("day_of_week", item.getDayOfWeek());
                values.put("item_category", item.getCategory());
                values.put("item_name", item.getItemName());
                values.put("is_active", item.isActive() ? 1 : 0);

                db.insert("default_menu", null, values);
            }

            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return success;
    }

    // Clear all default menu items
    public void clearAllDefaultMenus() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("default_menu", null, null);
    }

    // Get all default menu items
    public List<DefaultMenuItem> getAllDefaultMenuItems() {
        List<DefaultMenuItem> menuItems = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("default_menu", null, null, null, null, null,
                "diet_type, meal_type, day_of_week, item_category");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                DefaultMenuItem item = new DefaultMenuItem();
                item.setId(cursor.getInt(cursor.getColumnIndex("menu_id")));
                item.setDietType(cursor.getString(cursor.getColumnIndex("diet_type")));
                item.setMealType(cursor.getString(cursor.getColumnIndex("meal_type")));
                item.setDayOfWeek(cursor.getString(cursor.getColumnIndex("day_of_week")));
                item.setCategory(cursor.getString(cursor.getColumnIndex("item_category")));
                item.setItemName(cursor.getString(cursor.getColumnIndex("item_name")));
                item.setActive(cursor.getInt(cursor.getColumnIndex("is_active")) == 1);
                menuItems.add(item);
            }
            cursor.close();
        }

        return menuItems;
    }
}