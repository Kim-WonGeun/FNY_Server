package com.mailservice.fny.mailbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "labels")
public class Label {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false, length = 100)
    private String labelName;

    @Column(nullable = false, length = 50)
    private String labelCode;

    @Column(length = 50)
    private String labelType;

    @Column(length = 20)
    private String color;

    protected Label() {
    }

    public String getId() {
        return id;
    }

    public String getLabelName() {
        return labelName;
    }

    public String getLabelCode() {
        return labelCode;
    }

    public String getLabelType() {
        return labelType;
    }

    public String getColor() {
        return color;
    }
}
