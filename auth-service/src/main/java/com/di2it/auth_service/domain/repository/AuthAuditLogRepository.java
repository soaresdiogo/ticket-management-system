package com.di2it.auth_service.domain.repository;

import com.di2it.auth_service.domain.entity.AuthAuditLog;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, UUID> {

    List<AuthAuditLog> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
