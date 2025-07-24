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
    private static final String TAG = "UserDAO";
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
        values.put("role", user.getRole()); // FIXED: Changed from user_role to role
        values.put("is_active", user.isActive() ? 1 : 0);
        values.put("must_change_password", user.isMustChangePassword() ? 1 : 0); // FIXED: Changed from force_password_change
        values.put("created_date", dateFormat.format(new Date()));

        try {
            long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            Log.d(TAG, "Added user: " + user.getUsername() + " with result: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE username = ?";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{username});
            if (cursor.moveToFirst()) {
                user = createUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by username: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE user_id = ?";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                user = createUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    /**
     * Validate user login
     */
    public User validateLogin(String username, String password) {
        Log.d(TAG, "Validating login for username: " + username);
        User user = getUserByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            Log.d(TAG, "Login validation successful for: " + username);
            return user;
        }

        Log.d(TAG, "Login validation failed for: " + username);
        return null;
    }

    /**
     * Update user
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("full_name", user.getFullName());
        values.put("role", user.getRole()); // FIXED: Changed from user_role to role
        values.put("is_active", user.isActive() ? 1 : 0);
        values.put("must_change_password", user.isMustChangePassword() ? 1 : 0); // FIXED: Changed from force_password_change

        try {
            int rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values,
                    "user_id = ?", new String[]{String.valueOf(user.getUserId())});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete user
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            int rowsAffected = db.delete(DatabaseHelper.TABLE_USERS,
                    "user_id = ?", new String[]{String.valueOf(userId)});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update last login timestamp
     */
    public boolean updateLastLogin(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("last_login", dateFormat.format(new Date()));

        try {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating last login: " + e.getMessage());
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
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                users.add(createUserFromCursor(cursor));
            }
            Log.d(TAG, "Retrieved " + users.size() + " users");
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users: " + e.getMessage());
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
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE is_active = 1 ORDER BY username";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                users.add(createUserFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting active users: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return users;
    }

    /**
     * Get all active admin users
     */
    public List<User> getActiveAdmins() {
        List<User> admins = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE is_active = 1 AND LOWER(role) = LOWER('Admin') ORDER BY username"; // FIXED: Changed user_role to role
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                admins.add(createUserFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting active admins: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return admins;
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE role = ? ORDER BY username"; // FIXED: Changed user_role to role
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{role});
            while (cursor.moveToNext()) {
                users.add(createUserFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting users by role: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return users;
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE username = ?";
        Cursor cursor = null;

        boolean exists = false;
        try {
            cursor = db.rawQuery(query, new String[]{username});
            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if username exists: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return exists;
    }

    /**
     * Get users who must change password
     */
    public List<User> getUsersNeedingPasswordChange() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS +
                " WHERE must_change_password = 1 AND is_active = 1 ORDER BY username"; // FIXED: Changed force_password_change to must_change_password
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                users.add(createUserFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting users needing password change: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return users;
    }

    /**
     * Update password change requirement
     */
    public boolean updatePasswordChangeRequirement(int userId, boolean mustChange) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("must_change_password", mustChange ? 1 : 0); // FIXED: Changed force_password_change to must_change_password

        try {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password change requirement: " + e.getMessage());
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
        values.put("must_change_password", 0); // FIXED: Changed force_password_change to must_change_password

        try {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(userId)}
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error changing password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create User object from cursor - FIXED column names
     */
    private User createUserFromCursor(Cursor cursor) {
        User user = new User();

        try {
            user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role"))); // FIXED: Changed user_role to role
            user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);
            user.setMustChangePassword(cursor.getInt(cursor.getColumnIndexOrThrow("must_change_password")) == 1); // FIXED: Changed force_password_change to must_change_password

            // Parse dates
            try {
                String createdDateStr = cursor.getString(cursor.getColumnIndex("created_date"));
                if (createdDateStr != null) {
                    user.setCreatedDate(dateFormat.parse(createdDateStr));
                }

                String lastLoginStr = cursor.getString(cursor.getColumnIndex("last_login"));
                if (lastLoginStr != null) {
                    user.setLastLogin(dateFormat.parse(lastLoginStr));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing dates: " + e.getMessage());
                // Set default dates if parsing fails
                user.setCreatedDate(new Date());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating user from cursor: " + e.getMessage());
            e.printStackTrace();
        }

        return user;
    }
}