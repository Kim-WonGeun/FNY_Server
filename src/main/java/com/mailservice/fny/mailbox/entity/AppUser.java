package com.mailservice.fny.mailbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @Column(length = 20)
    private String id;

    @Column(length = 100)
    private String displayName;

    @Column(length = 255)
    private String primaryEmail;

    @Column(length = 20)
    private String status;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected AppUser() {
    }

    public AppUser(String id, String displayName, String primaryEmail, String status, LocalDateTime lastLoginAt) {
        this.id = id;
        this.displayName = displayName;
        this.primaryEmail = primaryEmail;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateLoginProfile(String displayName, String primaryEmail) {
        this.displayName = displayName;
        this.primaryEmail = primaryEmail;
        this.status = "ACTIVE";
        this.lastLoginAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
