package com.marquesdev.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "password_reset_codes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_password_reset_codes_user", columnNames = "user_id")
})
public class PasswordResetCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "code_hash", nullable = false, length = 200)
    private String codeHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    public PasswordResetCode(User user, String codeHash, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.user = user;
        this.codeHash = codeHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.failedAttempts = 0;
    }

    public void renew(String codeHash, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.codeHash = codeHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.failedAttempts = 0;
    }

    public void recordFailedAttempt() {
        failedAttempts++;
    }
}
