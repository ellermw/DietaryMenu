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
        
        // Meal items - convert lists to comma-separated strings
        values.put("breakfast_items", order.getBreakfastItems() != null ? 
                  String.join(",", order.getBreakfastItems()) : "");
        values.put("lunch_items", order.getLunchItems() != null ? 
                  String.join(",", order.getLunchItems()) : "");
        values.put("dinner_items", order.getDinnerItems() != null ? 
                  String.join(",", order.getDinnerItems()) : "");
        
        // Drink items
        values.put("breakfast_juices", order.getBreakfastJuices() != null ? 
                  String.join(",", order.getBreakfastJuices()) : "");
        values.put("lunch_juices", order.getLunchJuices() != null ? 
                  String.join(",", order.getLunchJuices()) : "");
        values.put("dinner_juices", order.getDinnerJuices() != null ? 
                  String.join(",", order.getDinnerJuices()) : "");
        
        values.put("breakfast_drinks", order.getBreakfastDrinks() != null ? 
                  String.join(",", order.getBreakfastDrinks()) : "");
        values.put("lunch_drinks", order.getLunchDrinks() != null ? 
                  String.join(",", order.getLunchDrinks()) : "");
        values.put("dinner_drinks", order.getDinnerDrinks() != null ? 
                  String.join(",", order.getDinnerDrinks()) : "");

        return db.insert("FinalizedOrder", null, values);
    }

    // Get all finalized orders
    public List<FinalizedOrder> getAllOrders() {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM FinalizedOrder ORDER BY order_date DESC, wing, room";
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

    // Get orders by location and date
    public List<FinalizedOrder> getOrdersByLocation(String wing, String room, String orderDate) {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM FinalizedOrder WHERE wing = ? AND room = ? AND order_date = ?";
        Cursor cursor = db.rawQuery(query, new String[]{wing, room, orderDate});

        if (cursor.moveToFirst()) {
            do {
                FinalizedOrder order = createOrderFromCursor(cursor);
                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return orders;
    }

    // Get orders by date
    public List<FinalizedOrder> getOrdersByDate(String orderDate) {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM FinalizedOrder WHERE order_date = ? ORDER BY wing, room";
        Cursor cursor = db.rawQuery(query, new String[]{orderDate});

        if (cursor.moveToFirst()) {
            do {
                FinalizedOrder order = createOrderFromCursor(cursor);
                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return orders;
    }

    // Delete an order
    public boolean deleteOrder(int orderId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete("FinalizedOrder", "order_id = ?", 
                                   new String[]{String.valueOf(orderId)});
        return rowsDeleted > 0;
    }

    // Helper method to create FinalizedOrder object from cursor
    private FinalizedOrder createOrderFromCursor(Cursor cursor) {
        FinalizedOrder order = new FinalizedOrder();
        
        order.setOrderId(cursor.getInt(cursor.getColumnIndex("order_id")));
        order.setPatientName(cursor.getString(cursor.getColumnIndex("patient_name")));
        order.setWing(cursor.getString(cursor.getColumnIndex("wing")));
        order.setRoom(cursor.getString(cursor.getColumnIndex("room")));
        order.setOrderDate(cursor.getString(cursor.getColumnIndex("order_date")));
        order.setDietType(cursor.getString(cursor.getColumnIndex("diet_type")));
        order.setFluidRestriction(cursor.getString(cursor.getColumnIndex("fluid_restriction")));
        
        // Texture modifications
        order.setMechanicalGround(cursor.getInt(cursor.getColumnIndex("mechanical_ground")) == 1);
        order.setMechanicalChopped(cursor.getInt(cursor.getColumnIndex("mechanical_chopped")) == 1);
        order.setBiteSize(cursor.getInt(cursor.getColumnIndex("bite_size")) == 1);
        order.setBreadOK(cursor.getInt(cursor.getColumnIndex("bread_ok")) == 1);
        
        // Parse meal items from comma-separated strings
        String breakfastItems = cursor.getString(cursor.getColumnIndex("breakfast_items"));
        order.setBreakfastItems(breakfastItems != null && !breakfastItems.isEmpty() ? 
                               Arrays.asList(breakfastItems.split(",")) : new ArrayList<>());
        
        String lunchItems = cursor.getString(cursor.getColumnIndex("lunch_items"));
        order.setLunchItems(lunchItems != null && !lunchItems.isEmpty() ? 
                           Arrays.asList(lunchItems.split(",")) : new ArrayList<>());
        
        String dinnerItems = cursor.getString(cursor.getColumnIndex("dinner_items"));
        order.setDinnerItems(dinnerItems != null && !dinnerItems.isEmpty() ? 
                            Arrays.asList(dinnerItems.split(",")) : new ArrayList<>());
        
        // Parse drink items
        String breakfastJuices = cursor.getString(cursor.getColumnIndex("breakfast_juices"));
        order.setBreakfastJuices(breakfastJuices != null && !breakfastJuices.isEmpty() ? 
                                Arrays.asList(breakfastJuices.split(",")) : new ArrayList<>());
        
        String lunchJuices = cursor.getString(cursor.getColumnIndex("lunch_juices"));
        order.setLunchJuices(lunchJuices != null && !lunchJuices.isEmpty() ? 
                            Arrays.asList(lunchJuices.split(",")) : new ArrayList<>());
        
        String dinnerJuices = cursor.getString(cursor.getColumnIndex("dinner_juices"));
        order.setDinnerJuices(dinnerJuices != null && !dinnerJuices.isEmpty() ? 
                             Arrays.asList(dinnerJuices.split(",")) : new ArrayList<>());
        
        String breakfastDrinks = cursor.getString(cursor.getColumnIndex("breakfast_drinks"));
        order.setBreakfastDrinks(breakfastDrinks != null && !breakfastDrinks.isEmpty() ? 
                                Arrays.asList(breakfastDrinks.split(",")) : new ArrayList<>());
        
        String lunchDrinks = cursor.getString(cursor.getColumnIndex("lunch_drinks"));
        order.setLunchDrinks(lunchDrinks != null && !lunchDrinks.isEmpty() ? 
                            Arrays.asList(lunchDrinks.split(",")) : new ArrayList<>());
        
        String dinnerDrinks = cursor.getString(cursor.getColumnIndex("dinner_drinks"));
        order.setDinnerDrinks(dinnerDrinks != null && !dinnerDrinks.isEmpty() ? 
                             Arrays.asList(dinnerDrinks.split(",")) : new ArrayList<>());
        
        return order;
    }
}