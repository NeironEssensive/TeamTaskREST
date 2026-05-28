package com.example.team_task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.team_task.entity.Comment;
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
}
