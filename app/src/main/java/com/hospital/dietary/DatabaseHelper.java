package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper class - simplified for Room migration
 * This class now only serves as a compatibility layer
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HospitalDietaryDB";
    private static final int DATABASE_VERSION = 11;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the users table for backward compatibility
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

        // Insert default admin user
        db.execSQL("INSERT OR IGNORE INTO users (username, password, full_name, role, is_active, must_change_password) " +
                "VALUES ('admin', 'admin123', 'System Administrator', 'Admin', 1, 0)");

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
        String CREATE_FINALIZED_ORDERS_TABLE = "CREATE TABLE IF NOT EXISTS finalized_orders (" +
                "finalized_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patient_id INTEGER," +
                "meal_type TEXT," +
                "order_details TEXT," +
                "finalized_date INTEGER," +
                "finalized_by TEXT," +
                "FOREIGN KEY(patient_id) REFERENCES patient_info(patient_id)" +
                ")";

        db.execSQL(CREATE_FINALIZED_ORDERS_TABLE);

        // Insert default categories
        insertDefaultCategories(db);

        // Insert default items
        insertDefaultItems(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[][] categories = {
                {"Breakfast Main", "Main breakfast items", "1"},
                {"Breakfast Cereal", "Hot and cold cereals", "2"},
                {"Breakfast Bread", "Toast and bread items", "3"},
                {"Breakfast Juice", "Fruit juices", "4"},
                {"Breakfast Beverage", "Coffee, tea, milk", "5"},
                {"Breakfast Dairy", "Yogurt and dairy items", "6"},
                {"Lunch Main", "Main lunch entrees", "7"},
                {"Lunch Soup", "Soups", "8"},
                {"Lunch Salad", "Salads", "9"},
                {"Lunch Side", "Side dishes", "10"},
                {"Lunch Vegetable", "Vegetables", "11"},
                {"Lunch Beverage", "Lunch beverages", "12"},
                {"Dinner Main", "Main dinner entrees", "13"},
                {"Dinner Side", "Side dishes", "14"},
                {"Dinner Vegetable", "Vegetables", "15"},
                {"Dinner Bread", "Bread and rolls", "16"},
                {"Dinner Dessert", "Desserts", "17"},
                {"Dinner Beverage", "Dinner beverages", "18"},
                {"Snack", "Snack items", "19"},
                {"Supplement", "Nutritional supplements", "20"}
        };

        for (String[] cat : categories) {
            db.execSQL("INSERT OR IGNORE INTO categories (category_name, description, sort_order) VALUES (?, ?, ?)",
                    new Object[]{cat[0], cat[1], Integer.parseInt(cat[2])});
        }
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        // Insert some default items
        String[][] items = {
                // Breakfast items
                {"Scrambled Eggs", "Breakfast Main", "Fresh scrambled eggs", "1"},
                {"Pancakes", "Breakfast Main", "Fluffy pancakes with syrup", "0"},
                {"Oatmeal", "Breakfast Cereal", "Heart-healthy oatmeal", "1"},
                {"Cornflakes", "Breakfast Cereal", "Classic corn flakes", "1"},
                {"White Toast", "Breakfast Bread", "White bread toasted", "1"},
                {"Wheat Toast", "Breakfast Bread", "Whole wheat bread toasted", "1"},
                {"Orange Juice", "Breakfast Juice", "100% orange juice", "1"},
                {"Apple Juice", "Breakfast Juice", "100% apple juice", "1"},
                {"Coffee", "Breakfast Beverage", "Regular coffee", "1"},
                {"Decaf Coffee", "Breakfast Beverage", "Decaffeinated coffee", "1"},
                {"Milk", "Breakfast Beverage", "2% milk", "1"},
                {"Yogurt", "Breakfast Dairy", "Low-fat yogurt", "1"},

                // Lunch items
                {"Grilled Chicken", "Lunch Main", "Herb grilled chicken breast", "1"},
                {"Baked Fish", "Lunch Main", "Lemon pepper baked fish", "1"},
                {"Beef Stew", "Lunch Main", "Hearty beef stew", "0"},
                {"Vegetable Soup", "Lunch Soup", "Fresh vegetable soup", "1"},
                {"Chicken Noodle Soup", "Lunch Soup", "Classic chicken noodle", "0"},
                {"Garden Salad", "Lunch Salad", "Fresh mixed greens", "1"},
                {"Caesar Salad", "Lunch Salad", "Romaine with caesar dressing", "0"},
                {"Rice Pilaf", "Lunch Side", "Seasoned rice", "1"},
                {"Mashed Potatoes", "Lunch Side", "Creamy mashed potatoes", "1"},
                {"Green Beans", "Lunch Vegetable", "Steamed green beans", "1"},
                {"Carrots", "Lunch Vegetable", "Glazed carrots", "1"},
                {"Iced Tea", "Lunch Beverage", "Sweetened iced tea", "1"},
                {"Lemonade", "Lunch Beverage", "Fresh lemonade", "1"}
        };

        for (String[] item : items) {
            db.execSQL("INSERT OR IGNORE INTO items (name, category, description, is_ada_friendly) VALUES (?, ?, ?, ?)",
                    new Object[]{item[0], item[1], item[2], Integer.parseInt(item[3])});
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, just recreate tables
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Allow downgrade by recreating tables
        onCreate(db);
    }
}