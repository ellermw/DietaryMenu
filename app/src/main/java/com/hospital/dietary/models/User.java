package com.hospital.dietary.models;

/**
 * User model class for backward compatibility
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private boolean isActive;
    private java.util.Date lastLogin;

    // Constructors
    public User() {}

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.isActive = true;
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

    public java.util.Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(java.util.Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getRoleDisplayName() {
        if ("Admin".equals(role)) {
            return "Administrator";
        } else if ("User".equals(role)) {
            return "Standard User";
        } else if ("Staff".equals(role)) {
            return "Staff Member";
        }
        return role;
    }
}