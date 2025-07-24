package com.hospital.dietary.data.dao;

import androidx.room.*;
import com.hospital.dietary.data.entities.MealOrderEntity;

@Dao
public interface MealOrderDao {
    @Insert
    long insertMealOrder(MealOrderEntity order);
}