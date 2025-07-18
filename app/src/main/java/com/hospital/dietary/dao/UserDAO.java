package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDAO {

    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Add a new user
     */
    public long addUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("full_name", user.getFullName());
        values.put("role", user.getRole());
        values.put("email", user.getEmail());
        values.put("is_active", user.isActive() ? 1 : 0);
        values.put("created_date", getCurrentTimestamp());

        try {
            return db.insert("User", null, values);
        } catch (Exception e) {
            Log.e("UserDAO", "Error adding user: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Update an existing user
     */
    public long updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("full_name", user.getFullName());
        values.put("role", user.getRole());
        values.put("email", user.getEmail());
        values.put("is_active", user.isActive() ? 1 : 0);
        
        // Only update password if it's provided (not null or empty)
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            values.put("password", user.getPassword());
        }

        try {
            return db.update("User", values, "user_id = ?", new String[]{String.valueOf(user.getUserId())});
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating user: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Delete a user
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rowsAffected = db.delete("User", "user_id = ?", new String[]{String.valueOf(userId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE username = ? AND is_active = 1";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{username});
            
            if (cursor.moveToFirst()) {
                return createUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting user by username: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return null;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE user_id = ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            
            if (cursor.moveToFirst()) {
                return createUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting user by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return null;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User ORDER BY full_name";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                do {
                    users.add(createUserFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting all users: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return users;
    }

    /**
     * Get all active users
     */
    public List<User> getActiveUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE is_active = 1 ORDER BY full_name";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                do {
                    users.add(createUserFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting active users: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return users;
    }

    /**
     * FIXED: Get count of admin users
     */
    public long getAdminUserCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM User WHERE role = 'admin' AND is_active = 1";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting admin user count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return 0;
    }

    /**
     * Get all admin users
     */
    public List<User> getAdminUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE role = 'admin' AND is_active = 1 ORDER BY full_name";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                do {
                    users.add(createUserFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting admin users: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return users;
    }

    /**
     * Authenticate user login
     */
    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE username = ? AND password = ? AND is_active = 1";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{username, password});
            
            if (cursor.moveToFirst()) {
                return createUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error authenticating user: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return null;
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM User WHERE username = ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{username});
            
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error checking username exists: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return false;
    }

    /**
     * Update user password
     */
    public boolean updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);

        try {
            int rowsAffected = db.update("User", values, "user_id = ?", new String[]{String.valueOf(userId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating user password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deactivate user (soft delete)
     */
    public boolean deactivateUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", 0);

        try {
            int rowsAffected = db.update("User", values, "user_id = ?", new String[]{String.valueOf(userId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error deactivating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reactivate user
     */
    public boolean reactivateUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", 1);

        try {
            int rowsAffected = db.update("User", values, "user_id = ?", new String[]{String.valueOf(userId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error reactivating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get total user count
     */
    public int getTotalUserCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM User";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting total user count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return 0;
    }

    /**
     * Get active user count
     */
    public int getActiveUserCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM User WHERE is_active = 1";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting active user count: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return 0;
    }

    /**
     * Helper method to create User object from cursor
     */
    private User createUserFromCursor(Cursor cursor) {
        User user = new User();
        
        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        
        // Handle email (may be null)
        int emailIndex = cursor.getColumnIndex("email");
        if (emailIndex >= 0 && !cursor.isNull(emailIndex)) {
            user.setEmail(cursor.getString(emailIndex));
        }
        
        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);
        user.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow("created_date")));
        
        return user;
    }

    /**
     * Get current timestamp
     */
    private String getCurrentTimestamp() {
        return dateFormat.format(new Date());
    }
}