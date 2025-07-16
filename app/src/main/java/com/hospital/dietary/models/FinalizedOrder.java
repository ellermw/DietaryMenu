// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/models/FinalizedOrder.java
// ================================================================================================

package com.hospital.dietary.models;

import java.util.ArrayList;
import java.util.List;

public class FinalizedOrder {
    private int orderId;
    private String patientName;
    private String wing;
    private String room;
    private String orderDate;
    private String dietType;
    private String fluidRestriction;
    
    // Texture modifications
    private boolean mechanicalGround;
    private boolean mechanicalChopped;
    private boolean biteSize;
    private boolean breadOK;
    
    // Meal items - storing as lists of strings for simplicity
    private List<String> breakfastItems;
    private List<String> lunchItems;
    private List<String> dinnerItems;
    private List<String> breakfastJuices;
    private List<String> lunchJuices;
    private List<String> dinnerJuices;
    private List<String> breakfastDrinks;
    private List<String> lunchDrinks;
    private List<String> dinnerDrinks;
    
    public FinalizedOrder() {
        this.breakfastItems = new ArrayList<>();
        this.lunchItems = new ArrayList<>();
        this.dinnerItems = new ArrayList<>();
        this.breakfastJuices = new ArrayList<>();
        this.lunchJuices = new ArrayList<>();
        this.dinnerJuices = new ArrayList<>();
        this.breakfastDrinks = new ArrayList<>();
        this.lunchDrinks = new ArrayList<>();
        this.dinnerDrinks = new ArrayList<>();
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
    
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    
    public String getDietType() { return dietType; }
    public void setDietType(String dietType) { this.dietType = dietType; }
    
    public String getFluidRestriction() { return fluidRestriction; }
    public void setFluidRestriction(String fluidRestriction) { this.fluidRestriction = fluidRestriction; }
    
    public boolean isMechanicalGround() { return mechanicalGround; }
    public void setMechanicalGround(boolean mechanicalGround) { this.mechanicalGround = mechanicalGround; }
    
    public boolean isMechanicalChopped() { return mechanicalChopped; }
    public void setMechanicalChopped(boolean mechanicalChopped) { this.mechanicalChopped = mechanicalChopped; }
    
    public boolean isBiteSize() { return biteSize; }
    public void setBiteSize(boolean biteSize) { this.biteSize = biteSize; }
    
    public boolean isBreadOK() { return breadOK; }
    public void setBreadOK(boolean breadOK) { this.breadOK = breadOK; }
    
    public List<String> getBreakfastItems() { return breakfastItems; }
    public void setBreakfastItems(List<String> breakfastItems) { this.breakfastItems = breakfastItems; }
    
    public List<String> getLunchItems() { return lunchItems; }
    public void setLunchItems(List<String> lunchItems) { this.lunchItems = lunchItems; }
    
    public List<String> getDinnerItems() { return dinnerItems; }
    public void setDinnerItems(List<String> dinnerItems) { this.dinnerItems = dinnerItems; }
    
    public List<String> getBreakfastJuices() { return breakfastJuices; }
    public void setBreakfastJuices(List<String> breakfastJuices) { this.breakfastJuices = breakfastJuices; }
    
    public List<String> getLunchJuices() { return lunchJuices; }
    public void setLunchJuices(List<String> lunchJuices) { this.lunchJuices = lunchJuices; }
    
    public List<String> getDinnerJuices() { return dinnerJuices; }
    public void setDinnerJuices(List<String> dinnerJuices) { this.dinnerJuices = dinnerJuices; }
    
    public List<String> getBreakfastDrinks() { return breakfastDrinks; }
    public void setBreakfastDrinks(List<String> breakfastDrinks) { this.breakfastDrinks = breakfastDrinks; }
    
    public List<String> getLunchDrinks() { return lunchDrinks; }
    public void setLunchDrinks(List<String> lunchDrinks) { this.lunchDrinks = lunchDrinks; }
    
    public List<String> getDinnerDrinks() { return dinnerDrinks; }
    public void setDinnerDrinks(List<String> dinnerDrinks) { this.dinnerDrinks = dinnerDrinks; }
    
    // Helper method to get texture modifications as string
    public String getTextureModificationsString() {
        List<String> mods = new ArrayList<>();
        if (mechanicalGround) mods.add("Mechanical Ground");
        if (mechanicalChopped) mods.add("Mechanical Chopped");
        if (biteSize) mods.add("Bite Size");
        if (breadOK) mods.add("Bread OK");
        return mods.isEmpty() ? "None" : String.join(", ", mods);
    }
    
    // Helper method for sorting comparison
    public String getSortKey() {
        return orderDate + "_" + wing + "_" + String.format("%03d", Integer.parseInt(room.replaceAll("\\D", "")));
    }
}