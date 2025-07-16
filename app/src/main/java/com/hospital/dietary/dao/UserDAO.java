// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/dao/UserDAO.java
// ================================================================================================

package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDAO {
    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat;

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    /**
     * Authenticate user with username and password
     */
    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String passwordHash = hashPassword(password);

        String query = "SELECT * FROM User WHERE username = ? AND password_hash = ? AND is_active = 1";
        Cursor cursor = db.rawQuery(query, new String[]{username, passwordHash});

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            // Update last login time
            updateLastLogin(user.getUserId());
        }

        cursor.close();
        return user;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM User ORDER BY username";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return users;
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM User WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }

        cursor.close();
        return user;
    }

    /**
     * Create new user
     */
    public boolean createUser(String username, String password, String role) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check if username already exists
            if (getUserByUsername(username) != null) {
                return false; // Username already exists
            }

            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password_hash", hashPassword(password));
            values.put("role", role);
            values.put("created_date", dateFormat.format(new Date()));
            values.put("is_active", 1);

            long result = db.insert("User", null, values);
            return result != -1;

        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update user
     */
    public boolean updateUser(User user) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("username", user.getUsername());
            values.put("role", user.getRole());
            values.put("is_active", user.isActive() ? 1 : 0);

            // Only update password if it's provided
            if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                values.put("password_hash", user.getPasswordHash());
            }

            int rowsUpdated = db.update("User", values, "user_id = ?",
                    new String[]{String.valueOf(user.getUserId())});

            return rowsUpdated > 0;

        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update user password
     */
    public boolean updateUserPassword(int userId, String newPassword) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("password_hash", hashPassword(newPassword));

            int rowsUpdated = db.update("User", values, "user_id = ?",
                    new String[]{String.valueOf(userId)});

            return rowsUpdated > 0;

        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete user (soft delete - set inactive)
     */
    public boolean deleteUser(int userId) {
        try {
            // Prevent deletion of the last admin user
            if (isLastAdmin(userId)) {
                return false;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("is_active", 0);

            int rowsUpdated = db.update("User", values, "user_id = ?",
                    new String[]{String.valueOf(userId)});

            return rowsUpdated > 0;

        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if user is the last admin
     */
    private boolean isLastAdmin(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Get the user being deleted
        String userQuery = "SELECT role FROM User WHERE user_id = ?";
        Cursor userCursor = db.rawQuery(userQuery, new String[]{String.valueOf(userId)});

        String userRole = null;
        if (userCursor.moveToFirst()) {
            userRole = userCursor.getString(0);
        }
        userCursor.close();

        // If not an admin, deletion is allowed
        if (!"Admin".equals(userRole)) {
            return false;
        }

        // Count active admin users
        String countQuery = "SELECT COUNT(*) FROM User WHERE role = 'Admin' AND is_active = 1";
        Cursor countCursor = db.rawQuery(countQuery, null);

        int adminCount = 0;
        if (countCursor.moveToFirst()) {
            adminCount = countCursor.getInt(0);
        }
        countCursor.close();

        // If there's only one admin, don't allow deletion
        return adminCount <= 1;
    }

    /**
     * Update last login time
     */
    private void updateLastLogin(int userId) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("last_login", dateFormat.format(new Date()));

            db.update("User", values, "user_id = ?",
                    new String[]{String.valueOf(userId)});

        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert cursor to User object
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();

        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow("password_hash")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        user.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow("created_date")));

        int lastLoginIndex = cursor.getColumnIndex("last_login");
        if (lastLoginIndex != -1 && !cursor.isNull(lastLoginIndex)) {
            user.setLastLogin(cursor.getString(lastLoginIndex));
        }

        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);

        return user;
    }

    /**
     * Simple password hashing - use proper hashing in production
     */
    private String hashPassword(String password) {
        // This is a very basic hash - use BCrypt or similar in production
        return String.valueOf(password.hashCode());
    }

    /**
     * Get active users count by role
     */
    public int getUserCountByRole(String role) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM User WHERE role = ? AND is_active = 1";
        Cursor cursor = db.rawQuery(query, new String[]{role});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }
}