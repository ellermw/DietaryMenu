package com.hospital.dietary.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "items",
        indices = {
                @Index(value = {"category"}),
                @Index(value = {"is_ada_friendly"})
        })
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
    private boolean isAdaFriendly;

    @ColumnInfo(name = "created_date", defaultValue = "CURRENT_TIMESTAMP")
    private Date createdDate;

    public ItemEntity() {
        this.createdDate = new Date();
    }

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

    public boolean isAdaFriendly() {
        return isAdaFriendly;
    }

    public void setAdaFriendly(boolean adaFriendly) {
        isAdaFriendly = adaFriendly;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Helper method for display
    @Override
    public String toString() {
        return name;
    }
}