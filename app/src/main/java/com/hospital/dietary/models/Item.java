package com.hospital.dietary.models;

/**
 * Item model class for backward compatibility
 */
public class Item {
    private int itemId;
    private String itemName;
    private String category;
    private String description;
    private int isAdaFriendly;

    // Constructors
    public Item() {}

    public Item(String itemName, String category, String description, int isAdaFriendly) {
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.isAdaFriendly = isAdaFriendly;
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

    // Helper method for boolean conversion
    public boolean isAdaFriendly() {
        return isAdaFriendly == 1;
    }

    public void setAdaFriendly(boolean adaFriendly) {
        this.isAdaFriendly = adaFriendly ? 1 : 0;
    }

    @Override
    public String toString() {
        return itemName;
    }
}