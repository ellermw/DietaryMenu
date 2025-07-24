package com.hospital.dietary.data.dao;

import androidx.room.*;
import com.hospital.dietary.data.entities.DefaultMenuEntity;

@Dao
public interface DefaultMenuDao {
    @Insert
    long insertDefaultMenuItem(DefaultMenuEntity item);
}