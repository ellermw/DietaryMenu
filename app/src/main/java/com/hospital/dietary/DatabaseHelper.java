package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "HospitalDietaryDB";
    private static final int DATABASE_VERSION = 9; // Updated version

    // Table names
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_PATIENT_INFO = "PatientInfo";
    public static final String TABLE_ITEMS = "Items";
    public static final String TABLE_MEAL_ORDERS = "MealOrders";
    public static final String TABLE_ORDER_ITEMS = "OrderItems";
    public static final String TABLE_FINALIZED_ORDER = "FinalizedOrder";
    public static final String TABLE_DEFAULT_MENU = "DefaultMenu";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createPatientInfoTable(db);
        createItemsTable(db);
        createMealOrdersTable(db);
        createOrderItemsTable(db);
        createFinalizedOrderTable(db);
        createDefaultMenuTable(db);

        // Insert initial data
        insertInitialUsers(db);
        insertInitialItems(db);
        insertDefaultMenuItems(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 2) {
            // Add texture modification columns
            try {
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN mechanical_chopped INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN mechanical_ground INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN bite_size INTEGER DEFAULT 0");
                Log.d(TAG, "Texture modification columns added");
            } catch (Exception e) {
                Log.e(TAG, "Error adding texture modification columns", e);
            }
        }

        if (oldVersion < 3) {
            // Add breadOK column
            try {
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN bread_ok INTEGER DEFAULT 1");
                Log.d(TAG, "bread_ok column added");
            } catch (Exception e) {
                Log.e(TAG, "Error adding bread_ok column", e);
            }
        }

        if (oldVersion < 4) {
            // Add additional texture modification columns
            try {
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN nectar_thick INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN pudding_thick INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN honey_thick INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN extra_gravy INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN meats_only INTEGER DEFAULT 0");
                Log.d(TAG, "Additional texture modification columns added");
            } catch (Exception e) {
                Log.e(TAG, "Error adding additional texture modification columns", e);
            }
        }

        if (oldVersion < 5) {
            // Add drink columns to patient info
            try {
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN breakfast_juices TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN lunch_juices TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN dinner_juices TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN breakfast_drinks TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN lunch_drinks TEXT");
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN dinner_drinks TEXT");
                Log.d(TAG, "Drink columns added to patient info");
            } catch (Exception e) {
                Log.e(TAG, "Error adding drink columns", e);
            }
        }

        if (oldVersion < 6) {
            // Add ada_diet column
            try {
                db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN ada_diet INTEGER DEFAULT 0");
                Log.d(TAG, "ada_diet column added");
            } catch (Exception e) {
                Log.e(TAG, "Error adding ada_diet column", e);
            }
        }

        if (oldVersion < 7) {
            // Update admin user password requirements
            try {
                ContentValues values = new ContentValues();
                values.put("force_password_change", 1);
                db.update(TABLE_USERS, values, "username = ?", new String[]{"admin"});
                Log.d(TAG, "Admin user updated to require password change");
            } catch (Exception e) {
                Log.e(TAG, "Error updating admin user", e);
            }
        }

        if (oldVersion < 8) {
            createDefaultMenuTable(db);
            insertDefaultMenuItems(db);
        }

        if (oldVersion < 9) {
            addIndividualMealDietFields(db);
        }
    }

    private void createUsersTable(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "user_role TEXT NOT NULL, " +
                "is_active INTEGER DEFAULT 1, " +
                "force_password_change INTEGER DEFAULT 0, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "last_login DATETIME" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);
        Log.d(TAG, "Users table created");
    }

    private void createPatientInfoTable(SQLiteDatabase db) {
        String CREATE_PATIENT_INFO_TABLE = "CREATE TABLE " + TABLE_PATIENT_INFO + " (" +
                "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_first_name TEXT NOT NULL, " +
                "patient_last_name TEXT NOT NULL, " +
                "wing TEXT, " +
                "room_number TEXT, " +
                "diet_type TEXT, " +
                "diet TEXT, " +
                "ada_diet INTEGER DEFAULT 0, " +
                "fluid_restriction TEXT, " +
                "texture_modifications TEXT, " +
                "mechanical_chopped INTEGER DEFAULT 0, " +
                "mechanical_ground INTEGER DEFAULT 0, " +
                "bite_size INTEGER DEFAULT 0, " +
                "bread_ok INTEGER DEFAULT 1, " +
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
                "breakfast_diet TEXT, " +
                "lunch_diet TEXT, " +
                "dinner_diet TEXT, " +
                "breakfast_ada INTEGER DEFAULT 0, " +
                "lunch_ada INTEGER DEFAULT 0, " +
                "dinner_ada INTEGER DEFAULT 0, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_PATIENT_INFO_TABLE);
        Log.d(TAG, "PatientInfo table created");
    }

    private void createItemsTable(SQLiteDatabase db) {
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + " (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "description TEXT, " +
                "is_ada_friendly INTEGER DEFAULT 0, " +
                "is_cardiac_friendly INTEGER DEFAULT 0, " +
                "is_renal_friendly INTEGER DEFAULT 0, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_ITEMS_TABLE);
        Log.d(TAG, "Items table created");
    }

    private void createMealOrdersTable(SQLiteDatabase db) {
        String CREATE_MEAL_ORDERS_TABLE = "CREATE TABLE " + TABLE_MEAL_ORDERS + " (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER, " +
                "meal TEXT NOT NULL, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (patient_id) REFERENCES " + TABLE_PATIENT_INFO + " (patient_id)" +
                ")";
        db.execSQL(CREATE_MEAL_ORDERS_TABLE);
        Log.d(TAG, "MealOrders table created");
    }

    private void createOrderItemsTable(SQLiteDatabase db) {
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, " +
                "item_id INTEGER, " +
                "quantity INTEGER DEFAULT 1, " +
                "FOREIGN KEY (order_id) REFERENCES " + TABLE_MEAL_ORDERS + " (order_id), " +
                "FOREIGN KEY (item_id) REFERENCES " + TABLE_ITEMS + " (item_id)" +
                ")";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);
        Log.d(TAG, "OrderItems table created");
    }

    private void createFinalizedOrderTable(SQLiteDatabase db) {
        String CREATE_FINALIZED_ORDER_TABLE = "CREATE TABLE " + TABLE_FINALIZED_ORDER + " (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_name TEXT NOT NULL, " +
                "wing TEXT, " +
                "room TEXT, " +
                "order_date TEXT, " +
                "diet_type TEXT, " +
                "fluid_restriction TEXT, " +
                "mechanical_ground INTEGER DEFAULT 0, " +
                "mechanical_chopped INTEGER DEFAULT 0, " +
                "bite_size INTEGER DEFAULT 0, " +
                "bread_ok INTEGER DEFAULT 1, " +
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
        db.execSQL(CREATE_FINALIZED_ORDER_TABLE);
        Log.d(TAG, "FinalizedOrder table created");
    }

    private void createDefaultMenuTable(SQLiteDatabase db) {
        String CREATE_DEFAULT_MENU_TABLE = "CREATE TABLE " + TABLE_DEFAULT_MENU + " (" +
                "menu_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "diet_type TEXT NOT NULL, " +
                "meal_type TEXT NOT NULL, " +
                "day_of_week TEXT NOT NULL, " +
                "item_name TEXT NOT NULL, " +
                "category TEXT, " +
                "is_default INTEGER DEFAULT 1, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_DEFAULT_MENU_TABLE);
        Log.d(TAG, "DefaultMenu table created");
    }

    private void addIndividualMealDietFields(SQLiteDatabase db) {
        try {
            // Add individual meal diet columns
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN breakfast_diet TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN lunch_diet TEXT");
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN dinner_diet TEXT");

            // Add individual meal ADA flags
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN breakfast_ada INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN lunch_ada INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_PATIENT_INFO + " ADD COLUMN dinner_ada INTEGER DEFAULT 0");

            // Migrate existing data: copy main diet to all three meals for existing patients
            db.execSQL("UPDATE " + TABLE_PATIENT_INFO + " SET breakfast_diet = diet WHERE breakfast_diet IS NULL");
            db.execSQL("UPDATE " + TABLE_PATIENT_INFO + " SET lunch_diet = diet WHERE lunch_diet IS NULL");
            db.execSQL("UPDATE " + TABLE_PATIENT_INFO + " SET dinner_diet = diet WHERE dinner_diet IS NULL");

            // Copy ADA flag to all meals
            db.execSQL("UPDATE " + TABLE_PATIENT_INFO + " SET breakfast_ada = ada_diet WHERE breakfast_ada = 0");
            db.execSQL("UPDATE " + TABLE_PATIENT_INFO + " SET lunch_ada = ada_diet WHERE lunch_ada = 0");
            db.execSQL("UPDATE " + TABLE_PATIENT_INFO + " SET dinner_ada = ada_diet WHERE dinner_ada = 0");

            Log.d(TAG, "Individual meal diet fields added successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error adding individual meal diet fields", e);
        }
    }

    private void insertInitialUsers(SQLiteDatabase db) {
        // Create admin user
        ContentValues adminValues = new ContentValues();
        adminValues.put("username", "admin");
        adminValues.put("password", "admin123");
        adminValues.put("full_name", "System Administrator");
        adminValues.put("user_role", "Administrator");
        adminValues.put("is_active", 1);
        adminValues.put("force_password_change", 1);
        db.insert(TABLE_USERS, null, adminValues);

        // Create test dietary staff user
        ContentValues dietaryValues = new ContentValues();
        dietaryValues.put("username", "dietary");
        dietaryValues.put("password", "dietary123");
        dietaryValues.put("full_name", "Dietary Staff");
        dietaryValues.put("user_role", "Dietary Staff");
        dietaryValues.put("is_active", 1);
        dietaryValues.put("force_password_change", 0);
        db.insert(TABLE_USERS, null, dietaryValues);

        Log.d(TAG, "Initial users inserted");
    }

    private void insertInitialItems(SQLiteDatabase db) {
        // Proteins
        insertItem(db, "Grilled Chicken", "Proteins", "Lean grilled chicken breast", 1, 1, 1);
        insertItem(db, "Baked Fish", "Proteins", "Fresh baked fish fillet", 1, 1, 0);
        insertItem(db, "Lean Beef", "Proteins", "Lean beef cuts", 0, 0, 0);
        insertItem(db, "Turkey", "Proteins", "Sliced turkey breast", 1, 1, 1);
        insertItem(db, "Eggs", "Proteins", "Fresh eggs", 1, 1, 1);

        // Starches
        insertItem(db, "Brown Rice", "Starches", "Whole grain brown rice", 1, 1, 1);
        insertItem(db, "White Rice", "Starches", "Regular white rice", 1, 1, 1);
        insertItem(db, "Baked Potato", "Starches", "Plain baked potato", 1, 1, 0);
        insertItem(db, "Whole Wheat Bread", "Starches", "Whole wheat bread slice", 1, 1, 0);
        insertItem(db, "Pasta", "Starches", "Regular pasta", 1, 1, 0);

        // Vegetables
        insertItem(db, "Steamed Broccoli", "Vegetables", "Fresh steamed broccoli", 1, 1, 0);
        insertItem(db, "Green Beans", "Vegetables", "Fresh green beans", 1, 1, 1);
        insertItem(db, "Carrots", "Vegetables", "Steamed carrots", 1, 1, 1);
        insertItem(db, "Mixed Vegetables", "Vegetables", "Seasonal mixed vegetables", 1, 1, 1);
        insertItem(db, "Corn", "Vegetables", "Sweet corn", 1, 0, 0);

        // Beverages
        insertItem(db, "Water", "Beverages", "Plain water", 1, 1, 1);
        insertItem(db, "Black Coffee", "Beverages", "Regular black coffee", 1, 1, 1);
        insertItem(db, "Unsweetened Tea", "Beverages", "Plain unsweetened tea", 1, 1, 1);
        insertItem(db, "Low-Fat Milk", "Beverages", "1% or 2% milk", 1, 1, 0);
        insertItem(db, "Orange Juice", "Beverages", "Fresh orange juice", 0, 0, 0);

        // Fruits
        insertItem(db, "Apple", "Fruits", "Fresh apple", 1, 1, 1);
        insertItem(db, "Banana", "Fruits", "Fresh banana", 1, 1, 1);
        insertItem(db, "Orange", "Fruits", "Fresh orange", 1, 1, 1);
        insertItem(db, "Berries", "Fruits", "Mixed berries", 1, 1, 1);
        insertItem(db, "Melon", "Fruits", "Fresh melon pieces", 1, 1, 1);

        // Desserts
        insertItem(db, "Sugar-Free Jello", "Desserts", "Sugar-free gelatin", 1, 1, 1);
        insertItem(db, "Fresh Fruit Cup", "Desserts", "Mixed fresh fruit", 1, 1, 1);
        insertItem(db, "Sugar-Free Pudding", "Desserts", "Sugar-free pudding", 1, 1, 0);
        insertItem(db, "Regular Jello", "Desserts", "Regular gelatin dessert", 0, 0, 0);
        insertItem(db, "Ice Cream", "Desserts", "Vanilla ice cream", 0, 0, 0);

        Log.d(TAG, "Initial items inserted");
    }

    private void insertItem(SQLiteDatabase db, String name, String category, String description,
                            int isAdaFriendly, int isCardiacFriendly, int isRenalFriendly) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("description", description);
        values.put("is_ada_friendly", isAdaFriendly);
        values.put("is_cardiac_friendly", isCardiacFriendly);
        values.put("is_renal_friendly", isRenalFriendly);
        db.insert(TABLE_ITEMS, null, values);
    }

    private void insertDefaultMenuItems(SQLiteDatabase db) {
        // Insert basic default menu items for different diets and meals
        String[] dietTypes = {"Regular", "ADA", "Cardiac", "Renal"};
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner"};
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (String dietType : dietTypes) {
            for (String mealType : mealTypes) {
                for (String dayOfWeek : daysOfWeek) {
                    insertBasicMenuItems(db, dietType, mealType, dayOfWeek);
                }
            }
        }

        Log.d(TAG, "Default menu items inserted");
    }

    private void insertBasicMenuItems(SQLiteDatabase db, String dietType, String mealType, String dayOfWeek) {
        ContentValues values = new ContentValues();
        values.put("diet_type", dietType);
        values.put("meal_type", mealType);
        values.put("day_of_week", dayOfWeek);
        values.put("is_default", 1);

        if ("Breakfast".equals(mealType)) {
            values.put("item_name", "Scrambled Eggs");
            values.put("category", "Proteins");
            db.insert(TABLE_DEFAULT_MENU, null, values);

            values.put("item_name", "Toast");
            values.put("category", "Starches");
            db.insert(TABLE_DEFAULT_MENU, null, values);
        } else if ("Lunch".equals(mealType)) {
            values.put("item_name", "Grilled Chicken");
            values.put("category", "Proteins");
            db.insert(TABLE_DEFAULT_MENU, null, values);

            values.put("item_name", "Rice");
            values.put("category", "Starches");
            db.insert(TABLE_DEFAULT_MENU, null, values);
        } else if ("Dinner".equals(mealType)) {
            values.put("item_name", "Baked Fish");
            values.put("category", "Proteins");
            db.insert(TABLE_DEFAULT_MENU, null, values);

            values.put("item_name", "Baked Potato");
            values.put("category", "Starches");
            db.insert(TABLE_DEFAULT_MENU, null, values);
        }
    }
}