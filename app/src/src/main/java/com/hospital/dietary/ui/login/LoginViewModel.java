package com.hospital.dietary.ui.login;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.hospital.dietary.data.database.AppDatabase;
import com.hospital.dietary.data.dao.UserDao;
import com.hospital.dietary.data.entities.UserEntity;
import com.hospital.dietary.data.repository.UserRepository;

/**
 * ViewModel for LoginActivity
 * Handles all login logic and data operations
 */
public class LoginViewModel extends AndroidViewModel {
    
    private final UserRepository userRepository;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }
    
    // Observable login result
    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }
    
    // Observable loading state
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    // Perform login
    public void login(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            loginResult.setValue(new LoginResult(false, "Username is required", null));
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            loginResult.setValue(new LoginResult(false, "Password is required", null));
            return;
        }
        
        // Show loading
        isLoading.setValue(true);
        
        // Perform login in background
        userRepository.validateLogin(username.trim(), password, new UserRepository.RepositoryCallback<UserEntity>() {
            @Override
            public void onSuccess(UserEntity user) {
                isLoading.postValue(false);
                
                if (user != null) {
                    if (!user.isActive()) {
                        loginResult.postValue(new LoginResult(false, 
                            "Account is inactive. Please contact administrator.", null));
                    } else {
                        // Update last login
                        userRepository.updateLastLogin(user.getUserId());
                        loginResult.postValue(new LoginResult(true, "Login successful", user));
                    }
                } else {
                    loginResult.postValue(new LoginResult(false, 
                        "Invalid username or password", null));
                }
            }
            
            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                loginResult.postValue(new LoginResult(false, 
                    "Login error: " + error, null));
            }
        });
    }
    
    // Create default admin if needed
    public void createDefaultAdminIfNeeded() {
        userRepository.createDefaultAdminIfNeeded();
    }
    
    // Login result class
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final UserEntity user;
        
        public LoginResult(boolean success, String message, UserEntity user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public UserEntity getUser() {
            return user;
        }
    }
}