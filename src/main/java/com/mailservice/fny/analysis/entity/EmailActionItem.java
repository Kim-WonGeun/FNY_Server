package com.mailservice.fny.analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_action_items")
public class EmailActionItem {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private EmailAnalysis analysis;

    @Column(nullable = false, length = 500)
    private String actionText;

    @Column(length = 50)
    private String actionType;

    @Column(length = 10)
    private String priorityLevel;

    private LocalDateTime dueAt;

    @Column(nullable = false)
    private boolean isCompleted;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected EmailActionItem() {
    }

    public EmailActionItem(
            String id,
            EmailAnalysis analysis,
            String actionText,
            String actionType,
            String priorityLevel,
            LocalDateTime dueAt
    ) {
        this.id = id;
        this.analysis = analysis;
        this.actionText = actionText;
        this.actionType = actionType;
        this.priorityLevel = priorityLevel;
        this.dueAt = dueAt;
        this.isCompleted = false;
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

    public String getId() {
        return id;
    }

    public String getActionText() {
        return actionText;
    }

    public String getActionType() {
        return actionType;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
