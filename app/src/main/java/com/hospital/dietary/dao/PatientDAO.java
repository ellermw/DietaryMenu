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

    private static final String TAG = "PatientDAO";
    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public PatientDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * FIXED: Add a new patient with meals set as INCOMPLETE by default
     */
    public long addPatient(Patient patient) {
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

        // Include all texture modification fields
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("nectar_thick", patient.isNectarThick() ? 1 : 0);
        values.put("pudding_thick", patient.isPuddingThick() ? 1 : 0);
        values.put("honey_thick", patient.isHoneyThick() ? 1 : 0);
        values.put("extra_gravy", patient.isExtraGravy() ? 1 : 0);
        values.put("meats_only", patient.isMeatsOnly() ? 1 : 0);

        // CRITICAL FIX: Always set meals as INCOMPLETE (0) for new patients
        values.put("breakfast_complete", 0);  // Always false for new patients
        values.put("lunch_complete", 0);      // Always false for new patients
        values.put("dinner_complete", 0);     // Always false for new patients
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
        values.put("created_date", dateFormat.format(patient.getCreatedDate()));

        long result = db.insert("PatientInfo", null, values);

        if (result > 0) {
            patient.setPatientId((int) result);
            saveMealSelections(patient);
            Log.d(TAG, "Patient added successfully with ID: " + result + " - meals set as INCOMPLETE");
        } else {
            Log.e(TAG, "Failed to add patient");
        }

        return result;
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
        values.put("diet_type", patient.getDietType());
        values.put("diet", patient.getDiet());
        values.put("ada_diet", patient.isAdaDiet() ? 1 : 0);
        values.put("fluid_restriction", patient.getFluidRestriction());
        values.put("texture_modifications", patient.getTextureModifications());

        // Include all texture modification fields
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("nectar_thick", patient.isNectarThick() ? 1 : 0);
        values.put("pudding_thick", patient.isPuddingThick() ? 1 : 0);
        values.put("honey_thick", patient.isHoneyThick() ? 1 : 0);
        values.put("extra_gravy", patient.isExtraGravy() ? 1 : 0);
        values.put("meats_only", patient.isMeatsOnly() ? 1 : 0);

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

        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?",
                new String[]{String.valueOf(patient.getPatientId())});

        boolean success = rowsAffected > 0;
        if (success) {
            saveMealSelections(patient);
            Log.d(TAG, "Patient updated successfully: " + patient.getFullName());
        } else {
            Log.e(TAG, "Failed to update patient: " + patient.getFullName());
        }

        return success;
    }

    /**
     * Get patient by ID
     */
    public Patient getPatientById(int patientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Patient patient = null;

        try (Cursor cursor = db.query("PatientInfo", null, "patient_id = ?",
                new String[]{String.valueOf(patientId)}, null, null, null)) {

            if (cursor.moveToFirst()) {
                patient = createPatientFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting patient by ID: " + patientId, e);
        }

        return patient;
    }

    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query("PatientInfo", null, null, null, null, null,
                "wing ASC, room_number ASC")) {

            while (cursor.moveToNext()) {
                patients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all patients", e);
        }

        return patients;
    }

    /**
     * FIXED: Get patients with pending orders - shows ALL patients that have incomplete meals
     */
    public List<Patient> getPendingPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // A patient is pending if ANY meal is incomplete AND not NPO
        String selection = "(breakfast_complete = 0 AND breakfast_npo = 0) OR " +
                "(lunch_complete = 0 AND lunch_npo = 0) OR " +
                "(dinner_complete = 0 AND dinner_npo = 0)";

        try (Cursor cursor = db.query("PatientInfo", null, selection, null, null, null,
                "wing ASC, room_number ASC")) {

            while (cursor.moveToNext()) {
                patients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting pending patients", e);
        }

        Log.d(TAG, "Found " + patients.size() + " pending patients");
        return patients;
    }

    /**
     * Get patients with completed orders (for finished orders view)
     */
    public List<Patient> getCompletedPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // A patient is completed if ALL meals are either complete OR NPO
        String selection = "(breakfast_complete = 1 OR breakfast_npo = 1) AND " +
                "(lunch_complete = 1 OR lunch_npo = 1) AND " +
                "(dinner_complete = 1 OR dinner_npo = 1)";

        try (Cursor cursor = db.query("PatientInfo", null, selection, null, null, null,
                "wing ASC, room_number ASC")) {

            while (cursor.moveToNext()) {
                patients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting completed patients", e);
        }

        return patients;
    }

    /**
     * Get orders by date (for retired orders view)
     */
    public List<Patient> getOrdersByDate(String dateString) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // For now, return completed patients (you can enhance this to filter by actual date)
        try (Cursor cursor = db.query("PatientInfo", null, null, null, null, null,
                "wing ASC, room_number ASC")) {

            while (cursor.moveToNext()) {
                patients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting orders by date: " + dateString, e);
        }

        return patients;
    }

    /**
     * Get patient by room location
     */
    public Patient getPatientInRoom(String wing, String roomNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Patient patient = null;

        try (Cursor cursor = db.query("PatientInfo", null, "wing = ? AND room_number = ?",
                new String[]{wing, roomNumber}, null, null, null)) {

            if (cursor.moveToFirst()) {
                patient = createPatientFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting patient in room: " + wing + " " + roomNumber, e);
        }

        return patient;
    }

    /**
     * Delete a patient
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            // Delete from PatientInfo table
            int rowsAffected = db.delete("PatientInfo", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});

            // Also delete from MealSelections table if it exists
            try {
                db.delete("MealSelections", "patient_id = ?",
                        new String[]{String.valueOf(patientId)});
            } catch (Exception e) {
                Log.w(TAG, "Could not delete meal selections (table may not exist): " + e.getMessage());
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Patient " + patientId + " deleted successfully");
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error deleting patient: " + patientId, e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Save meal selections for a patient
     */
    private void saveMealSelections(Patient patient) {
        // This method can be enhanced to save detailed meal selections
        // For now, it's a placeholder for future meal selection functionality
        Log.d(TAG, "Meal selections saved for patient: " + patient.getPatientId());
    }

    /**
     * Create a Patient object from database cursor
     */
    private Patient createPatientFromCursor(Cursor cursor) {
        Patient patient = new Patient();

        try {
            // Basic patient information
            patient.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
            patient.setPatientFirstName(cursor.getString(cursor.getColumnIndexOrThrow("patient_first_name")));
            patient.setPatientLastName(cursor.getString(cursor.getColumnIndexOrThrow("patient_last_name")));
            patient.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
            patient.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));

            // Diet information
            int dietTypeIdx = cursor.getColumnIndex("diet_type");
            if (dietTypeIdx >= 0) {
                patient.setDietType(cursor.getString(dietTypeIdx));
            }

            patient.setDiet(cursor.getString(cursor.getColumnIndexOrThrow("diet")));
            patient.setAdaDiet(cursor.getInt(cursor.getColumnIndexOrThrow("ada_diet")) == 1);

            // Fluid restriction
            int fluidRestrictionIdx = cursor.getColumnIndex("fluid_restriction");
            if (fluidRestrictionIdx >= 0) {
                patient.setFluidRestriction(cursor.getString(fluidRestrictionIdx));
            }

            // Texture modifications
            int textureModificationsIdx = cursor.getColumnIndex("texture_modifications");
            if (textureModificationsIdx >= 0) {
                patient.setTextureModifications(cursor.getString(textureModificationsIdx));
            }

            // Individual texture modification fields
            int mechanicalChoppedIdx = cursor.getColumnIndex("mechanical_chopped");
            if (mechanicalChoppedIdx >= 0) {
                patient.setMechanicalChopped(cursor.getInt(mechanicalChoppedIdx) == 1);
            }

            int mechanicalGroundIdx = cursor.getColumnIndex("mechanical_ground");
            if (mechanicalGroundIdx >= 0) {
                patient.setMechanicalGround(cursor.getInt(mechanicalGroundIdx) == 1);
            }

            int biteSizeIdx = cursor.getColumnIndex("bite_size");
            if (biteSizeIdx >= 0) {
                patient.setBiteSize(cursor.getInt(biteSizeIdx) == 1);
            }

            int breadOKIdx = cursor.getColumnIndex("bread_ok");
            if (breadOKIdx >= 0) {
                patient.setBreadOK(cursor.getInt(breadOKIdx) == 1);
            }

            // New texture modification fields
            int nectarThickIdx = cursor.getColumnIndex("nectar_thick");
            if (nectarThickIdx >= 0) {
                patient.setNectarThick(cursor.getInt(nectarThickIdx) == 1);
            }

            int puddingThickIdx = cursor.getColumnIndex("pudding_thick");
            if (puddingThickIdx >= 0) {
                patient.setPuddingThick(cursor.getInt(puddingThickIdx) == 1);
            }

            int honeyThickIdx = cursor.getColumnIndex("honey_thick");
            if (honeyThickIdx >= 0) {
                patient.setHoneyThick(cursor.getInt(honeyThickIdx) == 1);
            }

            int extraGravyIdx = cursor.getColumnIndex("extra_gravy");
            if (extraGravyIdx >= 0) {
                patient.setExtraGravy(cursor.getInt(extraGravyIdx) == 1);
            }

            int meatsOnlyIdx = cursor.getColumnIndex("meats_only");
            if (meatsOnlyIdx >= 0) {
                patient.setMeatsOnly(cursor.getInt(meatsOnlyIdx) == 1);
            }

            // Meal completion flags
            patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_complete")) == 1);
            patient.setLunchComplete(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_complete")) == 1);
            patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_complete")) == 1);

            // NPO flags
            int breakfastNPOIdx = cursor.getColumnIndex("breakfast_npo");
            if (breakfastNPOIdx >= 0) {
                patient.setBreakfastNPO(cursor.getInt(breakfastNPOIdx) == 1);
            }

            int lunchNPOIdx = cursor.getColumnIndex("lunch_npo");
            if (lunchNPOIdx >= 0) {
                patient.setLunchNPO(cursor.getInt(lunchNPOIdx) == 1);
            }

            int dinnerNPOIdx = cursor.getColumnIndex("dinner_npo");
            if (dinnerNPOIdx >= 0) {
                patient.setDinnerNPO(cursor.getInt(dinnerNPOIdx) == 1);
            }

            // Meal items
            int breakfastItemsIdx = cursor.getColumnIndex("breakfast_items");
            if (breakfastItemsIdx >= 0) {
                patient.setBreakfastItems(cursor.getString(breakfastItemsIdx));
            }

            int lunchItemsIdx = cursor.getColumnIndex("lunch_items");
            if (lunchItemsIdx >= 0) {
                patient.setLunchItems(cursor.getString(lunchItemsIdx));
            }

            int dinnerItemsIdx = cursor.getColumnIndex("dinner_items");
            if (dinnerItemsIdx >= 0) {
                patient.setDinnerItems(cursor.getString(dinnerItemsIdx));
            }

            // Juice items
            int breakfastJuicesIdx = cursor.getColumnIndex("breakfast_juices");
            if (breakfastJuicesIdx >= 0) {
                patient.setBreakfastJuices(cursor.getString(breakfastJuicesIdx));
            }

            int lunchJuicesIdx = cursor.getColumnIndex("lunch_juices");
            if (lunchJuicesIdx >= 0) {
                patient.setLunchJuices(cursor.getString(lunchJuicesIdx));
            }

            int dinnerJuicesIdx = cursor.getColumnIndex("dinner_juices");
            if (dinnerJuicesIdx >= 0) {
                patient.setDinnerJuices(cursor.getString(dinnerJuicesIdx));
            }

            // Drink items
            int breakfastDrinksIdx = cursor.getColumnIndex("breakfast_drinks");
            if (breakfastDrinksIdx >= 0) {
                patient.setBreakfastDrinks(cursor.getString(breakfastDrinksIdx));
            }

            int lunchDrinksIdx = cursor.getColumnIndex("lunch_drinks");
            if (lunchDrinksIdx >= 0) {
                patient.setLunchDrinks(cursor.getString(lunchDrinksIdx));
            }

            int dinnerDrinksIdx = cursor.getColumnIndex("dinner_drinks");
            if (dinnerDrinksIdx >= 0) {
                patient.setDinnerDrinks(cursor.getString(dinnerDrinksIdx));
            }

            // Created date
            int createdDateIdx = cursor.getColumnIndex("created_date");
            if (createdDateIdx >= 0) {
                String createdDateStr = cursor.getString(createdDateIdx);
                if (createdDateStr != null) {
                    try {
                        patient.setCreatedDate(dateFormat.parse(createdDateStr));
                    } catch (ParseException e) {
                        Log.w(TAG, "Error parsing created date: " + createdDateStr);
                        patient.setCreatedDate(new Date());
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating patient from cursor", e);
        }

        return patient;
    }
}