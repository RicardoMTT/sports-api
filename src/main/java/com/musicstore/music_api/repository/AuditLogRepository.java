package com.musicstore.music_api.repository;

import com.musicstore.music_api.domain.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
