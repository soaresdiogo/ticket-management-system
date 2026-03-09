package com.di2it.auth_service.domain.repository;

import com.di2it.auth_service.domain.entity.MfaCode;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MfaCodeRepository extends JpaRepository<MfaCode, UUID> {

    Optional<MfaCode> findByUser_IdAndUsedFalseAndExpiresAtAfter(
            UUID userId,
            LocalDateTime expiresAt);
}
