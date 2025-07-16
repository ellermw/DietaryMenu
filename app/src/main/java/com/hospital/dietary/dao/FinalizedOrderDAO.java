// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/dao/FinalizedOrderDAO.java
// ================================================================================================

package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.FinalizedOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FinalizedOrderDAO {
    private DatabaseHelper dbHelper;

    public FinalizedOrderDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Check if an order already exists for this wing, room, and date
    public boolean orderExists(String wing, String room, String orderDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM FinalizedOrder WHERE wing = ? AND room = ? AND order_date = ?";
        Cursor cursor = db.rawQuery(query, new String[]{wing, room, orderDate});
        
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    // Save a finalized order
    public long saveFinalizedOrder(FinalizedOrder order) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patient_name", order.getPatientName());
        values.put("wing", order.getWing());
        values.put("room", order.getRoom());
        values.put("order_date", order.getOrderDate());
        values.put("diet_type", order.getDietType());
        values.put("fluid_restriction", order.getFluidRestriction());
        
        // Texture modifications
        values.put("mechanical_ground", order.isMechanicalGround() ? 1 : 0);
        values.put("mechanical_chopped", order.isMechanicalChopped() ? 1 : 0);
        values.put("bite_size", order.isBiteSize() ? 1 : 0);
        values.put("bread_ok", order.isBreadOK() ? 1 : 0);
        
        // Convert lists to comma-separated strings for storage
        values.put("breakfast_items", String.join("|", order.getBreakfastItems()));
        values.put("lunch_items", String.join("|", order.getLunchItems()));
        values.put("dinner_items", String.join("|", order.getDinnerItems()));
        values.put("breakfast_juices", String.join("|", order.getBreakfastJuices()));
        values.put("lunch_juices", String.join("|", order.getLunchJuices()));
        values.put("dinner_juices", String.join("|", order.getDinnerJuices()));
        values.put("breakfast_drinks", String.join("|", order.getBreakfastDrinks()));
        values.put("lunch_drinks", String.join("|", order.getLunchDrinks()));
        values.put("dinner_drinks", String.join("|", order.getDinnerDrinks()));

        return db.insert("FinalizedOrder", null, values);
    }

    // Update an existing finalized order (for overwrite functionality)
    public boolean updateFinalizedOrder(FinalizedOrder order) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patient_name", order.getPatientName());
        values.put("diet_type", order.getDietType());
        values.put("fluid_restriction", order.getFluidRestriction());
        
        // Texture modifications
        values.put("mechanical_ground", order.isMechanicalGround() ? 1 : 0);
        values.put("mechanical_chopped", order.isMechanicalChopped() ? 1 : 0);
        values.put("bite_size", order.isBiteSize() ? 1 : 0);
        values.put("bread_ok", order.isBreadOK() ? 1 : 0);
        
        // Convert lists to comma-separated strings for storage
        values.put("breakfast_items", String.join("|", order.getBreakfastItems()));
        values.put("lunch_items", String.join("|", order.getLunchItems()));
        values.put("dinner_items", String.join("|", order.getDinnerItems()));
        values.put("breakfast_juices", String.join("|", order.getBreakfastJuices()));
        values.put("lunch_juices", String.join("|", order.getLunchJuices()));
        values.put("dinner_juices", String.join("|", order.getDinnerJuices()));
        values.put("breakfast_drinks", String.join("|", order.getBreakfastDrinks()));
        values.put("lunch_drinks", String.join("|", order.getLunchDrinks()));
        values.put("dinner_drinks", String.join("|", order.getDinnerDrinks()));

        int rowsAffected = db.update("FinalizedOrder", values, 
            "wing = ? AND room = ? AND order_date = ?", 
            new String[]{order.getWing(), order.getRoom(), order.getOrderDate()});
        
        return rowsAffected > 0;
    }

    // Get all finalized orders sorted by Date > Wing > Room (desc)
    public List<FinalizedOrder> getAllFinalizedOrders() {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM FinalizedOrder " +
                      "ORDER BY order_date ASC, wing ASC, " +
                      "CAST(CASE WHEN room GLOB '[0-9]*' THEN room ELSE '0' END AS INTEGER) DESC, " +
                      "room DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                FinalizedOrder order = createOrderFromCursor(cursor);
                orders.add(order);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return orders;
    }

    // Get finalized orders filtered by date
    public List<FinalizedOrder> getFinalizedOrdersByDate(String date) {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM FinalizedOrder WHERE order_date = ? " +
                      "ORDER BY wing ASC, " +
                      "CAST(CASE WHEN room GLOB '[0-9]*' THEN room ELSE '0' END AS INTEGER) DESC, " +
                      "room DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{date});
        
        if (cursor.moveToFirst()) {
            do {
                FinalizedOrder order = createOrderFromCursor(cursor);
                orders.add(order);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return orders;
    }

    // Get unique order dates
    public List<String> getDistinctOrderDates() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT DISTINCT order_date FROM FinalizedOrder ORDER BY order_date DESC";
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return dates;
    }

    // Delete a finalized order
    public boolean deleteFinalizedOrder(String wing, String room, String orderDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete("FinalizedOrder", 
            "wing = ? AND room = ? AND order_date = ?", 
            new String[]{wing, room, orderDate});
        return rowsDeleted > 0;
    }

    // Helper method to create FinalizedOrder object from cursor
    private FinalizedOrder createOrderFromCursor(Cursor cursor) {
        FinalizedOrder order = new FinalizedOrder();
        
        order.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow("order_id")));
        order.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
        order.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
        order.setRoom(cursor.getString(cursor.getColumnIndexOrThrow("room")));
        order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
        order.setDietType(cursor.getString(cursor.getColumnIndexOrThrow("diet_type")));
        order.setFluidRestriction(cursor.getString(cursor.getColumnIndexOrThrow("fluid_restriction")));
        
        // Texture modifications
        order.setMechanicalGround(cursor.getInt(cursor.getColumnIndexOrThrow("mechanical_ground")) == 1);
        order.setMechanicalChopped(cursor.getInt(cursor.getColumnIndexOrThrow("mechanical_chopped")) == 1);
        order.setBiteSize(cursor.getInt(cursor.getColumnIndexOrThrow("bite_size")) == 1);
        order.setBreadOK(cursor.getInt(cursor.getColumnIndexOrThrow("bread_ok")) == 1);
        
        // Convert pipe-separated strings back to lists
        order.setBreakfastItems(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("breakfast_items"))));
        order.setLunchItems(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("lunch_items"))));
        order.setDinnerItems(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("dinner_items"))));
        order.setBreakfastJuices(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("breakfast_juices"))));
        order.setLunchJuices(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("lunch_juices"))));
        order.setDinnerJuices(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("dinner_juices"))));
        order.setBreakfastDrinks(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("breakfast_drinks"))));
        order.setLunchDrinks(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("lunch_drinks"))));
        order.setDinnerDrinks(stringToList(cursor.getString(cursor.getColumnIndexOrThrow("dinner_drinks"))));
        
        return order;
    }
    
    // Helper method to convert pipe-separated string to list
    private List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(str.split("\\|")));
    }
}