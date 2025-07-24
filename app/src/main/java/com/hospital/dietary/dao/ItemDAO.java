package com.hospital.dietary.dao;

import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.data.database.AppDatabase;
import com.hospital.dietary.data.entities.ItemEntity;
import com.hospital.dietary.models.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ItemDAO compatibility class that wraps Room DAO operations
 */
public class ItemDAO {

    private final DatabaseHelper dbHelper;
    private final AppDatabase roomDatabase;
    private final com.hospital.dietary.data.dao.ItemDao roomItemDao;

    public ItemDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.roomDatabase = dbHelper.getRoomDatabase();
        this.roomItemDao = roomDatabase.itemDao();
    }

    /**
     * Add new item
     */
    public long addItem(Item item) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> result = new AtomicReference<>(-1L);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            ItemEntity entity = convertToEntity(item);
            long id = roomItemDao.insertItem(entity);
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
     * Get item by ID
     */
    public Item getItemById(long itemId) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Item> result = new AtomicReference<>();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            ItemEntity entity = roomItemDao.getItemById(itemId);
            if (entity != null) {
                result.set(convertFromEntity(entity));
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
     * Get all items
     */
    public List<Item> getAllItems() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Item>> result = new AtomicReference<>(new ArrayList<>());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ItemEntity> entities = roomItemDao.getAllItems();
            List<Item> items = new ArrayList<>();

            for (ItemEntity entity : entities) {
                items.add(convertFromEntity(entity));
            }

            result.set(items);
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
     * Get items by category
     */
    public List<Item> getItemsByCategory(String category) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Item>> result = new AtomicReference<>(new ArrayList<>());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ItemEntity> entities = roomItemDao.getItemsByCategory(category);
            List<Item> items = new ArrayList<>();

            for (ItemEntity entity : entities) {
                items.add(convertFromEntity(entity));
            }

            result.set(items);
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
     * Update item
     */
    public void updateItem(Item item) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ItemEntity entity = convertToEntity(item);
            entity.setItemId(item.getItemId());
            roomItemDao.updateItem(entity);
        });
    }

    /**
     * Delete item
     */
    public void deleteItem(long itemId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            roomItemDao.deleteItemById(itemId);
        });
    }

    /**
     * Search items
     */
    public List<Item> searchItems(String searchTerm) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Item>> result = new AtomicReference<>(new ArrayList<>());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ItemEntity> entities = roomItemDao.searchItems(searchTerm);
            List<Item> items = new ArrayList<>();

            for (ItemEntity entity : entities) {
                items.add(convertFromEntity(entity));
            }

            result.set(items);
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
     * Get all categories
     */
    public List<String> getAllCategories() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<String>> result = new AtomicReference<>(new ArrayList<>());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<String> categories = roomItemDao.getAllCategories();
            result.set(categories);
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
     * Convert Item model to Entity
     */
    private ItemEntity convertToEntity(Item item) {
        ItemEntity entity = new ItemEntity();
        entity.setName(item.getItemName()); // Changed from setItemName to setName
        entity.setCategory(item.getCategory());
        entity.setDescription(item.getDescription());
        entity.setAdaFriendly(item.getIsAdaFriendly() == 1); // Changed from setIsAdaFriendly
        return entity;
    }

    /**
     * Convert Entity to Item model
     */
    private Item convertFromEntity(ItemEntity entity) {
        Item item = new Item();
        item.setItemId((int) entity.getItemId());
        item.setItemName(entity.getName()); // Changed from getItemName to getName
        item.setCategory(entity.getCategory());
        item.setDescription(entity.getDescription());
        item.setIsAdaFriendly(entity.isAdaFriendly() ? 1 : 0); // Changed from getIsAdaFriendly
        return item;
    }
}