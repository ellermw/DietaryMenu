// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/dao/UserDAO.java
// ================================================================================================

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
     * Get all users from the database
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
     * Get a user by username
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM User WHERE username = ? AND is_active = 1";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        User user = null;
        if (cursor.moveToFirst()) {
            int idxId = cursor.getColumnIndexOrThrow("user_id");
            int idxUsername = cursor.getColumnIndexOrThrow("username");
            int idxPassword = cursor.getColumnIndexOrThrow("password");
            int idxRole = cursor.getColumnIndexOrThrow("role");
            int idxFullName = cursor.getColumnIndexOrThrow("full_name");
            int idxEmail = cursor.getColumnIndex("email");
            int idxActive = cursor.getColumnIndexOrThrow("is_active");
            int idxCreated = cursor.getColumnIndexOrThrow("created_date");

            user = new User();
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
        }

        cursor.close();
        return user;
    }

    /**
     * Authenticate a user with username and password
     */
    public User authenticateUser(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Add a new user to the database
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
        }
        
        values.put("is_active", user.isActive() ? 1 : 0);
        
        // Set current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        values.put("created_date", currentDate);

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
        }
        
        values.put("is_active", user.isActive() ? 1 : 0);

        return db.update("User", values, "user_id = ?", 
                        new String[]{String.valueOf(user.getUserId())});
    }

    /**
     * Soft delete a user (deactivate)
     */
    public boolean deleteUser(int userId) {
        // Don't actually delete, just deactivate
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", 0);

        return db.update("User", values, "user_id = ?", 
                        new String[]{String.valueOf(userId)}) > 0;
    }

    /**
     * Permanently delete a user (use with caution)
     */
    public boolean permanentDeleteUser(int userId) {
        // Only use this if you really want to permanently delete
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("User", "user_id = ?", 
                        new String[]{String.valueOf(userId)}) > 0;
    }

    /**
     * Check if a username already exists (excluding a specific user ID)
     */
    public boolean usernameExists(String username, int excludeUserId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM User WHERE LOWER(username) = LOWER(?) AND user_id != ? AND is_active = 1";
        Cursor cursor = db.rawQuery(query, new String[]{username, String.valueOf(excludeUserId)});

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }

        cursor.close();
        return exists;
    }

    /**
     * Get total count of active users
     */
    public int getUserCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM User WHERE is_active = 1";
        Cursor cursor = db.rawQuery(query, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        return count;
    }

    /**
     * Get count of active admin users
     */
    public int getAdminCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM User WHERE role = 'admin' AND is_active = 1";
        Cursor cursor = db.rawQuery(query, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        return count;
    }

    /**
     * Get count of active regular users
     */
    public int getRegularUserCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT COUNT(*) FROM User WHERE role = 'user' AND is_active = 1";
        Cursor cursor = db.rawQuery(query, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        return count;
    }

    /**
     * Search users by name or username
     */
    public List<User> searchUsers(String searchQuery) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM User WHERE " +
                      "(LOWER(full_name) LIKE LOWER(?) OR LOWER(username) LIKE LOWER(?)) " +
                      "AND is_active = 1 ORDER BY full_name";
        
        String searchPattern = "%" + searchQuery + "%";
        Cursor cursor = db.rawQuery(query, new String[]{searchPattern, searchPattern});

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

        String query = "SELECT * FROM User WHERE role = ? AND is_active = 1 ORDER BY full_name";
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
}