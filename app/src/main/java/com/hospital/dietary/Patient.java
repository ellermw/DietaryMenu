package com.hospital.dietary.models;

public class Patient {
    private int patientId;
    private String name;
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
    private String createdDate;
    
    // Constructors
    public Patient() {
        this.breakfastComplete = false;
        this.lunchComplete = false;
        this.dinnerComplete = false;
        this.breakfastNPO = false;
        this.lunchNPO = false;
        this.dinnerNPO = false;
    }
    
    public Patient(String name, String wing, String roomNumber, String diet) {
        this();
        this.name = name;
        this.wing = wing;
        this.roomNumber = roomNumber;
        this.diet = diet;
    }
    
    // Getters and Setters
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
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
    
    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
    
    // Utility methods
    public boolean isFullyComplete() {
        return (breakfastComplete || breakfastNPO) && 
               (lunchComplete || lunchNPO) && 
               (dinnerComplete || dinnerNPO);
    }
    
    public int getIncompleteMealCount() {
        int count = 0;
        if (!breakfastComplete && !breakfastNPO) count++;
        if (!lunchComplete && !lunchNPO) count++;
        if (!dinnerComplete && !dinnerNPO) count++;
        return count;
    }
    
    public String getLocationString() {
        return wing + " - Room " + roomNumber;
    }
    
    public String getRestrictionsString() {
        StringBuilder restrictions = new StringBuilder();
        
        if (fluidRestriction != null && !fluidRestriction.equals("None")) {
            restrictions.append("Fluid: ").append(fluidRestriction);
        }
        
        if (textureModifications != null && !textureModifications.isEmpty()) {
            if (restrictions.length() > 0) {
                restrictions.append(" | ");
            }
            restrictions.append("Texture: ").append(textureModifications);
        }
        
        return restrictions.toString();
    }
    
    @Override
    public String toString() {
        return name + " (" + getLocationString() + ")";
    }
}