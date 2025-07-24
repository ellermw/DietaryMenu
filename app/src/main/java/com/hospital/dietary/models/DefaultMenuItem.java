package com.hospital.dietary.models;

public class DefaultMenuItem {
    private String itemName;
    private String category;
    private String dietType;
    private String mealType;
    private String dayOfWeek;

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDietType() { return dietType; }
    public void setDietType(String dietType) { this.dietType = dietType; }
}