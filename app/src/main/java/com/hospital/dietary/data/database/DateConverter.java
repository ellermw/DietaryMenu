package com.hospital.dietary.data.database;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * Type converters for Room database
 * Converts Date objects to/from timestamps
 */
public class DateConverter {
    
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}