package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.User;
import java.text.ParseException;
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
        values.put("password", user.getPassword()); // Note: In production, hash the password
        values.put("full_name", user.getFullName());
        values.put("user_role", user.getRole());
        values.put("is_active", user.isActive() ? 1 : 0);
        values.put("force_password_change", user.isMustChangePassword() ? 1 : 0);
        values.put("created_date", getCurrentTimestamp());

        try {
            return db.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e("UserDAO", "Error adding user: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Update an existing user
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("full_name", user.getFullName());
        values.put("user_role", user.getRole());
        values.put("is_active", user.isActive() ? 1 : 0);
        values.put("force_password_change", user.isMustChangePassword() ? 1 : 0);

        try {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(user.getUserId())}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a user
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rowsAffected = db.delete(
                    DatabaseHelper.TABLE_USERS,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        User user = null;
        if (cursor.moveToFirst()) {
            user = createUserFromCursor(cursor);
        }
        cursor.close();
        return user;
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        User user = null;
        if (cursor.moveToFirst()) {
            user = createUserFromCursor(cursor);
        }
        cursor.close();
        return user;
    }

    /**
     * Authenticate user with username and password
     */
    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        User user = null;
        if (cursor.moveToFirst()) {
            user = createUserFromCursor(cursor);
        }
        cursor.close();
        return user;
    }

    /**
     * Update user's last login timestamp
     */
    public boolean updateLastLogin(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("last_login", getCurrentTimestamp());

        try {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating last login: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " ORDER BY username";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            users.add(createUserFromCursor(cursor));
        }
        cursor.close();
        return users;
    }

    /**
     * Get all active users
     */
    public List<User> getActiveUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE is_active = 1 ORDER BY username";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            users.add(createUserFromCursor(cursor));
        }
        cursor.close();
        return users;
    }

    /**
     * Get all active admin users
     */
    public List<User> getActiveAdmins() {
        List<User> admins = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE is_active = 1 AND user_role = 'admin' ORDER BY username";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            admins.add(createUserFromCursor(cursor));
        }
        cursor.close();
        return admins;
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE user_role = ? ORDER BY username";
        Cursor cursor = db.rawQuery(query, new String[]{role});

        while (cursor.moveToNext()) {
            users.add(createUserFromCursor(cursor));
        }
        cursor.close();
        return users;
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    /**
     * Get users who must change password
     */
    public List<User> getUsersNeedingPasswordChange() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE force_password_change = 1 AND is_active = 1 ORDER BY username";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            users.add(createUserFromCursor(cursor));
        }
        cursor.close();
        return users;
    }

    /**
     * Update password change requirement
     */
    public boolean updatePasswordChangeRequirement(int userId, boolean mustChange) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("force_password_change", mustChange ? 1 : 0);

        try {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating password change requirement: " + e.getMessage());
            return false;
        }
    }

    /**
     * Change user password
     */
    public boolean changePassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        values.put("force_password_change", 0); // Reset flag

        try {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error changing password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user count by role
     */
    public int getUserCountByRole(String role) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE user_role = ? AND is_active = 1";
        Cursor cursor = db.rawQuery(query, new String[]{role});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Get total active user count
     */
    public int getActiveUserCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE is_active = 1";
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Helper to build User from cursor
     */
    private User createUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("user_role")));
        user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);

        int forceChangeIndex = cursor.getColumnIndex("force_password_change");
        if (forceChangeIndex != -1) {
            user.setMustChangePassword(cursor.getInt(forceChangeIndex) == 1);
        }

        // Parse dates
        try {
            String createdDateStr = cursor.getString(cursor.getColumnIndexOrThrow("created_date"));
            if (createdDateStr != null) {
                user.setCreatedDate(dateFormat.parse(createdDateStr));
            }
        } catch (ParseException e) {
            Log.w("UserDAO", "Error parsing created_date: " + e.getMessage());
        }
        try {
            String lastLoginStr = cursor.getString(cursor.getColumnIndexOrThrow("last_login"));
            if (lastLoginStr != null) {
                user.setLastLogin(dateFormat.parse(lastLoginStr));
            }
        } catch (ParseException e) {
            Log.w("UserDAO", "Error parsing last_login: " + e.getMessage());
        }
        return user;
    }

    private String getCurrentTimestamp() {
        return dateFormat.format(new Date());
    }
}
