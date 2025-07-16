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

    public User authenticateUser(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

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

    public boolean deleteUser(int userId) {
        // Don't actually delete, just deactivate
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_active", 0);

        return db.update("User", values, "user_id = ?", 
                        new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean permanentDeleteUser(int userId) {
        // Only use this if you really want to permanently delete
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("User", "user_id = ?", 
                        new String[]{String.valueOf(userId)}) > 0;
    }

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
}