package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "DietaryMenu.db";
    private static final int DATABASE_VERSION = 9; // Incremented to force update

    // Table Names
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_PATIENT_INFO = "PatientInfo";
    public static final String TABLE_ITEMS = "Items";
    public static final String TABLE_MEAL_SELECTIONS = "MealSelections";
    public static final String TABLE_DEFAULT_MENU = "DefaultMenu";

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
        createDefaultMenuTable(db);

        insertDefaultUsers(db);
        insertDefaultItems(db);
        insertDefaultMenuItems(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 9) {
            // Fix admin role and force password change
            try {
                db.execSQL("UPDATE " + TABLE_USERS + " SET user_role = 'Admin', force_password_change = 1 WHERE username = 'admin'");
                Log.d(TAG, "Updated admin user role and password change flag");
            } catch (Exception e) {
                Log.e(TAG, "Error updating admin user", e);
            }
        }

        if (oldVersion < 8) {
            createDefaultMenuTable(db);
            insertDefaultMenuItems(db);
        }

        if (oldVersion < 7) {
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
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "last_login DATETIME, " +
                "force_password_change INTEGER DEFAULT 0" +
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
                "diet TEXT, " +
                "ada_diet INTEGER DEFAULT 0, " +
                "fluid_restriction TEXT, " +
                "texture_modifications TEXT, " +
                "mechanical_chopped INTEGER DEFAULT 0, " +
                "mechanical_ground INTEGER DEFAULT 0, " +
                "bite_size INTEGER DEFAULT 0, " +
                "bread_ok INTEGER DEFAULT 0, " +
                "nectar_thick INTEGER DEFAULT 0, " +
                "pudding_thick INTEGER DEFAULT 0, " +
                "honey_thick INTEGER DEFAULT 0, " +
                "extra_gravy INTEGER DEFAULT 0, " +
                "meats_only INTEGER DEFAULT 0, " +
                "breakfast_complete INTEGER DEFAULT 0, " +
                "lunch_complete INTEGER DEFAULT 0, " +
                "dinner_complete INTEGER DEFAULT 0, " +
                "breakfast_npo INTEGER DEFAULT 0, " +
                "lunch_npo INTEGER DEFAULT 0, " +
                "dinner_npo INTEGER DEFAULT 0, " +
                "breakfast_items TEXT, " +
                "lunch_items TEXT, " +
                "dinner_items TEXT, " +
                "breakfast_juices TEXT, " +
                "lunch_juices TEXT, " +
                "dinner_juices TEXT, " +
                "breakfast_drinks TEXT, " +
                "lunch_drinks TEXT, " +
                "dinner_drinks TEXT, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_PATIENT_INFO_TABLE);
        Log.d(TAG, "PatientInfo table created");
    }

    private void createItemsTable(SQLiteDatabase db) {
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + " (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_name TEXT UNIQUE NOT NULL, " +
                "category TEXT, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_ITEMS_TABLE);
        Log.d(TAG, "Items table created");
    }

    private void createMealSelectionsTable(SQLiteDatabase db) {
        String CREATE_MEAL_SELECTIONS_TABLE = "CREATE TABLE " + TABLE_MEAL_SELECTIONS + " (" +
                "selection_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER, " +
                "meal_type TEXT, " +
                "item_id INTEGER, " +
                "selection_date DATE, " +
                "FOREIGN KEY (patient_id) REFERENCES " + TABLE_PATIENT_INFO + "(patient_id), " +
                "FOREIGN KEY (item_id) REFERENCES " + TABLE_ITEMS + "(item_id)" +
                ")";
        db.execSQL(CREATE_MEAL_SELECTIONS_TABLE);
        Log.d(TAG, "MealSelections table created");
    }

    private void createDefaultMenuTable(SQLiteDatabase db) {
        String CREATE_DEFAULT_MENU_TABLE = "CREATE TABLE " + TABLE_DEFAULT_MENU + " (" +
                "menu_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "meal_type TEXT NOT NULL, " +
                "item_name TEXT NOT NULL, " +
                "category TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(meal_type, item_name)" +
                ")";
        db.execSQL(CREATE_DEFAULT_MENU_TABLE);
        Log.d(TAG, "DefaultMenu table created");
    }

    private void addMissingColumnsToPatientInfo(SQLiteDatabase db) {
        try {
            // Add texture modification columns
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
            // Admin user - FIXED: force_password_change = 1 for first login
            ContentValues adminValues = new ContentValues();
            adminValues.put("username", "admin");
            adminValues.put("password", "admin123");
            adminValues.put("full_name", "System Administrator");
            adminValues.put("user_role", "Admin"); // FIXED: Capital 'A' to match role checks
            adminValues.put("is_active", 1);
            adminValues.put("force_password_change", 1); // FIXED: Force password change on first login
            db.insert(TABLE_USERS, null, adminValues);

            // Regular user
            ContentValues userValues = new ContentValues();
            userValues.put("username", "dietary");
            userValues.put("password", "dietary123");
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
                    "Roast Beef", "Mashed Potatoes", "Carrots", "Apple Juice",
                    "Pancakes", "Bacon", "Milk", "Tea", "Soup of the Day",
                    "Salad", "French Fries", "Ice Cream", "Yogurt", "Fresh Fruit"
            };

            for (String item : defaultItems) {
                ContentValues values = new ContentValues();
                values.put("item_name", item);
                values.put("category", categorizeItem(item));
                db.insert(TABLE_ITEMS, null, values);
            }

            Log.d(TAG, "Default items inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default items", e);
        }
    }

    private void insertDefaultMenuItems(SQLiteDatabase db) {
        try {
            // Breakfast items
            String[] breakfastItems = {"Scrambled Eggs", "Toast", "Pancakes", "Bacon", "Orange Juice", "Coffee", "Milk", "Butter", "Jelly"};
            for (String item : breakfastItems) {
                ContentValues values = new ContentValues();
                values.put("meal_type", "Breakfast");
                values.put("item_name", item);
                values.put("category", categorizeItem(item));
                values.put("is_active", 1);
                db.insert(TABLE_DEFAULT_MENU, null, values);
            }

            // Lunch items
            String[] lunchItems = {"Grilled Chicken", "Rice", "Green Beans", "Salad", "Dinner Roll", "Apple Juice", "Tea", "Fresh Fruit"};
            for (String item : lunchItems) {
                ContentValues values = new ContentValues();
                values.put("meal_type", "Lunch");
                values.put("item_name", item);
                values.put("category", categorizeItem(item));
                values.put("is_active", 1);
                db.insert(TABLE_DEFAULT_MENU, null, values);
            }

            // Dinner items
            String[] dinnerItems = {"Roast Beef", "Mashed Potatoes", "Carrots", "Soup of the Day", "Dinner Roll", "Ice Cream", "Coffee", "Tea"};
            for (String item : dinnerItems) {
                ContentValues values = new ContentValues();
                values.put("meal_type", "Dinner");
                values.put("item_name", item);
                values.put("category", categorizeItem(item));
                values.put("is_active", 1);
                db.insert(TABLE_DEFAULT_MENU, null, values);
            }

            Log.d(TAG, "Default menu items inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default menu items", e);
        }
    }

    private String categorizeItem(String itemName) {
        String lowerItem = itemName.toLowerCase();
        if (lowerItem.contains("juice") || lowerItem.contains("coffee") || lowerItem.contains("tea") || lowerItem.contains("milk")) {
            return "Beverage";
        } else if (lowerItem.contains("chicken") || lowerItem.contains("beef") || lowerItem.contains("egg") || lowerItem.contains("bacon")) {
            return "Protein";
        } else if (lowerItem.contains("rice") || lowerItem.contains("potato") || lowerItem.contains("toast") || lowerItem.contains("roll")) {
            return "Starch";
        } else if (lowerItem.contains("beans") || lowerItem.contains("carrot") || lowerItem.contains("salad")) {
            return "Vegetable";
        } else if (lowerItem.contains("fruit") || lowerItem.contains("ice cream") || lowerItem.contains("yogurt")) {
            return "Dessert";
        } else {
            return "Other";
        }
    }
}