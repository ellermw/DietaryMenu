// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/dao/ItemDAO.java
// ================================================================================================

package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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
     * Get item by name
     */
    public Item getItemByName(String itemName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                "i.is_ada_friendly, i.is_soda, c.name as category_name " +
                "FROM Item i " +
                "INNER JOIN Category c ON i.category_id = c.category_id " +
                "WHERE i.name = ?";

        Cursor cursor = db.rawQuery(query, new String[]{itemName});
        Item item = null;

        if (cursor.moveToFirst()) {
            item = cursorToItem(cursor);
        }

        cursor.close();
        return item;
    }

    /**
     * Get items by category
     */
    public List<Item> getItemsByCategory(String categoryName) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                      "i.is_ada_friendly, i.is_soda, c.name as category_name " +
                      "FROM Item i " +
                      "INNER JOIN Category c ON i.category_id = c.category_id " +
                      "WHERE c.name = ? " +
                      "ORDER BY i.name";
        
        Cursor cursor = db.rawQuery(query, new String[]{categoryName});
        
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return items;
    }

    /**
     * Get all items
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                      "i.is_ada_friendly, i.is_soda, c.name as category_name " +
                      "FROM Item i " +
                      "INNER JOIN Category c ON i.category_id = c.category_id " +
                      "ORDER BY c.name, i.name";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return items;
    }

    /**
     * Insert new item
     */
    public long insertItem(Item item) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("category_id", item.getCategoryId());
            values.put("name", item.getName());

            if (item.getSizeML() != null) {
                values.put("size_ml", item.getSizeML());
            } else {
                values.putNull("size_ml");
            }

            values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);
            values.put("is_soda", item.isSoda() ? 1 : 0);

            return db.insert("Item", null, values);
            
        } catch (SQLiteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Update existing item
     */
    public long updateItem(Item item) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("category_id", item.getCategoryId());
            values.put("name", item.getName());

            if (item.getSizeML() != null) {
                values.put("size_ml", item.getSizeML());
            } else {
                values.putNull("size_ml");
            }

            values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);
            values.put("is_soda", item.isSoda() ? 1 : 0);

            return db.update("Item", values, "item_id = ?", 
                    new String[]{String.valueOf(item.getItemId())});
                    
        } catch (SQLiteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Delete item
     */
    public boolean deleteItem(int itemId) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            // Check if item is used in any meal orders
            String checkQuery = "SELECT COUNT(*) FROM MealLine WHERE item_id = ?";
            Cursor cursor = db.rawQuery(checkQuery, new String[]{String.valueOf(itemId)});
            
            boolean isUsed = false;
            if (cursor.moveToFirst()) {
                isUsed = cursor.getInt(0) > 0;
            }
            cursor.close();
            
            if (isUsed) {
                // Item is used in meal orders, don't delete
                return false;
            }
            
            // Delete item tags first (foreign key constraint)
            db.delete("ItemTag", "item_id = ?", new String[]{String.valueOf(itemId)});
            
            // Delete the item
            int rowsDeleted = db.delete("Item", "item_id = ?", new String[]{String.valueOf(itemId)});
            
            return rowsDeleted > 0;
            
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get items by category ID
     */
    public List<Item> getItemsByCategoryId(int categoryId) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                      "i.is_ada_friendly, i.is_soda, c.name as category_name " +
                      "FROM Item i " +
                      "INNER JOIN Category c ON i.category_id = c.category_id " +
                      "WHERE i.category_id = ? " +
                      "ORDER BY i.name";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});
        
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return items;
    }

    /**
     * Get ADA friendly items by category
     */
    public List<Item> getAdaFriendlyItemsByCategory(String categoryName) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                      "i.is_ada_friendly, i.is_soda, c.name as category_name " +
                      "FROM Item i " +
                      "INNER JOIN Category c ON i.category_id = c.category_id " +
                      "WHERE c.name = ? AND i.is_ada_friendly = 1 " +
                      "ORDER BY i.name";
        
        Cursor cursor = db.rawQuery(query, new String[]{categoryName});
        
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return items;
    }

    /**
     * Get items by name pattern (for search)
     */
    public List<Item> searchItemsByName(String namePattern) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                      "i.is_ada_friendly, i.is_soda, c.name as category_name " +
                      "FROM Item i " +
                      "INNER JOIN Category c ON i.category_id = c.category_id " +
                      "WHERE i.name LIKE ? " +
                      "ORDER BY i.name";
        
        Cursor cursor = db.rawQuery(query, new String[]{"%" + namePattern + "%"});
        
        if (cursor.moveToFirst()) {
            do {
                items.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return items;
    }

    /**
     * Check if item name exists (for validation)
     */
    public boolean itemNameExists(String itemName, int excludeItemId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM Item WHERE name = ? AND item_id != ?";
        Cursor cursor = db.rawQuery(query, new String[]{itemName, String.valueOf(excludeItemId)});
        
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        
        cursor.close();
        return exists;
    }

    /**
     * Get item count by category
     */
    public int getItemCountByCategory(String categoryName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM Item i " +
                      "INNER JOIN Category c ON i.category_id = c.category_id " +
                      "WHERE c.name = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{categoryName});
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        return count;
    }

    /**
     * Convert cursor to Item object
     */
    private Item cursorToItem(Cursor cursor) {
        Item item = new Item();
        
        item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
        item.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));

        int sizeIndex = cursor.getColumnIndex("size_ml");
        if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
            item.setSizeML(cursor.getInt(sizeIndex));
        }

        item.setAdaFriendly(cursor.getInt(cursor.getColumnIndexOrThrow("is_ada_friendly")) == 1);
        item.setSoda(cursor.getInt(cursor.getColumnIndexOrThrow("is_soda")) == 1);
        
        // Set category name last to trigger any necessary calculations
        item.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
        
        return item;
    }
}