package com.hospital.dietary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "DietaryMenu.db";
    private static final int DATABASE_VERSION = 10; // Incremented to force update

    // Table Names
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_PATIENT_INFO = "PatientInfo";
    public static final String TABLE_ITEMS = "Item"; // Fixed: Changed to "Item" to match DAO
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

        if (oldVersion < 10) {
            // Drop and recreate Item table with correct schema
            db.execSQL("DROP TABLE IF EXISTS Item");
            db.execSQL("DROP TABLE IF EXISTS Items");
            createItemsTable(db);
            insertDefaultItems(db);
            Log.d(TAG, "Recreated Item table with correct schema");
        }

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
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_PATIENT_INFO_TABLE);
        Log.d(TAG, "PatientInfo table created");
    }

    private void createItemsTable(SQLiteDatabase db) {
        // Fixed: Create table with columns that match what ItemDAO expects
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + " (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE NOT NULL, " +
                "category TEXT, " +
                "size_ml INTEGER, " +
                "description TEXT, " +
                "is_ada_friendly INTEGER DEFAULT 0, " +
                "ada_friendly INTEGER DEFAULT 0, " +
                "is_soda INTEGER DEFAULT 0, " +
                "is_clear_liquid INTEGER DEFAULT 0, " +
                "meal_type TEXT, " +
                "is_default INTEGER DEFAULT 0, " +
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_ITEMS_TABLE);
        Log.d(TAG, "Item table created with correct schema");
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
                "created_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(CREATE_DEFAULT_MENU_TABLE);
        Log.d(TAG, "DefaultMenu table created");
    }

    private void insertDefaultUsers(SQLiteDatabase db) {
        try {
            // Insert admin user
            ContentValues adminValues = new ContentValues();
            adminValues.put("username", "admin");
            adminValues.put("password", "admin123");
            adminValues.put("full_name", "System Administrator");
            adminValues.put("user_role", "Admin");
            adminValues.put("is_active", 1);
            adminValues.put("force_password_change", 1);
            db.insert(TABLE_USERS, null, adminValues);

            // Insert regular user
            ContentValues userValues = new ContentValues();
            userValues.put("username", "user");
            userValues.put("password", "user123");
            userValues.put("full_name", "Regular User");
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
            Log.d(TAG, "Starting to insert default items");

            // Proteins
            insertItem(db, "Grilled Chicken Breast", "Proteins", 0, "Lean grilled chicken", true);
            insertItem(db, "Roast Beef", "Proteins", 0, "Sliced roast beef", true);
            insertItem(db, "Baked Fish", "Proteins", 0, "Fresh baked fish fillet", true);
            insertItem(db, "Turkey Sandwich", "Proteins", 0, "Sliced turkey on bread", true);
            insertItem(db, "Scrambled Eggs", "Proteins", 0, "Fresh scrambled eggs", true);
            insertItem(db, "Bacon", "Proteins", 0, "Crispy bacon strips", false);
            insertItem(db, "Ham", "Proteins", 0, "Glazed ham slices", false);
            insertItem(db, "Meatloaf", "Proteins", 0, "Traditional meatloaf", false);
            insertItem(db, "Fried Chicken", "Proteins", 0, "Breaded fried chicken", false);
            insertItem(db, "Pork Chop", "Proteins", 0, "Grilled pork chop", false);

            // Starches
            insertItem(db, "White Rice", "Starches", 0, "Steamed white rice", true);
            insertItem(db, "Brown Rice", "Starches", 0, "Steamed brown rice", true);
            insertItem(db, "Mashed Potatoes", "Starches", 0, "Creamy mashed potatoes", true);
            insertItem(db, "Baked Potato", "Starches", 0, "Whole baked potato", true);
            insertItem(db, "Sweet Potato", "Starches", 0, "Roasted sweet potato", true);
            insertItem(db, "Whole Wheat Bread", "Starches", 0, "Fresh whole wheat bread", true);
            insertItem(db, "White Bread", "Starches", 0, "Fresh white bread", true);
            insertItem(db, "Dinner Roll", "Starches", 0, "Soft dinner roll", true);
            insertItem(db, "Pasta", "Starches", 0, "Cooked pasta", true);
            insertItem(db, "French Fries", "Starches", 0, "Golden french fries", false);

            // Vegetables
            insertItem(db, "Steamed Broccoli", "Vegetables", 0, "Fresh steamed broccoli", true);
            insertItem(db, "Green Beans", "Vegetables", 0, "Fresh green beans", true);
            insertItem(db, "Carrots", "Vegetables", 0, "Steamed carrots", true);
            insertItem(db, "Corn", "Vegetables", 0, "Sweet corn kernels", true);
            insertItem(db, "Peas", "Vegetables", 0, "Green peas", true);
            insertItem(db, "Mixed Vegetables", "Vegetables", 0, "Seasonal mixed vegetables", true);
            insertItem(db, "Spinach", "Vegetables", 0, "Fresh spinach", true);
            insertItem(db, "Asparagus", "Vegetables", 0, "Grilled asparagus", true);
            insertItem(db, "Cauliflower", "Vegetables", 0, "Steamed cauliflower", true);
            insertItem(db, "Brussels Sprouts", "Vegetables", 0, "Roasted brussels sprouts", true);

            // Beverages
            insertItem(db, "Water", "Beverages", 240, "Filtered water", true);
            insertItem(db, "Coffee", "Beverages", 240, "Fresh brewed coffee", true);
            insertItem(db, "Tea", "Beverages", 240, "Hot tea varieties", true);
            insertItem(db, "Milk", "Beverages", 240, "Low-fat milk", true);
            insertItem(db, "Skim Milk", "Beverages", 240, "Non-fat milk", true);
            insertItem(db, "Orange Juice", "Beverages", 240, "Fresh orange juice", true);
            insertItem(db, "Apple Juice", "Beverages", 240, "100% apple juice", true);
            insertItem(db, "Cranberry Juice", "Beverages", 240, "Unsweetened cranberry juice", true);
            insertItem(db, "Grape Juice", "Beverages", 240, "100% grape juice", false);
            insertItem(db, "Soda", "Beverages", 240, "Carbonated soft drink", false);

            // Fruits
            insertItem(db, "Fresh Apple", "Fruits", 0, "Crisp fresh apple", true);
            insertItem(db, "Fresh Banana", "Fruits", 0, "Ripe banana", true);
            insertItem(db, "Fresh Orange", "Fruits", 0, "Juicy orange", true);
            insertItem(db, "Fresh Berries", "Fruits", 0, "Mixed fresh berries", true);
            insertItem(db, "Applesauce", "Fruits", 0, "Unsweetened applesauce", true);
            insertItem(db, "Fruit Cocktail", "Fruits", 0, "Mixed fruit cocktail", true);
            insertItem(db, "Peaches", "Fruits", 0, "Fresh or canned peaches", true);
            insertItem(db, "Pears", "Fruits", 0, "Fresh or canned pears", true);
            insertItem(db, "Grapes", "Fruits", 0, "Fresh grapes", true);
            insertItem(db, "Melon", "Fruits", 0, "Fresh cantaloupe or honeydew", true);

            // Desserts
            insertItem(db, "Sugar-Free Jello", "Desserts", 0, "Sugar-free gelatin", true);
            insertItem(db, "Fresh Fruit", "Desserts", 0, "Seasonal fresh fruit", true);
            insertItem(db, "Yogurt", "Desserts", 0, "Low-fat yogurt", true);
            insertItem(db, "Pudding", "Desserts", 0, "Sugar-free pudding", true);
            insertItem(db, "Ice Cream", "Desserts", 0, "Vanilla ice cream", false);
            insertItem(db, "Cake", "Desserts", 0, "Slice of cake", false);
            insertItem(db, "Cookies", "Desserts", 0, "Fresh baked cookies", false);
            insertItem(db, "Pie", "Desserts", 0, "Fruit or cream pie", false);

            // Breakfast Items
            insertItem(db, "Oatmeal", "Breakfast Items", 0, "Hot oatmeal cereal", true);
            insertItem(db, "Cold Cereal", "Breakfast Items", 0, "Assorted cold cereals", true);
            insertItem(db, "Pancakes", "Breakfast Items", 0, "Fluffy pancakes", false);
            insertItem(db, "French Toast", "Breakfast Items", 0, "Golden french toast", false);
            insertItem(db, "Waffles", "Breakfast Items", 0, "Belgian waffles", false);
            insertItem(db, "Toast", "Breakfast Items", 0, "Toasted bread", true);
            insertItem(db, "Muffin", "Breakfast Items", 0, "Fresh baked muffin", false);
            insertItem(db, "Bagel", "Breakfast Items", 0, "Fresh bagel", true);

            // Condiments and Extras
            insertItem(db, "Butter", "Condiments", 0, "Fresh butter", false);
            insertItem(db, "Margarine", "Condiments", 0, "Low-fat margarine", true);
            insertItem(db, "Jelly", "Condiments", 0, "Assorted jelly", false);
            insertItem(db, "Sugar-Free Jelly", "Condiments", 0, "Sugar-free jelly", true);
            insertItem(db, "Honey", "Condiments", 0, "Natural honey", false);
            insertItem(db, "Salt", "Condiments", 0, "Table salt", false);
            insertItem(db, "Pepper", "Condiments", 0, "Black pepper", true);
            insertItem(db, "Ketchup", "Condiments", 0, "Tomato ketchup", false);

            Log.d(TAG, "Finished inserting default items");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default items", e);
        }
    }

    private void insertItem(SQLiteDatabase db, String name, String category, int sizeML, String description, boolean adaFriendly) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("size_ml", sizeML);
        values.put("description", description);
        values.put("is_ada_friendly", adaFriendly ? 1 : 0);
        values.put("ada_friendly", adaFriendly ? 1 : 0);
        values.put("is_soda", name.toLowerCase().contains("soda") ? 1 : 0);
        values.put("is_clear_liquid", (name.toLowerCase().contains("juice") || name.toLowerCase().contains("water") || name.toLowerCase().contains("tea")) ? 1 : 0);
        values.put("is_default", 1);

        long result = db.insert(TABLE_ITEMS, null, values);
        if (result == -1) {
            Log.e(TAG, "Failed to insert item: " + name);
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
            String[] lunchItems = {"Grilled Chicken Breast", "White Rice", "Green Beans", "Dinner Roll", "Apple Juice", "Tea", "Fresh Fruit"};
            for (String item : lunchItems) {
                ContentValues values = new ContentValues();
                values.put("meal_type", "Lunch");
                values.put("item_name", item);
                values.put("category", categorizeItem(item));
                values.put("is_active", 1);
                db.insert(TABLE_DEFAULT_MENU, null, values);
            }

            // Dinner items
            String[] dinnerItems = {"Roast Beef", "Mashed Potatoes", "Carrots", "Dinner Roll", "Ice Cream", "Coffee", "Tea"};
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
        if (lowerItem.contains("juice") || lowerItem.contains("coffee") || lowerItem.contains("tea") || lowerItem.contains("milk") || lowerItem.contains("water")) {
            return "Beverages";
        } else if (lowerItem.contains("chicken") || lowerItem.contains("beef") || lowerItem.contains("egg") || lowerItem.contains("bacon") || lowerItem.contains("fish") || lowerItem.contains("turkey") || lowerItem.contains("ham")) {
            return "Proteins";
        } else if (lowerItem.contains("rice") || lowerItem.contains("potato") || lowerItem.contains("toast") || lowerItem.contains("roll") || lowerItem.contains("bread") || lowerItem.contains("pasta")) {
            return "Starches";
        } else if (lowerItem.contains("beans") || lowerItem.contains("carrot") || lowerItem.contains("broccoli") || lowerItem.contains("corn") || lowerItem.contains("peas") || lowerItem.contains("spinach")) {
            return "Vegetables";
        } else if (lowerItem.contains("fruit") || lowerItem.contains("ice cream") || lowerItem.contains("cake") || lowerItem.contains("jello") || lowerItem.contains("pudding")) {
            return "Desserts";
        } else if (lowerItem.contains("pancake") || lowerItem.contains("cereal") || lowerItem.contains("oatmeal") || lowerItem.contains("muffin") || lowerItem.contains("bagel")) {
            return "Breakfast Items";
        } else {
            return "Other";
        }
    }
}