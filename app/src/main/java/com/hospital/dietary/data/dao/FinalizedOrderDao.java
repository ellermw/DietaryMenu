package com.hospital.dietary.data.dao;

import androidx.room.*;
import com.hospital.dietary.data.entities.FinalizedOrderEntity;

@Dao
public interface FinalizedOrderDao {
    @Insert
    long insertFinalizedOrder(FinalizedOrderEntity order);
}