package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.RefreshTokenEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.userId = :userId")
    void revokeAllUserTokens(@Param("userId") String userId);
}
