package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.DefaultMenuItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DefaultMenuDAO {

    private DatabaseHelper dbHelper;
    private static final String TAG = "DefaultMenuDAO";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public DefaultMenuDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Get default menu items for a specific diet, meal, and day
     */
    public List<DefaultMenuItem> getDefaultMenuItems(String dietType, String mealType, String dayOfWeek) {
        List<DefaultMenuItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "diet_type = ? AND meal_type = ? AND day_of_week = ?";
        String[] selectionArgs = {dietType, mealType, dayOfWeek};

        Cursor cursor = null;
        try {
            cursor = db.query("DefaultMenu", null, selection, selectionArgs, null, null, "item_name ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DefaultMenuItem item = new DefaultMenuItem();
                    item.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    item.setItemId(cursor.getInt(cursor.getColumnIndex("item_id")));
                    item.setItemName(cursor.getString(cursor.getColumnIndex("item_name")));
                    item.setDietType(cursor.getString(cursor.getColumnIndex("diet_type")));
                    item.setMealType(cursor.getString(cursor.getColumnIndex("meal_type")));
                    item.setDayOfWeek(cursor.getString(cursor.getColumnIndex("day_of_week")));

                    // Handle potentially null description
                    int descIndex = cursor.getColumnIndex("description");
                    if (descIndex != -1) {
                        item.setDescription(cursor.getString(descIndex));
                    }

                    // Handle created date
                    int dateIndex = cursor.getColumnIndex("created_date");
                    if (dateIndex != -1) {
                        item.setCreatedDate(cursor.getString(dateIndex));
                    }

                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading default menu items", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "Loaded " + items.size() + " default menu items for " + dietType + " " + mealType + " " + dayOfWeek);
        return items;
    }

    /**
     * Save default menu items for a specific combination
     */
    public boolean saveDefaultMenuItems(String dietType, String mealType, String dayOfWeek, List<DefaultMenuItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            // First, delete existing items for this combination
            String deleteSelection = "diet_type = ? AND meal_type = ? AND day_of_week = ?";
            String[] deleteArgs = {dietType, mealType, dayOfWeek};
            int deletedRows = db.delete("DefaultMenu", deleteSelection, deleteArgs);
            Log.d(TAG, "Deleted " + deletedRows + " existing items for " + dietType + " " + mealType + " " + dayOfWeek);

            // Then, insert the new items
            for (DefaultMenuItem item : items) {
                ContentValues values = new ContentValues();
                values.put("item_id", item.getItemId());
                values.put("item_name", item.getItemName());
                values.put("diet_type", dietType);
                values.put("meal_type", mealType);
                values.put("day_of_week", dayOfWeek);
                values.put("description", item.getDescription());
                values.put("created_date", getCurrentTimestamp());

                long result = db.insert("DefaultMenu", null, values);
                if (result == -1) {
                    Log.e(TAG, "Failed to insert default menu item: " + item.getItemName());
                    return false;
                }
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Successfully saved " + items.size() + " default menu items");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error saving default menu items", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Reset default menu to system defaults
     */
    public boolean resetToDefaults(String dietType, String mealType, String dayOfWeek) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            // Delete current items
            String deleteSelection = "diet_type = ? AND meal_type = ? AND day_of_week = ?";
            String[] deleteArgs = {dietType, mealType, dayOfWeek};
            int deletedRows = db.delete("DefaultMenu", deleteSelection, deleteArgs);
            Log.d(TAG, "Reset: Deleted " + deletedRows + " items for " + dietType + " " + mealType + " " + dayOfWeek);

            // Insert system defaults based on meal type and diet
            insertSystemDefaults(db, dietType, mealType, dayOfWeek);

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error resetting to defaults", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Insert system default menu items based on diet type and meal
     */
    private void insertSystemDefaults(SQLiteDatabase db, String dietType, String mealType, String dayOfWeek) {
        Log.d(TAG, "Inserting system defaults for " + dietType + " " + mealType + " " + dayOfWeek);

        if ("Breakfast".equals(mealType)) {
            // Breakfast defaults (same for all days)
            if ("Regular".equals(dietType)) {
                insertDefaultItem(db, "Scrambled Eggs", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Toast", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Orange Juice", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Coffee", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Butter", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Jelly", dietType, mealType, dayOfWeek);
            } else if ("ADA".equals(dietType)) {
                insertDefaultItem(db, "Egg White Omelet", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Whole Wheat Toast", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Sugar-Free Orange Juice", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Black Coffee", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Sugar-Free Jelly", dietType, mealType, dayOfWeek);
            } else if ("Cardiac".equals(dietType)) {
                insertDefaultItem(db, "Oatmeal", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Fresh Fruit", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Low-Sodium Orange Juice", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Herbal Tea", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Low-Fat Milk", dietType, mealType, dayOfWeek);
            }
        } else if ("Lunch".equals(mealType)) {
            // Lunch defaults (vary by day)
            if ("Regular".equals(dietType)) {
                if ("Monday".equals(dayOfWeek)) {
                    insertDefaultItem(db, "Grilled Chicken", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Rice", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Green Beans", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Dinner Roll", dietType, mealType, dayOfWeek);
                } else if ("Tuesday".equals(dayOfWeek)) {
                    insertDefaultItem(db, "Beef Stew", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Mashed Potatoes", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Carrots", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Cornbread", dietType, mealType, dayOfWeek);
                } else {
                    insertDefaultItem(db, "Baked Fish", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Baked Potato", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Mixed Vegetables", dietType, mealType, dayOfWeek);
                }
            } else if ("ADA".equals(dietType)) {
                insertDefaultItem(db, "Grilled Fish", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Brown Rice", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Steamed Broccoli", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Sugar-Free Dessert", dietType, mealType, dayOfWeek);
            } else if ("Cardiac".equals(dietType)) {
                insertDefaultItem(db, "Baked Salmon", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Quinoa", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Steamed Vegetables", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Low-Sodium Broth", dietType, mealType, dayOfWeek);
            }
        } else if ("Dinner".equals(mealType)) {
            // Dinner defaults (vary by day)
            if ("Regular".equals(dietType)) {
                if ("Monday".equals(dayOfWeek)) {
                    insertDefaultItem(db, "Roast Beef", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Roasted Potatoes", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Corn", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Chocolate Cake", dietType, mealType, dayOfWeek);
                } else if ("Tuesday".equals(dayOfWeek)) {
                    insertDefaultItem(db, "Fried Chicken", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Mac and Cheese", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Cole Slaw", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Apple Pie", dietType, mealType, dayOfWeek);
                } else {
                    insertDefaultItem(db, "Pork Chops", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Rice Pilaf", dietType, mealType, dayOfWeek);
                    insertDefaultItem(db, "Green Salad", dietType, mealType, dayOfWeek);
                }
            } else if ("ADA".equals(dietType)) {
                insertDefaultItem(db, "Lean Turkey", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Sweet Potato", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Asparagus", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Sugar-Free Pudding", dietType, mealType, dayOfWeek);
            } else if ("Cardiac".equals(dietType)) {
                insertDefaultItem(db, "Grilled Tilapia", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Wild Rice", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Steamed Spinach", dietType, mealType, dayOfWeek);
                insertDefaultItem(db, "Fresh Berries", dietType, mealType, dayOfWeek);
            }
        }
    }

    /**
     * Helper method to insert a single default item
     */
    private void insertDefaultItem(SQLiteDatabase db, String itemName, String dietType, String mealType, String dayOfWeek) {
        ContentValues values = new ContentValues();
        values.put("item_id", 0); // You can link to actual items later if needed
        values.put("item_name", itemName);
        values.put("diet_type", dietType);
        values.put("meal_type", mealType);
        values.put("day_of_week", dayOfWeek);
        values.put("created_date", getCurrentTimestamp());

        long result = db.insert("DefaultMenu", null, values);
        if (result == -1) {
            Log.e(TAG, "Failed to insert default item: " + itemName);
        } else {
            Log.d(TAG, "Inserted default item: " + itemName);
        }
    }

    /**
     * Get default menu items to apply to a new patient
     */
    public List<DefaultMenuItem> getDefaultMenuForPatient(String dietType) {
        List<DefaultMenuItem> allDefaults = new ArrayList<>();

        // Get breakfast items (apply to all days)
        allDefaults.addAll(getDefaultMenuItems(dietType, "Breakfast", "All Days"));

        // Get lunch and dinner items for each day
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : days) {
            allDefaults.addAll(getDefaultMenuItems(dietType, "Lunch", day));
            allDefaults.addAll(getDefaultMenuItems(dietType, "Dinner", day));
        }

        Log.d(TAG, "Retrieved " + allDefaults.size() + " default menu items for new " + dietType + " patient");
        return allDefaults;
    }

    /**
     * Check if default menu items exist for a specific combination
     */
    public boolean hasDefaultMenuItems(String dietType, String mealType, String dayOfWeek) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = "diet_type = ? AND meal_type = ? AND day_of_week = ?";
        String[] selectionArgs = {dietType, mealType, dayOfWeek};

        Cursor cursor = null;
        try {
            cursor = db.query("DefaultMenu", new String[]{"COUNT(*)"}, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for default menu items", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * Delete all default menu items for a specific diet type
     */
    public boolean deleteDefaultMenuForDiet(String dietType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int deletedRows = db.delete("DefaultMenu", "diet_type = ?", new String[]{dietType});
            Log.d(TAG, "Deleted " + deletedRows + " default menu items for diet: " + dietType);
            return deletedRows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting default menu items for diet: " + dietType, e);
            return false;
        }
    }

    /**
     * Get all available diet types that have default menus configured
     */
    public List<String> getAvailableDietTypes() {
        List<String> dietTypes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.query(true, "DefaultMenu", new String[]{"diet_type"}, null, null, null, null, "diet_type ASC", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    dietTypes.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting available diet types", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return dietTypes;
    }

    /**
     * Get current timestamp as string
     */
    private String getCurrentTimestamp() {
        return dateFormat.format(new Date());
    }

    /**
     * Get count of default menu items for a specific configuration
     */
    public int getItemCount(String dietType, String mealType, String dayOfWeek) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = "diet_type = ? AND meal_type = ? AND day_of_week = ?";
        String[] selectionArgs = {dietType, mealType, dayOfWeek};

        Cursor cursor = null;
        try {
            cursor = db.query("DefaultMenu", new String[]{"COUNT(*)"}, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting item count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * Update a single default menu item
     */
    public boolean updateDefaultMenuItem(DefaultMenuItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("item_name", item.getItemName());
        values.put("description", item.getDescription());

        try {
            int rowsAffected = db.update("DefaultMenu", values, "id = ?", new String[]{String.valueOf(item.getId())});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating default menu item", e);
            return false;
        }
    }

    /**
     * Copy default menu items from one diet type to another
     */
    public boolean copyDefaultMenu(String fromDietType, String toDietType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            // First, delete existing items for target diet type
            db.delete("DefaultMenu", "diet_type = ?", new String[]{toDietType});

            // Get all items from source diet type
            List<DefaultMenuItem> sourceItems = new ArrayList<>();
            Cursor cursor = db.query("DefaultMenu", null, "diet_type = ?", new String[]{fromDietType}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    DefaultMenuItem item = new DefaultMenuItem();
                    item.setItemName(cursor.getString(cursor.getColumnIndex("item_name")));
                    item.setMealType(cursor.getString(cursor.getColumnIndex("meal_type")));
                    item.setDayOfWeek(cursor.getString(cursor.getColumnIndex("day_of_week")));
                    item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                    sourceItems.add(item);
                } while (cursor.moveToNext());
            }

            if (cursor != null) {
                cursor.close();
            }

            // Insert items with new diet type
            for (DefaultMenuItem item : sourceItems) {
                ContentValues values = new ContentValues();
                values.put("item_name", item.getItemName());
                values.put("diet_type", toDietType);
                values.put("meal_type", item.getMealType());
                values.put("day_of_week", item.getDayOfWeek());
                values.put("description", item.getDescription());
                values.put("created_date", getCurrentTimestamp());

                db.insert("DefaultMenu", null, values);
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Copied " + sourceItems.size() + " items from " + fromDietType + " to " + toDietType);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error copying default menu", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }
}