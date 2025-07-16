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
    }

    // Getters and setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        // Update bread status when name changes
        this.isBread = isBreadItem(this.name, this.categoryName);
    }

    public Integer getSizeML() { return sizeML; }
    public void setSizeML(Integer sizeML) { this.sizeML = sizeML; }

    public boolean isAdaFriendly() { return isAdaFriendly; }
    public void setAdaFriendly(boolean adaFriendly) { isAdaFriendly = adaFriendly; }

    public boolean isSoda() { return isSoda; }
    public void setSoda(boolean soda) { isSoda = soda; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        // Update bread status when category changes
        this.isBread = isBreadItem(this.name, categoryName);
    }

    public boolean isBread() { return isBread; }
    public void setBread(boolean bread) { isBread = bread; }

    /**
     * Determines if an item should be considered a "bread item" for texture modification filtering.
     */
    private boolean isBreadItem(String name, String category) {
        if (name == null) return false;

        String lowerName = name.toLowerCase();

        // Category-based bread items
        if ("Breads".equals(category) || "Fresh Muffins".equals(category)) {
            return true;
        }

        // Name-based bread items
        return lowerName.contains("toast") || lowerName.contains("french toast") ||
                lowerName.contains("sandwich") || lowerName.contains("biscuit") ||
                lowerName.contains("muffin") || lowerName.contains("bread") ||
                lowerName.contains("hamburger") || lowerName.contains("cheeseburger") ||
                lowerName.contains("grilled cheese") || lowerName.contains("hot ham & cheese") ||
                lowerName.contains("bagel") || lowerName.contains("english muffin") ||
                lowerName.contains("pancake") || lowerName.contains("waffle") ||
                lowerName.contains("croissant") || lowerName.contains("roll") ||
                lowerName.contains("cracker") || lowerName.contains("breadstick");
    }

    @Override
    public String toString() {
        return name + (sizeML != null ? " (" + sizeML + "ml)" : "");
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
}