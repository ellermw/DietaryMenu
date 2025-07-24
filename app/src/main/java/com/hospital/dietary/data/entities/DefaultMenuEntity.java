package com.hospital.dietary.data.entities;

import androidx.room.*;

@Entity(tableName = "default_menu")
public class DefaultMenuEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "menu_id")
    private long menuId;

    @ColumnInfo(name = "diet_type")
    private String dietType;

    @ColumnInfo(name = "meal_type")
    private String mealType;

    @ColumnInfo(name = "day_of_week")
    private String dayOfWeek;

    @ColumnInfo(name = "item_name")
    private String itemName;

    @ColumnInfo(name = "item_category")
    private String itemCategory;

    @ColumnInfo(name = "is_active")
    private boolean isActive = true;

    // Getters and Setters
    public long getMenuId() {
        return menuId;
    }

    public void setMenuId(long menuId) {
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