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
        values.put("created_date", getCurrentTimestamp());

        try {
            long result = db.insert("PatientInfo", null, values);
            Log.d("PatientDAO", "Patient added with ID: " + result);
            return result;
        } catch (Exception e) {
            Log.e("PatientDAO", "Error adding patient: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Enhanced update patient with full meal selection support
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
                Log.d("PatientDAO", "Patient updated successfully: " + patient.getPatientId());
                return true;
            }
            return false;

        } catch (Exception e) {
            Log.e("PatientDAO", "Error updating patient: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save meal selections for a patient
     */
    private void saveMealSelections(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // First, delete existing meal selections
            db.delete("PatientMealSelection", "patient_id = ?",
                    new String[]{String.valueOf(patient.getPatientId())});

            // Insert new meal selections
            if (patient.getMealSelections() != null) {
                for (String selection : patient.getMealSelections()) {
                    ContentValues values = new ContentValues();
                    values.put("patient_id", patient.getPatientId());
                    values.put("meal_selection", selection);
                    db.insert("PatientMealSelection", null, values);
                }
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error saving meal selections: " + e.getMessage());
        }
    }

    /**
     * Delete a patient and associated data
     */
    public boolean deletePatient(int patientId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            // Delete meal selections first
            db.delete("PatientMealSelection", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});

            // Delete any order history
            db.delete("OrderHistory", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});

            // Finally delete the patient
            int rowsAffected = db.delete("PatientInfo", "patient_id = ?",
                    new String[]{String.valueOf(patientId)});

            db.setTransactionSuccessful();
            Log.d("PatientDAO", "Patient deleted: " + patientId);
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e("PatientDAO", "Error deleting patient: " + e.getMessage());
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
            Log.e("PatientDAO", "Error getting patient by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    /**
     * Load meal selections for a patient
     */
    private void loadPatientMealSelections(Patient patient) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
            Log.e("PatientDAO", "Error loading meal selections: " + e.getMessage());
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
            Log.d("PatientDAO", "Found " + cursor.getCount() + " patients in database");

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
                    Log.d("PatientDAO", "Loaded patient: " + patient.getFullName());
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting all patients: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d("PatientDAO", "Returning " + patients.size() + " patients");
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
            Log.e("PatientDAO", "Error getting patient count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Get pending orders count for dashboard
     */
    public int getPendingOrdersCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM PatientInfo " +
                "WHERE (breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0)";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting pending count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Get patients who have incomplete meal orders (for pending orders)
     */
    public List<Patient> getPendingPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get patients who have at least one incomplete meal
        String query = "SELECT * FROM PatientInfo " +
                "WHERE (breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting pending patients: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    /**
     * Get patients with all meals completed (breakfast, lunch, dinner)
     */
    public List<Patient> getCompletedPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo " +
                "WHERE breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting completed patients: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return patients;
    }

    /**
     * Get patients with all meals completed for a specific created_date (formatted "yyyy-MM-dd")
     */
    public List<Patient> getOrdersByDate(String date) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo " +
                "WHERE DATE(created_date) = ? " +
                "AND breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting orders by date: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return patients;
    }

    /**
     * Get patients with completed meal orders for a specific date
     */
    public List<Patient> getCompletedOrdersByDate(String date) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo " +
                "WHERE DATE(created_date) = ? " +
                "AND breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "ORDER BY wing, room_number";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor.moveToFirst()) {
                do {
                    Patient patient = createPatientFromCursor(cursor);
                    loadPatientMealSelections(patient);
                    patients.add(patient);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error getting completed orders: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }


    /**
     * Search patients by name, wing, or room
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM PatientInfo " +
                "WHERE LOWER(patient_first_name) LIKE ? " +
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
                    loadPatientMealSelections(patient);
                    patients.add(patient);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PatientDAO", "Error searching patients: " + e.getMessage());
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
        Patient patient = new Patient();

        try {
            patient.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow("patient_id")));
            patient.setPatientFirstName(cursor.getString(cursor.getColumnIndexOrThrow("patient_first_name")));
            patient.setPatientLastName(cursor.getString(cursor.getColumnIndexOrThrow("patient_last_name")));
            patient.setWing(cursor.getString(cursor.getColumnIndexOrThrow("wing")));
            patient.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow("room_number")));

            // Handle diet fields
            String dietType = cursor.getString(cursor.getColumnIndexOrThrow("diet_type"));
            patient.setDietType(dietType);

            // Check if diet column exists, otherwise use diet_type
            int dietIdx = cursor.getColumnIndex("diet");
            if (dietIdx >= 0 && !cursor.isNull(dietIdx)) {
                patient.setDiet(cursor.getString(dietIdx));
            } else {
                patient.setDiet(dietType);
            }

            // Handle optional columns with safe checks
            patient.setAdaDiet(getColumnIntValue(cursor, "ada_diet") == 1);
            patient.setFluidRestriction(getColumnStringValue(cursor, "fluid_restriction"));
            patient.setTextureModifications(getColumnStringValue(cursor, "texture_modifications"));

            // Texture modification booleans
            patient.setMechanicalChopped(getColumnIntValue(cursor, "mechanical_chopped") == 1);
            patient.setMechanicalGround(getColumnIntValue(cursor, "mechanical_ground") == 1);
            patient.setBiteSize(getColumnIntValue(cursor, "bite_size") == 1);
            patient.setBreadOK(getColumnIntValue(cursor, "bread_ok") == 1);

            // Meal completion
            patient.setBreakfastComplete(getColumnIntValue(cursor, "breakfast_complete") == 1);
            patient.setLunchComplete(getColumnIntValue(cursor, "lunch_complete") == 1);
            patient.setDinnerComplete(getColumnIntValue(cursor, "dinner_complete") == 1);

            // NPO status
            patient.setBreakfastNPO(getColumnIntValue(cursor, "breakfast_npo") == 1);
            patient.setLunchNPO(getColumnIntValue(cursor, "lunch_npo") == 1);
            patient.setDinnerNPO(getColumnIntValue(cursor, "dinner_npo") == 1);

            // Created date
            String createdDate = getColumnStringValue(cursor, "created_date");
            if (createdDate != null) {
                try {
                    patient.setCreatedDate(dateFormat.parse(createdDate));
                } catch (ParseException e) {
                    patient.setCreatedDate(new Date());
                }
            } else {
                patient.setCreatedDate(new Date());
            }

        } catch (Exception e) {
            Log.e("PatientDAO", "Error creating patient from cursor: " + e.getMessage());
            e.printStackTrace();
        }

        return patient;
    }

    /**
     * Safe method to get string value from cursor
     */
    private String getColumnStringValue(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex >= 0 && !cursor.isNull(columnIndex)) {
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.w("PatientDAO", "Column " + columnName + " not found or null");
        }
        return null;
    }

    /**
     * Safe method to get int value from cursor
     */
    private int getColumnIntValue(Cursor cursor, String columnName) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex >= 0 && !cursor.isNull(columnIndex)) {
                return cursor.getInt(columnIndex);
            }
        } catch (Exception e) {
            Log.w("PatientDAO", "Column " + columnName + " not found or null");
        }
        return 0;
    }

    /**
     * Get current timestamp
     */
    private String getCurrentTimestamp() {
        return dateFormat.format(new Date());
    }
}
