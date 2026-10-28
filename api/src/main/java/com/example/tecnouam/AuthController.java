package com.example.tecnouam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserDetailsService users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserDetailsService users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            UserDetails user = users.loadUserByUsername(request.username());
            if (encoder.matches(request.password(), user.getPassword()))
                return new LoginResponse(jwt.create(user.getUsername()), "Bearer", jwt.lifetimeSeconds());
        } catch (UsernameNotFoundException ignored) {
            // Se devuelve el mismo error para usuario y contraseña incorrectos.
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, String tokenType, long expiresIn) {}
}
