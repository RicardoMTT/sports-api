package com.musicstore.music_api.controller;

import com.musicstore.music_api.domain.dtos.AuthenticationRequest;
import com.musicstore.music_api.domain.dtos.AuthenticationResponse;
import com.musicstore.music_api.domain.dtos.RefreshRequest;
import com.musicstore.music_api.domain.dtos.RegisterRequest;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {


    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authenticationService.refreshAccessToken(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal User currentUser) {
        authenticationService.logout(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}

