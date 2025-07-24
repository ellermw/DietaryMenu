package com.hospital.dietary.data.entities;

import androidx.room.*;

@Entity(tableName = "order_items")
public class OrderItemEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "order_item_id")
    private long orderItemId;

    @ColumnInfo(name = "order_id")
    private long orderId;

    @ColumnInfo(name = "item_id")
    private long itemId;

    @ColumnInfo(name = "quantity")
    private int quantity = 1;

    // Getters and Setters
    public long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}