// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/models/Diet.java
// ================================================================================================

package com.hospital.dietary.models;

public class Diet {
    private int dietId;
    private String name;

    public Diet() {}

    public Diet(int dietId, String name) {
        this.dietId = dietId;
        this.name = name;
    }

    public int getDietId() { return dietId; }
    public void setDietId(int dietId) { this.dietId = dietId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}
