package com.hospital.dietary.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.hospital.dietary.data.entities.UserEntity;
import java.util.Date;
import java.util.List;

@Dao
public interface UserDao {

    @Insert
    long insertUser(UserEntity user);

    @Update
    int updateUser(UserEntity user);

    @Delete
    int deleteUser(UserEntity user);

    @Query("DELETE FROM users WHERE user_id = :userId")
    int deleteUserById(long userId);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE user_id = :userId")
    UserEntity getUserById(long userId);

    @Query("SELECT * FROM users ORDER BY username")
    LiveData<List<UserEntity>> getAllUsersLive();

    @Query("SELECT * FROM users ORDER BY username")
    List<UserEntity> getAllUsers();

    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY username")
    LiveData<List<UserEntity>> getActiveUsersLive();

    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY username")
    List<UserEntity> getActiveUsers();

    @Query("SELECT * FROM users WHERE role = :role ORDER BY username")
    LiveData<List<UserEntity>> getUsersByRoleLive(String role);

    @Query("SELECT * FROM users WHERE must_change_password = 1 AND is_active = 1")
    LiveData<List<UserEntity>> getUsersNeedingPasswordChangeLive();

    @Query("UPDATE users SET last_login = :lastLogin WHERE user_id = :userId")
    int updateLastLogin(long userId, Date lastLogin);

    @Query("UPDATE users SET password = :password WHERE user_id = :userId")
    int updatePassword(long userId, String password);

    @Query("UPDATE users SET must_change_password = :mustChange WHERE user_id = :userId")
    int updatePasswordChangeRequirement(long userId, boolean mustChange);

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countByUsername(String username);
}