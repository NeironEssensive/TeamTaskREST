package com.example.team_task.integration;

import com.example.team_task.dto.auth.RefreshTokenData;
import com.example.team_task.service.RateLimitService;
import com.example.team_task.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void redisConnection_ContainerRunning_CanStoreAndRetrieveData() {
        redisTemplate.opsForValue().set("test-key", "test-value");

        Object value = redisTemplate.opsForValue().get("test-key");

        assertThat(value).isEqualTo("test-value");
    }

    @Test
    void refreshTokenService_CreateAndValidate_ReturnsCorrectData() {
        String tokenId = refreshTokenService.createRefreshToken(1L, "testuser", "USER");

        RefreshTokenData data = refreshTokenService.validateRefreshToken(tokenId);

        assertThat(data).isNotNull();
        assertThat(data.getUserId()).isEqualTo(1L);
        assertThat(data.getUsername()).isEqualTo("testuser");
        assertThat(data.getRole()).isEqualTo("USER");
    }

    @Test
    void refreshTokenService_DeleteToken_TokenBecomesInvalid() {
        String tokenId = refreshTokenService.createRefreshToken(2L, "deleteuser", "ADMIN");

        refreshTokenService.deleteRefreshToken(tokenId);

        org.springframework.security.authentication.BadCredentialsException exception = null;
        try {
            refreshTokenService.validateRefreshToken(tokenId);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            exception = e;
        }

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("Invalid or expired refresh token");
    }

    @Test
    void refreshTokenService_CreateMultipleTokens_EachIsUnique() {
        String tokenId1 = refreshTokenService.createRefreshToken(1L, "user1", "USER");
        String tokenId2 = refreshTokenService.createRefreshToken(1L, "user1", "USER");

        assertThat(tokenId1).isNotEqualTo(tokenId2);

        RefreshTokenData data1 = refreshTokenService.validateRefreshToken(tokenId1);
        RefreshTokenData data2 = refreshTokenService.validateRefreshToken(tokenId2);

        assertThat(data1.getUserId()).isEqualTo(data2.getUserId());
    }

    @Test
    void refreshTokenService_DeleteAllTokens_RemovesOnlyMatchingUserTokens() {
        String token1 = refreshTokenService.createRefreshToken(1L, "user1", "USER");
        String token2 = refreshTokenService.createRefreshToken(1L, "user1", "USER");
        String token3 = refreshTokenService.createRefreshToken(2L, "user2", "USER");

        refreshTokenService.deleteAllTokens(1L);

        org.springframework.security.authentication.BadCredentialsException exception1 = null;
        try {
            refreshTokenService.validateRefreshToken(token1);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            exception1 = e;
        }

        org.springframework.security.authentication.BadCredentialsException exception2 = null;
        try {
            refreshTokenService.validateRefreshToken(token2);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            exception2 = e;
        }

        assertThat(exception1).isNotNull();
        assertThat(exception2).isNotNull();

        RefreshTokenData data3 = refreshTokenService.validateRefreshToken(token3);
        assertThat(data3).isNotNull();
        assertThat(data3.getUserId()).isEqualTo(2L);
    }

    @Test
    void rateLimitService_FirstAttempt_NotRateLimited() {
        boolean limited = rateLimitService.isLoginRateLimited();

        assertThat(limited).isFalse();
    }

    @Test
    void rateLimitService_LoginCounterIncrements() {
        rateLimitService.isLoginRateLimited();
        rateLimitService.isLoginRateLimited();

        String key = "rate_limit:login:127.0.0.1";
        Long attempts = (Long) redisTemplate.opsForValue().get(key);

        assertThat(attempts).isNotNull();
        assertThat(attempts).isEqualTo(2L);
    }

    @Test
    void rateLimitService_ResetLoginAttempts_ClearsCounter() {
        rateLimitService.isLoginRateLimited();
        rateLimitService.isLoginRateLimited();

        rateLimitService.resetLoginAttempts();

        String key = "rate_limit:login:127.0.0.1";
        Long attempts = (Long) redisTemplate.opsForValue().get(key);

        assertThat(attempts).isNull();
    }

    @Test
    void rateLimitService_RegisterCounterIndependentFromLogin() {
        rateLimitService.isLoginRateLimited();
        rateLimitService.isLoginRateLimited();
        rateLimitService.isLoginRateLimited();

        boolean registerLimited = rateLimitService.isRegisterRateLimited();

        assertThat(registerLimited).isFalse();
    }
}