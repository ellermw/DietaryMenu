package com.hospital.dietary.models;

/**
 * PatientOrder model class for patient meal orders display
 */
public class PatientOrder {
    private int patientId;
    private String patientName;
    private String room;
    private String wing;
    private String diet;
    private String timestamp;
    private String fluidRestriction;
    private String textureModifications;
    private String breakfastItems;
    private String lunchItems;
    private String dinnerItems;
    private boolean breakfastComplete;
    private boolean lunchComplete;
    private boolean dinnerComplete;

    // Constructor
    public PatientOrder() {
        this.breakfastComplete = false;
        this.lunchComplete = false;
        this.dinnerComplete = false;
    }

    // Getters and Setters
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getWing() {
        return wing;
    }

    public void setWing(String wing) {
        this.wing = wing;
    }

    public String getDiet() {
        return diet;
    }

    public void setDiet(String diet) {
        this.diet = diet;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getBreakfastItems() {
        return breakfastItems;
    }

    public void setBreakfastItems(String breakfastItems) {
        this.breakfastItems = breakfastItems;
    }

    public String getLunchItems() {
        return lunchItems;
    }

    public void setLunchItems(String lunchItems) {
        this.lunchItems = lunchItems;
    }

    public String getDinnerItems() {
        return dinnerItems;
    }

    public void setDinnerItems(String dinnerItems) {
        this.dinnerItems = dinnerItems;
    }

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

    // Helper methods
    public String getLocationInfo() {
        return wing + "-" + room;
    }

    public boolean hasBreakfastItems() {
        return breakfastItems != null && !breakfastItems.isEmpty();
    }

    public boolean hasLunchItems() {
        return lunchItems != null && !lunchItems.isEmpty();
    }

    public boolean hasDinnerItems() {
        return dinnerItems != null && !dinnerItems.isEmpty();
    }

    public boolean hasFluidRestriction() {
        return fluidRestriction != null && !fluidRestriction.isEmpty() && !"None".equalsIgnoreCase(fluidRestriction);
    }

    public boolean hasTextureModifications() {
        return textureModifications != null && !textureModifications.isEmpty() && !"None".equalsIgnoreCase(textureModifications);
    }

    @Override
    public String toString() {
        return patientName + " (" + getLocationInfo() + ")";
    }
}