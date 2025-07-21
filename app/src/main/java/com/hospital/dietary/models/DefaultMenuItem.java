package com.hospital.dietary.models;

public class DefaultMenuItem {
    private int id;
    private int itemId;
    private String itemName;
    private String dietType;
    private String mealType;
    private String dayOfWeek;
    private String description;
    private String createdDate;

    // Constructors
    public DefaultMenuItem() {}

    public DefaultMenuItem(int itemId, String itemName, String dietType, String mealType, String dayOfWeek) {
        this.itemId = itemId;
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

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    // Utility methods
    @Override
    public String toString() {
        return itemName + " (" + dietType + " - " + mealType +
                ("Breakfast".equals(mealType) ? "" : " - " + dayOfWeek) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DefaultMenuItem that = (DefaultMenuItem) obj;
        return itemId == that.itemId &&
                dietType.equals(that.dietType) &&
                mealType.equals(that.mealType) &&
                dayOfWeek.equals(that.dayOfWeek);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(itemId, dietType, mealType, dayOfWeek);
    }
}