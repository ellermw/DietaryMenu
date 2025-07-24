package com.hospital.dietary.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Room Entity for User table
 * Replaces the old User model with Room annotations
 */
@Entity(tableName = "users",
        indices = {@Index(value = {"username"}, unique = true)})
public class UserEntity {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private long userId;
    
    @ColumnInfo(name = "username")
    private String username;
    
    @ColumnInfo(name = "password")
    private String password;
    
    @ColumnInfo(name = "full_name")
    private String fullName;
    
    @ColumnInfo(name = "role")
    private String role;
    
    @ColumnInfo(name = "is_active", defaultValue = "1")
    private boolean isActive = true;
    
    @ColumnInfo(name = "must_change_password", defaultValue = "0")
    private boolean mustChangePassword = false;
    
    @ColumnInfo(name = "last_login")
    private Date lastLogin;
    
    @ColumnInfo(name = "created_date", defaultValue = "CURRENT_TIMESTAMP")
    private Date createdDate;

    // Constructors
    public UserEntity() {
        this.createdDate = new Date();
    }

    // Getters and Setters
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Helper methods
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role) || "Administrator".equalsIgnoreCase(role);
    }

    public String getDisplayName() {
        return fullName != null && !fullName.isEmpty() ? fullName : username;
    }
}