package com.example.team_task.service;

import com.example.team_task.dto.auth.RefreshTokenData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenTtl", 604800000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void createRefreshToken_ValidInput_ReturnsTokenId() {
        String tokenId = refreshTokenService.createRefreshToken(1L, "testuser", "USER");

        assertThat(tokenId).isNotNull();
        assertThat(tokenId).isNotEmpty();
        verify(valueOperations).set(anyString(), any(RefreshTokenData.class), eq(Duration.ofMillis(604800000L)));
    }

    @Test
    void createRefreshToken_DifferentUsers_ReturnsDifferentTokenIds() {
        String tokenId1 = refreshTokenService.createRefreshToken(1L, "user1", "USER");
        String tokenId2 = refreshTokenService.createRefreshToken(2L, "user2", "ADMIN");

        assertThat(tokenId1).isNotEqualTo(tokenId2);
    }

    @Test
    void createRefreshToken_StoresCorrectDataInRedis() {
        refreshTokenService.createRefreshToken(42L, "john", "ADMIN");

        verify(valueOperations).set(anyString(), any(RefreshTokenData.class), any(Duration.class));
    }

    @Test
    void validateRefreshToken_ValidToken_ReturnsRefreshTokenData() {
        RefreshTokenData expectedData = RefreshTokenData.builder()
                .userId(1L)
                .username("testuser")
                .role("USER")
                .build();

        when(valueOperations.get("refresh_token:valid-token-id")).thenReturn(expectedData);

        RefreshTokenData result = refreshTokenService.validateRefreshToken("valid-token-id");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void validateRefreshToken_ExpiredToken_ThrowsBadCredentialsException() {
        when(valueOperations.get("refresh_token:expired-token")).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("expired-token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void validateRefreshToken_NonExistentToken_ThrowsBadCredentialsException() {
        when(valueOperations.get("refresh_token:nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("nonexistent"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deleteRefreshToken_ValidToken_DeletesFromRedis() {
        refreshTokenService.deleteRefreshToken("token-to-delete");

        verify(redisTemplate).delete("refresh_token:token-to-delete");
    }

    @Test
    void deleteRefreshToken_AnyTokenId_CallsRedisDelete() {
        refreshTokenService.deleteRefreshToken("any-token-id");

        verify(redisTemplate).delete("refresh_token:any-token-id");
    }

    @Test
    void deleteAllTokens_UserHasTokens_DeletesMatchingTokens() {
        RefreshTokenData userData = RefreshTokenData.builder()
                .userId(1L)
                .username("testuser")
                .role("USER")
                .build();

        RefreshTokenData otherUserData = RefreshTokenData.builder()
                .userId(2L)
                .username("otheruser")
                .role("USER")
                .build();

        Set<String> allKeys = Set.of(
                "refresh_token:token-1",
                "refresh_token:token-2",
                "refresh_token:other-token"
        );

        when(redisTemplate.keys("refresh_token:*")).thenReturn(allKeys);
        when(valueOperations.get("refresh_token:token-1")).thenReturn(userData);
        when(valueOperations.get("refresh_token:token-2")).thenReturn(userData);
        when(valueOperations.get("refresh_token:other-token")).thenReturn(otherUserData);

        refreshTokenService.deleteAllTokens(1L);

        verify(redisTemplate).delete("refresh_token:token-1");
        verify(redisTemplate).delete("refresh_token:token-2");
        verify(redisTemplate, never()).delete("refresh_token:other-token");
    }

    @Test
    void deleteAllTokens_UserHasNoTokens_DeletesNothing() {
        when(redisTemplate.keys("refresh_token:*")).thenReturn(Set.of());

        refreshTokenService.deleteAllTokens(1L);

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void deleteAllTokens_NullKeys_DeletesNothing() {
        when(redisTemplate.keys("refresh_token:*")).thenReturn(null);

        refreshTokenService.deleteAllTokens(1L);

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void deleteAllTokens_NoMatchingTokens_DeletesNothing() {
        RefreshTokenData otherUserData = RefreshTokenData.builder()
                .userId(2L)
                .username("otheruser")
                .role("USER")
                .build();

        Set<String> allKeys = Set.of("refresh_token:other-token");

        when(redisTemplate.keys("refresh_token:*")).thenReturn(allKeys);
        when(valueOperations.get("refresh_token:other-token")).thenReturn(otherUserData);

        refreshTokenService.deleteAllTokens(1L);

        verify(redisTemplate, never()).delete(anyString());
    }
}