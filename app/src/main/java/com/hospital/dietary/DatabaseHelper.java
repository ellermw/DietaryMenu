// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/DatabaseHelper.java
// ================================================================================================

package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "hospital_dietary.db";
    private static final int DATABASE_VERSION = 1;

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
        // Drop all tables and recreate
        db.execSQL("DROP TABLE IF EXISTS MealLine");
        db.execSQL("DROP TABLE IF EXISTS MealOrder");
        db.execSQL("DROP TABLE IF EXISTS PatientFluidRestriction");
        db.execSQL("DROP TABLE IF EXISTS RestrictionLimit");
        db.execSQL("DROP TABLE IF EXISTS FluidRestriction");
        db.execSQL("DROP TABLE IF EXISTS PatientAdditionalRestriction");
        db.execSQL("DROP TABLE IF EXISTS AdditionalRestriction");
        db.execSQL("DROP TABLE IF EXISTS PatientFoodMod");
        db.execSQL("DROP TABLE IF EXISTS FoodModification");
        db.execSQL("DROP TABLE IF EXISTS Patient");
        db.execSQL("DROP TABLE IF EXISTS Diet");
        db.execSQL("DROP TABLE IF EXISTS ItemTag");
        db.execSQL("DROP TABLE IF EXISTS Item");
        db.execSQL("DROP TABLE IF EXISTS Category");
        onCreate(db);
    }

    private void createTables(SQLiteDatabase db) {
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

    private void insertInitialData(SQLiteDatabase db) {
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
        db.execSQL("INSERT OR IGNORE INTO FluidRestriction VALUES (6, '2500ml')");

        // Insert Restriction Limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'breakfast', 120)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'lunch', 120)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (1, 'dinner', 160)");
        
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'breakfast', 250)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'lunch', 170)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (2, 'dinner', 180)");
        
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'breakfast', 350)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'lunch', 170)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (3, 'dinner', 180)");
        
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'breakfast', 360)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'lunch', 240)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (4, 'dinner', 240)");
        
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'breakfast', 320)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'lunch', 240)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'dinner', 240)");
        
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'breakfast', 400)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'lunch', 400)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'dinner', 400)");

        // Insert Items (continuing with your existing data structure)
        insertAllItems(db);
    }

    private void insertAllItems(SQLiteDatabase db) {
        // Breakfast items (category 1)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (1, 1, 'Fried Eggs', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (2, 1, 'French Toast', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (3, 1, 'Pancakes', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (4, 1, 'Fruit Plate', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (5, 1, 'Chicken Broth', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (6, 1, 'Beef Broth', null, 1, 0)");
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

        // Starch items (category 3)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (25, 3, 'Baked Potato', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (26, 3, 'Fruit Cup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (27, 3, 'Fruit Plate', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (28, 3, 'Baked Lays Chips', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (29, 3, 'Chicken Noodle Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (30, 3, 'Cream of Mushroom Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (31, 3, 'Veggie Beef Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (32, 3, 'Tomato Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (33, 3, 'Potato Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (34, 3, 'LS Chicken Noodle Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (35, 3, 'LS Cream of Mushroom Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (36, 3, 'LS Veggie Beef Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (37, 3, 'LS Tomato Soup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (38, 3, 'Side Salad', null, 1, 0)");

        // Vegetable items (category 4)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (39, 4, 'Italian Blended Vegetables', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (40, 4, 'Green Beans', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (41, 4, 'Steamed Zucchini', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (42, 4, 'Roasted Potatoes', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (43, 4, 'Roasted Carrots', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (44, 4, 'Steamed Broccoli', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (45, 4, 'Prince Edward Vegetables', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (46, 4, 'Oriental Vegetables', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (47, 4, 'Spinach', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (48, 4, 'Capri Vegetable Blend', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (49, 4, 'Sugar Snap Peas', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (50, 4, 'Cape Cod Vegetables', null, 1, 0)");

        // Grill Item items (category 5)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (51, 5, 'Hamburger', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (52, 5, 'Cheeseburger', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (53, 5, 'Chicken Strips', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (54, 5, 'Popcorn Chicken', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (55, 5, 'Grilled Cheese', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (56, 5, 'Hot Ham & Cheese', null, 1, 0)");

        // Dessert items (category 6)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (57, 6, 'Jello', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (58, 6, 'Vanilla Pudding', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (59, 6, 'Chocolate Pudding', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (60, 6, 'Baked Lays', null, 1, 0)");

        // Sugar Free Dessert items (category 7)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (61, 7, 'Sugar Free Jello', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (62, 7, 'Sugar Free Vanilla Pudding', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (63, 7, 'Sugar Free Chocolate Pudding', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (64, 7, 'Baked Lays', null, 1, 0)");

        // Drink items (category 8)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (65, 8, 'Soda', 355, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (66, 8, 'Gatorade', 591, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (67, 8, 'Powerade', 591, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (68, 8, 'Bubblr', 355, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (69, 8, 'Gold Peak Sweet Tea', 500, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (70, 8, 'Gold Peak Sugar Free Sweet Tea', 500, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (71, 8, 'Bottled Water', 355, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (120, 8, 'Whole Milk', 240, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (121, 8, '2% Milk', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (122, 8, 'Coffee', 200, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (123, 8, 'Decaf Coffee', 200, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (124, 8, 'Hot Tea', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (125, 8, 'Ice Tea', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (126, 8, 'Hot Chocolate', 240, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (127, 8, 'Sugar Free Hot Chocolate', 240, 1, 0)");

        // Supplement items (category 9)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (84, 9, 'Ensure - Vanilla', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (85, 9, 'Ensure - Chocolate', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (86, 9, 'Ensure - Strawberry', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (87, 9, 'Ensure (Plus) - Vanilla', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (88, 9, 'Ensure (Plus) - Chocolate', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (89, 9, 'Ensure (Plus) - Strawberry', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (90, 9, 'Ensure High Protein - Strawberry', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (91, 9, 'Boost - Vanilla', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (92, 9, 'Boost - Chocolate', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (93, 9, 'Boost (High Calorie) - Strawberry', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (94, 9, 'Boost Breeze - Wild Berry', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (95, 9, 'Glucerna - Vanilla', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (96, 9, 'Glucerna - Chocolate', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (97, 9, 'Glucerna - Strawberry', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (98, 9, 'Nepro', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (99, 9, 'Two Cal', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (100, 9, 'Jevity 1.5', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (101, 9, 'Jevity 1.2', 237, 1, 0)");

        // Soda items (category 10) - Regular sodas are NOT ADA friendly, diet sodas ARE
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (72, 10, 'Sprite', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (73, 10, 'Coke', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (74, 10, 'Cherry Coke', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (75, 10, 'Root Beer', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (76, 10, 'Mountain Dew', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (77, 10, 'Pepsi', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (78, 10, 'Dr. Pepper', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (79, 10, 'Sprite Zero', 355, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (80, 10, 'Diet Coke', 355, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (81, 10, 'Coke Zero', 355, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (82, 10, 'Diet Mountain Dew', 355, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (83, 10, 'Diet Pepsi', 355, 1, 1)");

        // Juices (category 11) - All 120ml, All ADA friendly except Orange Juice
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (102, 11, 'Orange Juice', 120, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (103, 11, 'Cranberry Juice', 120, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (104, 11, 'Apple Juice', 120, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (105, 11, 'Pineapple Juice', 120, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (106, 11, 'Prune Juice', 120, 1, 0)");

        // Cold Cereals (category 12) - All ADA friendly
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (107, 12, 'Raisin Bran', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (108, 12, 'Cheerios', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (109, 12, 'Honey Nut Cheerios', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (110, 12, 'Corn Flakes', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (111, 12, 'Rice Krispies', null, 1, 0)");

        // Hot Cereals (category 13) - All ADA friendly
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (112, 13, 'Oatmeal', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (113, 13, 'Cream of Wheat', null, 1, 0)");

        // Breads (category 14) - All ADA friendly
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (114, 14, 'Biscuit', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (115, 14, 'Toast', null, 1, 0)");

        // Fresh Muffins (category 15) - All ADA friendly
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (116, 15, 'Banana Nut Muffin', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (117, 15, 'Blueberry Muffin', null, 1, 0)");

        // Fruits (category 16) - All ADA friendly
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (118, 16, 'Stewed Prunes', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (119, 16, 'Mixed Fruit', null, 1, 0)");
    }
}
