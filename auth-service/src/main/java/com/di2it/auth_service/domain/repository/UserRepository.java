package com.di2it.auth_service.domain.repository;

import com.di2it.auth_service.domain.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByTenant_Id(UUID tenantId);

    Optional<User> findByEmailAndTenant_Id(String email, UUID tenantId);
}
