package com.hospital.dietary.models;

public class DefaultMenuItem {
    private int id;
    private String dietType;       // Regular, ADA, Cardiac
    private String dayOfWeek;      // Monday-Sunday, or "All Days" for breakfast
    private String mealType;       // Breakfast, Lunch, Dinner
    private String itemName;       // Name of the food item
    private String category;       // Main Dish, Side Dish, Vegetable, etc.
    private String description;    // Optional description
    private boolean isActive;      // Whether this item is currently active
    private int sortOrder;         // Order for displaying items

    public DefaultMenuItem() {
        this.isActive = true;
        this.sortOrder = 0;
    }

    public DefaultMenuItem(String dietType, String dayOfWeek, String mealType, String itemName, String category) {
        this();
        this.dietType = dietType;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.itemName = itemName;
        this.category = category;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
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
    public String getDisplayName() {
        if (description != null && !description.trim().isEmpty()) {
            return itemName + " - " + description;
        }
        return itemName;
    }

    public boolean isBreakfastItem() {
        return "Breakfast".equalsIgnoreCase(mealType);
    }

    public boolean appliesToAllDays() {
        return "All Days".equalsIgnoreCase(dayOfWeek);
    }

    @Override
    public String toString() {
        return "DefaultMenuItem{" +
                "id=" + id +
                ", dietType='" + dietType + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", mealType='" + mealType + '\'' +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", isActive=" + isActive +
                ", sortOrder=" + sortOrder +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DefaultMenuItem that = (DefaultMenuItem) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}