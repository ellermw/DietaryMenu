package com.hospital.dietary.models;

import java.util.Date;

public class Patient {
    private int patientId;
    private String patientFirstName;
    private String patientLastName;
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
    private Date createdDate;

    // Constructors
    public Patient() {}

    public Patient(String patientFirstName, String patientLastName, String wing, String roomNumber, String diet) {
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

    // Meal completion getters and setters
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

    // NPO status getters and setters
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Utility methods
    public String getFullName() {
        if (patientFirstName != null && patientLastName != null) {
            return patientFirstName + " " + patientLastName;
        } else if (patientFirstName != null) {
            return patientFirstName;
        } else if (patientLastName != null) {
            return patientLastName;
        } else {
            return "Unknown Patient";
        }
    }

    public String getLocationString() {
        return wing + " - Room " + roomNumber;
    }

    // FIXED: Get incomplete meal count for pending orders
    public int getIncompleteMealCount() {
        int count = 0;
        if (!breakfastComplete) count++;
        if (!lunchComplete) count++;
        if (!dinnerComplete) count++;
        return count;
    }

    public boolean hasIncompleteMeals() {
        return !breakfastComplete || !lunchComplete || !dinnerComplete;
    }

    public boolean hasNPORestrictions() {
        return breakfastNPO || lunchNPO || dinnerNPO;
    }

    /**
     * NEW: Get meal completion status for display
     */
    public String getMealCompletionStatus() {
        int completed = 0;
        if (breakfastComplete) completed++;
        if (lunchComplete) completed++;
        if (dinnerComplete) completed++;

        StringBuilder status = new StringBuilder();
        status.append(completed).append("/3 meals complete");

        // Add NPO info if any
        if (hasNPORestrictions()) {
            status.append(" (");
            boolean first = true;
            if (breakfastNPO) {
                status.append("B-NPO");
                first = false;
            }
            if (lunchNPO) {
                if (!first) status.append(", ");
                status.append("L-NPO");
                first = false;
            }
            if (dinnerNPO) {
                if (!first) status.append(", ");
                status.append("D-NPO");
            }
            status.append(")");
        }

        return status.toString();
    }

    public String getNPOStatus() {
        if (!hasNPORestrictions()) {
            return "No NPO restrictions";
        }

        StringBuilder npo = new StringBuilder("NPO: ");
        if (breakfastNPO) npo.append("Breakfast ");
        if (lunchNPO) npo.append("Lunch ");
        if (dinnerNPO) npo.append("Dinner ");
        return npo.toString().trim();
    }

    /**
     * Get restrictions string for display (used by FinishedOrdersActivity)
     */
    public String getRestrictionsString() {
        StringBuilder restrictions = new StringBuilder();

        if (hasFluidRestrictions()) {
            restrictions.append("Fluid: ").append(fluidRestriction);
        }

        if (hasTextureModifications()) {
            if (restrictions.length() > 0) {
                restrictions.append(" | ");
            }
            restrictions.append("Texture: ").append(textureModifications);
        }

        if (hasNPORestrictions()) {
            if (restrictions.length() > 0) {
                restrictions.append(" | ");
            }
            restrictions.append(getNPOStatus());
        }

        return restrictions.length() > 0 ? restrictions.toString() : "No restrictions";
    }

    // Helper methods for restrictions
    public boolean hasFluidRestrictions() {
        return fluidRestriction != null && !fluidRestriction.trim().isEmpty() && !"None".equals(fluidRestriction);
    }

    public boolean hasTextureModifications() {
        return textureModifications != null && !textureModifications.trim().isEmpty();
    }

    // Validation methods
    public boolean isValid() {
        return patientFirstName != null && !patientFirstName.trim().isEmpty() &&
                patientLastName != null && !patientLastName.trim().isEmpty() &&
                wing != null && !wing.trim().isEmpty() &&
                roomNumber != null && !roomNumber.trim().isEmpty() &&
                diet != null && !diet.trim().isEmpty();
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();

        if (patientFirstName == null || patientFirstName.trim().isEmpty()) {
            errors.append("First name is required. ");
        }

        if (patientLastName == null || patientLastName.trim().isEmpty()) {
            errors.append("Last name is required. ");
        }

        if (wing == null || wing.trim().isEmpty()) {
            errors.append("Wing is required. ");
        }

        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            errors.append("Room number is required. ");
        }

        if (diet == null || diet.trim().isEmpty()) {
            errors.append("Diet is required. ");
        }

        return errors.toString().trim();
    }

    @Override
    public String toString() {
        return getFullName() + " - " + getLocationString() + " (" + diet + ")";
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
        return Integer.hashCode(patientId);
    }
}