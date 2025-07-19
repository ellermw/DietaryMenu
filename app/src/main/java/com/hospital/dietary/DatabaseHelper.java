package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HospitalDietary.db";
    private static final int DATABASE_VERSION = 6; // FIXED: Incremented version for new schema

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
        insertDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Handle database upgrades incrementally
        if (oldVersion < 4) {
            upgradeToVersion4(db);
        }
        if (oldVersion < 5) {
            upgradeToVersion5(db);
        }
        if (oldVersion < 6) {
            upgradeToVersion6(db);
        }

        // Ensure all data is properly migrated
        updateDefaultData(db);
    }

    private void createAllTables(SQLiteDatabase db) {
        // Create User table
        createUserTable(db);

        // Create Category table
        createCategoryTable(db);

        // Create Diet table
        createDietTable(db);

        // Create Item table
        createItemTable(db);

        // Create PatientInfo table
        createPatientInfoTable(db);

        // FIXED: Create PatientMealSelection table for meal editing support
        createPatientMealSelectionTable(db);

        // FIXED: Create OrderHistory table for tracking changes
        createOrderHistoryTable(db);

        // FIXED: Create UserSession table for session management
        createUserSessionTable(db);
    }

    private void createUserTable(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE IF NOT EXISTS User (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "role TEXT NOT NULL DEFAULT 'user', " +
                "is_active INTEGER DEFAULT 1, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "last_login TEXT" +
                ")";
        db.execSQL(createUserTable);
    }

    private void createCategoryTable(SQLiteDatabase db) {
        String createCategoryTable = "CREATE TABLE IF NOT EXISTS Category (" +
                "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE NOT NULL, " +
                "description TEXT, " +
                "display_order INTEGER DEFAULT 0" +
                ")";
        db.execSQL(createCategoryTable);
    }

    private void createDietTable(SQLiteDatabase db) {
        String createDietTable = "CREATE TABLE IF NOT EXISTS Diet (" +
                "diet_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE NOT NULL, " +
                "description TEXT" +
                ")";
        db.execSQL(createDietTable);
    }

    private void createItemTable(SQLiteDatabase db) {
        String createItemTable = "CREATE TABLE IF NOT EXISTS Item (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "meal_type TEXT NOT NULL DEFAULT 'All', " +
                "size_ml INTEGER, " +
                "is_ada_friendly INTEGER DEFAULT 0, " +
                "is_soda INTEGER DEFAULT 0, " +
                "is_clear_liquid INTEGER DEFAULT 0, " +
                "is_default INTEGER DEFAULT 0, " +
                "is_active INTEGER DEFAULT 1, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (category_id) REFERENCES Category(category_id)" +
                ")";
        db.execSQL(createItemTable);
    }

    private void createPatientInfoTable(SQLiteDatabase db) {
        String createPatientTable = "CREATE TABLE IF NOT EXISTS PatientInfo (" +
                "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_first_name TEXT NOT NULL, " +
                "patient_last_name TEXT NOT NULL, " +
                "wing TEXT NOT NULL, " +
                "room_number TEXT NOT NULL, " +
                "diet TEXT, " +
                "fluid_restriction TEXT, " +
                "texture_modifications TEXT, " +
                "breakfast_complete INTEGER DEFAULT 0, " +
                "lunch_complete INTEGER DEFAULT 0, " +
                "dinner_complete INTEGER DEFAULT 0, " +
                "breakfast_npo INTEGER DEFAULT 0, " +
                "lunch_npo INTEGER DEFAULT 0, " +
                "dinner_npo INTEGER DEFAULT 0, " +
                "allergies TEXT, " +
                "special_instructions TEXT, " +
                "is_ada_friendly INTEGER DEFAULT 0, " +
                "retired INTEGER DEFAULT 0, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "last_modified TEXT DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(createPatientTable);
    }

    /**
     * FIXED: Create PatientMealSelection table for storing individual meal selections
     */
    private void createPatientMealSelectionTable(SQLiteDatabase db) {
        String createMealSelectionTable = "CREATE TABLE IF NOT EXISTS PatientMealSelection (" +
                "selection_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER NOT NULL, " +
                "meal_type TEXT NOT NULL, " +
                "item_id INTEGER, " +
                "item_name TEXT, " +
                "meal_selection TEXT, " +
                "quantity INTEGER DEFAULT 1, " +
                "special_instructions TEXT, " +
                "created_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (patient_id) REFERENCES PatientInfo(patient_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (item_id) REFERENCES Item(item_id)" +
                ")";
        db.execSQL(createMealSelectionTable);
    }

    /**
     * FIXED: Create OrderHistory table for tracking patient changes
     */
    private void createOrderHistoryTable(SQLiteDatabase db) {
        String createOrderHistoryTable = "CREATE TABLE IF NOT EXISTS OrderHistory (" +
                "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER NOT NULL, " +
                "action_type TEXT NOT NULL, " +
                "old_values TEXT, " +
                "new_values TEXT, " +
                "changed_by TEXT, " +
                "change_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (patient_id) REFERENCES PatientInfo(patient_id) ON DELETE CASCADE" +
                ")";
        db.execSQL(createOrderHistoryTable);
    }

    /**
     * FIXED: Create UserSession table for session management
     */
    private void createUserSessionTable(SQLiteDatabase db) {
        String createSessionTable = "CREATE TABLE IF NOT EXISTS UserSession (" +
                "session_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "session_token TEXT NOT NULL, " +
                "login_time TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "logout_time TEXT, " +
                "is_active INTEGER DEFAULT 1, " +
                "FOREIGN KEY (user_id) REFERENCES User(user_id)" +
                ")";
        db.execSQL(createSessionTable);
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        try {
            // Add new columns to existing tables
            db.execSQL("ALTER TABLE PatientInfo ADD COLUMN allergies TEXT");
            db.execSQL("ALTER TABLE PatientInfo ADD COLUMN special_instructions TEXT");
            db.execSQL("ALTER TABLE PatientInfo ADD COLUMN is_ada_friendly INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE PatientInfo ADD COLUMN retired INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE PatientInfo ADD COLUMN last_modified TEXT DEFAULT CURRENT_TIMESTAMP");

            // Add new columns to Item table
            db.execSQL("ALTER TABLE Item ADD COLUMN is_active INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE Item ADD COLUMN created_date TEXT DEFAULT CURRENT_TIMESTAMP");

            // Add new columns to User table
            db.execSQL("ALTER TABLE User ADD COLUMN is_active INTEGER DEFAULT 1");
            db.execSQL("ALTER TABLE User ADD COLUMN created_date TEXT DEFAULT CURRENT_TIMESTAMP");
            db.execSQL("ALTER TABLE User ADD COLUMN last_login TEXT");

        } catch (Exception e) {
            Log.d("DatabaseHelper", "Version 4 upgrade: " + e.getMessage());
        }
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
        try {
            // Create new tables introduced in version 5
            createPatientMealSelectionTable(db);
            createOrderHistoryTable(db);

        } catch (Exception e) {
            Log.d("DatabaseHelper", "Version 5 upgrade: " + e.getMessage());
        }
    }

    private void upgradeToVersion6(SQLiteDatabase db) {
        try {
            // Create session management table
            createUserSessionTable(db);

            // Update diet options to remove redundant ADA variants
            updateDietOptions(db);

        } catch (Exception e) {
            Log.d("DatabaseHelper", "Version 6 upgrade: " + e.getMessage());
        }
    }

    private void updateDietOptions(SQLiteDatabase db) {
        // FIXED: Remove redundant ADA diet options and update existing patients
        try {
            // Update patients with old ADA diet names to new format
            db.execSQL("UPDATE PatientInfo SET diet = 'Clear Liquid ADA' WHERE diet = 'Clear Liquid ADA'");
            db.execSQL("UPDATE PatientInfo SET diet = 'ADA Diabetic' WHERE diet IN ('Puree ADA', 'Full Liquid ADA')");

            // Clean up old diet entries (optional - we can keep them for backwards compatibility)
            // db.execSQL("DELETE FROM Diet WHERE name IN ('Puree ADA', 'Full Liquid ADA')");

        } catch (Exception e) {
            Log.d("DatabaseHelper", "Diet options update: " + e.getMessage());
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

    private void updateDefaultData(SQLiteDatabase db) {
        // Update any missing default data during upgrades
        insertDefaultCategories(db);
        insertDefaultDiets(db);
        insertDefaultUser(db);

        // Update existing items if needed
        updateItemCategories(db);
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
        // FIXED: Simplified diet types (base diets only)
        String[] diets = {
                "Regular",
                "Cardiac",
                "ADA Diabetic",
                "Puree",
                "Renal",
                "Full Liquid",
                "Clear Liquid",
                "Clear Liquid ADA" // Only keep this ADA variant for Clear Liquid
        };

        String[] descriptions = {
                "Standard regular diet",
                "Heart-healthy cardiac diet",
                "American Diabetic Association diet",
                "Pureed consistency diet",
                "Kidney-friendly renal diet",
                "Full liquid diet",
                "Clear liquid diet with predetermined items",
                "Clear liquid diet with ADA substitutions (Sprite Zero, Apple Juice, Sugar Free items)"
        };

        for (int i = 0; i < diets.length; i++) {
            db.execSQL("INSERT OR IGNORE INTO Diet (name, description) VALUES (?, ?)",
                    new Object[]{diets[i], descriptions[i]});
        }
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        // Helper method to insert items
        insertItem(db, "Scrambled Eggs", 1, "Breakfast", 1, 0, 0, "Fresh scrambled eggs");
        insertItem(db, "Pancakes", 1, "Breakfast", 0, 0, 0, "Fluffy pancakes");
        insertItem(db, "Oatmeal", 1, "Breakfast", 1, 0, 0, "Hot oatmeal");
        insertItem(db, "French Toast", 1, "Breakfast", 0, 0, 0, "Golden french toast");
        insertItem(db, "Bacon", 1, "Breakfast", 0, 0, 0, "Crispy bacon strips");

        // Category 2: Proteins
        insertItem(db, "Grilled Chicken", 2, "Lunch", 1, 0, 0, "Seasoned grilled chicken breast");
        insertItem(db, "Baked Fish", 2, "Dinner", 1, 0, 0, "Fresh baked white fish");
        insertItem(db, "Beef Roast", 2, "Dinner", 1, 0, 0, "Tender beef roast");
        insertItem(db, "Turkey Sandwich", 2, "Lunch", 1, 0, 0, "Sliced turkey sandwich");
        insertItem(db, "Hamburger", 2, "Lunch", 0, 0, 0, "Grilled hamburger patty");

        // Category 3: Starches
        insertItem(db, "Mashed Potatoes", 3, "Dinner", 1, 0, 0, "Creamy mashed potatoes");
        insertItem(db, "Rice", 3, "Lunch", 1, 0, 0, "Steamed white rice");
        insertItem(db, "Pasta", 3, "Dinner", 1, 0, 0, "Italian pasta");
        insertItem(db, "Bread Roll", 3, "All", 1, 0, 0, "Fresh bread roll");
        insertItem(db, "Baked Potato", 3, "Dinner", 1, 0, 0, "Baked russet potato");

        // Category 4: Vegetables
        insertItem(db, "Green Beans", 4, "Dinner", 1, 0, 0, "Fresh green beans");
        insertItem(db, "Broccoli", 4, "Dinner", 1, 0, 0, "Steamed broccoli");
        insertItem(db, "Carrots", 4, "Lunch", 1, 0, 0, "Cooked carrots");
        insertItem(db, "Corn", 4, "Lunch", 0, 0, 0, "Sweet corn");
        insertItem(db, "Garden Salad", 4, "Lunch", 1, 0, 0, "Mixed greens salad");

        // Category 5: Beverages (Clear liquid compatible)
        insertItem(db, "Water", 5, "All", 1, 0, 1, 240, "Drinking water");
        insertItem(db, "Coffee", 5, "Breakfast", 1, 0, 1, 240, "Hot coffee");
        insertItem(db, "Tea", 5, "All", 1, 0, 1, 240, "Hot tea");
        insertItem(db, "Milk", 5, "Breakfast", 0, 0, 0, 240, "2% milk");
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

        // Category 8: Fruits
        insertItem(db, "Apple Slices", 8, "All", 1, 0, 0, "Fresh apple slices");
        insertItem(db, "Banana", 8, "All", 1, 0, 0, "Fresh banana");
        insertItem(db, "Orange Slices", 8, "All", 1, 0, 0, "Fresh orange slices");
        insertItem(db, "Mixed Fruit", 8, "All", 1, 0, 0, "Mixed fruit cup");

        // Category 9: Dairy
        insertItem(db, "Yogurt", 9, "All", 1, 0, 0, "Plain yogurt");
        insertItem(db, "Sugar Free Yogurt", 9, "All", 1, 0, 0, "ADA-friendly yogurt");
        insertItem(db, "Cheese", 9, "All", 1, 0, 0, "Sliced cheese");
        insertItem(db, "Cottage Cheese", 9, "All", 1, 0, 0, "Low-fat cottage cheese");
    }

    // Helper method to insert items with size
    private void insertItem(SQLiteDatabase db, String name, int categoryId, String mealType,
                            int isAda, int isSoda, int isClearLiquid, int sizeML, String description) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category_id", categoryId);
        values.put("meal_type", mealType);
        values.put("is_ada_friendly", isAda);
        values.put("is_soda", isSoda);
        values.put("is_clear_liquid", isClearLiquid);
        values.put("size_ml", sizeML);
        values.put("description", description);
        values.put("is_default", 1);

        db.insertWithOnConflict("Item", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    // Helper method to insert items without size
    private void insertItem(SQLiteDatabase db, String name, int categoryId, String mealType,
                            int isAda, int isSoda, int isClearLiquid, String description) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category_id", categoryId);
        values.put("meal_type", mealType);
        values.put("is_ada_friendly", isAda);
        values.put("is_soda", isSoda);
        values.put("is_clear_liquid", isClearLiquid);
        values.put("description", description);
        values.put("is_default", 1);

        db.insertWithOnConflict("Item", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void insertDefaultUser(SQLiteDatabase db) {
        // Insert default admin user
        ContentValues adminValues = new ContentValues();
        adminValues.put("username", "admin");
        adminValues.put("password", "admin123"); // In production, this should be hashed
        adminValues.put("full_name", "System Administrator");
        adminValues.put("role", "admin");
        adminValues.put("is_active", 1);

        db.insertWithOnConflict("User", null, adminValues, SQLiteDatabase.CONFLICT_IGNORE);

        // Insert default regular user
        ContentValues userValues = new ContentValues();
        userValues.put("username", "user");
        userValues.put("password", "user123"); // In production, this should be hashed
        userValues.put("full_name", "Regular User");
        userValues.put("role", "user");
        userValues.put("is_active", 1);

        db.insertWithOnConflict("User", null, userValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void updateItemCategories(SQLiteDatabase db) {
        try {
            // Update any items that might have null category_id
            db.execSQL("UPDATE Item SET category_id = 1 WHERE category_id IS NULL OR category_id = 0");

        } catch (Exception e) {
            Log.d("DatabaseHelper", "Item category update: " + e.getMessage());
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON");
    }
}