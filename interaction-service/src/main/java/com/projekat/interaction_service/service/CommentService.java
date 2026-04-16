package com.projekat.interaction_service.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.repository.CommentRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> getCommentsByReport(Long reportId) {
        // Vraca direktno listu entiteta iz baze
        return commentRepository.findByReportId(reportId);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment with ID " + id + " not found.");
        }
        commentRepository.deleteById(id);
    }

    public Comment updateComment(Long id, Comment commentDetails) {
        // da li postoji taj komentar
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment with ID " + id + " not found."));
        
        comment.setText(commentDetails.getText());
        
        return commentRepository.save(comment);
    }
}
