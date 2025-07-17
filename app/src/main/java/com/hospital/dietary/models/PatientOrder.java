// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/models/PatientOrder.java
// ================================================================================================

package com.hospital.dietary.models;

public class PatientOrder {
    private int patientId;
    private String patientName;
    private String wing;
    private String room;
    private String diet;
    private String fluidRestriction;
    private String textureModifications;
    private String timestamp;
    private String breakfastItems;
    private String lunchItems;
    private String dinnerItems;

    public PatientOrder() {}

    public PatientOrder(int patientId, String patientName, String wing, String room, String diet) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.wing = wing;
        this.room = room;
        this.diet = diet;
    }

    // Getters and Setters
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

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

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getBreakfastItems() { return breakfastItems != null ? breakfastItems : ""; }
    public void setBreakfastItems(String breakfastItems) { this.breakfastItems = breakfastItems; }

    public String getLunchItems() { return lunchItems != null ? lunchItems : ""; }
    public void setLunchItems(String lunchItems) { this.lunchItems = lunchItems; }

    public String getDinnerItems() { return dinnerItems != null ? dinnerItems : ""; }
    public void setDinnerItems(String dinnerItems) { this.dinnerItems = dinnerItems; }

    @Override
    public String toString() {
        return patientName + " - " + wing + " Room " + room;
    }

    /**
     * Get a summary of all restrictions for display
     */
    public String getRestrictionsDisplay() {
        StringBuilder restrictions = new StringBuilder();
        
        if (fluidRestriction != null && !fluidRestriction.trim().isEmpty()) {
            restrictions.append("Fluid: ").append(fluidRestriction);
        }
        
        if (textureModifications != null && !textureModifications.trim().isEmpty()) {
            if (restrictions.length() > 0) {
                restrictions.append(" | ");
            }
            restrictions.append("Texture: ").append(textureModifications);
        }
        
        return restrictions.toString();
    }

    /**
     * Check if this order has any items for any meal
     */
    public boolean hasAnyMealItems() {
        return (breakfastItems != null && !breakfastItems.trim().isEmpty()) ||
               (lunchItems != null && !lunchItems.trim().isEmpty()) ||
               (dinnerItems != null && !dinnerItems.trim().isEmpty());
    }

    /**
     * Get total number of different meal items
     */
    public int getTotalItemCount() {
        int count = 0;
        
        if (breakfastItems != null && !breakfastItems.trim().isEmpty()) {
            count += breakfastItems.split("\n").length;
        }
        
        if (lunchItems != null && !lunchItems.trim().isEmpty()) {
            count += lunchItems.split("\n").length;
        }
        
        if (dinnerItems != null && !dinnerItems.trim().isEmpty()) {
            count += dinnerItems.split("\n").length;
        }
        
        return count;
    }
}