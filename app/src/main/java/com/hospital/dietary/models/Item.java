package com.hospital.dietary.models;

public class Item {
    private int itemId;
    private String name;
    private String category; // FIXED: Added category field
    private boolean adaFriendly;

    // Default constructor
    public Item() {
        this.adaFriendly = false;
    }

    // FIXED: Constructor with category as String
    public Item(String name, String category, boolean adaFriendly) {
        this.name = name;
        this.category = category;
        this.adaFriendly = adaFriendly;
    }

    // Constructor with ID
    public Item(int itemId, String name, String category, boolean adaFriendly) {
        this.itemId = itemId;
        this.name = name;
        this.category = category;
        this.adaFriendly = adaFriendly;
    }

    // Legacy constructors for backward compatibility
    public Item(String name, int categoryId, Integer adaFriendlyInt) {
        this.name = name;
        this.category = getCategoryName(categoryId);
        this.adaFriendly = (adaFriendlyInt != null && adaFriendlyInt == 1);
    }

    public Item(String name, int categoryId, String adaFriendlyStr) {
        this.name = name;
        this.category = getCategoryName(categoryId);
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

    // FIXED: Added getCategory method
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAdaFriendly() {
        return adaFriendly;
    }

    public void setAdaFriendly(boolean adaFriendly) {
        this.adaFriendly = adaFriendly;
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
                ", adaFriendly=" + adaFriendly +
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