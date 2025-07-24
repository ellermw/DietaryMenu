package com.hospital.dietary.models;

import com.hospital.dietary.data.entities.UserEntity;

public class User {
    private UserEntity entity;

    public User() {
        this.entity = new UserEntity();
    }

    public User(UserEntity entity) {
        this.entity = entity;
    }

    public long getUserId() { return entity.getUserId(); }
    public void setUserId(long id) { entity.setUserId(id); }

    public String getUsername() { return entity.getUsername(); }
    public void setUsername(String username) { entity.setUsername(username); }

    public String getPassword() { return entity.getPassword(); }
    public void setPassword(String password) { entity.setPassword(password); }

    public String getFullName() { return entity.getFullName(); }
    public void setFullName(String name) { entity.setFullName(name); }

    public String getRole() { return entity.getRole(); }
    public void setRole(String role) { entity.setRole(role); }

    public boolean isActive() { return entity.isActive(); }
    public void setActive(boolean active) { entity.setActive(active); }

    public boolean isMustChangePassword() { return entity.isMustChangePassword(); }
    public void setMustChangePassword(boolean must) { entity.setMustChangePassword(must); }

    public UserEntity toEntity() { return entity; }
    public static User fromEntity(UserEntity entity) { return new User(entity); }
}