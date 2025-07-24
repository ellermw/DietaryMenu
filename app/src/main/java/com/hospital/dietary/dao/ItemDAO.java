package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Item;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO class for managing dietary menu items
 */
public class ItemDAO {

    private DatabaseHelper dbHelper;

    public ItemDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Insert a new item
     */
    public long insertItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", item.getItemName());
        values.put("category", item.getCategory());
        values.put("description", item.getDescription());
        values.put("is_ada_friendly", item.getIsAdaFriendly());

        return db.insert("items", null, values);
    }

    /**
     * Update an existing item
     */
    public int updateItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", item.getItemName());
        values.put("category", item.getCategory());
        values.put("description", item.getDescription());
        values.put("is_ada_friendly", item.getIsAdaFriendly());

        return db.update("items", values, "item_id = ?",
                new String[]{String.valueOf(item.getItemId())});
    }

    /**
     * Delete an item - returns boolean for success
     */
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete("items", "item_id = ?",
                new String[]{String.valueOf(itemId)});
        return rowsDeleted > 0;
    }

    /**
     * Get item by ID
     */
    public Item getItemById(int itemId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("items", null, "item_id = ?",
                new String[]{String.valueOf(itemId)}, null, null, null);

        Item item = null;
        if (cursor != null && cursor.moveToFirst()) {
            item = cursorToItem(cursor);
            cursor.close();
        }

        return item;
    }

    /**
     * Get all items
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM items ORDER BY category, name";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return items;
    }

    /**
     * Get items by category
     */
    public List<Item> getItemsByCategory(String category) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM items WHERE category = ? ORDER BY name";
        Cursor cursor = db.rawQuery(query, new String[]{category});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return items;
    }

    /**
     * Get ADA-friendly items
     */
    public List<Item> getAdaFriendlyItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM items WHERE is_ada_friendly = 1 ORDER BY category, name";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return items;
    }

    /**
     * Get ADA-friendly items by category
     */
    public List<Item> getAdaItemsByCategory(String category) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM items WHERE category = ? AND is_ada_friendly = 1 ORDER BY name";
        Cursor cursor = db.rawQuery(query, new String[]{category});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return items;
    }

    /**
     * Search items by name or category
     */
    public List<Item> searchItems(String searchTerm) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM items WHERE " +
                "LOWER(name) LIKE LOWER(?) OR " +
                "LOWER(category) LIKE LOWER(?) " +
                "ORDER BY category, name";

        String searchPattern = "%" + searchTerm + "%";
        Cursor cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return items;
    }

    /**
     * Get all unique categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT category FROM items ORDER BY category";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return categories;
    }

    /**
     * Check if item exists by name and category
     */
    public boolean itemExists(String name, String category) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("items", new String[]{"item_id"},
                "LOWER(name) = LOWER(?) AND LOWER(category) = LOWER(?)",
                new String[]{name, category}, null, null, null);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }

        return exists;
    }

    /**
     * Get item count
     */
    public int getItemCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM items", null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    /**
     * Get item count by category
     */
    public int getItemCountByCategory(String category) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("items", new String[]{"COUNT(*)"},
                "category = ?", new String[]{category}, null, null, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    /**
     * Convert cursor to Item object
     */
    private Item cursorToItem(Cursor cursor) {
        Item item = new Item();

        item.setItemId(cursor.getInt(cursor.getColumnIndex("item_id")));
        item.setItemName(cursor.getString(cursor.getColumnIndex("name")));
        item.setCategory(cursor.getString(cursor.getColumnIndex("category")));
        item.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        item.setIsAdaFriendly(cursor.getInt(cursor.getColumnIndex("is_ada_friendly")));

        return item;
    }
}