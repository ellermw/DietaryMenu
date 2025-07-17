// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/models/Order.java
// ================================================================================================

package com.hospital.dietary.models;

import java.util.List;

public class Order {
    private int orderId;
    private String patientName;
    private String wing;
    private String room;
    private String diet;
    private String fluidRestriction;
    private String textureModifications;
    private String orderDate;
    private String orderTime;
    private boolean isFinalized;
    
    // Meal items
    private List<String> breakfastItems;
    private List<String> lunchItems;
    private List<String> dinnerItems;
    
    // Drinks
    private List<String> breakfastDrinks;
    private List<String> lunchDrinks;
    private List<String> dinnerDrinks;

    public Order() {}

    public Order(String patientName, String wing, String room, String diet) {
        this.patientName = patientName;
        this.wing = wing;
        this.room = room;
        this.diet = diet;
        this.isFinalized = false;
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getWing() { return wing; }
    public void setWing(String wing) { this.wing = wing; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getDiet() { return diet; }
    public void setDiet(String diet) { this.diet = diet; }

    public String getFluidRestriction() { return fluidRestriction; }
    public void setFluidRestriction(String fluidRestriction) { this.fluidRestriction = fluidRestriction; }

    public String getTextureModifications() { return textureModifications; }
    public void setTextureModifications(String textureModifications) { this.textureModifications = textureModifications; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getOrderTime() { return orderTime; }
    public void setOrderTime(String orderTime) { this.orderTime = orderTime; }

    public boolean isFinalized() { return isFinalized; }
    public void setFinalized(boolean finalized) { isFinalized = finalized; }

    public List<String> getBreakfastItems() { return breakfastItems; }
    public void setBreakfastItems(List<String> breakfastItems) { this.breakfastItems = breakfastItems; }

    public List<String> getLunchItems() { return lunchItems; }
    public void setLunchItems(List<String> lunchItems) { this.lunchItems = lunchItems; }

    public List<String> getDinnerItems() { return dinnerItems; }
    public void setDinnerItems(List<String> dinnerItems) { this.dinnerItems = dinnerItems; }

    public List<String> getBreakfastDrinks() { return breakfastDrinks; }
    public void setBreakfastDrinks(List<String> breakfastDrinks) { this.breakfastDrinks = breakfastDrinks; }

    public List<String> getLunchDrinks() { return lunchDrinks; }
    public void setLunchDrinks(List<String> lunchDrinks) { this.lunchDrinks = lunchDrinks; }

    public List<String> getDinnerDrinks() { return dinnerDrinks; }
    public void setDinnerDrinks(List<String> dinnerDrinks) { this.dinnerDrinks = dinnerDrinks; }

    // Helper methods
    public String getWingRoom() {
        return wing + " - " + room;
    }
    
    public int getRoomNumber() {
        try {
            return Integer.parseInt(room);
        } catch (NumberFormatException e) {
            return 0; // Default for non-numeric rooms
        }
    }

    @Override
    public String toString() {
        return patientName + " (" + getWingRoom() + ") - " + diet;
    }
}