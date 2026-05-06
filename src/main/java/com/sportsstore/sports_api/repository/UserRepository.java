package com.sportsstore.sports_api.repository;

import com.sportsstore.sports_api.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Este método es crucial para el Login
    Optional<User> findByEmail(String email);

    // Útil para validaciones al registrar un nuevo usuario
    boolean existsByEmail(String email);
}
