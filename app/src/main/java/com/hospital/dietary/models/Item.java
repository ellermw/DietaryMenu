// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/models/Item.java
// ================================================================================================

package com.hospital.dietary.models;

public class Item {
    private int itemId;
    private int categoryId;
    private String name;
    private Integer sizeML;
    private boolean isAdaFriendly;
    private boolean isSoda;
    private String categoryName;
    private boolean isBread;

    public Item() {}

    public Item(int itemId, int categoryId, String name, Integer sizeML, 
               boolean isAdaFriendly, boolean isSoda) {
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.name = name;
        this.sizeML = sizeML;
        this.isAdaFriendly = isAdaFriendly;
        this.isSoda = isSoda;
        this.isBread = isBreadItem(name, categoryName);
    }

    // Getters and setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSizeML() { return sizeML; }
    public void setSizeML(Integer sizeML) { this.sizeML = sizeML; }

    public boolean isAdaFriendly() { return isAdaFriendly; }
    public void setAdaFriendly(boolean adaFriendly) { isAdaFriendly = adaFriendly; }

    public boolean isSoda() { return isSoda; }
    public void setSoda(boolean soda) { isSoda = soda; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { 
        this.categoryName = categoryName;
        this.isBread = isBreadItem(this.name, categoryName);
    }

    public boolean isBread() { return isBread; }
    public void setBread(boolean bread) { isBread = bread; }

    private boolean isBreadItem(String name, String category) {
        if (name == null) return false;
        
        // Items that are considered bread items for texture modification filtering
        return name.contains("Toast") || name.contains("French Toast") || 
               name.contains("Sandwich") || name.contains("Biscuit") ||
               name.contains("Muffin") || "Breads".equals(category) ||
               "Fresh Muffins".equals(category) || name.contains("Bread") ||
               name.contains("Hamburger") || name.contains("Cheeseburger") ||
               name.contains("Grilled Cheese") || name.contains("Hot Ham & Cheese");
    }

    @Override
    public String toString() {
        return name + (sizeML != null ? " (" + sizeML + "ml)" : "");
    }
}
