package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Patient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PatientDAO {
    private static final String TAG = "PatientDAO";
    private final DatabaseHelper dbHelper;

    public PatientDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Insert new patient
    public long insertPatient(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = getContentValues(patient);

        // Set created_date if not already set
        if (patient.getCreatedDate() == null) {
            values.put("created_date", System.currentTimeMillis());
        }

        long id = db.insert("patient_info", null, values);
        patient.setPatientId(id);
        return id;
    }

    // Update patient - returns int (number of rows updated)
    public int updatePatientInt(Patient patient) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = getContentValues(patient);

        return db.update("patient_info", values, "patient_id = ?",
                new String[]{String.valueOf(patient.getPatientId())});
    }

    // Update patient - returns boolean (for compatibility with existing code)
    public boolean updatePatient(Patient patient) {
        return updatePatientInt(patient) > 0;
    }

    // Helper method to create ContentValues from Patient
    private ContentValues getContentValues(Patient patient) {
        ContentValues values = new ContentValues();

        // Basic information
        values.put("patient_first_name", patient.getPatientFirstName());
        values.put("patient_last_name", patient.getPatientLastName());
        values.put("wing", patient.getWing());
        values.put("room_number", patient.getRoomNumber());

        // Diet information
        values.put("diet_type", patient.getDietType());
        values.put("diet", patient.getDiet());
        values.put("ada_diet", patient.isAdaDiet() ? 1 : 0);

        // Dietary information
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

        // Meal completion status
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

        // Meal juices
        values.put("breakfast_juices", patient.getBreakfastJuices());
        values.put("lunch_juices", patient.getLunchJuices());
        values.put("dinner_juices", patient.getDinnerJuices());

        // Meal drinks
        values.put("breakfast_drinks", patient.getBreakfastDrinks());
        values.put("lunch_drinks", patient.getLunchDrinks());
        values.put("dinner_drinks", patient.getDinnerDrinks());

        // Additional patient information
        values.put("allergies", patient.getAllergies());
        values.put("likes", patient.getLikes());
        values.put("dislikes", patient.getDislikes());
        values.put("comments", patient.getComments());

        // Individual meal components
        values.put("breakfast_main", patient.getBreakfastMain());
        values.put("breakfast_side", patient.getBreakfastSide());
        values.put("breakfast_drink", patient.getBreakfastDrink());
        values.put("lunch_main", patient.getLunchMain());
        values.put("lunch_side", patient.getLunchSide());
        values.put("lunch_drink", patient.getLunchDrink());
        values.put("dinner_main", patient.getDinnerMain());
        values.put("dinner_side", patient.getDinnerSide());
        values.put("dinner_drink", patient.getDinnerDrink());

        // Individual meal diets
        values.put("breakfast_diet", patient.getBreakfastDiet());
        values.put("lunch_diet", patient.getLunchDiet());
        values.put("dinner_diet", patient.getDinnerDiet());
        values.put("breakfast_ada", patient.isBreakfastAda() ? 1 : 0);
        values.put("lunch_ada", patient.isLunchAda() ? 1 : 0);
        values.put("dinner_ada", patient.isDinnerAda() ? 1 : 0);

        // Discharge status
        values.put("discharged", patient.isDischarged() ? 1 : 0);

        // Created date
        if (patient.getCreatedAt() > 0) {
            values.put("created_date", patient.getCreatedAt());
        }

        return values;
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

    // Get patient by ID (overloaded for long)
    public Patient getPatientById(long patientId) {
        return getPatientById((int) patientId);
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

    // Get pending patients (meals not complete)
    public List<Patient> getPendingPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM patient_info WHERE " +
                "(discharged = 0 OR discharged IS NULL) AND " +
                "(breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) " +
                "ORDER BY wing, CAST(room_number AS INTEGER)";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Get completed patients (all meals complete)
    public List<Patient> getCompletedPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM patient_info WHERE " +
                "(discharged = 0 OR discharged IS NULL) AND " +
                "breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
                "ORDER BY wing, CAST(room_number AS INTEGER)";

        Cursor cursor = db.rawQuery(query, null);

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

    // Search patients by name
    public List<Patient> searchPatientsByName(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM patient_info WHERE " +
                "patient_first_name LIKE ? OR patient_last_name LIKE ? " +
                "ORDER BY wing, CAST(room_number AS INTEGER)";

        String searchPattern = "%" + searchTerm + "%";
        Cursor cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Get patients by wing
    public List<Patient> getPatientsByWing(String wing) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("patient_info", null, "wing = ?",
                new String[]{wing}, null, null, "CAST(room_number AS INTEGER)");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                patients.add(cursorToPatient(cursor));
            }
            cursor.close();
        }

        return patients;
    }

    // Check if room is occupied
    public boolean isRoomOccupied(String wing, String roomNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM patient_info WHERE wing = ? AND room_number = ? " +
                "AND (discharged = 0 OR discharged IS NULL)";

        Cursor cursor = db.rawQuery(query, new String[]{wing, roomNumber});
        boolean occupied = false;

        if (cursor != null && cursor.moveToFirst()) {
            occupied = cursor.getInt(0) > 0;
            cursor.close();
        }

        return occupied;
    }

    // Update meal completion status
    public boolean updateMealComplete(long patientId, String mealType, boolean complete) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        switch (mealType.toLowerCase()) {
            case "breakfast":
                values.put("breakfast_complete", complete ? 1 : 0);
                break;
            case "lunch":
                values.put("lunch_complete", complete ? 1 : 0);
                break;
            case "dinner":
                values.put("dinner_complete", complete ? 1 : 0);
                break;
            default:
                return false;
        }

        int rowsUpdated = db.update("patient_info", values, "patient_id = ?",
                new String[]{String.valueOf(patientId)});

        return rowsUpdated > 0;
    }

    // Update NPO status
    public boolean updateNPOStatus(long patientId, String mealType, boolean npo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        switch (mealType.toLowerCase()) {
            case "breakfast":
                values.put("breakfast_npo", npo ? 1 : 0);
                break;
            case "lunch":
                values.put("lunch_npo", npo ? 1 : 0);
                break;
            case "dinner":
                values.put("dinner_npo", npo ? 1 : 0);
                break;
            default:
                return false;
        }

        int rowsUpdated = db.update("patient_info", values, "patient_id = ?",
                new String[]{String.valueOf(patientId)});

        return rowsUpdated > 0;
    }

    // Convert cursor to Patient object
    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();

        // Basic information
        patient.setPatientId(getLong(cursor, "patient_id"));
        patient.setPatientFirstName(getString(cursor, "patient_first_name"));
        patient.setPatientLastName(getString(cursor, "patient_last_name"));
        patient.setWing(getString(cursor, "wing"));
        patient.setRoomNumber(getString(cursor, "room_number"));

        // Diet information
        patient.setDietType(getString(cursor, "diet_type"));
        patient.setDiet(getString(cursor, "diet"));
        patient.setAdaDiet(getBoolean(cursor, "ada_diet"));
        patient.setFluidRestriction(getString(cursor, "fluid_restriction"));
        patient.setTextureModifications(getString(cursor, "texture_modifications"));

        // Texture modification flags
        patient.setMechanicalChopped(getBoolean(cursor, "mechanical_chopped"));
        patient.setMechanicalGround(getBoolean(cursor, "mechanical_ground"));
        patient.setBiteSize(getBoolean(cursor, "bite_size"));
        patient.setBreadOK(getBoolean(cursor, "bread_ok"));
        patient.setExtraGravy(getBoolean(cursor, "extra_gravy"));
        patient.setMeatsOnly(getBoolean(cursor, "meats_only"));

        // Liquid thickness flags
        patient.setNectarThick(getBoolean(cursor, "nectar_thick"));
        patient.setHoneyThick(getBoolean(cursor, "honey_thick"));
        patient.setPuddingThick(getBoolean(cursor, "pudding_thick"));

        // Meal completion status
        patient.setBreakfastComplete(getBoolean(cursor, "breakfast_complete"));
        patient.setLunchComplete(getBoolean(cursor, "lunch_complete"));
        patient.setDinnerComplete(getBoolean(cursor, "dinner_complete"));
        patient.setBreakfastNPO(getBoolean(cursor, "breakfast_npo"));
        patient.setLunchNPO(getBoolean(cursor, "lunch_npo"));
        patient.setDinnerNPO(getBoolean(cursor, "dinner_npo"));

        // Meal items
        patient.setBreakfastItems(getString(cursor, "breakfast_items"));
        patient.setLunchItems(getString(cursor, "lunch_items"));
        patient.setDinnerItems(getString(cursor, "dinner_items"));

        // Meal juices
        patient.setBreakfastJuices(getString(cursor, "breakfast_juices"));
        patient.setLunchJuices(getString(cursor, "lunch_juices"));
        patient.setDinnerJuices(getString(cursor, "dinner_juices"));

        // Meal drinks
        patient.setBreakfastDrinks(getString(cursor, "breakfast_drinks"));
        patient.setLunchDrinks(getString(cursor, "lunch_drinks"));
        patient.setDinnerDrinks(getString(cursor, "dinner_drinks"));

        // Additional patient information
        patient.setAllergies(getString(cursor, "allergies"));
        patient.setLikes(getString(cursor, "likes"));
        patient.setDislikes(getString(cursor, "dislikes"));
        patient.setComments(getString(cursor, "comments"));

        // Individual meal components
        patient.setBreakfastMain(getString(cursor, "breakfast_main"));
        patient.setBreakfastSide(getString(cursor, "breakfast_side"));
        patient.setBreakfastDrink(getString(cursor, "breakfast_drink"));
        patient.setLunchMain(getString(cursor, "lunch_main"));
        patient.setLunchSide(getString(cursor, "lunch_side"));
        patient.setLunchDrink(getString(cursor, "lunch_drink"));
        patient.setDinnerMain(getString(cursor, "dinner_main"));
        patient.setDinnerSide(getString(cursor, "dinner_side"));
        patient.setDinnerDrink(getString(cursor, "dinner_drink"));

        // Individual meal diets
        patient.setBreakfastDiet(getString(cursor, "breakfast_diet"));
        patient.setLunchDiet(getString(cursor, "lunch_diet"));
        patient.setDinnerDiet(getString(cursor, "dinner_diet"));
        patient.setBreakfastAda(getBoolean(cursor, "breakfast_ada"));
        patient.setLunchAda(getBoolean(cursor, "lunch_ada"));
        patient.setDinnerAda(getBoolean(cursor, "dinner_ada"));

        // Discharge status
        patient.setDischarged(getBoolean(cursor, "discharged"));

        // Created date
        long createdDate = getLong(cursor, "created_date");
        if (createdDate > 0) {
            patient.setCreatedAt(createdDate);
            patient.setCreatedDate(new Date(createdDate));
        }

        // Order date
        long orderDate = getLong(cursor, "order_date");
        if (orderDate > 0) {
            patient.setOrderDate(new Date(orderDate));
        }

        return patient;
    }

    // Helper methods for cursor data extraction
    private String getString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getString(index) : null;
    }

    private long getLong(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getLong(index) : 0;
    }

    private boolean getBoolean(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 && cursor.getInt(index) == 1;
    }

    // Close database connection
    public void close() {
        dbHelper.close();
    }
}