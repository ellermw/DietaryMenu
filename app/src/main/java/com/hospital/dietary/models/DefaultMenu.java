package com.hospital.dietary.models;

/**
 * DefaultMenu model class for backward compatibility
 */
public class DefaultMenu {
    private int menuId;
    private String dietType;
    private String mealType;
    private String dayOfWeek;
    private String itemName;
    private String itemCategory;
    private boolean isActive;

    // Constructors
    public DefaultMenu() {
        this.isActive = true;
    }

    public DefaultMenu(String dietType, String mealType, String dayOfWeek,
                       String itemName, String itemCategory) {
        this.dietType = dietType;
        this.mealType = mealType;
        this.dayOfWeek = dayOfWeek;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.isActive = true;
    }

    // Getters and Setters
    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}