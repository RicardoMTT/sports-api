package com.sportsstore.sports_api.repository;

import com.sportsstore.sports_api.domain.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}