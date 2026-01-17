package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a refresh token in the database.
 *
 * <p>Refresh tokens are long-lived tokens used to obtain new access tokens without requiring the
 * user to log in again.
 *
 * <h2>Security Features:</h2>
 *
 * <ul>
 *   <li>Tokens are stored hashed in the database
 *   <li>Each token is bound to a specific user
 *   <li>Tokens have expiration dates
 *   <li>Used tokens are marked for rotation/invalidation
 * </ul>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (revoked == null) {
            revoked = false;
        }
    }
}
