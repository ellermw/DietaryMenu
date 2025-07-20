package com.hospital.dietary.models;

import java.util.Date;

public class Patient {
    private int patientId;
    private String patientFirstName;
    private String patientLastName;
    private String wing;
    private String roomNumber;
    private String dietType;
    private boolean adaDiet;
    private String fluidRestriction;

    // FIXED: Texture modification fields
    private boolean mechanicalChopped;
    private boolean mechanicalGround;
    private boolean biteSize;
    private boolean breadOK;

    private boolean breakfastComplete;
    private boolean lunchComplete;
    private boolean dinnerComplete;
    private Date createdDate;

    // Default constructor
    public Patient() {
        this.adaDiet = false;
        this.mechanicalChopped = false;
        this.mechanicalGround = false;
        this.biteSize = false;
        this.breadOK = false;
        this.breakfastComplete = false;
        this.lunchComplete = false;
        this.dinnerComplete = false;
        this.createdDate = new Date();
    }

    // Full constructor
    public Patient(int patientId, String patientFirstName, String patientLastName,
                   String wing, String roomNumber, String dietType, boolean adaDiet,
                   String fluidRestriction, boolean mechanicalChopped, boolean mechanicalGround,
                   boolean biteSize, boolean breadOK, boolean breakfastComplete,
                   boolean lunchComplete, boolean dinnerComplete) {
        this.patientId = patientId;
        this.patientFirstName = patientFirstName;
        this.patientLastName = patientLastName;
        this.wing = wing;
        this.roomNumber = roomNumber;
        this.dietType = dietType;
        this.adaDiet = adaDiet;
        this.fluidRestriction = fluidRestriction;
        this.mechanicalChopped = mechanicalChopped;
        this.mechanicalGround = mechanicalGround;
        this.biteSize = biteSize;
        this.breadOK = breadOK;
        this.breakfastComplete = breakfastComplete;
        this.lunchComplete = lunchComplete;
        this.dinnerComplete = dinnerComplete;
        this.createdDate = new Date();
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

    // FIXED: Diet type methods
    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }

    // FIXED: ADA diet methods
    public boolean isAdaDiet() {
        return adaDiet;
    }

    public void setAdaDiet(boolean adaDiet) {
        this.adaDiet = adaDiet;
    }

    public String getFluidRestriction() {
        return fluidRestriction;
    }

    public void setFluidRestriction(String fluidRestriction) {
        this.fluidRestriction = fluidRestriction;
    }

    // FIXED: Texture modification methods
    public boolean isMechanicalChopped() {
        return mechanicalChopped;
    }

    public void setMechanicalChopped(boolean mechanicalChopped) {
        this.mechanicalChopped = mechanicalChopped;
    }

    public boolean isMechanicalGround() {
        return mechanicalGround;
    }

    public void setMechanicalGround(boolean mechanicalGround) {
        this.mechanicalGround = mechanicalGround;
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

    // Meal completion methods
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Helper methods
    public String getFullName() {
        return patientFirstName + " " + patientLastName;
    }

    public String getTextureModifications() {
        StringBuilder modifications = new StringBuilder();
        if (mechanicalChopped) modifications.append("Mechanical Chopped, ");
        if (mechanicalGround) modifications.append("Mechanical Ground, ");
        if (biteSize) modifications.append("Bite Size, ");
        if (breadOK) modifications.append("Bread OK, ");

        if (modifications.length() > 0) {
            // Remove trailing comma and space
            modifications.setLength(modifications.length() - 2);
            return modifications.toString();
        } else {
            return "Regular";
        }
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", name='" + patientFirstName + " " + patientLastName + '\'' +
                ", wing='" + wing + '\'' +
                ", room='" + roomNumber + '\'' +
                ", diet='" + dietType + '\'' +
                ", ada=" + adaDiet +
                '}';
    }
}