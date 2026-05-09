package com.sportsstore.sports_api.services;

import com.sportsstore.sports_api.domain.entities.RefreshToken;
import com.sportsstore.sports_api.domain.entities.User;
import com.sportsstore.sports_api.exception.ResourceNotFoundException;
import com.sportsstore.sports_api.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service  // ← faltaba esta anotación — sin ella Spring no registra el bean
public class RefreshTokenService {

    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.revokeAllByUserId(user.getId());

        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        rt.setRevoked(false);

        return refreshTokenRepository.save(rt);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token no encontrado"));

        if (rt.isRevoked()) {
            throw new IllegalStateException("Refresh token revocado");
        }
        if (rt.isExpired()) {
            throw new IllegalStateException("Refresh token expirado. Inicia sesión de nuevo.");
        }

        return rt;
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked();
    }
}
