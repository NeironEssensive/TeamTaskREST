package com.example.team_task.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.example.team_task.dto.auth.RefreshTokenData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${jwt.refresh-token-ttl}")
    private long refreshTokenTtl;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    public String createRefreshToken(Long userId, String username, String role) {
        String tokenId = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + tokenId;

        RefreshTokenData data = RefreshTokenData.builder()
                .userId(userId)
                .username(username)
                .role(role)
                .build();

        redisTemplate.opsForValue().set(key, data, Duration.ofMillis(refreshTokenTtl));

        return tokenId;
    }

    public RefreshTokenData validateRefreshToken(String tokenId) {
        String key = REFRESH_TOKEN_PREFIX + tokenId;
        RefreshTokenData data = (RefreshTokenData) redisTemplate.opsForValue().get(key);

        if (data == null) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        return data;
    }

     public void deleteRefreshToken(String tokenId) {
        String key = REFRESH_TOKEN_PREFIX + tokenId;
        redisTemplate.delete(key);
    }

    public void deleteAllTokens(Long userId){
        var keys = redisTemplate.keys(REFRESH_TOKEN_PREFIX + "*");
        if(keys != null){
            for(String key : keys){
                RefreshTokenData data = (RefreshTokenData) redisTemplate.opsForValue().get(key);
                if(data != null && data.getUserId().equals(userId)){
                    redisTemplate.delete(key);
                }
            }
        }
    }

}
