package com.example.team_task.service;

import org.springframework.stereotype.Service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final HttpServletRequest request;
    @Value("${rate-limit.login-max-attempts:3}")
    private int loginMaxAttempts;
    @Value("${rate-limit.register-max-attempts:3}")
    private int registerMaxAttempts;
    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;
    private static final String LOGIN_PREFIX = "rate_limit:login:";
    private static final String REGISTER_PREFIX = "rate_limit:register:";
     public boolean isLoginRateLimited() {
        return checkRateLimit(LOGIN_PREFIX, loginMaxAttempts);
    }

    public boolean isRegisterRateLimited() {
        return checkRateLimit(REGISTER_PREFIX, registerMaxAttempts);
    }

    public void resetLoginAttempts() {
        String key = LOGIN_PREFIX + getClientIp();
        redisTemplate.delete(key);
    }
    
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    private boolean checkRateLimit(String prefix, int maxAttempts) {
        String key = prefix + getClientIp();
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        return attempts != null && attempts > maxAttempts;
    }
}
