package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "hospital_dietary.db";
    private static final int DATABASE_VERSION = 4; // UPDATED: Incremented version for table name fix

    // User table
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE User (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'user', " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +
                    "must_change_password INTEGER NOT NULL DEFAULT 0, " +
                    "created_date TEXT, " +
                    "last_login TEXT" +
                    ")";

    // FIXED: Changed table name from Patient to PatientInfo to match DAO expectations
    private static final String CREATE_PATIENT_TABLE =
            "CREATE TABLE PatientInfo (" +
                    "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_first_name TEXT NOT NULL, " +
                    "patient_last_name TEXT NOT NULL, " +
                    "wing TEXT NOT NULL, " +
                    "room_number TEXT NOT NULL, " +
                    "diet_type TEXT, " +
                    "diet TEXT, " +
                    "ada_diet INTEGER NOT NULL DEFAULT 0, " +
                    "fluid_restriction TEXT, " +
                    "texture_modifications TEXT, " +
                    "mechanical_chopped INTEGER NOT NULL DEFAULT 0, " +
                    "mechanical_ground INTEGER NOT NULL DEFAULT 0, " +
                    "bite_size INTEGER NOT NULL DEFAULT 0, " +
                    "bread_ok INTEGER NOT NULL DEFAULT 0, " +
                    "breakfast_complete INTEGER NOT NULL DEFAULT 0, " +
                    "lunch_complete INTEGER NOT NULL DEFAULT 0, " +
                    "dinner_complete INTEGER NOT NULL DEFAULT 0, " +
                    "breakfast_npo INTEGER NOT NULL DEFAULT 0, " +
                    "lunch_npo INTEGER NOT NULL DEFAULT 0, " +
                    "dinner_npo INTEGER NOT NULL DEFAULT 0, " +
                    "breakfast_items TEXT, " +
                    "lunch_items TEXT, " +
                    "dinner_items TEXT, " +
                    "breakfast_juices TEXT, " +
                    "lunch_juices TEXT, " +
                    "dinner_juices TEXT, " +
                    "breakfast_drinks TEXT, " +
                    "lunch_drinks TEXT, " +
                    "dinner_drinks TEXT, " +
                    "created_date TEXT" +
                    ")";

    // Item table
    private static final String CREATE_ITEM_TABLE =
            "CREATE TABLE Item (" +
                    "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "ada_friendly INTEGER NOT NULL DEFAULT 0" +
                    ")";

    // FinalizedOrder table
    private static final String CREATE_FINALIZED_ORDER_TABLE =
            "CREATE TABLE FinalizedOrder (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_name TEXT NOT NULL, " +
                    "wing TEXT NOT NULL, " +
                    "room TEXT NOT NULL, " +
                    "order_date TEXT NOT NULL, " +
                    "diet_type TEXT NOT NULL, " +
                    "fluid_restriction TEXT, " +
                    "mechanical_chopped INTEGER NOT NULL DEFAULT 0, " +
                    "mechanical_ground INTEGER NOT NULL DEFAULT 0, " +
                    "bite_size INTEGER NOT NULL DEFAULT 0, " +
                    "bread_ok INTEGER NOT NULL DEFAULT 0, " +
                    "breakfast_items TEXT, " +
                    "lunch_items TEXT, " +
                    "dinner_items TEXT, " +
                    "breakfast_juices TEXT, " +
                    "lunch_juices TEXT, " +
                    "dinner_juices TEXT, " +
                    "breakfast_drinks TEXT, " +
                    "lunch_drinks TEXT, " +
                    "dinner_drinks TEXT" +
                    ")";

    // Category table (for Item categorization)
    private static final String CREATE_CATEGORY_TABLE =
            "CREATE TABLE Category (" +
                    "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "display_order INTEGER NOT NULL DEFAULT 0" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");

        try {
            // Create all tables
            db.execSQL(CREATE_USER_TABLE);
            db.execSQL(CREATE_PATIENT_TABLE);
            db.execSQL(CREATE_ITEM_TABLE);
            db.execSQL(CREATE_FINALIZED_ORDER_TABLE);
            db.execSQL(CREATE_CATEGORY_TABLE);

            // Insert default data
            insertDefaultUsers(db);
            insertDefaultCategories(db);
            insertDefaultItems(db);

            Log.d(TAG, "Database tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        try {
            if (oldVersion < 2) {
                // Add texture modification columns if upgrading from version 1
                addTextureModificationColumns(db);
            }

            if (oldVersion < 3) {
                // Add password change tracking column
                addPasswordChangeColumn(db);
            }

            if (oldVersion < 4) {
                // FIXED: Rename Patient table to PatientInfo and add missing columns
                migratePatientTableToPatientInfo(db);
            }

            Log.d(TAG, "Database upgrade completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
            // If upgrade fails, recreate database
            db.execSQL("DROP TABLE IF EXISTS User");
            db.execSQL("DROP TABLE IF EXISTS Patient");
            db.execSQL("DROP TABLE IF EXISTS PatientInfo");
            db.execSQL("DROP TABLE IF EXISTS Item");
            db.execSQL("DROP TABLE IF EXISTS FinalizedOrder");
            db.execSQL("DROP TABLE IF EXISTS Category");
            onCreate(db);
        }
    }

    // FIXED: Migrate old Patient table to PatientInfo with new columns
    private void migratePatientTableToPatientInfo(SQLiteDatabase db) {
        try {
            // Check if old Patient table exists
            if (tableExists(db, "Patient")) {
                Log.d(TAG, "Migrating Patient table to PatientInfo");

                // Create the new PatientInfo table
                db.execSQL(CREATE_PATIENT_TABLE);

                // Copy data from Patient to PatientInfo
                db.execSQL("INSERT INTO PatientInfo " +
                        "(patient_id, patient_first_name, patient_last_name, wing, room_number, " +
                        "diet_type, ada_diet, fluid_restriction, mechanical_chopped, mechanical_ground, " +
                        "bite_size, bread_ok, breakfast_complete, lunch_complete, dinner_complete, " +
                        "breakfast_items, lunch_items, dinner_items, breakfast_juices, lunch_juices, " +
                        "dinner_juices, breakfast_drinks, lunch_drinks, dinner_drinks, created_date) " +
                        "SELECT patient_id, patient_first_name, patient_last_name, wing, room_number, " +
                        "diet_type, ada_diet, fluid_restriction, " +
                        "COALESCE(mechanical_chopped, 0), COALESCE(mechanical_ground, 0), " +
                        "COALESCE(bite_size, 0), COALESCE(bread_ok, 0), " +
                        "breakfast_complete, lunch_complete, dinner_complete, " +
                        "breakfast_items, lunch_items, dinner_items, breakfast_juices, lunch_juices, " +
                        "dinner_juices, breakfast_drinks, lunch_drinks, dinner_drinks, created_date " +
                        "FROM Patient");

                // Update diet column to match diet_type where diet is null
                db.execSQL("UPDATE PatientInfo SET diet = diet_type WHERE diet IS NULL");

                // Drop the old Patient table
                db.execSQL("DROP TABLE Patient");

                Log.d(TAG, "Successfully migrated Patient table to PatientInfo");
            } else {
                // PatientInfo table doesn't exist, create it
                Log.d(TAG, "Creating PatientInfo table");
                db.execSQL(CREATE_PATIENT_TABLE);
            }

            // Add Category table if it doesn't exist
            if (!tableExists(db, "Category")) {
                db.execSQL(CREATE_CATEGORY_TABLE);
                insertDefaultCategories(db);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error migrating Patient table: " + e.getMessage());
            throw e;
        }
    }

    // Helper method to check if table exists
    private boolean tableExists(SQLiteDatabase db, String tableName) {
        try {
            db.rawQuery("SELECT 1 FROM " + tableName + " LIMIT 1", null).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Add password change tracking column for existing databases
    private void addPasswordChangeColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE User ADD COLUMN must_change_password INTEGER NOT NULL DEFAULT 0");
            Log.d(TAG, "Added must_change_password column to User table");
        } catch (Exception e) {
            Log.w(TAG, "Column must_change_password may already exist: " + e.getMessage());
        }
    }

    // Add texture modification columns for existing databases
    private void addTextureModificationColumns(SQLiteDatabase db) {
        try {
            // Add columns to Patient table (will be migrated to PatientInfo)
            String tableName = tableExists(db, "PatientInfo") ? "PatientInfo" : "Patient";

            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN mechanical_chopped INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN mechanical_ground INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN bite_size INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN bread_ok INTEGER NOT NULL DEFAULT 0");

            // Add columns to FinalizedOrder table
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN mechanical_chopped INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN mechanical_ground INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN bite_size INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN bread_ok INTEGER NOT NULL DEFAULT 0");

            Log.d(TAG, "Added texture modification columns");
        } catch (Exception e) {
            Log.w(TAG, "Texture modification columns may already exist: " + e.getMessage());
        }
    }

    private void insertDefaultUsers(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default users");

        try {
            // Default admin user
            ContentValues adminValues = new ContentValues();
            adminValues.put("username", "admin");
            adminValues.put("password", "admin123");
            adminValues.put("full_name", "System Administrator");
            adminValues.put("role", "admin");
            adminValues.put("is_active", 1);
            adminValues.put("must_change_password", 1);
            adminValues.put("created_date", getCurrentTimestamp());

            long adminResult = db.insert("User", null, adminValues);

            // Default user
            ContentValues userValues = new ContentValues();
            userValues.put("username", "user");
            userValues.put("password", "user123");
            userValues.put("full_name", "Default User");
            userValues.put("role", "user");
            userValues.put("is_active", 1);
            userValues.put("must_change_password", 1);
            userValues.put("created_date", getCurrentTimestamp());

            long userResult = db.insert("User", null, userValues);

            Log.d(TAG, "Default users inserted. Admin: " + adminResult + ", User: " + userResult);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default users: " + e.getMessage());
        }
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default categories");

        try {
            String[] categories = {
                    "Breakfast Items", "Proteins", "Starches", "Vegetables",
                    "Beverages", "Juices", "Desserts", "Fruits", "Dairy"
            };

            for (int i = 0; i < categories.length; i++) {
                ContentValues values = new ContentValues();
                values.put("name", categories[i]);
                values.put("display_order", i + 1);
                db.insert("Category", null, values);
            }

            Log.d(TAG, "Default categories inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default categories: " + e.getMessage());
        }
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default items");

        try {
            // Sample items - you can expand this list
            String[][] items = {
                    {"Scrambled Eggs", "Breakfast Items", "1"},
                    {"Pancakes", "Breakfast Items", "0"},
                    {"Oatmeal", "Breakfast Items", "1"},
                    {"Grilled Chicken", "Proteins", "1"},
                    {"Baked Fish", "Proteins", "1"},
                    {"Rice", "Starches", "1"},
                    {"Mashed Potatoes", "Starches", "0"},
                    {"Green Beans", "Vegetables", "1"},
                    {"Carrots", "Vegetables", "1"},
                    {"Water", "Beverages", "1"},
                    {"Coffee", "Beverages", "1"},
                    {"Orange Juice", "Juices", "1"},
                    {"Apple Juice", "Juices", "1"},
                    {"Sugar-Free Jello", "Desserts", "1"},
                    {"Regular Jello", "Desserts", "0"},
                    {"Apple", "Fruits", "1"},
                    {"Banana", "Fruits", "1"},
                    {"Milk", "Dairy", "1"},
                    {"Yogurt", "Dairy", "1"}
            };

            for (String[] item : items) {
                ContentValues values = new ContentValues();
                values.put("name", item[0]);
                values.put("category", item[1]);
                values.put("ada_friendly", Integer.parseInt(item[2]));
                db.insert("Item", null, values);
            }

            Log.d(TAG, "Default items inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default items: " + e.getMessage());
        }
    }

    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }
}