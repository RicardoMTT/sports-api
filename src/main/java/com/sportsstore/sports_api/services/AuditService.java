package com.sportsstore.sports_api.services;

import com.sportsstore.sports_api.domain.entities.AuditLog;
import com.sportsstore.sports_api.repository.AuditLogRepository;
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
