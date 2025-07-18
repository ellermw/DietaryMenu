package com.hospital.dietary.models;

public class Item {
    private int itemId;
    private int categoryId;
    private String name;
    private String description;
    private Integer sizeML;
    private boolean isAdaFriendly;
    private boolean isSoda;
    private boolean isClearLiquid;
    private String mealType;
    private boolean isDefault;
    private String categoryName; // For display purposes, populated by JOIN queries

    // Default constructor
    public Item() {
        this.isAdaFriendly = false;
        this.isSoda = false;
        this.isClearLiquid = false;
        this.isDefault = false;
        this.mealType = "General";
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

    // Constructor with meal type
    public Item(String name, int categoryId, String mealType) {
        this(name, categoryId);
        this.mealType = mealType;
    }

    // Full constructor
    public Item(String name, int categoryId, Integer sizeML, boolean isAdaFriendly, 
                boolean isSoda, boolean isClearLiquid, String mealType, boolean isDefault) {
        this.name = name;
        this.categoryId = categoryId;
        this.sizeML = sizeML;
        this.isAdaFriendly = isAdaFriendly;
        this.isSoda = isSoda;
        this.isClearLiquid = isClearLiquid;
        this.mealType = mealType != null ? mealType : "General";
        this.isDefault = isDefault;
        this.description = "";
    }

    // Primary Getters and Setters
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
        this.isAdaFriendly = adaFriendly;
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

    public String getMealType() {
        return mealType != null ? mealType : "General";
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean defaultItem) {
        this.isDefault = defaultItem;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // Utility methods
    public boolean hasSize() {
        return sizeML != null && sizeML > 0;
    }

    public String getSizeDisplay() {
        return hasSize() ? sizeML + "ml" : "N/A";
    }

    public String getPropertiesDisplay() {
        StringBuilder props = new StringBuilder();
        if (isAdaFriendly) props.append("ADA Friendly, ");
        if (isSoda) props.append("Carbonated, ");
        if (isClearLiquid) props.append("Clear Liquid, ");
        if (isDefault) props.append("Default Item, ");
        
        String result = props.toString();
        return result.endsWith(", ") ? result.substring(0, result.length() - 2) : result;
    }

    public boolean matchesFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return true;
        }
        
        String lowerFilter = filter.toLowerCase();
        return name.toLowerCase().contains(lowerFilter) ||
               (description != null && description.toLowerCase().contains(lowerFilter)) ||
               (categoryName != null && categoryName.toLowerCase().contains(lowerFilter)) ||
               mealType.toLowerCase().contains(lowerFilter);
    }

    public boolean isValidForDiet(String diet) {
        if (diet == null) return true;
        
        // ADA Diabetic diet restrictions
        if ("ADA Diabetic".equalsIgnoreCase(diet)) {
            return isAdaFriendly;
        }
        
        // Liquid diet restrictions  
        if ("Liquid".equalsIgnoreCase(diet)) {
            return isClearLiquid || hasSize();
        }
        
        // Clear liquid diet restrictions
        if ("Clear Liquid".equalsIgnoreCase(diet)) {
            return isClearLiquid;
        }
        
        // All other diets accept all items
        return true;
    }

    // Validation methods
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() && categoryId > 0;
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (name == null || name.trim().isEmpty()) {
            errors.append("Name is required. ");
        }
        
        if (categoryId <= 0) {
            errors.append("Valid category is required. ");
        }
        
        if (sizeML != null && sizeML <= 0) {
            errors.append("Size must be greater than 0. ");
        }
        
        return errors.toString().trim();
    }

    // Override methods
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        
        if (hasSize()) {
            sb.append(" (").append(getSizeDisplay()).append(")");
        }
        
        if (categoryName != null) {
            sb.append(" - ").append(categoryName);
        }
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Item item = (Item) obj;
        return itemId == item.itemId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(itemId);
    }

    // Copy methods for editing
    public Item copy() {
        Item copy = new Item();
        copy.itemId = this.itemId;
        copy.categoryId = this.categoryId;
        copy.name = this.name;
        copy.description = this.description;
        copy.sizeML = this.sizeML;
        copy.isAdaFriendly = this.isAdaFriendly;
        copy.isSoda = this.isSoda;
        copy.isClearLiquid = this.isClearLiquid;
        copy.mealType = this.mealType;
        copy.isDefault = this.isDefault;
        copy.categoryName = this.categoryName;
        return copy;
    }

    public void copyFrom(Item other) {
        this.categoryId = other.categoryId;
        this.name = other.name;
        this.description = other.description;
        this.sizeML = other.sizeML;
        this.isAdaFriendly = other.isAdaFriendly;
        this.isSoda = other.isSoda;
        this.isClearLiquid = other.isClearLiquid;
        this.mealType = other.mealType;
        this.isDefault = other.isDefault;
        this.categoryName = other.categoryName;
    }
}