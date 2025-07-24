package com.hospital.dietary.models;

import java.util.List;

public class Item {
    private int itemId;
    private String name;
    private String category;
    private int categoryId;
    private String categoryName;
    private boolean adaFriendly;
    private String mealType;
    private Integer sizeML;
    private String description;
    private boolean isSoda;
    private boolean isClearLiquid;
    private boolean isDefault;

    // Default constructor
    public Item() {
        this.adaFriendly = false;
        this.isSoda = false;
        this.isClearLiquid = false;
        this.isDefault = false;
    }

    // Constructor with essential fields
    public Item(String name, String category, boolean adaFriendly) {
        this();
        this.name = name;
        this.category = category;
        this.adaFriendly = adaFriendly;
    }

    // Constructor with ID
    public Item(int itemId, String name, String category, boolean adaFriendly) {
        this(name, category, adaFriendly);
        this.itemId = itemId;
    }

    // Legacy constructors for backward compatibility
    public Item(String name, int categoryId, Integer adaFriendlyInt) {
        this();
        this.name = name;
        this.categoryId = categoryId;
        this.category = getCategoryName(categoryId);
        this.categoryName = this.category;
        this.adaFriendly = (adaFriendlyInt != null && adaFriendlyInt == 1);
    }

    public Item(String name, int categoryId, String adaFriendlyStr) {
        this();
        this.name = name;
        this.categoryId = categoryId;
        this.category = getCategoryName(categoryId);
        this.categoryName = this.category;
        this.adaFriendly = "1".equals(adaFriendlyStr) || "true".equalsIgnoreCase(adaFriendlyStr);
    }

    // Helper method to convert category ID to name
    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1: return "Breakfast Items";
            case 2: return "Proteins";
            case 3: return "Starches";
            case 4: return "Vegetables";
            case 5: return "Beverages";
            case 6: return "Juices";
            case 7: return "Desserts";
            case 8: return "Fruits";
            case 9: return "Dairy";
            default: return "Other";
        }
    }

    // Helper method to convert category name to ID
    private int getCategoryId(String categoryName) {
        if (categoryName == null) return 0;
        switch (categoryName) {
            case "Breakfast Items": return 1;
            case "Proteins": return 2;
            case "Starches": return 3;
            case "Vegetables": return 4;
            case "Beverages": return 5;
            case "Juices": return 6;
            case "Desserts": return 7;
            case "Fruits": return 8;
            case "Dairy": return 9;
            default: return 0;
        }
    }

    // Getters and Setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        this.categoryName = category;
        this.categoryId = getCategoryId(category);
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
        this.category = getCategoryName(categoryId);
        this.categoryName = this.category;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        this.category = categoryName;
        this.categoryId = getCategoryId(categoryName);
    }

    public boolean isAdaFriendly() {
        return adaFriendly;
    }

    public void setAdaFriendly(boolean adaFriendly) {
        this.adaFriendly = adaFriendly;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public Integer getSizeML() {
        return sizeML;
    }

    public void setSizeML(Integer sizeML) {
        this.sizeML = sizeML;
    }

    public void setSizeML(int sizeML) {
        this.sizeML = sizeML;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSoda() {
        return isSoda;
    }

    public void setSoda(boolean soda) {
        this.isSoda = soda;
    }

    public boolean isClearLiquid() {
        return isClearLiquid;
    }

    public void setClearLiquid(boolean clearLiquid) {
        this.isClearLiquid = clearLiquid;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    // Helper methods
    public String getAdaFriendlyString() {
        return adaFriendly ? "ADA Friendly" : "Regular";
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId=" + itemId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", categoryId=" + categoryId +
                ", mealType='" + mealType + '\'' +
                ", sizeML=" + sizeML +
                ", description='" + description + '\'' +
                ", adaFriendly=" + adaFriendly +
                ", isSoda=" + isSoda +
                ", isClearLiquid=" + isClearLiquid +
                ", isDefault=" + isDefault +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Item item = (Item) obj;
        return itemId == item.itemId &&
                name != null && name.equals(item.name);
    }

    @Override
    public int hashCode() {
        int result = itemId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}