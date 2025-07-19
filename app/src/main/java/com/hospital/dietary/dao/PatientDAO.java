package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Patient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientDAO {

    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public PatientDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Add a new patient
     */
    public long addPatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patient_first_name", patient.getPatientFirstName());
        values.put("patient_last_name", patient.getPatientLastName());
        values.put("wing", patient.getWing());
        values.put("room_number", patient.getRoomNumber());
        values.put("diet", patient.getDiet());
        values.put("fluid_restriction", patient.getFluidRestriction());
        values.put("texture_modifications", patient.getTextureModifications());
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);
        values.put("created_date", getCurrentTimestamp());

        try {
            return db.insert("PatientInfo", null, values);
        } catch (Exception e) {
            Log.e("PatientDAO", "Error adding patient: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Update an existing patient
     */
    public boolean updatePatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patient_first_name", patient.getPatientFirstName());
        values.put("patient_last_name", patient.getPatientLastName());
        values.put("wing", patient.getWing());
        values.put("room_number", patient.getRoomNumber());
        values.put("diet", patient.getDiet());
        values.put("fluid_restriction", patient.getFluidRestriction());
        values.put("texture_modifications", patient.getTextureModifications());
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);

        try {
            int rowsAffected = db.update("PatientInfo", values, "patient_id = ?",
                    new String[]{String.valueOf(patient.getPatientId())});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("PatientDAO", "Error updating patient: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a patient
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rowsAffected = db.delete("PatientInfo", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("PatientDAO", "Error deleting patient: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get patient by ID
     */
    public Patient getPatientById(int patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE patient_id = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

            if (cursor.moveToFirst()) {
                return createPatientFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting patient by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting all patients: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * Get patients who have incomplete meal orders (for pending orders)
     */
    public List<Patient> getPendingPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get patients who have at least one incomplete meal
        String query = "SELECT * FROM PatientInfo " +
                "WHERE (breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting pending patients: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * Get patients with completed meal orders for a specific date
     */
    public List<Patient> getCompletedOrdersByDate(String date) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get patients who have completed all meals for specific date
        String query = "SELECT * FROM PatientInfo " +
                "WHERE DATE(created_date) = ? " +
                "AND breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting completed orders by date: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * Get orders before a specific date (for archiving)
     */
    public List<Patient> getOrdersBeforeDate(String cutoffDate) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo " +
                "WHERE DATE(created_date) < ? " +
                "AND breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "ORDER BY created_date DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{cutoffDate});

            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting orders before date: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * Archive old orders by moving them to retired status
     */
    public int archiveOldOrders(String cutoffDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // For now, we'll add a 'retired' flag to mark archived orders
            // In a full implementation, you might move these to a separate RetiredOrders table

            // First, let's add the retired column if it doesn't exist
            try {
                db.execSQL("ALTER TABLE PatientInfo ADD COLUMN retired INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Column likely already exists, ignore
            }

            // Mark old completed orders as retired
            ContentValues values = new ContentValues();
            values.put("retired", 1);

            int rowsAffected = db.update("PatientInfo", values,
                    "DATE(created_date) < ? AND breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 AND (retired IS NULL OR retired = 0)",
                    new String[]{cutoffDate});

            return rowsAffected;

        } catch (Exception e) {
            Log.e("PatientDAO", "Error archiving old orders: " + e.getMessage());
            return 0;
        }
    }

    /**
     * FIXED: Get patients who need orders (only those with incomplete meals)
     */
    public List<Patient> getPatientsNeedingOrders() {
        // Return patients with incomplete meals instead of all patients
        return getPendingPatients();
    }

    /**
     * Get total patient count
     */
    public int getPatientCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo WHERE (retired IS NULL OR retired = 0)";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting patient count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Get pending orders count
     */
    public int getPendingOrdersCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo " +
                "WHERE (breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) " +
                "AND (retired IS NULL OR retired = 0)";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting pending orders count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Get patients with completed meal orders (for finished orders)
     */
    public List<Patient> getCompletedPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get patients who have completed all meals and are not retired
        String query = "SELECT * FROM PatientInfo " +
                "WHERE breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "AND (retired IS NULL OR retired = 0) " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting completed patients: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * NEW: Get patient by room for today's date (for duplicate validation)
     */
    public Patient getPatientByRoomToday(String wing, String roomNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SimpleDateFormat todayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = todayFormat.format(new Date());

        String query = "SELECT * FROM PatientInfo WHERE wing = ? AND room_number = ? AND DATE(created_date) = ? AND (retired IS NULL OR retired = 0)";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{wing, roomNumber, today});

            if (cursor.moveToFirst()) {
                return createPatientFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error checking room availability: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null; // No patient found in this room today
    }

    /**
     * NEW: Get orders by specific date (for retired orders)
     */
    public List<Patient> getOrdersByDate(String date) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get retired patients for specific date
        String query = "SELECT * FROM PatientInfo " +
                "WHERE DATE(created_date) = ? " +
                "AND retired = 1 " +
                "AND (breakfast_complete = 1 OR breakfast_npo = 1) " +
                "AND (lunch_complete = 1 OR lunch_npo = 1) " +
                "AND (dinner_complete = 1 OR dinner_npo = 1) " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting orders by date: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * NEW: Get orders for date range (useful for reports)
     */
    public List<Patient> getOrdersByDateRange(String startDate, String endDate) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo " +
                "WHERE DATE(created_date) BETWEEN ? AND ? " +
                "AND (breakfast_complete = 1 OR breakfast_npo = 1) " +
                "AND (lunch_complete = 1 OR lunch_npo = 1) " +
                "AND (dinner_complete = 1 OR dinner_npo = 1) " +
                "ORDER BY created_date DESC, wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{startDate, endDate});

            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting orders by date range: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * NEW: Get available order dates (for calendar picker)
     */
    public List<String> getAvailableOrderDates() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT DATE(created_date) as order_date " +
                "FROM PatientInfo " +
                "WHERE retired = 1 " +
                "AND (breakfast_complete = 1 OR breakfast_npo = 1) " +
                "AND (lunch_complete = 1 OR lunch_npo = 1) " +
                "AND (dinner_complete = 1 OR dinner_npo = 1) " +
                "ORDER BY order_date DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    dates.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting available order dates: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return dates;
    }

    /**
     * NEW: Get order count by date (for statistics)
     */
    public int getOrderCountByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo " +
                "WHERE DATE(created_date) = ? " +
                "AND (breakfast_complete = 1 OR breakfast_npo = 1) " +
                "AND (lunch_complete = 1 OR lunch_npo = 1) " +
                "AND (dinner_complete = 1 OR dinner_npo = 1)";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting order count by date: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Helper method to create Patient object from cursor
     */
    private Patient createPatientFromCursor(Cursor cursor) {
        Patient patient = new Patient();

        patient.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
        patient.setPatientFirstName(cursor.getString(cursor.getColumnIndexOrThrow("patient_first_name")));
        patient.setPatientLastName(cursor.getString(cursor.getColumnIndexOrThrow("patient_last_name")));
        patient.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
        patient.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));
        patient.setDiet(cursor.getString(cursor.getColumnIndexOrThrow("diet")));

        // Handle optional fields
        int fluidIndex = cursor.getColumnIndex("fluid_restriction");
        if (fluidIndex >= 0 && !cursor.isNull(fluidIndex)) {
            patient.setFluidRestriction(cursor.getString(fluidIndex));
        }

        int textureIndex = cursor.getColumnIndex("texture_modifications");
        if (textureIndex >= 0 && !cursor.isNull(textureIndex)) {
            patient.setTextureModifications(cursor.getString(textureIndex));
        }

        int createdDateIndex = cursor.getColumnIndex("created_date");
        if (createdDateIndex >= 0 && !cursor.isNull(createdDateIndex)) {
            try {
                String dateString = cursor.getString(createdDateIndex);
                Date createdDate = dateFormat.parse(dateString);
                patient.setCreatedDate(createdDate);
            } catch (Exception e) {
                Log.e("PatientDAO", "Error parsing created date: " + e.getMessage());
                // Set to current date if parsing fails
                patient.setCreatedDate(new Date());
            }
        }

        // Boolean fields
        patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_complete")) == 1);
        patient.setLunchComplete(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_complete")) == 1);
        patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_complete")) == 1);
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_npo")) == 1);

        return patient;
    }

    /**
     * Helper method to get current timestamp
     */
    private String getCurrentTimestamp() {
        return dateFormat.format(new Date());
    }
}