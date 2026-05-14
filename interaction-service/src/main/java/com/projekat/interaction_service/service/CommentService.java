package com.projekat.interaction_service.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.repository.CommentRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ObjectMapper objectMapper;

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

    public Comment patchComment(Long id, String patchJson) {
        try {
            Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

            JsonPatch patch = JsonPatch.fromJson(objectMapper.readTree(patchJson));

            JsonNode patched = patch.apply(objectMapper.convertValue(existingComment, JsonNode.class));

            Comment result = objectMapper.treeToValue(patched, Comment.class);

            return commentRepository.save(result);

        } catch (Exception e) {
            throw new RuntimeException("Patch failed: " + e.getMessage(), e);
        }   
    }   

    public Page<Comment> getCommentsByReport(Long reportId, Pageable pageable) {
        return commentRepository.findByReportId(reportId, pageable);
    }

    public List<Comment> searchComments(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        return commentRepository.searchByKeyword(keyword);
    }
}
