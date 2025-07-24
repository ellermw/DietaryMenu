package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PatientDAO {
    private DatabaseHelper dbHelper;

    public PatientDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Insert new patient
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
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("extra_gravy", patient.isExtraGravy() ? 1 : 0);
        values.put("meats_only", patient.isMeatsOnly() ? 1 : 0);
        values.put("nectar_thick", patient.isNectarThick() ? 1 : 0);
        values.put("honey_thick", patient.isHoneyThick() ? 1 : 0);
        values.put("pudding_thick", patient.isPuddingThick() ? 1 : 0);
        values.put("created_date", patient.getCreatedDate() != null ? patient.getCreatedDate().getTime() : System.currentTimeMillis());

        // Initialize meal status
        values.put("breakfast_complete", 0);
        values.put("lunch_complete", 0);
        values.put("dinner_complete", 0);
        values.put("breakfast_npo", 0);
        values.put("lunch_npo", 0);
        values.put("dinner_npo", 0);

        return db.insert("patient_info", null, values);
    }

    // Update patient - returns int for affected rows
    public int updatePatient(Patient patient) {
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
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("extra_gravy", patient.isExtraGravy() ? 1 : 0);
        values.put("meats_only", patient.isMeatsOnly() ? 1 : 0);
        values.put("nectar_thick", patient.isNectarThick() ? 1 : 0);
        values.put("honey_thick", patient.isHoneyThick() ? 1 : 0);
        values.put("pudding_thick", patient.isPuddingThick() ? 1 : 0);

        // Update meal completion status
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);

        // Update meal items
        values.put("breakfast_items", patient.getBreakfastItems());
        values.put("lunch_items", patient.getLunchItems());
        values.put("dinner_items", patient.getDinnerItems());
        values.put("breakfast_drinks", patient.getBreakfastDrinks());
        values.put("lunch_drinks", patient.getLunchDrinks());
        values.put("dinner_drinks", patient.getDinnerDrinks());

        // Update meal diet types
        values.put("breakfast_diet", patient.getBreakfastDiet());
        values.put("lunch_diet", patient.getLunchDiet());
        values.put("dinner_diet", patient.getDinnerDiet());
        values.put("breakfast_ada", patient.isBreakfastAda() ? 1 : 0);
        values.put("lunch_ada", patient.isLunchAda() ? 1 : 0);
        values.put("dinner_ada", patient.isDinnerAda() ? 1 : 0);

        return db.update("patient_info", values, "patient_id = ?",
                new String[]{String.valueOf(patient.getPatientId())});
    }

    // Delete patient by ID
    public int deletePatientById(long patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("patient_info", "patient_id = ?",
                new String[]{String.valueOf(patientId)});
    }

    // Delete patient by ID (overloaded for int)
    public int deletePatientById(int patientId) {
        return deletePatientById((long) patientId);
    }

    // Delete patient - returns boolean
    public boolean deletePatient(long patientId) {
        return deletePatientById(patientId) > 0;
    }

    // Get patient by ID
    public Patient getPatientById(int patientId) {
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

    // Get all patients
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("patient_info", null, null, null, null, null,
                "wing ASC, CAST(room_number AS INTEGER) ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Get patients with incomplete meals
    public List<Patient> getPatientsWithIncompleteMeals() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM patient_info WHERE " +
                "(breakfast_complete = 0 AND breakfast_npo = 0) OR " +
                "(lunch_complete = 0 AND lunch_npo = 0) OR " +
                "(dinner_complete = 0 AND dinner_npo = 0) " +
                "ORDER BY wing ASC, CAST(room_number AS INTEGER) ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Get pending patients (alias for getPatientsWithIncompleteMeals)
    public List<Patient> getPendingPatients() {
        return getPatientsWithIncompleteMeals();
    }

    // Get patients with complete meals
    public List<Patient> getPatientsWithCompleteMeals() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM patient_info WHERE " +
                "(breakfast_complete = 1 OR breakfast_npo = 1) AND " +
                "(lunch_complete = 1 OR lunch_npo = 1) AND " +
                "(dinner_complete = 1 OR dinner_npo = 1) " +
                "ORDER BY wing ASC, CAST(room_number AS INTEGER) ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Get completed patients (alias for getPatientsWithCompleteMeals)
    public List<Patient> getCompletedPatients() {
        return getPatientsWithCompleteMeals();
    }

    // Helper method to convert cursor to Patient object
    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();

        patient.setPatientId(cursor.getLong(cursor.getColumnIndexOrThrow("patient_id")));
        patient.setPatientFirstName(cursor.getString(cursor.getColumnIndexOrThrow("patient_first_name")));
        patient.setPatientLastName(cursor.getString(cursor.getColumnIndexOrThrow("patient_last_name")));
        patient.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
        patient.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));
        patient.setDietType(cursor.getString(cursor.getColumnIndexOrThrow("diet_type")));
        patient.setDiet(cursor.getString(cursor.getColumnIndexOrThrow("diet")));
        patient.setAdaDiet(cursor.getInt(cursor.getColumnIndexOrThrow("ada_diet")) == 1);
        patient.setFluidRestriction(cursor.getString(cursor.getColumnIndexOrThrow("fluid_restriction")));
        patient.setTextureModifications(cursor.getString(cursor.getColumnIndexOrThrow("texture_modifications")));

        // Set texture modification flags
        patient.setMechanicalGround(cursor.getInt(cursor.getColumnIndexOrThrow("mechanical_ground")) == 1);
        patient.setMechanicalChopped(cursor.getInt(cursor.getColumnIndexOrThrow("mechanical_chopped")) == 1);
        patient.setBiteSize(cursor.getInt(cursor.getColumnIndexOrThrow("bite_size")) == 1);
        patient.setBreadOK(cursor.getInt(cursor.getColumnIndexOrThrow("bread_ok")) == 1);
        patient.setExtraGravy(cursor.getInt(cursor.getColumnIndexOrThrow("extra_gravy")) == 1);
        patient.setMeatsOnly(cursor.getInt(cursor.getColumnIndexOrThrow("meats_only")) == 1);
        patient.setNectarThick(cursor.getInt(cursor.getColumnIndexOrThrow("nectar_thick")) == 1);
        patient.setHoneyThick(cursor.getInt(cursor.getColumnIndexOrThrow("honey_thick")) == 1);
        patient.setPuddingThick(cursor.getInt(cursor.getColumnIndexOrThrow("pudding_thick")) == 1);

        // Set meal completion status
        patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_complete")) == 1);
        patient.setLunchComplete(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_complete")) == 1);
        patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_complete")) == 1);
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_npo")) == 1);

        // Set meal items
        int breakfastItemsIndex = cursor.getColumnIndex("breakfast_items");
        if (breakfastItemsIndex != -1) {
            patient.setBreakfastItems(cursor.getString(breakfastItemsIndex));
        }

        int lunchItemsIndex = cursor.getColumnIndex("lunch_items");
        if (lunchItemsIndex != -1) {
            patient.setLunchItems(cursor.getString(lunchItemsIndex));
        }

        int dinnerItemsIndex = cursor.getColumnIndex("dinner_items");
        if (dinnerItemsIndex != -1) {
            patient.setDinnerItems(cursor.getString(dinnerItemsIndex));
        }

        // Set drinks
        int breakfastDrinksIndex = cursor.getColumnIndex("breakfast_drinks");
        if (breakfastDrinksIndex != -1) {
            patient.setBreakfastDrinks(cursor.getString(breakfastDrinksIndex));
        }

        int lunchDrinksIndex = cursor.getColumnIndex("lunch_drinks");
        if (lunchDrinksIndex != -1) {
            patient.setLunchDrinks(cursor.getString(lunchDrinksIndex));
        }

        int dinnerDrinksIndex = cursor.getColumnIndex("dinner_drinks");
        if (dinnerDrinksIndex != -1) {
            patient.setDinnerDrinks(cursor.getString(dinnerDrinksIndex));
        }

        // Set meal diet types
        int breakfastDietIndex = cursor.getColumnIndex("breakfast_diet");
        if (breakfastDietIndex != -1) {
            patient.setBreakfastDiet(cursor.getString(breakfastDietIndex));
        }

        int lunchDietIndex = cursor.getColumnIndex("lunch_diet");
        if (lunchDietIndex != -1) {
            patient.setLunchDiet(cursor.getString(lunchDietIndex));
        }

        int dinnerDietIndex = cursor.getColumnIndex("dinner_diet");
        if (dinnerDietIndex != -1) {
            patient.setDinnerDiet(cursor.getString(dinnerDietIndex));
        }

        // Set meal ADA flags
        int breakfastAdaIndex = cursor.getColumnIndex("breakfast_ada");
        if (breakfastAdaIndex != -1) {
            patient.setBreakfastAda(cursor.getInt(breakfastAdaIndex) == 1);
        }

        int lunchAdaIndex = cursor.getColumnIndex("lunch_ada");
        if (lunchAdaIndex != -1) {
            patient.setLunchAda(cursor.getInt(lunchAdaIndex) == 1);
        }

        int dinnerAdaIndex = cursor.getColumnIndex("dinner_ada");
        if (dinnerAdaIndex != -1) {
            patient.setDinnerAda(cursor.getInt(dinnerAdaIndex) == 1);
        }

        // Set created date
        long createdDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("created_date"));
        patient.setCreatedDate(new Date(createdDateMillis));

        // Set order date if exists
        int orderDateIndex = cursor.getColumnIndex("order_date");
        if (orderDateIndex != -1 && !cursor.isNull(orderDateIndex)) {
            long orderDateMillis = cursor.getLong(orderDateIndex);
            patient.setOrderDate(new Date(orderDateMillis));
        }

        return patient;
    }
}