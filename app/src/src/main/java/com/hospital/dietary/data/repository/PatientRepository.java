package com.hospital.dietary.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.hospital.dietary.data.database.AppDatabase;
import com.hospital.dietary.data.dao.PatientDao;
import com.hospital.dietary.data.entities.PatientEntity;
import java.util.List;

/**
 * Repository for Patient data operations
 * Single source of truth for patient data
 */
public class PatientRepository {
    
    private final PatientDao patientDao;
    private final AppDatabase database;
    
    public PatientRepository(Application application) {
        database = AppDatabase.getDatabase(application);
        patientDao = database.patientDao();
    }
    
    // Get all patients as LiveData
    public LiveData<List<PatientEntity>> getAllPatientsLive() {
        return patientDao.getAllPatientsLive();
    }
    
    // Get pending patients as LiveData
    public LiveData<List<PatientEntity>> getPendingPatientsLive() {
        return patientDao.getPendingPatientsLive();
    }
    
    // Get completed patients as LiveData
    public LiveData<List<PatientEntity>> getCompletedPatientsLive() {
        return patientDao.getCompletedPatientsLive();
    }
    
    // Search patients
    public LiveData<List<PatientEntity>> searchPatientsLive(String searchTerm) {
        return patientDao.searchPatientsLive(searchTerm);
    }
    
    // Get patient by ID
    public void getPatientById(long patientId, RepositoryCallback<PatientEntity> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                PatientEntity patient = patientDao.getPatientById(patientId);
                callback.onSuccess(patient);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Add new patient
    public void addPatient(PatientEntity patient, RepositoryCallback<Long> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Check if room is already occupied
                int occupied = patientDao.isRoomOccupied(patient.getWing(), patient.getRoomNumber());
                if (occupied > 0) {
                    callback.onError("Room " + patient.getWing() + " - " + patient.getRoomNumber() + " is already occupied");
                    return;
                }
                
                long id = patientDao.insertPatient(patient);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Update patient
    public void updatePatient(PatientEntity patient, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsUpdated = patientDao.updatePatient(patient);
                patientDao.updateModifiedDate(patient.getPatientId());
                callback.onSuccess(rowsUpdated > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Delete patient
    public void deletePatient(long patientId, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsDeleted = patientDao.deletePatientById(patientId);
                callback.onSuccess(rowsDeleted > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Mark meal as complete
    public void markMealComplete(long patientId, String mealType, boolean complete, 
                                RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsUpdated = 0;
                switch (mealType.toLowerCase()) {
                    case "breakfast":
                        rowsUpdated = patientDao.updateBreakfastComplete(patientId, complete);
                        break;
                    case "lunch":
                        rowsUpdated = patientDao.updateLunchComplete(patientId, complete);
                        break;
                    case "dinner":
                        rowsUpdated = patientDao.updateDinnerComplete(patientId, complete);
                        break;
                }
                callback.onSuccess(rowsUpdated > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Update all meal completion status
    public void updateMealCompletion(long patientId, boolean breakfastComplete, 
                                   boolean lunchComplete, boolean dinnerComplete,
                                   RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsUpdated = patientDao.updateMealCompletion(patientId, 
                    breakfastComplete, lunchComplete, dinnerComplete);
                callback.onSuccess(rowsUpdated > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Update breakfast items
    public void updateBreakfastItems(long patientId, String items, String juices, 
                                   String drinks, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsUpdated = patientDao.updateBreakfastItems(patientId, items, juices, drinks);
                callback.onSuccess(rowsUpdated > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Update lunch items
    public void updateLunchItems(long patientId, String items, String juices, 
                               String drinks, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsUpdated = patientDao.updateLunchItems(patientId, items, juices, drinks);
                callback.onSuccess(rowsUpdated > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Update dinner items
    public void updateDinnerItems(long patientId, String items, String juices, 
                                String drinks, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int rowsUpdated = patientDao.updateDinnerItems(patientId, items, juices, drinks);
                callback.onSuccess(rowsUpdated > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    // Get patients by wing
    public LiveData<List<PatientEntity>> getPatientsByWingLive(String wing) {
        return patientDao.getPatientsByWingLive(wing);
    }
    
    // Get patients by diet type
    public LiveData<List<PatientEntity>> getPatientsByDietTypeLive(String dietType) {
        return patientDao.getPatientsByDietTypeLive(dietType);
    }
    
    // Get ADA diet patients
    public LiveData<List<PatientEntity>> getAdaDietPatientsLive() {
        return patientDao.getAdaDietPatientsLive();
    }
    
    // Get counts
    public LiveData<Integer> getPatientCountLive() {
        return patientDao.getPatientCountLive();
    }
    
    public LiveData<Integer> getPendingCountLive() {
        return patientDao.getPendingCountLive();
    }
    
    public LiveData<Integer> getCompletedCountLive() {
        return patientDao.getCompletedCountLive();
    }
    
    // Generic callback interface
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}