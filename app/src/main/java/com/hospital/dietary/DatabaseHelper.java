// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/DatabaseHelper.java
// ================================================================================================

package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "hospital_dietary.db";
    private static final int DATABASE_VERSION = 5; // Incremented for Clear Liquid items

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
    }

    private void createTables(SQLiteDatabase db) {
        // User table (for authentication and role management)
        createUserTable(db);
        
        // Category table
        db.execSQL("CREATE TABLE IF NOT EXISTS Category (" +
                "category_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL UNIQUE)");

        // Item table
        db.execSQL("CREATE TABLE IF NOT EXISTS Item (" +
                "item_id INTEGER PRIMARY KEY," +
                "category_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "size_ml INTEGER," +
                "is_ada_friendly BOOLEAN NOT NULL DEFAULT 1," +
                "is_soda BOOLEAN NOT NULL DEFAULT 0," +
                "is_bread BOOLEAN NOT NULL DEFAULT 0," +
                "FOREIGN KEY(category_id) REFERENCES Category(category_id))");

        // ItemTag table
        db.execSQL("CREATE TABLE IF NOT EXISTS ItemTag (" +
                "item_id INTEGER," +
                "tag TEXT," +
                "PRIMARY KEY(item_id, tag)," +
                "FOREIGN KEY(item_id) REFERENCES Item(item_id))");

        // Diet table
        db.execSQL("CREATE TABLE IF NOT EXISTS Diet (" +
                "diet_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL UNIQUE)");

        // Patient table (updated with new columns)
        db.execSQL("CREATE TABLE IF NOT EXISTS Patient (" +
                "patient_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "room_number TEXT NOT NULL," +
                "wing TEXT NOT NULL," +
                "diet_id INTEGER," +
                "fluid_restriction TEXT," +
                "texture_modifications TEXT," +
                "FOREIGN KEY(diet_id) REFERENCES Diet(diet_id))");

        // FoodModification table
        db.execSQL("CREATE TABLE IF NOT EXISTS FoodModification (" +
                "mod_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL UNIQUE)");

        // PatientFoodMod table
        db.execSQL("CREATE TABLE IF NOT EXISTS PatientFoodMod (" +
                "patient_id INTEGER," +
                "mod_id INTEGER," +
                "PRIMARY KEY (patient_id, mod_id)," +
                "FOREIGN KEY(patient_id) REFERENCES Patient(patient_id)," +
                "FOREIGN KEY(mod_id) REFERENCES FoodModification(mod_id))");

        // AdditionalRestriction table
        db.execSQL("CREATE TABLE IF NOT EXISTS AdditionalRestriction (" +
                "restriction_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL UNIQUE)");

        // PatientAdditionalRestriction table
        db.execSQL("CREATE TABLE IF NOT EXISTS PatientAdditionalRestriction (" +
                "patient_id INTEGER," +
                "restriction_id INTEGER," +
                "PRIMARY KEY (patient_id, restriction_id)," +
                "FOREIGN KEY(patient_id) REFERENCES Patient(patient_id)," +
                "FOREIGN KEY(restriction_id) REFERENCES AdditionalRestriction(restriction_id))");

        // FluidRestriction table
        db.execSQL("CREATE TABLE IF NOT EXISTS FluidRestriction (" +
                "fluid_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL UNIQUE)");

        // RestrictionLimit table
        db.execSQL("CREATE TABLE IF NOT EXISTS RestrictionLimit (" +
                "fluid_id INTEGER," +
                "meal TEXT NOT NULL," +
                "limit_ml INTEGER NOT NULL," +
                "PRIMARY KEY (fluid_id, meal)," +
                "FOREIGN KEY(fluid_id) REFERENCES FluidRestriction(fluid_id))");

        // PatientFluidRestriction table
        db.execSQL("CREATE TABLE IF NOT EXISTS PatientFluidRestriction (" +
                "patient_id INTEGER," +
                "fluid_id INTEGER," +
                "PRIMARY KEY (patient_id, fluid_id)," +
                "FOREIGN KEY(patient_id) REFERENCES Patient(patient_id)," +
                "FOREIGN KEY(fluid_id) REFERENCES FluidRestriction(fluid_id))");

        // MealOrder table
        db.execSQL("CREATE TABLE IF NOT EXISTS MealOrder (" +
                "order_id INTEGER PRIMARY KEY," +
                "patient_id INTEGER," +
                "meal TEXT NOT NULL," +
                "guest_tray BOOLEAN NOT NULL DEFAULT 0," +
                "timestamp TEXT NOT NULL," +
                "FOREIGN KEY(patient_id) REFERENCES Patient(patient_id))");

        // MealLine table
        db.execSQL("CREATE TABLE IF NOT EXISTS MealLine (" +
                "line_id INTEGER PRIMARY KEY," +
                "order_id INTEGER," +
                "item_id INTEGER," +
                "FOREIGN KEY(order_id) REFERENCES MealOrder(order_id)," +
                "FOREIGN KEY(item_id) REFERENCES Item(item_id))");

        // FinalizedOrder table (for storing completed orders)
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
        // Schema matches UserDAO expectations
        db.execSQL("CREATE TABLE IF NOT EXISTS User (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +                    // Changed from password_hash
                "role TEXT NOT NULL CHECK(role IN ('admin', 'user'))," +
                "full_name TEXT NOT NULL," +                   // Added full_name
                "email TEXT," +                                // Added email
                "is_active INTEGER NOT NULL DEFAULT 1," +
                "created_date TEXT NOT NULL)");
    }

    private void insertInitialData(SQLiteDatabase db) {
        // Insert default users first
        insertDefaultUsers(db);
        
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

        // Insert Diets
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (1, 'Regular')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (2, 'ADA')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (3, 'Diabetic')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (4, 'Low Sodium')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (5, 'Cardiac')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (6, 'Renal')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (7, 'Puree')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (8, 'Full Liquid')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (9, 'Clear Liquid')");

        // Insert Fluid Restrictions
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (1, '1000ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (2, '1200ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (3, '1500ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (4, '1800ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (5, '2000ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (6, '2500ml')");

        // Insert Restriction Limits
        insertRestrictionLimits(db);

        // Insert all food items
        insertAllItems(db);
        
        // Insert Clear Liquid items
        insertClearLiquidItems(db);
    }

    private void insertDefaultUsers(SQLiteDatabase db) {
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
                                                            java.util.Locale.getDefault())
                                                            .format(new java.util.Date());

        // Insert default admin user (plain text password - matches UserDAO)
        db.execSQL("INSERT OR IGNORE INTO User (username, password, role, full_name, email, is_active, created_date) " +
                   "VALUES ('admin', 'admin123', 'admin', 'System Administrator', 'admin@hospital.com', 1, '" + currentDate + "')");

        // Insert default regular user
        db.execSQL("INSERT OR IGNORE INTO User (username, password, role, full_name, email, is_active, created_date) " +
                   "VALUES ('user', 'user123', 'user', 'Standard User', 'user@hospital.com', 1, '" + currentDate + "')");
    }

    private void insertRestrictionLimits(SQLiteDatabase db) {
        // 1000ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'breakfast', 120)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'lunch', 120)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'dinner', 160)");

        // 1200ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'breakfast', 250)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'lunch', 170)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'dinner', 180)");

        // 1500ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'breakfast', 350)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'lunch', 170)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'dinner', 180)");

        // 1800ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'breakfast', 360)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'lunch', 240)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'dinner', 240)");

        // 2000ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'breakfast', 320)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'lunch', 240)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'dinner', 280)");

        // 2500ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'breakfast', 400)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'lunch', 300)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'dinner', 350)");
    }

    private void insertAllItems(SQLiteDatabase db) {
        // Cold Cereals (category 12)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (1, 12, 'Cheerios', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (2, 12, 'Corn Flakes', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (3, 12, 'Rice Krispies', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (4, 12, 'Frosted Flakes', null, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (5, 12, 'Raisin Bran', null, 1, 0, 0)");

        // Hot Cereals (category 13)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (6, 13, 'Oatmeal', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (7, 13, 'Cream of Wheat', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (8, 13, 'Grits', null, 1, 0, 0)");

        // Breads (category 14)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (9, 14, 'White Toast', null, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (10, 14, 'Wheat Toast', null, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (11, 14, 'English Muffin', null, 1, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (12, 14, 'Bagel', null, 1, 0, 1)");

        // Fresh Muffins (category 15)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (13, 15, 'Blueberry Muffin', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (14, 15, 'Bran Muffin', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (15, 15, 'Chocolate Chip Muffin', null, 0, 0, 0)");

        // Breakfast items (category 1)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (16, 1, 'Scrambled Eggs', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (17, 1, 'Pancakes', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (18, 1, 'French Toast', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (19, 1, 'Sausage Links', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (20, 1, 'Bacon', null, 1, 0, 0)");

        // Fruits (category 16)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (21, 16, 'Fresh Banana', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (22, 16, 'Apple Slices', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (23, 16, 'Orange Slices', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (24, 16, 'Mixed Fruit Cup', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (25, 16, 'Applesauce', null, 1, 0, 0)");

        // Protein/Entrée items (category 2)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (26, 2, 'Grilled Chicken Breast', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (27, 2, 'Baked Salmon', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (28, 2, 'Turkey & Cheese Sandwich', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (29, 2, 'Ham & Cheese Sandwich', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (30, 2, 'Chef Salad', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (31, 2, 'Grilled Cheese', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (32, 2, 'Chicken Strips', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (33, 2, 'Fish Fillet', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (34, 2, 'Meatloaf', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (35, 2, 'Roast Beef', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (36, 2, 'Baked Chicken', null, 1, 0, 0)");

        // Starch items (category 3)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (37, 3, 'Baked Potato', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (38, 3, 'Mashed Potatoes', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (39, 3, 'French Fries', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (40, 3, 'Rice Pilaf', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (41, 3, 'Wild Rice', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (42, 3, 'Pasta Salad', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (43, 3, 'Macaroni & Cheese', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (44, 3, 'Noodles', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (45, 3, 'Stuffing', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (46, 3, 'Sweet Potato', null, 1, 0, 0)");

        // Vegetable items (category 4)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (47, 4, 'Green Beans', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (48, 4, 'Steamed Broccoli', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (49, 4, 'Carrots', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (50, 4, 'Corn', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (51, 4, 'Peas', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (52, 4, 'Mixed Vegetables', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (53, 4, 'Cauliflower', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (54, 4, 'Spinach', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (55, 4, 'Asparagus', null, 1, 0, 0)");

        // Dessert items (category 6)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (56, 6, 'Chocolate Cake', null, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (57, 6, 'Vanilla Ice Cream', null, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (58, 6, 'Apple Pie', null, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (59, 6, 'Cookies', null, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (60, 6, 'Pudding', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (61, 6, 'Jello', null, 0, 0, 0)");

        // Sugar Free Dessert items (category 7)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (62, 7, 'Sugar Free Pudding', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (63, 7, 'Sugar Free Jello', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (64, 7, 'Sugar Free Ice Cream', null, 1, 0, 0)");

        // Grill Item items (category 5)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (65, 5, 'Hamburger', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (66, 5, 'Cheeseburger', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (67, 5, 'Grilled Chicken Sandwich', null, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (68, 5, 'Hot Dog', null, 1, 0, 0)");

        // Supplement items (category 9)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (69, 9, 'Ensure Plus', 240, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (70, 9, 'Boost', 240, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (71, 9, 'Resource', 200, 1, 0, 0)");

        // Regular Drink items (category 8) - Basic drinks
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (72, 8, 'Water', 240, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (73, 8, 'Milk', 240, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (74, 8, 'Chocolate Milk', 240, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (75, 8, 'Hot Tea', 240, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (76, 8, 'Hot Chocolate', 240, 0, 0, 0)");

        // Soda items (category 10)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (77, 10, 'Coca Cola', 240, 0, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (78, 10, 'Diet Coke', 240, 1, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (79, 10, 'Pepsi', 240, 0, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (80, 10, 'Diet Pepsi', 240, 1, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (81, 10, 'Lemon Lime Soda', 240, 0, 1, 0)");

        // Juice items (category 11) - Basic juices
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (82, 11, 'Orange Juice', 120, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (83, 11, 'Grape Juice', 120, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (84, 11, 'Tomato Juice', 120, 1, 0, 0)");
    }

    // NEW: Clear Liquid items for the Clear Liquid diet
    private void insertClearLiquidItems(SQLiteDatabase db) {
        // Clear Liquid Diet Items for Drinks Category (category_id = 8)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (100, 8, 'Coffee', 200, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (101, 8, 'Decaf Coffee', 200, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (102, 8, 'Ice Tea', 240, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (103, 8, 'Chicken Broth', 200, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (104, 8, 'Beef Broth', 200, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (105, 8, 'Jello', 0, 0, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (106, 8, 'Sugar Free Jello', 0, 1, 0, 0)");

        // Clear Liquid Items for Juices Category (category_id = 11)  
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (107, 11, 'Apple Juice', 120, 1, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (108, 11, 'Cranberry Juice', 120, 1, 0, 0)");

        // Clear Liquid Items for Soda Category (category_id = 10)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (109, 10, 'Sprite', 240, 0, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (110, 10, 'Sprite Zero', 240, 1, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (111, 10, 'Ginger Ale', 240, 0, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (112, 10, 'Diet Ginger Ale', 240, 1, 1, 0)");
    }
}