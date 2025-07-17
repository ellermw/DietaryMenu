// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/dao/OrderDAO.java
// ================================================================================================

package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.PatientOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDAO {
    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public OrderDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Get all finalized orders for a specific date
     */
    public List<PatientOrder> getOrdersForDate(String date) {
        List<PatientOrder> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT " +
                "p.patient_id, p.name, p.room_number, p.wing, d.name as diet_name, " +
                "mo.timestamp, p.fluid_restriction, p.texture_modifications " +
                "FROM Patient p " +
                "JOIN MealOrder mo ON p.patient_id = mo.patient_id " +
                "JOIN Diet d ON p.diet_id = d.diet_id " +
                "WHERE DATE(mo.timestamp) = ? " +
                "ORDER BY p.wing, CAST(p.room_number AS INTEGER) DESC";

        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            int idxPatientId = cursor.getColumnIndexOrThrow("patient_id");
            int idxName = cursor.getColumnIndexOrThrow("name");
            int idxRoom = cursor.getColumnIndexOrThrow("room_number");
            int idxWing = cursor.getColumnIndexOrThrow("wing");
            int idxDiet = cursor.getColumnIndexOrThrow("diet_name");
            int idxTimestamp = cursor.getColumnIndexOrThrow("timestamp");
            int idxFluidRestriction = cursor.getColumnIndex("fluid_restriction");
            int idxTextureModifications = cursor.getColumnIndex("texture_modifications");

            do {
                PatientOrder order = new PatientOrder();
                order.setPatientId(cursor.getInt(idxPatientId));
                order.setPatientName(cursor.getString(idxName));
                order.setRoom(cursor.getString(idxRoom));
                order.setWing(cursor.getString(idxWing));
                order.setDiet(cursor.getString(idxDiet));
                order.setTimestamp(cursor.getString(idxTimestamp));

                if (!cursor.isNull(idxFluidRestriction)) {
                    order.setFluidRestriction(cursor.getString(idxFluidRestriction));
                }

                if (!cursor.isNull(idxTextureModifications)) {
                    order.setTextureModifications(cursor.getString(idxTextureModifications));
                }

                // Get meal items for this patient
                loadMealItemsForPatient(order, date);
                orders.add(order);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return orders;
    }

    /**
     * Load meal items for a specific patient and date
     */
    private void loadMealItemsForPatient(PatientOrder order, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get all meals for this patient on this date
        String mealQuery = "SELECT mo.meal, i.name as item_name " +
                "FROM MealOrder mo " +
                "JOIN MealLine ml ON mo.order_id = ml.order_id " +
                "JOIN Item i ON ml.item_id = i.item_id " +
                "WHERE mo.patient_id = ? AND DATE(mo.timestamp) = ? " +
                "ORDER BY mo.meal, i.name";

        Cursor mealCursor = db.rawQuery(mealQuery, new String[]{
                String.valueOf(order.getPatientId()), date});

        StringBuilder breakfast = new StringBuilder();
        StringBuilder lunch = new StringBuilder();
        StringBuilder dinner = new StringBuilder();

        if (mealCursor.moveToFirst()) {
            int idxMeal = mealCursor.getColumnIndexOrThrow("meal");
            int idxItemName = mealCursor.getColumnIndexOrThrow("item_name");

            do {
                String meal = mealCursor.getString(idxMeal);
                String itemName = mealCursor.getString(idxItemName);

                switch (meal.toLowerCase()) {
                    case "breakfast":
                        if (breakfast.length() > 0) breakfast.append("\n");
                        breakfast.append(itemName);
                        break;
                    case "lunch":
                        if (lunch.length() > 0) lunch.append("\n");
                        lunch.append(itemName);
                        break;
                    case "dinner":
                        if (dinner.length() > 0) dinner.append("\n");
                        dinner.append(itemName);
                        break;
                }

            } while (mealCursor.moveToNext());
        }

        mealCursor.close();

        order.setBreakfastItems(breakfast.toString());
        order.setLunchItems(lunch.toString());
        order.setDinnerItems(dinner.toString());
    }

    /**
     * Save a complete patient order with all three meals
     */
    public long savePatientOrder(String patientName, String wing, String room, String diet,
                                String fluidRestriction, String textureModifications,
                                List<String> breakfastItems, List<String> lunchItems, 
                                List<String> dinnerItems) {
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        
        try {
            // 1. Save or get patient
            long patientId = saveOrGetPatient(patientName, wing, room, diet, 
                                            fluidRestriction, textureModifications);
            
            if (patientId == -1) {
                db.endTransaction();
                return -1;
            }
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            
            // 2. Save meal orders
            long breakfastOrderId = saveMealOrder(patientId, "breakfast", timestamp);
            long lunchOrderId = saveMealOrder(patientId, "lunch", timestamp);
            long dinnerOrderId = saveMealOrder(patientId, "dinner", timestamp);
            
            // 3. Save meal items
            saveMealItems(breakfastOrderId, breakfastItems);
            saveMealItems(lunchOrderId, lunchItems);
            saveMealItems(dinnerOrderId, dinnerItems);
            
            db.setTransactionSuccessful();
            return patientId;
            
        } finally {
            db.endTransaction();
        }
    }

    private long saveOrGetPatient(String name, String wing, String room, String diet,
                                 String fluidRestriction, String textureModifications) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // First get diet ID
        long dietId = getDietId(diet);
        if (dietId == -1) {
            dietId = createDiet(diet);
        }
        
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("wing", wing);
        values.put("room_number", room);
        values.put("diet_id", dietId);
        
        if (fluidRestriction != null && !fluidRestriction.isEmpty()) {
            values.put("fluid_restriction", fluidRestriction);
        }
        
        if (textureModifications != null && !textureModifications.isEmpty()) {
            values.put("texture_modifications", textureModifications);
        }
        
        return db.insert("Patient", null, values);
    }

    private long getDietId(String dietName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT diet_id FROM Diet WHERE name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{dietName});
        
        long dietId = -1;
        if (cursor.moveToFirst()) {
            dietId = cursor.getLong(0);
        }
        cursor.close();
        return dietId;
    }

    private long createDiet(String dietName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", dietName);
        return db.insert("Diet", null, values);
    }

    private long saveMealOrder(long patientId, String meal, String timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("patient_id", patientId);
        values.put("meal", meal);
        values.put("guest_tray", 0);
        values.put("timestamp", timestamp);
        
        return db.insert("MealOrder", null, values);
    }

    private void saveMealItems(long orderId, List<String> items) {
        if (items == null || items.isEmpty()) return;
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        for (String itemName : items) {
            if (itemName == null || itemName.trim().isEmpty()) continue;
            
            // Get item ID
            long itemId = getItemIdByName(itemName.trim());
            if (itemId != -1) {
                ContentValues values = new ContentValues();
                values.put("order_id", orderId);
                values.put("item_id", itemId);
                db.insert("MealLine", null, values);
            }
        }
    }

    private long getItemIdByName(String itemName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT item_id FROM Item WHERE name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{itemName});
        
        long itemId = -1;
        if (cursor.moveToFirst()) {
            itemId = cursor.getLong(0);
        }
        cursor.close();
        return itemId;
    }

    /**
     * Get order count for a specific date
     */
    public int getOrderCountForDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(DISTINCT mo.patient_id) " +
                "FROM MealOrder mo " +
                "WHERE DATE(mo.timestamp) = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{date});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}