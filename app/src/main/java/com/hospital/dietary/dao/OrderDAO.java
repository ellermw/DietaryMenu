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

/**
 * OrderDAO class for managing dietary orders
 */
public class OrderDAO {

    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat;

    public OrderDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    /**
     * Insert a new order
     */
    public long insertOrder(Order order) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patient_id", order.getPatientId());
        values.put("patient_name", order.getPatientName());
        values.put("wing", order.getWing());
        values.put("room_number", order.getRoom());
        values.put("diet_name", order.getDiet());
        values.put("meal_type", order.getMealType());
        values.put("order_date", order.getOrderDate());
        values.put("timestamp", order.getOrderTime());
        values.put("fluid_restriction", order.getFluidRestriction());
        values.put("texture_modifications", order.getTextureModifications());
        values.put("status", order.getStatus());
        values.put("items", order.getItems());
        values.put("special_instructions", order.getSpecialInstructions());

        return db.insert("meal_orders", null, values);
    }

    /**
     * Update an existing order
     */
    public int updateOrder(Order order) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patient_name", order.getPatientName());
        values.put("wing", order.getWing());
        values.put("room_number", order.getRoom());
        values.put("diet_name", order.getDiet());
        values.put("meal_type", order.getMealType());
        values.put("fluid_restriction", order.getFluidRestriction());
        values.put("texture_modifications", order.getTextureModifications());
        values.put("status", order.getStatus());
        values.put("items", order.getItems());
        values.put("special_instructions", order.getSpecialInstructions());

        return db.update("meal_orders", values, "order_id = ?",
                new String[]{String.valueOf(order.getOrderId())});
    }

    /**
     * Delete an order
     */
    public int deleteOrder(int orderId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("meal_orders", "order_id = ?",
                new String[]{String.valueOf(orderId)});
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(int orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("meal_orders", null, "order_id = ?",
                new String[]{String.valueOf(orderId)}, null, null, null);

        Order order = null;
        if (cursor != null && cursor.moveToFirst()) {
            order = cursorToOrder(cursor);
            cursor.close();
        }

        return order;
    }

    /**
     * Get all orders
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM meal_orders ORDER BY order_date DESC, timestamp DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return orders;
    }

    /**
     * Get orders by date
     */
    public List<Order> getOrdersByDate(String date) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM meal_orders WHERE date(order_date) = ? " +
                "ORDER BY wing, CAST(room_number AS INTEGER)";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return orders;
    }

    /**
     * Get orders by patient
     */
    public List<Order> getOrdersByPatient(long patientId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM meal_orders WHERE patient_id = ? " +
                "ORDER BY order_date DESC, timestamp DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return orders;
    }

    /**
     * Get pending orders
     */
    public List<Order> getPendingOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM meal_orders WHERE status = 'Pending' " +
                "ORDER BY order_date DESC, timestamp DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return orders;
    }

    /**
     * Get completed orders
     */
    public List<Order> getCompletedOrders() {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM meal_orders WHERE status = 'Completed' " +
                "ORDER BY order_date DESC, timestamp DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                orders.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return orders;
    }

    /**
     * Update order status
     */
    public int updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);

        if ("Completed".equals(status)) {
            values.put("completed_date", dateFormat.format(new Date()));
        }

        return db.update("meal_orders", values, "order_id = ?",
                new String[]{String.valueOf(orderId)});
    }

    /**
     * Get orders for daily report
     */
    public List<Order> getOrdersForDailyReport(String date) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT o.*, p.patient_first_name || ' ' || p.patient_last_name as name " +
                "FROM meal_orders o " +
                "JOIN patient_info p ON o.patient_id = p.patient_id " +
                "WHERE date(o.order_date) = ? " +
                "ORDER BY p.wing, CAST(p.room_number AS INTEGER)";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor != null && cursor.moveToFirst()) {
            do {
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
            } while (cursor.moveToNext());
            cursor.close();
        }

        return orders;
    }

    /**
     * Get PatientOrders for display
     */
    public List<PatientOrder> getPatientOrdersByDate(String date) {
        List<PatientOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT p.patient_id, " +
                "p.patient_first_name || ' ' || p.patient_last_name as name, " +
                "p.room_number, p.wing, p.diet, " +
                "MAX(mo.timestamp) as timestamp, " +
                "p.fluid_restriction, p.texture_modifications " +
                "FROM patient_info p " +
                "LEFT JOIN meal_orders mo ON p.patient_id = mo.patient_id " +
                "AND date(mo.order_date) = ? " +
                "GROUP BY p.patient_id " +
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

    /**
     * Load meal items for a patient order
     */
    private void loadMealItems(PatientOrder order, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get meal items
        String query = "SELECT meal, GROUP_CONCAT(item_name, ', ') as items " +
                "FROM (SELECT mo.meal, i.name as item_name " +
                "FROM meal_orders mo " +
                "JOIN order_items oi ON mo.order_id = oi.order_id " +
                "JOIN items i ON oi.item_id = i.item_id " +
                "WHERE mo.patient_id = ? AND date(mo.order_date) = ? " +
                "ORDER BY i.name) " +
                "GROUP BY meal";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(order.getPatientId()), date});

        while (cursor.moveToNext()) {
            String meal = cursor.getString(0);
            String items = cursor.getString(1);

            if ("Breakfast".equals(meal)) {
                order.setBreakfastItems(items);
            } else if ("Lunch".equals(meal)) {
                order.setLunchItems(items);
            } else if ("Dinner".equals(meal)) {
                order.setDinnerItems(items);
            }
        }
        cursor.close();
    }

    /**
     * Convert cursor to Order object
     */
    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();

        order.setOrderId(cursor.getInt(cursor.getColumnIndex("order_id")));
        order.setPatientId(cursor.getLong(cursor.getColumnIndex("patient_id")));
        order.setPatientName(cursor.getString(cursor.getColumnIndex("patient_name")));
        order.setWing(cursor.getString(cursor.getColumnIndex("wing")));
        order.setRoom(cursor.getString(cursor.getColumnIndex("room_number")));
        order.setDiet(cursor.getString(cursor.getColumnIndex("diet_name")));
        order.setMealType(cursor.getString(cursor.getColumnIndex("meal_type")));
        order.setOrderDate(cursor.getString(cursor.getColumnIndex("order_date")));
        order.setOrderTime(cursor.getString(cursor.getColumnIndex("timestamp")));
        order.setFluidRestriction(cursor.getString(cursor.getColumnIndex("fluid_restriction")));
        order.setTextureModifications(cursor.getString(cursor.getColumnIndex("texture_modifications")));
        order.setStatus(cursor.getString(cursor.getColumnIndex("status")));
        order.setItems(cursor.getString(cursor.getColumnIndex("items")));
        order.setSpecialInstructions(cursor.getString(cursor.getColumnIndex("special_instructions")));

        return order;
    }
}