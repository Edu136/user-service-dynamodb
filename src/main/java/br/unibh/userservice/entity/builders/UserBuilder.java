package br.unibh.userservice.entity.builders;

import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserRole;
import br.unibh.userservice.entity.UserState;

import java.time.LocalDateTime;

public class UserBuilder {

    private String id;
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private UserState status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserBuilder id(String id) {
        this.id = id;
        return this;
    }

    public UserBuilder username(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder role(UserRole role) {
        this.role = role;
        return this;
    }

    public UserBuilder status(UserState status) {
        this.status = status;
        return this;
    }

    public UserBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UserBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public User build() {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role != null ? role : UserRole.USER);
        user.setStatus(status != null ? status : UserState.ACTIVE);
        user.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
        user.setUpdatedAt(updatedAt);
        return user;
    }
}
