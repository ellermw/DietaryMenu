// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/models/User.java
// ================================================================================================

package com.hospital.dietary.models;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String role; // "User" or "Admin"
    private String createdDate;
    private String lastLogin;
    private boolean isActive;

    // Default constructor
    public User() {
        this.isActive = true;
    }

    // Constructor with essential fields
    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
    }

    // Full constructor
    public User(int userId, String username, String passwordHash, String role,
                String createdDate, String lastLogin, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdDate = createdDate;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Utility methods
    public boolean isAdmin() {
        return "Admin".equals(role);
    }

    public boolean isUser() {
        return "User".equals(role);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userId);
    }
}