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

    // Default constructor
    public Patient() {
        this.createdDate = new Date();
        this.breakfastComplete = false;
        this.lunchComplete = false;
        this.dinnerComplete = false;
        this.breakfastNPO = false;
        this.lunchNPO = false;
        this.dinnerNPO = false;
    }

    // Constructor with basic info
    public Patient(String firstName, String lastName, String wing, String roomNumber, String diet) {
        this();
        this.patientFirstName = firstName;
        this.patientLastName = lastName;
        this.wing = wing;
        this.roomNumber = roomNumber;
        this.diet = diet;
    }

    // Primary Getters and Setters
    public int getPatientId() { 
        return patientId; 
    }
    
    public void setPatientId(int patientId) { 
        this.patientId = patientId; 
    }

    public String getPatientFirstName() { 
        return patientFirstName; 
    }
    
    public void setPatientFirstName(String patientFirstName) { 
        this.patientFirstName = patientFirstName; 
    }

    public String getPatientLastName() { 
        return patientLastName; 
    }
    
    public void setPatientLastName(String patientLastName) { 
        this.patientLastName = patientLastName; 
    }

    public String getWing() { 
        return wing; 
    }
    
    public void setWing(String wing) { 
        this.wing = wing; 
    }

    public String getRoomNumber() { 
        return roomNumber; 
    }
    
    public void setRoomNumber(String roomNumber) { 
        this.roomNumber = roomNumber; 
    }

    public String getDiet() { 
        return diet; 
    }
    
    public void setDiet(String diet) { 
        this.diet = diet; 
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

    // Meal completion getters and setters
    public boolean isBreakfastComplete() { 
        return breakfastComplete; 
    }
    
    public void setBreakfastComplete(boolean breakfastComplete) { 
        this.breakfastComplete = breakfastComplete; 
    }

    public boolean isLunchComplete() { 
        return lunchComplete; 
    }
    
    public void setLunchComplete(boolean lunchComplete) { 
        this.lunchComplete = lunchComplete; 
    }

    public boolean isDinnerComplete() { 
        return dinnerComplete; 
    }
    
    public void setDinnerComplete(boolean dinnerComplete) { 
        this.dinnerComplete = dinnerComplete; 
    }

    // NPO status getters and setters
    public boolean isBreakfastNPO() { 
        return breakfastNPO; 
    }
    
    public void setBreakfastNPO(boolean breakfastNPO) { 
        this.breakfastNPO = breakfastNPO; 
    }

    public boolean isLunchNPO() { 
        return lunchNPO; 
    }
    
    public void setLunchNPO(boolean lunchNPO) { 
        this.lunchNPO = lunchNPO; 
    }

    public boolean isDinnerNPO() { 
        return dinnerNPO; 
    }
    
    public void setDinnerNPO(boolean dinnerNPO) { 
        this.dinnerNPO = dinnerNPO; 
    }

    public Date getCreatedDate() { 
        return createdDate; 
    }
    
    public void setCreatedDate(Date createdDate) { 
        this.createdDate = createdDate; 
    }

    // Utility methods
    public String getFullName() {
        if (patientFirstName != null && patientLastName != null) {
            return patientFirstName + " " + patientLastName;
        } else if (patientFirstName != null) {
            return patientFirstName;
        } else if (patientLastName != null) {
            return patientLastName;
        } else {
            return "Unknown Patient";
        }
    }

    public String getLocationString() {
        return wing + " - Room " + roomNumber;
    }

    // FIXED: Get incomplete meal count for pending orders
    public int getIncompleteMealCount() {
        int count = 0;
        if (!breakfastComplete) count++;
        if (!lunchComplete) count++;
        if (!dinnerComplete) count++;
        return count;
    }

    public boolean hasIncompleteMeals() {
        return !breakfastComplete || !lunchComplete || !dinnerComplete;
    }

    public boolean hasNPORestrictions() {
        return breakfastNPO || lunchNPO || dinnerNPO;
    }

    public String getMealCompletionStatus() {
        int completed = 0;
        if (breakfastComplete) completed++;
        if (lunchComplete) completed++;
        if (dinnerComplete) completed++;
        return completed + "/3 meals complete";
    }

    public String getNPOStatus() {
        if (!hasNPORestrictions()) {
            return "No NPO restrictions";
        }
        
        StringBuilder npo = new StringBuilder("NPO: ");
        if (breakfastNPO) npo.append("Breakfast ");
        if (lunchNPO) npo.append("Lunch ");
        if (dinnerNPO) npo.append("Dinner ");
        return npo.toString().trim();
    }

    /**
     * FIXED: Get restrictions string for display (used by FinishedOrdersActivity)
     */
    public String getRestrictionsString() {
        StringBuilder restrictions = new StringBuilder();
        
        if (hasFluidRestrictions()) {
            restrictions.append("Fluid: ").append(fluidRestriction);
        }
        
        if (hasTextureModifications()) {
            if (restrictions.length() > 0) {
                restrictions.append(" | ");
            }
            restrictions.append("Texture: ").append(textureModifications);
        }
        
        if (hasNPORestrictions()) {
            if (restrictions.length() > 0) {
                restrictions.append(" | ");
            }
            restrictions.append(getNPOStatus());
        }
        
        return restrictions.length() > 0 ? restrictions.toString() : "No restrictions";
    }

    // Diet validation
    public boolean isADADiet() {
        return diet != null && diet.toLowerCase().contains("ada");
    }

    public boolean isLiquidDiet() {
        return diet != null && (diet.toLowerCase().contains("liquid") || 
                               diet.toLowerCase().contains("npo"));
    }

    public boolean hasFluidRestrictions() {
        return fluidRestriction != null && !fluidRestriction.isEmpty() && 
               !fluidRestriction.equalsIgnoreCase("none");
    }

    public boolean hasTextureModifications() {
        return textureModifications != null && !textureModifications.isEmpty();
    }

    // Texture modification helpers
    public boolean hasMechanicalGround() {
        return textureModifications != null && 
               textureModifications.toLowerCase().contains("mechanical ground");
    }

    public boolean hasMechanicalChopped() {
        return textureModifications != null && 
               textureModifications.toLowerCase().contains("mechanical chopped");
    }

    public boolean hasBiteSize() {
        return textureModifications != null && 
               textureModifications.toLowerCase().contains("bite size");
    }

    public boolean hasBreadOK() {
        return textureModifications != null && 
               textureModifications.toLowerCase().contains("bread ok");
    }

    // Validation methods
    public boolean isValidPatient() {
        return patientFirstName != null && !patientFirstName.trim().isEmpty() &&
               patientLastName != null && !patientLastName.trim().isEmpty() &&
               wing != null && !wing.trim().isEmpty() &&
               roomNumber != null && !roomNumber.trim().isEmpty() &&
               diet != null && !diet.trim().isEmpty();
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (patientFirstName == null || patientFirstName.trim().isEmpty()) {
            errors.append("First name is required. ");
        }
        
        if (patientLastName == null || patientLastName.trim().isEmpty()) {
            errors.append("Last name is required. ");
        }
        
        if (wing == null || wing.trim().isEmpty()) {
            errors.append("Wing is required. ");
        }
        
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            errors.append("Room number is required. ");
        }
        
        if (diet == null || diet.trim().isEmpty()) {
            errors.append("Diet is required. ");
        }
        
        return errors.toString().trim();
    }

    // Display helpers
    public String getCreatedDateString() {
        return createdDate != null ? dateFormat.format(createdDate) : "Unknown";
    }

    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Name: ").append(getFullName()).append("\n");
        info.append("Location: ").append(getLocationString()).append("\n");
        info.append("Diet: ").append(diet).append("\n");
        
        if (hasFluidRestrictions()) {
            info.append("Fluid Restriction: ").append(fluidRestriction).append("\n");
        }
        
        if (hasTextureModifications()) {
            info.append("Texture Modifications: ").append(textureModifications).append("\n");
        }
        
        info.append("Meal Status: ").append(getMealCompletionStatus()).append("\n");
        
        if (hasNPORestrictions()) {
            info.append(getNPOStatus()).append("\n");
        }
        
        info.append("Created: ").append(getCreatedDateString());
        
        return info.toString();
    }

    // Sorting helpers
    public String getSortKey() {
        return wing + "_" + String.format("%05d", getRoomNumberAsInt());
    }

    private int getRoomNumberAsInt() {
        try {
            // Extract numbers from room number (e.g., "101A" -> 101)
            return Integer.parseInt(roomNumber.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            // If parsing fails, use hashcode for consistent sorting
            return roomNumber.hashCode();
        }
    }

    // Comparison methods for sorting
    public int compareByLocation(Patient other) {
        int wingCompare = this.wing.compareTo(other.wing);
        if (wingCompare != 0) {
            return wingCompare;
        }
        return Integer.compare(this.getRoomNumberAsInt(), other.getRoomNumberAsInt());
    }

    public int compareByName(Patient other) {
        int lastNameCompare = this.patientLastName.compareTo(other.patientLastName);
        if (lastNameCompare != 0) {
            return lastNameCompare;
        }
        return this.patientFirstName.compareTo(other.patientFirstName);
    }

    // Override methods
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

    // Copy methods for editing
    public Patient copy() {
        Patient copy = new Patient();
        copy.patientId = this.patientId;
        copy.patientFirstName = this.patientFirstName;
        copy.patientLastName = this.patientLastName;
        copy.wing = this.wing;
        copy.roomNumber = this.roomNumber;
        copy.diet = this.diet;
        copy.fluidRestriction = this.fluidRestriction;
        copy.textureModifications = this.textureModifications;
        copy.breakfastComplete = this.breakfastComplete;
        copy.lunchComplete = this.lunchComplete;
        copy.dinnerComplete = this.dinnerComplete;
        copy.breakfastNPO = this.breakfastNPO;
        copy.lunchNPO = this.lunchNPO;
        copy.dinnerNPO = this.dinnerNPO;
        copy.createdDate = this.createdDate != null ? new Date(this.createdDate.getTime()) : null;
        return copy;
    }

    public void copyFrom(Patient other) {
        this.patientFirstName = other.patientFirstName;
        this.patientLastName = other.patientLastName;
        this.wing = other.wing;
        this.roomNumber = other.roomNumber;
        this.diet = other.diet;
        this.fluidRestriction = other.fluidRestriction;
        this.textureModifications = other.textureModifications;
        this.breakfastComplete = other.breakfastComplete;
        this.lunchComplete = other.lunchComplete;
        this.dinnerComplete = other.dinnerComplete;
        this.breakfastNPO = other.breakfastNPO;
        this.lunchNPO = other.lunchNPO;
        this.dinnerNPO = other.dinnerNPO;
        this.createdDate = other.createdDate != null ? new Date(other.createdDate.getTime()) : null;
    }
}