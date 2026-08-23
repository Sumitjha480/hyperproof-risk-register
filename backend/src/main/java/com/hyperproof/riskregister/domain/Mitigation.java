package com.hyperproof.riskregister.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mitigations")
public class Mitigation {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "risk_id", nullable = false)
    private Risk risk;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private int effectiveness;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Mitigation() {
    }

    public Mitigation(String description, int effectiveness) {
        update(description, effectiveness);
    }

    public void update(String description, int effectiveness) {
        this.description = description;
        this.effectiveness = effectiveness;
    }

    void assignTo(Risk risk) {
        this.risk = risk;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Risk getRisk() {
        return risk;
    }

    public String getDescription() {
        return description;
    }

    public int getEffectiveness() {
        return effectiveness;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
