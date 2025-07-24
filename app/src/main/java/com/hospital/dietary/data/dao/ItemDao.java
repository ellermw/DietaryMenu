package com.hospital.dietary.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.hospital.dietary.data.entities.ItemEntity;
import java.util.List;

@Dao
public interface ItemDao {

    @Insert
    long insertItem(ItemEntity item);

    @Insert
    long[] insertItems(List<ItemEntity> items);

    @Update
    int updateItem(ItemEntity item);

    @Delete
    int deleteItem(ItemEntity item);

    @Query("DELETE FROM items WHERE item_id = :itemId")
    int deleteItemById(long itemId);

    @Query("SELECT * FROM items ORDER BY category, name")
    LiveData<List<ItemEntity>> getAllItemsLive();

    @Query("SELECT * FROM items ORDER BY category, name")
    List<ItemEntity> getAllItems();

    @Query("SELECT * FROM items WHERE item_id = :itemId")
    ItemEntity getItemById(long itemId);

    @Query("SELECT * FROM items WHERE category = :category ORDER BY name")
    LiveData<List<ItemEntity>> getItemsByCategoryLive(String category);

    @Query("SELECT * FROM items WHERE category = :category ORDER BY name")
    List<ItemEntity> getItemsByCategory(String category);

    @Query("SELECT * FROM items WHERE category = :category AND is_ada_friendly = 1 ORDER BY name")
    LiveData<List<ItemEntity>> getAdaItemsByCategoryLive(String category);

    @Query("SELECT * FROM items WHERE category = :category AND is_ada_friendly = 1 ORDER BY name")
    List<ItemEntity> getAdaItemsByCategory(String category);

    @Query("SELECT * FROM items WHERE is_ada_friendly = 1 ORDER BY category, name")
    LiveData<List<ItemEntity>> getAllAdaItemsLive();

    @Query("SELECT * FROM items WHERE " +
            "LOWER(name) LIKE LOWER(:searchTerm) OR " +
            "LOWER(category) LIKE LOWER(:searchTerm) " +
            "ORDER BY category, name")
    LiveData<List<ItemEntity>> searchItemsLive(String searchTerm);

    @Query("SELECT * FROM items WHERE " +
            "LOWER(name) LIKE LOWER(:searchTerm) OR " +
            "LOWER(category) LIKE LOWER(:searchTerm) " +
            "ORDER BY category, name")
    List<ItemEntity> searchItems(String searchTerm);

    @Query("SELECT DISTINCT category FROM items ORDER BY category")
    LiveData<List<String>> getAllCategoriesLive();

    @Query("SELECT DISTINCT category FROM items ORDER BY category")
    List<String> getAllCategories();

    @Query("SELECT COUNT(*) FROM items")
    LiveData<Integer> getItemCountLive();

    @Query("SELECT COUNT(*) FROM items")
    int getItemCount();

    @Query("SELECT COUNT(*) FROM items WHERE LOWER(name) = LOWER(:name) AND LOWER(category) = LOWER(:category)")
    int countByNameAndCategory(String name, String category);
}