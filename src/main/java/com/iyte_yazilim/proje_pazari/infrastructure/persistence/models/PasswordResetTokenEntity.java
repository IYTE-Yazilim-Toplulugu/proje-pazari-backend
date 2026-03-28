package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import com.github.f4b6a3.ulid.Ulid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Password reset token persistence entity.
 *
 * <p>Stores single-use password reset tokens. Kept separate from UserEntity following DDD
 * principles. Tokens expire after 1 hour and are invalidated on use.
 */
@Entity
@Table(
        name = "password_reset_tokens",
        // idx_prt_user_id  → for deleteByUserId() cleanup queries
        indexes = @Index(name = "idx_prt_user_id", columnList = "userId"),
        // uq_prt_token → implicit unique index on token for O(log n) findByToken() lookups
        uniqueConstraints = @UniqueConstraint(name = "uq_prt_token", columnNames = "token"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenEntity {

    @Id
    @Column(length = 26)
    private String id;

    @Column(nullable = false)
    private String userId;

    /** Required for audit/compliance. Not used for user lookup after token creation. */
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = Ulid.fast().toString();
        }
        createdAt = LocalDateTime.now();
    }
}
