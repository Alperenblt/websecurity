package com.alperen.websecurity.repository;

import com.alperen.websecurity.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken rt set rt.revokedAt = ?2 where rt.user.id = ?1 and rt.revokedAt is null")
    int revokeAllActiveForUser(Long userId, Instant revokedAt);
}
