package com.hospital.dietary.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

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

    // FIXED: Enhanced meal selection support for full editing
    private List<String> mealSelections;
    private List<String> breakfastSelections;
    private List<String> lunchSelections;
    private List<String> dinnerSelections;

    // FIXED: Additional fields for comprehensive patient management
    private String allergies;
    private String specialInstructions;
    private boolean isAdaFriendly;
    private Date lastModified;

    // Default constructor
    public Patient() {
        this.mealSelections = new ArrayList<>();
        this.breakfastSelections = new ArrayList<>();
        this.lunchSelections = new ArrayList<>();
        this.dinnerSelections = new ArrayList<>();
        this.createdDate = new Date();
        this.lastModified = new Date();
    }

    // Constructor with basic info
    public Patient(String patientFirstName, String patientLastName, String wing, String roomNumber) {
        this();
        this.patientFirstName = patientFirstName;
        this.patientLastName = patientLastName;
        this.wing = wing;
        this.roomNumber = roomNumber;
    }

    // Getters and Setters
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
        updateLastModified();
    }

    public String getPatientLastName() {
        return patientLastName;
    }

    public void setPatientLastName(String patientLastName) {
        this.patientLastName = patientLastName;
        updateLastModified();
    }

    public String getWing() {
        return wing;
    }

    public void setWing(String wing) {
        this.wing = wing;
        updateLastModified();
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
        updateLastModified();
    }

    public String getDiet() {
        return diet;
    }

    public void setDiet(String diet) {
        this.diet = diet;

        // FIXED: Auto-update ADA friendly flag based on diet
        this.isAdaFriendly = (diet != null && diet.contains("ADA"));
        updateLastModified();
    }

    public String getFluidRestriction() {
        return fluidRestriction;
    }

    public void setFluidRestriction(String fluidRestriction) {
        this.fluidRestriction = fluidRestriction;
        updateLastModified();
    }

    public String getTextureModifications() {
        return textureModifications;
    }

    public void setTextureModifications(String textureModifications) {
        this.textureModifications = textureModifications;
        updateLastModified();
    }

    public boolean isBreakfastComplete() {
        return breakfastComplete;
    }

    public void setBreakfastComplete(boolean breakfastComplete) {
        this.breakfastComplete = breakfastComplete;
        updateLastModified();
    }

    public boolean isLunchComplete() {
        return lunchComplete;
    }

    public void setLunchComplete(boolean lunchComplete) {
        this.lunchComplete = lunchComplete;
        updateLastModified();
    }

    public boolean isDinnerComplete() {
        return dinnerComplete;
    }

    public void setDinnerComplete(boolean dinnerComplete) {
        this.dinnerComplete = dinnerComplete;
        updateLastModified();
    }

    public boolean isBreakfastNPO() {
        return breakfastNPO;
    }

    public void setBreakfastNPO(boolean breakfastNPO) {
        this.breakfastNPO = breakfastNPO;
        updateLastModified();
    }

    public boolean isLunchNPO() {
        return lunchNPO;
    }

    public void setLunchNPO(boolean lunchNPO) {
        this.lunchNPO = lunchNPO;
        updateLastModified();
    }

    public boolean isDinnerNPO() {
        return dinnerNPO;
    }

    public void setDinnerNPO(boolean dinnerNPO) {
        this.dinnerNPO = dinnerNPO;
        updateLastModified();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // FIXED: Enhanced meal selection methods
    public List<String> getMealSelections() {
        return mealSelections;
    }

    public void setMealSelections(List<String> mealSelections) {
        this.mealSelections = mealSelections != null ? mealSelections : new ArrayList<>();
        updateLastModified();
    }

    public void addMealSelection(String selection) {
        if (this.mealSelections == null) {
            this.mealSelections = new ArrayList<>();
        }
        this.mealSelections.add(selection);
        updateLastModified();
    }

    public void removeMealSelection(String selection) {
        if (this.mealSelections != null) {
            this.mealSelections.remove(selection);
            updateLastModified();
        }
    }

    public List<String> getBreakfastSelections() {
        return breakfastSelections;
    }

    public void setBreakfastSelections(List<String> breakfastSelections) {
        this.breakfastSelections = breakfastSelections != null ? breakfastSelections : new ArrayList<>();
        updateLastModified();
    }

    public List<String> getLunchSelections() {
        return lunchSelections;
    }

    public void setLunchSelections(List<String> lunchSelections) {
        this.lunchSelections = lunchSelections != null ? lunchSelections : new ArrayList<>();
        updateLastModified();
    }

    public List<String> getDinnerSelections() {
        return dinnerSelections;
    }

    public void setDinnerSelections(List<String> dinnerSelections) {
        this.dinnerSelections = dinnerSelections != null ? dinnerSelections : new ArrayList<>();
        updateLastModified();
    }

    // FIXED: New enhanced fields
    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
        updateLastModified();
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
        updateLastModified();
    }

    public boolean isAdaFriendly() {
        return isAdaFriendly;
    }

    public void setAdaFriendly(boolean adaFriendly) {
        this.isAdaFriendly = adaFriendly;
        updateLastModified();
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    private void updateLastModified() {
        this.lastModified = new Date();
    }

    // FIXED: Utility methods
    public String getFullName() {
        return patientFirstName + " " + patientLastName;
    }

    public String getLocationInfo() {
        return wing + " - Room " + roomNumber;
    }

    public String getCompletionStatus() {
        int completed = 0;
        if (breakfastComplete) completed++;
        if (lunchComplete) completed++;
        if (dinnerComplete) completed++;
        return completed + "/3 meals complete";
    }

    /**
     * FIXED: Alias method for compatibility
     */
    public String getMealCompletionStatus() {
        return getCompletionStatus();
    }

    public boolean isAllMealsComplete() {
        return breakfastComplete && lunchComplete && dinnerComplete;
    }

    public boolean hasAnyMealComplete() {
        return breakfastComplete || lunchComplete || dinnerComplete;
    }

    public int getCompletedMealCount() {
        int count = 0;
        if (breakfastComplete) count++;
        if (lunchComplete) count++;
        if (dinnerComplete) count++;
        return count;
    }

    public int getPendingMealCount() {
        return 3 - getCompletedMealCount();
    }

    // FIXED: Clear liquid diet helper methods
    public boolean isClearLiquidDiet() {
        return diet != null && diet.toLowerCase().contains("clear liquid");
    }

    public boolean isAdaClearLiquidDiet() {
        return isClearLiquidDiet() && (diet.contains("ADA") || isAdaFriendly);
    }

    // FIXED: Validation methods
    public boolean isValid() {
        return patientFirstName != null && !patientFirstName.trim().isEmpty() &&
                patientLastName != null && !patientLastName.trim().isEmpty() &&
                wing != null && !wing.trim().isEmpty() &&
                roomNumber != null && !roomNumber.trim().isEmpty();
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

        return errors.toString().trim();
    }

    // FIXED: Enhanced toString for debugging and display
    @Override
    public String toString() {
        return getFullName() + " (" + getLocationInfo() + ") - " +
                (diet != null ? diet : "No diet specified") + " - " +
                getCompletionStatus();
    }

    // FIXED: Copy constructor for editing support
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
        copy.createdDate = this.createdDate;
        copy.allergies = this.allergies;
        copy.specialInstructions = this.specialInstructions;
        copy.isAdaFriendly = this.isAdaFriendly;
        copy.lastModified = this.lastModified;

        // Deep copy lists
        copy.mealSelections = new ArrayList<>(this.mealSelections);
        copy.breakfastSelections = new ArrayList<>(this.breakfastSelections);
        copy.lunchSelections = new ArrayList<>(this.lunchSelections);
        copy.dinnerSelections = new ArrayList<>(this.dinnerSelections);

        return copy;
    }

    // FIXED: Equals method for comparison
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