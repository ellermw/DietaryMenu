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

        try {
            return db.insert("FinalizedOrder", null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // FIXED: Get orders by wing, room and date
    public List<FinalizedOrder> getOrdersByWingRoomAndDate(String wing, String room, String orderDate) {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM FinalizedOrder WHERE wing = ? AND room = ? AND order_date = ?";
        Cursor cursor = db.rawQuery(query, new String[]{wing, room, orderDate});

        while (cursor.moveToNext()) {
            orders.add(createOrderFromCursor(cursor));
        }

        cursor.close();
        return orders;
    }

    // FIXED: Get single order by wing, room and date
    public FinalizedOrder getOrderByWingRoomAndDate(String wing, String room, String orderDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM FinalizedOrder WHERE wing = ? AND room = ? AND order_date = ? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{wing, room, orderDate});

        FinalizedOrder order = null;
        if (cursor.moveToFirst()) {
            order = createOrderFromCursor(cursor);
        }

        cursor.close();
        return order;
    }

    // Get all orders for a specific date
    public List<FinalizedOrder> getOrdersByDate(String orderDate) {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM FinalizedOrder WHERE order_date = ? ORDER BY wing, room";
        Cursor cursor = db.rawQuery(query, new String[]{orderDate});

        while (cursor.moveToNext()) {
            orders.add(createOrderFromCursor(cursor));
        }

        cursor.close();
        return orders;
    }

    // Get all orders
    public List<FinalizedOrder> getAllOrders() {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM FinalizedOrder ORDER BY order_date DESC, wing, room";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            orders.add(createOrderFromCursor(cursor));
        }

        cursor.close();
        return orders;
    }

    // Delete an order
    public boolean deleteOrder(int orderId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete("FinalizedOrder", "order_id = ?",
                new String[]{String.valueOf(orderId)});
        return rowsAffected > 0;
    }

    // Update an existing order
    public boolean updateOrder(FinalizedOrder order) {
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

        // Meal items
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

        int rowsAffected = db.update("FinalizedOrder", values, "order_id = ?",
                new String[]{String.valueOf(order.getOrderId())});
        return rowsAffected > 0;
    }

    // Helper method to create FinalizedOrder from cursor
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

        // Parse meal items from comma-separated strings
        String breakfastItems = cursor.getString(cursor.getColumnIndexOrThrow("breakfast_items"));
        if (breakfastItems != null && !breakfastItems.trim().isEmpty()) {
            order.setBreakfastItems(Arrays.asList(breakfastItems.split(",")));
        }

        String lunchItems = cursor.getString(cursor.getColumnIndexOrThrow("lunch_items"));
        if (lunchItems != null && !lunchItems.trim().isEmpty()) {
            order.setLunchItems(Arrays.asList(lunchItems.split(",")));
        }

        String dinnerItems = cursor.getString(cursor.getColumnIndexOrThrow("dinner_items"));
        if (dinnerItems != null && !dinnerItems.trim().isEmpty()) {
            order.setDinnerItems(Arrays.asList(dinnerItems.split(",")));
        }

        // Parse drink items
        String breakfastJuices = cursor.getString(cursor.getColumnIndexOrThrow("breakfast_juices"));
        if (breakfastJuices != null && !breakfastJuices.trim().isEmpty()) {
            order.setBreakfastJuices(Arrays.asList(breakfastJuices.split(",")));
        }

        String lunchJuices = cursor.getString(cursor.getColumnIndexOrThrow("lunch_juices"));
        if (lunchJuices != null && !lunchJuices.trim().isEmpty()) {
            order.setLunchJuices(Arrays.asList(lunchJuices.split(",")));
        }

        String dinnerJuices = cursor.getString(cursor.getColumnIndexOrThrow("dinner_juices"));
        if (dinnerJuices != null && !dinnerJuices.trim().isEmpty()) {
            order.setDinnerJuices(Arrays.asList(dinnerJuices.split(",")));
        }

        String breakfastDrinks = cursor.getString(cursor.getColumnIndexOrThrow("breakfast_drinks"));
        if (breakfastDrinks != null && !breakfastDrinks.trim().isEmpty()) {
            order.setBreakfastDrinks(Arrays.asList(breakfastDrinks.split(",")));
        }

        String lunchDrinks = cursor.getString(cursor.getColumnIndexOrThrow("lunch_drinks"));
        if (lunchDrinks != null && !lunchDrinks.trim().isEmpty()) {
            order.setLunchDrinks(Arrays.asList(lunchDrinks.split(",")));
        }

        String dinnerDrinks = cursor.getString(cursor.getColumnIndexOrThrow("dinner_drinks"));
        if (dinnerDrinks != null && !dinnerDrinks.trim().isEmpty()) {
            order.setDinnerDrinks(Arrays.asList(dinnerDrinks.split(",")));
        }

        return order;
    }
}