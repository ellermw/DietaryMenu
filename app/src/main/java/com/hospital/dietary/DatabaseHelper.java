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
    private static final int DATABASE_VERSION = 6; // FIXED: Incremented for new features

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

    // FIXED: Patient table with all new texture modification fields
    private static final String CREATE_PATIENT_TABLE =
            "CREATE TABLE PatientInfo (" +
                    "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_first_name TEXT NOT NULL, " +
                    "patient_last_name TEXT NOT NULL, " +
                    "wing TEXT NOT NULL, " +
                    "room_number TEXT NOT NULL, " +
                    "diet_type TEXT NOT NULL, " +
                    "diet TEXT, " +
                    "ada_diet INTEGER NOT NULL DEFAULT 0, " +
                    "fluid_restriction TEXT, " +
                    "texture_modifications TEXT, " +
                    "mechanical_chopped INTEGER NOT NULL DEFAULT 0, " +
                    "mechanical_ground INTEGER NOT NULL DEFAULT 0, " +
                    "bite_size INTEGER NOT NULL DEFAULT 0, " +
                    "bread_ok INTEGER NOT NULL DEFAULT 1, " +
                    "nectar_thick INTEGER NOT NULL DEFAULT 0, " +
                    "pudding_thick INTEGER NOT NULL DEFAULT 0, " +
                    "honey_thick INTEGER NOT NULL DEFAULT 0, " +
                    "extra_gravy INTEGER NOT NULL DEFAULT 0, " +
                    "meats_only INTEGER NOT NULL DEFAULT 0, " +
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
                    "size_ml INTEGER DEFAULT 0, " +
                    "description TEXT, " +
                    "is_ada_friendly INTEGER NOT NULL DEFAULT 0, " +
                    "is_soda INTEGER NOT NULL DEFAULT 0, " +
                    "is_clear_liquid INTEGER NOT NULL DEFAULT 0, " +
                    "meal_type TEXT, " +
                    "is_default INTEGER NOT NULL DEFAULT 0, " +
                    "ada_friendly INTEGER NOT NULL DEFAULT 0" +
                    ")";

    // FIXED: New DefaultMenuItems table for the admin feature
    private static final String CREATE_DEFAULT_MENU_TABLE =
            "CREATE TABLE DefaultMenuItems (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "diet_type TEXT NOT NULL, " +
                    "day_of_week TEXT NOT NULL, " +
                    "meal_type TEXT NOT NULL, " +
                    "item_name TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "description TEXT, " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +
                    "sort_order INTEGER NOT NULL DEFAULT 0, " +
                    "created_date TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    // FinalizedOrder table with updated texture modifications
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
                // FIXED: Add new texture modification columns and DefaultMenuItems table
                addNewTextureModificationColumns(db);
                createDefaultMenuItemsTable(db);
                insertDefaultMenuItems(db);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
            // If upgrade fails, recreate database
            dropAllTables(db);
            onCreate(db);
        }
    }

    // FIXED: Add new texture modification columns for version 6
    private void addNewTextureModificationColumns(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Adding new texture modification columns");

            // Check which table exists (Patient or PatientInfo)
            String tableName = tableExists(db, "PatientInfo") ? "PatientInfo" : "Patient";

            // Add new texture modification columns
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN nectar_thick INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN pudding_thick INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN honey_thick INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN extra_gravy INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN meats_only INTEGER NOT NULL DEFAULT 0");

            // Add columns to FinalizedOrder table
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN nectar_thick INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN pudding_thick INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN honey_thick INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN extra_gravy INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE FinalizedOrder ADD COLUMN meats_only INTEGER NOT NULL DEFAULT 0");

            Log.d(TAG, "Added new texture modification columns");
        } catch (Exception e) {
            Log.w(TAG, "New texture modification columns may already exist: " + e.getMessage());
        }
    }

    // FIXED: Create DefaultMenuItems table for version 6
    private void createDefaultMenuItemsTable(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Creating DefaultMenuItems table");

            if (!tableExists(db, "DefaultMenuItems")) {
                db.execSQL(CREATE_DEFAULT_MENU_TABLE);
                Log.d(TAG, "DefaultMenuItems table created successfully");
            } else {
                Log.d(TAG, "DefaultMenuItems table already exists");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating DefaultMenuItems table", e);
        }
    }

    private void addTextureModificationColumns(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Adding texture modification columns");

            // Check which table exists (Patient or PatientInfo)
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
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = ?", new String[]{tableName});
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if table exists: " + tableName, e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void dropAllTables(SQLiteDatabase db) {
        Log.d(TAG, "Dropping all tables");
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS Category");
        db.execSQL("DROP TABLE IF EXISTS Patient");
        db.execSQL("DROP TABLE IF EXISTS PatientInfo");
        db.execSQL("DROP TABLE IF EXISTS Item");
        db.execSQL("DROP TABLE IF EXISTS DefaultMenuItems");
        db.execSQL("DROP TABLE IF EXISTS FinalizedOrder");
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

            Log.d(TAG, "Default users inserted. Admin ID: " + adminResult + ", User ID: " + userResult);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default users", e);
        }
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default categories");

        String[] categories = {
                "Breakfast Items", "Proteins", "Starches", "Vegetables",
                "Beverages", "Juices", "Desserts", "Fruits", "Dairy"
        };

        try {
            for (int i = 0; i < categories.length; i++) {
                ContentValues values = new ContentValues();
                values.put("name", categories[i]);
                values.put("description", "Default category for " + categories[i]);
                values.put("sort_order", i);

                db.insert("Category", null, values);
            }
            Log.d(TAG, "Default categories inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default categories", e);
        }
    }

    // FIXED: Insert default menu items for the new feature
    private void insertDefaultMenuItems(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default menu items");

        try {
            // Regular Diet Breakfast (applies to all days)
            insertDefaultMenuItem(db, "Regular", "All Days", "Breakfast", "Scrambled Eggs", "Protein", "", 0);
            insertDefaultMenuItem(db, "Regular", "All Days", "Breakfast", "Toast", "Starch", "", 1);
            insertDefaultMenuItem(db, "Regular", "All Days", "Breakfast", "Orange Juice", "Beverage", "", 2);
            insertDefaultMenuItem(db, "Regular", "All Days", "Breakfast", "Coffee", "Beverage", "", 3);
            insertDefaultMenuItem(db, "Regular", "All Days", "Breakfast", "Butter", "Condiment", "", 4);
            insertDefaultMenuItem(db, "Regular", "All Days", "Breakfast", "Whole Milk", "Dairy", "", 5);

            // ADA Diet Breakfast (applies to all days)
            insertDefaultMenuItem(db, "ADA", "All Days", "Breakfast", "Scrambled Eggs", "Protein", "", 0);
            insertDefaultMenuItem(db, "ADA", "All Days", "Breakfast", "Whole Wheat Toast", "Starch", "", 1);
            insertDefaultMenuItem(db, "ADA", "All Days", "Breakfast", "Sugar-Free Orange Juice", "Beverage", "", 2);
            insertDefaultMenuItem(db, "ADA", "All Days", "Breakfast", "Coffee", "Beverage", "", 3);
            insertDefaultMenuItem(db, "ADA", "All Days", "Breakfast", "Sugar-Free Syrup", "Condiment", "", 4);
            insertDefaultMenuItem(db, "ADA", "All Days", "Breakfast", "2% Milk", "Dairy", "", 5);

            // Cardiac Diet Breakfast (applies to all days)
            insertDefaultMenuItem(db, "Cardiac", "All Days", "Breakfast", "Egg Whites", "Protein", "", 0);
            insertDefaultMenuItem(db, "Cardiac", "All Days", "Breakfast", "Whole Grain Toast", "Starch", "", 1);
            insertDefaultMenuItem(db, "Cardiac", "All Days", "Breakfast", "Fresh Orange Juice", "Beverage", "", 2);
            insertDefaultMenuItem(db, "Cardiac", "All Days", "Breakfast", "Coffee", "Beverage", "", 3);
            insertDefaultMenuItem(db, "Cardiac", "All Days", "Breakfast", "Low-Sodium Butter", "Condiment", "", 4);
            insertDefaultMenuItem(db, "Cardiac", "All Days", "Breakfast", "Fresh Fruit", "Fruit", "", 5);

            // Add some lunch and dinner examples for Monday
            String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

            for (String day : daysOfWeek) {
                // Regular Diet Lunch
                insertDefaultMenuItem(db, "Regular", day, "Lunch", "Grilled Chicken", "Main Dish", "", 0);
                insertDefaultMenuItem(db, "Regular", day, "Lunch", "Rice", "Starch", "", 1);
                insertDefaultMenuItem(db, "Regular", day, "Lunch", "Green Beans", "Vegetable", "", 2);
                insertDefaultMenuItem(db, "Regular", day, "Lunch", "Ice Cream", "Dessert", "", 3);

                // Regular Diet Dinner
                insertDefaultMenuItem(db, "Regular", day, "Dinner", "Baked Fish", "Main Dish", "", 0);
                insertDefaultMenuItem(db, "Regular", day, "Dinner", "Mashed Potatoes", "Starch", "", 1);
                insertDefaultMenuItem(db, "Regular", day, "Dinner", "Broccoli", "Vegetable", "", 2);
                insertDefaultMenuItem(db, "Regular", day, "Dinner", "Chocolate Cake", "Dessert", "", 3);
            }

            Log.d(TAG, "Default menu items inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default menu items", e);
        }
    }

    private void insertDefaultMenuItem(SQLiteDatabase db, String dietType, String dayOfWeek, String mealType,
                                       String itemName, String category, String description, int sortOrder) {
        ContentValues values = new ContentValues();
        values.put("diet_type", dietType);
        values.put("day_of_week", dayOfWeek);
        values.put("meal_type", mealType);
        values.put("item_name", itemName);
        values.put("category", category);
        values.put("description", description);
        values.put("is_active", 1);
        values.put("sort_order", sortOrder);
        values.put("created_date", getCurrentTimestamp());

        db.insert("DefaultMenuItems", null, values);
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default items");

        try {
            // Sample items - you can expand this list
            String[][] items = {
                    // Format: {name, category, description, ada_friendly}
                    {"Orange Juice", "Beverages", "Fresh squeezed orange juice", "0"},
                    {"Apple Juice", "Beverages", "100% apple juice", "0"},
                    {"Coffee", "Beverages", "Fresh brewed coffee", "1"},
                    {"Tea", "Beverages", "Hot tea", "1"},
                    {"Water", "Beverages", "Filtered water", "1"},
                    {"Grilled Chicken", "Proteins", "Seasoned grilled chicken breast", "1"},
                    {"Baked Fish", "Proteins", "Fresh baked fish fillet", "1"},
                    {"Scrambled Eggs", "Proteins", "Fresh scrambled eggs", "1"},
                    {"Rice", "Starches", "Steamed white rice", "1"},
                    {"Mashed Potatoes", "Starches", "Creamy mashed potatoes", "0"},
                    {"Green Beans", "Vegetables", "Fresh steamed green beans", "1"},
                    {"Broccoli", "Vegetables", "Fresh steamed broccoli", "1"},
                    {"Ice Cream", "Desserts", "Vanilla ice cream", "0"},
                    {"Sugar-Free Pudding", "Desserts", "Chocolate sugar-free pudding", "1"}
            };

            for (String[] item : items) {
                ContentValues values = new ContentValues();
                values.put("name", item[0]);
                values.put("category", item[1]);
                values.put("description", item[2]);
                values.put("ada_friendly", Integer.parseInt(item[3]));
                values.put("is_default", 1);

                db.insert("Item", null, values);
            }

            Log.d(TAG, "Default items inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default items", e);
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}