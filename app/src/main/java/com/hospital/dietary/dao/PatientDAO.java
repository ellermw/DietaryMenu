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

        return db.insert("PatientInfo", null, values);
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

        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?", 
                                   new String[]{String.valueOf(patient.getPatientId())});
        return rowsAffected > 0;
    }

    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo ORDER BY created_date DESC, wing, CAST(room_number AS INTEGER)";
        
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
     * Get patient by ID
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
     * Get patient by location (wing and room)
     */
    public Patient getPatientByLocation(String wing, String roomNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM PatientInfo WHERE wing = ? AND room_number = ?";
        Cursor cursor = db.rawQuery(query, new String[]{wing, roomNumber});

        Patient patient = null;
        if (cursor.moveToFirst()) {
            patient = createPatientFromCursor(cursor);
        }

        cursor.close();
        return patient;
    }

    /**
     * Search patients by name
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT * FROM PatientInfo WHERE " +
                      "patient_first_name LIKE ? OR patient_last_name LIKE ? OR " +
                      "(patient_first_name || ' ' || patient_last_name) LIKE ? " +
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
     * Delete a patient
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete("PatientInfo", "patient_id = ?", 
                                   new String[]{String.valueOf(patientId)});
        return rowsAffected > 0;
    }

    /**
     * Get patient count
     */
    public int getPatientCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo";
        Cursor cursor = db.rawQuery(query, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Get available room numbers for a wing - Updated with correct hospital layout
     */
    public List<String> getAvailableRooms(String wing) {
        List<String> availableRooms = new ArrayList<>();
        
        if ("1 South".equals(wing)) {
            for (int i = 106; i <= 122; i++) {
                if (!isRoomOccupied(wing, String.valueOf(i))) {
                    availableRooms.add(String.valueOf(i));
                }
            }
        } else if ("2 North".equals(wing)) {
            for (int i = 250; i <= 264; i++) {
                if (!isRoomOccupied(wing, String.valueOf(i))) {
                    availableRooms.add(String.valueOf(i));
                }
            }
        } else if ("Labor and Delivery".equals(wing)) {
            for (int i = 1; i <= 6; i++) {
                String room = "LDR" + i;
                if (!isRoomOccupied(wing, room)) {
                    availableRooms.add(room);
                }
            }
        } else if ("2 West".equals(wing)) {
            for (int i = 225; i <= 248; i++) {
                if (!isRoomOccupied(wing, String.valueOf(i))) {
                    availableRooms.add(String.valueOf(i));
                }
            }
        } else if ("3 North".equals(wing)) {
            for (int i = 349; i <= 371; i++) {
                if (!isRoomOccupied(wing, String.valueOf(i))) {
                    availableRooms.add(String.valueOf(i));
                }
            }
        } else if ("ICU".equals(wing)) {
            for (int i = 1; i <= 13; i++) {
                String room = "ICU" + i;
                if (!isRoomOccupied(wing, room)) {
                    availableRooms.add(room);
                }
            }
        }
        
        return availableRooms;
    }

    /**
     * Check if a room is occupied
     */
    private boolean isRoomOccupied(String wing, String room) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo WHERE wing = ? AND room_number = ?";
        Cursor cursor = db.rawQuery(query, new String[]{wing, room});
        
        boolean occupied = false;
        if (cursor.moveToFirst()) {
            occupied = cursor.getInt(0) > 0;
        }
        cursor.close();
        return occupied;
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
        
        // Handle nullable columns
        int fluidIndex = cursor.getColumnIndex("fluid_restriction");
        if (fluidIndex != -1 && !cursor.isNull(fluidIndex)) {
            patient.setFluidRestriction(cursor.getString(fluidIndex));
        }
        
        int textureIndex = cursor.getColumnIndex("texture_modifications");
        if (textureIndex != -1 && !cursor.isNull(textureIndex)) {
            patient.setTextureModifications(cursor.getString(textureIndex));
        }
        
        patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_complete")) == 1);
        patient.setLunchComplete(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_complete")) == 1);
        patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_complete")) == 1);
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_npo")) == 1);
        
        // Handle created_date
        int createdDateIndex = cursor.getColumnIndex("created_date");
        if (createdDateIndex != -1 && !cursor.isNull(createdDateIndex)) {
            String createdDateStr = cursor.getString(createdDateIndex);
            try {
                Date createdDate = dateFormat.parse(createdDateStr);
                patient.setCreatedDate(createdDate);
            } catch (Exception e) {
                // If parsing fails, set to current date
                patient.setCreatedDate(new Date());
            }
        } else {
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