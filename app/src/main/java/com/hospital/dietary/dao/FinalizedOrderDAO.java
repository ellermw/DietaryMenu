package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.FinalizedOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FinalizedOrderDAO compatibility class
 */
public class FinalizedOrderDAO {

    private final DatabaseHelper dbHelper;

    public FinalizedOrderDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Insert finalized order
     */
    public long insertFinalizedOrder(FinalizedOrder order) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Basic info
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

        // Juices
        values.put("breakfast_juices", order.getBreakfastJuices() != null ?
                String.join(",", order.getBreakfastJuices()) : "");
        values.put("lunch_juices", order.getLunchJuices() != null ?
                String.join(",", order.getLunchJuices()) : "");
        values.put("dinner_juices", order.getDinnerJuices() != null ?
                String.join(",", order.getDinnerJuices()) : "");

        // Drinks
        values.put("breakfast_drinks", order.getBreakfastDrinks() != null ?
                String.join(",", order.getBreakfastDrinks()) : "");
        values.put("lunch_drinks", order.getLunchDrinks() != null ?
                String.join(",", order.getLunchDrinks()) : "");
        values.put("dinner_drinks", order.getDinnerDrinks() != null ?
                String.join(",", order.getDinnerDrinks()) : "");

        return db.insert("finalized_order", null, values);
    }

    /**
     * Get finalized order by ID
     */
    public FinalizedOrder getFinalizedOrderById(int orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("finalized_order", null,
                "order_id = ?", new String[]{String.valueOf(orderId)},
                null, null, null);

        FinalizedOrder order = null;
        if (cursor.moveToFirst()) {
            order = cursorToFinalizedOrder(cursor);
        }
        cursor.close();
        return order;
    }

    /**
     * Get all finalized orders
     */
    public List<FinalizedOrder> getAllFinalizedOrders() {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("finalized_order", null,
                null, null, null, null, "order_date DESC");

        if (cursor.moveToFirst()) {
            do {
                orders.add(cursorToFinalizedOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    /**
     * Get finalized orders by date
     */
    public List<FinalizedOrder> getFinalizedOrdersByDate(String date) {
        List<FinalizedOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("finalized_order", null,
                "order_date = ?", new String[]{date},
                null, null, "wing, room");

        if (cursor.moveToFirst()) {
            do {
                orders.add(cursorToFinalizedOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    /**
     * Update finalized order
     */
    public int updateFinalizedOrder(FinalizedOrder order) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Basic info
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

        // Juices
        values.put("breakfast_juices", order.getBreakfastJuices() != null ?
                String.join(",", order.getBreakfastJuices()) : "");
        values.put("lunch_juices", order.getLunchJuices() != null ?
                String.join(",", order.getLunchJuices()) : "");
        values.put("dinner_juices", order.getDinnerJuices() != null ?
                String.join(",", order.getDinnerJuices()) : "");

        // Drinks
        values.put("breakfast_drinks", order.getBreakfastDrinks() != null ?
                String.join(",", order.getBreakfastDrinks()) : "");
        values.put("lunch_drinks", order.getLunchDrinks() != null ?
                String.join(",", order.getLunchDrinks()) : "");
        values.put("dinner_drinks", order.getDinnerDrinks() != null ?
                String.join(",", order.getDinnerDrinks()) : "");

        return db.update("finalized_order", values, "order_id = ?",
                new String[]{String.valueOf(order.getOrderId())});
    }

    /**
     * Convert cursor to FinalizedOrder
     */
    private FinalizedOrder cursorToFinalizedOrder(Cursor cursor) {
        FinalizedOrder order = new FinalizedOrder();
        order.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow("order_id")));
        order.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("patient_name")));
        order.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
        order.setRoom(cursor.getString(cursor.getColumnIndexOrThrow("room")));
        order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow("order_date")));
        order.setDietType(cursor.getString(cursor.getColumnIndexOrThrow("diet_type")));

        // Get other fields if they exist
        int fluidIndex = cursor.getColumnIndex("fluid_restriction");
        if (fluidIndex != -1) {
            order.setFluidRestriction(cursor.getString(fluidIndex));
        }

        // Texture modifications
        int groundIndex = cursor.getColumnIndex("mechanical_ground");
        if (groundIndex != -1) {
            order.setMechanicalGround(cursor.getInt(groundIndex) == 1);
        }

        int choppedIndex = cursor.getColumnIndex("mechanical_chopped");
        if (choppedIndex != -1) {
            order.setMechanicalChopped(cursor.getInt(choppedIndex) == 1);
        }

        int biteIndex = cursor.getColumnIndex("bite_size");
        if (biteIndex != -1) {
            order.setBiteSize(cursor.getInt(biteIndex) == 1);
        }

        int breadIndex = cursor.getColumnIndex("bread_ok");
        if (breadIndex != -1) {
            order.setBreadOK(cursor.getInt(breadIndex) == 1);
        }

        // Meal items
        int breakfastItemsIndex = cursor.getColumnIndex("breakfast_items");
        if (breakfastItemsIndex != -1) {
            String items = cursor.getString(breakfastItemsIndex);
            if (items != null && !items.isEmpty()) {
                order.setBreakfastItems(Arrays.asList(items.split(",")));
            }
        }

        int lunchItemsIndex = cursor.getColumnIndex("lunch_items");
        if (lunchItemsIndex != -1) {
            String items = cursor.getString(lunchItemsIndex);
            if (items != null && !items.isEmpty()) {
                order.setLunchItems(Arrays.asList(items.split(",")));
            }
        }

        int dinnerItemsIndex = cursor.getColumnIndex("dinner_items");
        if (dinnerItemsIndex != -1) {
            String items = cursor.getString(dinnerItemsIndex);
            if (items != null && !items.isEmpty()) {
                order.setDinnerItems(Arrays.asList(items.split(",")));
            }
        }

        // Similar for juices and drinks...

        return order;
    }
}