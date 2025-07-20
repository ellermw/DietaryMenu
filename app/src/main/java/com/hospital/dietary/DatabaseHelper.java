package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "hospital_dietary.db";
    private static final int DATABASE_VERSION = 3; // UPDATED: Incremented version for schema changes

    // User table
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE User (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'user', " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +
                    "must_change_password INTEGER NOT NULL DEFAULT 0, " + // FEATURE: Password change tracking
                    "created_date TEXT, " +
                    "last_login TEXT" +
                    ")";

    // Patient table with FIXED texture modification columns
    private static final String CREATE_PATIENT_TABLE =
            "CREATE TABLE Patient (" +
                    "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_first_name TEXT NOT NULL, " +
                    "patient_last_name TEXT NOT NULL, " +
                    "wing TEXT NOT NULL, " +
                    "room_number TEXT NOT NULL, " +
                    "diet_type TEXT NOT NULL, " +
                    "ada_diet INTEGER NOT NULL DEFAULT 0, " +
                    "fluid_restriction TEXT, " +
                    "mechanical_chopped INTEGER NOT NULL DEFAULT 0, " + // FIXED: Multiple texture modifications
                    "mechanical_ground INTEGER NOT NULL DEFAULT 0, " +   // FIXED: Multiple texture modifications
                    "bite_size INTEGER NOT NULL DEFAULT 0, " +           // FIXED: Multiple texture modifications
                    "bread_ok INTEGER NOT NULL DEFAULT 0, " +            // FIXED: Multiple texture modifications
                    "breakfast_complete INTEGER NOT NULL DEFAULT 0, " +
                    "lunch_complete INTEGER NOT NULL DEFAULT 0, " +
                    "dinner_complete INTEGER NOT NULL DEFAULT 0, " +
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

    // FinalizedOrder table with FIXED texture modification columns
    private static final String CREATE_FINALIZED_ORDER_TABLE =
            "CREATE TABLE FinalizedOrder (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_name TEXT NOT NULL, " +
                    "wing TEXT NOT NULL, " +
                    "room TEXT NOT NULL, " +
                    "order_date TEXT NOT NULL, " +
                    "diet_type TEXT NOT NULL, " +
                    "fluid_restriction TEXT, " +
                    "mechanical_chopped INTEGER NOT NULL DEFAULT 0, " + // FIXED: Multiple texture modifications
                    "mechanical_ground INTEGER NOT NULL DEFAULT 0, " +  // FIXED: Multiple texture modifications
                    "bite_size INTEGER NOT NULL DEFAULT 0, " +          // FIXED: Multiple texture modifications
                    "bread_ok INTEGER NOT NULL DEFAULT 0, " +           // FIXED: Multiple texture modifications
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

            // Insert default data
            insertDefaultUsers(db);
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
                // FEATURE: Add password change tracking column
                addPasswordChangeColumn(db);
            }

            Log.d(TAG, "Database upgrade completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
            // If upgrade fails, recreate database
            db.execSQL("DROP TABLE IF EXISTS User");
            db.execSQL("DROP TABLE IF EXISTS Patient");
            db.execSQL("DROP TABLE IF EXISTS Item");
            db.execSQL("DROP TABLE IF EXISTS FinalizedOrder");
            onCreate(db);
        }
    }

    // FEATURE: Add password change tracking column for existing databases
    private void addPasswordChangeColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE User ADD COLUMN must_change_password INTEGER NOT NULL DEFAULT 0");
            Log.d(TAG, "Added must_change_password column to User table");
        } catch (Exception e) {
            Log.w(TAG, "Column must_change_password may already exist: " + e.getMessage());
        }
    }

    // FIXED: Add texture modification columns for existing databases
    private void addTextureModificationColumns(SQLiteDatabase db) {
        try {
            // Add columns to Patient table
            db.execSQL("ALTER TABLE Patient ADD COLUMN mechanical_chopped INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE Patient ADD COLUMN mechanical_ground INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE Patient ADD COLUMN bite_size INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE Patient ADD COLUMN bread_ok INTEGER NOT NULL DEFAULT 0");

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
            adminValues.put("must_change_password", 1); // FEATURE: Force password change on first login
            adminValues.put("created_date", getCurrentTimestamp());

            long adminResult = db.insert("User", null, adminValues);

            // Default user
            ContentValues userValues = new ContentValues();
            userValues.put("username", "user");
            userValues.put("password", "user123");
            userValues.put("full_name", "Default User");
            userValues.put("role", "user");
            userValues.put("is_active", 1);
            userValues.put("must_change_password", 1); // FEATURE: Force password change on first login
            userValues.put("created_date", getCurrentTimestamp());

            long userResult = db.insert("User", null, userValues);

            Log.d(TAG, "Default users inserted. Admin ID: " + adminResult + ", User ID: " + userResult);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default users: " + e.getMessage());
        }
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default food items");

        try {
            // Sample food items for each category
            String[][] items = {
                    // Breakfast Items
                    {"Scrambled Eggs", "Breakfast Items", "1"},
                    {"Oatmeal", "Breakfast Items", "1"},
                    {"Whole Wheat Toast", "Breakfast Items", "1"},
                    {"Pancakes", "Breakfast Items", "0"},
                    {"French Toast", "Breakfast Items", "0"},
                    {"Cereal", "Breakfast Items", "1"},
                    {"Yogurt", "Breakfast Items", "1"},
                    {"Fresh Fruit", "Breakfast Items", "1"},

                    // Proteins
                    {"Grilled Chicken Breast", "Proteins", "1"},
                    {"Baked Fish", "Proteins", "1"},
                    {"Lean Beef", "Proteins", "1"},
                    {"Turkey", "Proteins", "1"},
                    {"Tofu", "Proteins", "1"},
                    {"Beans", "Proteins", "1"},
                    {"Cottage Cheese", "Proteins", "1"},

                    // Starches
                    {"Brown Rice", "Starches", "1"},
                    {"White Rice", "Starches", "1"},
                    {"Baked Potato", "Starches", "1"},
                    {"Sweet Potato", "Starches", "1"},
                    {"Quinoa", "Starches", "1"},
                    {"Whole Wheat Pasta", "Starches", "1"},
                    {"Regular Pasta", "Starches", "0"},

                    // Vegetables
                    {"Steamed Broccoli", "Vegetables", "1"},
                    {"Green Beans", "Vegetables", "1"},
                    {"Carrots", "Vegetables", "1"},
                    {"Spinach", "Vegetables", "1"},
                    {"Corn", "Vegetables", "1"},
                    {"Mixed Vegetables", "Vegetables", "1"},

                    // Beverages
                    {"Water", "Beverages", "1"},
                    {"Diet Soda", "Beverages", "1"},
                    {"Regular Soda", "Beverages", "0"},
                    {"Coffee", "Beverages", "1"},
                    {"Tea", "Beverages", "1"},
                    {"Milk (Low-fat)", "Beverages", "1"},
                    {"Milk (Whole)", "Beverages", "0"},

                    // Juices
                    {"Orange Juice (Sugar-free)", "Juices", "1"},
                    {"Orange Juice", "Juices", "0"},
                    {"Apple Juice (Sugar-free)", "Juices", "1"},
                    {"Apple Juice", "Juices", "0"},
                    {"Cranberry Juice", "Juices", "0"},

                    // Desserts
                    {"Sugar-free Jello", "Desserts", "1"},
                    {"Regular Jello", "Desserts", "0"},
                    {"Sugar-free Pudding", "Desserts", "1"},
                    {"Regular Pudding", "Desserts", "0"},
                    {"Fresh Berries", "Desserts", "1"},

                    // Fruits
                    {"Apple", "Fruits", "1"},
                    {"Banana", "Fruits", "1"},
                    {"Orange", "Fruits", "1"},
                    {"Grapes", "Fruits", "1"},
                    {"Berries", "Fruits", "1"},

                    // Dairy
                    {"Low-fat Cheese", "Dairy", "1"},
                    {"Regular Cheese", "Dairy", "0"},
                    {"Greek Yogurt", "Dairy", "1"},
                    {"Ice Cream", "Dairy", "0"}
            };

            for (String[] item : items) {
                ContentValues values = new ContentValues();
                values.put("name", item[0]);
                values.put("category", item[1]);
                values.put("ada_friendly", Integer.parseInt(item[2]));

                db.insert("Item", null, values);
            }

            Log.d(TAG, "Default food items inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default items: " + e.getMessage());
        }
    }

    private String getCurrentTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.d(TAG, "Database opened");
    }
}