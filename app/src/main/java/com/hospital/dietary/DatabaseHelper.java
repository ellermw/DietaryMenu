package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "DietaryMenu.db";
    private static final int DATABASE_VERSION = 8; // UPDATED: Incremented version for new table

    // Table Names
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_PATIENT_INFO = "PatientInfo";
    public static final String TABLE_ITEMS = "Items";
    public static final String TABLE_MEAL_SELECTIONS = "MealSelections";
    public static final String TABLE_DEFAULT_MENU = "DefaultMenu"; // NEW TABLE

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");

        createUsersTable(db);
        createPatientInfoTable(db);
        createItemsTable(db);
        createMealSelectionsTable(db);
        createDefaultMenuTable(db); // NEW

        insertDefaultUsers(db);
        insertDefaultItems(db);
        insertDefaultMenuItems(db); // NEW
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 8) {
            // Add DefaultMenu table for version 8
            createDefaultMenuTable(db);
            insertDefaultMenuItems(db);
        }

        // Handle other version upgrades as needed
        if (oldVersion < 7) {
            // Add any missing columns to existing tables
            addMissingColumnsToPatientInfo(db);
        }
    }

    private void createUsersTable(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT, " +
                "user_role TEXT DEFAULT 'User', " +
                "is_active INTEGER DEFAULT 1, " +
                "force_password_change INTEGER DEFAULT 0, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "last_login TEXT" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);
        Log.d(TAG, "Users table created");
    }

    private void createPatientInfoTable(SQLiteDatabase db) {
        String CREATE_PATIENT_INFO_TABLE = "CREATE TABLE " + TABLE_PATIENT_INFO + " (" +
                "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_first_name TEXT NOT NULL, " +
                "patient_last_name TEXT NOT NULL, " +
                "wing TEXT NOT NULL, " +
                "room_number TEXT NOT NULL, " +
                "diet_type TEXT, " +
                "diet TEXT NOT NULL, " +
                "ada_diet INTEGER DEFAULT 0, " +
                "fluid_restriction TEXT, " +
                "texture_modifications TEXT, " +

                // Texture modification fields
                "mechanical_chopped INTEGER DEFAULT 0, " +
                "mechanical_ground INTEGER DEFAULT 0, " +
                "bite_size INTEGER DEFAULT 0, " +
                "bread_ok INTEGER DEFAULT 0, " +
                "nectar_thick INTEGER DEFAULT 0, " +
                "pudding_thick INTEGER DEFAULT 0, " +
                "honey_thick INTEGER DEFAULT 0, " +
                "extra_gravy INTEGER DEFAULT 0, " +
                "meats_only INTEGER DEFAULT 0, " +

                // Meal completion flags
                "breakfast_complete INTEGER DEFAULT 0, " +
                "lunch_complete INTEGER DEFAULT 0, " +
                "dinner_complete INTEGER DEFAULT 0, " +
                "breakfast_npo INTEGER DEFAULT 0, " +
                "lunch_npo INTEGER DEFAULT 0, " +
                "dinner_npo INTEGER DEFAULT 0, " +

                // Meal items
                "breakfast_items TEXT, " +
                "lunch_items TEXT, " +
                "dinner_items TEXT, " +
                "breakfast_juices TEXT, " +
                "lunch_juices TEXT, " +
                "dinner_juices TEXT, " +
                "breakfast_drinks TEXT, " +
                "lunch_drinks TEXT, " +
                "dinner_drinks TEXT, " +

                "created_date TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_PATIENT_INFO_TABLE);
        Log.d(TAG, "PatientInfo table created");
    }

    private void createItemsTable(SQLiteDatabase db) {
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + " (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_name TEXT NOT NULL, " +
                "category TEXT, " +
                "description TEXT, " +
                "is_ada_friendly INTEGER DEFAULT 0, " +
                "is_active INTEGER DEFAULT 1, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_ITEMS_TABLE);
        Log.d(TAG, "Items table created");
    }

    private void createMealSelectionsTable(SQLiteDatabase db) {
        String CREATE_MEAL_SELECTIONS_TABLE = "CREATE TABLE " + TABLE_MEAL_SELECTIONS + " (" +
                "selection_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER, " +
                "item_id INTEGER, " +
                "meal_type TEXT, " +
                "selection_date TEXT, " +
                "FOREIGN KEY(patient_id) REFERENCES " + TABLE_PATIENT_INFO + "(patient_id), " +
                "FOREIGN KEY(item_id) REFERENCES " + TABLE_ITEMS + "(item_id)" +
                ")";
        db.execSQL(CREATE_MEAL_SELECTIONS_TABLE);
        Log.d(TAG, "MealSelections table created");
    }

    // NEW: Create DefaultMenu table
    private void createDefaultMenuTable(SQLiteDatabase db) {
        String CREATE_DEFAULT_MENU_TABLE = "CREATE TABLE " + TABLE_DEFAULT_MENU + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_id INTEGER DEFAULT 0, " +
                "item_name TEXT NOT NULL, " +
                "diet_type TEXT NOT NULL, " +
                "meal_type TEXT NOT NULL, " +
                "day_of_week TEXT NOT NULL, " +
                "description TEXT, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(item_name, diet_type, meal_type, day_of_week)" +
                ")";
        db.execSQL(CREATE_DEFAULT_MENU_TABLE);
        Log.d(TAG, "DefaultMenu table created");
    }

    private void addMissingColumnsToPatientInfo(SQLiteDatabase db) {
        try {
            // Add any missing texture modification columns
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN nectar_thick INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN pudding_thick INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN honey_thick INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN extra_gravy INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN meats_only INTEGER DEFAULT 0");
            Log.d(TAG, "Added missing columns to PatientInfo table");
        } catch (Exception e) {
            Log.w(TAG, "Some columns may already exist: " + e.getMessage());
        }
    }

    private void insertDefaultUsers(SQLiteDatabase db) {
        try {
            // Admin user
            ContentValues adminValues = new ContentValues();
            adminValues.put("username", "admin");
            adminValues.put("password", "admin123"); // In production, this should be hashed
            adminValues.put("full_name", "System Administrator");
            adminValues.put("user_role", "Admin");
            adminValues.put("is_active", 1);
            adminValues.put("force_password_change", 0);
            db.insert(TABLE_USERS, null, adminValues);

            // Regular user
            ContentValues userValues = new ContentValues();
            userValues.put("username", "dietary");
            userValues.put("password", "dietary123"); // In production, this should be hashed
            userValues.put("full_name", "Dietary Staff");
            userValues.put("user_role", "User");
            userValues.put("is_active", 1);
            userValues.put("force_password_change", 0);
            db.insert(TABLE_USERS, null, userValues);

            Log.d(TAG, "Default users inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default users", e);
        }
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        try {
            String[] defaultItems = {
                    "Scrambled Eggs", "Toast", "Orange Juice", "Coffee", "Butter", "Jelly",
                    "Grilled Chicken", "Rice", "Green Beans", "Dinner Roll",
                    "Roast Beef", "Mashed Potatoes", "Carrots", "Chocolate Cake",
                    "Baked Fish", "Steamed Vegetables", "Salad", "Fruit Cup"
            };

            for (String itemName : defaultItems) {
                ContentValues values = new ContentValues();
                values.put("item_name", itemName);
                values.put("category", "Main");
                values.put("is_ada_friendly", 1);
                values.put("is_active", 1);
                db.insert(TABLE_ITEMS, null, values);
            }

            Log.d(TAG, "Default food items inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default items", e);
        }
    }

    // NEW: Insert default menu configurations
    private void insertDefaultMenuItems(SQLiteDatabase db) {
        try {
            // Insert system default menu items for different diet types and meals
            insertDefaultMenuForDiet(db, "Regular");
            insertDefaultMenuForDiet(db, "ADA");
            insertDefaultMenuForDiet(db, "Cardiac");

            Log.d(TAG, "Default menu items inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default menu items", e);
        }
    }

    private void insertDefaultMenuForDiet(SQLiteDatabase db, String dietType) {
        // Breakfast items (same for all days)
        insertDefaultMenuItem(db, "Scrambled Eggs", dietType, "Breakfast", "All Days");
        insertDefaultMenuItem(db, "Toast", dietType, "Breakfast", "All Days");
        insertDefaultMenuItem(db, "Orange Juice", dietType, "Breakfast", "All Days");
        insertDefaultMenuItem(db, "Coffee", dietType, "Breakfast", "All Days");

        // Lunch items (day-specific)
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (String day : daysOfWeek) {
            if ("Regular".equals(dietType)) {
                insertDefaultMenuItem(db, "Grilled Chicken", dietType, "Lunch", day);
                insertDefaultMenuItem(db, "Rice", dietType, "Lunch", day);
                insertDefaultMenuItem(db, "Green Beans", dietType, "Lunch", day);
            } else if ("ADA".equals(dietType)) {
                insertDefaultMenuItem(db, "Grilled Fish", dietType, "Lunch", day);
                insertDefaultMenuItem(db, "Brown Rice", dietType, "Lunch", day);
                insertDefaultMenuItem(db, "Steamed Broccoli", dietType, "Lunch", day);
            } else if ("Cardiac".equals(dietType)) {
                insertDefaultMenuItem(db, "Baked Salmon", dietType, "Lunch", day);
                insertDefaultMenuItem(db, "Quinoa", dietType, "Lunch", day);
                insertDefaultMenuItem(db, "Steamed Vegetables", dietType, "Lunch", day);
            }

            // Dinner items (day-specific)
            if ("Regular".equals(dietType)) {
                insertDefaultMenuItem(db, "Roast Beef", dietType, "Dinner", day);
                insertDefaultMenuItem(db, "Mashed Potatoes", dietType, "Dinner", day);
                insertDefaultMenuItem(db, "Carrots", dietType, "Dinner", day);
            } else if ("ADA".equals(dietType)) {
                insertDefaultMenuItem(db, "Lean Turkey", dietType, "Dinner", day);
                insertDefaultMenuItem(db, "Sweet Potato", dietType, "Dinner", day);
                insertDefaultMenuItem(db, "Asparagus", dietType, "Dinner", day);
            } else if ("Cardiac".equals(dietType)) {
                insertDefaultMenuItem(db, "Grilled Tilapia", dietType, "Dinner", day);
                insertDefaultMenuItem(db, "Wild Rice", dietType, "Dinner", day);
                insertDefaultMenuItem(db, "Steamed Spinach", dietType, "Dinner", day);
            }
        }
    }

    private void insertDefaultMenuItem(SQLiteDatabase db, String itemName, String dietType, String mealType, String dayOfWeek) {
        ContentValues values = new ContentValues();
        values.put("item_name", itemName);
        values.put("diet_type", dietType);
        values.put("meal_type", mealType);
        values.put("day_of_week", dayOfWeek);
        values.put("description", "System default item for " + dietType + " diet");

        try {
            db.insert(TABLE_DEFAULT_MENU, null, values);
        } catch (Exception e) {
            Log.w(TAG, "Default menu item may already exist: " + itemName + " for " + dietType + " " + mealType + " " + dayOfWeek);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}