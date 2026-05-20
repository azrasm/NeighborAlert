package com.projekat.interaction_service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import com.projekat.interaction_service.controller.CommentController;
import com.projekat.interaction_service.exception.ServiceUnavailableException;
import com.projekat.interaction_service.service.CommentService;

import org.springframework.http.MediaType;

import com.projekat.interaction_service.model.Comment;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Web sloj testovi za CommentController.
 *
 * @WebMvcTest učitava samo web kontekst — CommentService je mockovan.
 * Testira HTTP statuse, JSON odgovore i propagaciju izuzetaka.
 */
@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @MockitoBean
    CommentService commentService;
    
    @Autowired
    MockMvcTester mockMvcTester;

    @Test
    @DisplayName("GET /api/comments/report/{id} — vraća listu komentara")
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
    @DisplayName("POST /api/comments — 400 Bad Request za prazan tekst")
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
    @DisplayName("POST /api/comments — 503 kada report-service nije dostupan (Zadatak 5f)")
    void createComment_WhenReportServiceDown_Returns503() {

        when(commentService.saveComment(any(Comment.class)))
                .thenThrow(new ServiceUnavailableException(
                        "report-service trenutno nije dostupan. Pokušajte ponovo kasnije."));

        String requestBody = """
        {
            "reportId": 1,
            "userId": 5,
            "text": "Komentar"
        }
        """;

        assertThat(mockMvcTester.post()
                .uri("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyJson()
                .isLenientlyEqualTo("""
                {
                    "error": "service_unavailable",
                    "message": "report-service trenutno nije dostupan. Pokušajte ponovo kasnije."
                }
                """);
    }

    @Test
    @DisplayName("POST /api/comments — 503 kada user-service nije dostupan (Zadatak 5f)")
    void createComment_WhenUserServiceDown_Returns503() {

        when(commentService.saveComment(any(Comment.class)))
                .thenThrow(new ServiceUnavailableException(
                        "user-service trenutno nije dostupan. Pokušajte ponovo kasnije."));

        String requestBody = """
        {
            "reportId": 1,
            "userId": 5,
            "text": "Komentar"
        }
        """;

        assertThat(mockMvcTester.post()
                .uri("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .hasStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyJson()
                .isLenientlyEqualTo("""
                {
                    "error": "service_unavailable",
                    "message": "user-service trenutno nije dostupan. Pokušajte ponovo kasnije."
                }
                """);
    }

    @Test
    @DisplayName("PUT /api/comments/{id} — ažurira komentar")
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
    @DisplayName("PUT /api/comments/{id} — 400 za prazan tekst")
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