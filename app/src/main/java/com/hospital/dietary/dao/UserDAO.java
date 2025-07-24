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

        // For now, use plain text password comparison
        Cursor cursor = db.query("users", null,
                "username = ? AND password = ? AND is_active = 1",
                new String[]{username, password}, null, null, null);

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
        values.put("password", newPassword); // Store plain text for now
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
        values.put("password", user.getPassword()); // Store plain text for now
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
            values.put("password", user.getPassword()); // Store plain text for now
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

        Cursor cursor = db.query("users", null, null, null, null, null, "username ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return users;
    }

    /**
     * Get all active users
     */
    public List<User> getActiveUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("users", null, "is_active = 1",
                null, null, null, "username ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return users;
    }

    /**
     * Check if a username exists
     */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"user_id"},
                "username = ?", new String[]{username}, null, null, null);

        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();

        return exists;
    }

    /**
     * Create default admin user if none exists
     */
    public void createDefaultAdminIfNeeded() {
        if (getActiveAdmins().isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setFullName("System Administrator");
            admin.setRole("Admin");
            admin.setActive(true);
            admin.setMustChangePassword(false);

            insertUser(admin);
        }
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

        long lastLogin = cursor.getLong(cursor.getColumnIndex("last_login"));
        if (lastLogin > 0) {
            user.setLastLogin(new Date(lastLogin));
        }

        long createdDate = cursor.getLong(cursor.getColumnIndex("created_date"));
        if (createdDate > 0) {
            user.setCreatedDate(new Date(createdDate));
        }

        return user;
    }

    /**
     * Hash password using SHA-256 (kept for future use)
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
            e.printStackTrace();
            return password; // Return plain password if hashing fails
        }
    }
}