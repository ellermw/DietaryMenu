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
     * Get all items with proper column references and error handling
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // FIXED: Use correct table name from DatabaseHelper
        String query = "SELECT item_id, name, category, " +
                "COALESCE(description, '') as description, " +
                "COALESCE(is_ada_friendly, 0) as is_ada_friendly, " +
                "COALESCE(is_cardiac_friendly, 0) as is_cardiac_friendly, " +
                "COALESCE(is_renal_friendly, 0) as is_renal_friendly, " +
                "created_date " +
                "FROM " + DatabaseHelper.TABLE_ITEMS + " ORDER BY category, name";

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

        // FIXED: Use correct table name
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ITEMS + " WHERE category = ? ORDER BY name";

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
            Log.e(TAG, "Error getting items by category: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return items;
    }

    /**
     * Get ADA-friendly items by category
     */
    public List<Item> getAdaItemsByCategory(String category) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // FIXED: Use correct table name
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ITEMS +
                " WHERE category = ? AND is_ada_friendly = 1 ORDER BY name";

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
            Log.e(TAG, "Error getting ADA items by category: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return items;
    }

    /**
     * Add a new item
     */
    public long addItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", item.getName());
        values.put("category", item.getCategory());
        values.put("description", item.getDescription());
        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);

        try {
            // FIXED: Use correct table name constant
            return db.insert(DatabaseHelper.TABLE_ITEMS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error adding item: " + e.getMessage());
            e.printStackTrace();
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
        values.put("description", item.getDescription());
        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);

        try {
            // FIXED: Use correct table name constant
            int rowsAffected = db.update(DatabaseHelper.TABLE_ITEMS, values, "item_id = ?",
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
            // FIXED: Use correct table name constant
            int rowsAffected = db.delete(DatabaseHelper.TABLE_ITEMS, "item_id = ?",
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

        // FIXED: Use correct table name constant
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ITEMS + " WHERE " +
                "LOWER(name) LIKE ? OR LOWER(category) LIKE ? ORDER BY name";

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
     * Check if item exists by name and category
     */
    public boolean itemExists(String name, String category) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // FIXED: Use correct table name constant
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ITEMS +
                " WHERE LOWER(name) = ? AND LOWER(category) = ?";

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
     * Get items for meal planning with ADA filter option
     */
    public List<Item> getItemsForMealPlanning(String category, boolean adaOnly) {
        List<Item> items = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // FIXED: Use correct table name constant
            String query = "SELECT * FROM " + DatabaseHelper.TABLE_ITEMS + " WHERE category = ?";
            if (adaOnly) {
                query += " AND is_ada_friendly = 1";
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

        // FIXED: Use correct table name constant
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ITEMS + " WHERE item_id = ?";

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

        // FIXED: Use correct table name constant
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ITEMS;

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
     * Helper method to create Item from cursor with error handling
     */
    private Item createItemFromCursor(Cursor cursor) {
        try {
            Item item = new Item();

            item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
            item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));

            // Handle optional columns safely
            int descIndex = cursor.getColumnIndex("description");
            if (descIndex >= 0) {
                item.setDescription(cursor.getString(descIndex));
            }

            int adaIndex = cursor.getColumnIndex("is_ada_friendly");
            if (adaIndex >= 0) {
                item.setAdaFriendly(cursor.getInt(adaIndex) == 1);
            }

            return item;
        } catch (Exception e) {
            Log.e(TAG, "Error creating item from cursor: " + e.getMessage());
            return null;
        }
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