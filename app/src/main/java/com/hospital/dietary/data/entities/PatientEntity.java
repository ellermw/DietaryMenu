package com.hospital.dietary.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Room Entity for Patient table
 * Maintains all existing fields for backward compatibility
 */
@Entity(tableName = "patient_info",
        indices = {@Index(value = {"wing", "room_number"}, unique = true)})
public class PatientEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "patient_id")
    private long patientId;

    @ColumnInfo(name = "patient_first_name")
    private String patientFirstName;

    @ColumnInfo(name = "patient_last_name")
    private String patientLastName;

    @ColumnInfo(name = "wing")
    private String wing;

    @ColumnInfo(name = "room_number")
    private String roomNumber;

    @ColumnInfo(name = "diet_type")
    private String dietType;

    @ColumnInfo(name = "diet")
    private String diet;

    @ColumnInfo(name = "ada_diet", defaultValue = "0")
    private boolean adaDiet;

    @ColumnInfo(name = "fluid_restriction")
    private String fluidRestriction;

    @ColumnInfo(name = "texture_modifications")
    private String textureModifications;

    // Texture modification fields
    @ColumnInfo(name = "mechanical_chopped", defaultValue = "0")
    private boolean mechanicalChopped;

    @ColumnInfo(name = "mechanical_ground", defaultValue = "0")
    private boolean mechanicalGround;

    @ColumnInfo(name = "bite_size", defaultValue = "0")
    private boolean biteSize;

    @ColumnInfo(name = "bread_ok", defaultValue = "0")
    private boolean breadOK;

    @ColumnInfo(name = "nectar_thick", defaultValue = "0")
    private boolean nectarThick;

    @ColumnInfo(name = "pudding_thick", defaultValue = "0")
    private boolean puddingThick;

    @ColumnInfo(name = "honey_thick", defaultValue = "0")
    private boolean honeyThick;

    @ColumnInfo(name = "extra_gravy", defaultValue = "0")
    private boolean extraGravy;

    @ColumnInfo(name = "meats_only", defaultValue = "0")
    private boolean meatsOnly;

    @ColumnInfo(name = "is_puree", defaultValue = "0")
    private boolean isPuree;

    // Additional dietary fields
    @ColumnInfo(name = "allergies")
    private String allergies;

    @ColumnInfo(name = "likes")
    private String likes;

    @ColumnInfo(name = "dislikes")
    private String dislikes;

    @ColumnInfo(name = "comments")
    private String comments;

    @ColumnInfo(name = "preferred_drink")
    private String preferredDrink;

    @ColumnInfo(name = "drink_variety")
    private String drinkVariety;

    // Meal completion status
    @ColumnInfo(name = "breakfast_complete", defaultValue = "0")
    private boolean breakfastComplete;

    @ColumnInfo(name = "lunch_complete", defaultValue = "0")
    private boolean lunchComplete;

    @ColumnInfo(name = "dinner_complete", defaultValue = "0")
    private boolean dinnerComplete;

    @ColumnInfo(name = "breakfast_npo", defaultValue = "0")
    private boolean breakfastNPO;

    @ColumnInfo(name = "lunch_npo", defaultValue = "0")
    private boolean lunchNPO;

    @ColumnInfo(name = "dinner_npo", defaultValue = "0")
    private boolean dinnerNPO;

    // Meal items (stored as comma-separated strings for backward compatibility)
    @ColumnInfo(name = "breakfast_items")
    private String breakfastItems;

    @ColumnInfo(name = "lunch_items")
    private String lunchItems;

    @ColumnInfo(name = "dinner_items")
    private String dinnerItems;

    @ColumnInfo(name = "breakfast_juices")
    private String breakfastJuices;

    @ColumnInfo(name = "lunch_juices")
    private String lunchJuices;

    @ColumnInfo(name = "dinner_juices")
    private String dinnerJuices;

    @ColumnInfo(name = "breakfast_drinks")
    private String breakfastDrinks;

    @ColumnInfo(name = "lunch_drinks")
    private String lunchDrinks;

    @ColumnInfo(name = "dinner_drinks")
    private String dinnerDrinks;

    @ColumnInfo(name = "created_date", defaultValue = "CURRENT_TIMESTAMP")
    private Date createdDate;

    @ColumnInfo(name = "breakfast_diet")
    private String breakfastDiet;

    @ColumnInfo(name = "lunch_diet")
    private String lunchDiet;

    @ColumnInfo(name = "dinner_diet")
    private String dinnerDiet;

    @ColumnInfo(name = "breakfast_ada", defaultValue = "0")
    private boolean breakfastAda;

    @ColumnInfo(name = "lunch_ada", defaultValue = "0")
    private boolean lunchAda;

    @ColumnInfo(name = "dinner_ada", defaultValue = "0")
    private boolean dinnerAda;

    // Constructors
    public PatientEntity() {}

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

    // Fixed: Only one getter for isPuree field to avoid ambiguity
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    // Helper methods
    public String getFullName() {
        return patientFirstName + " " + patientLastName;
    }

    @Ignore
    public String getLocationInfo() {
        return wing + "-" + roomNumber;
    }
}