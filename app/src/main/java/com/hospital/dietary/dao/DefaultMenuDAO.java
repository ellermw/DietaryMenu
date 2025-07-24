package com.hospital.dietary.dao;

import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.data.database.AppDatabase;
import com.hospital.dietary.data.entities.DefaultMenuEntity;
import com.hospital.dietary.models.DefaultMenu;
import com.hospital.dietary.models.DefaultMenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DefaultMenuDAO compatibility class that wraps Room DAO operations
 */
public class DefaultMenuDAO {

    private final DatabaseHelper dbHelper;
    private final AppDatabase roomDatabase;
    private final com.hospital.dietary.data.dao.DefaultMenuDao roomDefaultMenuDao;

    public DefaultMenuDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.roomDatabase = dbHelper.getRoomDatabase();
        this.roomDefaultMenuDao = roomDatabase.defaultMenuDao();
    }

    /**
     * Add default menu item
     */
    public long addDefaultMenuItem(DefaultMenu menuItem) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> result = new AtomicReference<>(-1L);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            DefaultMenuEntity entity = convertToEntity(menuItem);
            long id = roomDefaultMenuDao.insertDefaultMenuItem(entity);
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
     * Get all default menu items
     */
    public List<DefaultMenu> getAllDefaultMenuItems() {
        // For now, return empty list - implement when Room DAO is complete
        return new ArrayList<>();
    }

    /**
     * Get default menu items by diet and meal type
     */
    public List<DefaultMenu> getDefaultMenuByDietAndMeal(String dietType, String mealType) {
        // For now, return empty list - implement when Room DAO is complete
        return new ArrayList<>();
    }

    /**
     * Get default menu items by diet, meal type and day
     */
    public List<DefaultMenuItem> getDefaultMenuItems(String dietType, String mealType, String dayOfWeek) {
        // For now, return empty list - implement when Room DAO is complete
        List<DefaultMenuItem> items = new ArrayList<>();
        // Temporary implementation
        return items;
    }

    /**
     * Convert DefaultMenu model to Entity
     */
    private DefaultMenuEntity convertToEntity(DefaultMenu menu) {
        DefaultMenuEntity entity = new DefaultMenuEntity();
        entity.setDietType(menu.getDietType());
        entity.setMealType(menu.getMealType());
        entity.setDayOfWeek(menu.getDayOfWeek());
        entity.setItemName(menu.getItemName());
        entity.setItemCategory(menu.getItemCategory());
        entity.setActive(menu.isActive());
        return entity;
    }

    /**
     * Convert Entity to DefaultMenu model
     */
    private DefaultMenu convertFromEntity(DefaultMenuEntity entity) {
        DefaultMenu menu = new DefaultMenu();
        menu.setMenuId((int) entity.getMenuId());
        menu.setDietType(entity.getDietType());
        menu.setMealType(entity.getMealType());
        menu.setDayOfWeek(entity.getDayOfWeek());
        menu.setItemName(entity.getItemName());
        menu.setItemCategory(entity.getItemCategory());
        menu.setActive(entity.isActive());
        return menu;
    }
}