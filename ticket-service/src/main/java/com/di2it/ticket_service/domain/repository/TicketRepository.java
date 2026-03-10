package com.di2it.ticket_service.domain.repository;

import com.di2it.ticket_service.domain.entity.Ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Page<Ticket> findByClientId(UUID clientId, Pageable pageable);

    Page<Ticket> findByTenantId(UUID tenantId, Pageable pageable);

    boolean existsByIdAndClientId(UUID id, UUID clientId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
