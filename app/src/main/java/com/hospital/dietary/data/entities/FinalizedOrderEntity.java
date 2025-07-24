package com.hospital.dietary.data.entities;

import androidx.room.*;

@Entity(tableName = "finalized_order")
public class FinalizedOrderEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "order_id")
    private long orderId;

    @ColumnInfo(name = "patient_name")
    private String patientName;

    @ColumnInfo(name = "wing")
    private String wing;

    @ColumnInfo(name = "room")
    private String room;

    @ColumnInfo(name = "order_date")
    private String orderDate;

    @ColumnInfo(name = "diet_type")
    private String dietType;

    // Getters and Setters
    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getWing() {
        return wing;
    }

    public void setWing(String wing) {
        this.wing = wing;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getDietType() {
        return dietType;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }
}