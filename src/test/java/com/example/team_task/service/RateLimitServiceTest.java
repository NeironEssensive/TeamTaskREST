package com.example.team_task.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimitService, "loginMaxAttempts", 3);
        ReflectionTestUtils.setField(rateLimitService, "registerMaxAttempts", 3);
        ReflectionTestUtils.setField(rateLimitService, "windowSeconds", 60);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    @Test
    void isLoginRateLimited_FirstAttempt_ReturnsFalse() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        boolean result = rateLimitService.isLoginRateLimited();

        assertThat(result).isFalse();
        verify(redisTemplate).expire(anyString(), eq(Duration.ofSeconds(60)));
    }

    @Test
    void isLoginRateLimited_UnderLimit_ReturnsFalse() {
        when(valueOperations.increment(anyString())).thenReturn(2L);

        boolean result = rateLimitService.isLoginRateLimited();

        assertThat(result).isFalse();
    }

    @Test
    void isLoginRateLimited_ExactlyAtLimit_ReturnsFalse() {
        when(valueOperations.increment(anyString())).thenReturn(3L);

        boolean result = rateLimitService.isLoginRateLimited();

        assertThat(result).isFalse();
    }

    @Test
    void isLoginRateLimited_ExceedsLimit_ReturnsTrue() {
        when(valueOperations.increment(anyString())).thenReturn(4L);

        boolean result = rateLimitService.isLoginRateLimited();

        assertThat(result).isTrue();
    }

    @Test
    void isLoginRateLimited_ManyAttempts_ReturnsTrue() {
        when(valueOperations.increment(anyString())).thenReturn(10L);

        boolean result = rateLimitService.isLoginRateLimited();

        assertThat(result).isTrue();
    }

    @Test
    void isRegisterRateLimited_FirstAttempt_ReturnsFalse() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        boolean result = rateLimitService.isRegisterRateLimited();

        assertThat(result).isFalse();
    }

    @Test
    void isRegisterRateLimited_ExceedsLimit_ReturnsTrue() {
        when(valueOperations.increment(anyString())).thenReturn(5L);

        boolean result = rateLimitService.isRegisterRateLimited();

        assertThat(result).isTrue();
    }

    @Test
    void resetLoginAttempts_DeletesKey() {
        rateLimitService.resetLoginAttempts();

        verify(redisTemplate).delete(anyString());
    }

    @Test
    void isLoginRateLimited_UsesClientIp() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimitService.isLoginRateLimited();

        verify(valueOperations).increment("rate_limit:login:192.168.1.1");
    }

    @Test
    void isRegisterRateLimited_UsesClientIp() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimitService.isRegisterRateLimited();

        verify(valueOperations).increment("rate_limit:register:192.168.1.1");
    }

    @Test
    void isLoginRateLimited_XForwardedForHeader_UsesProxyIp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.5");
        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimitService.isLoginRateLimited();

        verify(valueOperations).increment("rate_limit:login:10.0.0.5");
    }

    @Test
    void isLoginRateLimited_EmptyXForwardedFor_FallsBackToRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("172.16.0.1");
        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimitService.isLoginRateLimited();

        verify(valueOperations).increment("rate_limit:login:172.16.0.1");
    }

    @Test
    void isLoginRateLimited_LoginAndRegisterCounters_AreSeparate() {
        when(valueOperations.increment("rate_limit:login:192.168.1.1")).thenReturn(5L);
        when(valueOperations.increment("rate_limit:register:192.168.1.1")).thenReturn(1L);

        boolean loginLimited = rateLimitService.isLoginRateLimited();
        boolean registerLimited = rateLimitService.isRegisterRateLimited();

        assertThat(loginLimited).isTrue();
        assertThat(registerLimited).isFalse();
    }
}