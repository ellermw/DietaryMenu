package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "HospitalDietaryDB";
    private static final int DATABASE_VERSION = 10; // INCREMENTED to force recreation

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
        Log.d(TAG, "Creating database tables...");

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

        Log.d(TAG, "Database creation completed");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop all tables and recreate them to ensure clean state
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEFAULT_MENU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FINALIZED_ORDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENT_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Recreate all tables
        onCreate(db);
    }

    private void createUsersTable(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "role TEXT NOT NULL DEFAULT 'User', " +
                "is_active INTEGER DEFAULT 1, " +
                "must_change_password INTEGER DEFAULT 0, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "last_login DATETIME" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);
        Log.d(TAG, "Users table created");
    }

    private void createPatientInfoTable(SQLiteDatabase db) {
        String CREATE_PATIENT_INFO_TABLE = "CREATE TABLE " + TABLE_PATIENT_INFO + " (" +
                "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_name TEXT NOT NULL, " +
                "wing TEXT, " +
                "room TEXT, " +
                "diet TEXT, " +
                "fluid_restriction TEXT, " +
                "texture_modifications TEXT, " +
                "order_date TEXT, " +
                "breakfast_complete INTEGER DEFAULT 0, " +
                "lunch_complete INTEGER DEFAULT 0, " +
                "dinner_complete INTEGER DEFAULT 0, " +
                "breakfast_items TEXT, " +
                "lunch_items TEXT, " +
                "dinner_items TEXT, " +
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
                "breakfast_items TEXT, " +
                "lunch_items TEXT, " +
                "dinner_items TEXT, " +
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
                "meal TEXT NOT NULL, " +
                "item_id INTEGER, " +
                "FOREIGN KEY (item_id) REFERENCES " + TABLE_ITEMS + " (item_id)" +
                ")";
        db.execSQL(CREATE_DEFAULT_MENU_TABLE);
        Log.d(TAG, "DefaultMenu table created");
    }

    private void insertInitialUsers(SQLiteDatabase db) {
        Log.d(TAG, "Inserting initial users...");

        ContentValues adminValues = new ContentValues();
        adminValues.put("username", "admin");
        adminValues.put("password", "admin123");
        adminValues.put("full_name", "System Administrator");
        adminValues.put("role", "Admin");
        adminValues.put("is_active", 1);
        adminValues.put("must_change_password", 1);

        long adminResult = db.insert(TABLE_USERS, null, adminValues);
        Log.d(TAG, "Admin user inserted with ID: " + adminResult);

        ContentValues userValues = new ContentValues();
        userValues.put("username", "user");
        userValues.put("password", "user123");
        userValues.put("full_name", "Test User");
        userValues.put("role", "User");
        userValues.put("is_active", 1);
        userValues.put("must_change_password", 0);

        long userResult = db.insert(TABLE_USERS, null, userValues);
        Log.d(TAG, "Test user inserted with ID: " + userResult);
    }

    private void insertInitialItems(SQLiteDatabase db) {
        Log.d(TAG, "Inserting initial items...");

        // Breakfast items
        insertItem(db, "Oatmeal", "Breakfast", "Plain oatmeal", 1, 1, 1);
        insertItem(db, "Scrambled Eggs", "Breakfast", "Plain scrambled eggs", 1, 1, 1);
        insertItem(db, "Whole Wheat Toast", "Breakfast", "Whole grain bread", 1, 1, 0);
        insertItem(db, "Fresh Fruit", "Breakfast", "Seasonal fresh fruit", 1, 1, 1);
        insertItem(db, "Greek Yogurt", "Breakfast", "Plain low-fat yogurt", 1, 1, 0);

        // Lunch items
        insertItem(db, "Grilled Chicken", "Lunch", "Skinless grilled chicken breast", 1, 1, 1);
        insertItem(db, "Baked Fish", "Lunch", "Fresh baked fish fillet", 1, 1, 1);
        insertItem(db, "Brown Rice", "Lunch", "Steamed brown rice", 1, 1, 1);
        insertItem(db, "Quinoa", "Lunch", "Cooked quinoa", 1, 1, 1);
        insertItem(db, "Garden Salad", "Lunch", "Mixed green salad", 1, 1, 1);

        // Dinner items
        insertItem(db, "Lean Beef", "Dinner", "Grilled lean beef", 1, 0, 1);
        insertItem(db, "Salmon", "Dinner", "Baked salmon fillet", 1, 1, 0);
        insertItem(db, "Sweet Potato", "Dinner", "Baked sweet potato", 1, 1, 1);
        insertItem(db, "Steamed Vegetables", "Dinner", "Mixed steamed vegetables", 1, 1, 1);
        insertItem(db, "Whole Grain Pasta", "Dinner", "Whole wheat pasta", 1, 1, 0);

        // Vegetables
        insertItem(db, "Broccoli", "Vegetables", "Fresh steamed broccoli", 1, 1, 0);
        insertItem(db, "Green Beans", "Vegetables", "Fresh green beans", 1, 1, 1);
        insertItem(db, "Carrots", "Vegetables", "Steamed carrots", 1, 1, 1);
        insertItem(db, "Spinach", "Vegetables", "Fresh spinach", 1, 1, 1);
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

        Log.d(TAG, "Initial items insertion completed");
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

        long result = db.insert(TABLE_ITEMS, null, values);
        if (result == -1) {
            Log.e(TAG, "Failed to insert item: " + name);
        } else {
            Log.d(TAG, "Inserted item: " + name + " with ID: " + result);
        }
    }

    private void insertDefaultMenuItems(SQLiteDatabase db) {
        Log.d(TAG, "Inserting default menu items...");
        // Default menu items can be added here if needed
        Log.d(TAG, "Default menu items insertion completed");
    }
}