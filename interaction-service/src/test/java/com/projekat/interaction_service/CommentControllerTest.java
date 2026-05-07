package com.projekat.interaction_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import com.projekat.interaction_service.controller.CommentController;
import com.projekat.interaction_service.service.CommentService;

import org.springframework.http.MediaType;

import com.projekat.interaction_service.model.Comment;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @MockitoBean
    CommentService commentService;
    
    @Autowired
    MockMvcTester mockMvcTester;

    @Test
    void getCommentsByReportSuccessful() {

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setReportId(1L);
        comment1.setUserId(5L);
        comment1.setText("Prvi komentar");

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setReportId(1L);
        comment2.setUserId(6L);
        comment2.setText("Drugi komentar");

        when(commentService.getCommentsByReport(1L)).thenReturn(List.of(comment1, comment2));

        assertThat(
                mockMvcTester.get().uri("/api/comments/report/1"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.length()")
                .isEqualTo(2);

        verify(commentService).getCommentsByReport(1L);
    }

    @Test
    void createCommentErrorWhenInputDataIsInvalid() {
        
        String invalidRequest = """
        {
        "reportId": 4,
        "userId": 2,
        "text": ""
        }
        """;
        
        assertThat(mockMvcTester.post()
        .uri("/api/comments")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidRequest))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .isLenientlyEqualTo("""
        {
            "error": "validation_error",
            "message": "Comment message cannot be empty"
        }
        """);
    }

    @Test
    void updateCommentSuccessful() {

        String requestBody = """
                {
                    "reportId": 1,
                    "text": "Azurirani komentar",
                    "userId": 5
                }
                """;

        Comment updatedComment = new Comment();
        updatedComment.setId(1L);
        updatedComment.setReportId(1L);
        updatedComment.setUserId(5L);
        updatedComment.setText("Azurirani komentar");

        when(commentService.updateComment(eq(1L), any(Comment.class)))
                .thenReturn(updatedComment);

        assertThat(
                mockMvcTester.put()
                        .uri("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        {
                            "reportId": 1,
                            "text": "Azurirani komentar",
                            "userId": 5,
                            "id": 1
                        }
                        """);

        verify(commentService).updateComment(eq(1L), any(Comment.class));
    }

    @Test
    void updateCommentError() {

        String invalidRequest = """
        {
        "reportId": 1,
        "text": "",
        "userId": 5
        }
        """;
        
        assertThat(mockMvcTester.put()
        .uri("/api/comments/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidRequest))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .isLenientlyEqualTo("""
        {
            "error": "validation_error",
            "message": "Comment message cannot be empty"
        }
        """);
    }
}