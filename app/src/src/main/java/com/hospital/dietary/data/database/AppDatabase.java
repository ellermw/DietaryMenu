package com.hospital.dietary.data.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.hospital.dietary.data.entities.*;
import com.hospital.dietary.data.dao.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room Database class that replaces the old DatabaseHelper
 * Includes migration from existing SQLite database
 */
@Database(entities = {
        UserEntity.class,
        PatientEntity.class,
        ItemEntity.class,
        MealOrderEntity.class,
        OrderItemEntity.class,
        DefaultMenuEntity.class,
        FinalizedOrderEntity.class
}, version = 2, exportSchema = true)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    // DAO methods
    public abstract UserDao userDao();
    public abstract PatientDao patientDao();
    public abstract ItemDao itemDao();
    public abstract MealOrderDao mealOrderDao();
    public abstract OrderItemDao orderItemDao();
    public abstract DefaultMenuDao defaultMenuDao();
    public abstract FinalizedOrderDao finalizedOrderDao();
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    
    // Executor service for database operations
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    
    // Get database instance
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "HospitalDietaryDB")
                            .addMigrations(MIGRATION_1_2)
                            .addCallback(roomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    
    // Migration from version 1 (existing SQLite) to version 2 (Room)
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Since we're keeping the same schema, we mainly need to ensure indexes exist
            
            // Add indexes for performance
            database.execSQL("CREATE INDEX IF NOT EXISTS index_patient_info_wing_room_number ON patient_info(wing, room_number)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_patient_info_diet_type ON patient_info(diet_type)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_items_category ON items(category)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_items_is_ada_friendly ON items(is_ada_friendly)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_meal_orders_patient_id ON meal_orders(patient_id)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_meal_orders_order_date ON meal_orders(order_date)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_finalized_order_order_date ON finalized_order(order_date)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_default_menu_diet_type_meal_type_day_of_week ON default_menu(diet_type, meal_type, day_of_week)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_username ON users(username)");
            
            // Add any missing columns with defaults (for backward compatibility)
            try {
                database.execSQL("ALTER TABLE meal_orders ADD COLUMN order_date DATE");
            } catch (Exception e) {
                // Column might already exist
            }
            
            try {
                database.execSQL("ALTER TABLE meal_orders ADD COLUMN is_complete INTEGER DEFAULT 0");
            } catch (Exception e) {
                // Column might already exist
            }
            
            try {
                database.execSQL("ALTER TABLE meal_orders ADD COLUMN created_by TEXT");
            } catch (Exception e) {
                // Column might already exist
            }
        }
    };
    
    // Database callback for initial population
    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            
            // Populate database in the background
            databaseWriteExecutor.execute(() -> {
                // Get a reference to the database
                AppDatabase database = INSTANCE;
                
                // Clear all tables
                database.clearAllTables();
                
                // Add default admin user
                UserEntity admin = new UserEntity();
                admin.setUsername("admin");
                admin.setPassword("admin123");
                admin.setFullName("System Administrator");
                admin.setRole("Admin");
                admin.setActive(true);
                database.userDao().insertUser(admin);
                
                // Add default food items
                insertDefaultFoodItems(database);
            });
        }
        
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Enable foreign keys
            db.execSQL("PRAGMA foreign_keys=ON");
        }
    };
    
    // Insert default food items
    private static void insertDefaultFoodItems(AppDatabase database) {
        ItemDao itemDao = database.itemDao();
        
        // Breakfast items
        String[][] breakfastItems = {
            {"Scrambled Eggs", "Breakfast Main", "Fresh scrambled eggs", "1"},
            {"Pancakes", "Breakfast Main", "Fluffy pancakes with syrup", "0"},
            {"Oatmeal", "Hot Cereal", "Heart-healthy oatmeal", "1"},
            {"Cornflakes", "Cold Cereal", "Classic corn flakes", "1"},
            {"White Toast", "Bread", "White bread toasted", "1"},
            {"Wheat Toast", "Bread", "Whole wheat bread toasted", "1"},
            {"Orange Juice", "Juice", "100% orange juice", "1"},
            {"Apple Juice", "Juice", "100% apple juice", "1"},
            {"Coffee", "Hot Beverage", "Regular coffee", "1"},
            {"Tea", "Hot Beverage", "Hot tea", "1"}
        };
        
        // Lunch items
        String[][] lunchItems = {
            {"Grilled Chicken", "Lunch Protein", "Seasoned grilled chicken breast", "1"},
            {"Turkey Sandwich", "Lunch Protein", "Turkey on wheat bread", "0"},
            {"Rice", "Starch", "Steamed white rice", "1"},
            {"Mashed Potatoes", "Starch", "Creamy mashed potatoes", "1"},
            {"Green Beans", "Vegetable", "Steamed green beans", "1"},
            {"Garden Salad", "Vegetable", "Fresh mixed greens", "1"},
            {"Pudding", "Dessert", "Chocolate or vanilla pudding", "1"},
            {"Jello", "Dessert", "Sugar-free jello", "1"}
        };
        
        // Dinner items
        String[][] dinnerItems = {
            {"Baked Fish", "Dinner Protein", "Seasoned baked white fish", "1"},
            {"Beef Stew", "Dinner Protein", "Hearty beef stew", "0"},
            {"Pasta", "Starch", "Pasta with marinara sauce", "0"},
            {"Baked Potato", "Starch", "Baked potato with toppings", "1"},
            {"Broccoli", "Vegetable", "Steamed broccoli", "1"},
            {"Carrots", "Vegetable", "Glazed carrots", "1"},
            {"Ice Cream", "Dessert", "Vanilla ice cream", "0"},
            {"Fruit Cup", "Dessert", "Fresh fruit cup", "1"}
        };
        
        // Insert all items
        insertItemArray(itemDao, breakfastItems);
        insertItemArray(itemDao, lunchItems);
        insertItemArray(itemDao, dinnerItems);
    }
    
    private static void insertItemArray(ItemDao itemDao, String[][] items) {
        for (String[] item : items) {
            ItemEntity entity = new ItemEntity();
            entity.setName(item[0]);
            entity.setCategory(item[1]);
            entity.setDescription(item[2]);
            entity.setAdaFriendly("1".equals(item[3]));
            itemDao.insertItem(entity);
        }
    }
}