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
     * Add a new patient
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
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
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
        values.put("created_date", dateFormat.format(new Date()));

        try {
            long result = db.insert("PatientInfo", null, values);
            if (result != -1) {
                patient.setPatientId((int) result);
                // Save meal selections if they exist
                saveMealSelections(patient);
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error adding patient: " + e.getMessage());
            e.printStackTrace();
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
        values.put("diet_type", patient.getDietType());
        values.put("diet", patient.getDiet());
        values.put("ada_diet", patient.isAdaDiet() ? 1 : 0);
        values.put("fluid_restriction", patient.getFluidRestriction());
        values.put("texture_modifications", patient.getTextureModifications());
        values.put("mechanical_chopped", patient.isMechanicalChopped() ? 1 : 0);
        values.put("mechanical_ground", patient.isMechanicalGround() ? 1 : 0);
        values.put("bite_size", patient.isBiteSize() ? 1 : 0);
        values.put("bread_ok", patient.isBreadOK() ? 1 : 0);
        values.put("breakfast_items", patient.getBreakfastItems());
        values.put("lunch_items", patient.getLunchItems());
        values.put("dinner_items", patient.getDinnerItems());
        values.put("breakfast_juices", patient.getBreakfastJuices());
        values.put("lunch_juices", patient.getLunchJuices());
        values.put("dinner_juices", patient.getDinnerJuices());
        values.put("breakfast_drinks", patient.getBreakfastDrinks());
        values.put("lunch_drinks", patient.getLunchDrinks());
        values.put("dinner_drinks", patient.getDinnerDrinks());
        values.put("breakfast_complete", patient.isBreakfastComplete() ? 1 : 0);
        values.put("lunch_complete", patient.isLunchComplete() ? 1 : 0);
        values.put("dinner_complete", patient.isDinnerComplete() ? 1 : 0);
        values.put("breakfast_npo", patient.isBreakfastNPO() ? 1 : 0);
        values.put("lunch_npo", patient.isLunchNPO() ? 1 : 0);
        values.put("dinner_npo", patient.isDinnerNPO() ? 1 : 0);

        try {
            // First update the patient record
            int rowsAffected = db.update("PatientInfo", values, "patient_id = ?",
                    new String[]{String.valueOf(patient.getPatientId())});

            if (rowsAffected > 0) {
                // Save meal selections if they exist
                saveMealSelections(patient);
                Log.d(TAG, "Patient updated successfully: " + patient.getPatientId());
                return true;
            }
            return false;

        } catch (Exception e) {
            Log.e(TAG, "Error updating patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * FIXED: Save meal selections with error handling for missing table
     */
    private void saveMealSelections(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if table exists first
        if (!tableExists(db, "PatientMealSelection")) {
            Log.w(TAG, "PatientMealSelection table does not exist, cannot save meal selections");
            return;
        }

        try {
            // First, delete existing meal selections
            db.delete("PatientMealSelection", "patient_id = ?",
                    new String[]{String.valueOf(patient.getPatientId())});

            // Insert new meal selections
            if (patient.getMealSelections() != null && !patient.getMealSelections().isEmpty()) {
                for (String selection : patient.getMealSelections()) {
                    ContentValues values = new ContentValues();
                    values.put("patient_id", patient.getPatientId());
                    values.put("meal_selection", selection);
                    db.insert("PatientMealSelection", null, values);
                }
                Log.d(TAG, "Saved " + patient.getMealSelections().size() + " meal selections");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving meal selections: " + e.getMessage());
        }
    }

    /**
     * Delete a patient and associated data
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            // Delete meal selections first (if table exists)
            if (tableExists(db, "PatientMealSelection")) {
                db.delete("PatientMealSelection", "patient_id = ?",
                        new String[]{String.valueOf(patientId)});
            }

            // Delete any order history (if table exists)
            if (tableExists(db, "OrderHistory")) {
                db.delete("OrderHistory", "patient_id = ?",
                        new String[]{String.valueOf(patientId)});
            }

            // Finally delete the patient
            int rowsAffected = db.delete("PatientInfo", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});

            db.setTransactionSuccessful();
            Log.d(TAG, "Patient deleted: " + patientId);
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error deleting patient: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
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
                Patient patient = createPatientFromCursor(cursor);
                loadPatientMealSelections(patient);
                return patient;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting patient by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * FIXED: Load meal selections for a patient with error handling
     */
    private void loadPatientMealSelections(Patient patient) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Check if table exists first
        if (!tableExists(db, "PatientMealSelection")) {
            Log.w(TAG, "PatientMealSelection table does not exist, skipping meal selection loading");
            patient.setMealSelections(new ArrayList<>()); // Set empty list
            return;
        }

        String query = "SELECT meal_selection FROM PatientMealSelection WHERE patient_id = ?";
        Cursor cursor = null;
        List<String> selections = new ArrayList<>();

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(patient.getPatientId())});

            while (cursor.moveToNext()) {
                selections.add(cursor.getString(0));
            }

            patient.setMealSelections(selections);

        } catch (Exception e) {
            Log.e(TAG, "Error loading meal selections: " + e.getMessage());
            patient.setMealSelections(new ArrayList<>()); // Set empty list on error
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * FIXED: Get all patients - ensure this includes ALL patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // FIXED: Simple query to get ALL patients without any filtering
        String query = "SELECT * FROM PatientInfo ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            Log.d(TAG, "Found " + cursor.getCount() + " patients in database");

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    if (patient != null) {
                        loadPatientMealSelections(patient);
                        patients.add(patient);
                        Log.d(TAG, "Loaded patient: " + patient.getFullName());
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all patients: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "Returning " + patients.size() + " patients");
        return patients;
    }

    /**
     * Get total patient count for dashboard
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
            Log.e(TAG, "Error getting patient count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Search patients by various criteria
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo WHERE " +
                "LOWER(patient_first_name) LIKE ? " +
                "OR LOWER(patient_last_name) LIKE ? " +
                "OR LOWER(wing) LIKE ? " +
                "OR room_number LIKE ? " +
                "ORDER BY wing, room_number";

        String searchPattern = "%" + searchTerm.toLowerCase() + "%";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern, searchPattern, searchPattern});

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    if (patient != null) {
                        loadPatientMealSelections(patient);
                        patients.add(patient);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching patients: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * Helper method to create patient from cursor
     */
    private Patient createPatientFromCursor(Cursor cursor) {
        try {
            Patient patient = new Patient();

            patient.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
            patient.setPatientFirstName(cursor.getString(cursor.getColumnIndexOrThrow("patient_first_name")));
            patient.setPatientLastName(cursor.getString(cursor.getColumnIndexOrThrow("patient_last_name")));
            patient.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
            patient.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));

            // Handle diet fields
            String dietType = cursor.getString(cursor.getColumnIndexOrThrow("diet_type"));
            patient.setDietType(dietType);

            // Check for diet column
            int dietIdx = cursor.getColumnIndex("diet");
            if (dietIdx >= 0) {
                String diet = cursor.getString(dietIdx);
                patient.setDiet(diet != null ? diet : dietType);
            } else {
                patient.setDiet(dietType);
            }

            // Boolean fields with safe handling
            patient.setAdaDiet(cursor.getInt(cursor.getColumnIndex("ada_diet")) == 1);

            // Optional text fields
            patient.setFluidRestriction(cursor.getString(cursor.getColumnIndex("fluid_restriction")));
            patient.setTextureModifications(cursor.getString(cursor.getColumnIndex("texture_modifications")));

            // Texture modification boolean fields with safe handling
            int mechChoppedIdx = cursor.getColumnIndex("mechanical_chopped");
            if (mechChoppedIdx >= 0) {
                patient.setMechanicalChopped(cursor.getInt(mechChoppedIdx) == 1);
            }

            int mechGroundIdx = cursor.getColumnIndex("mechanical_ground");
            if (mechGroundIdx >= 0) {
                patient.setMechanicalGround(cursor.getInt(mechGroundIdx) == 1);
            }

            int biteSizeIdx = cursor.getColumnIndex("bite_size");
            if (biteSizeIdx >= 0) {
                patient.setBiteSize(cursor.getInt(biteSizeIdx) == 1);
            }

            int breadOkIdx = cursor.getColumnIndex("bread_ok");
            if (breadOkIdx >= 0) {
                patient.setBreadOK(cursor.getInt(breadOkIdx) == 1);
            }

            // Meal completion status
            patient.setBreakfastComplete(cursor.getInt(cursor.getColumnIndex("breakfast_complete")) == 1);
            patient.setLunchComplete(cursor.getInt(cursor.getColumnIndex("lunch_complete")) == 1);
            patient.setDinnerComplete(cursor.getInt(cursor.getColumnIndex("dinner_complete")) == 1);

            // NPO status
            int breakfastNpoIdx = cursor.getColumnIndex("breakfast_npo");
            if (breakfastNpoIdx >= 0) {
                patient.setBreakfastNPO(cursor.getInt(breakfastNpoIdx) == 1);
            }

            int lunchNpoIdx = cursor.getColumnIndex("lunch_npo");
            if (lunchNpoIdx >= 0) {
                patient.setLunchNPO(cursor.getInt(lunchNpoIdx) == 1);
            }

            int dinnerNpoIdx = cursor.getColumnIndex("dinner_npo");
            if (dinnerNpoIdx >= 0) {
                patient.setDinnerNPO(cursor.getInt(dinnerNpoIdx) == 1);
            }

            // Meal items
            patient.setBreakfastItems(cursor.getString(cursor.getColumnIndex("breakfast_items")));
            patient.setLunchItems(cursor.getString(cursor.getColumnIndex("lunch_items")));
            patient.setDinnerItems(cursor.getString(cursor.getColumnIndex("dinner_items")));

            // Meal drinks
            patient.setBreakfastJuices(cursor.getString(cursor.getColumnIndex("breakfast_juices")));
            patient.setLunchJuices(cursor.getString(cursor.getColumnIndex("lunch_juices")));
            patient.setDinnerJuices(cursor.getString(cursor.getColumnIndex("dinner_juices")));
            patient.setBreakfastDrinks(cursor.getString(cursor.getColumnIndex("breakfast_drinks")));
            patient.setLunchDrinks(cursor.getString(cursor.getColumnIndex("lunch_drinks")));
            patient.setDinnerDrinks(cursor.getString(cursor.getColumnIndex("dinner_drinks")));

            // Created date
            String createdDateStr = cursor.getString(cursor.getColumnIndex("created_date"));
            if (createdDateStr != null) {
                try {
                    Date createdDate = dateFormat.parse(createdDateStr);
                    patient.setCreatedDate(createdDate);
                } catch (ParseException e) {
                    Log.w(TAG, "Could not parse created date: " + createdDateStr);
                }
            }

            return patient;

        } catch (Exception e) {
            Log.e(TAG, "Error creating patient from cursor: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to check if table exists
     */
    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = ?",
                    new String[]{tableName});
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking table existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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
                    Patient patient = createPatientFromCursor(cursor);
                    if (patient != null) {
                        loadPatientMealSelections(patient);
                        patients.add(patient);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting patients by wing: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * Check if room is occupied by another patient
     */
    public boolean isRoomOccupied(String wing, String roomNumber, int excludePatientId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo WHERE wing = ? AND room_number = ? AND patient_id != ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{wing, roomNumber, String.valueOf(excludePatientId)});
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking room occupancy: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    /**
     * Get all wings in the hospital
     */
    public List<String> getAllWings() {
        List<String> wings = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT DISTINCT wing FROM PatientInfo ORDER BY wing";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    wings.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all wings: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return wings;
    }

    /**
     * Get patients with incomplete meal plans
     */
    public List<Patient> getPatientsWithIncompleteMeals() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo WHERE " +
                "breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0 " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    if (patient != null) {
                        loadPatientMealSelections(patient);
                        patients.add(patient);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting patients with incomplete meals: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }
}