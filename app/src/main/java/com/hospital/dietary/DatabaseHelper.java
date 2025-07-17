package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "hospital_dietary.db";
    private static final int DATABASE_VERSION = 7; // FIXED: Incremented for schema fix

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign keys
        db.execSQL("PRAGMA foreign_keys = ON;");
        
        // Create tables
        createTables(db);
        
        // Insert initial data
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add User table for version 2
            createUserTable(db);
            insertDefaultUsers(db);
        }
        if (oldVersion < 3) {
            // Fix User table schema for version 3
            db.execSQL("DROP TABLE IF EXISTS User");
            createUserTable(db);
            insertDefaultUsers(db);
        }
        if (oldVersion < 4) {
            // Update Patient table for version 4 - add fluid restriction and texture modification columns
            db.execSQL("ALTER TABLE Patient ADD COLUMN fluid_restriction TEXT");
            db.execSQL("ALTER TABLE Patient ADD COLUMN texture_modifications TEXT");
        }
        if (oldVersion < 5) {
            // Add Clear Liquid items for version 5
            insertClearLiquidItems(db);
        }
        if (oldVersion < 6) {
            // Add PatientInfo table for version 6
            createPatientInfoTable(db);
            // Update diet types
            updateDietTypes(db);
        }
        if (oldVersion < 7) {
            // FIXED: Fix column name mismatch issue
            // Create new table with correct schema
            db.execSQL("CREATE TABLE IF NOT EXISTS Item_new (" +
                    "item_id INTEGER PRIMARY KEY," +
                    "category_id INTEGER," +
                    "name TEXT NOT NULL," +
                    "size_ml INTEGER," +
                    "is_ada_friendly BOOLEAN NOT NULL DEFAULT 0," +
                    "is_soda BOOLEAN NOT NULL DEFAULT 0," +
                    "is_clear_liquid BOOLEAN NOT NULL DEFAULT 0," +
                    "FOREIGN KEY (category_id) REFERENCES Category(category_id))");

            // Copy data from old table to new table, mapping ada_friendly to is_ada_friendly
            db.execSQL("INSERT INTO Item_new (item_id, category_id, name, size_ml, is_ada_friendly, is_soda, is_clear_liquid) " +
                    "SELECT item_id, category_id, name, size_ml, " +
                    "COALESCE(ada_friendly, 0) as is_ada_friendly, " +
                    "COALESCE(is_soda, 0) as is_soda, " +
                    "COALESCE(is_clear_liquid, 0) as is_clear_liquid " +
                    "FROM Item");

            // Drop old table and rename new table
            db.execSQL("DROP TABLE Item");
            db.execSQL("ALTER TABLE Item_new RENAME TO Item");
        }
    }

    private void createTables(SQLiteDatabase db) {
        // User table (for authentication and role management)
        createUserTable(db);
        
        // PatientInfo table (for patient management)
        createPatientInfoTable(db);
        
        // Category table
        db.execSQL("CREATE TABLE IF NOT EXISTS Category (" +
                "category_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL UNIQUE)");

        // Item table - FIXED: Use is_ada_friendly instead of ada_friendly
        db.execSQL("CREATE TABLE IF NOT EXISTS Item (" +
                "item_id INTEGER PRIMARY KEY," +
                "category_id INTEGER," +
                "name TEXT NOT NULL," +
                "size_ml INTEGER," +
                "is_ada_friendly BOOLEAN NOT NULL DEFAULT 0," +
                "is_soda BOOLEAN NOT NULL DEFAULT 0," +
                "is_clear_liquid BOOLEAN NOT NULL DEFAULT 0," +
                "FOREIGN KEY (category_id) REFERENCES Category(category_id))");

        // Diet table - Updated with only the 7 requested diet types
        db.execSQL("CREATE TABLE IF NOT EXISTS Diet (" +
                "diet_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL UNIQUE)");

        // Patient table (legacy - keeping for compatibility)
        db.execSQL("CREATE TABLE IF NOT EXISTS Patient (" +
                "patient_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "room_number TEXT NOT NULL," +
                "wing TEXT NOT NULL," +
                "diet_id INTEGER," +
                "fluid_restriction TEXT," +
                "texture_modifications TEXT," +
                "FOREIGN KEY (diet_id) REFERENCES Diet(diet_id))");

        // MealOrder table
        db.execSQL("CREATE TABLE IF NOT EXISTS MealOrder (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patient_id INTEGER," +
                "meal TEXT NOT NULL," +
                "timestamp TEXT NOT NULL," +
                "FOREIGN KEY (patient_id) REFERENCES Patient(patient_id))");

        // MealLine table
        db.execSQL("CREATE TABLE IF NOT EXISTS MealLine (" +
                "line_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "item_id INTEGER," +
                "quantity INTEGER NOT NULL DEFAULT 1," +
                "FOREIGN KEY (order_id) REFERENCES MealOrder(order_id)," +
                "FOREIGN KEY (item_id) REFERENCES Item(item_id))");

        // FinalizedOrder table
        db.execSQL("CREATE TABLE IF NOT EXISTS FinalizedOrder (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patient_name TEXT NOT NULL," +
                "wing TEXT NOT NULL," +
                "room TEXT NOT NULL," +
                "order_date TEXT NOT NULL," +
                "diet_type TEXT NOT NULL," +
                "fluid_restriction TEXT," +
                "mechanical_ground BOOLEAN NOT NULL DEFAULT 0," +
                "mechanical_chopped BOOLEAN NOT NULL DEFAULT 0," +
                "bite_size BOOLEAN NOT NULL DEFAULT 0," +
                "bread_ok BOOLEAN NOT NULL DEFAULT 0," +
                "breakfast_items TEXT," +
                "lunch_items TEXT," +
                "dinner_items TEXT," +
                "breakfast_juices TEXT," +
                "lunch_juices TEXT," +
                "dinner_juices TEXT," +
                "breakfast_drinks TEXT," +
                "lunch_drinks TEXT," +
                "dinner_drinks TEXT)");
    }

    private void createUserTable(SQLiteDatabase db) {
        // User table for authentication and role management
        db.execSQL("CREATE TABLE IF NOT EXISTS User (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL CHECK(role IN ('admin', 'user'))," +
                "full_name TEXT NOT NULL," +
                "email TEXT," +
                "is_active INTEGER NOT NULL DEFAULT 1," +
                "created_date TEXT NOT NULL)");
    }

    private void createPatientInfoTable(SQLiteDatabase db) {
        // PatientInfo table for the new patient management system
        db.execSQL("CREATE TABLE IF NOT EXISTS PatientInfo (" +
                "patient_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "wing TEXT NOT NULL," +
                "room_number TEXT NOT NULL," +
                "diet TEXT NOT NULL," +
                "fluid_restriction TEXT," +
                "texture_modifications TEXT," +
                "breakfast_complete INTEGER NOT NULL DEFAULT 0," +
                "lunch_complete INTEGER NOT NULL DEFAULT 0," +
                "dinner_complete INTEGER NOT NULL DEFAULT 0," +
                "breakfast_npo INTEGER NOT NULL DEFAULT 0," +
                "lunch_npo INTEGER NOT NULL DEFAULT 0," +
                "dinner_npo INTEGER NOT NULL DEFAULT 0," +
                "created_date TEXT NOT NULL)");
    }

    private void insertInitialData(SQLiteDatabase db) {
        // Insert default users first
        insertDefaultUsers(db);
        
        // Insert updated Diet types (only the 7 requested)
        insertDietTypes(db);
        
        // Insert Categories
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (1, 'Breakfast')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (2, 'Protein/Entrée')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (3, 'Starch')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (4, 'Vegetable')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (5, 'Grill Item')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (6, 'Dessert')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (7, 'Sugar Free Dessert')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (8, 'Drink')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (9, 'Supplement')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (10, 'Soda')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (11, 'Juices')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (12, 'Cold Cereals')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (13, 'Hot Cereals')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (14, 'Breads')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (15, 'Fresh Muffins')");
        db.execSQL("INSERT OR IGNORE INTO Category VALUES (16, 'Fruits')");

        // Insert all food items including Clear Liquid items
        insertFoodItems(db);
        insertClearLiquidItems(db);
    }

    private void insertDefaultUsers(SQLiteDatabase db) {
        // Insert default admin user
        db.execSQL("INSERT OR IGNORE INTO User (username, password, role, full_name, email, is_active, created_date) " +
                "VALUES ('admin', 'admin123', 'admin', 'System Administrator', 'admin@hospital.com', 1, datetime('now'))");
        
        // Insert default regular user
        db.execSQL("INSERT OR IGNORE INTO User (username, password, role, full_name, email, is_active, created_date) " +
                "VALUES ('user', 'user123', 'user', 'Regular User', 'user@hospital.com', 1, datetime('now'))");
    }

    private void insertDietTypes(SQLiteDatabase db) {
        // Insert only the 7 requested diet types
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (1, 'Regular')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (2, 'ADA')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (3, 'Cardiac')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (4, 'Renal')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (5, 'Puree')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (6, 'Full Liquid')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (7, 'Clear Liquid')");
    }

    private void updateDietTypes(SQLiteDatabase db) {
        // Clear existing diet types and insert only the 7 requested ones
        db.execSQL("DELETE FROM Diet");
        insertDietTypes(db);
    }

    private void insertFoodItems(SQLiteDatabase db) {
        // Cold Cereals (category 12)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (1, 12, 'Cheerios', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (2, 12, 'Cornflakes', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (3, 12, 'Rice Krispies', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (4, 12, 'Frosted Flakes', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (5, 12, 'Fruit Loops', 0, 0, 0, 0)");

        // Hot Cereals (category 13)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (6, 13, 'Oatmeal', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (7, 13, 'Cream of Wheat', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (8, 13, 'Grits', 0, 1, 0, 0)");

        // Breads (category 14)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (9, 14, 'White Bread', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (10, 14, 'Wheat Bread', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (11, 14, 'Rye Bread', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (12, 14, 'Sourdough', 0, 1, 0, 0)");

        // Fresh Muffins (category 15)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (13, 15, 'Blueberry Muffin', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (14, 15, 'Bran Muffin', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (15, 15, 'Chocolate Chip Muffin', 0, 0, 0, 0)");

        // Breakfast items (category 1)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (16, 1, 'Scrambled Eggs', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (17, 1, 'Pancakes', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (18, 1, 'French Toast', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (19, 1, 'Bacon', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (20, 1, 'Sausage', 0, 1, 0, 0)");

        // Fruits (category 16)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (21, 16, 'Apple', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (22, 16, 'Banana', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (23, 16, 'Orange', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (24, 16, 'Grapes', 0, 1, 0, 0)");

        // Protein/Entrée items (category 2)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (25, 2, 'Grilled Chicken', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (26, 2, 'Baked Fish', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (27, 2, 'Roast Beef', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (28, 2, 'Pork Chop', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (29, 2, 'Turkey', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (30, 2, 'Meatloaf', 0, 1, 0, 0)");

        // Starch items (category 3)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (31, 3, 'Mashed Potatoes', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (32, 3, 'Baked Potato', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (33, 3, 'Rice', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (34, 3, 'Pasta', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (35, 3, 'French Fries', 0, 0, 0, 0)");

        // Vegetable items (category 4)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (40, 4, 'Green Beans', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (41, 4, 'Carrots', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (42, 4, 'Broccoli', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (43, 4, 'Corn', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (44, 4, 'Peas', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (45, 4, 'Spinach', 0, 1, 0, 0)");

        // Grill Items (category 5)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (46, 5, 'Hamburger', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (47, 5, 'Cheeseburger', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (48, 5, 'Grilled Cheese', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (49, 5, 'Hot Dog', 0, 1, 0, 0)");

        // Dessert items (category 6)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (50, 6, 'Chocolate Cake', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (51, 6, 'Vanilla Ice Cream', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (52, 6, 'Apple Pie', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (53, 6, 'Cookies', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (54, 6, 'Pudding', 0, 0, 0, 0)");

        // Sugar Free Dessert items (category 7)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (55, 7, 'Sugar Free Cake', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (56, 7, 'Sugar Free Ice Cream', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (57, 7, 'Sugar Free Cookies', 0, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (58, 7, 'Sugar Free Pudding', 0, 1, 0, 0)");

        // Drink items (category 8)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (59, 8, 'Water', 240, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (60, 8, 'Iced Tea', 240, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (61, 8, 'Coffee', 240, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (62, 8, 'Decaf Coffee', 240, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (63, 8, 'Hot Tea', 240, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (64, 8, 'Hot Chocolate', 240, 0, 0, 0)");

        // Supplement items (category 9)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (65, 9, 'Ensure', 240, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (66, 9, 'Ensure Plus', 240, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (67, 9, 'Boost', 240, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (68, 9, 'Protein Shake', 240, 1, 0, 0)");

        // Soda items (category 10)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (69, 10, 'Coca Cola', 240, 0, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (70, 10, 'Diet Coke', 240, 1, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (71, 10, 'Pepsi', 240, 0, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (72, 10, 'Diet Pepsi', 240, 1, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (73, 10, 'Sprite', 240, 0, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (74, 10, 'Sprite Zero', 240, 1, 1, 1)");

        // Juice items (category 11)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (75, 11, 'Orange Juice', 120, 0, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (76, 11, 'Apple Juice', 120, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (77, 11, 'Cranberry Juice', 120, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (78, 11, 'Grape Juice', 120, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (79, 11, 'Tomato Juice', 120, 1, 0, 0)");
    }

    // Clear Liquid items for the Clear Liquid diet
    private void insertClearLiquidItems(SQLiteDatabase db) {
        // Clear Liquid specific items
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (100, 8, 'Chicken Broth', 200, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (101, 8, 'Beef Broth', 200, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (102, 8, 'Jello', 0, 0, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (103, 8, 'Sugar Free Jello', 0, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (104, 10, 'Ginger Ale', 240, 0, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (105, 10, 'Diet Ginger Ale', 240, 1, 1, 1)");
    }
}