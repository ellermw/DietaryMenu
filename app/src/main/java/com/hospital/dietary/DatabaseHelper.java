package com.hospital.dietary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.hospital.dietary.data.database.AppDatabase;

/**
 * DatabaseHelper class for backward compatibility with existing activities
 * This class acts as a wrapper around the Room database
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HospitalDietaryDB";
    private static final int DATABASE_VERSION = 2;

    private final Context context;
    private AppDatabase roomDatabase;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.roomDatabase = AppDatabase.getDatabase(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Room handles table creation
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Room handles migrations
    }

    /**
     * Get the Room database instance for use in new code
     */
    public AppDatabase getRoomDatabase() {
        return roomDatabase;
    }

    /**
     * Get the context
     */
    public Context getContext() {
        return context;
    }
}