package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DatabaseHelper class - manages SQLite database creation and migrations
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "HospitalDietaryDB";
    private static final int DATABASE_VERSION = 12; // Incremented for discharged field

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        // Create all tables
        createAllTables(db);

        // Insert initial data
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // For version 11, ensure all tables exist
        if (oldVersion < 11) {
            // Create all tables if they don't exist
            createAllTables(db);
        }

        // For version 12, add discharged field
        if (oldVersion < 12) {
            try {
                db.execSQL("ALTER TABLE patient_info ADD COLUMN discharged INTEGER DEFAULT 0");
                Log.d(TAG, "Added discharged column to patient_info table");
            } catch (Exception e) {
                Log.e(TAG, "Error adding discharged column: " + e.getMessage());
            }
        }
    }

    /**
     * Create all database tables
     */
    private void createAllTables(SQLiteDatabase db) {
        // Create the users table
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "full_name TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "is_active INTEGER DEFAULT 1," +
                "must_change_password INTEGER DEFAULT 0," +
                "last_login INTEGER," +
                "created_date INTEGER" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create patient table with discharged field
        String CREATE_PATIENT_TABLE = "CREATE TABLE IF NOT EXISTS patient_info (" +
                "patient_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patient_first_name TEXT," +
                "patient_last_name TEXT," +
                "wing TEXT," +
                "room_number TEXT," +
                "diet_type TEXT," +
                "diet TEXT," +
                "ada_diet INTEGER DEFAULT 0," +
                "fluid_restriction TEXT," +
                "texture_modifications TEXT," +
                "mechanical_chopped INTEGER DEFAULT 0," +
                "mechanical_ground INTEGER DEFAULT 0," +
                "bite_size INTEGER DEFAULT 0," +
                "bread_ok INTEGER DEFAULT 0," +
                "nectar_thick INTEGER DEFAULT 0," +
                "pudding_thick INTEGER DEFAULT 0," +
                "honey_thick INTEGER DEFAULT 0," +
                "extra_gravy INTEGER DEFAULT 0," +
                "meats_only INTEGER DEFAULT 0," +
                "breakfast_complete INTEGER DEFAULT 0," +
                "lunch_complete INTEGER DEFAULT 0," +
                "dinner_complete INTEGER DEFAULT 0," +
                "breakfast_npo INTEGER DEFAULT 0," +
                "lunch_npo INTEGER DEFAULT 0," +
                "dinner_npo INTEGER DEFAULT 0," +
                "breakfast_items TEXT," +
                "lunch_items TEXT," +
                "dinner_items TEXT," +
                "breakfast_juices TEXT," +
                "lunch_juices TEXT," +
                "dinner_juices TEXT," +
                "breakfast_drinks TEXT," +
                "lunch_drinks TEXT," +
                "dinner_drinks TEXT," +
                "created_date INTEGER," +
                "order_date INTEGER," +
                "breakfast_diet TEXT," +
                "lunch_diet TEXT," +
                "dinner_diet TEXT," +
                "breakfast_ada INTEGER DEFAULT 0," +
                "lunch_ada INTEGER DEFAULT 0," +
                "dinner_ada INTEGER DEFAULT 0," +
                "discharged INTEGER DEFAULT 0" +
                ")";
        db.execSQL(CREATE_PATIENT_TABLE);

        // Create items table
        String CREATE_ITEMS_TABLE = "CREATE TABLE IF NOT EXISTS items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "description TEXT," +
                "is_ada_friendly INTEGER DEFAULT 0" +
                ")";
        db.execSQL(CREATE_ITEMS_TABLE);

        // Create categories table
        String CREATE_CATEGORIES_TABLE = "CREATE TABLE IF NOT EXISTS categories (" +
                "category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category_name TEXT NOT NULL UNIQUE," +
                "description TEXT," +
                "sort_order INTEGER DEFAULT 0" +
                ")";
        db.execSQL(CREATE_CATEGORIES_TABLE);

        // Create meal_orders table
        String CREATE_MEAL_ORDERS_TABLE = "CREATE TABLE IF NOT EXISTS meal_orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patient_id INTEGER," +
                "meal TEXT," +
                "order_date INTEGER," +
                "is_complete INTEGER DEFAULT 0," +
                "created_by TEXT," +
                "timestamp INTEGER," +
                "FOREIGN KEY(patient_id) REFERENCES patient_info(patient_id)" +
                ")";
        db.execSQL(CREATE_MEAL_ORDERS_TABLE);

        // Create order_items table
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE IF NOT EXISTS order_items (" +
                "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "item_id INTEGER," +
                "quantity INTEGER DEFAULT 1," +
                "FOREIGN KEY(order_id) REFERENCES meal_orders(order_id)," +
                "FOREIGN KEY(item_id) REFERENCES items(item_id)" +
                ")";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);

        // Create default_menu table
        String CREATE_DEFAULT_MENU_TABLE = "CREATE TABLE IF NOT EXISTS default_menu (" +
                "menu_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "diet_type TEXT," +
                "meal_type TEXT," +
                "day_of_week TEXT," +
                "item_name TEXT," +
                "item_category TEXT," +
                "is_active INTEGER DEFAULT 1" +
                ")";
        db.execSQL(CREATE_DEFAULT_MENU_TABLE);

        // Create finalized_orders table
        String CREATE_FINALIZED_ORDERS_TABLE = "CREATE TABLE IF NOT EXISTS finalized_order (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patient_name TEXT," +
                "wing TEXT," +
                "room TEXT," +
                "order_date TEXT," +
                "diet_type TEXT" +
                ")";
        db.execSQL(CREATE_FINALIZED_ORDERS_TABLE);

        // Create finalized_order_items table
        String CREATE_FINALIZED_ORDER_ITEMS_TABLE = "CREATE TABLE IF NOT EXISTS finalized_order_items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "meal_type TEXT," +
                "item_name TEXT," +
                "quantity INTEGER," +
                "category TEXT," +
                "FOREIGN KEY(order_id) REFERENCES finalized_order(order_id)" +
                ")";
        db.execSQL(CREATE_FINALIZED_ORDER_ITEMS_TABLE);

        Log.d(TAG, "All tables created successfully");
    }

    /**
     * Insert initial data into the database
     */
    private void insertInitialData(SQLiteDatabase db) {
        // Insert default admin user
        db.execSQL("INSERT OR IGNORE INTO users (username, password, full_name, role, is_active, must_change_password) " +
                "VALUES ('admin', 'admin123', 'System Administrator', 'Admin', 1, 0)");

        // Insert default categories if they don't exist
        insertDefaultCategories(db);

        // Insert default food items if the table is empty
        insertDefaultFoodItems(db);
    }

    /**
     * Insert default categories
     */
    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] categories = {
                "Breakfast Entrees",
                "Lunch Entrees",
                "Dinner Entrees",
                "Beverages",
                "Breads",
                "Condiments",
                "Desserts",
                "Fruits",
                "Vegetables",
                "Soups",
                "Salads",
                "Snacks"
        };

        for (int i = 0; i < categories.length; i++) {
            ContentValues values = new ContentValues();
            values.put("category_name", categories[i]);
            values.put("sort_order", i);
            db.insertWithOnConflict("categories", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    /**
     * Insert default food items
     */
    private void insertDefaultFoodItems(SQLiteDatabase db) {
        // Check if items table is empty
        String countQuery = "SELECT COUNT(*) FROM items";
        android.database.Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            // Insert breakfast items
            insertItem(db, "Scrambled Eggs", "Breakfast Entrees", "Fluffy scrambled eggs", false);
            insertItem(db, "Oatmeal", "Breakfast Entrees", "Hot oatmeal with toppings", true);
            insertItem(db, "Pancakes", "Breakfast Entrees", "Fluffy pancakes with syrup", false);
            insertItem(db, "French Toast", "Breakfast Entrees", "Classic French toast", false);

            // Insert lunch/dinner entrees
            insertItem(db, "Grilled Chicken", "Lunch Entrees", "Seasoned grilled chicken breast", true);
            insertItem(db, "Baked Fish", "Dinner Entrees", "Lightly seasoned baked fish", true);
            insertItem(db, "Meatloaf", "Dinner Entrees", "Homestyle meatloaf", false);
            insertItem(db, "Vegetable Stir Fry", "Lunch Entrees", "Mixed vegetable stir fry", true);

            // Insert beverages
            insertItem(db, "Orange Juice", "Beverages", "Fresh orange juice", true);
            insertItem(db, "Apple Juice", "Beverages", "100% apple juice", true);
            insertItem(db, "Coffee", "Beverages", "Regular or decaf coffee", true);
            insertItem(db, "Tea", "Beverages", "Hot tea - various flavors", true);
            insertItem(db, "Milk", "Beverages", "2% milk", true);

            // Insert sides
            insertItem(db, "White Bread", "Breads", "Sliced white bread", false);
            insertItem(db, "Wheat Bread", "Breads", "Whole wheat bread", true);
            insertItem(db, "Butter", "Condiments", "Butter packets", true);
            insertItem(db, "Jelly", "Condiments", "Assorted jelly packets", true);

            Log.d(TAG, "Default food items inserted");
        }
    }

    /**
     * Helper method to insert an item
     */
    private void insertItem(SQLiteDatabase db, String name, String category, String description, boolean isAdaFriendly) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("description", description);
        values.put("is_ada_friendly", isAdaFriendly ? 1 : 0);
        db.insert("items", null, values);
    }
}