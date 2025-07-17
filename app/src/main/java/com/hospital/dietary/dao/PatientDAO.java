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
     * Check if a patient exists in a specific wing/room
     */
    public boolean patientExistsInLocation(String wing, String roomNumber, int excludePatientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo WHERE wing = ? AND room_number = ? AND patient_id != ?";
        Cursor cursor = db.rawQuery(query, new String[]{wing, roomNumber, String.valueOf(excludePatientId)});
        
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        
        cursor.close();
        return exists;
    }

    /**
     * Helper method to create Patient object from cursor
     */
    private Patient createPatientFromCursor(Cursor cursor) {
        Patient patient = new Patient();
        
        int idxId = cursor.getColumnIndex("patient_id");
        int idxName = cursor.getColumnIndex("name");
        int idxWing = cursor.getColumnIndex("wing");
        int idxRoom = cursor.getColumnIndex("room_number");
        int idxDiet = cursor.getColumnIndex("diet");
        int idxFluidRestriction = cursor.getColumnIndex("fluid_restriction");
        int idxTextureModifications = cursor.getColumnIndex("texture_modifications");
        int idxBreakfastComplete = cursor.getColumnIndex("breakfast_complete");
        int idxLunchComplete = cursor.getColumnIndex("lunch_complete");
        int idxDinnerComplete = cursor.getColumnIndex("dinner_complete");
        int idxBreakfastNPO = cursor.getColumnIndex("breakfast_npo");
        int idxLunchNPO = cursor.getColumnIndex("lunch_npo");
        int idxDinnerNPO = cursor.getColumnIndex("dinner_npo");
        int idxCreatedDate = cursor.getColumnIndex("created_date");
        
        if (idxId != -1) patient.setPatientId(cursor.getInt(idxId));
        if (idxName != -1) patient.setName(cursor.getString(idxName));
        if (idxWing != -1) patient.setWing(cursor.getString(idxWing));
        if (idxRoom != -1) patient.setRoomNumber(cursor.getString(idxRoom));
        if (idxDiet != -1) patient.setDiet(cursor.getString(idxDiet));
        if (idxFluidRestriction != -1) patient.setFluidRestriction(cursor.getString(idxFluidRestriction));
        if (idxTextureModifications != -1) patient.setTextureModifications(cursor.getString(idxTextureModifications));
        if (idxBreakfastComplete != -1) patient.setBreakfastComplete(cursor.getInt(idxBreakfastComplete) == 1);
        if (idxLunchComplete != -1) patient.setLunchComplete(cursor.getInt(idxLunchComplete) == 1);
        if (idxDinnerComplete != -1) patient.setDinnerComplete(cursor.getInt(idxDinnerComplete) == 1);
        if (idxBreakfastNPO != -1) patient.setBreakfastNPO(cursor.getInt(idxBreakfastNPO) == 1);
        if (idxLunchNPO != -1) patient.setLunchNPO(cursor.getInt(idxLunchNPO) == 1);
        if (idxDinnerNPO != -1) patient.setDinnerNPO(cursor.getInt(idxDinnerNPO) == 1);
        if (idxCreatedDate != -1) patient.setCreatedDate(cursor.getString(idxCreatedDate));
        
        return patient;
    }
}