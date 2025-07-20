package com.hospital.dietary.models;

import java.util.Date;

public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private boolean isActive;
    private Date createdDate;
    private Date lastLogin;
    private boolean mustChangePassword; // FEATURE: Force password change on first login

    // Default constructor
    public User() {
        this.isActive = true;
        this.createdDate = new Date();
        this.role = "user"; // Default role
        this.mustChangePassword = false; // Default to false
    }

    // Constructor with essential fields
    public User(String username, String password, String fullName, String role) {
        this();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // Constructor with all fields except dates
    public User(String username, String password, String fullName, String role, boolean isActive) {
        this(username, password, fullName, role);
        this.isActive = isActive;
    }

    // Full constructor
    public User(int userId, String username, String password, String fullName, String role,
                boolean isActive, Date createdDate, Date lastLogin, boolean mustChangePassword) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.lastLogin = lastLogin;
        this.mustChangePassword = mustChangePassword;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    // FEATURE: Must change password support
    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    // Utility methods
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "user".equalsIgnoreCase(role);
    }

    public String getRoleDisplayName() {
        return role != null ? role.substring(0, 1).toUpperCase() + role.substring(1) : "User";
    }

    public String getStatusDisplayName() {
        return isActive ? "Active" : "Inactive";
    }

    // Helper method for display
    public String getDisplayName() {
        return fullName != null ? fullName : username;
    }

    // Helper method to check if user needs password change
    public boolean needsPasswordChange() {
        return mustChangePassword;
    }

    // Override toString for debugging
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", mustChangePassword=" + mustChangePassword +
                '}';
    }

    // Override equals and hashCode for proper comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User user = (User) obj;
        return userId == user.userId &&
                username != null && username.equals(user.username);
    }

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }
}