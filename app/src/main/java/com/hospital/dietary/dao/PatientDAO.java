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

        // Individual meal diet fields
        values.put("breakfast_diet", patient.getBreakfastDiet());
        values.put("lunch_diet", patient.getLunchDiet());
        values.put("dinner_diet", patient.getDinnerDiet());
        values.put("breakfast_ada", patient.isBreakfastAda() ? 1 : 0);
        values.put("lunch_ada", patient.isLunchAda() ? 1 : 0);
        values.put("dinner_ada", patient.isDinnerAda() ? 1 : 0);

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

        // Individual meal diet fields
        values.put("breakfast_diet", patient.getBreakfastDiet());
        values.put("lunch_diet", patient.getLunchDiet());
        values.put("dinner_diet", patient.getDinnerDiet());
        values.put("breakfast_ada", patient.isBreakfastAda() ? 1 : 0);
        values.put("lunch_ada", patient.isLunchAda() ? 1 : 0);
        values.put("dinner_ada", patient.isDinnerAda() ? 1 : 0);

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
     * Update individual meal diet
     */
    public boolean updateMealDiet(int patientId, String meal, String diet, boolean isAda) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String dietField, adaField;
        switch (meal.toLowerCase()) {
            case "breakfast":
                dietField = "breakfast_diet";
                adaField = "breakfast_ada";
                break;
            case "lunch":
                dietField = "lunch_diet";
                adaField = "lunch_ada";
                break;
            case "dinner":
                dietField = "dinner_diet";
                adaField = "dinner_ada";
                break;
            default:
                Log.e(TAG, "Invalid meal type: " + meal);
                return false;
        }

        values.put(dietField, diet);
        values.put(adaField, isAda ? 1 : 0);

        int rowsAffected = db.update("PatientInfo", values, "patient_id = ?",
                new String[]{String.valueOf(patientId)});

        boolean success = rowsAffected > 0;
        if (success) {
            Log.d(TAG, "Updated " + meal + " diet for patient ID " + patientId + " to " + diet + (isAda ? " (ADA)" : ""));
        } else {
            Log.e(TAG, "Failed to update " + meal + " diet for patient ID " + patientId);
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
                "patient_last_name ASC, patient_first_name ASC")) {

            while (cursor.moveToNext()) {
                patients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all patients", e);
        }

        return patients;
    }

    /**
     * Get patients with pending meal orders
     */
    public List<Patient> getPendingPatients() {
        List<Patient> pendingPatients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0";

        try (Cursor cursor = db.query("PatientInfo", null, selection, null, null, null,
                "patient_last_name ASC, patient_first_name ASC")) {

            while (cursor.moveToNext()) {
                pendingPatients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting pending patients", e);
        }

        Log.d(TAG, "Found " + pendingPatients.size() + " patients with pending meals");
        return pendingPatients;
    }

    /**
     * Get patients with completed meal orders
     */
    public List<Patient> getCompletedPatients() {
        List<Patient> completedPatients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1";

        try (Cursor cursor = db.query("PatientInfo", null, selection, null, null, null,
                "patient_last_name ASC, patient_first_name ASC")) {

            while (cursor.moveToNext()) {
                completedPatients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting completed patients", e);
        }

        return completedPatients;
    }

    /**
     * Delete patient by ID
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsDeleted = db.delete("PatientInfo", "patient_id = ?",
                new String[]{String.valueOf(patientId)});

        boolean success = rowsDeleted > 0;
        if (success) {
            Log.d(TAG, "Patient deleted successfully: " + patientId);
        } else {
            Log.e(TAG, "Failed to delete patient: " + patientId);
        }

        return success;
    }

    /**
     * Search patients by name or room
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "patient_first_name LIKE ? OR patient_last_name LIKE ? OR room_number LIKE ?";
        String[] selectionArgs = {"%" + searchTerm + "%", "%" + searchTerm + "%", "%" + searchTerm + "%"};

        try (Cursor cursor = db.query("PatientInfo", null, selection, selectionArgs, null, null,
                "patient_last_name ASC, patient_first_name ASC")) {

            while (cursor.moveToNext()) {
                patients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching patients", e);
        }

        return patients;
    }

    /**
     * Get patients by wing
     */
    public List<Patient> getPatientsByWing(String wing) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query("PatientInfo", null, "wing = ?",
                new String[]{wing}, null, null, "room_number ASC")) {

            while (cursor.moveToNext()) {
                patients.add(createPatientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting patients by wing: " + wing, e);
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
        patient.setDietType(cursor.getString(cursor.getColumnIndexOrThrow("diet_type")));
        patient.setDiet(cursor.getString(cursor.getColumnIndexOrThrow("diet")));
        patient.setAdaDiet(cursor.getInt(cursor.getColumnIndexOrThrow("ada_diet")) == 1);
        patient.setFluidRestriction(cursor.getString(cursor.getColumnIndexOrThrow("fluid_restriction")));
        patient.setTextureModifications(cursor.getString(cursor.getColumnIndexOrThrow("texture_modifications")));

        // Texture modification flags
        patient.setMechanicalChopped(cursor.getInt(cursor.getColumnIndexOrThrow("mechanical_chopped")) == 1);
        patient.setMechanicalGround(cursor.getInt(cursor.getColumnIndexOrThrow("mechanical_ground")) == 1);
        patient.setBiteSize(cursor.getInt(cursor.getColumnIndexOrThrow("bite_size")) == 1);
        patient.setBreadOK(cursor.getInt(cursor.getColumnIndexOrThrow("bread_ok")) == 1);
        patient.setNectarThick(cursor.getInt(cursor.getColumnIndexOrThrow("nectar_thick")) == 1);
        patient.setPuddingThick(cursor.getInt(cursor.getColumnIndexOrThrow("pudding_thick")) == 1);
        patient.setHoneyThick(cursor.getInt(cursor.getColumnIndexOrThrow("honey_thick")) == 1);
        patient.setExtraGravy(cursor.getInt(cursor.getColumnIndexOrThrow("extra_gravy")) == 1);
        patient.setMeatsOnly(cursor.getInt(cursor.getColumnIndexOrThrow("meats_only")) == 1);

        // Meal completion flags
        patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_complete")) == 1);
        patient.setLunchComplete(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_complete")) == 1);
        patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_complete")) == 1);

        // NPO flags
        patient.setBreakfastNPO(cursor.getInt(cursor.getColumnIndexOrThrow("breakfast_npo")) == 1);
        patient.setLunchNPO(cursor.getInt(cursor.getColumnIndexOrThrow("lunch_npo")) == 1);
        patient.setDinnerNPO(cursor.getInt(cursor.getColumnIndexOrThrow("dinner_npo")) == 1);

        // Meal items
        patient.setBreakfastItems(cursor.getString(cursor.getColumnIndexOrThrow("breakfast_items")));
        patient.setLunchItems(cursor.getString(cursor.getColumnIndexOrThrow("lunch_items")));
        patient.setDinnerItems(cursor.getString(cursor.getColumnIndexOrThrow("dinner_items")));

        // Drink items
        patient.setBreakfastJuices(cursor.getString(cursor.getColumnIndexOrThrow("breakfast_juices")));
        patient.setLunchJuices(cursor.getString(cursor.getColumnIndexOrThrow("lunch_juices")));
        patient.setDinnerJuices(cursor.getString(cursor.getColumnIndexOrThrow("dinner_juices")));
        patient.setBreakfastDrinks(cursor.getString(cursor.getColumnIndexOrThrow("breakfast_drinks")));
        patient.setLunchDrinks(cursor.getString(cursor.getColumnIndexOrThrow("lunch_drinks")));
        patient.setDinnerDrinks(cursor.getString(cursor.getColumnIndexOrThrow("dinner_drinks")));

        // Individual meal diet fields (handle potential null values)
        int breakfastDietIndex = cursor.getColumnIndex("breakfast_diet");
        int lunchDietIndex = cursor.getColumnIndex("lunch_diet");
        int dinnerDietIndex = cursor.getColumnIndex("dinner_diet");
        int breakfastAdaIndex = cursor.getColumnIndex("breakfast_ada");
        int lunchAdaIndex = cursor.getColumnIndex("lunch_ada");
        int dinnerAdaIndex = cursor.getColumnIndex("dinner_ada");

        if (breakfastDietIndex != -1) {
            patient.setBreakfastDiet(cursor.getString(breakfastDietIndex));
        }
        if (lunchDietIndex != -1) {
            patient.setLunchDiet(cursor.getString(lunchDietIndex));
        }
        if (dinnerDietIndex != -1) {
            patient.setDinnerDiet(cursor.getString(dinnerDietIndex));
        }
        if (breakfastAdaIndex != -1) {
            patient.setBreakfastAda(cursor.getInt(breakfastAdaIndex) == 1);
        }
        if (lunchAdaIndex != -1) {
            patient.setLunchAda(cursor.getInt(lunchAdaIndex) == 1);
        }
        if (dinnerAdaIndex != -1) {
            patient.setDinnerAda(cursor.getInt(dinnerAdaIndex) == 1);
        }

        // Created date
        try {
            String dateString = cursor.getString(cursor.getColumnIndexOrThrow("created_date"));
            if (dateString != null) {
                patient.setCreatedDate(dateFormat.parse(dateString));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing created date", e);
            patient.setCreatedDate(new Date());
        }

        return patient;
    }

    /**
     * Save meal selections for a patient
     */
    private void saveMealSelections(Patient patient) {
        // This method can be used to save additional meal selection data if needed
        // Currently, meal items are stored as strings in the patient record
        Log.d(TAG, "Meal selections saved for patient: " + patient.getFullName());
    }

    /**
     * Get count of patients by status
     */
    public int getPendingPatientsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo WHERE breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting pending patients count", e);
        }

        return 0;
    }

    public int getCompletedPatientsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo WHERE breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting completed patients count", e);
        }

        return 0;
    }
}