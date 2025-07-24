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

        // Texture modification flags
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("extra_gravy", patient.isExtraGravy() ? 1 : 0);
        values.put("meats_only", patient.isMeatsOnly() ? 1 : 0);

        // Liquid thickness flags
        values.put("nectar_thick", patient.isNectarThick() ? 1 : 0);
        values.put("honey_thick", patient.isHoneyThick() ? 1 : 0);
        values.put("pudding_thick", patient.isPuddingThick() ? 1 : 0);

        // Meal status
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);

        // Meal items
        values.put("breakfast_items", patient.getBreakfastItems());
        values.put("lunch_items", patient.getLunchItems());
        values.put("dinner_items", patient.getDinnerItems());
        values.put("breakfast_juices", patient.getBreakfastJuices());
        values.put("lunch_juices", patient.getLunchJuices());
        values.put("dinner_juices", patient.getDinnerJuices());
        values.put("breakfast_drinks", patient.getBreakfastDrinks());
        values.put("lunch_drinks", patient.getLunchDrinks());
        values.put("dinner_drinks", patient.getDinnerDrinks());

        // Individual meal diets
        values.put("breakfast_diet", patient.getBreakfastDiet());
        values.put("lunch_diet", patient.getLunchDiet());
        values.put("dinner_diet", patient.getDinnerDiet());
        values.put("breakfast_ada", patient.isBreakfastAda() ? 1 : 0);
        values.put("lunch_ada", patient.isLunchAda() ? 1 : 0);
        values.put("dinner_ada", patient.isDinnerAda() ? 1 : 0);

        // Dates
        values.put("created_date", patient.getCreatedDate() != null ?
                patient.getCreatedDate().getTime() : System.currentTimeMillis());
        values.put("order_date", patient.getOrderDate() != null ?
                patient.getOrderDate().getTime() : System.currentTimeMillis());

        // Discharge status
        values.put("discharged", patient.isDischarged() ? 1 : 0);

        return db.insert("patient_info", null, values);
    }

    // Update patient
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

        // Texture modification flags
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("extra_gravy", patient.isExtraGravy() ? 1 : 0);
        values.put("meats_only", patient.isMeatsOnly() ? 1 : 0);

        // Liquid thickness flags
        values.put("nectar_thick", patient.isNectarThick() ? 1 : 0);
        values.put("honey_thick", patient.isHoneyThick() ? 1 : 0);
        values.put("pudding_thick", patient.isPuddingThick() ? 1 : 0);

        // Meal status
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);

        // Meal items
        values.put("breakfast_items", patient.getBreakfastItems());
        values.put("lunch_items", patient.getLunchItems());
        values.put("dinner_items", patient.getDinnerItems());
        values.put("breakfast_juices", patient.getBreakfastJuices());
        values.put("lunch_juices", patient.getLunchJuices());
        values.put("dinner_juices", patient.getDinnerJuices());
        values.put("breakfast_drinks", patient.getBreakfastDrinks());
        values.put("lunch_drinks", patient.getLunchDrinks());
        values.put("dinner_drinks", patient.getDinnerDrinks());

        // Individual meal diets
        values.put("breakfast_diet", patient.getBreakfastDiet());
        values.put("lunch_diet", patient.getLunchDiet());
        values.put("dinner_diet", patient.getDinnerDiet());
        values.put("breakfast_ada", patient.isBreakfastAda() ? 1 : 0);
        values.put("lunch_ada", patient.isLunchAda() ? 1 : 0);
        values.put("dinner_ada", patient.isDinnerAda() ? 1 : 0);

        // Discharge status
        values.put("discharged", patient.isDischarged() ? 1 : 0);

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
                "wing, CAST(room_number AS INTEGER)");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Get active patients (not discharged)
    public List<Patient> getActivePatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("patient_info", null, "discharged = 0 OR discharged IS NULL",
                null, null, null, "wing, CAST(room_number AS INTEGER)");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Get discharged patients
    public List<Patient> getDischargedPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("patient_info", null, "discharged = 1", null, null, null,
                "wing, CAST(room_number AS INTEGER)");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Get retired patients (discharged or > 6 days old)
    public List<Patient> getRetiredPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Calculate 6 days ago timestamp
        long sixDaysAgo = System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000);

        String query = "SELECT * FROM patient_info WHERE discharged = 1 OR created_date < ? " +
                "ORDER BY wing, CAST(room_number AS INTEGER)";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sixDaysAgo)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Helper method to convert cursor to Patient object
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

        // Texture modification flags
        patient.setMechanicalChopped(cursor.getInt(cursor.getColumnIndex("mechanical_chopped")) == 1);
        patient.setMechanicalGround(cursor.getInt(cursor.getColumnIndex("mechanical_ground")) == 1);
        patient.setBiteSize(cursor.getInt(cursor.getColumnIndex("bite_size")) == 1);
        patient.setBreadOK(cursor.getInt(cursor.getColumnIndex("bread_ok")) == 1);
        patient.setExtraGravy(cursor.getInt(cursor.getColumnIndex("extra_gravy")) == 1);
        patient.setMeatsOnly(cursor.getInt(cursor.getColumnIndex("meats_only")) == 1);

        // Liquid thickness flags
        patient.setNectarThick(cursor.getInt(cursor.getColumnIndex("nectar_thick")) == 1);
        patient.setHoneyThick(cursor.getInt(cursor.getColumnIndex("honey_thick")) == 1);
        patient.setPuddingThick(cursor.getInt(cursor.getColumnIndex("pudding_thick")) == 1);

        // Meal status
        patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndex("breakfast_complete")) == 1);
        patient.setLunchComplete(cursor.getInt(cursor.getColumnIndex("lunch_complete")) == 1);
        patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndex("dinner_complete")) == 1);
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndex("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndex("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndex("dinner_npo")) == 1);

        // Meal items
        patient.setBreakfastItems(cursor.getString(cursor.getColumnIndex("breakfast_items")));
        patient.setLunchItems(cursor.getString(cursor.getColumnIndex("lunch_items")));
        patient.setDinnerItems(cursor.getString(cursor.getColumnIndex("dinner_items")));
        patient.setBreakfastJuices(cursor.getString(cursor.getColumnIndex("breakfast_juices")));
        patient.setLunchJuices(cursor.getString(cursor.getColumnIndex("lunch_juices")));
        patient.setDinnerJuices(cursor.getString(cursor.getColumnIndex("dinner_juices")));
        patient.setBreakfastDrinks(cursor.getString(cursor.getColumnIndex("breakfast_drinks")));
        patient.setLunchDrinks(cursor.getString(cursor.getColumnIndex("lunch_drinks")));
        patient.setDinnerDrinks(cursor.getString(cursor.getColumnIndex("dinner_drinks")));

        // Individual meal diets
        patient.setBreakfastDiet(cursor.getString(cursor.getColumnIndex("breakfast_diet")));
        patient.setLunchDiet(cursor.getString(cursor.getColumnIndex("lunch_diet")));
        patient.setDinnerDiet(cursor.getString(cursor.getColumnIndex("dinner_diet")));
        patient.setBreakfastAda(cursor.getInt(cursor.getColumnIndex("breakfast_ada")) == 1);
        patient.setLunchAda(cursor.getInt(cursor.getColumnIndex("lunch_ada")) == 1);
        patient.setDinnerAda(cursor.getInt(cursor.getColumnIndex("dinner_ada")) == 1);

        // Dates
        long createdTime = cursor.getLong(cursor.getColumnIndex("created_date"));
        if (createdTime > 0) {
            patient.setCreatedDate(new Date(createdTime));
        }

        long orderTime = cursor.getLong(cursor.getColumnIndex("order_date"));
        if (orderTime > 0) {
            patient.setOrderDate(new Date(orderTime));
        }

        // Discharge status - handle column that might not exist
        int dischargedIndex = cursor.getColumnIndex("discharged");
        if (dischargedIndex >= 0) {
            patient.setDischarged(cursor.getInt(dischargedIndex) == 1);
        } else {
            patient.setDischarged(false);
        }

        return patient;
    }
}