package com.hospital.dietary.models;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private int patientId;
    private String patientFirstName;  // FIXED: Split patient name into first and last
    private String patientLastName;   // FIXED: Split patient name into first and last
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

    // Default constructor
    public Patient() {
        this.breakfastComplete = false;
        this.lunchComplete = false;
        this.dinnerComplete = false;
        this.breakfastNPO = false;
        this.lunchNPO = false;
        this.dinnerNPO = false;
    }

    // Constructor with basic info
    public Patient(String patientFirstName, String patientLastName, String wing, String roomNumber, String diet) {
        this();
        this.patientFirstName = patientFirstName;
        this.patientLastName = patientLastName;
        this.wing = wing;
        this.roomNumber = roomNumber;
        this.diet = diet;
    }

    // Getters and Setters
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    // FIXED: First name getter/setter
    public String getPatientFirstName() {
        return patientFirstName;
    }

    public void setPatientFirstName(String patientFirstName) {
        this.patientFirstName = patientFirstName;
    }

    // FIXED: Last name getter/setter
    public String getPatientLastName() {
        return patientLastName;
    }

    public void setPatientLastName(String patientLastName) {
        this.patientLastName = patientLastName;
    }

    // FIXED: Legacy compatibility method for full name
    public String getPatientName() {
        if (patientFirstName != null && patientLastName != null) {
            return patientFirstName.trim() + " " + patientLastName.trim();
        } else if (patientFirstName != null) {
            return patientFirstName.trim();
        } else if (patientLastName != null) {
            return patientLastName.trim();
        }
        return "";
    }

    // FIXED: Additional backward compatibility method
    public String getName() {
        return getPatientName();
    }

    // FIXED: Legacy compatibility method for setting full name (splits into first/last)
    public void setPatientName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] nameParts = fullName.trim().split("\\s+", 2);
            if (nameParts.length >= 1) {
                this.patientFirstName = nameParts[0];
            }
            if (nameParts.length >= 2) {
                this.patientLastName = nameParts[1];
            } else {
                this.patientLastName = ""; // Set empty last name if only one name part
            }
        }
    }

    // FIXED: Additional backward compatibility method
    public void setName(String fullName) {
        setPatientName(fullName);
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

    public String getDiet() {
        return diet;
    }

    public void setDiet(String diet) {
        this.diet = diet;
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

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    // Helper methods
    public boolean hasIncompleteBreakfast() {
        return !breakfastComplete && !breakfastNPO;
    }

    public boolean hasIncompleteLunch() {
        return !lunchComplete && !lunchNPO;
    }

    public boolean hasIncompleteDinner() {
        return !dinnerComplete && !dinnerNPO;
    }

    public boolean hasAnyIncompleteMeals() {
        return hasIncompleteBreakfast() || hasIncompleteLunch() || hasIncompleteDinner();
    }

    // FIXED: Backward compatibility method for incomplete meal count
    public int getIncompleteMealCount() {
        int count = 0;
        if (hasIncompleteBreakfast()) count++;
        if (hasIncompleteLunch()) count++;
        if (hasIncompleteDinner()) count++;
        return count;
    }

    public boolean isAllMealsComplete() {
        return breakfastComplete && lunchComplete && dinnerComplete;
    }

    public String getDisplayName() {
        return getPatientName();
    }

    public String getRoomInfo() {
        return wing + " - Room " + roomNumber;
    }

    // FIXED: Backward compatibility method
    public String getLocationString() {
        return getRoomInfo();
    }

    public String getShortInfo() {
        return getDisplayName() + " (" + getRoomInfo() + ")";
    }

    // Diet helper methods
    public boolean isADADiet() {
        return diet != null && diet.equalsIgnoreCase("ADA");
    }

    public boolean isClearLiquidDiet() {
        return diet != null && diet.equalsIgnoreCase("Clear Liquid");
    }

    public boolean isFullLiquidDiet() {
        return diet != null && diet.equalsIgnoreCase("Full Liquid");
    }

    public boolean isPureeDiet() {
        return diet != null && diet.equalsIgnoreCase("Puree");
    }

    // Fluid restriction helper methods
    public boolean hasFluidRestriction() {
        return fluidRestriction != null && !fluidRestriction.equalsIgnoreCase("None") && !fluidRestriction.isEmpty();
    }

    // Texture modification helper methods
    public boolean hasTextureModifications() {
        return textureModifications != null && !textureModifications.isEmpty();
    }

    // FIXED: Backward compatibility method for restrictions
    public String getRestrictionsString() {
        List<String> restrictions = new ArrayList<>();
        
        // Add fluid restriction if present
        if (hasFluidRestriction()) {
            restrictions.add("Fluid: " + fluidRestriction);
        }
        
        // Add texture modifications if present
        if (hasTextureModifications()) {
            restrictions.add("Texture: " + textureModifications);
        }
        
        // Add diet-specific restrictions
        if (isADADiet()) {
            restrictions.add("ADA Diet");
        }
        
        if (isClearLiquidDiet()) {
            restrictions.add("Clear Liquid Only");
        } else if (isFullLiquidDiet()) {
            restrictions.add("Full Liquid Only");
        } else if (isPureeDiet()) {
            restrictions.add("Pureed Foods");
        }
        
        // Return formatted string or "None" if no restrictions
        if (restrictions.isEmpty()) {
            return "None";
        } else {
            return String.join(", ", restrictions);
        }
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", patientFirstName='" + patientFirstName + '\'' +
                ", patientLastName='" + patientLastName + '\'' +
                ", wing='" + wing + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", diet='" + diet + '\'' +
                ", fluidRestriction='" + fluidRestriction + '\'' +
                ", textureModifications='" + textureModifications + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Patient patient = (Patient) o;

        if (patientId != patient.patientId) return false;
        if (patientFirstName != null ? !patientFirstName.equals(patient.patientFirstName) : patient.patientFirstName != null)
            return false;
        if (patientLastName != null ? !patientLastName.equals(patient.patientLastName) : patient.patientLastName != null)
            return false;
        if (wing != null ? !wing.equals(patient.wing) : patient.wing != null) return false;
        return roomNumber != null ? roomNumber.equals(patient.roomNumber) : patient.roomNumber == null;
    }

    @Override
    public int hashCode() {
        int result = patientId;
        result = 31 * result + (patientFirstName != null ? patientFirstName.hashCode() : 0);
        result = 31 * result + (patientLastName != null ? patientLastName.hashCode() : 0);
        result = 31 * result + (wing != null ? wing.hashCode() : 0);
        result = 31 * result + (roomNumber != null ? roomNumber.hashCode() : 0);
        return result;
    }
}