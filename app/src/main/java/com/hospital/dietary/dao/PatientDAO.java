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

    public PatientDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * FIXED: Updated to handle split patient names
     */
    public long addPatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // FIXED: Use split first and last names
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

        return db.insert("PatientInfo", null, values);
    }

    /**
     * FIXED: Updated to handle split patient names
     */
    public boolean updatePatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // FIXED: Use split first and last names
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

        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                   new String[]{String.valueOf(patient.getPatientId())});
        return rowsAffected > 0;
    }

    /**
     * FIXED: Updated to handle split patient names
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
     * FIXED: Updated to handle split patient names
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
     * FIXED: Search by last name
     */
    public List<Patient> searchPatientsByLastName(String lastName) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE patient_last_name LIKE ? ORDER BY patient_last_name, patient_first_name";
        Cursor cursor = db.rawQuery(query, new String[]{"%" + lastName + "%"});

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
     * FIXED: Search by first name
     */
    public List<Patient> searchPatientsByFirstName(String firstName) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE patient_first_name LIKE ? ORDER BY patient_last_name, patient_first_name";
        Cursor cursor = db.rawQuery(query, new String[]{"%" + firstName + "%"});

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
     * FIXED: Search by full name (first + last)
     */
    public List<Patient> searchPatientsByFullName(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE " +
                      "(patient_first_name LIKE ? OR patient_last_name LIKE ? OR " +
                      "(patient_first_name || ' ' || patient_last_name) LIKE ?) " +
                      "ORDER BY patient_last_name, patient_first_name";
        String searchPattern = "%" + searchTerm + "%";
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
     * Get patients with incomplete meals (for pending orders)
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
                      "breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
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
     * Get patients by wing
     */
    public List<Patient> getPatientsByWing(String wing) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE wing = ? ORDER BY CAST(room_number AS INTEGER)";
        Cursor cursor = db.rawQuery(query, new String[]{wing});

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
     * Delete patient
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("PatientInfo", "patient_id = ?", new String[]{String.valueOf(patientId)}) > 0;
    }

    /**
     * Mark meal as complete
     */
    public boolean markMealComplete(int patientId, String meal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        String columnName = meal.toLowerCase() + "_complete";
        values.put(columnName, 1);
        
        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                   new String[]{String.valueOf(patientId)});
        return rowsAffected > 0;
    }

    /**
     * Mark meal as NPO (Nothing by Mouth)
     */
    public boolean markMealNPO(int patientId, String meal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        String columnName = meal.toLowerCase() + "_npo";
        values.put(columnName, 1);
        
        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                   new String[]{String.valueOf(patientId)});
        return rowsAffected > 0;
    }

    /**
     * Reset all meals for a patient (for new day)
     */
    public boolean resetPatientMeals(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("breakfast_complete", 0);
        values.put("lunch_complete", 0);
        values.put("dinner_complete", 0);
        values.put("breakfast_npo", 0);
        values.put("lunch_npo", 0);
        values.put("dinner_npo", 0);
        
        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                   new String[]{String.valueOf(patientId)});
        return rowsAffected > 0;
    }

    /**
     * FIXED: Helper method to create Patient from cursor with split names
     */
    private Patient createPatientFromCursor(Cursor cursor) {
        Patient patient = new Patient();
        
        int idxId = cursor.getColumnIndexOrThrow("patient_id");
        int idxFirstName = cursor.getColumnIndex("patient_first_name");
        int idxLastName = cursor.getColumnIndex("patient_last_name");
        int idxName = cursor.getColumnIndex("name"); // Legacy fallback
        int idxWing = cursor.getColumnIndexOrThrow("wing");
        int idxRoom = cursor.getColumnIndexOrThrow("room_number");
        int idxDiet = cursor.getColumnIndexOrThrow("diet");
        int idxFluid = cursor.getColumnIndex("fluid_restriction");
        int idxTexture = cursor.getColumnIndex("texture_modifications");
        int idxBreakfastComplete = cursor.getColumnIndexOrThrow("breakfast_complete");
        int idxLunchComplete = cursor.getColumnIndexOrThrow("lunch_complete");
        int idxDinnerComplete = cursor.getColumnIndexOrThrow("dinner_complete");
        int idxBreakfastNPO = cursor.getColumnIndexOrThrow("breakfast_npo");
        int idxLunchNPO = cursor.getColumnIndexOrThrow("lunch_npo");
        int idxDinnerNPO = cursor.getColumnIndexOrThrow("dinner_npo");
        int idxCreated = cursor.getColumnIndexOrThrow("created_date");

        patient.setPatientId(cursor.getInt(idxId));
        
        // FIXED: Handle both new split names and legacy single name
        if (idxFirstName >= 0 && !cursor.isNull(idxFirstName)) {
            patient.setPatientFirstName(cursor.getString(idxFirstName));
        }
        if (idxLastName >= 0 && !cursor.isNull(idxLastName)) {
            patient.setPatientLastName(cursor.getString(idxLastName));
        }
        
        // Legacy fallback - if we have old "name" field but no first/last names
        if (idxName >= 0 && !cursor.isNull(idxName) && 
            (patient.getPatientFirstName() == null && patient.getPatientLastName() == null)) {
            String fullName = cursor.getString(idxName);
            String[] nameParts = fullName.trim().split("\\s+", 2);
            if (nameParts.length >= 1) {
                patient.setPatientFirstName(nameParts[0]);
            }
            if (nameParts.length >= 2) {
                patient.setPatientLastName(nameParts[1]);
            } else {
                patient.setPatientLastName(""); // Set empty last name if only one name part
            }
        }
        
        patient.setWing(cursor.getString(idxWing));
        patient.setRoomNumber(cursor.getString(idxRoom));
        patient.setDiet(cursor.getString(idxDiet));
        
        if (idxFluid >= 0 && !cursor.isNull(idxFluid)) {
            patient.setFluidRestriction(cursor.getString(idxFluid));
        }
        if (idxTexture >= 0 && !cursor.isNull(idxTexture)) {
            patient.setTextureModifications(cursor.getString(idxTexture));
        }
        
        patient.setBreakfastComplete(cursor.getInt(idxBreakfastComplete) == 1);
        patient.setLunchComplete(cursor.getInt(idxLunchComplete) == 1);
        patient.setDinnerComplete(cursor.getInt(idxDinnerComplete) == 1);
        patient.setBreakfastNPO(cursor.getInt(idxBreakfastNPO) == 1);
        patient.setLunchNPO(cursor.getInt(idxLunchNPO) == 1);
        patient.setDinnerNPO(cursor.getInt(idxDinnerNPO) == 1);
        patient.setCreatedDate(cursor.getString(idxCreated));

        return patient;
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}