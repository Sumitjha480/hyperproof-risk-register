package com.hyperproof.riskregister.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "risks")
public class Risk {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RiskCategory category;

    @Column(nullable = false, length = 200)
    private String owner;

    @Column(nullable = false)
    private int likelihood;

    @Column(nullable = false)
    private int impact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RiskStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "risk", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Mitigation> mitigations = new ArrayList<>();

    protected Risk() {
    }

    public Risk(
            String title,
            String description,
            RiskCategory category,
            String owner,
            int likelihood,
            int impact,
            RiskStatus status
    ) {
        update(title, description, category, owner, likelihood, impact, status);
    }

    public void update(
            String title,
            String description,
            RiskCategory category,
            String owner,
            int likelihood,
            int impact,
            RiskStatus status
    ) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.owner = owner;
        this.likelihood = likelihood;
        this.impact = impact;
        this.status = status;
        touch();
    }

    public void addMitigation(Mitigation mitigation) {
        mitigation.assignTo(this);
        mitigations.add(mitigation);
        touch();
    }

    public void removeMitigation(Mitigation mitigation) {
        mitigations.remove(mitigation);
        touch();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public RiskCategory getCategory() {
        return category;
    }

    public String getOwner() {
        return owner;
    }

    public int getLikelihood() {
        return likelihood;
    }

    public int getImpact() {
        return impact;
    }

    public RiskStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<Mitigation> getMitigations() {
        return mitigations;
    }
}
