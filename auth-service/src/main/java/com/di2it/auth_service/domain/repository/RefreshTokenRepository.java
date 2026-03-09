package com.di2it.auth_service.domain.repository;

import com.di2it.auth_service.domain.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByTokenHashAndRevokedFalseAndExpiresAtAfter(
            String tokenHash,
            LocalDateTime expiresAt);

    List<RefreshToken> findByUser_Id(UUID userId);

    void deleteByUser_Id(UUID userId);
}
