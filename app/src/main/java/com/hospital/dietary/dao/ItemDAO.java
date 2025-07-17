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
            // Test query with new column names
            String testQuery = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                              "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                              "FROM Item i " +
                              "INNER JOIN Category c ON i.category_id = c.category_id " +
                              "LIMIT 1";
            Cursor testCursor = db.rawQuery(testQuery, null);
            testCursor.close();
            
            // If test succeeds, use new column names
            return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                   "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                   "FROM Item i " +
                   "INNER JOIN Category c ON i.category_id = c.category_id " +
                   "ORDER BY c.name, i.name";
        } catch (Exception e) {
            // Fall back to old column names
            Log.w("ItemDAO", "Using fallback column names due to: " + e.getMessage());
            return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                   "i.ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                   "FROM Item i " +
                   "INNER JOIN Category c ON i.category_id = c.category_id " +
                   "ORDER BY c.name, i.name";
        }
    }
    
    // FIXED: Get ADA friendly column index with fallback
    private int getAdaFriendlyColumnIndex(Cursor cursor) {
        try {
            return cursor.getColumnIndexOrThrow("is_ada_friendly");
        } catch (IllegalArgumentException e) {
            try {
                return cursor.getColumnIndexOrThrow("ada_friendly");
            } catch (IllegalArgumentException e2) {
                Log.w("ItemDAO", "Neither is_ada_friendly nor ada_friendly column found, defaulting to 0");
                return -1; // Will be handled as false
            }
        }
    }
    
    // FIXED: Get soda column index with fallback
    private int getSodaColumnIndex(Cursor cursor) {
        try {
            return cursor.getColumnIndexOrThrow("is_soda");
        } catch (IllegalArgumentException e) {
            Log.w("ItemDAO", "is_soda column not found, defaulting to 0");
            return -1; // Will be handled as false
        }
    }
    
    // FIXED: Get clear liquid column index with fallback
    private int getClearLiquidColumnIndex(Cursor cursor) {
        try {
            return cursor.getColumnIndexOrThrow("is_clear_liquid");
        } catch (IllegalArgumentException e) {
            Log.w("ItemDAO", "is_clear_liquid column not found, defaulting to 0");
            return -1; // Will be handled as false
        }
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
            // Test query with new column names
            String testQuery = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                              "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                              "FROM Item i " +
                              "INNER JOIN Category c ON i.category_id = c.category_id " +
                              "LIMIT 1";
            Cursor testCursor = db.rawQuery(testQuery, null);
            testCursor.close();
            
            // If test succeeds, use new column names
            return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                   "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                   "FROM Item i " +
                   "INNER JOIN Category c ON i.category_id = c.category_id " +
                   "WHERE c.name = ? " +
                   "ORDER BY i.name";
        } catch (Exception e) {
            // Fall back to old column names
            return "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                   "i.ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                   "FROM Item i " +
                   "INNER JOIN Category c ON i.category_id = c.category_id " +
                   "WHERE c.name = ? " +
                   "ORDER BY i.name";
        }
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
            // ADA filtering - skip non-ADA items if ADA diet is selected
            if (isADA && !item.isAdaFriendly()) {
                continue;
            }

            // Bread filtering for texture modifications - skip bread items if filterBread is true
            if (filterBread && item.isBread()) {
                continue;
            }

            filteredItems.add(item);
        }

        return filteredItems;
    }

    public List<Item> getAdaFriendlyItems(List<Item> items) {
        List<Item> adaItems = new ArrayList<>();
        for (Item item : items) {
            if (item.isAdaFriendly()) {
                adaItems.add(item);
            }
        }
        return adaItems;
    }

    public List<Item> getNonBreadItems(List<Item> items) {
        List<Item> nonBreadItems = new ArrayList<>();
        for (Item item : items) {
            if (!item.isBread()) {
                nonBreadItems.add(item);
            }
        }
        return nonBreadItems;
    }

    public List<Item> getClearLiquidItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT i.item_id, i.category_id, i.name, i.size_ml, " +
                      "i.is_ada_friendly, i.is_soda, i.is_clear_liquid, c.name as category_name " +
                      "FROM Item i " +
                      "INNER JOIN Category c ON i.category_id = c.category_id " +
                      "WHERE i.is_clear_liquid = 1 " +
                      "ORDER BY c.name, i.name";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            int idxId       = cursor.getColumnIndexOrThrow("item_id");
            int idxCatId    = cursor.getColumnIndexOrThrow("category_id");
            int idxName     = cursor.getColumnIndexOrThrow("name");
            int idxSize     = cursor.getColumnIndex("size_ml");
            int idxAda      = cursor.getColumnIndexOrThrow("is_ada_friendly");
            int idxSoda     = cursor.getColumnIndexOrThrow("is_soda");
            int idxClearLiq = cursor.getColumnIndexOrThrow("is_clear_liquid");
            int idxCatName  = cursor.getColumnIndexOrThrow("category_name");

            do {
                Item item = new Item();
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
                items.add(item);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return items;
    }

    // Fluid restriction method
    public Integer getFluidLimit(String fluidRestriction, String meal) {
        if (fluidRestriction == null || fluidRestriction.equals("No Restriction") || fluidRestriction.equals("None")) {
            return null;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT rl.limit_ml FROM FluidRestriction fr " +
                "INNER JOIN RestrictionLimit rl ON fr.fluid_id = rl.fluid_id " +
                "WHERE fr.name = ? AND rl.meal = ?";

        Cursor cursor = db.rawQuery(query, new String[]{fluidRestriction, meal});

        Integer limit = null;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndexOrThrow("limit_ml");
            limit = cursor.getInt(index);
        }

        cursor.close();
        return limit;
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

        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);
        values.put("is_soda", item.isSoda() ? 1 : 0);
        values.put("is_clear_liquid", item.isClearLiquid() ? 1 : 0);

        return db.insert("Item", null, values);
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
}