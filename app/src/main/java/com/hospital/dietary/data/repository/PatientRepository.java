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
        AppDatabase db = AppDatabase.getInstance(application);
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

    // Meal completion updates
    public void updateBreakfastComplete(long patientId, boolean complete, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int result = patientDao.updateBreakfastComplete(patientId, complete);
                callback.onSuccess(result > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void updateLunchComplete(long patientId, boolean complete, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int result = patientDao.updateLunchComplete(patientId, complete);
                callback.onSuccess(result > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void updateDinnerComplete(long patientId, boolean complete, RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int result = patientDao.updateDinnerComplete(patientId, complete);
                callback.onSuccess(result > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

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

    // Meal items updates
    public void updateBreakfastItems(long patientId, String items, String juices, String drinks,
                                     RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int result = patientDao.updateBreakfastItems(patientId, items, juices, drinks);
                callback.onSuccess(result > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void updateLunchItems(long patientId, String items, String juices, String drinks,
                                 RepositoryCallback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                int result = patientDao.updateLunchItems(patientId, items, juices, drinks);
                callback.onSuccess(result > 0);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void updateDinnerItems(long patientId, String items, String juices, String drinks,
                                  RepositoryCallback<Boolean> callback) {
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
        entity.setExtraGravy(patient.isExtraGravy());
        entity.setMeatsOnly(patient.isMeatsOnly());
        entity.setPuree(patient.isPuree());
        entity.setAllergies(patient.getAllergies());
        entity.setLikes(patient.getLikes());
        entity.setDislikes(patient.getDislikes());
        entity.setComments(patient.getComments());
        entity.setPreferredDrink(patient.getPreferredDrink());
        entity.setDrinkVariety(patient.getDrinkVariety());
        entity.setBreakfastComplete(patient.isBreakfastComplete());
        entity.setLunchComplete(patient.isLunchComplete());
        entity.setDinnerComplete(patient.isDinnerComplete());
        entity.setBreakfastNPO(patient.isBreakfastNPO());
        entity.setLunchNPO(patient.isLunchNPO());
        entity.setDinnerNPO(patient.isDinnerNPO());
        entity.setBreakfastItems(patient.getBreakfastItems());
        entity.setLunchItems(patient.getLunchItems());
        entity.setDinnerItems(patient.getDinnerItems());
        entity.setBreakfastJuices(patient.getBreakfastJuices());
        entity.setLunchJuices(patient.getLunchJuices());
        entity.setDinnerJuices(patient.getDinnerJuices());
        entity.setBreakfastDrinks(patient.getBreakfastDrinks());
        entity.setLunchDrinks(patient.getLunchDrinks());
        entity.setDinnerDrinks(patient.getDinnerDrinks());
        entity.setBreakfastDiet(patient.getBreakfastDiet());
        entity.setLunchDiet(patient.getLunchDiet());
        entity.setDinnerDiet(patient.getDinnerDiet());
        entity.setBreakfastAda(patient.isBreakfastAda());
        entity.setLunchAda(patient.isLunchAda());
        entity.setDinnerAda(patient.isDinnerAda());
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
        patient.setExtraGravy(entity.isExtraGravy());
        patient.setMeatsOnly(entity.isMeatsOnly());
        patient.setPuree(entity.isPuree());
        patient.setAllergies(entity.getAllergies());
        patient.setLikes(entity.getLikes());
        patient.setDislikes(entity.getDislikes());
        patient.setComments(entity.getComments());
        patient.setPreferredDrink(entity.getPreferredDrink());
        patient.setDrinkVariety(entity.getDrinkVariety());
        patient.setBreakfastComplete(entity.isBreakfastComplete());
        patient.setLunchComplete(entity.isLunchComplete());
        patient.setDinnerComplete(entity.isDinnerComplete());
        patient.setBreakfastNPO(entity.isBreakfastNPO());
        patient.setLunchNPO(entity.isLunchNPO());
        patient.setDinnerNPO(entity.isDinnerNPO());
        patient.setBreakfastItems(entity.getBreakfastItems());
        patient.setLunchItems(entity.getLunchItems());
        patient.setDinnerItems(entity.getDinnerItems());
        patient.setBreakfastJuices(entity.getBreakfastJuices());
        patient.setLunchJuices(entity.getLunchJuices());
        patient.setDinnerJuices(entity.getDinnerJuices());
        patient.setBreakfastDrinks(entity.getBreakfastDrinks());
        patient.setLunchDrinks(entity.getLunchDrinks());
        patient.setDinnerDrinks(entity.getDinnerDrinks());
        patient.setBreakfastDiet(entity.getBreakfastDiet());
        patient.setLunchDiet(entity.getLunchDiet());
        patient.setDinnerDiet(entity.getDinnerDiet());
        patient.setBreakfastAda(entity.isBreakfastAda());
        patient.setLunchAda(entity.isLunchAda());
        patient.setDinnerAda(entity.isDinnerAda());
        patient.setCreatedDate(entity.getCreatedDate());
        return patient;
    }
}