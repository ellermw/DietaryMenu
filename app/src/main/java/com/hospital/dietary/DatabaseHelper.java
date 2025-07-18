package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "dietary_menu.db";
    private static final int DATABASE_VERSION = 4; // Incremented for new features
    
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

    // Item table for food items
    private static final String CREATE_ITEM_TABLE = 
        "CREATE TABLE IF NOT EXISTS Item (" +
        "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "name TEXT NOT NULL, " +
        "category TEXT NOT NULL, " +
        "meal_type TEXT NOT NULL, " +
        "is_ada_compliant INTEGER DEFAULT 0, " +
        "fluid_amount INTEGER DEFAULT 0, " +
        "is_default INTEGER DEFAULT 0, " +
        "description TEXT" +
        ")";

    // MealOrder table for tracking orders
    private static final String CREATE_MEAL_ORDER_TABLE = 
        "CREATE TABLE IF NOT EXISTS MealOrder (" +
        "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "patient_id INTEGER NOT NULL, " +
        "meal TEXT NOT NULL, " +
        "timestamp TEXT NOT NULL, " +
        "FOREIGN KEY(patient_id) REFERENCES PatientInfo(patient_id)" +
        ")";

    // MealLine table for order details
    private static final String CREATE_MEAL_LINE_TABLE = 
        "CREATE TABLE IF NOT EXISTS MealLine (" +
        "line_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "order_id INTEGER NOT NULL, " +
        "item_id INTEGER NOT NULL, " +
        "quantity INTEGER DEFAULT 1, " +
        "FOREIGN KEY(order_id) REFERENCES MealOrder(order_id), " +
        "FOREIGN KEY(item_id) REFERENCES Item(item_id)" +
        ")";

    // User table for authentication - FIXED PASSWORD COLUMN NAME
	private static final String CREATE_USER_TABLE = 
		"CREATE TABLE IF NOT EXISTS User (" +
		"user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
		"username TEXT NOT NULL UNIQUE, " +
		"password TEXT NOT NULL, " +
		"full_name TEXT NOT NULL, " +
		"role TEXT NOT NULL, " +
		"email TEXT, " +  // Add this line
		"is_active INTEGER DEFAULT 1, " +
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
        db.execSQL(CREATE_ITEM_TABLE);
        db.execSQL(CREATE_MEAL_ORDER_TABLE);
        db.execSQL(CREATE_MEAL_LINE_TABLE);
        db.execSQL(CREATE_USER_TABLE);
        
        // Insert default data
        insertDefaultData(db);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 4) {
			// ... existing upgrade code ...
        
			// Add email column to User table if it doesn't exist
			try {
				db.execSQL("ALTER TABLE User ADD COLUMN email TEXT");
			} catch (Exception e) {
				// Column already exists or other error
				android.util.Log.d("DatabaseHelper", "Email column may already exist: " + e.getMessage());
			}
		}
    
		// Increment DATABASE_VERSION to 5 at the top of the class
		// private static final int DATABASE_VERSION = 5;
    
		insertDefaultData(db);
	}

    private void insertDefaultData(SQLiteDatabase db) {
        // Insert default diets
        insertDefaultDiets(db);
        
        // Insert default food items
        insertDefaultItems(db);
        
        // Insert default user
        insertDefaultUser(db);
    }

    private void insertDefaultDiets(SQLiteDatabase db) {
        String[] diets = {
            "Regular", "ADA Diabetic", "Cardiac", "Renal", "Soft", 
            "Liquid", "NPO", "Pureed", "Mechanical Soft"
        };
        
        for (String diet : diets) {
            db.execSQL("INSERT OR IGNORE INTO Diet (name, description) VALUES (?, ?)", 
                      new String[]{diet, "Standard " + diet + " diet"});
        }
    }

    private void insertDefaultItems(SQLiteDatabase db) {
        // Breakfast items
        insertItem(db, "Scrambled Eggs", "Protein", "Breakfast", 1, 0, 1);
        insertItem(db, "Toast", "Carbs", "Breakfast", 1, 0, 1);
        insertItem(db, "Oatmeal", "Carbs", "Breakfast", 1, 0, 1);
        insertItem(db, "Fresh Fruit", "Fruit", "Breakfast", 1, 0, 1);
        insertItem(db, "Coffee", "Beverages", "Breakfast", 1, 240, 1);
        insertItem(db, "Orange Juice", "Beverages", "Breakfast", 0, 240, 0);
        
        // Lunch items
        insertItem(db, "Grilled Chicken", "Protein", "Lunch", 1, 0, 1);
        insertItem(db, "Rice", "Carbs", "Lunch", 1, 0, 1);
        insertItem(db, "Steamed Vegetables", "Vegetables", "Lunch", 1, 0, 1);
        insertItem(db, "Garden Salad", "Vegetables", "Lunch", 1, 0, 0);
        insertItem(db, "Water", "Beverages", "Lunch", 1, 240, 1);
        insertItem(db, "Apple Juice", "Beverages", "Lunch", 0, 240, 0);
        
        // Dinner items
        insertItem(db, "Baked Fish", "Protein", "Dinner", 1, 0, 1);
        insertItem(db, "Mashed Potatoes", "Carbs", "Dinner", 1, 0, 1);
        insertItem(db, "Green Beans", "Vegetables", "Dinner", 1, 0, 1);
        insertItem(db, "Dinner Roll", "Carbs", "Dinner", 0, 0, 0);
        insertItem(db, "Milk", "Beverages", "Dinner", 1, 240, 1);
        insertItem(db, "Cranberry Juice", "Beverages", "Dinner", 0, 240, 0);
        
        // Additional items for variety
        insertItem(db, "Soup", "Soup", "Lunch", 1, 200, 0);
        insertItem(db, "Yogurt", "Dairy", "Breakfast", 1, 0, 0);
        insertItem(db, "Sandwich", "Mixed", "Lunch", 1, 0, 0);
        insertItem(db, "Pasta", "Carbs", "Dinner", 1, 0, 0);
    }

    private void insertItem(SQLiteDatabase db, String name, String category, String mealType, 
                           int isAdaCompliant, int fluidAmount, int isDefault) {
        db.execSQL("INSERT OR IGNORE INTO Item (name, category, meal_type, is_ada_compliant, fluid_amount, is_default) " +
                  "VALUES (?, ?, ?, ?, ?, ?)", 
                  new Object[]{name, category, mealType, isAdaCompliant, fluidAmount, isDefault});
    }

    private void insertDefaultUser(SQLiteDatabase db) {
        // Insert default admin user (username: admin, password: admin123)
        // FIXED: Now using 'password' column instead of 'password_hash'
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