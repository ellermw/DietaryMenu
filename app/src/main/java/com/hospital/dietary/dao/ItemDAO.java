// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/dao/ItemDAO.java
// ================================================================================================

package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Item;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {
    private DatabaseHelper dbHelper;

    public ItemDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

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
            int idxId       = cursor.getColumnIndexOrThrow("item_id");
            int idxCatId    = cursor.getColumnIndexOrThrow("category_id");
            int idxName     = cursor.getColumnIndexOrThrow("name");
            int idxSize     = cursor.getColumnIndex("size_ml");
            int idxAda      = cursor.getColumnIndexOrThrow("is_ada_friendly");
            int idxSoda     = cursor.getColumnIndexOrThrow("is_soda");
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
                item.setCategoryName( cursor.getString(idxCatName) );
                items.add(item);
            } while (cursor.moveToNext());

            // INFO: The below was wrong.
            /*do {
                Item item = new Item();
                item.setItemId(cursor.getInt("item_id"));
                item.setCategoryId(cursor.getInt("category_id"));
                item.setName(cursor.getString("name"));
                
                if (!cursor.isNull(cursor.getColumnIndex("size_ml"))) {
                    item.setSizeML(cursor.getInt("size_ml"));
                }
                
                item.setAdaFriendly(cursor.getInt("is_ada_friendly") == 1);
                item.setSoda(cursor.getInt("is_soda") == 1);
                item.setCategoryName(cursor.getString("category_name"));
                
                items.add(item);
            } while (cursor.moveToNext());*/
        }
        
        cursor.close();
        return items;
    }

    public List<Item> getBreakfastItems() {
        return getItemsByCategory("Breakfast");
    }

    public List<Item> getProteinItems() {
        List<Item> items = new ArrayList<>();
        items.addAll(getItemsByCategory("Protein/Entrée"));
        items.addAll(getItemsByCategory("Grill Item"));
        return items;
    }

    public List<Item> getDrinkItems() {
        List<Item> items = new ArrayList<>();
        items.addAll(getItemsByCategory("Drink"));
        items.addAll(getItemsByCategory("Soda"));
        items.addAll(getItemsByCategory("Supplement"));
        return items;
    }

    public List<Item> getJuiceItems() {
        return getItemsByCategory("Juices");
    }

    public List<Item> getDessertItems() {
        List<Item> items = new ArrayList<>();
        items.addAll(getItemsByCategory("Dessert"));
        items.addAll(getItemsByCategory("Sugar Free Dessert"));
        return items;
    }

    public List<Item> getStarchItems() {
        return getItemsByCategory("Starch");
    }

    public List<Item> getVegetableItems() {
        return getItemsByCategory("Vegetable");
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

    public List<Item> getFilteredItems(List<Item> items, boolean isADA, boolean filterBread) {
        List<Item> filteredItems = new ArrayList<>();
        
        for (Item item : items) {
            // ADA filtering
            if (isADA && !item.isAdaFriendly()) {
                continue;
            }
            
            // Bread filtering for texture modifications
            if (filterBread && item.isBread()) {
                continue;
            }
            
            filteredItems.add(item);
        }
        
        return filteredItems;
    }

    public Integer getFluidLimit(String fluidRestriction, String meal) {
        if (fluidRestriction == null || fluidRestriction.equals("No Restriction")) {
            return null;
        }
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT rl.limit_ml FROM FluidRestriction fr " +
                      "INNER JOIN RestrictionLimit rl ON fr.fluid_id = rl.fluid_id " +
                      "WHERE fr.name = ? AND rl.meal = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{fluidRestriction, meal});
        
        Integer limit = null;
        if (cursor.moveToFirst()) {
            // INFO: Need to pull index first
            int index = cursor.getColumnIndexOrThrow("limit_ml");
            limit = cursor.getInt(index);
        }
        
        cursor.close();
        return limit;
    }

    public long addItem(Item item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("category_id", item.getCategoryId());
        values.put("name", item.getName());
        
        if (item.getSizeML() != null) {
            values.put("size_ml", item.getSizeML());
        }
        
        values.put("is_ada_friendly", item.isAdaFriendly() ? 1 : 0);
        values.put("is_soda", item.isSoda() ? 1 : 0);
        
        return db.insert("Item", null, values);
    }

    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("Item", "item_id = ?", new String[]{String.valueOf(itemId)}) > 0;
    }

    public long savePatient(String name, String wing, String room, String diet) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("name", name);
        values.put("wing", wing);
        values.put("room_number", room);
        
        // Get diet ID
        Cursor cursor = db.rawQuery("SELECT diet_id FROM Diet WHERE name = ?", new String[]{diet});
        if (cursor.moveToFirst()) {
            // INFO: This is how this needs to happen
            int index = cursor.getColumnIndexOrThrow("diet_id");
            values.put("diet_id", cursor.getInt(index));
        }
        cursor.close();
        
        return db.insert("Patient", null, values);
    }

    public long saveMealOrder(int patientId, String meal, String timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("patient_id", patientId);
        values.put("meal", meal);
        values.put("guest_tray", 0);
        values.put("timestamp", timestamp);
        
        return db.insert("MealOrder", null, values);
    }

    public void saveMealLine(int orderId, int itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("order_id", orderId);
        values.put("item_id", itemId);
        
        db.insert("MealLine", null, values);
    }

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
            int itemIndex = cursor.getColumnIndexOrThrow("item_id");
            int catIndex = cursor.getColumnIndexOrThrow("category_id");
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int sizeIndex = cursor.getColumnIndexOrThrow("size_ml");
            int friendlyIndex = cursor.getColumnIndexOrThrow("is_ada_friendly");
            int sodaIndex = cursor.getColumnIndexOrThrow("is_soda");
            int catNameIndex = cursor.getColumnIndexOrThrow("category_name");

            item = new Item();
            item.setItemId(cursor.getInt(itemIndex));
            item.setCategoryId(cursor.getInt(catIndex));
            item.setName(cursor.getString(nameIndex));
            
            if (!cursor.isNull(sizeIndex)) {
                item.setSizeML(cursor.getInt(sizeIndex));
            }
            
            item.setAdaFriendly(cursor.getInt(friendlyIndex) == 1);
            item.setSoda(cursor.getInt(sodaIndex) == 1);
            item.setCategoryName(cursor.getString(catNameIndex));
        }
        
        cursor.close();
        return item;
    }
}
