package com.musicstore.music_api.services;

import com.musicstore.music_api.domain.dtos.AuthenticationRequest;
import com.musicstore.music_api.domain.dtos.AuthenticationResponse;
import com.musicstore.music_api.domain.dtos.RegisterRequest;
import com.musicstore.music_api.domain.dtos.UserDto;
import com.musicstore.music_api.domain.entities.RefreshToken;
import com.musicstore.music_api.domain.enums.Role;
import com.musicstore.music_api.domain.entities.User;
import com.musicstore.music_api.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;  // ← nuevo

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService  // ← nuevo
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;  // ← nuevo
    }

    public AuthenticationResponse register(RegisterRequest request) {
        // 1. Validar que el correo no exista ya en la base de datos
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema.");
        }

        // 2. Crear el objeto User, encriptando la contraseña y asignando rol por defecto
        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.CUSTOMER // Todos los que se registran por la web son clientes
        );

        // 3. Guardar en SQL Server
        userRepository.save(user);

        // 4. Generar el JWT para este nuevo usuario
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        UserDto userDto     = new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());

        return new AuthenticationResponse(jwtToken, refreshToken, userDto);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Spring Security hace el trabajo pesado aquí.
        // Va a la BD, saca el hash de la contraseña, lo compara con el texto plano que llegó, etc.
        // Si las credenciales están mal, lanzará una excepción (BadCredentialsException) que retorna un 403.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. Si llegamos a esta línea, el usuario existe y la contraseña es correcta
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(); // Ya sabemos que existe por el paso anterior

        String accessToken  = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        UserDto userDto     = new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());


        return new AuthenticationResponse(accessToken,refreshToken,userDto);
    }


    // Nuevo método — renovar el access token sin login
    public AuthenticationResponse refreshAccessToken(String refreshTokenStr) {
        RefreshToken rt    = refreshTokenService.validateRefreshToken(refreshTokenStr);
        User user          = rt.getUser();
        String newAccess   = jwtService.generateToken(user);
        UserDto userDto    = new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());

        // El refresh token se mantiene — no se rota en cada llamada
        return new AuthenticationResponse(newAccess, refreshTokenStr, userDto);
    }

    // Logout — revocar el refresh token activo
    public void logout(Long userId) {
        refreshTokenService.revokeAllUserTokens(userId);
    }

}

