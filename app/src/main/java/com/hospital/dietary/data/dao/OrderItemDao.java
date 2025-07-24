package com.hospital.dietary.data.dao;

import androidx.room.*;
import com.hospital.dietary.data.entities.OrderItemEntity;

@Dao
public interface OrderItemDao {
    @Insert
    long insertOrderItem(OrderItemEntity item);
}