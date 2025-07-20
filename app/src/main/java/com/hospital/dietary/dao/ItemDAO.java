package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    private static final String TAG = "ItemDAO";
    private DatabaseHelper dbHelper;

    public ItemDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * FIXED: Get all items with proper column references and error handling
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // FIXED: Use simple query without Category table join since Item table has category as TEXT
        String query = "SELECT item_id, name, category, " +
                "COALESCE(size_ml, 0) as size_ml, " +
                "COALESCE(description, '') as description, " +
                "COALESCE(is_ada_friendly, 0) as is_ada_friendly, " +
                "COALESCE(ada_friendly, 0) as ada_friendly, " +
                "COALESCE(is_soda, 0) as is_soda, " +
                "COALESCE(is_clear_liquid, 0) as is_clear_liquid, " +
                "COALESCE(meal_type, '') as meal_type, " +
                "COALESCE(is_default, 0) as is_default " +
                "FROM Item ORDER BY category, name";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            Log.d(TAG, "Found " + cursor.getCount() + " items");

            if (cursor.moveToFirst()) {
                do {
                    Item item = createItemFromCursor(cursor);
                    if (item != null) {
                        items.add(item);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getAllItems: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "Returning " + items.size() + " items");
        return items;
    }

    /**
     * Get items by category
     */
    public List<Item> getItemsByCategory(String category) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM Item WHERE category = ? ORDER BY name";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{category});

            if (cursor.moveToFirst()) {
                do {
                    Item item = createItemFromCursor(cursor);
                    if (item != null) {
                        items.add(item);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getItemsByCategory: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return items;
    }

    /**
     * Get ADA friendly items by category
     */
    public List<Item> getAdaItemsByCategory(String category) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Check both ada_friendly and is_ada_friendly columns for compatibility
        String query = "SELECT * FROM Item WHERE category = ? AND " +
                "(ada_friendly = 1 OR is_ada_friendly = 1) ORDER BY name";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{category});

            if (cursor.moveToFirst()) {
                do {
                    Item item = createItemFromCursor(cursor);
                    if (item != null) {
                        items.add(item);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getAdaItemsByCategory: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return items;
    }

    /**
     * Get all categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT category FROM Item ORDER BY category";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    categories.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getAllCategories: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return categories;
    }

    /**
     * Add a new item
     */
    public long addItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", item.getName());
        values.put("category", item.getCategory());
        values.put("ada_friendly", item.isAdaFriendly() ? 1 : 0);
        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0); // Both columns for compatibility

        // Add optional fields if they exist
        if (item.getSizeML() != null) {
            values.put("size_ml", item.getSizeML());
        }
        if (item.getDescription() != null) {
            values.put("description", item.getDescription());
        }
        if (item.getMealType() != null) {
            values.put("meal_type", item.getMealType());
        }

        try {
            return db.insert("Item", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error adding item: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Update an existing item
     */
    public boolean updateItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", item.getName());
        values.put("category", item.getCategory());
        values.put("ada_friendly", item.isAdaFriendly() ? 1 : 0);
        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0); // Both columns for compatibility

        // Add optional fields if they exist
        if (item.getSizeML() != null) {
            values.put("size_ml", item.getSizeML());
        }
        if (item.getDescription() != null) {
            values.put("description", item.getDescription());
        }
        if (item.getMealType() != null) {
            values.put("meal_type", item.getMealType());
        }

        try {
            int rowsAffected = db.update("Item", values, "item_id = ?",
                    new String[]{String.valueOf(item.getItemId())});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete an item
     */
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            int rowsAffected = db.delete("Item", "item_id = ?",
                    new String[]{String.valueOf(itemId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search items by name or category
     */
    public List<Item> searchItems(String searchTerm) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM Item WHERE " +
                "LOWER(name) LIKE ? OR LOWER(category) LIKE ? " +
                "ORDER BY category, name";

        String searchPattern = "%" + searchTerm.toLowerCase() + "%";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern});

            if (cursor.moveToFirst()) {
                do {
                    Item item = createItemFromCursor(cursor);
                    if (item != null) {
                        items.add(item);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching items: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return items;
    }

    /**
     * Get items for specific meal categories (for meal planning)
     */
    public List<Item> getItemsForMealPlanning(String mealType, boolean adaOnly) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define categories for different meal planning
        String[] categories;
        if ("Breakfast".equals(mealType)) {
            categories = new String[]{"Breakfast Items", "Beverages", "Juices", "Fruits", "Dairy"};
        } else {
            // Lunch and Dinner
            categories = new String[]{"Proteins", "Starches", "Vegetables", "Desserts", "Beverages"};
        }

        for (String category : categories) {
            String query = "SELECT * FROM Item WHERE category = ?";
            if (adaOnly) {
                query += " AND (ada_friendly = 1 OR is_ada_friendly = 1)";
            }
            query += " ORDER BY name";

            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, new String[]{category});

                if (cursor.moveToFirst()) {
                    do {
                        Item item = createItemFromCursor(cursor);
                        if (item != null) {
                            items.add(item);
                        }
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting items for meal planning: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return items;
    }

    /**
     * Get items by multiple categories
     */
    public List<Item> getItemsByCategories(String[] categories, boolean adaOnly) {
        List<Item> items = new ArrayList<>();

        for (String category : categories) {
            List<Item> categoryItems = adaOnly ?
                    getAdaItemsByCategory(category) :
                    getItemsByCategory(category);
            items.addAll(categoryItems);
        }

        return items;
    }

    /**
     * Get item by ID
     */
    public Item getItemById(int itemId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM Item WHERE item_id = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(itemId)});

            if (cursor.moveToFirst()) {
                return createItemFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting item by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * Get total item count
     */
    public int getItemCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM Item";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting item count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * FIXED: Helper method to create Item from cursor with error handling
     */
    private Item createItemFromCursor(Cursor cursor) {
        try {
            Item item = new Item();

            // Required fields
            item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
            item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));

            // Handle ada_friendly column (check both possible column names)
            boolean isAdaFriendly = false;
            int adaIdx = cursor.getColumnIndex("ada_friendly");
            if (adaIdx >= 0) {
                isAdaFriendly = cursor.getInt(adaIdx) == 1;
            } else {
                int isAdaIdx = cursor.getColumnIndex("is_ada_friendly");
                if (isAdaIdx >= 0) {
                    isAdaFriendly = cursor.getInt(isAdaIdx) == 1;
                }
            }
            item.setAdaFriendly(isAdaFriendly);

            // Optional fields with safe handling
            int sizeIdx = cursor.getColumnIndex("size_ml");
            if (sizeIdx >= 0) {
                item.setSizeML(cursor.getInt(sizeIdx));
            }

            int descIdx = cursor.getColumnIndex("description");
            if (descIdx >= 0) {
                item.setDescription(cursor.getString(descIdx));
            }

            int sodaIdx = cursor.getColumnIndex("is_soda");
            if (sodaIdx >= 0) {
                item.setSoda(cursor.getInt(sodaIdx) == 1);
            }

            int clearIdx = cursor.getColumnIndex("is_clear_liquid");
            if (clearIdx >= 0) {
                item.setClearLiquid(cursor.getInt(clearIdx) == 1);
            }

            int mealTypeIdx = cursor.getColumnIndex("meal_type");
            if (mealTypeIdx >= 0) {
                item.setMealType(cursor.getString(mealTypeIdx));
            }

            int defaultIdx = cursor.getColumnIndex("is_default");
            if (defaultIdx >= 0) {
                item.setDefault(cursor.getInt(defaultIdx) == 1);
            }

            return item;

        } catch (Exception e) {
            Log.e(TAG, "Error creating item from cursor: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if item exists by name and category
     */
    public boolean itemExists(String name, String category) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM Item WHERE LOWER(name) = ? AND LOWER(category) = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{name.toLowerCase(), category.toLowerCase()});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if item exists: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    /**
     * Get items suitable for specific diets
     */
    public List<Item> getItemsForDiet(String dietType, String category) {
        List<Item> items = new ArrayList<>();

        if ("ADA".equalsIgnoreCase(dietType) || (dietType != null && dietType.contains("ADA"))) {
            items = getAdaItemsByCategory(category);
        } else {
            items = getItemsByCategory(category);
        }

        return items;
    }

    /**
     * Bulk insert items (for initial setup)
     */
    public boolean bulkInsertItems(List<Item> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            for (Item item : items) {
                addItem(item);
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in bulk insert: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }
}