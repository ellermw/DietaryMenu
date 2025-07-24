package com.hospital.dietary.ui.patient;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.hospital.dietary.data.entities.PatientEntity;
import com.hospital.dietary.data.repository.PatientRepository;
import java.util.List;

/**
 * ViewModel for Patient-related Activities
 * Manages patient data and operations
 */
public class PatientViewModel extends AndroidViewModel {
    
    private final PatientRepository patientRepository;
    
    // Observable data
    private final LiveData<List<PatientEntity>> allPatients;
    private final LiveData<List<PatientEntity>> pendingPatients;
    private final LiveData<List<PatientEntity>> completedPatients;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<PatientEntity>> searchResults;
    
    // Operation results
    private final MutableLiveData<OperationResult> operationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // Selected patient for detail view
    private final MutableLiveData<PatientEntity> selectedPatient = new MutableLiveData<>();
    
    public PatientViewModel(@NonNull Application application) {
        super(application);
        patientRepository = new PatientRepository(application);
        
        // Initialize LiveData
        allPatients = patientRepository.getAllPatientsLive();
        pendingPatients = patientRepository.getPendingPatientsLive();
        completedPatients = patientRepository.getCompletedPatientsLive();
        
        // Set up search results to update when search query changes
        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return allPatients;
            } else {
                return patientRepository.searchPatientsLive("%" + query.trim() + "%");
            }
        });
    }
    
    // Getters for observable data
    public LiveData<List<PatientEntity>> getAllPatients() {
        return allPatients;
    }
    
    public LiveData<List<PatientEntity>> getPendingPatients() {
        return pendingPatients;
    }
    
    public LiveData<List<PatientEntity>> getCompletedPatients() {
        return completedPatients;
    }
    
    public LiveData<List<PatientEntity>> getSearchResults() {
        return searchResults;
    }
    
    public LiveData<OperationResult> getOperationResult() {
        return operationResult;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<PatientEntity> getSelectedPatient() {
        return selectedPatient;
    }
    
    // Set search query
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
    
    // Set selected patient
    public void setSelectedPatient(PatientEntity patient) {
        selectedPatient.setValue(patient);
    }
    
    // Load patient by ID
    public void loadPatientById(long patientId) {
        isLoading.setValue(true);
        patientRepository.getPatientById(patientId, new PatientRepository.RepositoryCallback<PatientEntity>() {
            @Override
            public void onSuccess(PatientEntity result) {
                isLoading.postValue(false);
                selectedPatient.postValue(result);
            }
            
            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                operationResult.postValue(new OperationResult(false, "Error loading patient: " + error));
            }
        });
    }
    
    // Add new patient
    public void addPatient(PatientEntity patient) {
        isLoading.setValue(true);
        patientRepository.addPatient(patient, new PatientRepository.RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long patientId) {
                isLoading.postValue(false);
                patient.setPatientId(patientId);
                operationResult.postValue(new OperationResult(true, "Patient added successfully", patientId));
            }
            
            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                operationResult.postValue(new OperationResult(false, "Error adding patient: " + error));
            }
        });
    }
    
    // Update patient
    public void updatePatient(PatientEntity patient) {
        isLoading.setValue(true);
        patientRepository.updatePatient(patient, new PatientRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                isLoading.postValue(false);
                if (success) {
                    operationResult.postValue(new OperationResult(true, "Patient updated successfully"));
                } else {
                    operationResult.postValue(new OperationResult(false, "Failed to update patient"));
                }
            }
            
            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                operationResult.postValue(new OperationResult(false, "Error updating patient: " + error));
            }
        });
    }
    
    // Delete patient
    public void deletePatient(long patientId) {
        isLoading.setValue(true);
        patientRepository.deletePatient(patientId, new PatientRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                isLoading.postValue(false);
                if (success) {
                    operationResult.postValue(new OperationResult(true, "Patient deleted successfully"));
                } else {
                    operationResult.postValue(new OperationResult(false, "Failed to delete patient"));
                }
            }
            
            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                operationResult.postValue(new OperationResult(false, "Error deleting patient: " + error));
            }
        });
    }
    
    // Mark meal as complete
    public void markMealComplete(long patientId, String mealType, boolean complete) {
        patientRepository.markMealComplete(patientId, mealType, complete, 
            new PatientRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean success) {
                    if (success) {
                        operationResult.postValue(new OperationResult(true, 
                            mealType + " marked as " + (complete ? "complete" : "incomplete")));
                    }
                }
                
                @Override
                public void onError(String error) {
                    operationResult.postValue(new OperationResult(false, 
                        "Error updating meal status: " + error));
                }
            });
    }
    
    // Clear operation result
    public void clearOperationResult() {
        operationResult.setValue(null);
    }
    
    // Operation result class
    public static class OperationResult {
        private final boolean success;
        private final String message;
        private final Long patientId;
        
        public OperationResult(boolean success, String message) {
            this(success, message, null);
        }
        
        public OperationResult(boolean success, String message, Long patientId) {
            this.success = success;
            this.message = message;
            this.patientId = patientId;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Long getPatientId() {
            return patientId;
        }
    }
}