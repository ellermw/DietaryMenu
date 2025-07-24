package com.hospital.dietary.data.entities;

import androidx.room.*;
import java.util.Date;

@Entity(tableName = "meal_orders")
public class MealOrderEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "order_id")
    private long orderId;

    @ColumnInfo(name = "patient_id")
    private long patientId;

    @ColumnInfo(name = "meal")
    private String meal;

    @ColumnInfo(name = "order_date")
    private Date orderDate;

    @ColumnInfo(name = "is_complete")
    private boolean isComplete;

    @ColumnInfo(name = "created_by")
    private String createdBy;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    // Add getters and setters
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }

    public String getMeal() { return meal; }
    public void setMeal(String meal) { this.meal = meal; }
}