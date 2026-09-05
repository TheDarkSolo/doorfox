package com.doorfox.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lead")
@Getter
@Setter
@NoArgsConstructor
public class Lead {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String phone;

    @Column(name = "instagram_handle")
    private String instagramHandle;

    private String budget;

    @Column(name = "project_type")
    private String projectType;

    private String timeline;
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "funnel_stage", nullable = false)
    private FunnelStage funnelStage = FunnelStage.CONTACTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
