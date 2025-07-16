// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/DatabaseHelper.java
// ================================================================================================

package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "hospital_dietary.db";
    private static final int DATABASE_VERSION = 2; // Incremented for User table

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
        // For major version changes, you might want to drop and recreate all tables
        // but for now we'll just add the User table
    }

    private void createTables(SQLiteDatabase db) {
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

        // Patient table
        db.execSQL("CREATE TABLE IF NOT EXISTS Patient (" +
                "patient_id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "room_number TEXT NOT NULL," +
                "wing TEXT NOT NULL," +
                "diet_id INTEGER," +
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
    }

    private void createUserTable(SQLiteDatabase db) {
        // User table for authentication and role management
        db.execSQL("CREATE TABLE IF NOT EXISTS User (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password_hash TEXT NOT NULL," +
                "role TEXT NOT NULL CHECK(role IN ('User', 'Admin'))," +
                "created_date TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "last_login TEXT," +
                "is_active BOOLEAN NOT NULL DEFAULT 1)");
    }

    private void insertInitialData(SQLiteDatabase db) {
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
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (3, 'Cardiac')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (4, 'Renal')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (5, 'Puree')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (6, 'Full Liquid')");
        db.execSQL("INSERT OR IGNORE INTO Diet VALUES (7, 'Clear Liquid')");

        // Insert Food Modifications
        db.execSQL("INSERT OR IGNORE INTO FoodModification VALUES (1, 'Mechanical Ground')");
        db.execSQL("INSERT OR IGNORE INTO FoodModification VALUES (2, 'Mechanical Chopped')");
        db.execSQL("INSERT OR IGNORE INTO FoodModification VALUES (3, 'Bite Size')");

        // Insert Additional Restrictions
        db.execSQL("INSERT OR IGNORE INTO AdditionalRestriction VALUES (1, 'Nectar Thick')");
        db.execSQL("INSERT OR IGNORE INTO AdditionalRestriction VALUES (2, 'Honey Thick')");
        db.execSQL("INSERT OR IGNORE INTO AdditionalRestriction VALUES (3, 'Pudding Thick')");
        db.execSQL("INSERT OR IGNORE INTO AdditionalRestriction VALUES (4, 'No Straws')");
        db.execSQL("INSERT OR IGNORE INTO AdditionalRestriction VALUES (5, 'Paper Service')");
        db.execSQL("INSERT OR IGNORE INTO AdditionalRestriction VALUES (6, 'No Dairy / Lactose')");

        // Insert Fluid Restrictions
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (1, '1000ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (2, '1200ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (3, '1500ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (4, '1800ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (5, '2000ml')");
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (6, 'None')");

        // Insert Restriction Limits for each fluid restriction
        insertRestrictionLimits(db);

        // Insert all food items
        insertFoodItems(db);
    }

    private void insertDefaultUsers(SQLiteDatabase db) {
        // Insert default admin user (password: admin123)
        // Note: In production, you should use proper password hashing
        String adminPasswordHash = hashPassword("admin123");
        db.execSQL("INSERT OR IGNORE INTO User (username, password_hash, role) VALUES ('admin', ?, 'Admin')", 
                new String[]{adminPasswordHash});
        
        // Insert default regular user (password: user123)
        String userPasswordHash = hashPassword("user123");
        db.execSQL("INSERT OR IGNORE INTO User (username, password_hash, role) VALUES ('user', ?, 'User')", 
                new String[]{userPasswordHash});
    }

    // Simple password hashing - in production use BCrypt or similar
    private String hashPassword(String password) {
        // This is a very basic hash - use proper hashing in production
        return String.valueOf(password.hashCode());
    }

    private void insertRestrictionLimits(SQLiteDatabase db) {
        // 1000ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'Breakfast', 300)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'Lunch', 350)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'Dinner', 350)");

        // 1200ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'Breakfast', 360)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'Lunch', 420)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'Dinner', 420)");

        // 1500ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'Breakfast', 450)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'Lunch', 525)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'Dinner', 525)");

        // 1800ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'Breakfast', 540)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'Lunch', 630)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'Dinner', 630)");

        // 2000ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'Breakfast', 600)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'Lunch', 700)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'Dinner', 700)");

        // None - no limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'Breakfast', 999999)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'Lunch', 999999)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'Dinner', 999999)");
    }

    private void insertFoodItems(SQLiteDatabase db) {
        // Breakfast items (category 1)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (1, 1, 'Scrambled Eggs', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (2, 1, 'Pancakes', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (3, 1, 'French Toast', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (4, 1, 'Bacon', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (5, 1, 'Sausage', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (6, 1, 'Toast', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (7, 1, 'Hashbrowns', null, 1, 0)");

        // Protein/Entrée items (category 2)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (8, 2, 'Tuna Salad Sandwich', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (9, 2, 'Chicken Salad Sandwich', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (10, 2, 'Egg Salad Bowl', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (11, 2, 'Tuna Salad Bowl', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (12, 2, 'Chicken Noodle Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (13, 2, 'Cream of Mushroom Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (14, 2, 'Veggie Beef Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (15, 2, 'Tomato Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (16, 2, 'Potato Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (17, 2, 'LS Chicken Noodle Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (18, 2, 'LS Cream of Mushroom Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (19, 2, 'LS Veggie Beef Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (20, 2, 'LS Tomato Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (21, 2, 'Turkey Sandwich', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (22, 2, 'Turkey & Cheese Sandwich', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (23, 2, 'Ham & Cheese Sandwich', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (24, 2, 'Chef Salad', null, 1, 0)");

        // Continue with more items... (truncated for brevity)
        // You can add all the remaining items from your existing database
    }
}