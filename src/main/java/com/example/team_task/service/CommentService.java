package com.example.team_task.service;

import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.example.team_task.dto.comment.CommentResponse;
import com.example.team_task.dto.error.CommentNotFoundException;
import com.example.team_task.entity.Comment;
import com.example.team_task.repository.CommentRepository;

import jakarta.transaction.Transactional;

@Service
public class CommentService {
    private CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Transactional
    @Caching(put = @CachePut(value = "comments", key = "#result.id"), evict = @CacheEvict(value = "comments", allEntries = true))
    public CommentResponse saveComment(Comment comment) {
        return mapToResponse(commentRepository.save(comment));
    }

    @Cacheable(value = "comments", key = "#id")
    public CommentResponse findById(Long id) {
        return mapToResponse(commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id)));
    }

    @Caching(evict = {
            @CacheEvict(value = "comments", key = "#id"),
            @CacheEvict(value = "comments", allEntries = true)
    })
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .userId(comment.getUser().getId())
                .taskId(comment.getTask().getId())
                .userName(comment.getUser().getName())
                .createdAt(comment.getCreatedAt())
                .build();
    }

}
