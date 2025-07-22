package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hospital.dietary.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private static final String TAG = "CategoryDAO";
    private DatabaseHelper dbHelper;

    public CategoryDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Get all categories from the database
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT category FROM Item WHERE category IS NOT NULL AND category != '' ORDER BY category";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(0);
                    if (category != null && !category.trim().isEmpty()) {
                        categories.add(category);
                    }
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
     * Add a new category by creating an item with that category
     * This ensures the category exists in the system
     */
    public boolean addCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }

        // Check if category already exists
        if (categoryExists(categoryName)) {
            return false; // Category already exists
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Create a placeholder item to establish the category
            values.put("name", "Category Placeholder - " + categoryName);
            values.put("category", categoryName.trim());
            values.put("ada_friendly", 0);
            values.put("is_ada_friendly", 0);
            values.put("description", "System generated placeholder for category: " + categoryName);

            long result = db.insert("Item", null, values);
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error adding category: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a category exists
     */
    public boolean categoryExists(String categoryName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM Item WHERE category = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{categoryName});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking category existence: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    /**
     * Delete a category (only if no items are using it)
     */
    public boolean deleteCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // First check if any non-placeholder items exist in this category
            String checkQuery = "SELECT COUNT(*) FROM Item WHERE category = ? AND name NOT LIKE 'Category Placeholder - %'";
            Cursor cursor = db.rawQuery(checkQuery, new String[]{categoryName});

            int itemCount = 0;
            if (cursor.moveToFirst()) {
                itemCount = cursor.getInt(0);
            }
            cursor.close();

            if (itemCount > 0) {
                return false; // Cannot delete category with items
            }

            // Delete all placeholder items for this category
            int rowsDeleted = db.delete("Item",
                    "category = ? AND name LIKE 'Category Placeholder - %'",
                    new String[]{categoryName});

            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting category: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get count of items in a category
     */
    public int getItemCountInCategory(String categoryName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM Item WHERE category = ? AND name NOT LIKE 'Category Placeholder - %'";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{categoryName});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting item count in category: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Rename a category
     */
    public boolean renameCategory(String oldName, String newName) {
        if (oldName == null || newName == null ||
                oldName.trim().isEmpty() || newName.trim().isEmpty()) {
            return false;
        }

        if (oldName.equals(newName)) {
            return true; // No change needed
        }

        // Check if new name already exists
        if (categoryExists(newName)) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("category", newName.trim());

            int rowsUpdated = db.update("Item", values, "category = ?", new String[]{oldName});
            return rowsUpdated > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error renaming category: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get categories with item counts
     */
    public List<CategoryInfo> getCategoriesWithCounts() {
        List<CategoryInfo> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT category, COUNT(*) as item_count FROM Item " +
                "WHERE category IS NOT NULL AND category != '' AND name NOT LIKE 'Category Placeholder - %' " +
                "GROUP BY category ORDER BY category";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String categoryName = cursor.getString(0);
                    int itemCount = cursor.getInt(1);
                    categories.add(new CategoryInfo(categoryName, itemCount));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getCategoriesWithCounts: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return categories;
    }

    /**
     * Inner class to hold category information
     */
    public static class CategoryInfo {
        private String name;
        private int itemCount;

        public CategoryInfo(String name, int itemCount) {
            this.name = name;
            this.itemCount = itemCount;
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
}