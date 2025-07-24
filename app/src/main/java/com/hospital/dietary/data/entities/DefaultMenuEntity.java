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

    // Add getters and setters
    public long getMenuId() { return menuId; }
    public void setMenuId(long menuId) { this.menuId = menuId; }
}