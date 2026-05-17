package com.musicstore.music_api.services;

import com.musicstore.music_api.domain.dtos.ChangePasswordRequest;
import com.musicstore.music_api.domain.dtos.UpdateProfileRequest;
import com.musicstore.music_api.domain.dtos.UserProfileResponse;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.exception.ResourceNotFoundException;
import com.musicstore.music_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retorna el perfil completo del usuario autenticado.
     */
    public UserProfileResponse getProfile(Long userId) {
        User user = findById(userId);
        return toResponse(user);
    }

    /**
     * Actualiza nombre y apellido del usuario autenticado.
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findById(userId);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);
        return toResponse(user);
    }

    /**
     * Cambia la contraseña verificando primero la contraseña actual.
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findById(userId);

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Verificar que la nueva contraseña sea diferente a la actual
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // ─── helpers ────────────────────────────────────────────────

    private User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
