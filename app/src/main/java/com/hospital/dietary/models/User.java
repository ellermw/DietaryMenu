package com.hospital.dietary.models;

import java.util.Date;

/**
 * User model class for authentication and user management
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private boolean isActive;
    private boolean mustChangePassword;
    private Date lastLogin;
    private Date createdDate;

    // Constructor
    public User() {
        this.isActive = true;
        this.mustChangePassword = false;
        this.createdDate = new Date();
    }

    public User(String username, String password, String fullName, String role) {
        this();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
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

    public boolean isStaff() {
        return "Staff".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "User".equalsIgnoreCase(role);
    }

    public String getDisplayName() {
        return fullName != null && !fullName.isEmpty() ? fullName : username;
    }

    public String getRoleDisplayName() {
        if (role == null) return "Unknown";

        // Format role for display
        switch (role.toLowerCase()) {
            case "admin":
            case "administrator":
                return "Administrator";
            case "staff":
                return "Staff";
            case "user":
                return "User";
            default:
                return role;
        }
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + role + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    @Override
    public int hashCode() {
        return userId;
    }
}