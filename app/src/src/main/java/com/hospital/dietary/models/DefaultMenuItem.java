package com.hospital.dietary.models;

public class DefaultMenuItem {

    private int id;
    private int itemId;
    private String itemName;
    private String dietType;
    private String mealType;
    private String dayOfWeek;
    private String description;
    private String createdDate;

    // Constructors
    public DefaultMenuItem() {
    }

    public DefaultMenuItem(String itemName, String dietType, String mealType, String dayOfWeek) {
        this.itemName = itemName;
        this.dietType = dietType;
        this.mealType = mealType;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName != null ? itemName : "";
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDietType() {
        return dietType != null ? dietType : "";
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }

    public String getMealType() {
        return mealType != null ? mealType : "";
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getDayOfWeek() {
        return dayOfWeek != null ? dayOfWeek : "";
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedDate() {
        return createdDate != null ? createdDate : "";
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    // Helper Methods
    public String getDisplayName() {
        return getItemName();
    }

    public String getFullDescription() {
        StringBuilder desc = new StringBuilder(getItemName());

        if (!getDietType().isEmpty()) {
            desc.append(" (").append(getDietType()).append(")");
        }

        if (!getDescription().isEmpty()) {
            desc.append(" - ").append(getDescription());
        }

        return desc.toString();
    }

    public String getConfigurationKey() {
        return getDietType() + "_" + getMealType() + "_" + getDayOfWeek();
    }

    @Override
    public String toString() {
        return getItemName() + " - " + getDietType() + " " + getMealType() + " " + getDayOfWeek();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DefaultMenuItem that = (DefaultMenuItem) obj;

        if (!getItemName().equals(that.getItemName())) return false;
        if (!getDietType().equals(that.getDietType())) return false;
        if (!getMealType().equals(that.getMealType())) return false;
        return getDayOfWeek().equals(that.getDayOfWeek());
    }

    @Override
    public int hashCode() {
        int result = getItemName().hashCode();
        result = 31 * result + getDietType().hashCode();
        result = 31 * result + getMealType().hashCode();
        result = 31 * result + getDayOfWeek().hashCode();
        return result;
    }
}