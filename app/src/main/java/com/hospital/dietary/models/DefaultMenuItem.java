package com.hospital.dietary.models;

import java.util.Date;

/**
 * DefaultMenuItem model class for default menu configurations
 */
public class DefaultMenuItem {
    private int id;
    private int itemId;
    private String itemName;
    private String category;
    private String dietType;
    private String mealType;
    private String dayOfWeek;
    private String description;
    private boolean isActive;
    private Date createdDate;

    // Constructor
    public DefaultMenuItem() {
        this.isActive = true;
        this.createdDate = new Date();
    }

    public DefaultMenuItem(String itemName, String category) {
        this();
        this.itemName = itemName;
        this.category = category;
    }

    public DefaultMenuItem(String itemName, String dietType, String mealType, String dayOfWeek) {
        this();
        this.itemName = itemName;
        this.dietType = dietType;
        this.mealType = mealType;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Helper methods
    public String getDisplayName() {
        return itemName != null ? itemName : "";
    }

    public String getFullDescription() {
        StringBuilder desc = new StringBuilder(getDisplayName());

        if (dietType != null && !dietType.isEmpty()) {
            desc.append(" (").append(dietType).append(")");
        }

        if (description != null && !description.isEmpty()) {
            desc.append(" - ").append(description);
        }

        return desc.toString();
    }

    public String getConfigurationKey() {
        return (dietType != null ? dietType : "") + "_" +
                (mealType != null ? mealType : "") + "_" +
                (dayOfWeek != null ? dayOfWeek : "");
    }

    @Override
    public String toString() {
        if (category != null) {
            return itemName + " (" + category + ")";
        } else if (dietType != null && mealType != null) {
            return itemName + " - " + dietType + " " + mealType + " " + dayOfWeek;
        } else {
            return itemName;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DefaultMenuItem that = (DefaultMenuItem) obj;

        if (id != that.id) return false;
        if (!itemName.equals(that.itemName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (itemName != null ? itemName.hashCode() : 0);
        return result;
    }
}