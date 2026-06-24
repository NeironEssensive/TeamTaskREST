package com.example.team_task.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET_KEY = "test-secret-key-for-jwt-that-is-at-least-32-bytes-long";
    private static final long EXPIRATION_MS = 3600000;

    private JwtService jwtService;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_KEY, EXPIRATION_MS);
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generateToken_ValidUsernameAndRole_ReturnsNonEmptyToken() {
        String token = jwtService.generateToken("testuser", "USER");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateToken_ContainsSubjectAsUsername() {
        String token = jwtService.generateToken("john_doe", "USER");

        String extractedUsername = jwtService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("john_doe");
    }

    @Test
    void generateToken_ContainsRoleClaim() {
        String token = jwtService.generateToken("testuser", "ADMIN");

        String extractedRole = jwtService.extractRole(token);

        assertThat(extractedRole).isEqualTo("ADMIN");
    }

    @Test
    void generateToken_WithDifferentRoles_ExtractsCorrectRole() {
        String userToken = jwtService.generateToken("testuser", "USER");
        String adminToken = jwtService.generateToken("admin", "ADMIN");

        assertThat(jwtService.extractRole(userToken)).isEqualTo("USER");
        assertThat(jwtService.extractRole(adminToken)).isEqualTo("ADMIN");
    }

    @Test
    void extractUsername_ValidToken_ReturnsCorrectUsername() {
        String username = "john_doe";
        String token = jwtService.generateToken(username, "USER");

        String result = jwtService.extractUsername(token);

        assertThat(result).isEqualTo(username);
    }

    @Test
    void extractRole_ValidToken_ReturnsCorrectRole() {
        String token = jwtService.generateToken("testuser", "ADMIN");

        String role = jwtService.extractRole(token);

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void isTokenValid_ValidToken_ReturnsTrue() {
        String token = jwtService.generateToken("testuser", "USER");

        boolean isValid = jwtService.isTokenValid(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        String expiredToken = createExpiredToken();

        boolean isValid = jwtService.isTokenValid(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_MalformedToken_ReturnsFalse() {
        String malformedToken = "this.is.not.a.valid.jwt.token";

        boolean isValid = jwtService.isTokenValid(malformedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_NullToken_ReturnsFalse() {
        boolean isValid = jwtService.isTokenValid(null);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_EmptyToken_ReturnsFalse() {
        boolean isValid = jwtService.isTokenValid("");

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_TokenWithInvalidSignature_ReturnsFalse() {
        SecretKey differentKey = Keys.hmacShaKeyFor("different-secret-key-32-bytes-long!!".getBytes(StandardCharsets.UTF_8));
        String tokenWithWrongSignature = Jwts.builder()
                .subject("testuser")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(EXPIRATION_MS)))
                .signWith(differentKey)
                .compact();

        boolean isValid = jwtService.isTokenValid(tokenWithWrongSignature);

        assertThat(isValid).isFalse();
    }

    @Test
    void generateToken_SetsExpirationInFuture() throws Exception {
        String token = jwtService.generateToken("testuser", "USER");

        boolean isValidAfterShortWait = jwtService.isTokenValid(token);
        assertThat(isValidAfterShortWait).isTrue();
    }

    @Test
    void generateToken_SameInput_ProducesDifferentTokens() {
        String token1 = jwtService.generateToken("testuser", "USER");
        Thread.sleep(1);
        String token2 = jwtService.generateToken("testuser", "USER");

        assertThat(token1).isNotEqualTo(token2);
    }

    private String createExpiredToken() {
        Instant expiredInstant = Instant.now().minusMillis(3600000);
        return Jwts.builder()
                .subject("testuser")
                .claim("role", "USER")
                .issuedAt(Date.from(expiredInstant.minusSeconds(3600)))
                .expiration(Date.from(expiredInstant))
                .signWith(key)
                .compact();
    }
}