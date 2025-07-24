package com.hospital.dietary.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.hospital.dietary.data.database.AppDatabase;
import com.hospital.dietary.data.dao.UserDao;
import com.hospital.dietary.data.entities.UserEntity;
import java.util.Date;
import java.util.List;

/**
 * Repository for User data operations
 * Provides a clean API layer between ViewModels and data sources
 */
public class UserRepository {
    
    private final UserDao userDao;
    private final AppDatabase database;
    
    public UserRepository(Application application) {
        database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
    }
    
    // Get all users as LiveData
    public LiveData<List<UserEntity>> getAllUsersLive() {
        return userDao.getAllUsersLive();
    }
    
    // Get all active users as LiveData
    public LiveData<List<UserEntity>> getActiveUsersLive() {
        return userDao.getActiveUsersLive();
    }
    
    // Get user by username
    public void getUserByUsername(String username, RepositoryCallback<UserEntity> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                UserEntity user = userDao.getUserByUsername(username);
                callback.onSuccess(user);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Validate login
    public void validateLogin(String username, String password, RepositoryCallback<UserEntity> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                UserEntity user = userDao.getUserByUsername(username);
                if (user != null && user.getPassword().equals(password)) {
                    callback.onSuccess(user);
                } else {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Add new user
    public void addUser(UserEntity user, RepositoryCallback<Long> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                long id = userDao.insertUser(user);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Update user
    public void updateUser(UserEntity user, RepositoryCallback<Integer> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsUpdated = userDao.updateUser(user);
                callback.onSuccess(rowsUpdated);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Delete user
    public void deleteUser(UserEntity user, RepositoryCallback<Integer> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsDeleted = userDao.deleteUser(user);
                callback.onSuccess(rowsDeleted);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Update last login
    public void updateLastLogin(long userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateLastLogin(userId, new Date());
        });
    }
    
    // Update password
    public void updatePassword(long userId, String newPassword, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rows = userDao.updatePassword(userId, newPassword);
                userDao.updatePasswordChangeRequirement(userId, false);
                callback.onSuccess(rows > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Check if username exists
    public void checkUsernameExists(String username, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int count = userDao.countByUsername(username);
                callback.onSuccess(count > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Create default admin if needed
    public void createDefaultAdminIfNeeded() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                UserEntity existingAdmin = userDao.getUserByUsername("admin");
                if (existingAdmin == null) {
                    UserEntity admin = new UserEntity();
                    admin.setUsername("admin");
                    admin.setPassword("admin123");
                    admin.setFullName("System Administrator");
                    admin.setRole("Admin");
                    admin.setActive(true);
                    admin.setMustChangePassword(true);
                    userDao.insertUser(admin);
                }
            } catch (Exception e) {
                // Log error but don't crash
                e.printStackTrace();
            }
        });
    }
    
    // Get users needing password change
    public LiveData<List<UserEntity>> getUsersNeedingPasswordChangeLive() {
        return userDao.getUsersNeedingPasswordChangeLive();
    }
    
    // Get users by role
    public LiveData<List<UserEntity>> getUsersByRoleLive(String role) {
        return userDao.getUsersByRoleLive(role);
    }
    
    // Generic callback interface
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}