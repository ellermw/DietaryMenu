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

    private DatabaseHelper dbHelper;

    public ItemDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * FIXED: Get all items with simple query (no Category table dependency)
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // FIXED: Simple query without Category table join
        String query = "SELECT * FROM Item ORDER BY category, name";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));

                    // Handle ada_friendly column
                    int adaIdx = cursor.getColumnIndex("ada_friendly");
                    if (adaIdx >= 0) {
                        item.setAdaFriendly(cursor.getInt(adaIdx) == 1);
                    }

                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getAllItems: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

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
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getItemsByCategory: " + e.getMessage());
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

        String query = "SELECT * FROM Item WHERE category = ? AND ada_friendly = 1 ORDER BY name";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{category});

            if (cursor.moveToFirst()) {
                do {
                    Item item = createItemFromCursor(cursor);
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getAdaItemsByCategory: " + e.getMessage());
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
            Log.e("ItemDAO", "Error in getAllCategories: " + e.getMessage());
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

        try {
            return db.insert("Item", null, values);
        } catch (Exception e) {
            Log.e("ItemDAO", "Error adding item: " + e.getMessage());
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

        try {
            int rowsAffected = db.update("Item", values, "item_id = ?",
                    new String[]{String.valueOf(item.getItemId())});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("ItemDAO", "Error updating item: " + e.getMessage());
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
            Log.e("ItemDAO", "Error deleting item: " + e.getMessage());
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
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error searching items: " + e.getMessage());
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
                query += " AND ada_friendly = 1";
            }
            query += " ORDER BY name";

            Cursor cursor = null;
            try {
                cursor = db.rawQuery(query, new String[]{category});

                if (cursor.moveToFirst()) {
                    do {
                        Item item = createItemFromCursor(cursor);
                        items.add(item);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.e("ItemDAO", "Error getting items for meal planning: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return items;
    }

    /**
     * Helper method to create Item from cursor
     */
    private Item createItemFromCursor(Cursor cursor) {
        Item item = new Item();

        item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));

        // Handle ada_friendly column
        int adaIdx = cursor.getColumnIndex("ada_friendly");
        if (adaIdx >= 0) {
            item.setAdaFriendly(cursor.getInt(adaIdx) == 1);
        }

        return item;
    }
}