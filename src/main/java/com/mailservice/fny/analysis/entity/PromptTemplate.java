package com.mailservice.fny.analysis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_templates")
public class PromptTemplate {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 80)
    private String promptCode;

    @Column(nullable = false, length = 100)
    private String promptName;

    @Column(nullable = false, length = 50)
    private String promptType;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, length = 100)
    private String modelName;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String roleContent;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String policyContent;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String guideContent;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String outputContent;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected PromptTemplate() {
    }

    public String getPromptCode() {
        return promptCode;
    }

    public int getVersion() {
        return version;
    }

    public String getModelName() {
        return modelName;
    }

    public String getRoleContent() {
        return roleContent;
    }

    public String getPolicyContent() {
        return policyContent;
    }

    public String getGuideContent() {
        return guideContent;
    }

    public String getOutputContent() {
        return outputContent;
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
}
