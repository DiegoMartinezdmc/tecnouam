package com.example.tecnouam;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtService {
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final long LIFETIME_SECONDS = 3600;
    private final byte[] secret;
    private final ObjectMapper mapper;

    public JwtService(@Value("${JWT_SECRET}") String secret, ObjectMapper mapper) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) throw new IllegalArgumentException("JWT_SECRET debe contener al menos 32 bytes UTF-8");
        this.mapper = mapper;
    }

    public long lifetimeSeconds() { return LIFETIME_SECONDS; }

    public String create(String username) {
        try {
            long now = Instant.now().getEpochSecond();
            String header = encode(HEADER.getBytes(StandardCharsets.UTF_8));
            String payload = encode(mapper.writeValueAsBytes(Map.of(
                "sub", username, "iat", now, "exp", now + LIFETIME_SECONDS)));
            String content = header + "." + payload;
            return content + "." + encode(sign(content));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo emitir el token", ex);
        }
    }

    public String verifyAndGetSubject(String token) {
        try {
            if (token.length() > 4096) return null;
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3 || !HEADER.equals(new String(decode(parts[0]), StandardCharsets.UTF_8))) return null;
            byte[] expected = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected, decode(parts[2]))) return null;
            Map<?, ?> claims = mapper.readValue(decode(parts[1]), Map.class);
            Object subject = claims.get("sub");
            Object expiration = claims.get("exp");
            Object issuedAt = claims.get("iat");
            long now = Instant.now().getEpochSecond();
            if (!(subject instanceof String username) || username.isBlank()
                    || !(expiration instanceof Number exp) || !(issuedAt instanceof Number iat)
                    || exp.longValue() <= now || iat.longValue() > now
                    || exp.longValue() - iat.longValue() > LIFETIME_SECONDS) return null;
            return username;
        } catch (Exception ex) {
            return null;
        }
    }

    private byte[] sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(content.getBytes(StandardCharsets.US_ASCII));
    }

    private String encode(byte[] bytes) { return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private byte[] decode(String value) { return Base64.getUrlDecoder().decode(value); }
}
