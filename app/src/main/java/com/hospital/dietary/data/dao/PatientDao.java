package com.hospital.dietary.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.hospital.dietary.data.entities.PatientEntity;
import java.util.List;

/**
 * Room DAO for Patient operations
 * All queries return LiveData for automatic UI updates
 */
@Dao
public interface PatientDao {
    
    // Insert operations
    @Insert
    long insertPatient(PatientEntity patient);
    
    @Insert
    long[] insertPatients(List<PatientEntity> patients);
    
    // Update operations
    @Update
    int updatePatient(PatientEntity patient);
    
    @Query("UPDATE patient_info SET breakfast_complete = :complete WHERE patient_id = :patientId")
    int updateBreakfastComplete(long patientId, boolean complete);
    
    @Query("UPDATE patient_info SET lunch_complete = :complete WHERE patient_id = :patientId")
    int updateLunchComplete(long patientId, boolean complete);
    
    @Query("UPDATE patient_info SET dinner_complete = :complete WHERE patient_id = :patientId")
    int updateDinnerComplete(long patientId, boolean complete);
    
    @Query("UPDATE patient_info SET modified_date = CURRENT_TIMESTAMP WHERE patient_id = :patientId")
    void updateModifiedDate(long patientId);
    
    // Delete operations
    @Delete
    int deletePatient(PatientEntity patient);
    
    @Query("DELETE FROM patient_info WHERE patient_id = :patientId")
    int deletePatientById(long patientId);
    
    @Query("DELETE FROM patient_info")
    void deleteAllPatients();
    
    // Query operations - LiveData for automatic UI updates
    @Query("SELECT * FROM patient_info ORDER BY wing, CAST(room_number AS INTEGER)")
    LiveData<List<PatientEntity>> getAllPatientsLive();
    
    @Query("SELECT * FROM patient_info ORDER BY wing, CAST(room_number AS INTEGER)")
    List<PatientEntity> getAllPatients();
    
    @Query("SELECT * FROM patient_info WHERE patient_id = :patientId")
    LiveData<PatientEntity> getPatientByIdLive(long patientId);
    
    @Query("SELECT * FROM patient_info WHERE patient_id = :patientId")
    PatientEntity getPatientById(long patientId);
    
    @Query("SELECT * FROM patient_info WHERE " +
           "(breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) " +
           "ORDER BY wing, CAST(room_number AS INTEGER)")
    LiveData<List<PatientEntity>> getPendingPatientsLive();
    
    @Query("SELECT * FROM patient_info WHERE " +
           "(breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0) " +
           "ORDER BY wing, CAST(room_number AS INTEGER)")
    List<PatientEntity> getPendingPatients();
    
    @Query("SELECT * FROM patient_info WHERE " +
           "breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
           "ORDER BY wing, CAST(room_number AS INTEGER)")
    LiveData<List<PatientEntity>> getCompletedPatientsLive();
    
    @Query("SELECT * FROM patient_info WHERE " +
           "breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1 " +
           "ORDER BY wing, CAST(room_number AS INTEGER)")
    List<PatientEntity> getCompletedPatients();
    
    @Query("SELECT * FROM patient_info WHERE wing = :wing ORDER BY CAST(room_number AS INTEGER)")
    LiveData<List<PatientEntity>> getPatientsByWingLive(String wing);
    
    @Query("SELECT * FROM patient_info WHERE wing = :wing ORDER BY CAST(room_number AS INTEGER)")
    List<PatientEntity> getPatientsByWing(String wing);
    
    @Query("SELECT * FROM patient_info WHERE " +
           "LOWER(patient_first_name) LIKE LOWER(:searchTerm) OR " +
           "LOWER(patient_last_name) LIKE LOWER(:searchTerm) OR " +
           "room_number LIKE :searchTerm " +
           "ORDER BY wing, CAST(room_number AS INTEGER)")
    LiveData<List<PatientEntity>> searchPatientsLive(String searchTerm);
    
    @Query("SELECT * FROM patient_info WHERE " +
           "LOWER(patient_first_name) LIKE LOWER(:searchTerm) OR " +
           "LOWER(patient_last_name) LIKE LOWER(:searchTerm) OR " +
           "room_number LIKE :searchTerm " +
           "ORDER BY wing, CAST(room_number AS INTEGER)")
    List<PatientEntity> searchPatients(String searchTerm);
    
    @Query("SELECT * FROM patient_info WHERE diet_type = :dietType ORDER BY wing, CAST(room_number AS INTEGER)")
    LiveData<List<PatientEntity>> getPatientsByDietTypeLive(String dietType);
    
    @Query("SELECT * FROM patient_info WHERE ada_diet = 1 ORDER BY wing, CAST(room_number AS INTEGER)")
    LiveData<List<PatientEntity>> getAdaDietPatientsLive();
    
    @Query("SELECT COUNT(*) FROM patient_info")
    LiveData<Integer> getPatientCountLive();
    
    @Query("SELECT COUNT(*) FROM patient_info")
    int getPatientCount();
    
    @Query("SELECT COUNT(*) FROM patient_info WHERE " +
           "(breakfast_complete = 0 OR lunch_complete = 0 OR dinner_complete = 0)")
    LiveData<Integer> getPendingCountLive();
    
    @Query("SELECT COUNT(*) FROM patient_info WHERE " +
           "breakfast_complete = 1 AND lunch_complete = 1 AND dinner_complete = 1")
    LiveData<Integer> getCompletedCountLive();
    
    // Check if room is occupied
    @Query("SELECT COUNT(*) FROM patient_info WHERE wing = :wing AND room_number = :roomNumber")
    int isRoomOccupied(String wing, String roomNumber);
    
    // Batch update meal completion
    @Query("UPDATE patient_info SET " +
           "breakfast_complete = :breakfastComplete, " +
           "lunch_complete = :lunchComplete, " +
           "dinner_complete = :dinnerComplete, " +
           "modified_date = CURRENT_TIMESTAMP " +
           "WHERE patient_id = :patientId")
    int updateMealCompletion(long patientId, boolean breakfastComplete, 
                           boolean lunchComplete, boolean dinnerComplete);
    
    // Update meal items
    @Query("UPDATE patient_info SET " +
           "breakfast_items = :items, " +
           "breakfast_juices = :juices, " +
           "breakfast_drinks = :drinks, " +
           "modified_date = CURRENT_TIMESTAMP " +
           "WHERE patient_id = :patientId")
    int updateBreakfastItems(long patientId, String items, String juices, String drinks);
    
    @Query("UPDATE patient_info SET " +
           "lunch_items = :items, " +
           "lunch_juices = :juices, " +
           "lunch_drinks = :drinks, " +
           "modified_date = CURRENT_TIMESTAMP " +
           "WHERE patient_id = :patientId")
    int updateLunchItems(long patientId, String items, String juices, String drinks);
    
    @Query("UPDATE patient_info SET " +
           "dinner_items = :items, " +
           "dinner_juices = :juices, " +
           "dinner_drinks = :drinks, " +
           "modified_date = CURRENT_TIMESTAMP " +
           "WHERE patient_id = :patientId")
    int updateDinnerItems(long patientId, String items, String juices, String drinks);
}