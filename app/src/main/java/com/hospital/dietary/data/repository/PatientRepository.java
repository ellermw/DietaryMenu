package com.hospital.dietary.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.hospital.dietary.data.database.AppDatabase;
import com.hospital.dietary.data.dao.PatientDao;
import com.hospital.dietary.data.entities.PatientEntity;
import com.hospital.dietary.models.Patient;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Repository class for Patient operations
 * Handles data operations and provides a clean API for the ViewModel
 */
public class PatientRepository {

    private PatientDao patientDao;

    public PatientRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        patientDao = db.patientDao();
    }

    // LiveData operations for UI observation
    public LiveData<List<PatientEntity>> getAllPatientsLive() {
        return patientDao.getAllPatientsLive();
    }

    public LiveData<List<PatientEntity>> getPendingPatientsLive() {
        return patientDao.getPendingPatientsLive();
    }

    public LiveData<List<PatientEntity>> getCompletedPatientsLive() {
        return patientDao.getCompletedPatientsLive();
    }

    public LiveData<PatientEntity> getPatientByIdLive(long patientId) {
        return patientDao.getPatientByIdLive(patientId);
    }

    public LiveData<List<PatientEntity>> getPatientsByWingLive(String wing) {
        return patientDao.getPatientsByWingLive(wing);
    }

    public LiveData<List<PatientEntity>> searchPatientsLive(String searchTerm) {
        return patientDao.searchPatientsLive("%" + searchTerm + "%");
    }

    // Async operations with callbacks
    public void addPatient(PatientEntity patient, RepositoryCallback<Long> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Check if room is already occupied
                int occupied = patientDao.isRoomOccupied(patient.getWing(), patient.getRoomNumber());
                if (occupied > 0) {
                    callback.onError("Room " + patient.getWing() + "-" + patient.getRoomNumber() + " is already occupied");
                    return;
                }

                long id = patientDao.insertPatient(patient);
                callback.onSuccess(id);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void updatePatient(PatientEntity patient, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int result = patientDao.updatePatient(patient);
                callback.onSuccess(result > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void deletePatient(long patientId, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int result = patientDao.deletePatientById(patientId);
                callback.onSuccess(result > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // Get patient by ID
    public void getPatientById(long patientId, RepositoryCallback<PatientEntity> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                PatientEntity patient = patientDao.getPatientById(patientId);
                if (patient != null) {
                    callback.onSuccess(patient);
                } else {
                    callback.onError("Patient not found");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // Mark meal as complete or incomplete
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
                    default:
                        callback.onError("Invalid meal type: " + mealType);
                        return;
                }
                callback.onSuccess(rowsUpdated > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // Update meal completion status
    public void updateMealCompletion(long patientId, boolean breakfastComplete,
                                     boolean lunchComplete, boolean dinnerComplete,
                                     RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int result = patientDao.updateMealCompletion(patientId,
                        breakfastComplete, lunchComplete, dinnerComplete);
                callback.onSuccess(result > 0);
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
                int result = patientDao.updateBreakfastItems(patientId, items, juices, drinks);
                callback.onSuccess(result > 0);
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
                int result = patientDao.updateLunchItems(patientId, items, juices, drinks);
                callback.onSuccess(result > 0);
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
                int result = patientDao.updateDinnerItems(patientId, items, juices, drinks);
                callback.onSuccess(result > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // Statistics
    public LiveData<List<PatientEntity>> getPatientsByDietTypeLive(String dietType) {
        return patientDao.getPatientsByDietTypeLive(dietType);
    }

    public LiveData<List<PatientEntity>> getAdaDietPatientsLive() {
        return patientDao.getAdaDietPatientsLive();
    }

    public LiveData<Integer> getPatientCountLive() {
        return patientDao.getPatientCountLive();
    }

    public LiveData<Integer> getPendingCountLive() {
        return patientDao.getPendingCountLive();
    }

    public LiveData<Integer> getCompletedCountLive() {
        return patientDao.getCompletedCountLive();
    }

    // Callback interface
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // Model conversion methods
    public static PatientEntity convertToEntity(Patient patient) {
        PatientEntity entity = new PatientEntity();
        entity.setPatientId(patient.getPatientId());
        entity.setPatientFirstName(patient.getPatientFirstName());
        entity.setPatientLastName(patient.getPatientLastName());
        entity.setWing(patient.getWing());
        entity.setRoomNumber(patient.getRoomNumber());
        entity.setDietType(patient.getDietType());
        entity.setDiet(patient.getDiet());
        entity.setAdaDiet(patient.isAdaDiet());
        entity.setFluidRestriction(patient.getFluidRestriction());
        entity.setTextureModifications(patient.getTextureModifications());
        entity.setMechanicalChopped(patient.isMechanicalChopped());
        entity.setMechanicalGround(patient.isMechanicalGround());
        entity.setBiteSize(patient.isBiteSize());
        entity.setBreadOK(patient.isBreadOK());
        entity.setNectarThick(patient.isNectarThick());
        entity.setPuddingThick(patient.isPuddingThick());
        entity.setHoneyThick(patient.isHoneyThick());
        entity.setBreakfastItems(patient.getBreakfastItems());
        entity.setBreakfastJuices(patient.getBreakfastJuices());
        entity.setBreakfastDrinks(patient.getBreakfastDrinks());
        entity.setLunchItems(patient.getLunchItems());
        entity.setLunchJuices(patient.getLunchJuices());
        entity.setLunchDrinks(patient.getLunchDrinks());
        entity.setDinnerItems(patient.getDinnerItems());
        entity.setDinnerJuices(patient.getDinnerJuices());
        entity.setDinnerDrinks(patient.getDinnerDrinks());
        entity.setBreakfastComplete(patient.isBreakfastComplete());
        entity.setLunchComplete(patient.isLunchComplete());
        entity.setDinnerComplete(patient.isDinnerComplete());
        entity.setCreatedDate(patient.getCreatedDate());
        return entity;
    }

    public static Patient convertFromEntity(PatientEntity entity) {
        Patient patient = new Patient();
        patient.setPatientId(entity.getPatientId());
        patient.setPatientFirstName(entity.getPatientFirstName());
        patient.setPatientLastName(entity.getPatientLastName());
        patient.setWing(entity.getWing());
        patient.setRoomNumber(entity.getRoomNumber());
        patient.setDietType(entity.getDietType());
        patient.setDiet(entity.getDiet());
        patient.setAdaDiet(entity.isAdaDiet());
        patient.setFluidRestriction(entity.getFluidRestriction());
        patient.setTextureModifications(entity.getTextureModifications());
        patient.setMechanicalChopped(entity.isMechanicalChopped());
        patient.setMechanicalGround(entity.isMechanicalGround());
        patient.setBiteSize(entity.isBiteSize());
        patient.setBreadOK(entity.isBreadOK());
        patient.setNectarThick(entity.isNectarThick());
        patient.setPuddingThick(entity.isPuddingThick());
        patient.setHoneyThick(entity.isHoneyThick());
        patient.setBreakfastItems(entity.getBreakfastItems());
        patient.setBreakfastJuices(entity.getBreakfastJuices());
        patient.setBreakfastDrinks(entity.getBreakfastDrinks());
        patient.setLunchItems(entity.getLunchItems());
        patient.setLunchJuices(entity.getLunchJuices());
        patient.setLunchDrinks(entity.getLunchDrinks());
        patient.setDinnerItems(entity.getDinnerItems());
        patient.setDinnerJuices(entity.getDinnerJuices());
        patient.setDinnerDrinks(entity.getDinnerDrinks());
        patient.setBreakfastComplete(entity.isBreakfastComplete());
        patient.setLunchComplete(entity.isLunchComplete());
        patient.setDinnerComplete(entity.isDinnerComplete());
        patient.setCreatedDate(entity.getCreatedDate());
        return patient;
    }
}