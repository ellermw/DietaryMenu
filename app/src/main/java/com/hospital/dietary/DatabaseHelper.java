package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dietary_menu.db";
    private static final int DATABASE_VERSION = 7; // Incremented for diet options update

    // Updated PatientInfo table with enhanced features
    private static final String CREATE_PATIENT_INFO_TABLE =
            "CREATE TABLE IF NOT EXISTS PatientInfo (" +
                    "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_first_name TEXT NOT NULL, " +
                    "patient_last_name TEXT NOT NULL, " +
                    "wing TEXT NOT NULL, " +
                    "room_number TEXT NOT NULL, " +
                    "diet TEXT NOT NULL, " +
                    "fluid_restriction TEXT, " +
                    "texture_modifications TEXT, " +
                    "breakfast_complete INTEGER DEFAULT 0, " +
                    "lunch_complete INTEGER DEFAULT 0, " +
                    "dinner_complete INTEGER DEFAULT 0, " +
                    "breakfast_npo INTEGER DEFAULT 0, " +
                    "lunch_npo INTEGER DEFAULT 0, " +
                    "dinner_npo INTEGER DEFAULT 0, " +
                    "created_date TEXT NOT NULL, " +
                    "UNIQUE(wing, room_number)" + // Ensure only one patient per room
                    ")";

    // Enhanced FinalizedOrder table for order history tracking
    private static final String CREATE_FINALIZED_ORDER_TABLE =
            "CREATE TABLE IF NOT EXISTS FinalizedOrder (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_name TEXT NOT NULL, " +
                    "wing TEXT NOT NULL, " +
                    "room TEXT NOT NULL, " +
                    "order_date TEXT NOT NULL, " +
                    "diet_type TEXT NOT NULL, " +
                    "fluid_restriction TEXT, " +
                    "mechanical_ground INTEGER DEFAULT 0, " +
                    "mechanical_chopped INTEGER DEFAULT 0, " +
                    "bite_size INTEGER DEFAULT 0, " +
                    "bread_ok INTEGER DEFAULT 0, " +
                    "breakfast_items TEXT, " +
                    "lunch_items TEXT, " +
                    "dinner_items TEXT, " +
                    "breakfast_juices TEXT, " +
                    "lunch_juices TEXT, " +
                    "dinner_juices TEXT, " +
                    "breakfast_drinks TEXT, " +
                    "lunch_drinks TEXT, " +
                    "dinner_drinks TEXT, " +
                    "created_timestamp TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    // Diet table
    private static final String CREATE_DIET_TABLE =
            "CREATE TABLE IF NOT EXISTS Diet (" +
                    "diet_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "description TEXT" +
                    ")";

    // FIXED: Add the missing Category table
    private static final String CREATE_CATEGORY_TABLE =
            "CREATE TABLE IF NOT EXISTS Category (" +
                    "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "description TEXT, " +
                    "display_order INTEGER DEFAULT 0" +
                    ")";

    // FIXED: Updated Item table to use category_id instead of category text
    private static final String CREATE_ITEM_TABLE =
            "CREATE TABLE IF NOT EXISTS Item (" +
                    "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "category_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "size_ml INTEGER, " +
                    "description TEXT, " +
                    "is_ada_friendly INTEGER DEFAULT 0, " +
                    "is_soda INTEGER DEFAULT 0, " +
                    "is_clear_liquid INTEGER DEFAULT 0, " +
                    "meal_type TEXT NOT NULL, " +
                    "is_default INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(category_id) REFERENCES Category(category_id)" +
                    ")";

    // MealOrder table for tracking orders
    private static final String CREATE_MEAL_ORDER_TABLE =
            "CREATE TABLE IF NOT EXISTS MealOrder (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "meal_type TEXT NOT NULL, " +
                    "FOREIGN KEY(patient_id) REFERENCES PatientInfo(patient_id)" +
                    ")";

    // OrderItem table for detailed order items
    private static final String CREATE_ORDER_ITEM_TABLE =
            "CREATE TABLE IF NOT EXISTS OrderItem (" +
                    "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER NOT NULL, " +
                    "item_id INTEGER NOT NULL, " +
                    "quantity INTEGER DEFAULT 1, " +
                    "FOREIGN KEY(order_id) REFERENCES MealOrder(order_id), " +
                    "FOREIGN KEY(item_id) REFERENCES Item(item_id)" +
                    ")";

    // User table for login system
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS User (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "created_date TEXT NOT NULL" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL(CREATE_PATIENT_INFO_TABLE);
        db.execSQL(CREATE_FINALIZED_ORDER_TABLE);
        db.execSQL(CREATE_DIET_TABLE);
        db.execSQL(CREATE_CATEGORY_TABLE);
        db.execSQL(CREATE_ITEM_TABLE);
        db.execSQL(CREATE_MEAL_ORDER_TABLE);
        db.execSQL(CREATE_ORDER_ITEM_TABLE);
        db.execSQL(CREATE_USER_TABLE);

        // Insert default data
        insertDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            // Add missing Category table if it doesn't exist
            db.execSQL(CREATE_CATEGORY_TABLE);

            // Update Item table to use category_id
            migrateItemTable(db);

            // Insert default data if not already present
            insertDefaultData(db);
        }

        if (oldVersion < 6) {
            // Update diet options with ADA varieties
            updateDietOptions(db);
        }

        if (oldVersion < 7) {
            // Add 'retired' column to PatientInfo table
            db.execSQL("ALTER TABLE PatientInfo ADD COLUMN retired INTEGER DEFAULT 0");
        }
    }

    private void migrateItemTable(SQLiteDatabase db) {
        try {
            // Try to add category_id column if it doesn't exist
            db.execSQL("ALTER TABLE Item ADD COLUMN category_id INTEGER");

            // Map existing category text to category_id
            db.execSQL("UPDATE Item SET category_id = 1 WHERE category LIKE '%Breakfast%'");
            db.execSQL("UPDATE Item SET category_id = 2 WHERE category LIKE '%Protein%'");
            db.execSQL("UPDATE Item SET category_id = 3 WHERE category LIKE '%Starch%' OR category LIKE '%Carb%' OR category LIKE '%Potato%'");
            db.execSQL("UPDATE Item SET category_id = 4 WHERE category LIKE '%Vegetable%'");
            db.execSQL("UPDATE Item SET category_id = 5 WHERE category LIKE '%Beverage%' OR category LIKE '%Drink%'");
            db.execSQL("UPDATE Item SET category_id = 6 WHERE category LIKE '%Juice%'");
            db.execSQL("UPDATE Item SET category_id = 7 WHERE category LIKE '%Dessert%'");
            db.execSQL("UPDATE Item SET category_id = 8 WHERE category LIKE '%Fruit%'");
            db.execSQL("UPDATE Item SET category_id = 9 WHERE category LIKE '%Dairy%' OR category LIKE '%Milk%'");

            // Set default category for any unmapped items
            db.execSQL("UPDATE Item SET category_id = 1 WHERE category_id IS NULL");

        } catch (Exception e) {
            android.util.Log.d("DatabaseHelper", "Item table migration: " + e.getMessage());
        }
    }

    private void updateDietOptions(SQLiteDatabase db) {
        // Add new ADA diet varieties
        String[] newDiets = {
                "Puree ADA",
                "Full Liquid ADA",
                "Clear Liquid ADA"
        };

        String[] descriptions = {
                "Pureed consistency diet with ADA guidelines",
                "Full liquid diet with ADA guidelines",
                "Clear liquid diet with ADA substitutions (Sprite Zero, Apple Juice, Sugar Free Jello)"
        };

        for (int i = 0; i < newDiets.length; i++) {
            db.execSQL("INSERT OR IGNORE INTO Diet (name, description) VALUES (?, ?)",
                    new String[]{newDiets[i], descriptions[i]});
        }
    }

    private void insertDefaultData(SQLiteDatabase db) {
        // Insert default categories first
        insertDefaultCategories(db);

        // Insert default diets
        insertDefaultDiets(db);

        // Insert default food items
        insertDefaultItems(db);

        // Insert default user
        insertDefaultUser(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        String[] categories = {
                "Breakfast Items",
                "Proteins",
                "Starches",
                "Vegetables",
                "Beverages",
                "Juices",
                "Desserts",
                "Fruits",
                "Dairy"
        };

        for (int i = 0; i < categories.length; i++) {
            db.execSQL("INSERT OR IGNORE INTO Category (name, display_order) VALUES (?, ?)",
                    new Object[]{categories[i], i + 1});
        }
    }

    private void insertDefaultDiets(SQLiteDatabase db) {
        // UPDATED: Complete diet types including ADA varieties
        String[] diets = {
                "Regular",
                "Cardiac",
                "ADA",
                "Puree",
                "Puree ADA",
                "Renal",
                "Full Liquid",
                "Full Liquid ADA",
                "Clear Liquid",
                "Clear Liquid ADA"
        };

        String[] descriptions = {
                "Standard regular diet",
                "Heart-healthy cardiac diet",
                "American Diabetic Association diet",
                "Pureed consistency diet",
                "Pureed consistency diet with ADA guidelines",
                "Kidney-friendly renal diet",
                "Full liquid diet",
                "Full liquid diet with ADA guidelines",
                "Clear liquid diet with predetermined items",
                "Clear liquid diet with ADA substitutions (Sprite Zero, Apple Juice, Sugar Free Jello)"
        };

        for (int i = 0; i < diets.length; i++) {
            db.execSQL("INSERT OR IGNORE INTO Diet (name, description) VALUES (?, ?)",
                    new String[]{diets[i], descriptions[i]});
        }
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        // Get category IDs for proper foreign key references
        // Category 1: Breakfast Items
        insertItem(db, "Scrambled Eggs", 1, "Breakfast", 1, 0, 1, "Fluffy scrambled eggs");
        insertItem(db, "Toast", 1, "Breakfast", 1, 0, 1, "Whole wheat toast");
        insertItem(db, "Oatmeal", 1, "Breakfast", 1, 0, 1, "Hot oatmeal cereal");
        insertItem(db, "Cold Cereal", 1, "Breakfast", 1, 0, 1, "Assorted cold cereals");
        insertItem(db, "Pancakes", 1, "Breakfast", 0, 0, 0, "Fluffy pancakes");
        insertItem(db, "French Toast", 1, "Breakfast", 0, 0, 0, "Classic french toast");
        insertItem(db, "Muffin", 1, "Breakfast", 0, 0, 0, "Bran or blueberry muffin");

        // Category 2: Proteins
        insertItem(db, "Grilled Chicken", 2, "Lunch", 1, 0, 1, "Grilled chicken breast");
        insertItem(db, "Baked Fish", 2, "Dinner", 1, 0, 1, "Baked white fish");
        insertItem(db, "Beef Stew", 2, "Dinner", 1, 0, 0, "Tender beef stew");
        insertItem(db, "Turkey Sandwich", 2, "Lunch", 1, 0, 0, "Sliced turkey sandwich");
        insertItem(db, "Salmon", 2, "Dinner", 1, 0, 0, "Baked salmon fillet");
        insertItem(db, "Hamburger", 2, "Lunch", 0, 0, 0, "Beef hamburger patty");

        // Category 3: Starches
        insertItem(db, "Rice", 3, "Lunch", 1, 0, 1, "White or brown rice");
        insertItem(db, "Mashed Potatoes", 3, "Dinner", 1, 0, 1, "Creamy mashed potatoes");
        insertItem(db, "Pasta", 3, "Dinner", 1, 0, 0, "Various pasta dishes");
        insertItem(db, "Bread Roll", 3, "Lunch", 1, 0, 1, "Dinner roll");
        insertItem(db, "Baked Potato", 3, "Dinner", 1, 0, 0, "Baked russet potato");

        // Category 4: Vegetables
        insertItem(db, "Green Beans", 4, "Dinner", 1, 0, 1, "Fresh green beans");
        insertItem(db, "Broccoli", 4, "Dinner", 1, 0, 1, "Steamed broccoli");
        insertItem(db, "Carrots", 4, "Lunch", 1, 0, 1, "Cooked carrots");
        insertItem(db, "Corn", 4, "Lunch", 1, 0, 0, "Sweet corn");
        insertItem(db, "Garden Salad", 4, "Lunch", 1, 0, 0, "Mixed greens salad");

        // Category 5: Beverages (Clear liquid compatible)
        insertItem(db, "Water", 5, "All", 1, 0, 1, 240, "Drinking water");
        insertItem(db, "Coffee", 5, "Breakfast", 1, 0, 1, 240, "Hot coffee");
        insertItem(db, "Tea", 5, "All", 1, 0, 1, 240, "Hot tea");
        insertItem(db, "Milk", 5, "Breakfast", 1, 0, 0, 240, "2% milk");
        insertItem(db, "Sprite", 5, "All", 0, 1, 1, 240, "Lemon-lime soda");
        insertItem(db, "Sprite Zero", 5, "All", 1, 1, 1, 240, "Sugar-free lemon-lime soda");
        insertItem(db, "Iced Tea", 5, "Lunch", 1, 0, 1, 240, "Iced tea");

        // Category 6: Juices (Clear liquid compatible)
        insertItem(db, "Orange Juice", 6, "Breakfast", 0, 0, 0, 240, "Fresh orange juice");
        insertItem(db, "Apple Juice", 6, "All", 1, 0, 1, 240, "Clear apple juice");
        insertItem(db, "Cranberry Juice", 6, "All", 1, 0, 1, 240, "Cranberry juice");
        insertItem(db, "Grape Juice", 6, "All", 0, 0, 1, 240, "Purple grape juice");
        insertItem(db, "Tomato Juice", 6, "Breakfast", 1, 0, 0, 240, "Tomato juice");

        // Category 7: Desserts (with ADA options)
        insertItem(db, "Ice Cream", 7, "Dinner", 0, 0, 0, "Vanilla ice cream");
        insertItem(db, "Sugar Free Ice Cream", 7, "Dinner", 1, 0, 0, "ADA-friendly ice cream");
        insertItem(db, "Pudding", 7, "Lunch", 0, 0, 0, "Chocolate pudding");
        insertItem(db, "Sugar Free Pudding", 7, "Lunch", 1, 0, 0, "ADA-friendly pudding");
        insertItem(db, "Jello", 7, "All", 0, 0, 1, "Regular jello");
        insertItem(db, "Sugar Free Jello", 7, "All", 1, 0, 1, "ADA-friendly jello");
        insertItem(db, "Cake", 7, "Dinner", 0, 0, 0, "Slice of cake");
        insertItem(db, "Fresh Fruit", 7, "All", 1, 0, 0, "Seasonal fresh fruit");

        // Category 8: Fruits
        insertItem(db, "Banana", 8, "Breakfast", 1, 0, 0, "Fresh banana");
        insertItem(db, "Orange", 8, "Breakfast", 1, 0, 0, "Fresh orange");
        insertItem(db, "Apple", 8, "All", 1, 0, 0, "Fresh apple");
        insertItem(db, "Fruit Cup", 8, "All", 1, 0, 0, "Mixed fruit cup");
        insertItem(db, "Berries", 8, "Breakfast", 1, 0, 0, "Fresh berries");

        // Category 9: Dairy
        insertItem(db, "Yogurt", 9, "Breakfast", 1, 0, 0, "Plain or flavored yogurt");
        insertItem(db, "Greek Yogurt", 9, "Breakfast", 1, 0, 0, "Greek style yogurt");
        insertItem(db, "Cheese", 9, "Lunch", 1, 0, 0, "Sliced cheese");
        insertItem(db, "Cottage Cheese", 9, "Lunch", 1, 0, 0, "Low-fat cottage cheese");
    }

    private void insertItem(SQLiteDatabase db, String name, int categoryId, String mealType, int isAdaFriendly, int isSoda, int isClearLiquid, String description) {
        insertItem(db, name, categoryId, mealType, isAdaFriendly, isSoda, isClearLiquid, null, description);
    }

    private void insertItem(SQLiteDatabase db, String name, int categoryId, String mealType, int isAdaFriendly, int isSoda, int isClearLiquid, Integer fluidAmount, String description) {
        String sql = "INSERT OR IGNORE INTO Item (name, category_id, meal_type, is_ada_friendly, size_ml, is_soda, is_clear_liquid, is_default, description) VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?)";
        db.execSQL(sql, new Object[]{name, categoryId, mealType, isAdaFriendly, fluidAmount, isSoda, isClearLiquid, description});
    }

    private void insertDefaultUser(SQLiteDatabase db) {
        // Insert default admin user (username: admin, password: admin123)
        db.execSQL("INSERT OR IGNORE INTO User (username, password, full_name, role, created_date) " +
                        "VALUES (?, ?, ?, ?, datetime('now'))",
                new String[]{"admin", "admin123", "System Administrator", "admin"});
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON");
    }
}