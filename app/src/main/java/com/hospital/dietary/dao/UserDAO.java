package com.hospital.dietary.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.data.database.AppDatabase;
import com.hospital.dietary.data.entities.UserEntity;
import com.hospital.dietary.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * UserDAO compatibility class that wraps Room DAO operations
 */
public class UserDAO {

    private final DatabaseHelper dbHelper;
    private final AppDatabase roomDatabase;
    private final com.hospital.dietary.data.dao.UserDao roomUserDao;

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.roomDatabase = dbHelper.getRoomDatabase();
        this.roomUserDao = roomDatabase.userDao();
    }

    /**
     * Validate user credentials
     */
    public boolean validateUser(String username, String password) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> result = new AtomicReference<>(false);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity user = roomUserDao.getUserByUsername(username);
            result.set(user != null && user.getPassword().equals(password) && user.isActive());
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result.get();
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<User> result = new AtomicReference<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity entity = roomUserDao.getUserByUsername(username);
            if (entity != null) {
                User user = new User();
                user.setUserId((int) entity.getUserId());
                user.setUsername(entity.getUsername());
                user.setPassword(entity.getPassword());
                user.setFullName(entity.getFullName());
                user.setRole(entity.getRole());
                user.setActive(entity.isActive());
                result.set(user);
            }
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result.get();
    }

    /**
     * Add new user
     */
    public long addUser(User user) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> result = new AtomicReference<>(-1L);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity entity = new UserEntity();
            entity.setUsername(user.getUsername());
            entity.setPassword(user.getPassword());
            entity.setFullName(user.getFullName());
            entity.setRole(user.getRole());
            entity.setActive(user.isActive());

            long id = roomUserDao.insertUser(entity);
            result.set(id);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result.get();
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<User>> result = new AtomicReference<>(new ArrayList<>());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<UserEntity> entities = roomUserDao.getAllUsers();
            List<User> users = new ArrayList<>();

            for (UserEntity entity : entities) {
                User user = new User();
                user.setUserId((int) entity.getUserId());
                user.setUsername(entity.getUsername());
                user.setPassword(entity.getPassword());
                user.setFullName(entity.getFullName());
                user.setRole(entity.getRole());
                user.setActive(entity.isActive());
                users.add(user);
            }

            result.set(users);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result.get();
    }

    /**
     * Update user
     */
    public boolean updateUser(User user) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> result = new AtomicReference<>(false);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity entity = roomUserDao.getUserById(user.getUserId());
            if (entity != null) {
                entity.setUsername(user.getUsername());
                entity.setPassword(user.getPassword());
                entity.setFullName(user.getFullName());
                entity.setRole(user.getRole());
                entity.setActive(user.isActive());
                roomUserDao.updateUser(entity);
                result.set(true);
            }
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result.get();
    }

    /**
     * Delete user
     */
    public void deleteUser(int userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            roomUserDao.deleteUserById(userId);
        });
    }
}