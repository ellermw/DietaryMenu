package com.hospital.dietary.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room Entity for Item table
 * Represents food items that can be ordered
 */
@Entity(tableName = "items",
        indices = {@Index("category"), @Index("is_ada_friendly")})
public class ItemEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "item_id")
    private long itemId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "is_ada_friendly", defaultValue = "0")
    private int isAdaFriendly;

    // Constructor
    public ItemEntity() {}

    // Getters and Setters
    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIsAdaFriendly() {
        return isAdaFriendly;
    }

    public void setIsAdaFriendly(int isAdaFriendly) {
        this.isAdaFriendly = isAdaFriendly;
    }

    // Helper methods
    public boolean isAdaFriendly() {
        return isAdaFriendly == 1;
    }

    public void setAdaFriendly(boolean adaFriendly) {
        this.isAdaFriendly = adaFriendly ? 1 : 0;
    }
}