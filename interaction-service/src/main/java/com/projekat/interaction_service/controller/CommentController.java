package com.projekat.interaction_service.controller;
import org.springframework.web.bind.annotation.RestController;
import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comments", description = "Comments of reports APIs")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /******************* RUTE ******************/

    // GET za vravanje svih komentara odredjene prijave
    @GetMapping("/report/{reportId}")
    @Operation(summary = "Returns comments from a report")
    public List<Comment> getCommentsByReport(@PathVariable Long reportId) {
        // Kontroler salje List<Comment> koji se automatski pretvara u JSON
        return commentService.getCommentsByReport(reportId);
    }

    // POST ruta za dodavanje novog komentara
    @PostMapping
    @Operation(summary = "Creates a new comment")
    public Comment createComment(@Valid @RequestBody Comment newComment) {
        return commentService.saveComment(newComment);
    }

    // DELETE Brisanje komentara
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a comment")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        
        // Vracamo 204 No Content (za uspjesno brisanje)
       return ResponseEntity.noContent().build();
    }
    
    // PUT Izmjena kometara
    @PutMapping("/{id}")
    @Operation(summary = "Updates an existing comment")
    public Comment updateComment(@PathVariable Long id, @Valid @RequestBody Comment commentDetails) {
        return commentService.updateComment(id, commentDetails);
    }
    
}
