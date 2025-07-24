package com.hospital.dietary.models;

import java.util.List;

/**
 * FinalizedOrder model class for backward compatibility
 */
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

    // Meal items
    private List<String> breakfastItems;
    private List<String> lunchItems;
    private List<String> dinnerItems;

    // Juices
    private List<String> breakfastJuices;
    private List<String> lunchJuices;
    private List<String> dinnerJuices;

    // Drinks
    private List<String> breakfastDrinks;
    private List<String> lunchDrinks;
    private List<String> dinnerDrinks;

    // Constructor
    public FinalizedOrder() {}

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getWing() {
        return wing;
    }

    public void setWing(String wing) {
        this.wing = wing;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }

    public String getFluidRestriction() {
        return fluidRestriction;
    }

    public void setFluidRestriction(String fluidRestriction) {
        this.fluidRestriction = fluidRestriction;
    }

    public boolean isMechanicalGround() {
        return mechanicalGround;
    }

    public void setMechanicalGround(boolean mechanicalGround) {
        this.mechanicalGround = mechanicalGround;
    }

    public boolean isMechanicalChopped() {
        return mechanicalChopped;
    }

    public void setMechanicalChopped(boolean mechanicalChopped) {
        this.mechanicalChopped = mechanicalChopped;
    }

    public boolean isBiteSize() {
        return biteSize;
    }

    public void setBiteSize(boolean biteSize) {
        this.biteSize = biteSize;
    }

    public boolean isBreadOK() {
        return breadOK;
    }

    public void setBreadOK(boolean breadOK) {
        this.breadOK = breadOK;
    }

    public List<String> getBreakfastItems() {
        return breakfastItems;
    }

    public void setBreakfastItems(List<String> breakfastItems) {
        this.breakfastItems = breakfastItems;
    }

    public List<String> getLunchItems() {
        return lunchItems;
    }

    public void setLunchItems(List<String> lunchItems) {
        this.lunchItems = lunchItems;
    }

    public List<String> getDinnerItems() {
        return dinnerItems;
    }

    public void setDinnerItems(List<String> dinnerItems) {
        this.dinnerItems = dinnerItems;
    }

    public List<String> getBreakfastJuices() {
        return breakfastJuices;
    }

    public void setBreakfastJuices(List<String> breakfastJuices) {
        this.breakfastJuices = breakfastJuices;
    }

    public List<String> getLunchJuices() {
        return lunchJuices;
    }

    public void setLunchJuices(List<String> lunchJuices) {
        this.lunchJuices = lunchJuices;
    }

    public List<String> getDinnerJuices() {
        return dinnerJuices;
    }

    public void setDinnerJuices(List<String> dinnerJuices) {
        this.dinnerJuices = dinnerJuices;
    }

    public List<String> getBreakfastDrinks() {
        return breakfastDrinks;
    }

    public void setBreakfastDrinks(List<String> breakfastDrinks) {
        this.breakfastDrinks = breakfastDrinks;
    }

    public List<String> getLunchDrinks() {
        return lunchDrinks;
    }

    public void setLunchDrinks(List<String> lunchDrinks) {
        this.lunchDrinks = lunchDrinks;
    }

    public List<String> getDinnerDrinks() {
        return dinnerDrinks;
    }

    public void setDinnerDrinks(List<String> dinnerDrinks) {
        this.dinnerDrinks = dinnerDrinks;
    }
}