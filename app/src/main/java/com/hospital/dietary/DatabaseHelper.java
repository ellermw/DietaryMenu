// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/DatabaseHelper.java
// ================================================================================================

package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "hospital_dietary.db";
    private static final int DATABASE_VERSION = 4; // Incremented for User table schema fix

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
			// Add FinalizedOrder table for version 4
			db.execSQL("CREATE TABLE IF NOT EXISTS FinalizedOrder (" +
					"order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"patient_name TEXT NOT NULL," +
					"wing TEXT NOT NULL," +
					"room TEXT NOT NULL," +
					"order_date TEXT NOT NULL," +
					"diet_type TEXT NOT NULL," +
					"fluid_restriction TEXT," +
					"mechanical_ground INTEGER NOT NULL DEFAULT 0," +
					"mechanical_chopped INTEGER NOT NULL DEFAULT 0," +
					"bite_size INTEGER NOT NULL DEFAULT 0," +
					"bread_ok INTEGER NOT NULL DEFAULT 0," +
					"breakfast_items TEXT," +
					"lunch_items TEXT," +
					"dinner_items TEXT," +
					"breakfast_juices TEXT," +
					"lunch_juices TEXT," +
					"dinner_juices TEXT," +
					"breakfast_drinks TEXT," +
					"lunch_drinks TEXT," +
					"dinner_drinks TEXT," +
					"created_timestamp TEXT DEFAULT CURRENT_TIMESTAMP," +
					"UNIQUE(wing, room, order_date))");
		}
	}

    private void createTables(SQLiteDatabase db) {
        // User table (NEW - for authentication and role management)
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
		
		db.execSQL("CREATE TABLE IF NOT EXISTS FinalizedOrder (" +
				"order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"patient_name TEXT NOT NULL," +
				"wing TEXT NOT NULL," +
				"room TEXT NOT NULL," +
				"order_date TEXT NOT NULL," +
				"diet_type TEXT NOT NULL," +
				"fluid_restriction TEXT," +
				"mechanical_ground INTEGER NOT NULL DEFAULT 0," +
				"mechanical_chopped INTEGER NOT NULL DEFAULT 0," +
				"bite_size INTEGER NOT NULL DEFAULT 0," +
				"bread_ok INTEGER NOT NULL DEFAULT 0," +
				"breakfast_items TEXT," +
				"lunch_items TEXT," +
				"dinner_items TEXT," +
				"breakfast_juices TEXT," +
				"lunch_juices TEXT," +
				"dinner_juices TEXT," +
				"breakfast_drinks TEXT," +
				"lunch_drinks TEXT," +
				"dinner_drinks TEXT," +
				"created_timestamp TEXT DEFAULT CURRENT_TIMESTAMP," +
				"UNIQUE(wing, room, order_date))");
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
        insertRestrictionLimits(db);

        // Insert all food items
        insertAllItems(db);
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
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (5, 'dinner', 240)");

        // 2500ml limits
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'breakfast', 400)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'lunch', 400)");
        db.execSQL("INSERT OR IGNORE INTO RestrictionLimit VALUES (6, 'dinner', 400)");
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
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (25, 2, 'Grilled Cheese', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (26, 2, 'Chicken Strips', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (27, 2, 'Fish Fillet', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (28, 2, 'Meatloaf', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (29, 2, 'Roast Beef', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (30, 2, 'Baked Chicken', null, 1, 0)");

        // Starch items (category 3)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (31, 3, 'Baked Potato', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (32, 3, 'Mashed Potatoes', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (33, 3, 'French Fries', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (34, 3, 'Rice Pilaf', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (35, 3, 'Wild Rice', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (36, 3, 'Pasta Salad', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (37, 3, 'Macaroni & Cheese', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (38, 3, 'Noodles', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (39, 3, 'Stuffing', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (40, 3, 'Sweet Potato', null, 1, 0)");

        // Vegetable items (category 4)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (41, 4, 'Green Beans', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (42, 4, 'Steamed Broccoli', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (43, 4, 'Carrots', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (44, 4, 'Corn', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (45, 4, 'Peas', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (46, 4, 'Mixed Vegetables', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (47, 4, 'Cauliflower', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (48, 4, 'Spinach', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (49, 4, 'Asparagus', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (50, 4, 'Side Salad', null, 1, 0)");

        // Grill Item items (category 5)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (51, 5, 'Hamburger', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (52, 5, 'Cheeseburger', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (53, 5, 'Hot Dog', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (54, 5, 'Grilled Chicken Breast', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (55, 5, 'Fish Sandwich', null, 1, 0)");

        // Dessert items (category 6)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (56, 6, 'Jello', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (57, 6, 'Vanilla Pudding', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (58, 6, 'Chocolate Pudding', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (59, 6, 'Ice Cream', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (60, 6, 'Cake', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (61, 6, 'Pie', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (62, 6, 'Cookies', null, 1, 0)");

        // Sugar Free Dessert items (category 7)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (63, 7, 'Sugar Free Jello', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (64, 7, 'Sugar Free Vanilla Pudding', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (65, 7, 'Sugar Free Chocolate Pudding', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (66, 7, 'Sugar Free Ice Cream', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (67, 7, 'Sugar Free Cookies', null, 1, 0)");

        // Drink items (category 8)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (68, 8, 'Coffee', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (69, 8, 'Decaf Coffee', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (70, 8, 'Tea', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (71, 8, 'Iced Tea', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (72, 8, 'Hot Chocolate', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (73, 8, 'Sugar Free Hot Chocolate', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (74, 8, 'Milk', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (75, 8, '2% Milk', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (76, 8, 'Skim Milk', 240, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (77, 8, 'Bottled Water', 355, 1, 0)");

        // Supplement items (category 9)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (78, 9, 'Ensure', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (79, 9, 'Boost', 237, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (80, 9, 'Protein Shake', 240, 1, 0)");

        // Soda items (category 10)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (81, 10, 'Coke', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (82, 10, 'Pepsi', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (83, 10, 'Sprite', 355, 0, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (84, 10, 'Diet Coke', 355, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (85, 10, 'Diet Pepsi', 355, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (86, 10, 'Diet Sprite', 355, 1, 1)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (87, 10, 'Ginger Ale', 355, 0, 1)");

        // Juices items (category 11)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (88, 11, 'Orange Juice', 120, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (89, 11, 'Apple Juice', 120, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (90, 11, 'Cranberry Juice', 120, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (91, 11, 'Grape Juice', 120, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (92, 11, 'Tomato Juice', 120, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (93, 11, 'Grapefruit Juice', 120, 1, 0)");

        // Cold Cereals items (category 12)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (94, 12, 'Cheerios', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (95, 12, 'Corn Flakes', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (96, 12, 'Rice Krispies', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (97, 12, 'Frosted Flakes', null, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (98, 12, 'Raisin Bran', null, 1, 0)");

        // Hot Cereals items (category 13)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (99, 13, 'Oatmeal', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (100, 13, 'Cream of Wheat', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (101, 13, 'Grits', null, 1, 0)");

        // Breads items (category 14)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (102, 14, 'White Bread', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (103, 14, 'Wheat Bread', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (104, 14, 'Rye Bread', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (105, 14, 'Toast', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (106, 14, 'Dinner Roll', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (107, 14, 'Biscuit', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (108, 14, 'Croissant', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (109, 14, 'Bagel', null, 1, 0)");

        // Fresh Muffins items (category 15)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (110, 15, 'Blueberry Muffin', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (111, 15, 'Chocolate Chip Muffin', null, 0, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (112, 15, 'Bran Muffin', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (113, 15, 'Banana Nut Muffin', null, 1, 0)");

        // Fruits items (category 16)
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (114, 16, 'Mixed Fruit', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (115, 16, 'Fruit Cup', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (116, 16, 'Apple Slices', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (117, 16, 'Orange Slices', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (118, 16, 'Banana', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (119, 16, 'Grapes', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (120, 16, 'Strawberries', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (121, 16, 'Pineapple', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (122, 16, 'Melon', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (123, 16, 'Peaches', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (124, 16, 'Pears', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (125, 16, 'Applesauce', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (126, 16, 'Fruit Cocktail', null, 1, 0)");
        db.execSQL("INSERT OR IGNORE INTO Item VALUES (127, 16, 'Fresh Fruit Plate', null, 1, 0)");
    }
}