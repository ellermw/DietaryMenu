package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO class for managing item categories
 */
public class CategoryDAO {

    private DatabaseHelper dbHelper;

    public CategoryDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Inner class to hold category information
     */
    public static class CategoryInfo {
        private int id;
        private String name;
        private int itemCount;

        public CategoryInfo(int id, String name, int itemCount) {
            this.id = id;
            this.name = name;
            this.itemCount = itemCount;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getItemCount() {
            return itemCount;
        }

        @Override
        public String toString() {
            return name + " (" + itemCount + " items)";
        }
    }

    /**
     * Get all categories with item counts
     */
    public List<CategoryInfo> getAllCategoriesWithCounts() {
        List<CategoryInfo> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT category, COUNT(*) as item_count " +
                "FROM items " +
                "GROUP BY category " +
                "ORDER BY category";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = 0;
            do {
                String categoryName = cursor.getString(cursor.getColumnIndex("category"));
                int itemCount = cursor.getInt(cursor.getColumnIndex("item_count"));
                categories.add(new CategoryInfo(id++, categoryName, itemCount));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return categories;
    }

    /**
     * Get all unique category names
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
     * Add a new category (by adding an item with that category)
     */
    public long addCategory(String categoryName) {
        // Categories are implicit from items, so we create a placeholder item
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", "New Item");
        values.put("category", categoryName);
        values.put("description", "");
        values.put("is_ada_friendly", 0);

        return db.insert("items", null, values);
    }

    /**
     * Update category name for all items in that category
     */
    public int updateCategory(String oldName, String newName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category", newName);

        return db.update("items", values, "category = ?", new String[]{oldName});
    }

    /**
     * Delete a category (delete all items in that category)
     */
    public int deleteCategory(String categoryName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("items", "category = ?", new String[]{categoryName});
    }

    /**
     * Check if category exists
     */
    public boolean categoryExists(String categoryName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("items", new String[]{"item_id"},
                "category = ?", new String[]{categoryName},
                null, null, null, "1");

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }

        return exists;
    }

    /**
     * Get item count for a specific category
     */
    public int getCategoryItemCount(String categoryName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("items", new String[]{"COUNT(*)"},
                "category = ?", new String[]{categoryName},
                null, null, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }
}