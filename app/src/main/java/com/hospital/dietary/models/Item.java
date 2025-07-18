package com.hospital.dietary.models;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private int itemId;
    private int categoryId;
    private String name;
    private String description; // Added for admin functionality
    private Integer sizeML;
    private boolean isAdaFriendly;
    private boolean isSoda;
    private boolean isClearLiquid;  // FIXED: Added clear liquid property
    private String categoryName;

    // Default constructor
    public Item() {
        this.isAdaFriendly = false;
        this.isSoda = false;
        this.isClearLiquid = false;
        this.description = "";
    }

    // Constructor with basic parameters
    public Item(String name, int categoryId) {
        this();
        this.name = name;
        this.categoryId = categoryId;
    }

    // Constructor with size
    public Item(String name, int categoryId, Integer sizeML) {
        this(name, categoryId);
        this.sizeML = sizeML;
    }

    // Full constructor
    public Item(String name, int categoryId, Integer sizeML, boolean isAdaFriendly, boolean isSoda, boolean isClearLiquid) {
        this.name = name;
        this.categoryId = categoryId;
        this.sizeML = sizeML;
        this.isAdaFriendly = isAdaFriendly;
        this.isSoda = isSoda;
        this.isClearLiquid = isClearLiquid;
        this.description = "";
    }

    // Getters and Setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Added description methods for admin functionality
    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSizeML() {
        return sizeML;
    }

    public void setSizeML(Integer sizeML) {
        this.sizeML = sizeML;
    }

    public boolean isAdaFriendly() {
        return isAdaFriendly;
    }

    public void setAdaFriendly(boolean adaFriendly) {
        isAdaFriendly = adaFriendly;
    }

    // Added alias methods for admin functionality
    public boolean isAdaCompliant() {
        return isAdaFriendly();
    }

    public void setAdaCompliant(boolean adaCompliant) {
        setAdaFriendly(adaCompliant);
    }

    public boolean isSoda() {
        return isSoda;
    }

    public void setSoda(boolean soda) {
        isSoda = soda;
    }

    // FIXED: Clear liquid property getter/setter
    public boolean isClearLiquid() {
        return isClearLiquid;
    }

    public void setClearLiquid(boolean clearLiquid) {
        isClearLiquid = clearLiquid;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // Added alias method for admin functionality
    public String getCategory() {
        return getCategoryName();
    }

    public void setCategory(String category) {
        setCategoryName(category);
    }

    // Helper methods
    public boolean hasSize() {
        return sizeML != null && sizeML > 0;
    }

    public String getSizeDisplay() {
        if (hasSize()) {
            return sizeML + " ml";
        }
        return "";
    }

    public boolean isBread() {
        return categoryName != null && (
            categoryName.equalsIgnoreCase("Breads") ||
            categoryName.equalsIgnoreCase("Fresh Muffins") ||
            name.toLowerCase().contains("bread") ||
            name.toLowerCase().contains("muffin") ||
            name.toLowerCase().contains("roll")
        );
    }

    public boolean isDrink() {
        return categoryName != null && (
            categoryName.equalsIgnoreCase("Drink") ||
            categoryName.equalsIgnoreCase("Soda") ||
            categoryName.equalsIgnoreCase("Juices") ||
            categoryName.equalsIgnoreCase("Supplement") ||
            isSoda
        );
    }

    public boolean isGrillItem() {
        return categoryName != null && categoryName.equalsIgnoreCase("Grill Item");
    }

    // Diet compatibility methods
    public boolean isCompatibleWithDiet(String dietType) {
        if (dietType == null) {
            return true;
        }

        switch (dietType.toLowerCase()) {
            case "ada":
            case "diabetic":
                return isAdaFriendly;
            case "clear liquid":
                return isClearLiquid;
            case "cardiac":
            case "renal":
                // These diets typically require ADA-friendly items
                return isAdaFriendly;
            case "regular":
            case "full liquid":
            case "puree":
            default:
                return true; // Most items are compatible with regular diets
        }
    }

    // Display methods
    public String getDisplayName() {
        StringBuilder display = new StringBuilder(name);
        
        if (hasSize()) {
            display.append(" (").append(getSizeDisplay()).append(")");
        }
        
        List<String> tags = new ArrayList<>();
        if (isAdaFriendly) tags.add("ADA");
        if (isSoda) tags.add("Soda");
        if (isClearLiquid) tags.add("Clear");
        
        if (!tags.isEmpty()) {
            display.append(" [").append(String.join(", ", tags)).append("]");
        }
        
        return display.toString();
    }

    public String getFullDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Name: ").append(name).append("\n");
        desc.append("Category: ").append(categoryName != null ? categoryName : "Unknown").append("\n");
        
        if (description != null && !description.isEmpty()) {
            desc.append("Description: ").append(description).append("\n");
        }
        
        if (hasSize()) {
            desc.append("Size: ").append(getSizeDisplay()).append("\n");
        }
        
        desc.append("ADA Friendly: ").append(isAdaFriendly ? "Yes" : "No").append("\n");
        desc.append("Is Soda: ").append(isSoda ? "Yes" : "No").append("\n");
        desc.append("Clear Liquid: ").append(isClearLiquid ? "Yes" : "No");
        
        return desc.toString();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (itemId != item.itemId) return false;
        if (categoryId != item.categoryId) return false;
        if (isAdaFriendly != item.isAdaFriendly) return false;
        if (isSoda != item.isSoda) return false;
        if (isClearLiquid != item.isClearLiquid) return false;
        if (name != null ? !name.equals(item.name) : item.name != null) return false;
        if (description != null ? !description.equals(item.description) : item.description != null) return false;
        return sizeML != null ? sizeML.equals(item.sizeML) : item.sizeML == null;
    }

    @Override
    public int hashCode() {
        int result = itemId;
        result = 31 * result + categoryId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (sizeML != null ? sizeML.hashCode() : 0);
        result = 31 * result + (isAdaFriendly ? 1 : 0);
        result = 31 * result + (isSoda ? 1 : 0);
        result = 31 * result + (isClearLiquid ? 1 : 0);
        return result;
    }
}