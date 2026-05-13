package com.musicstore.music_api.domain.entities;


import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token único opaco — no es JWT, es un UUID aleatorio
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    // A qué usuario pertenece
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Cuándo expira
    @Column(nullable = false)
    private Instant expiresAt;

    // Si fue revocado explícitamente (logout)
    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshToken() {}

    // Getters y setters
    public Long getId()                    { return id; }
    public String getToken()               { return token; }
    public void setToken(String token)     { this.token = token; }
    public User getUser()                  { return user; }
    public void setUser(User user)         { this.user = user; }
    public Instant getExpiresAt()          { return expiresAt; }
    public void setExpiresAt(Instant e)    { this.expiresAt = e; }
    public boolean isRevoked()             { return revoked; }
    public void setRevoked(boolean r)      { this.revoked = r; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

}

