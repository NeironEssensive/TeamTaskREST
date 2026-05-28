package com.example.team_task.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.team_task.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long>  {
    List<Task> findByUserId(Long userId);
}
