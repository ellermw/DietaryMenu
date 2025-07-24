package com.hospital.dietary.models;

import java.util.Date;

/**
 * Patient model class with all required fields and methods
 */
public class Patient {
    private long patientId;
    private String patientFirstName;
    private String patientLastName;
    private String wing;
    private String roomNumber;
    private String dietType;
    private String diet;
    private boolean adaDiet;
    private String fluidRestriction;
    private String textureModifications;
    private boolean mechanicalChopped;
    private boolean mechanicalGround;
    private boolean biteSize;
    private boolean breadOK;
    private boolean nectarThick;
    private boolean puddingThick;
    private boolean honeyThick;
    private boolean extraGravy;
    private boolean meatsOnly;
    private boolean isPuree;
    private String allergies;
    private String likes;
    private String dislikes;
    private String comments;
    private String preferredDrink;
    private String drinkVariety;
    private boolean breakfastComplete;
    private boolean lunchComplete;
    private boolean dinnerComplete;
    private boolean breakfastNPO;
    private boolean lunchNPO;
    private boolean dinnerNPO;
    private String breakfastItems;
    private String lunchItems;
    private String dinnerItems;
    private String breakfastJuices;
    private String lunchJuices;
    private String dinnerJuices;
    private String breakfastDrinks;
    private String lunchDrinks;
    private String dinnerDrinks;
    private String breakfastDiet;
    private String lunchDiet;
    private String dinnerDiet;
    private boolean breakfastAda;
    private boolean lunchAda;
    private boolean dinnerAda;
    private Date createdDate;

    // Constructor
    public Patient() {
        this.createdDate = new Date();
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
        this.breadOK = true;
        this.nectarThick = false;
        this.puddingThick = false;
        this.honeyThick = false;
        this.extraGravy = false;
        this.meatsOnly = false;
        this.isPuree = false;
    }

    // Getters and Setters
    public long getPatientId() {
        return patientId;
    }

    public void setPatientId(long patientId) {
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
    }

    public String getDiet() {
        return diet;
    }

    public void setDiet(String diet) {
        this.diet = diet;
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

    public String getTextureModifications() {
        return textureModifications;
    }

    public void setTextureModifications(String textureModifications) {
        this.textureModifications = textureModifications;
    }

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

    public boolean isPuree() {
        return isPuree;
    }

    public void setPuree(boolean puree) {
        isPuree = puree;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getDislikes() {
        return dislikes;
    }

    public void setDislikes(String dislikes) {
        this.dislikes = dislikes;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getPreferredDrink() {
        return preferredDrink;
    }

    public void setPreferredDrink(String preferredDrink) {
        this.preferredDrink = preferredDrink;
    }

    public String getDrinkVariety() {
        return drinkVariety;
    }

    public void setDrinkVariety(String drinkVariety) {
        this.drinkVariety = drinkVariety;
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

    public String getBreakfastJuices() {
        return breakfastJuices;
    }

    public void setBreakfastJuices(String breakfastJuices) {
        this.breakfastJuices = breakfastJuices;
    }

    public String getLunchJuices() {
        return lunchJuices;
    }

    public void setLunchJuices(String lunchJuices) {
        this.lunchJuices = lunchJuices;
    }

    public String getDinnerJuices() {
        return dinnerJuices;
    }

    public void setDinnerJuices(String dinnerJuices) {
        this.dinnerJuices = dinnerJuices;
    }

    public String getBreakfastDrinks() {
        return breakfastDrinks;
    }

    public void setBreakfastDrinks(String breakfastDrinks) {
        this.breakfastDrinks = breakfastDrinks;
    }

    public String getLunchDrinks() {
        return lunchDrinks;
    }

    public void setLunchDrinks(String lunchDrinks) {
        this.lunchDrinks = lunchDrinks;
    }

    public String getDinnerDrinks() {
        return dinnerDrinks;
    }

    public void setDinnerDrinks(String dinnerDrinks) {
        this.dinnerDrinks = dinnerDrinks;
    }

    public String getBreakfastDiet() {
        return breakfastDiet;
    }

    public void setBreakfastDiet(String breakfastDiet) {
        this.breakfastDiet = breakfastDiet;
    }

    public String getLunchDiet() {
        return lunchDiet;
    }

    public void setLunchDiet(String lunchDiet) {
        this.lunchDiet = lunchDiet;
    }

    public String getDinnerDiet() {
        return dinnerDiet;
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

    public String getLocationInfo() {
        return wing + "-" + roomNumber;
    }
}