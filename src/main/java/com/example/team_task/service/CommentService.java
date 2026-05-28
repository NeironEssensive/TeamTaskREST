package com.example.team_task.service;

import org.springframework.stereotype.Service;

import com.example.team_task.dto.comment.CommentResponse;
import com.example.team_task.dto.error.CommentNotFoundException;
import com.example.team_task.entity.Comment;
import com.example.team_task.repository.CommentRepository;

import jakarta.transaction.Transactional;

@Service
public class CommentService {
    private CommentRepository commentRepository;
    public CommentService(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }
    @Transactional
    public CommentResponse saveComment(Comment comment){
        return mapToResponse(commentRepository.save(comment));
    }   

    public CommentResponse findById(Long id){
        return mapToResponse(commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id)));
    }

    public void deleteComment(Long id){
        commentRepository.deleteById(id);
    }

    

    public CommentResponse mapToResponse(Comment comment){
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
