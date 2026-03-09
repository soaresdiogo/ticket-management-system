package com.di2it.auth_service.domain.repository;

import com.di2it.auth_service.domain.entity.Tenant;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByEmail(String email);

    boolean existsByEmail(String email);
}
