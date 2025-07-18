package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Patient;
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
     * FIXED: Get patients who have incomplete meal orders (for pending orders)
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
     * FIXED: Get patients who need orders (all patients, as they all need daily orders)
     */
    public List<Patient> getPatientsNeedingOrders() {
        // For now, return all patients since they all need daily meal orders
        // In a more complex system, you might filter by admission date, discharge status, etc.
        return getAllPatients();
    }

    /**
     * FIXED: Check if room is occupied
     */
    public boolean isRoomOccupied(String wing, String roomNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo WHERE wing = ? AND room_number = ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{wing, roomNumber});
            
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error checking room occupancy: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return false;
    }

    /**
     * Get patients by wing
     */
    public List<Patient> getPatientsByWing(String wing) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE wing = ? ORDER BY room_number";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{wing});
            
            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting patients by wing: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return patients;
    }

    /**
     * Get patients by diet type
     */
    public List<Patient> getPatientsByDiet(String diet) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE diet = ? ORDER BY wing, room_number";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{diet});
            
            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting patients by diet: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return patients;
    }

    /**
     * Search patients by name
     */
    public List<Patient> searchPatientsByName(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM PatientInfo " +
                      "WHERE patient_first_name LIKE ? OR patient_last_name LIKE ? " +
                      "ORDER BY patient_last_name, patient_first_name";
        
        String searchPattern = "%" + searchTerm + "%";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern});
            
            if (cursor.moveToFirst()) {
                do {
                    patients.add(createPatientFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error searching patients by name: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return patients;
    }

    /**
     * Update meal completion status
     */
    public boolean updateMealCompletionStatus(int patientId, String meal, boolean completed) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        String columnName;
        switch (meal.toLowerCase()) {
            case "breakfast":
                columnName = "breakfast_complete";
                break;
            case "lunch":
                columnName = "lunch_complete";
                break;
            case "dinner":
                columnName = "dinner_complete";
                break;
            default:
                Log.e("PatientDAO", "Invalid meal type: " + meal);
                return false;
        }
        
        values.put(columnName, completed ? 1 : 0);
        
        try {
            int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                       new String[]{String.valueOf(patientId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("PatientDAO", "Error updating meal completion status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update NPO status
     */
    public boolean updateNPOStatus(int patientId, String meal, boolean npo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        String columnName;
        switch (meal.toLowerCase()) {
            case "breakfast":
                columnName = "breakfast_npo";
                break;
            case "lunch":
                columnName = "lunch_npo";
                break;
            case "dinner":
                columnName = "dinner_npo";
                break;
            default:
                Log.e("PatientDAO", "Invalid meal type: " + meal);
                return false;
        }
        
        values.put(columnName, npo ? 1 : 0);
        
        try {
            int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                       new String[]{String.valueOf(patientId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("PatientDAO", "Error updating NPO status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reset all meal completion statuses (for new day)
     */
    public boolean resetMealCompletionStatuses() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("breakfast_complete", 0);
        values.put("lunch_complete", 0);
        values.put("dinner_complete", 0);
        values.put("breakfast_npo", 0);
        values.put("lunch_npo", 0);
        values.put("dinner_npo", 0);
        
        try {
            int rowsAffected = db.update("PatientInfo", values, null, null);
            Log.d("PatientDAO", "Reset meal completion for " + rowsAffected + " patients");
            return true;
        } catch (Exception e) {
            Log.e("PatientDAO", "Error resetting meal completion statuses: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get patient count
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
     * Get pending orders count
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
            Log.e("PatientDAO", "Error getting pending orders count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return 0;
    }

    /**
     * FIXED: Get patients with completed meal orders (for finished orders)
     */
    public List<Patient> getCompletedPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Get patients who have completed all meals
        String query = "SELECT * FROM PatientInfo " +
                      "WHERE breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
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
        
        // Meal completion status
        patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_complete")) == 1);
        patient.setLunchComplete(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_complete")) == 1);
        patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_complete")) == 1);
        
        // NPO status
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_npo")) == 1);
        
        // Created date
        String createdDateStr = cursor.getString(cursor.getColumnIndexOrThrow("created_date"));
        try {
            patient.setCreatedDate(dateFormat.parse(createdDateStr));
        } catch (Exception e) {
            Log.w("PatientDAO", "Error parsing created date: " + e.getMessage());
            patient.setCreatedDate(new Date());
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