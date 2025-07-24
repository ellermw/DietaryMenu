package com.hospital.dietary.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Patient {
    // Basic patient information
    private long patientId;
    private String patientFirstName;
    private String patientLastName;
    private String wing;
    private String roomNumber;

    // Diet information
    private String dietType;
    private String diet;
    private boolean adaDiet;
    private String fluidRestriction;
    private String textureModifications;

    // Texture modification flags
    private boolean mechanicalGround;
    private boolean mechanicalChopped;
    private boolean biteSize;
    private boolean breadOK;
    private boolean extraGravy;
    private boolean meatsOnly;

    // Liquid thickness flags
    private boolean nectarThick;
    private boolean honeyThick;
    private boolean puddingThick;

    // Meal completion status
    private boolean breakfastComplete;
    private boolean lunchComplete;
    private boolean dinnerComplete;
    private boolean breakfastNPO;
    private boolean lunchNPO;
    private boolean dinnerNPO;

    // Meal items
    private String breakfastItems;
    private String lunchItems;
    private String dinnerItems;

    // Meal juices
    private String breakfastJuices;
    private String lunchJuices;
    private String dinnerJuices;

    // Meal drinks
    private String breakfastDrinks;
    private String lunchDrinks;
    private String dinnerDrinks;

    // Individual meal diet types
    private String breakfastDiet;
    private String lunchDiet;
    private String dinnerDiet;
    private boolean breakfastAda;
    private boolean lunchAda;
    private boolean dinnerAda;

    // Other fields
    private List<String> mealSelections;
    private Date createdDate;
    private Date orderDate;

    // Constructor
    public Patient() {
        this.breakfastComplete = false;
        this.lunchComplete = false;
        this.dinnerComplete = false;
        this.breakfastNPO = false;
        this.lunchNPO = false;
        this.dinnerNPO = false;
        this.createdDate = new Date();
        this.mealSelections = new ArrayList<>();

        // Initialize individual meal diets to null (will use main diet as fallback)
        this.breakfastDiet = null;
        this.lunchDiet = null;
        this.dinnerDiet = null;
        this.breakfastAda = false;
        this.lunchAda = false;
        this.dinnerAda = false;
    }

    // Getters and Setters
    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
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

    public boolean isNectarThick() {
        return nectarThick;
    }

    public void setNectarThick(boolean nectarThick) {
        this.nectarThick = nectarThick;
    }

    public boolean isHoneyThick() {
        return honeyThick;
    }

    public void setHoneyThick(boolean honeyThick) {
        this.honeyThick = honeyThick;
    }

    public boolean isPuddingThick() {
        return puddingThick;
    }

    public void setPuddingThick(boolean puddingThick) {
        this.puddingThick = puddingThick;
    }

    // Meal completion status
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

    // Meal items
    public String getBreakfastItems() {
        return breakfastItems != null ? breakfastItems : "";
    }

    public void setBreakfastItems(String breakfastItems) {
        this.breakfastItems = breakfastItems;
    }

    public String getLunchItems() {
        return lunchItems != null ? lunchItems : "";
    }

    public void setLunchItems(String lunchItems) {
        this.lunchItems = lunchItems;
    }

    public String getDinnerItems() {
        return dinnerItems != null ? dinnerItems : "";
    }

    public void setDinnerItems(String dinnerItems) {
        this.dinnerItems = dinnerItems;
    }

    // Juices
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

    // Drinks
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

    // Individual meal diets
    public String getBreakfastDiet() {
        return breakfastDiet != null ? breakfastDiet : getDiet();
    }

    public void setBreakfastDiet(String breakfastDiet) {
        this.breakfastDiet = breakfastDiet;
    }

    public String getLunchDiet() {
        return lunchDiet != null ? lunchDiet : getDiet();
    }

    public void setLunchDiet(String lunchDiet) {
        this.lunchDiet = lunchDiet;
    }

    public String getDinnerDiet() {
        return dinnerDiet != null ? dinnerDiet : getDiet();
    }

    public void setDinnerDiet(String dinnerDiet) {
        this.dinnerDiet = dinnerDiet;
    }

    public boolean isBreakfastAda() {
        return breakfastAda;
    }

    public void setBreakfastAda(boolean breakfastAda) {
        this.breakfastAda = breakfastAda;
    }

    public boolean isLunchAda() {
        return lunchAda;
    }

    public void setLunchAda(boolean lunchAda) {
        this.lunchAda = lunchAda;
    }

    public boolean isDinnerAda() {
        return dinnerAda;
    }

    public void setDinnerAda(boolean dinnerAda) {
        this.dinnerAda = dinnerAda;
    }

    // Other getters and setters
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

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    // Helper methods
    public String getFullName() {
        return (patientFirstName != null ? patientFirstName : "") + " " +
                (patientLastName != null ? patientLastName : "");
    }

    public String getLocationInfo() {
        return (wing != null ? wing : "") + " - Room " + (roomNumber != null ? roomNumber : "");
    }

    public boolean hasAnyMealComplete() {
        return breakfastComplete || lunchComplete || dinnerComplete;
    }

    public boolean hasAllMealsComplete() {
        return breakfastComplete && lunchComplete && dinnerComplete;
    }

    public boolean isPendingAnyMeal() {
        return !breakfastComplete || !lunchComplete || !dinnerComplete;
    }

    @Override
    public String toString() {
        return getFullName() + " - " + getLocationInfo() + " (" + getDiet() + ")";
    }
}