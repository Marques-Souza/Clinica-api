package com.marquesdev.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "admin_password_reset_audits")
public class AdminPasswordResetAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor_user_id", nullable = false)
    private UUID actorUserId;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AdminPasswordResetAudit(UUID actorUserId, UUID targetUserId) {
        this.actorUserId = actorUserId;
        this.targetUserId = targetUserId;
        this.createdAt = LocalDateTime.now();
    }
}
