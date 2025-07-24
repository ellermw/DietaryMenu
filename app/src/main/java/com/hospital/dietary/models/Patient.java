package com.hospital.dietary.models;

import com.hospital.dietary.data.entities.PatientEntity;

public class Patient {
    private PatientEntity entity;

    public Patient() {
        this.entity = new PatientEntity();
    }

    public Patient(PatientEntity entity) {
        this.entity = entity;
    }

    public long getPatientId() { return entity.getPatientId(); }
    public void setPatientId(long id) { entity.setPatientId(id); }

    public String getPatientFirstName() { return entity.getPatientFirstName(); }
    public void setPatientFirstName(String name) { entity.setPatientFirstName(name); }

    public String getPatientLastName() { return entity.getPatientLastName(); }
    public void setPatientLastName(String name) { entity.setPatientLastName(name); }

    public String getWing() { return entity.getWing(); }
    public void setWing(String wing) { entity.setWing(wing); }

    public String getRoomNumber() { return entity.getRoomNumber(); }
    public void setRoomNumber(String room) { entity.setRoomNumber(room); }

    public String getDiet() { return entity.getDiet(); }
    public void setDiet(String diet) { entity.setDiet(diet); }

    public String getDietType() { return entity.getDietType(); }
    public void setDietType(String dietType) { entity.setDietType(dietType); }

    public boolean isAdaDiet() { return entity.isAdaDiet(); }
    public void setAdaDiet(boolean ada) { entity.setAdaDiet(ada); }

    public String getFluidRestriction() { return entity.getFluidRestriction(); }
    public void setFluidRestriction(String restriction) { entity.setFluidRestriction(restriction); }

    public String getTextureModifications() { return entity.getTextureModifications(); }
    public void setTextureModifications(String mods) { entity.setTextureModifications(mods); }

    public boolean isMechanicalChopped() { return entity.isMechanicalChopped(); }
    public void setMechanicalChopped(boolean val) { entity.setMechanicalChopped(val); }

    public boolean isMechanicalGround() { return entity.isMechanicalGround(); }
    public void setMechanicalGround(boolean val) { entity.setMechanicalGround(val); }

    public boolean isBiteSize() { return entity.isBiteSize(); }
    public void setBiteSize(boolean val) { entity.setBiteSize(val); }

    public boolean isBreadOK() { return entity.isBreadOK(); }
    public void setBreadOK(boolean val) { entity.setBreadOK(val); }

    public boolean isBreakfastComplete() { return entity.isBreakfastComplete(); }
    public void setBreakfastComplete(boolean val) { entity.setBreakfastComplete(val); }

    public boolean isLunchComplete() { return entity.isLunchComplete(); }
    public void setLunchComplete(boolean val) { entity.setLunchComplete(val); }

    public boolean isDinnerComplete() { return entity.isDinnerComplete(); }
    public void setDinnerComplete(boolean val) { entity.setDinnerComplete(val); }

    public PatientEntity toEntity() { return entity; }
    public static Patient fromEntity(PatientEntity entity) { return new Patient(entity); }
}