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
     * FIXED: Enhanced update patient with full meal selection support
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

            // FIXED: Also update meal selections if patient has them
            if (rowsAffected > 0) {
                updatePatientMealSelections(patient);
            }

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("PatientDAO", "Error updating patient: " + e.getMessage());
            return false;
        }
    }

    /**
     * FIXED: New method to update meal selections for existing patients
     */
    private void updatePatientMealSelections(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Clear existing meal selections for this patient
            db.delete("PatientMealSelection", "patient_id = ?",
                    new String[]{String.valueOf(patient.getPatientId())});

            // Add new meal selections if patient has any
            if (patient.getMealSelections() != null) {
                for (String selection : patient.getMealSelections()) {
                    ContentValues values = new ContentValues();
                    values.put("patient_id", patient.getPatientId());
                    values.put("meal_selection", selection);
                    values.put("created_date", getCurrentTimestamp());

                    db.insert("PatientMealSelection", null, values);
                }
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error updating meal selections: " + e.getMessage());
        }
    }

    /**
     * FIXED: Enhanced delete patient with cascade delete of related data
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Start transaction for cascade delete
        db.beginTransaction();

        try {
            // Delete related meal selections first
            db.delete("PatientMealSelection", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});

            // Delete any order history
            db.delete("OrderHistory", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});

            // Finally delete the patient
            int rowsAffected = db.delete("PatientInfo", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e("PatientDAO", "Error deleting patient: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
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
                Patient patient = createPatientFromCursor(cursor);

                // FIXED: Load meal selections for this patient
                loadPatientMealSelections(patient);

                return patient;
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
     * FIXED: Load meal selections for a patient
     */
    private void loadPatientMealSelections(Patient patient) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT meal_selection FROM PatientMealSelection WHERE patient_id = ?";

        Cursor cursor = null;
        List<String> selections = new ArrayList<>();

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(patient.getPatientId())});

            while (cursor.moveToNext()) {
                selections.add(cursor.getString(0));
            }

            patient.setMealSelections(selections);

        } catch (Exception e) {
            Log.e("PatientDAO", "Error loading meal selections: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
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
     * FIXED: Get total patient count for dashboard
     */
    public int getPatientCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo";

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
     * FIXED: Get pending orders count for dashboard
     */
    public int getPendingOrdersCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo " +
                "WHERE (breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0)";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting pending count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
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
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
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

        String query = "SELECT * FROM PatientInfo " +
                "WHERE DATE(created_date) = ? " +
                "AND breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
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
     * FIXED: Get completed patients (alias for getCompletedOrdersByDate with today's date)
     */
    public List<Patient> getCompletedPatients() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());
        return getCompletedOrdersByDate(today);
    }

    /**
     * FIXED: Get orders by date (alias for getCompletedOrdersByDate)
     */
    public List<Patient> getOrdersByDate(String date) {
        return getCompletedOrdersByDate(date);
    }

    /**
     * FIXED: Search patients by name, wing, or room
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo " +
                "WHERE LOWER(patient_first_name) LIKE ? " +
                "OR LOWER(patient_last_name) LIKE ? " +
                "OR LOWER(wing) LIKE ? " +
                "OR room_number LIKE ? " +
                "ORDER BY wing, room_number";

        String searchPattern = "%" + searchTerm.toLowerCase() + "%";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern, searchPattern, searchPattern});

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error searching patients: " + e.getMessage());
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
            // Add the retired column if it doesn't exist
            try {
                db.execSQL("ALTER TABLE PatientInfo ADD COLUMN retired INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Column likely already exists, ignore
            }

            // Mark old completed orders as retired
            ContentValues values = new ContentValues();
            values.put("retired", 1);

            int rowsAffected = db.update("PatientInfo", values,
                    "DATE(created_date) < ? AND breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1",
                    new String[]{cutoffDate});

            return rowsAffected;
        } catch (Exception e) {
            Log.e("PatientDAO", "Error archiving old orders: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Helper method to create patient from cursor
     */
    private Patient createPatientFromCursor(Cursor cursor) {
        Patient patient = new Patient();

        patient.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
        patient.setPatientFirstName(cursor.getString(cursor.getColumnIndexOrThrow("patient_first_name")));
        patient.setPatientLastName(cursor.getString(cursor.getColumnIndexOrThrow("patient_last_name")));
        patient.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
        patient.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));
        patient.setDiet(cursor.getString(cursor.getColumnIndexOrThrow("diet")));
        patient.setFluidRestriction(cursor.getString(cursor.getColumnIndexOrThrow("fluid_restriction")));
        patient.setTextureModifications(cursor.getString(cursor.getColumnIndexOrThrow("texture_modifications")));
        patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_complete")) == 1);
        patient.setLunchComplete(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_complete")) == 1);
        patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_complete")) == 1);
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_npo")) == 1);

        String createdDate = cursor.getString(cursor.getColumnIndexOrThrow("created_date"));
        if (createdDate != null) {
            try {
                patient.setCreatedDate(dateFormat.parse(createdDate));
            } catch (ParseException e) {
                patient.setCreatedDate(new Date());
            }
        }

        return patient;
    }

    /**
     * Get current timestamp
     */
    private String getCurrentTimestamp() {
        return dateFormat.format(new Date());
    }
}