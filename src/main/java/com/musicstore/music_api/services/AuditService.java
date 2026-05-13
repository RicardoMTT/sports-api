package com.musicstore.music_api.services;

import com.musicstore.music_api.domain.entities.AuditLog;
import com.musicstore.music_api.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(String operation, String reason) {
        auditLogRepository.save(new AuditLog(operation, reason));
    }
}

