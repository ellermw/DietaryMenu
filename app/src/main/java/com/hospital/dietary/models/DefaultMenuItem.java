package com.hospital.dietary.models;

/**
 * DefaultMenuItem model class for backward compatibility
 */
public class DefaultMenuItem {
    private int id;
    private String itemName;
    private String category;
    private boolean isActive;

    // Constructors
    public DefaultMenuItem() {}

    public DefaultMenuItem(String itemName, String category) {
        this.itemName = itemName;
        this.category = category;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return itemName + " (" + category + ")";
    }
}