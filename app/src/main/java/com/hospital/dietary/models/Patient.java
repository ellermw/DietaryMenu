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
    private String dietType;
    private String diet;
    private boolean adaDiet;
    private String fluidRestriction;
    private String textureModifications;

    // Texture modification flags - existing
    private boolean mechanicalChopped;
    private boolean mechanicalGround;
    private boolean biteSize;
    private boolean breadOK;

    // FIXED: New texture modification options
    private boolean nectarThick;
    private boolean puddingThick;
    private boolean honeyThick;
    private boolean extraGravy;
    private boolean meatsOnly; // Shows only when Mechanical Ground or Chopped is selected

    // NPO (Nothing by mouth) flags
    private boolean breakfastNPO;
    private boolean lunchNPO;
    private boolean dinnerNPO;

    // Meal completion flags
    private boolean breakfastComplete;
    private boolean lunchComplete;
    private boolean dinnerComplete;

    // Meal items
    private String breakfastItems;
    private String lunchItems;
    private String dinnerItems;

    // Drink items
    private String breakfastJuices;
    private String lunchJuices;
    private String dinnerJuices;
    private String breakfastDrinks;
    private String lunchDrinks;
    private String dinnerDrinks;

    // Metadata
    private Date createdDate;
    private List<String> mealSelections;

    // Constructor
    public Patient() {
        this.createdDate = new Date();
        this.mealSelections = new ArrayList<>();
        // Set default values
        this.fluidRestriction = "No Restriction";
        this.textureModifications = "Regular";
        this.breakfastComplete = false;
        this.lunchComplete = false;
        this.dinnerComplete = false;
        this.breakfastNPO = false;
        this.lunchNPO = false;
        this.dinnerNPO = false;
        this.adaDiet = false;
        this.mechanicalChopped = false;
        this.mechanicalGround = false;
        this.biteSize = false;
        this.breadOK = true; // Default to bread being OK
        // Initialize new fields
        this.nectarThick = false;
        this.puddingThick = false;
        this.honeyThick = false;
        this.extraGravy = false;
        this.meatsOnly = false;
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

    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
        this.diet = dietType; // Keep both in sync
    }

    public String getDiet() {
        return diet != null ? diet : dietType;
    }

    public void setDiet(String diet) {
        this.diet = diet;
        this.dietType = diet; // Keep both in sync
    }

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

    // Texture modifications
    public String getTextureModifications() {
        return textureModifications;
    }

    public void setTextureModifications(String textureModifications) {
        this.textureModifications = textureModifications;
    }

    // NPO (Nothing by mouth) methods
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

    // Existing texture modification methods
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

    // FIXED: New texture modification getters and setters
    public boolean isNectarThick() {
        return nectarThick;
    }

    public void setNectarThick(boolean nectarThick) {
        this.nectarThick = nectarThick;
    }

    public boolean isPuddingThick() {
        return puddingThick;
    }

    public void setPuddingThick(boolean puddingThick) {
        this.puddingThick = puddingThick;
    }

    public boolean isHoneyThick() {
        return honeyThick;
    }

    public void setHoneyThick(boolean honeyThick) {
        this.honeyThick = honeyThick;
    }

    public boolean isExtraGravy() {
        return extraGravy;
    }

    public void setExtraGravy(boolean extraGravy) {
        this.extraGravy = extraGravy;
    }

    public boolean isMeatsOnly() {
        return meatsOnly;
    }

    public void setMeatsOnly(boolean meatsOnly) {
        this.meatsOnly = meatsOnly;
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

    // Breakfast Items
    public String getBreakfastItems() {
        return breakfastItems != null ? breakfastItems : "";
    }

    public void setBreakfastItems(String breakfastItems) {
        this.breakfastItems = breakfastItems;
    }

    // Lunch Items
    public String getLunchItems() {
        return lunchItems != null ? lunchItems : "";
    }

    public void setLunchItems(String lunchItems) {
        this.lunchItems = lunchItems;
    }

    // Dinner Items
    public String getDinnerItems() {
        return dinnerItems != null ? dinnerItems : "";
    }

    public void setDinnerItems(String dinnerItems) {
        this.dinnerItems = dinnerItems;
    }

    // Juice getters and setters
    public String getBreakfastJuices() {
        return breakfastJuices != null ? breakfastJuices : "";
    }

    public void setBreakfastJuices(String breakfastJuices) {
        this.breakfastJuices = breakfastJuices;
    }

    public String getLunchJuices() {
        return lunchJuices != null ? lunchJuices : "";
    }

    public void setLunchJuices(String lunchJuices) {
        this.lunchJuices = lunchJuices;
    }

    public String getDinnerJuices() {
        return dinnerJuices != null ? dinnerJuices : "";
    }

    public void setDinnerJuices(String dinnerJuices) {
        this.dinnerJuices = dinnerJuices;
    }

    // Drink getters and setters
    public String getBreakfastDrinks() {
        return breakfastDrinks != null ? breakfastDrinks : "";
    }

    public void setBreakfastDrinks(String breakfastDrinks) {
        this.breakfastDrinks = breakfastDrinks;
    }

    public String getLunchDrinks() {
        return lunchDrinks != null ? lunchDrinks : "";
    }

    public void setLunchDrinks(String lunchDrinks) {
        this.lunchDrinks = lunchDrinks;
    }

    public String getDinnerDrinks() {
        return dinnerDrinks != null ? dinnerDrinks : "";
    }

    public void setDinnerDrinks(String dinnerDrinks) {
        this.dinnerDrinks = dinnerDrinks;
    }

    public List<String> getMealSelections() {
        return mealSelections != null ? mealSelections : new ArrayList<>();
    }

    public void setMealSelections(List<String> mealSelections) {
        this.mealSelections = mealSelections != null ? mealSelections : new ArrayList<>();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Helper methods
    public String getFullName() {
        return (patientFirstName != null ? patientFirstName : "") + " " +
                (patientLastName != null ? patientLastName : "");
    }

    public String getLocationInfo() {
        return (wing != null ? wing : "") + " - " + (roomNumber != null ? roomNumber : "");
    }

    public String getMealCompletionStatus() {
        int completed = 0;
        int total = 3;

        if (breakfastComplete || breakfastNPO) completed++;
        if (lunchComplete || lunchNPO) completed++;
        if (dinnerComplete || dinnerNPO) completed++;

        if (completed == total) {
            return "All Meals Complete";
        } else if (completed == 0) {
            return "No Meals Complete";
        } else {
            return completed + "/" + total + " Meals Complete";
        }
    }

    // FIXED: Updated utility methods to check if any texture modifications are applied
    public boolean hasTextureModifications() {
        return mechanicalChopped || mechanicalGround || biteSize || !breadOK ||
                nectarThick || puddingThick || honeyThick || extraGravy;
    }

    public String getTextureModificationsDescription() {
        List<String> modifications = new ArrayList<>();
        if (mechanicalChopped) modifications.add("Mechanical Chopped");
        if (mechanicalGround) modifications.add("Mechanical Ground");
        if (biteSize) modifications.add("Bite Size");
        if (!breadOK) modifications.add("No Bread");
        if (nectarThick) modifications.add("Nectar Thick");
        if (puddingThick) modifications.add("Pudding Thick");
        if (honeyThick) modifications.add("Honey Thick");
        if (extraGravy) modifications.add("Extra Gravy");
        if (meatsOnly && (mechanicalChopped || mechanicalGround)) modifications.add("Meats Only");

        return modifications.isEmpty() ? "None" : String.join(", ", modifications);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", fullName='" + getFullName() + '\'' +
                ", location='" + getLocationInfo() + '\'' +
                ", diet='" + getDiet() + '\'' +
                ", adaDiet=" + adaDiet +
                ", fluidRestriction='" + fluidRestriction + '\'' +
                ", textureModifications='" + getTextureModificationsDescription() + '\'' +
                ", mealStatus='" + getMealCompletionStatus() + '\'' +
                '}';
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
        return patientId;
    }
}