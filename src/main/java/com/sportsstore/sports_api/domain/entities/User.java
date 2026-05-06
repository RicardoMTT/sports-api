package com.sportsstore.sports_api.domain.entities;

import com.sportsstore.sports_api.domain.enums.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Entity
@Table(name = "users")
public class User extends Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    // --- Constructores ---
    public User() {}

    public User(String firstName, String lastName, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // --- Getters y Setters Básicos ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // =========================================================
    // MÉTODOS DE LA INTERFAZ UserDetails (Spring Security)
    // =========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security espera que los roles empiecen con "ROLE_"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        // Para nosotros, el identificador único de login es el correo
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Podrías tener un campo booleano en BD para controlar esto
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Útil si quieres bloquear cuentas tras intentos fallidos
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // Útil si requieres confirmación por correo antes de activar la cuenta
    }
}
