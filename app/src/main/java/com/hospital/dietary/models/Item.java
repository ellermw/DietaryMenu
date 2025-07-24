package com.hospital.dietary.models;

/**
 * Item model class for dietary menu items
 */
public class Item {
    private int itemId;
    private String itemName;
    private String category;
    private String description;
    private int isAdaFriendly;
    private boolean isActive;
    private int sortOrder;

    // Constructor
    public Item() {
        this.isActive = true;
        this.isAdaFriendly = 0;
        this.sortOrder = 0;
    }

    public Item(String itemName, String category) {
        this();
        this.itemName = itemName;
        this.category = category;
    }

    // Getters and Setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    // Alias methods for compatibility
    public String getName() {
        return itemName;
    }

    public void setName(String name) {
        this.itemName = name;
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

    public boolean isAdaFriendly() {
        return isAdaFriendly == 1;
    }

    public void setAdaFriendly(boolean adaFriendly) {
        this.isAdaFriendly = adaFriendly ? 1 : 0;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    // Helper methods
    @Override
    public String toString() {
        return itemName + " (" + category + ")";
    }

    public String getDisplayName() {
        if (isAdaFriendly == 1) {
            return itemName + " (ADA)";
        }
        return itemName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return itemId == item.itemId;
    }

    @Override
    public int hashCode() {
        return itemId;
    }
}