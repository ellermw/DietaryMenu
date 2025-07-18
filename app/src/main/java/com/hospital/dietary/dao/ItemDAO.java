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

    // FIXED: Updated getAllItems() with fallback for column name compatibility
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Try with new column names first, fall back to old names if needed
        String query = buildGetAllItemsQuery();
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                int idxId       = cursor.getColumnIndexOrThrow("item_id");
                int idxCatId    = cursor.getColumnIndexOrThrow("category_id");
                int idxName     = cursor.getColumnIndexOrThrow("name");
                int idxSize     = cursor.getColumnIndex("size_ml");
                int idxDesc     = cursor.getColumnIndex("description"); // Optional field
                
                // Handle both old and new column names
                int idxAda      = getAdaFriendlyColumnIndex(cursor);
                int idxSoda     = getSodaColumnIndex(cursor);
                int idxClearLiq = getClearLiquidColumnIndex(cursor);
                int idxCatName  = cursor.getColumnIndexOrThrow("category_name");

                do {
                    Item item = new Item();
                    item.setItemId(       cursor.getInt(idxId)         );
                    item.setCategoryId(   cursor.getInt(idxCatId)      );
                    item.setName(         cursor.getString(idxName)    );
                    if (!cursor.isNull(idxSize)) {
                        item.setSizeML(cursor.getInt(idxSize));
                    }
                    // Handle description field if it exists
                    if (idxDesc >= 0 && !cursor.isNull(idxDesc)) {
                        item.setDescription(cursor.getString(idxDesc));
                    }
                    item.setAdaFriendly(  idxAda >= 0 ? cursor.getInt(idxAda)  == 1 : false );
                    item.setSoda(         idxSoda >= 0 ? cursor.getInt(idxSoda) == 1 : false );
                    item.setClearLiquid(  idxClearLiq >= 0 ? cursor.getInt(idxClearLiq) == 1 : false );
                    item.setCategoryName( cursor.getString(idxCatName) );
                    items.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ItemDAO", "Error in getAllItems: " + e.getMessage());
            // If there's still a schema issue, return empty list rather than crash
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return items;
    }
    
    // FIXED: Build query with fallback column names
    private String buildGetAllItemsQuery() {
        // Check if new column names exist, otherwise use old ones
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            // Test query with new column names and description field
            String testQuery = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                              "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                              "FROM Item i " +
                              "INNER JOIN Category c ON i.category_id = c.category_id " +
                              "LIMIT 1";
            Cursor testCursor = db.rawQuery(testQuery, null);
            testCursor.close();
            
            // If test succeeds, use new column names with description
            return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                   "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                   "FROM Item i " +
                   "INNER JOIN Category c ON i.category_id = c.category_id " +
                   "ORDER BY c.name, i.name";
        } catch (Exception e) {
            try {
                // Try without description field
                String testQuery = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                                  "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                                  "FROM Item i " +
                                  "INNER JOIN Category c ON i.category_id = c.category_id " +
                                  "LIMIT 1";
                Cursor testCursor = db.rawQuery(testQuery, null);
                testCursor.close();
                
                // If test succeeds, use new column names without description
                return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "ORDER BY c.name, i.name";
            } catch (Exception e2) {
                // Fall back to old column names
                return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "ORDER BY c.name, i.name";
            }
        }
    }
    
    // Helper methods to handle column name variations
    private int getAdaFriendlyColumnIndex(Cursor cursor) {
        int index = cursor.getColumnIndex("is_ada_friendly");
        if (index >= 0) return index;
        
        index = cursor.getColumnIndex("ada_friendly");
        if (index >= 0) return index;
        
        Log.w("ItemDAO", "ada_friendly column not found, defaulting to 0");
        return -1; // Will be handled as false
    }
    
    private int getSodaColumnIndex(Cursor cursor) {
        int index = cursor.getColumnIndex("is_soda");
        if (index >= 0) return index;
        
        index = cursor.getColumnIndex("soda");
        if (index >= 0) return index;
        
        Log.w("ItemDAO", "is_soda column not found, defaulting to 0");
        return -1; // Will be handled as false
    }
    
    private int getClearLiquidColumnIndex(Cursor cursor) {
        int index = cursor.getColumnIndex("is_clear_liquid");
        if (index >= 0) return index;
        
        index = cursor.getColumnIndex("clear_liquid");
        if (index >= 0) return index;
        
        Log.w("ItemDAO", "is_clear_liquid column not found, defaulting to 0");
        return -1; // Will be handled as false
    }

    public List<Item> getItemsByCategory(String categoryName) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Build query with fallback column names
        String query = buildGetItemsByCategoryQuery();
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{categoryName});
            
            if (cursor.moveToFirst()) {
                int idxId       = cursor.getColumnIndexOrThrow("item_id");
                int idxCatId    = cursor.getColumnIndexOrThrow("category_id");
                int idxName     = cursor.getColumnIndexOrThrow("name");
                int idxSize     = cursor.getColumnIndex("size_ml");
                int idxDesc     = cursor.getColumnIndex("description"); // Optional field
                
                // Handle both old and new column names
                int idxAda      = getAdaFriendlyColumnIndex(cursor);
                int idxSoda     = getSodaColumnIndex(cursor);
                int idxClearLiq = getClearLiquidColumnIndex(cursor);
                int idxCatName  = cursor.getColumnIndexOrThrow("category_name");

                do {
                    Item item = new Item();
                    item.setItemId(       cursor.getInt(idxId)         );
                    item.setCategoryId(   cursor.getInt(idxCatId)      );
                    item.setName(         cursor.getString(idxName)    );
                    if (!cursor.isNull(idxSize)) {
                        item.setSizeML(cursor.getInt(idxSize));
                    }
                    // Handle description field if it exists
                    if (idxDesc >= 0 && !cursor.isNull(idxDesc)) {
                        item.setDescription(cursor.getString(idxDesc));
                    }
                    item.setAdaFriendly(  idxAda >= 0 ? cursor.getInt(idxAda)  == 1 : false );
                    item.setSoda(         idxSoda >= 0 ? cursor.getInt(idxSoda) == 1 : false );
                    item.setClearLiquid(  idxClearLiq >= 0 ? cursor.getInt(idxClearLiq) == 1 : false );
                    item.setCategoryName( cursor.getString(idxCatName) );
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
    
    // FIXED: Build getItemsByCategory query with fallback column names
    private String buildGetItemsByCategoryQuery() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            // Test query with new column names and description field
            String testQuery = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                              "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                              "FROM Item i " +
                              "INNER JOIN Category c ON i.category_id = c.category_id " +
                              "LIMIT 1";
            Cursor testCursor = db.rawQuery(testQuery, null);
            testCursor.close();
            
            // If test succeeds, use new column names with description
            return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                   "i.description, i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                   "FROM Item i " +
                   "INNER JOIN Category c ON i.category_id = c.category_id " +
                   "WHERE c.name = ? " +
                   "ORDER BY i.name";
        } catch (Exception e) {
            try {
                // Try without description field
                String testQuery = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                                  "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                                  "FROM Item i " +
                                  "INNER JOIN Category c ON i.category_id = c.category_id " +
                                  "LIMIT 1";
                Cursor testCursor = db.rawQuery(testQuery, null);
                testCursor.close();
                
                // If test succeeds, use new column names without description
                return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "WHERE c.name = ? " +
                       "ORDER BY i.name";
            } catch (Exception e2) {
                // Fall back to old column names
                return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "WHERE c.name = ? " +
                       "ORDER BY i.name";
            }
        }
    }

    public Item getItemByName(String itemName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                       "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                       "FROM Item i " +
                       "INNER JOIN Category c ON i.category_id = c.category_id " +
                       "WHERE i.name = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{itemName});
        
        Item item = null;
        if (cursor.moveToFirst()) {
            int idxId       = cursor.getColumnIndexOrThrow("item_id");
            int idxCatId    = cursor.getColumnIndexOrThrow("category_id");
            int idxName     = cursor.getColumnIndexOrThrow("name");
            int idxSize     = cursor.getColumnIndex("size_ml");
            int idxAda      = cursor.getColumnIndexOrThrow("is_ada_friendly");
            int idxSoda     = cursor.getColumnIndexOrThrow("is_soda");
            int idxClearLiq = cursor.getColumnIndexOrThrow("is_clear_liquid");
            int idxCatName  = cursor.getColumnIndexOrThrow("category_name");

            item = new Item();
            item.setItemId(       cursor.getInt(idxId)         );
            item.setCategoryId(   cursor.getInt(idxCatId)      );
            item.setName(         cursor.getString(idxName)    );
            if (!cursor.isNull(idxSize)) {
                item.setSizeML(cursor.getInt(idxSize));
            }
            item.setAdaFriendly(  cursor.getInt(idxAda)  == 1  );
            item.setSoda(         cursor.getInt(idxSoda) == 1  );
            item.setClearLiquid(  cursor.getInt(idxClearLiq) == 1);
            item.setCategoryName( cursor.getString(idxCatName) );
        }
        
        cursor.close();
        return item;
    }

    public long updateItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("category_id", item.getCategoryId());
        values.put("name", item.getName());

        if (item.getSizeML() != null) {
            values.put("size_ml", item.getSizeML());
        } else {
            values.putNull("size_ml");
        }

        // Add description if the field exists in the database
        if (hasDescriptionColumn(db)) {
            values.put("description", item.getDescription());
        }

        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);
        values.put("is_soda", item.isSoda() ? 1 : 0);
        values.put("is_clear_liquid", item.isClearLiquid() ? 1 : 0);

        return db.update("Item", values, "item_id = ?", 
                        new String[]{String.valueOf(item.getItemId())});
    }

    public long addItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("category_id", item.getCategoryId());
        values.put("name", item.getName());

        if (item.getSizeML() != null) {
            values.put("size_ml", item.getSizeML());
        } else {
            values.putNull("size_ml");
        }

        // Add description if the field exists in the database
        if (hasDescriptionColumn(db)) {
            values.put("description", item.getDescription());
        }

        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);
        values.put("is_soda", item.isSoda() ? 1 : 0);
        values.put("is_clear_liquid", item.isClearLiquid() ? 1 : 0);

        return db.insert("Item", null, values);
    }

    // Helper method to check if description column exists
    private boolean hasDescriptionColumn(SQLiteDatabase db) {
        try {
            Cursor cursor = db.rawQuery("PRAGMA table_info(Item)", null);
            boolean hasDesc = false;
            if (cursor.moveToFirst()) {
                do {
                    String columnName = cursor.getString(cursor.getColumnIndex("name"));
                    if ("description".equals(columnName)) {
                        hasDesc = true;
                        break;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            return hasDesc;
        } catch (Exception e) {
            Log.w("ItemDAO", "Could not check for description column: " + e.getMessage());
            return false;
        }
    }

    // Alias method for AdminActivity compatibility
    public long insertItem(Item item) {
        return addItem(item);
    }

    public boolean deleteItem(int itemId) {
        // First check if item is used in any orders
        if (isItemUsedInOrders(itemId)) {
            return false; // Cannot delete item that's used in orders
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("Item", "item_id = ?", new String[]{String.valueOf(itemId)}) > 0;
    }

    public boolean isItemUsedInOrders(int itemId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM MealLine WHERE item_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(itemId)});

        boolean isUsed = false;
        if (cursor.moveToFirst()) {
            isUsed = cursor.getInt(0) > 0;
        }

        cursor.close();
        return isUsed;
    }

    // Category-specific methods
    public List<Item> getBreakfastItems() {
        return getItemsByCategory("Breakfast");
    }

    public List<Item> getProteinItems() {
        return getItemsByCategory("Protein/Entrée");
    }

    public List<Item> getStarchItems() {
        return getItemsByCategory("Starch");
    }

    public List<Item> getVegetableItems() {
        return getItemsByCategory("Vegetable");
    }

    public List<Item> getGrillItems() {
        return getItemsByCategory("Grill Item");
    }

    public List<Item> getDessertItems() {
        return getItemsByCategory("Dessert");
    }

    public List<Item> getSugarFreeDessertItems() {
        return getItemsByCategory("Sugar Free Dessert");
    }

    public List<Item> getDrinkItems() {
        return getItemsByCategory("Drink");
    }

    public List<Item> getSupplementItems() {
        return getItemsByCategory("Supplement");
    }

    public List<Item> getSodaItems() {
        return getItemsByCategory("Soda");
    }

    public List<Item> getJuiceItems() {
        return getItemsByCategory("Juices");
    }

    public List<Item> getColdCerealItems() {
        return getItemsByCategory("Cold Cereals");
    }

    public List<Item> getHotCerealItems() {
        return getItemsByCategory("Hot Cereals");
    }

    public List<Item> getBreadItems() {
        return getItemsByCategory("Breads");
    }

    public List<Item> getMuffinItems() {
        return getItemsByCategory("Fresh Muffins");
    }

    public List<Item> getFruitItems() {
        return getItemsByCategory("Fruits");
    }

    // Filter methods
    public List<Item> filterItemsForDiet(List<Item> items, boolean isADA, boolean filterBread) {
        List<Item> filteredItems = new ArrayList<>();
        
        for (Item item : items) {
            // ADA diet filtering
            if (isADA && !item.isAdaFriendly()) {
                continue; // Skip non-ADA items for ADA diet
            }
            
            // Bread filtering (for certain texture modifications)
            if (filterBread && item.isBread()) {
                continue; // Skip bread items when bread is not allowed
            }
            
            filteredItems.add(item);
        }
        
        return filteredItems;
    }
    
    public List<Item> filterClearLiquidItems(List<Item> items) {
        List<Item> filteredItems = new ArrayList<>();
        
        for (Item item : items) {
            if (item.isClearLiquid()) {
                filteredItems.add(item);
            }
        }
        
        return filteredItems;
    }
}