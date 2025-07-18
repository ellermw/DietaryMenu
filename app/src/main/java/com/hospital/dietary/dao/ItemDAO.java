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
     * FIXED: Get all items with proper Category table JOIN
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, " +
                       "i.meal_type, i.is_default, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "ORDER BY c.display_order, i.name";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
                    item.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    
                    // Handle size_ml (can be null)
                    int sizeIdx = cursor.getColumnIndex("size_ml");
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        item.setSizeML(cursor.getInt(sizeIdx));
                    }
                    
                    // Handle description (can be null)
                    int descIdx = cursor.getColumnIndex("description");
                    if (descIdx >= 0 && !cursor.isNull(descIdx)) {
                        item.setDescription(cursor.getString(descIdx));
                    }
                    
                    // Boolean flags
                    item.setAdaFriendly(cursor.getInt(cursor.getColumnIndexOrThrow("is_ada_friendly")) == 1);
                    
                    // Handle is_soda (may not exist in all schemas)
                    int sodaIdx = cursor.getColumnIndex("is_soda");
                    if (sodaIdx >= 0) {
                        item.setSoda(cursor.getInt(sodaIdx) == 1);
                    }
                    
                    // Handle is_clear_liquid (may not exist in all schemas)
                    int clearLiquidIdx = cursor.getColumnIndex("is_clear_liquid");
                    if (clearLiquidIdx >= 0) {
                        item.setClearLiquid(cursor.getInt(clearLiquidIdx) == 1);
                    }
                    
                    // Set category name
                    item.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
                    
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getAllItems: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return items;
    }

    /**
     * Get items by category name
     */
    public List<Item> getItemsByCategory(String categoryName) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, " +
                       "i.meal_type, i.is_default, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "WHERE c.name = ? " +
                       "ORDER BY i.name";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{categoryName});
            
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
                    item.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    
                    // Handle size_ml (can be null)
                    int sizeIdx = cursor.getColumnIndex("size_ml");
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        item.setSizeML(cursor.getInt(sizeIdx));
                    }
                    
                    // Handle description (can be null)
                    int descIdx = cursor.getColumnIndex("description");
                    if (descIdx >= 0 && !cursor.isNull(descIdx)) {
                        item.setDescription(cursor.getString(descIdx));
                    }
                    
                    // Boolean flags
                    item.setAdaFriendly(cursor.getInt(cursor.getColumnIndexOrThrow("is_ada_friendly")) == 1);
                    
                    // Handle optional columns
                    int sodaIdx = cursor.getColumnIndex("is_soda");
                    if (sodaIdx >= 0) {
                        item.setSoda(cursor.getInt(sodaIdx) == 1);
                    }
                    
                    int clearLiquidIdx = cursor.getColumnIndex("is_clear_liquid");
                    if (clearLiquidIdx >= 0) {
                        item.setClearLiquid(cursor.getInt(clearLiquidIdx) == 1);
                    }
                    
                    item.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
                    
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
     * Get item by name
     */
    public Item getItemByName(String itemName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, " +
                       "i.meal_type, i.is_default, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "WHERE i.name = ?";
        
        Cursor cursor = null;
        Item item = null;
        
        try {
            cursor = db.rawQuery(query, new String[]{itemName});
            
            if (cursor.moveToFirst()) {
                item = new Item();
                item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
                item.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                
                // Handle size_ml (can be null)
                int sizeIdx = cursor.getColumnIndex("size_ml");
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                    item.setSizeML(cursor.getInt(sizeIdx));
                }
                
                // Handle description (can be null)
                int descIdx = cursor.getColumnIndex("description");
                if (descIdx >= 0 && !cursor.isNull(descIdx)) {
                    item.setDescription(cursor.getString(descIdx));
                }
                
                // Boolean flags
                item.setAdaFriendly(cursor.getInt(cursor.getColumnIndexOrThrow("is_ada_friendly")) == 1);
                
                // Handle optional columns
                int sodaIdx = cursor.getColumnIndex("is_soda");
                if (sodaIdx >= 0) {
                    item.setSoda(cursor.getInt(sodaIdx) == 1);
                }
                
                int clearLiquidIdx = cursor.getColumnIndex("is_clear_liquid");
                if (clearLiquidIdx >= 0) {
                    item.setClearLiquid(cursor.getInt(clearLiquidIdx) == 1);
                }
                
                item.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getItemByName: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return item;
    }

    /**
     * Add new item
     */
    public long addItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("category_id", item.getCategoryId());
        values.put("name", item.getName());
        values.put("meal_type", item.getMealType() != null ? item.getMealType() : "General");

        if (item.getSizeML() != null) {
            values.put("size_ml", item.getSizeML());
        }

        if (item.getDescription() != null) {
            values.put("description", item.getDescription());
        }

        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);
        values.put("is_soda", item.isSoda() ? 1 : 0);
        values.put("is_clear_liquid", item.isClearLiquid() ? 1 : 0);
        values.put("is_default", item.isDefault() ? 1 : 0);

        return db.insert("Item", null, values);
    }

    /**
     * Update existing item
     */
    public long updateItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("category_id", item.getCategoryId());
        values.put("name", item.getName());
        values.put("meal_type", item.getMealType() != null ? item.getMealType() : "General");

        if (item.getSizeML() != null) {
            values.put("size_ml", item.getSizeML());
        } else {
            values.putNull("size_ml");
        }

        if (item.getDescription() != null) {
            values.put("description", item.getDescription());
        } else {
            values.putNull("description");
        }

        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);
        values.put("is_soda", item.isSoda() ? 1 : 0);
        values.put("is_clear_liquid", item.isClearLiquid() ? 1 : 0);
        values.put("is_default", item.isDefault() ? 1 : 0);

        return db.update("Item", values, "item_id = ?", new String[]{String.valueOf(item.getItemId())});
    }

    /**
     * Delete item
     */
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete("Item", "item_id = ?", new String[]{String.valueOf(itemId)});
        return rowsAffected > 0;
    }

    /**
     * Get items by meal type
     */
    public List<Item> getItemsByMealType(String mealType) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, " +
                       "i.meal_type, i.is_default, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "WHERE i.meal_type = ? " +
                       "ORDER BY c.display_order, i.name";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{mealType});
            
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
                    item.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    
                    // Handle size_ml (can be null)
                    int sizeIdx = cursor.getColumnIndex("size_ml");
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        item.setSizeML(cursor.getInt(sizeIdx));
                    }
                    
                    // Handle description (can be null)
                    int descIdx = cursor.getColumnIndex("description");
                    if (descIdx >= 0 && !cursor.isNull(descIdx)) {
                        item.setDescription(cursor.getString(descIdx));
                    }
                    
                    // Boolean flags
                    item.setAdaFriendly(cursor.getInt(cursor.getColumnIndexOrThrow("is_ada_friendly")) == 1);
                    
                    // Handle optional columns
                    int sodaIdx = cursor.getColumnIndex("is_soda");
                    if (sodaIdx >= 0) {
                        item.setSoda(cursor.getInt(sodaIdx) == 1);
                    }
                    
                    int clearLiquidIdx = cursor.getColumnIndex("is_clear_liquid");
                    if (clearLiquidIdx >= 0) {
                        item.setClearLiquid(cursor.getInt(clearLiquidIdx) == 1);
                    }
                    
                    item.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
                    
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getItemsByMealType: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return items;
    }

    /**
     * Get ADA-friendly items
     */
    public List<Item> getAdaFriendlyItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, " +
                       "i.meal_type, i.is_default, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "WHERE i.is_ada_friendly = 1 " +
                       "ORDER BY c.display_order, i.name";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
                    item.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    
                    // Handle size_ml (can be null)
                    int sizeIdx = cursor.getColumnIndex("size_ml");
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        item.setSizeML(cursor.getInt(sizeIdx));
                    }
                    
                    // Handle description (can be null)
                    int descIdx = cursor.getColumnIndex("description");
                    if (descIdx >= 0 && !cursor.isNull(descIdx)) {
                        item.setDescription(cursor.getString(descIdx));
                    }
                    
                    // Boolean flags
                    item.setAdaFriendly(cursor.getInt(cursor.getColumnIndexOrThrow("is_ada_friendly")) == 1);
                    
                    // Handle optional columns
                    int sodaIdx = cursor.getColumnIndex("is_soda");
                    if (sodaIdx >= 0) {
                        item.setSoda(cursor.getInt(sodaIdx) == 1);
                    }
                    
                    int clearLiquidIdx = cursor.getColumnIndex("is_clear_liquid");
                    if (clearLiquidIdx >= 0) {
                        item.setClearLiquid(cursor.getInt(clearLiquidIdx) == 1);
                    }
                    
                    item.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
                    
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getAdaFriendlyItems: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return items;
    }

    /**
     * Get default items for a specific meal type
     */
    public List<Item> getDefaultItemsByMealType(String mealType) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, " +
                       "i.meal_type, i.is_default, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "WHERE i.meal_type = ? AND i.is_default = 1 " +
                       "ORDER BY c.display_order, i.name";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{mealType});
            
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.setItemId(cursor.getInt(cursor.getColumnIndexOrThrow("item_id")));
                    item.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
                    item.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    
                    // Handle size_ml (can be null)
                    int sizeIdx = cursor.getColumnIndex("size_ml");
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        item.setSizeML(cursor.getInt(sizeIdx));
                    }
                    
                    // Handle description (can be null)
                    int descIdx = cursor.getColumnIndex("description");
                    if (descIdx >= 0 && !cursor.isNull(descIdx)) {
                        item.setDescription(cursor.getString(descIdx));
                    }
                    
                    // Boolean flags
                    item.setAdaFriendly(cursor.getInt(cursor.getColumnIndexOrThrow("is_ada_friendly")) == 1);
                    
                    // Handle optional columns
                    int sodaIdx = cursor.getColumnIndex("is_soda");
                    if (sodaIdx >= 0) {
                        item.setSoda(cursor.getInt(sodaIdx) == 1);
                    }
                    
                    int clearLiquidIdx = cursor.getColumnIndex("is_clear_liquid");
                    if (clearLiquidIdx >= 0) {
                        item.setClearLiquid(cursor.getInt(clearLiquidIdx) == 1);
                    }
                    
                    item.setCategoryName(cursor.getString(cursor.getColumnIndexOrThrow("category_name")));
                    
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getDefaultItemsByMealType: " + e.getMessage());
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
        
        String query = "SELECT name FROM Category ORDER BY display_order";
        
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
}