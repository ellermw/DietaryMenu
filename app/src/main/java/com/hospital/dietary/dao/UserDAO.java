package com.hospital.dietary.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDAO {

    private DatabaseHelper dbHelper;

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
        values.put("role", user.getRole());
        values.put("full_name", user.getFullName());
        
        if (user.getEmail() != null) {
            values.put("email", user.getEmail());
        } else {
            values.putNull("email");
        }
        
        values.put("is_active", user.isActive() ? 1 : 0);
        values.put("created_date", getCurrentTimestamp());

        return db.insert("User", null, values);
    }

    /**
     * Update an existing user
     */
    public long updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("role", user.getRole());
        values.put("full_name", user.getFullName());
        
        if (user.getEmail() != null) {
            values.put("email", user.getEmail());
        } else {
            values.putNull("email");
        }
        
        values.put("is_active", user.isActive() ? 1 : 0);

        return db.update("User", values, "user_id = ?", 
                        new String[]{String.valueOf(user.getUserId())});
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE username = ? AND is_active = 1";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        User user = null;
        if (cursor.moveToFirst()) {
            user = createUserFromCursor(cursor);
        }

        cursor.close();
        return user;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        User user = null;
        if (cursor.moveToFirst()) {
            user = createUserFromCursor(cursor);
        }

        cursor.close();
        return user;
    }

    /**
     * Validate user login
     */
    public User validateLogin(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE username = ? AND password = ? AND is_active = 1";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        User user = null;
        if (cursor.moveToFirst()) {
            user = createUserFromCursor(cursor);
        }

        cursor.close();
        return user;
    }

    /**
     * FIXED: Backward compatibility method for authentication
     */
    public User authenticateUser(String username, String password) {
        return validateLogin(username, password);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User ORDER BY full_name";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int idxId = cursor.getColumnIndexOrThrow("user_id");
            int idxUsername = cursor.getColumnIndexOrThrow("username");
            int idxPassword = cursor.getColumnIndexOrThrow("password");
            int idxRole = cursor.getColumnIndexOrThrow("role");
            int idxFullName = cursor.getColumnIndexOrThrow("full_name");
            int idxEmail = cursor.getColumnIndex("email");
            int idxActive = cursor.getColumnIndexOrThrow("is_active");
            int idxCreated = cursor.getColumnIndexOrThrow("created_date");

            do {
                User user = new User();
                user.setUserId(cursor.getInt(idxId));
                user.setUsername(cursor.getString(idxUsername));
                user.setPassword(cursor.getString(idxPassword));
                user.setRole(cursor.getString(idxRole));
                user.setFullName(cursor.getString(idxFullName));
                
                if (!cursor.isNull(idxEmail)) {
                    user.setEmail(cursor.getString(idxEmail));
                }
                
                user.setActive(cursor.getInt(idxActive) == 1);
                user.setCreatedDate(cursor.getString(idxCreated));
                
                users.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return users;
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE role = ? " +
                       "AND is_active = 1 ORDER BY full_name";
        Cursor cursor = db.rawQuery(query, new String[]{role});

        if (cursor.moveToFirst()) {
            int idxId = cursor.getColumnIndexOrThrow("user_id");
            int idxUsername = cursor.getColumnIndexOrThrow("username");
            int idxPassword = cursor.getColumnIndexOrThrow("password");
            int idxRole = cursor.getColumnIndexOrThrow("role");
            int idxFullName = cursor.getColumnIndexOrThrow("full_name");
            int idxEmail = cursor.getColumnIndex("email");
            int idxActive = cursor.getColumnIndexOrThrow("is_active");
            int idxCreated = cursor.getColumnIndexOrThrow("created_date");

            do {
                User user = new User();
                user.setUserId(cursor.getInt(idxId));
                user.setUsername(cursor.getString(idxUsername));
                user.setPassword(cursor.getString(idxPassword));
                user.setRole(cursor.getString(idxRole));
                user.setFullName(cursor.getString(idxFullName));
                
                if (!cursor.isNull(idxEmail)) {
                    user.setEmail(cursor.getString(idxEmail));
                }
                
                user.setActive(cursor.getInt(idxActive) == 1);
                user.setCreatedDate(cursor.getString(idxCreated));
                
                users.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return users;
    }

    /**
     * FIXED: Check if username already exists (excluding specific user ID)
     */
    public boolean isUsernameExists(String username, int excludeUserId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query;
        String[] args;
        
        if (excludeUserId > 0) {
            query = "SELECT COUNT(*) FROM User WHERE username = ? AND user_id != ?";
            args = new String[]{username, String.valueOf(excludeUserId)};
        } else {
            query = "SELECT COUNT(*) FROM User WHERE username = ?";
            args = new String[]{username};
        }
        
        Cursor cursor = db.rawQuery(query, args);
        boolean exists = false;
        
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        
        cursor.close();
        return exists;
    }

    /**
     * Check if deleting this admin user would leave no active admins
     */
    public boolean isLastActiveAdmin(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Get the user's role first
        String roleQuery = "SELECT role FROM User WHERE user_id = ?";
        Cursor roleCursor = db.rawQuery(roleQuery, new String[]{String.valueOf(userId)});
        
        boolean isAdmin = false;
        if (roleCursor.moveToFirst()) {
            isAdmin = "admin".equals(roleCursor.getString(0));
        }
        roleCursor.close();
        
        if (!isAdmin) {
            return false; // Not an admin, so safe to delete
        }
        
        // Count other active admins
        String countQuery = "SELECT COUNT(*) FROM User WHERE role = 'admin' AND is_active = 1 AND user_id != ?";
        Cursor countCursor = db.rawQuery(countQuery, new String[]{String.valueOf(userId)});
        
        int otherAdminCount = 0;
        if (countCursor.moveToFirst()) {
            otherAdminCount = countCursor.getInt(0);
        }
        countCursor.close();
        
        return otherAdminCount == 0;
    }

    /**
     * Delete user (soft delete - set inactive)
     */
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", 0);
        
        int rowsAffected = db.update("User", values, "user_id = ?", 
                                   new String[]{String.valueOf(userId)});
        return rowsAffected > 0;
    }

    /**
     * Permanently delete user (hard delete)
     */
    public boolean permanentlyDeleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("User", "user_id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    /**
     * Activate/deactivate user
     */
    public boolean setUserActive(int userId, boolean active) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", active ? 1 : 0);
        
        int rowsAffected = db.update("User", values, "user_id = ?", 
                                   new String[]{String.valueOf(userId)});
        return rowsAffected > 0;
    }

    /**
     * Change user password
     */
    public boolean changePassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        
        int rowsAffected = db.update("User", values, "user_id = ?", 
                                   new String[]{String.valueOf(userId)});
        return rowsAffected > 0;
    }

    /**
     * Get user count by role
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

    /**
     * Get active admin count
     */
    public int getActiveAdminCount() {
        return getUserCountByRole("admin");
    }

    /**
     * Search users by name or username
     */
    public List<User> searchUsers(String searchTerm) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM User WHERE " +
                      "(full_name LIKE ? OR username LIKE ?) " +
                      "ORDER BY full_name";
        String searchPattern = "%" + searchTerm + "%";
        Cursor cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern});

        if (cursor.moveToFirst()) {
            do {
                User user = createUserFromCursor(cursor);
                users.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return users;
    }

    /**
     * Helper method to create User object from cursor
     */
    private User createUserFromCursor(Cursor cursor) {
        User user = new User();
        
        int idxId = cursor.getColumnIndexOrThrow("user_id");
        int idxUsername = cursor.getColumnIndexOrThrow("username");
        int idxPassword = cursor.getColumnIndexOrThrow("password");
        int idxRole = cursor.getColumnIndexOrThrow("role");
        int idxFullName = cursor.getColumnIndexOrThrow("full_name");
        int idxEmail = cursor.getColumnIndex("email");
        int idxActive = cursor.getColumnIndexOrThrow("is_active");
        int idxCreated = cursor.getColumnIndexOrThrow("created_date");

        user.setUserId(cursor.getInt(idxId));
        user.setUsername(cursor.getString(idxUsername));
        user.setPassword(cursor.getString(idxPassword));
        user.setRole(cursor.getString(idxRole));
        user.setFullName(cursor.getString(idxFullName));
        
        if (idxEmail >= 0 && !cursor.isNull(idxEmail)) {
            user.setEmail(cursor.getString(idxEmail));
        }
        
        user.setActive(cursor.getInt(idxActive) == 1);
        user.setCreatedDate(cursor.getString(idxCreated));

        return user;
    }

    /**
     * Get current timestamp
     */
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}