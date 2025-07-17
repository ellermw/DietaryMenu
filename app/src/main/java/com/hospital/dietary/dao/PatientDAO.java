package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
     * Add a new patient to the database
     * FIXED: Ensure this method works for new patients appearing in pending orders
     */
    public long addPatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", patient.getName());
        values.put("wing", patient.getWing());
        values.put("room_number", patient.getRoomNumber());
        values.put("diet", patient.getDiet());
        values.put("fluid_restriction", patient.getFluidRestriction());
        values.put("texture_modifications", patient.getTextureModifications());
        
        // FIXED: Explicitly set meal completion statuses so new patients appear in pending orders
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);
        values.put("created_date", dateFormat.format(new Date()));

        long patientId = db.insert("PatientInfo", null, values);
        if (patientId > 0) {
            patient.setPatientId((int) patientId);
        }
        return patientId;
    }

    /**
     * Alternative method name for consistency
     */
    public long insertPatient(Patient patient) {
        return addPatient(patient);
    }

    /**
     * Update an existing patient
     */
    public boolean updatePatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", patient.getName());
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

        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                   new String[]{String.valueOf(patient.getPatientId())});
        return rowsAffected > 0;
    }

    /**
     * Get a patient by ID
     */
    public Patient getPatientById(int patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE patient_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

        Patient patient = null;
        if (cursor.moveToFirst()) {
            patient = createPatientFromCursor(cursor);
        }

        cursor.close();
        return patient;
    }

    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo ORDER BY wing, CAST(room_number AS INTEGER)";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Patient patient = createPatientFromCursor(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return patients;
    }

    /**
     * Get patients with incomplete meals (for pending orders)
     * FIXED: This query now correctly identifies patients with incomplete meals
     */
    public List<Patient> getPendingPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM PatientInfo WHERE " +
                      "(breakfast_complete = 0 AND breakfast_npo = 0) OR " +
                      "(lunch_complete = 0 AND lunch_npo = 0) OR " +
                      "(dinner_complete = 0 AND dinner_npo = 0) " +
                      "ORDER BY wing, CAST(room_number AS INTEGER)";
        
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Patient patient = createPatientFromCursor(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return patients;
    }

    /**
     * Get patients with complete meals (for finished orders)
     */
    public List<Patient> getCompletedPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM PatientInfo WHERE " +
                      "(breakfast_complete = 1 OR breakfast_npo = 1) AND " +
                      "(lunch_complete = 1 OR lunch_npo = 1) AND " +
                      "(dinner_complete = 1 OR dinner_npo = 1) " +
                      "ORDER BY wing, CAST(room_number AS INTEGER) DESC";
        
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Patient patient = createPatientFromCursor(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return patients;
    }

    /**
     * Update meal completion status
     */
    public boolean updateMealStatus(int patientId, String meal, boolean completed, boolean npo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        switch (meal.toLowerCase()) {
            case "breakfast":
                values.put("breakfast_complete", completed ? 1 : 0);
                values.put("breakfast_npo", npo ? 1 : 0);
                break;
            case "lunch":
                values.put("lunch_complete", completed ? 1 : 0);
                values.put("lunch_npo", npo ? 1 : 0);
                break;
            case "dinner":
                values.put("dinner_complete", completed ? 1 : 0);
                values.put("dinner_npo", npo ? 1 : 0);
                break;
            default:
                return false;
        }

        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                   new String[]{String.valueOf(patientId)});
        return rowsAffected > 0;
    }

    /**
     * Delete a patient
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete("PatientInfo", "patient_id = ?", 
                                   new String[]{String.valueOf(patientId)});
        return rowsDeleted > 0;
    }

    /**
     * Search patients by name or room
     */
    public List<Patient> searchPatients(String searchQuery) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM PatientInfo WHERE " +
                      "name LIKE ? OR room_number LIKE ? OR wing LIKE ? " +
                      "ORDER BY wing, CAST(room_number AS INTEGER)";
        
        String searchPattern = "%" + searchQuery + "%";
        Cursor cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern, searchPattern});

        if (cursor.moveToFirst()) {
            do {
                Patient patient = createPatientFromCursor(cursor);
                patients.add(patient);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return patients;
    }

    /**
     * Helper method to create Patient object from cursor
     */
    private Patient createPatientFromCursor(Cursor cursor) {
        Patient patient = new Patient();
        
        patient.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
        patient.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        patient.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
        patient.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));
        patient.setDiet(cursor.getString(cursor.getColumnIndexOrThrow("diet")));
        
        // Handle nullable fields
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
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_npo")) == 1);
        
        return patient;
    }

    /**
     * Get count of incomplete meals for a patient
     */
    public int getIncompleteOrderCount(int patientId) {
        Patient patient = getPatientById(patientId);
        if (patient == null) return 0;
        
        int count = 0;
        if (!patient.isBreakfastComplete() && !patient.isBreakfastNPO()) count++;
        if (!patient.isLunchComplete() && !patient.isLunchNPO()) count++;
        if (!patient.isDinnerComplete() && !patient.isDinnerNPO()) count++;
        
        return count;
    }
}