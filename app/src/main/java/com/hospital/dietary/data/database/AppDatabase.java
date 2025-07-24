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
}, version = 11, exportSchema = true)
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
                            .fallbackToDestructiveMigration() // This will recreate the database if migration fails
                            .addMigrations(MIGRATION_10_11)
                            .addCallback(roomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Migration from version 10 to 11
    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // The tables should already exist from version 10
            // We just need to ensure they match Room's expected schema

            // Add any missing columns or make schema adjustments if needed
            // For now, we'll assume the schema is compatible
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

        // Since ItemEntity doesn't have meal field, we'll use categories that include meal type
        String[][] allItems = {
                // Breakfast items
                {"Scrambled Eggs", "Breakfast Main", "Fresh scrambled eggs", "1"},
                {"Pancakes", "Breakfast Main", "Fluffy pancakes with syrup", "0"},
                {"Oatmeal", "Breakfast Cereal", "Heart-healthy oatmeal", "1"},
                {"Cornflakes", "Breakfast Cereal", "Classic corn flakes", "1"},
                {"White Toast", "Breakfast Bread", "White bread toasted", "1"},
                {"Wheat Toast", "Breakfast Bread", "Whole wheat bread toasted", "1"},
                {"Orange Juice", "Breakfast Juice", "100% orange juice", "1"},
                {"Apple Juice", "Breakfast Juice", "100% apple juice", "1"},
                {"Coffee", "Breakfast Beverage", "Regular coffee", "1"},
                {"Decaf Coffee", "Breakfast Beverage", "Decaffeinated coffee", "1"},
                {"Milk", "Breakfast Beverage", "2% milk", "1"},
                {"Yogurt", "Breakfast Dairy", "Low-fat yogurt", "1"},

                // Lunch items
                {"Grilled Chicken", "Lunch Main", "Herb grilled chicken breast", "1"},
                {"Baked Fish", "Lunch Main", "Lemon pepper baked fish", "1"},
                {"Beef Stew", "Lunch Main", "Hearty beef stew", "0"},
                {"Vegetable Soup", "Lunch Soup", "Fresh vegetable soup", "1"},
                {"Chicken Noodle Soup", "Lunch Soup", "Classic chicken noodle", "0"},
                {"Garden Salad", "Lunch Salad", "Fresh mixed greens", "1"},
                {"Caesar Salad", "Lunch Salad", "Romaine with caesar dressing", "0"},
                {"Rice Pilaf", "Lunch Side", "Seasoned rice", "1"},
                {"Mashed Potatoes", "Lunch Side", "Creamy mashed potatoes", "1"},
                {"Green Beans", "Lunch Vegetable", "Steamed green beans", "1"},
                {"Carrots", "Lunch Vegetable", "Glazed carrots", "1"},
                {"Iced Tea", "Lunch Beverage", "Sweetened iced tea", "1"},
                {"Lemonade", "Lunch Beverage", "Fresh lemonade", "1"},

                // Dinner items
                {"Roast Beef", "Dinner Main", "Tender roast beef", "0"},
                {"Baked Chicken", "Dinner Main", "Herb baked chicken", "1"},
                {"Pork Chops", "Dinner Main", "Grilled pork chops", "0"},
                {"Pasta Primavera", "Dinner Main", "Vegetable pasta", "1"},
                {"Meatloaf", "Dinner Main", "Traditional meatloaf", "0"},
                {"Baked Potato", "Dinner Side", "Butter and sour cream", "1"},
                {"Rice", "Dinner Side", "Steamed white rice", "1"},
                {"Mixed Vegetables", "Dinner Vegetable", "Seasonal mix", "1"},
                {"Broccoli", "Dinner Vegetable", "Steamed broccoli", "1"},
                {"Dinner Roll", "Dinner Bread", "Fresh baked roll", "1"},
                {"Fruit Cocktail", "Dinner Dessert", "Mixed fruit cup", "1"},
                {"Pudding", "Dinner Dessert", "Vanilla pudding", "1"},
                {"Jello", "Dinner Dessert", "Sugar-free jello", "1"}
        };

        // Insert all items
        for (String[] item : allItems) {
            ItemEntity entity = new ItemEntity();
            entity.setName(item[0]);
            entity.setCategory(item[1]);
            entity.setDescription(item[2]);
            entity.setAdaFriendly(item[3].equals("1"));
            itemDao.insertItem(entity);
        }
    }
}