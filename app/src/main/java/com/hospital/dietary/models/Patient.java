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

    // Individual meal components
    private String breakfastMain;
    private String breakfastSide;
    private String breakfastDrink;
    private String lunchMain;
    private String lunchSide;
    private String lunchDrink;
    private String dinnerMain;
    private String dinnerSide;
    private String dinnerDrink;

    // Individual meal diet types
    private String breakfastDiet;
    private String lunchDiet;
    private String dinnerDiet;
    private boolean breakfastAda;
    private boolean lunchAda;
    private boolean dinnerAda;

    // Additional patient information
    private String allergies;
    private String likes;
    private String dislikes;
    private String comments;

    // Other fields
    private List<String> mealSelections;
    private Date createdDate;
    private Date orderDate;
    private long createdAt;

    // Discharge status
    private boolean discharged;

    // Constructor
    public Patient() {
        this.breakfastComplete = false;
        this.lunchComplete = false;
        this.dinnerComplete = false;
        this.breakfastNPO = false;
        this.lunchNPO = false;
        this.dinnerNPO = false;
        this.createdDate = new Date();
        this.createdAt = System.currentTimeMillis();
        this.mealSelections = new ArrayList<>();
        this.discharged = false;

        // Initialize individual meal diets to null (will use main diet as fallback)
        this.breakfastDiet = null;
        this.lunchDiet = null;
        this.dinnerDiet = null;
        this.breakfastAda = false;
        this.lunchAda = false;
        this.dinnerAda = false;
    }

    // Basic getters and setters
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

    // Convenience methods for first/last name (for compatibility)
    public String getFirstName() {
        return patientFirstName;
    }

    public void setFirstName(String firstName) {
        this.patientFirstName = firstName;
    }

    public String getLastName() {
        return patientLastName;
    }

    public void setLastName(String lastName) {
        this.patientLastName = lastName;
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

    // Diet information getters and setters
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

    // Texture modification flags getters and setters
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

    // Liquid thickness flags getters and setters
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

    // Meal completion status getters and setters
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

    // Meal items getters and setters
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

    // Meal juices getters and setters
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

    // Meal drinks getters and setters
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

    // Individual meal components getters and setters
    public String getBreakfastMain() {
        return breakfastMain;
    }

    public void setBreakfastMain(String breakfastMain) {
        this.breakfastMain = breakfastMain;
    }

    public String getBreakfastSide() {
        return breakfastSide;
    }

    public void setBreakfastSide(String breakfastSide) {
        this.breakfastSide = breakfastSide;
    }

    public String getBreakfastDrink() {
        return breakfastDrink;
    }

    public void setBreakfastDrink(String breakfastDrink) {
        this.breakfastDrink = breakfastDrink;
    }

    public String getLunchMain() {
        return lunchMain;
    }

    public void setLunchMain(String lunchMain) {
        this.lunchMain = lunchMain;
    }

    public String getLunchSide() {
        return lunchSide;
    }

    public void setLunchSide(String lunchSide) {
        this.lunchSide = lunchSide;
    }

    public String getLunchDrink() {
        return lunchDrink;
    }

    public void setLunchDrink(String lunchDrink) {
        this.lunchDrink = lunchDrink;
    }

    public String getDinnerMain() {
        return dinnerMain;
    }

    public void setDinnerMain(String dinnerMain) {
        this.dinnerMain = dinnerMain;
    }

    public String getDinnerSide() {
        return dinnerSide;
    }

    public void setDinnerSide(String dinnerSide) {
        this.dinnerSide = dinnerSide;
    }

    public String getDinnerDrink() {
        return dinnerDrink;
    }

    public void setDinnerDrink(String dinnerDrink) {
        this.dinnerDrink = dinnerDrink;
    }

    // Individual meal diet types getters and setters
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

    // Additional patient information getters and setters
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

    // Other getters and setters
    public List<String> getMealSelections() {
        return mealSelections;
    }

    public void setMealSelections(List<String> mealSelections) {
        this.mealSelections = mealSelections;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDischarged() {
        return discharged;
    }

    public void setDischarged(boolean discharged) {
        this.discharged = discharged;
    }

    // Helper methods
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (patientFirstName != null && !patientFirstName.isEmpty()) {
            name.append(patientFirstName);
        }
        if (patientLastName != null && !patientLastName.isEmpty()) {
            if (name.length() > 0) {
                name.append(" ");
            }
            name.append(patientLastName);
        }
        return name.toString();
    }

    public String getLocation() {
        return wing + " - " + roomNumber;
    }

    public String getLocationInfo() {
        return wing + " - " + roomNumber;
    }

    public boolean hasTextureModifications() {
        return mechanicalGround || mechanicalChopped || biteSize ||
                breadOK || extraGravy || meatsOnly;
    }

    public boolean hasThickenedLiquids() {
        return nectarThick || honeyThick || puddingThick;
    }

    public boolean hasSpecialDiet() {
        return adaDiet || (fluidRestriction != null && !fluidRestriction.equals("No Restriction"));
    }
}