package com.musicstore.music_api.services;

import com.musicstore.music_api.domain.entities.IdempotencyKey;
import com.musicstore.music_api.exception.IdempotencyConflictException;
import com.musicstore.music_api.repository.IdempotencyKeyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository keyRepository;

    public IdempotencyService(IdempotencyKeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    // REQUIRES_NEW asegura que el "candado" se guarde en BD inmediatamente,
    // independientemente de lo que pase después en el checkout.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyKey createOrReturnLock(String headerKey) {
        Optional<IdempotencyKey> existingKey = keyRepository.findByKeyValue(headerKey);

        if (existingKey.isPresent()) {
            IdempotencyKey key = existingKey.get();
            if ("PROCESSING".equals(key.getStatus())) {
                throw new IdempotencyConflictException("Esta transacción ya está en proceso. Por favor, espere.");
            }
            // Si es COMPLETED, devolvemos la llave para que el Controller reutilice el orderId
            return key;
        }

        try {
            // Si no existe, la creamos bloqueando futuros intentos
            IdempotencyKey newKey = new IdempotencyKey(headerKey, "PROCESSING");
            return keyRepository.save(newKey);
        } catch (DataIntegrityViolationException e) {
            // Condición de carrera: Dos hilos intentaron insertar al mismo milisegundo exacto.
            // SQL Server lanzará un error de llave única.
            throw new IdempotencyConflictException("Transacción concurrente detectada. Por favor, intente de nuevo.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsCompleted(String headerKey, Long orderId) {
        keyRepository.findByKeyValue(headerKey).ifPresent(key -> {
            key.setStatus("COMPLETED");
            key.setOrderId(orderId);
            keyRepository.save(key);
        });
    }
}

