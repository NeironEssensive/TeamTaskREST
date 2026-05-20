package com.example.team_task.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.team_task.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Object findFirstByName(String name);
    Optional<User> findByName(String name);

} 
