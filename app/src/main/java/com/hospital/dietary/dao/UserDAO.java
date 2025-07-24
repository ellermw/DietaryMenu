package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UserDAO class for user authentication and management
 */
public class UserDAO {

    private DatabaseHelper dbHelper;

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Validate user login
     */
    public User validateLogin(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        Cursor cursor = db.query("users", null,
                "username = ? AND password = ? AND is_active = 1",
                new String[]{username, hashedPassword}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    /**
     * Update last login time
     */
    public void updateLastLogin(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("last_login", System.currentTimeMillis());

        db.update("users", values, "user_id = ?",
                new String[]{String.valueOf(userId)});
    }

    /**
     * Change user password
     */
    public boolean changePassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", hashPassword(newPassword));
        values.put("must_change_password", 0);

        int rowsAffected = db.update("users", values, "user_id = ?",
                new String[]{String.valueOf(userId)});

        return rowsAffected > 0;
    }

    /**
     * Get all active admin users
     */
    public List<User> getActiveAdmins() {
        List<User> admins = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("users", null,
                "role = 'Admin' AND is_active = 1",
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                admins.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return admins;
    }

    /**
     * Insert a new user (alias for insertUser)
     */
    public long addUser(User user) {
        return insertUser(user);
    }

    /**
     * Insert a new user
     */
    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", user.getUsername());
        values.put("password", hashPassword(user.getPassword()));
        values.put("full_name", user.getFullName());
        values.put("role", user.getRole());
        values.put("is_active", user.isActive() ? 1 : 0);
        values.put("must_change_password", user.isMustChangePassword() ? 1 : 0);
        values.put("created_date", System.currentTimeMillis());

        return db.insert("users", null, values);
    }

    /**
     * Update an existing user - returns boolean for success
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", user.getUsername());
        values.put("full_name", user.getFullName());
        values.put("role", user.getRole());
        values.put("is_active", user.isActive() ? 1 : 0);
        values.put("must_change_password", user.isMustChangePassword() ? 1 : 0);

        // Only update password if provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            values.put("password", hashPassword(user.getPassword()));
        }

        int rowsUpdated = db.update("users", values, "user_id = ?",
                new String[]{String.valueOf(user.getUserId())});

        return rowsUpdated > 0;
    }

    /**
     * Delete a user (soft delete by marking inactive) - returns boolean for success
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", 0);

        int rowsUpdated = db.update("users", values, "user_id = ?",
                new String[]{String.valueOf(userId)});

        return rowsUpdated > 0;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "user_id = ?",
                new String[]{String.valueOf(userId)}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "username = ?",
                new String[]{username}, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("users", null, null, null, null, null, "username");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return users;
    }

    /**
     * Get active users
     */
    public List<User> getActiveUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("users", null, "is_active = 1",
                null, null, null, "username");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return users;
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"user_id"},
                "username = ?", new String[]{username}, null, null, null);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }

        return exists;
    }

    /**
     * Convert cursor to User object
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();

        user.setUserId(cursor.getInt(cursor.getColumnIndex("user_id")));
        user.setUsername(cursor.getString(cursor.getColumnIndex("username")));
        user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
        user.setFullName(cursor.getString(cursor.getColumnIndex("full_name")));
        user.setRole(cursor.getString(cursor.getColumnIndex("role")));
        user.setActive(cursor.getInt(cursor.getColumnIndex("is_active")) == 1);
        user.setMustChangePassword(cursor.getInt(cursor.getColumnIndex("must_change_password")) == 1);

        long lastLoginMillis = cursor.getLong(cursor.getColumnIndex("last_login"));
        if (lastLoginMillis > 0) {
            user.setLastLogin(new Date(lastLoginMillis));
        }

        long createdDateMillis = cursor.getLong(cursor.getColumnIndex("created_date"));
        if (createdDateMillis > 0) {
            user.setCreatedDate(new Date(createdDateMillis));
        }

        return user;
    }

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to plain password if hashing fails
            return password;
        }
    }
}