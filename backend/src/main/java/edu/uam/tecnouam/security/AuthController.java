package edu.uam.tecnouam.security;

import edu.uam.tecnouam.web.Dtos.Login;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserDetailsService users;
  private final PasswordEncoder passwords;
  private final JwtEncoder encoder;

  public AuthController(UserDetailsService users, PasswordEncoder passwords, JwtEncoder encoder) {
    this.users = users;
    this.passwords = passwords;
    this.encoder = encoder;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@Valid @RequestBody Login input) {
    UserDetails user;
    try {
      user = users.loadUserByUsername(input.username());
    } catch (UsernameNotFoundException e) {
      throw invalid();
    }
    if (!passwords.matches(input.password(), user.getPassword())) throw invalid();
    var now = Instant.now();
    var roles =
        user.getAuthorities().stream().map(a -> a.getAuthority().replace("ROLE_", "")).toList();
    var claims =
        JwtClaimsSet.builder()
            .issuer("tecnouam")
            .subject(user.getUsername())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .claim("roles", roles)
            .build();
    String token =
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    return Map.of(
        "token",
        token,
        "tokenType",
        "Bearer",
        "expiresIn",
        3600,
        "username",
        user.getUsername(),
        "role",
        roles.getFirst());
  }

  private ResponseStatusException invalid() {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
  }
}
