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
    private static final int DATABASE_VERSION = 11;

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

        // Create patient table
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
                "is_puree INTEGER DEFAULT 0," +
                "allergies TEXT," +
                "likes TEXT," +
                "dislikes TEXT," +
                "comments TEXT," +
                "preferred_drink TEXT," +
                "drink_variety TEXT," +
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
                "breakfast_diet TEXT," +
                "lunch_diet TEXT," +
                "dinner_diet TEXT," +
                "breakfast_ada INTEGER DEFAULT 0," +
                "lunch_ada INTEGER DEFAULT 0," +
                "dinner_ada INTEGER DEFAULT 0," +
                "created_date INTEGER" +
                ")";
        db.execSQL(CREATE_PATIENT_TABLE);

        // Create items table - THIS IS THE CRITICAL TABLE THAT WAS MISSING
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
                "Juices",
                "Sides",
                "Snacks",
                "Soups",
                "Vegetables"
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
        // Check if items already exist
        String countQuery = "SELECT COUNT(*) FROM items";
        android.database.Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count > 0) {
            Log.d(TAG, "Items table already populated with " + count + " items");
            return;
        }

        Log.d(TAG, "Inserting default food items...");

        // Breakfast items
        insertItem(db, "Scrambled Eggs", "Breakfast Entrees", "Fluffy scrambled eggs", true);
        insertItem(db, "Pancakes", "Breakfast Entrees", "Stack of 3 pancakes", false);
        insertItem(db, "French Toast", "Breakfast Entrees", "Two slices of French toast", false);
        insertItem(db, "Oatmeal", "Breakfast Entrees", "Hot oatmeal with brown sugar", true);
        insertItem(db, "Yogurt", "Breakfast Entrees", "Low-fat vanilla yogurt", true);
        insertItem(db, "Bacon", "Breakfast Entrees", "Crispy bacon strips", false);
        insertItem(db, "Sausage", "Breakfast Entrees", "Pork sausage links", false);

        // Lunch entrees
        insertItem(db, "Grilled Chicken", "Lunch Entrees", "Seasoned grilled chicken breast", true);
        insertItem(db, "Turkey Sandwich", "Lunch Entrees", "Turkey on whole wheat", false);
        insertItem(db, "Hamburger", "Lunch Entrees", "Beef patty on bun", false);
        insertItem(db, "Tuna Salad", "Lunch Entrees", "Tuna salad sandwich", true);
        insertItem(db, "Caesar Salad", "Lunch Entrees", "Romaine lettuce with Caesar dressing", false);

        // Dinner entrees
        insertItem(db, "Baked Fish", "Dinner Entrees", "Herb-crusted baked fish", true);
        insertItem(db, "Roast Beef", "Dinner Entrees", "Sliced roast beef with gravy", false);
        insertItem(db, "Chicken Parmesan", "Dinner Entrees", "Breaded chicken with marinara", false);
        insertItem(db, "Meatloaf", "Dinner Entrees", "Traditional meatloaf", false);
        insertItem(db, "Pork Chops", "Dinner Entrees", "Grilled pork chops", true);

        // Beverages
        insertItem(db, "Coffee", "Beverages", "Hot coffee", true);
        insertItem(db, "Tea", "Beverages", "Hot tea", true);
        insertItem(db, "Milk", "Beverages", "2% milk", true);
        insertItem(db, "Water", "Beverages", "Bottled water", true);
        insertItem(db, "Soda", "Beverages", "Assorted soft drinks", false);

        // Breads
        insertItem(db, "White Bread", "Breads", "Sliced white bread", false);
        insertItem(db, "Wheat Bread", "Breads", "Whole wheat bread", true);
        insertItem(db, "Dinner Roll", "Breads", "Soft dinner roll", false);
        insertItem(db, "Crackers", "Breads", "Saltine crackers", true);

        // Condiments
        insertItem(db, "Butter", "Condiments", "Butter pat", true);
        insertItem(db, "Jelly", "Condiments", "Grape or strawberry jelly", true);
        insertItem(db, "Sugar", "Condiments", "Sugar packets", true);
        insertItem(db, "Salt", "Condiments", "Salt packets", true);
        insertItem(db, "Pepper", "Condiments", "Pepper packets", true);
        insertItem(db, "Ketchup", "Condiments", "Ketchup packets", true);
        insertItem(db, "Mustard", "Condiments", "Mustard packets", true);

        // Desserts
        insertItem(db, "Ice Cream", "Desserts", "Vanilla ice cream", false);
        insertItem(db, "Pudding", "Desserts", "Chocolate or vanilla pudding", true);
        insertItem(db, "Jello", "Desserts", "Sugar-free jello", true);
        insertItem(db, "Cookies", "Desserts", "Chocolate chip cookies", false);
        insertItem(db, "Cake", "Desserts", "Slice of cake", false);

        // Fruits
        insertItem(db, "Apple", "Fruits", "Fresh apple", true);
        insertItem(db, "Banana", "Fruits", "Fresh banana", true);
        insertItem(db, "Orange", "Fruits", "Fresh orange", true);
        insertItem(db, "Fruit Cup", "Fruits", "Mixed fruit cup", true);
        insertItem(db, "Applesauce", "Fruits", "Unsweetened applesauce", true);

        // Juices
        insertItem(db, "Orange Juice", "Juices", "Fresh orange juice", true);
        insertItem(db, "Apple Juice", "Juices", "100% apple juice", true);
        insertItem(db, "Cranberry Juice", "Juices", "Cranberry juice cocktail", false);
        insertItem(db, "Grape Juice", "Juices", "100% grape juice", false);

        // Sides
        insertItem(db, "Mashed Potatoes", "Sides", "Creamy mashed potatoes", false);
        insertItem(db, "Rice", "Sides", "Steamed white rice", true);
        insertItem(db, "French Fries", "Sides", "Crispy french fries", false);
        insertItem(db, "Baked Potato", "Sides", "Baked potato with toppings", true);

        // Vegetables
        insertItem(db, "Green Beans", "Vegetables", "Steamed green beans", true);
        insertItem(db, "Carrots", "Vegetables", "Cooked carrots", true);
        insertItem(db, "Broccoli", "Vegetables", "Steamed broccoli", true);
        insertItem(db, "Corn", "Vegetables", "Sweet corn", false);
        insertItem(db, "Peas", "Vegetables", "Green peas", true);

        // Soups
        insertItem(db, "Chicken Noodle Soup", "Soups", "Classic chicken noodle", false);
        insertItem(db, "Tomato Soup", "Soups", "Creamy tomato soup", false);
        insertItem(db, "Vegetable Soup", "Soups", "Mixed vegetable soup", true);
        insertItem(db, "Broth", "Soups", "Chicken or beef broth", true);

        Log.d(TAG, "Default food items inserted successfully");
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
        db.insertWithOnConflict("items", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
}