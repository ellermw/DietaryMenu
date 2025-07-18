package com.hospital.dietary.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Patient {
    private int patientId;
    private String patientFirstName;
    private String patientLastName;
    private String wing;
    private String roomNumber;
    private String diet;
    private String fluidRestriction;
    private String textureModifications;
    private boolean breakfastComplete;
    private boolean lunchComplete;
    private boolean dinnerComplete;
    private boolean breakfastNPO;
    private boolean lunchNPO;
    private boolean dinnerNPO;
    private Date createdDate;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public Patient() {
        this.createdDate = new Date();
    }

    public Patient(String firstName, String lastName, String wing, String roomNumber, String diet) {
        this.patientFirstName = firstName;
        this.patientLastName = lastName;
        this.wing = wing;
        this.roomNumber = roomNumber;
        this.diet = diet;
        this.createdDate = new Date();
    }

    // Primary Getters and Setters
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientFirstName() { return patientFirstName; }
    public void setPatientFirstName(String patientFirstName) { this.patientFirstName = patientFirstName; }

    public String getPatientLastName() { return patientLastName; }
    public void setPatientLastName(String patientLastName) { this.patientLastName = patientLastName; }

    public String getWing() { return wing; }
    public void setWing(String wing) { this.wing = wing; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getDiet() { return diet; }
    public void setDiet(String diet) { this.diet = diet; }

    public String getFluidRestriction() { return fluidRestriction; }
    public void setFluidRestriction(String fluidRestriction) { this.fluidRestriction = fluidRestriction; }

    public String getTextureModifications() { return textureModifications; }
    public void setTextureModifications(String textureModifications) { this.textureModifications = textureModifications; }

    public boolean isBreakfastComplete() { return breakfastComplete; }
    public void setBreakfastComplete(boolean breakfastComplete) { this.breakfastComplete = breakfastComplete; }

    public boolean isLunchComplete() { return lunchComplete; }
    public void setLunchComplete(boolean lunchComplete) { this.lunchComplete = lunchComplete; }

    public boolean isDinnerComplete() { return dinnerComplete; }
    public void setDinnerComplete(boolean dinnerComplete) { this.dinnerComplete = dinnerComplete; }

    public boolean isBreakfastNPO() { return breakfastNPO; }
    public void setBreakfastNPO(boolean breakfastNPO) { this.breakfastNPO = breakfastNPO; }

    public boolean isLunchNPO() { return lunchNPO; }
    public void setLunchNPO(boolean lunchNPO) { this.lunchNPO = lunchNPO; }

    public boolean isDinnerNPO() { return dinnerNPO; }
    public void setDinnerNPO(boolean dinnerNPO) { this.dinnerNPO = dinnerNPO; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    // ================================================================================================
    // COMPATIBILITY METHODS FOR EXISTING CODE
    // ================================================================================================
    
    /**
     * Returns full name - compatibility for getName()
     */
    public String getName() {
        return getFullName();
    }
    
    /**
     * Returns full name - compatibility for getPatientName()
     */
    public String getPatientName() {
        return getFullName();
    }
    
    /**
     * Returns full name (first + last)
     */
    public String getFullName() { 
        String first = (patientFirstName != null ? patientFirstName : "");
        String last = (patientLastName != null ? patientLastName : "");
        return (first + " " + last).trim();
    }
    
    /**
     * Returns room info - compatibility for getRoomInfo()
     */
    public String getRoomInfo() {
        return getLocationString();
    }
    
    /**
     * Returns location string
     */
    public String getLocationString() {
        return wing + " - Room " + roomNumber;
    }
    
    /**
     * Returns restrictions as string - compatibility for getRestrictionsString()
     */
    public String getRestrictionsString() {
        StringBuilder restrictions = new StringBuilder();
        
        if (fluidRestriction != null && !fluidRestriction.isEmpty()) {
            restrictions.append("Fluid: ").append(fluidRestriction);
        }
        
        if (textureModifications != null && !textureModifications.isEmpty()) {
            if (restrictions.length() > 0) restrictions.append(", ");
            restrictions.append("Texture: ").append(textureModifications);
        }
        
        return restrictions.length() > 0 ? restrictions.toString() : "None";
    }
    
    /**
     * Returns incomplete meal count - compatibility for getIncompleteMealCount()
     */
    public int getIncompleteMealCount() {
        int count = 0;
        if (!breakfastComplete && !breakfastNPO) count++;
        if (!lunchComplete && !lunchNPO) count++;
        if (!dinnerComplete && !dinnerNPO) count++;
        return count;
    }
    
    /**
     * Checks if patient is on ADA diet - compatibility for isADADiet()
     */
    public boolean isADADiet() {
        return diet != null && diet.toLowerCase().contains("ada");
    }

    // ================================================================================================
    // UTILITY METHODS
    // ================================================================================================
    
    /**
     * Checks if patient has incomplete orders
     */
    public boolean hasIncompleteOrder() {
        return (!breakfastComplete && !breakfastNPO) || 
               (!lunchComplete && !lunchNPO) || 
               (!dinnerComplete && !dinnerNPO);
    }

    /**
     * Checks if all orders are complete
     */
    public boolean isAllOrdersComplete() {
        return breakfastComplete && lunchComplete && dinnerComplete;
    }
    
    /**
     * Gets created date as formatted string
     */
    public String getCreatedDateString() {
        if (createdDate != null) {
            return dateFormat.format(createdDate);
        }
        return "";
    }

    @Override
    public String toString() {
        return getFullName() + " (" + getLocationString() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Patient patient = (Patient) obj;
        return patientId == patient.patientId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(patientId);
    }
}