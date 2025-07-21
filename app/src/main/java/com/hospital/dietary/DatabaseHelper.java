package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "DietaryManagement.db";
    private static final int DATABASE_VERSION = 7; // FIXED: Incremented for new features

    // User table
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE User (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +
                    "must_change_password INTEGER NOT NULL DEFAULT 0, " +
                    "created_date TEXT, " +
                    "last_login TEXT" +
                    ")";

    // Category table
    private static final String CREATE_CATEGORY_TABLE =
            "CREATE TABLE Category (" +
                    "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "description TEXT, " +
                    "sort_order INTEGER DEFAULT 0" +
                    ")";

    // FIXED: Patient table with all texture modification fields
    private static final String CREATE_PATIENT_TABLE =
            "CREATE TABLE PatientInfo (" +
                    "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_first_name TEXT NOT NULL, " +
                    "patient_last_name TEXT NOT NULL, " +
                    "wing TEXT NOT NULL, " +
                    "room_number TEXT NOT NULL, " +
                    "diet_type TEXT NOT NULL, " +
                    "ada_diet INTEGER NOT NULL DEFAULT 0, " +
                    "fluid_restriction TEXT, " +
                    "mechanical_chopped INTEGER NOT NULL DEFAULT 0, " +
                    "mechanical_ground INTEGER NOT NULL DEFAULT 0, " +
                    "bite_size INTEGER NOT NULL DEFAULT 0, " +
                    "bread_ok INTEGER NOT NULL DEFAULT 0, " +
                    "nectar_thick INTEGER NOT NULL DEFAULT 0, " +
                    "pudding_thick INTEGER NOT NULL DEFAULT 0, " +
                    "honey_thick INTEGER NOT NULL DEFAULT 0, " +
                    "extra_gravy INTEGER NOT NULL DEFAULT 0, " +
                    "meats_only INTEGER NOT NULL DEFAULT 0, " +
                    "breakfast_complete INTEGER NOT NULL DEFAULT 0, " +
                    "lunch_complete INTEGER NOT NULL DEFAULT 0, " +
                    "dinner_complete INTEGER NOT NULL DEFAULT 0, " +
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
                    "description TEXT, " +
                    "ada_friendly INTEGER NOT NULL DEFAULT 0, " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +
                    "created_date TEXT" +
                    ")";

    // FIXED: NEW DefaultMenu table for storing default menu configurations
    private static final String CREATE_DEFAULT_MENU_TABLE =
            "CREATE TABLE DefaultMenu (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "item_id INTEGER, " +
                    "item_name TEXT NOT NULL, " +
                    "diet_type TEXT NOT NULL, " +  // Regular, ADA, Cardiac
                    "meal_type TEXT NOT NULL, " +   // Breakfast, Lunch, Dinner
                    "day_of_week TEXT NOT NULL, " + // Monday-Sunday, or "All Days" for breakfast
                    "description TEXT, " +
                    "created_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(item_id) REFERENCES Item(item_id), " +
                    "UNIQUE(item_id, diet_type, meal_type, day_of_week)" +
                    ");";

    // FIXED: Finalized Order table
    private static final String CREATE_FINALIZED_ORDER_TABLE =
            "CREATE TABLE FinalizedOrder (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER NOT NULL, " +
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
                    "nectar_thick INTEGER NOT NULL DEFAULT 0, " +
                    "pudding_thick INTEGER NOT NULL DEFAULT 0, " +
                    "honey_thick INTEGER NOT NULL DEFAULT 0, " +
                    "extra_gravy INTEGER NOT NULL DEFAULT 0, " +
                    "meats_only INTEGER NOT NULL DEFAULT 0, " +
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
        try {
            Log.d(TAG, "Creating database tables");

            // Create all tables
            db.execSQL(CREATE_USER_TABLE);
            db.execSQL(CREATE_CATEGORY_TABLE);
            db.execSQL(CREATE_PATIENT_TABLE);
            db.execSQL(CREATE_ITEM_TABLE);
            db.execSQL(CREATE_DEFAULT_MENU_TABLE); // FIXED: New table
            db.execSQL(CREATE_FINALIZED_ORDER_TABLE);

            // Insert default data
            insertDefaultUsers(db);
            insertDefaultCategories(db);
            insertDefaultItems(db);
            insertDefaultMenuItems(db); // FIXED: Insert default menu items

            Log.d(TAG, "Database created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating database", e);
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        try {
            if (oldVersion < 4) {
                // Add texture modification columns
                addTextureModificationColumns(db);
            }

            if (oldVersion < 5) {
                // Migrate Patient table to PatientInfo
                migratePatientTable(db);
            }

            if (oldVersion < 6) {
                // FIXED: Add new texture modification columns
                addNewTextureModificationColumns(db);
            }

            if (oldVersion < 7) {
                // FIXED: Add DefaultMenu table
                createDefaultMenuTable(db);
                insertDefaultMenuItems(db);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
            // If upgrade fails, recreate database
            dropAllTables(db);
            onCreate(db);
        }
    }

    // FIXED: Create DefaultMenu table for existing databases
    private void createDefaultMenuTable(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_DEFAULT_MENU_TABLE);
            Log.d(TAG, "Created DefaultMenu table");
        } catch (Exception e) {
            Log.e(TAG, "Error creating DefaultMenu table", e);
        }
    }

    // FIXED: Add new texture modification columns for version 6+
    private void addNewTextureModificationColumns(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Adding new texture modification columns");

            // Check which table exists (Patient or PatientInfo)
            String tableName = tableExists(db, "PatientInfo") ? "PatientInfo" : "Patient";

            // Add missing columns to patient table
            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN nectar_thick INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN pudding_thick INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN honey_thick INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN extra_gravy INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN meats_only INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}

            // Add columns to FinalizedOrder table if it exists
            if (tableExists(db, "FinalizedOrder")) {
                try { db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN nectar_thick INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
                try { db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN pudding_thick INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
                try { db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN honey_thick INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
                try { db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN extra_gravy INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
                try { db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN meats_only INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
            }

            Log.d(TAG, "Added new texture modification columns");
        } catch (Exception e) {
            Log.w(TAG, "Some texture modification columns may already exist: " + e.getMessage());
        }
    }

    // FIXED: Add texture modification columns for older versions
    private void addTextureModificationColumns(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Adding basic texture modification columns");

            String tableName = tableExists(db, "PatientInfo") ? "PatientInfo" : "Patient";

            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN mechanical_chopped INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN mechanical_ground INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN bite_size INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}
            try { db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN bread_ok INTEGER NOT NULL DEFAULT 0"); } catch (Exception e) {}

            Log.d(TAG, "Added basic texture modification columns");
        } catch (Exception e) {
            Log.w(TAG, "Basic texture modification columns may already exist: " + e.getMessage());
        }
    }

    private void migratePatientTable(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Migrating Patient table to PatientInfo");

            // Check if PatientInfo table already exists
            if (tableExists(db, "PatientInfo")) {
                Log.d(TAG, "PatientInfo table already exists, skipping migration");
                return;
            }

            // Check if Patient table exists
            if (tableExists(db, "Patient")) {
                // Create new PatientInfo table
                db.execSQL(CREATE_PATIENT_TABLE);

                // Migrate data from Patient to PatientInfo
                db.execSQL("INSERT INTO PatientInfo (patient_id, patient_first_name, patient_last_name, wing, room_number, " +
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

                // Drop old Patient table
                db.execSQL("DROP TABLE Patient");
                Log.d(TAG, "Successfully migrated Patient table to PatientInfo");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error migrating Patient table", e);
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void dropAllTables(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS User");
            db.execSQL("DROP TABLE IF EXISTS Category");
            db.execSQL("DROP TABLE IF EXISTS Patient");
            db.execSQL("DROP TABLE IF EXISTS PatientInfo");
            db.execSQL("DROP TABLE IF EXISTS Item");
            db.execSQL("DROP TABLE IF EXISTS DefaultMenu");
            db.execSQL("DROP TABLE IF EXISTS FinalizedOrder");
            Log.d(TAG, "Dropped all tables");
        } catch (Exception e) {
            Log.e(TAG, "Error dropping tables", e);
        }
    }

    // FIXED: Insert default menu items
    private void insertDefaultMenuItems(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default menu items");

        try {
            // Regular Diet - Breakfast (applies to all days)
            insertDefaultMenuItem(db, "Scrambled Eggs", "Regular", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Toast", "Regular", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Orange Juice", "Regular", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Coffee", "Regular", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Butter", "Regular", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Jelly", "Regular", "Breakfast", "All Days");

            // Regular Diet - Lunch (Monday example)
            insertDefaultMenuItem(db, "Grilled Chicken", "Regular", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Rice", "Regular", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Green Beans", "Regular", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Dinner Roll", "Regular", "Lunch", "Monday");

            // Regular Diet - Dinner (Monday example)
            insertDefaultMenuItem(db, "Roast Beef", "Regular", "Dinner", "Monday");
            insertDefaultMenuItem(db, "Roasted Potatoes", "Regular", "Dinner", "Monday");
            insertDefaultMenuItem(db, "Corn", "Regular", "Dinner", "Monday");
            insertDefaultMenuItem(db, "Chocolate Cake", "Regular", "Dinner", "Monday");

            // ADA Diet - Breakfast
            insertDefaultMenuItem(db, "Egg White Omelet", "ADA", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Whole Wheat Toast", "ADA", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Sugar-Free Orange Juice", "ADA", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Black Coffee", "ADA", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Sugar-Free Jelly", "ADA", "Breakfast", "All Days");

            // ADA Diet - Lunch (Monday example)
            insertDefaultMenuItem(db, "Grilled Fish", "ADA", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Brown Rice", "ADA", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Steamed Broccoli", "ADA", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Sugar-Free Dessert", "ADA", "Lunch", "Monday");

            // Cardiac Diet - Breakfast
            insertDefaultMenuItem(db, "Oatmeal", "Cardiac", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Fresh Fruit", "Cardiac", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Low-Sodium Orange Juice", "Cardiac", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Herbal Tea", "Cardiac", "Breakfast", "All Days");
            insertDefaultMenuItem(db, "Low-Fat Milk", "Cardiac", "Breakfast", "All Days");

            // Cardiac Diet - Lunch (Monday example)
            insertDefaultMenuItem(db, "Baked Salmon", "Cardiac", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Quinoa", "Cardiac", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Steamed Vegetables", "Cardiac", "Lunch", "Monday");
            insertDefaultMenuItem(db, "Low-Sodium Broth", "Cardiac", "Lunch", "Monday");

            Log.d(TAG, "Default menu items inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default menu items", e);
        }
    }

    private void insertDefaultMenuItem(SQLiteDatabase db, String itemName, String dietType, String mealType, String dayOfWeek) {
        ContentValues values = new ContentValues();
        values.put("item_id", 0); // You can link to actual items later
        values.put("item_name", itemName);
        values.put("diet_type", dietType);
        values.put("meal_type", mealType);
        values.put("day_of_week", dayOfWeek);
        values.put("created_date", getCurrentTimestamp());

        long result = db.insert("DefaultMenu", null, values);
        if (result == -1) {
            Log.w(TAG, "Failed to insert default menu item: " + itemName);
        }
    }

    private void insertDefaultUsers(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Inserting default users");

            // Admin user
            ContentValues adminValues = new ContentValues();
            adminValues.put("username", "admin");
            adminValues.put("password", "admin123"); // In production, hash this
            adminValues.put("full_name", "System Administrator");
            adminValues.put("role", "admin");
            adminValues.put("is_active", 1);
            adminValues.put("must_change_password", 0);
            adminValues.put("created_date", getCurrentTimestamp());
            db.insert("User", null, adminValues);

            // Regular user
            ContentValues userValues = new ContentValues();
            userValues.put("username", "user");
            userValues.put("password", "user123"); // In production, hash this
            userValues.put("full_name", "Regular User");
            userValues.put("role", "user");
            userValues.put("is_active", 1);
            userValues.put("must_change_password", 0);
            userValues.put("created_date", getCurrentTimestamp());
            db.insert("User", null, userValues);

            Log.d(TAG, "Default users inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default users", e);
        }
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Inserting default categories");

            String[] categories = {
                    "Breakfast Items", "Proteins", "Starches", "Vegetables",
                    "Beverages", "Juices", "Desserts", "Fruits", "Dairy"
            };

            for (int i = 0; i < categories.length; i++) {
                ContentValues values = new ContentValues();
                values.put("name", categories[i]);
                values.put("description", "Default " + categories[i] + " category");
                values.put("sort_order", i + 1);
                db.insert("Category", null, values);
            }

            Log.d(TAG, "Default categories inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default categories", e);
        }
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Inserting default items");

            // Breakfast Items
            insertDefaultItem(db, "Scrambled Eggs", "Breakfast Items", "Protein-rich breakfast option", true);
            insertDefaultItem(db, "Toast", "Breakfast Items", "Bread for breakfast", true);
            insertDefaultItem(db, "Pancakes", "Breakfast Items", "Sweet breakfast option", false);
            insertDefaultItem(db, "Oatmeal", "Breakfast Items", "Heart-healthy breakfast", true);

            // Proteins
            insertDefaultItem(db, "Grilled Chicken", "Proteins", "Lean protein source", true);
            insertDefaultItem(db, "Baked Fish", "Proteins", "Heart-healthy protein", true);
            insertDefaultItem(db, "Roast Beef", "Proteins", "Traditional protein option", false);

            // Starches
            insertDefaultItem(db, "Rice", "Starches", "Basic starch option", true);
            insertDefaultItem(db, "Mashed Potatoes", "Starches", "Comfort food starch", false);
            insertDefaultItem(db, "Brown Rice", "Starches", "Whole grain option", true);

            // Vegetables
            insertDefaultItem(db, "Green Beans", "Vegetables", "Fresh vegetable option", true);
            insertDefaultItem(db, "Carrots", "Vegetables", "Vitamin A rich vegetable", true);
            insertDefaultItem(db, "Broccoli", "Vegetables", "Nutrient-dense vegetable", true);

            // Beverages
            insertDefaultItem(db, "Coffee", "Beverages", "Morning beverage", false);
            insertDefaultItem(db, "Tea", "Beverages", "Afternoon beverage", true);
            insertDefaultItem(db, "Water", "Beverages", "Essential hydration", true);

            // Juices
            insertDefaultItem(db, "Orange Juice", "Juices", "Vitamin C rich juice", false);
            insertDefaultItem(db, "Apple Juice", "Juices", "Sweet fruit juice", false);
            insertDefaultItem(db, "Sugar-Free Orange Juice", "Juices", "ADA-friendly juice", true);

            Log.d(TAG, "Default items inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default items", e);
        }
    }

    private void insertDefaultItem(SQLiteDatabase db, String name, String category, String description, boolean adaFriendly) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("description", description);
        values.put("ada_friendly", adaFriendly ? 1 : 0);
        values.put("is_active", 1);
        values.put("created_date", getCurrentTimestamp());
        db.insert("Item", null, values);
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}