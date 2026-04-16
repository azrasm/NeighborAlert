package com.projekat.interaction_service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.repository.CommentRepository;
import com.projekat.interaction_service.service.CommentService;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;

    @BeforeEach
    void setUp() {
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setReportId(10L);
        testComment.setUserId(5L);
        testComment.setText("Originalni tekst");
    }

    // 1. Test za listanje komentara po reportu
    @Test
    void getCommentsByReport_ShouldReturnList() {
        when(commentRepository.findByReportId(10L)).thenReturn(Arrays.asList(testComment));

        List<Comment> result = commentService.getCommentsByReport(10L);

        assertEquals(1, result.size());
        assertEquals("Originalni tekst", result.get(0).getText());
        verify(commentRepository, times(1)).findByReportId(10L);
    }

    // 2. Test za spasavanje komentara
    @Test
    void saveComment_ShouldReturnSavedComment() {
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment saved = commentService.saveComment(testComment);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        verify(commentRepository, times(1)).save(testComment);
    }

    // 3. Test za brisanje komentara
    @Test
    void deleteComment_WhenExists_ShouldDelete() {
        when(commentRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> commentService.deleteComment(1L));

        verify(commentRepository, times(1)).deleteById(1L);
    }

    // 4. Test za neuspjedno brisanje (kad Id ne postoji)
    
    void deleteComment_WhenNotExists_ShouldThrowException() {
        when(commentRepository.existsById(99L)).thenReturn(false);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            commentService.deleteComment(99L);
        });

        assertEquals("Comment with ID 99 not found.", exception.getMessage());
        verify(commentRepository, never()).deleteById(anyLong());
    }

    // 5. Test za uspješan update
    @Test
    void updateComment_WhenExists_ShouldUpdateText() {
        Comment updatedDetails = new Comment();
        updatedDetails.setText("Novi, izmijenjeni tekst");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment result = commentService.updateComment(1L, updatedDetails);

        assertEquals("Novi, izmijenjeni tekst", result.getText());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    // 6. Test za NEUSPJEŠAN update (kad ID ne postoji)
    @Test
    void updateComment_WhenNotExists_ShouldThrowException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.updateComment(99L, new Comment());
        });

        verify(commentRepository, never()).save(any(Comment.class));
    }
}