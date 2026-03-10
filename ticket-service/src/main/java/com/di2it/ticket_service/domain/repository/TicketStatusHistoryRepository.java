package com.di2it.ticket_service.domain.repository;

import com.di2it.ticket_service.domain.entity.TicketStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistory, UUID> {

    List<TicketStatusHistory> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}
