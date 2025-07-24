package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Order;
import com.hospital.dietary.models.PatientOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDAO {
    private DatabaseHelper dbHelper;

    public OrderDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<String> getAvailableDates() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // Get distinct dates from meal_orders
            Cursor cursor = db.rawQuery(
                    "SELECT DISTINCT date(order_date) as order_date FROM meal_orders " +
                            "ORDER BY order_date DESC LIMIT 30", null);

            while (cursor.moveToNext()) {
                dates.add(cursor.getString(0));
            }
            cursor.close();

            // If no dates, add today
            if (dates.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                dates.add(sdf.format(new Date()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dates;
    }

    public List<PatientOrder> getOrdersByDate(String date) {
        List<PatientOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT p.patient_id, p.patient_first_name || ' ' || p.patient_last_name as name, " +
                "p.room_number, p.wing, p.diet, mo.timestamp, " +
                "p.fluid_restriction, p.texture_modifications " +
                "FROM patient_info p " +
                "LEFT JOIN meal_orders mo ON p.patient_id = mo.patient_id " +
                "AND date(mo.order_date) = ? " +
                "ORDER BY p.wing, CAST(p.room_number AS INTEGER)";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        int idxPatientId = cursor.getColumnIndex("patient_id");
        int idxName = cursor.getColumnIndex("name");
        int idxRoom = cursor.getColumnIndex("room_number");
        int idxWing = cursor.getColumnIndex("wing");
        int idxDiet = cursor.getColumnIndex("diet");
        int idxTimestamp = cursor.getColumnIndex("timestamp");
        int idxFluidRestriction = cursor.getColumnIndex("fluid_restriction");
        int idxTextureModifications = cursor.getColumnIndex("texture_modifications");

        while (cursor.moveToNext()) {
            PatientOrder order = new PatientOrder();
            if (idxPatientId != -1) order.setPatientId(cursor.getInt(idxPatientId));
            if (idxName != -1) order.setPatientName(cursor.getString(idxName));
            if (idxRoom != -1) order.setRoom(cursor.getString(idxRoom));
            if (idxWing != -1) order.setWing(cursor.getString(idxWing));
            if (idxDiet != -1) order.setDiet(cursor.getString(idxDiet));
            if (idxTimestamp != -1) order.setTimestamp(cursor.getString(idxTimestamp));

            if (idxFluidRestriction != -1 && !cursor.isNull(idxFluidRestriction)) {
                order.setFluidRestriction(cursor.getString(idxFluidRestriction));
            }

            if (idxTextureModifications != -1 && !cursor.isNull(idxTextureModifications)) {
                order.setTextureModifications(cursor.getString(idxTextureModifications));
            }

            // Get meal items
            loadMealItems(order, date);

            orders.add(order);
        }
        cursor.close();

        return orders;
    }

    private void loadMealItems(PatientOrder order, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get meal items
        String query = "SELECT meal, GROUP_CONCAT(item_name, ', ') as items " +
                "FROM (SELECT mo.meal, i.name as item_name " +
                "FROM meal_orders mo " +
                "JOIN order_items oi ON mo.order_id = oi.order_id " +
                "JOIN items i ON oi.item_id = i.item_id " +
                "WHERE mo.patient_id = ? AND date(mo.order_date) = ? " +
                "ORDER BY i.category) " +
                "GROUP BY meal";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(order.getPatientId()), date});

        StringBuilder breakfast = new StringBuilder();
        StringBuilder lunch = new StringBuilder();
        StringBuilder dinner = new StringBuilder();

        while (cursor.moveToNext()) {
            String meal = cursor.getString(0);
            String items = cursor.getString(1);

            if ("Breakfast".equals(meal)) {
                breakfast.append(items);
            } else if ("Lunch".equals(meal)) {
                lunch.append(items);
            } else if ("Dinner".equals(meal)) {
                dinner.append(items);
            }
        }
        cursor.close();

        order.setBreakfastItems(breakfast.toString());
        order.setLunchItems(lunch.toString());
        order.setDinnerItems(dinner.toString());
    }

    public int getOrderCountForDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(DISTINCT patient_id) FROM meal_orders " +
                        "WHERE date(order_date) = ?", new String[]{date});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

    public boolean createOrderForPatient(long patientId, String meal, String date, String createdBy) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("patient_id", patientId);
        values.put("meal", meal);
        values.put("order_date", date);
        values.put("is_complete", 0);
        values.put("created_by", createdBy);
        values.put("timestamp", System.currentTimeMillis());

        long orderId = db.insert("meal_orders", null, values);
        return orderId != -1;
    }

    public boolean addItemToOrder(long orderId, long itemId, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("order_id", orderId);
        values.put("item_id", itemId);
        values.put("quantity", quantity);

        long id = db.insert("order_items", null, values);
        return id != -1;
    }

    public boolean removeItemFromOrder(long orderId, long itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rows = db.delete("order_items",
                "order_id = ? AND item_id = ?",
                new String[]{String.valueOf(orderId), String.valueOf(itemId)});

        return rows > 0;
    }

    public boolean updateOrderStatus(long orderId, boolean isComplete) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("is_complete", isComplete ? 1 : 0);

        int rows = db.update("meal_orders", values,
                "order_id = ?", new String[]{String.valueOf(orderId)});

        return rows > 0;
    }

    public long getOrderIdForPatientMealDate(long patientId, String meal, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("meal_orders",
                new String[]{"order_id"},
                "patient_id = ? AND meal = ? AND date(order_date) = ?",
                new String[]{String.valueOf(patientId), meal, date},
                null, null, null);

        long orderId = -1;
        if (cursor.moveToFirst()) {
            orderId = cursor.getLong(0);
        }
        cursor.close();

        return orderId;
    }

    public List<Order> getOrdersByDateForReport(String date) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " +
                "p.patient_first_name || ' ' || p.patient_last_name as name, " +
                "p.wing, p.room_number, p.diet as diet_name, " +
                "mo.timestamp, p.fluid_restriction, p.texture_modifications " +
                "FROM patient_info p " +
                "INNER JOIN meal_orders mo ON p.patient_id = mo.patient_id " +
                "WHERE date(mo.order_date) = ? " +
                "ORDER BY p.wing, CAST(p.room_number AS INTEGER)";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        while (cursor.moveToNext()) {
            Order order = new Order();
            order.setOrderId(0); // Not needed for report
            order.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            order.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
            order.setRoom(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));
            order.setDiet(cursor.getString(cursor.getColumnIndexOrThrow("diet_name")));
            order.setOrderTime(cursor.getString(cursor.getColumnIndexOrThrow("timestamp")));
            order.setFluidRestriction(cursor.getString(cursor.getColumnIndex("fluid_restriction")));
            order.setTextureModifications(cursor.getString(cursor.getColumnIndex("texture_modifications")));
            order.setOrderDate(date);

            orders.add(order);
        }
        cursor.close();

        return orders;
    }
}