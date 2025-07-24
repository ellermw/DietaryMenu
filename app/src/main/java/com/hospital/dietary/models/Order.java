package com.hospital.dietary.models;

import java.util.Date;

/**
 * Order model class for dietary orders
 */
public class Order {
    private int orderId;
    private long patientId;
    private String patientName;
    private String wing;
    private String room;
    private String diet;
    private String orderTime;
    private String orderDate;
    private String fluidRestriction;
    private String textureModifications;
    private String mealType;
    private String status;
    private Date createdDate;
    private Date completedDate;
    private String items;
    private String specialInstructions;

    // Constructor
    public Order() {
        this.createdDate = new Date();
        this.status = "Pending";
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
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

    public String getDiet() {
        return diet;
    }

    public void setDiet(String diet) {
        this.diet = diet;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getFluidRestriction() {
        return fluidRestriction;
    }

    public void setFluidRestriction(String fluidRestriction) {
        this.fluidRestriction = fluidRestriction;
    }

    public String getTextureModifications() {
        return textureModifications;
    }

    public void setTextureModifications(String textureModifications) {
        this.textureModifications = textureModifications;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    // Helper methods
    public String getLocationInfo() {
        return wing + "-" + room;
    }

    public boolean isPending() {
        return "Pending".equals(status);
    }

    public boolean isCompleted() {
        return "Completed".equals(status);
    }

    public boolean isCancelled() {
        return "Cancelled".equals(status);
    }
}