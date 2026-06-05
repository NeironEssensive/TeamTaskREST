package com.example.team_task.service;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import com.example.team_task.dto.error.UserNotFoundException;
import com.example.team_task.dto.user.UserResponse;
import com.example.team_task.entity.User;
import com.example.team_task.repository.UserRepository;

@Service
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "users", key = "#root.methodName + ':' + @userService.getCurrentUsername()")
    public UserResponse getCurrentUser() {
        String username = getCurrentUsername();
        User user = userRepository.findByName(username).orElseThrow(() -> new UserNotFoundException(username));
        return mapToResponse(user);
    }

    @Cacheable(value = "users", key = "'allUsers'")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        return authentication.getName();
    }

    @Cacheable(value = "users", key = "'findByName:' + #name")
    public UserResponse findByName(String name) {
        User user = userRepository.findByName(name).orElseThrow(() -> new UserNotFoundException(name));
        return mapToResponse(user);
    }

    @Cacheable(value = "users", key = "'findById:' + #id")
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "'findById:' + #id"),
            @CacheEvict(value = "users", key = "'findByName:' + #result.name", condition = "#result != null"),
            @CacheEvict(value = "users", key = "'allUsers'")
    })
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getCurrentUserEntity() {
        UserResponse current = getCurrentUser();
        return findById(current.getId());
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
