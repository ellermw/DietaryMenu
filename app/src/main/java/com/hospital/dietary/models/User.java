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

    // Default constructor
    public User() {
        this.isActive = true;
        this.createdDate = new Date();
        this.role = "user"; // Default role
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

    // Validation methods
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                password != null && !password.trim().isEmpty() &&
                fullName != null && !fullName.trim().isEmpty() &&
                role != null && !role.trim().isEmpty();
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();

        if (username == null || username.trim().isEmpty()) {
            errors.append("Username is required. ");
        }
        if (password == null || password.trim().isEmpty()) {
            errors.append("Password is required. ");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            errors.append("Full name is required. ");
        }
        if (role == null || role.trim().isEmpty()) {
            errors.append("Role is required. ");
        }

        return errors.toString().trim();
    }

    // Authentication helper
    public boolean authenticate(String inputPassword) {
        // In a real application, you would hash the passwords and compare hashes
        // For this demo, we're using plain text (NOT recommended for production)
        return password != null && password.equals(inputPassword) && isActive;
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ") - " + getRoleDisplayName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User user = (User) obj;
        return userId == user.userId ||
                (username != null && username.equals(user.username));
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}