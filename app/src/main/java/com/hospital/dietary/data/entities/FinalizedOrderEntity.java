package com.hospital.dietary.data.entities;

import androidx.room.*;
import java.util.Date;

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

    // Add getters and setters
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
}