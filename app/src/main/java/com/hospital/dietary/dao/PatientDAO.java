package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PatientDAO class for database operations on patient data
 */
public class PatientDAO {

    private DatabaseHelper dbHelper;

    public PatientDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Insert a new patient (alias for insertPatient)
     */
    public long addPatient(Patient patient) {
        return insertPatient(patient);
    }

    /**
     * Insert a new patient
     */
    public long insertPatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patient_first_name", patient.getPatientFirstName());
        values.put("patient_last_name", patient.getPatientLastName());
        values.put("wing", patient.getWing());
        values.put("room_number", patient.getRoomNumber());
        values.put("diet_type", patient.getDietType());
        values.put("diet", patient.getDiet());
        values.put("ada_diet", patient.isAdaDiet() ? 1 : 0);
        values.put("fluid_restriction", patient.getFluidRestriction());
        values.put("texture_modifications", patient.getTextureModifications());
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("nectar_thick", patient.isNectarThick() ? 1 : 0);
        values.put("pudding_thick", patient.isPuddingThick() ? 1 : 0);
        values.put("honey_thick", patient.isHoneyThick() ? 1 : 0);
        values.put("extra_gravy", patient.isExtraGravy() ? 1 : 0);
        values.put("meats_only", patient.isMeatsOnly() ? 1 : 0);
        values.put("is_puree", patient.isPuree() ? 1 : 0);
        values.put("allergies", patient.getAllergies());
        values.put("likes", patient.getLikes());
        values.put("dislikes", patient.getDislikes());
        values.put("comments", patient.getComments());
        values.put("preferred_drink", patient.getPreferredDrink());
        values.put("drink_variety", patient.getDrinkVariety());
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);
        values.put("breakfast_items", patient.getBreakfastItems());
        values.put("lunch_items", patient.getLunchItems());
        values.put("dinner_items", patient.getDinnerItems());
        values.put("breakfast_juices", patient.getBreakfastJuices());
        values.put("lunch_juices", patient.getLunchJuices());
        values.put("dinner_juices", patient.getDinnerJuices());
        values.put("breakfast_drinks", patient.getBreakfastDrinks());
        values.put("lunch_drinks", patient.getLunchDrinks());
        values.put("dinner_drinks", patient.getDinnerDrinks());
        values.put("breakfast_diet", patient.getBreakfastDiet());
        values.put("lunch_diet", patient.getLunchDiet());
        values.put("dinner_diet", patient.getDinnerDiet());
        values.put("breakfast_ada", patient.isBreakfastAda() ? 1 : 0);
        values.put("lunch_ada", patient.isLunchAda() ? 1 : 0);
        values.put("dinner_ada", patient.isDinnerAda() ? 1 : 0);
        values.put("created_date", System.currentTimeMillis());

        return db.insert("patient_info", null, values);
    }

    /**
     * Update an existing patient - returns boolean for success
     */
    public boolean updatePatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("patient_first_name", patient.getPatientFirstName());
        values.put("patient_last_name", patient.getPatientLastName());
        values.put("wing", patient.getWing());
        values.put("room_number", patient.getRoomNumber());
        values.put("diet_type", patient.getDietType());
        values.put("diet", patient.getDiet());
        values.put("ada_diet", patient.isAdaDiet() ? 1 : 0);
        values.put("fluid_restriction", patient.getFluidRestriction());
        values.put("texture_modifications", patient.getTextureModifications());
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("nectar_thick", patient.isNectarThick() ? 1 : 0);
        values.put("pudding_thick", patient.isPuddingThick() ? 1 : 0);
        values.put("honey_thick", patient.isHoneyThick() ? 1 : 0);
        values.put("extra_gravy", patient.isExtraGravy() ? 1 : 0);
        values.put("meats_only", patient.isMeatsOnly() ? 1 : 0);
        values.put("is_puree", patient.isPuree() ? 1 : 0);
        values.put("allergies", patient.getAllergies());
        values.put("likes", patient.getLikes());
        values.put("dislikes", patient.getDislikes());
        values.put("comments", patient.getComments());
        values.put("preferred_drink", patient.getPreferredDrink());
        values.put("drink_variety", patient.getDrinkVariety());
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);
        values.put("breakfast_items", patient.getBreakfastItems());
        values.put("lunch_items", patient.getLunchItems());
        values.put("dinner_items", patient.getDinnerItems());
        values.put("breakfast_juices", patient.getBreakfastJuices());
        values.put("lunch_juices", patient.getLunchJuices());
        values.put("dinner_juices", patient.getDinnerJuices());
        values.put("breakfast_drinks", patient.getBreakfastDrinks());
        values.put("lunch_drinks", patient.getLunchDrinks());
        values.put("dinner_drinks", patient.getDinnerDrinks());
        values.put("breakfast_diet", patient.getBreakfastDiet());
        values.put("lunch_diet", patient.getLunchDiet());
        values.put("dinner_diet", patient.getDinnerDiet());
        values.put("breakfast_ada", patient.isBreakfastAda() ? 1 : 0);
        values.put("lunch_ada", patient.isLunchAda() ? 1 : 0);
        values.put("dinner_ada", patient.isDinnerAda() ? 1 : 0);

        int rowsUpdated = db.update("patient_info", values, "patient_id = ?",
                new String[]{String.valueOf(patient.getPatientId())});

        return rowsUpdated > 0;
    }

    /**
     * Delete a patient - returns boolean for success
     */
    public boolean deletePatient(long patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete("patient_info", "patient_id = ?",
                new String[]{String.valueOf(patientId)});
        return rowsDeleted > 0;
    }

    /**
     * Get a patient by ID
     */
    public Patient getPatientById(long patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("patient_info", null, "patient_id = ?",
                new String[]{String.valueOf(patientId)}, null, null, null);

        Patient patient = null;
        if (cursor != null && cursor.moveToFirst()) {
            patient = cursorToPatient(cursor);
            cursor.close();
        }

        return patient;
    }

    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM patient_info ORDER BY wing, CAST(room_number AS INTEGER)";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return patients;
    }

    /**
     * Get active patients (not discharged)
     */
    public List<Patient> getActivePatients() {
        // For now, return all patients since discharge status isn't tracked
        return getAllPatients();
    }

    /**
     * Get patients with pending meals
     */
    public List<Patient> getPendingPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM patient_info WHERE " +
                "(breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) " +
                "ORDER BY wing, CAST(room_number AS INTEGER)";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return patients;
    }

    /**
     * Get patients with completed meals
     */
    public List<Patient> getCompletedPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM patient_info WHERE " +
                "breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "ORDER BY wing, CAST(room_number AS INTEGER)";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return patients;
    }

    /**
     * Check if a room is occupied
     */
    public int isRoomOccupied(String wing, String roomNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("patient_info", new String[]{"patient_id"},
                "wing = ? AND room_number = ?",
                new String[]{wing, roomNumber}, null, null, null);

        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }

    /**
     * Update meal completion status
     */
    public int updateMealCompletion(long patientId, boolean breakfastComplete,
                                    boolean lunchComplete, boolean dinnerComplete) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("breakfast_complete", breakfastComplete ? 1 : 0);
        values.put("lunch_complete", lunchComplete ? 1 : 0);
        values.put("dinner_complete", dinnerComplete ? 1 : 0);

        return db.update("patient_info", values, "patient_id = ?",
                new String[]{String.valueOf(patientId)});
    }

    /**
     * Convert cursor to Patient object
     */
    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();

        patient.setPatientId(cursor.getLong(cursor.getColumnIndex("patient_id")));
        patient.setPatientFirstName(cursor.getString(cursor.getColumnIndex("patient_first_name")));
        patient.setPatientLastName(cursor.getString(cursor.getColumnIndex("patient_last_name")));
        patient.setWing(cursor.getString(cursor.getColumnIndex("wing")));
        patient.setRoomNumber(cursor.getString(cursor.getColumnIndex("room_number")));
        patient.setDietType(cursor.getString(cursor.getColumnIndex("diet_type")));
        patient.setDiet(cursor.getString(cursor.getColumnIndex("diet")));
        patient.setAdaDiet(cursor.getInt(cursor.getColumnIndex("ada_diet")) == 1);
        patient.setFluidRestriction(cursor.getString(cursor.getColumnIndex("fluid_restriction")));
        patient.setTextureModifications(cursor.getString(cursor.getColumnIndex("texture_modifications")));
        patient.setMechanicalChopped(cursor.getInt(cursor.getColumnIndex("mechanical_chopped")) == 1);
        patient.setMechanicalGround(cursor.getInt(cursor.getColumnIndex("mechanical_ground")) == 1);
        patient.setBiteSize(cursor.getInt(cursor.getColumnIndex("bite_size")) == 1);
        patient.setBreadOK(cursor.getInt(cursor.getColumnIndex("bread_ok")) == 1);
        patient.setNectarThick(cursor.getInt(cursor.getColumnIndex("nectar_thick")) == 1);
        patient.setPuddingThick(cursor.getInt(cursor.getColumnIndex("pudding_thick")) == 1);
        patient.setHoneyThick(cursor.getInt(cursor.getColumnIndex("honey_thick")) == 1);
        patient.setExtraGravy(cursor.getInt(cursor.getColumnIndex("extra_gravy")) == 1);
        patient.setMeatsOnly(cursor.getInt(cursor.getColumnIndex("meats_only")) == 1);
        patient.setPuree(cursor.getInt(cursor.getColumnIndex("is_puree")) == 1);
        patient.setAllergies(cursor.getString(cursor.getColumnIndex("allergies")));
        patient.setLikes(cursor.getString(cursor.getColumnIndex("likes")));
        patient.setDislikes(cursor.getString(cursor.getColumnIndex("dislikes")));
        patient.setComments(cursor.getString(cursor.getColumnIndex("comments")));
        patient.setPreferredDrink(cursor.getString(cursor.getColumnIndex("preferred_drink")));
        patient.setDrinkVariety(cursor.getString(cursor.getColumnIndex("drink_variety")));
        patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndex("breakfast_complete")) == 1);
        patient.setLunchComplete(cursor.getInt(cursor.getColumnIndex("lunch_complete")) == 1);
        patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndex("dinner_complete")) == 1);
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndex("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndex("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndex("dinner_npo")) == 1);
        patient.setBreakfastItems(cursor.getString(cursor.getColumnIndex("breakfast_items")));
        patient.setLunchItems(cursor.getString(cursor.getColumnIndex("lunch_items")));
        patient.setDinnerItems(cursor.getString(cursor.getColumnIndex("dinner_items")));
        patient.setBreakfastJuices(cursor.getString(cursor.getColumnIndex("breakfast_juices")));
        patient.setLunchJuices(cursor.getString(cursor.getColumnIndex("lunch_juices")));
        patient.setDinnerJuices(cursor.getString(cursor.getColumnIndex("dinner_juices")));
        patient.setBreakfastDrinks(cursor.getString(cursor.getColumnIndex("breakfast_drinks")));
        patient.setLunchDrinks(cursor.getString(cursor.getColumnIndex("lunch_drinks")));
        patient.setDinnerDrinks(cursor.getString(cursor.getColumnIndex("dinner_drinks")));
        patient.setBreakfastDiet(cursor.getString(cursor.getColumnIndex("breakfast_diet")));
        patient.setLunchDiet(cursor.getString(cursor.getColumnIndex("lunch_diet")));
        patient.setDinnerDiet(cursor.getString(cursor.getColumnIndex("dinner_diet")));
        patient.setBreakfastAda(cursor.getInt(cursor.getColumnIndex("breakfast_ada")) == 1);
        patient.setLunchAda(cursor.getInt(cursor.getColumnIndex("lunch_ada")) == 1);
        patient.setDinnerAda(cursor.getInt(cursor.getColumnIndex("dinner_ada")) == 1);

        // Set created date if available
        long createdDateMillis = cursor.getLong(cursor.getColumnIndex("created_date"));
        if (createdDateMillis > 0) {
            patient.setCreatedDate(new Date(createdDateMillis));
        }

        return patient;
    }
}