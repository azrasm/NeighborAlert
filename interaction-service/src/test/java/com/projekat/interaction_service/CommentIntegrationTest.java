package com.projekat.interaction_service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.repository.CommentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CommentIntegrationTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void createCommentIntegrationTest() {

        String requestBody = """
                {
                    "reportId": 1,
                    "userId": 5,
                    "text": "Integration test komentar"
                }
                """;

        assertThat(
                mockMvcTester.post()
                        .uri("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.text")
                .isEqualTo("Integration test komentar");

        // verifikacija baze
        assertThat(commentRepository.findAll())
                .hasSize(4);
    }

    @Test
    void deleteCommentIntegrationTest() {

        Comment comment = new Comment();
        comment.setReportId(1L);
        comment.setUserId(5L);
        comment.setText("To be deleted");

        comment = commentRepository.save(comment);

        Long id = comment.getId();


        assertThat(
            mockMvcTester.delete().uri("/api/comments/" + id))
            .hasStatus(HttpStatus.NO_CONTENT);

        // verifikacija baze
        assertThat(commentRepository.existsById(id)).isFalse();
    }

    @Test
    void deleteCommentNotFoundIntegrationTest() {

        assertThat(
            mockMvcTester.delete()
            .uri("/api/comments/444"))
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson()
            .isLenientlyEqualTo("""
            {
                "error": "not_found",
                "message": "Comment with ID 444 not found."
            }
            """);
    }

    @Test
    void updateCommentIntegrationTest() {

        String requestBody = """
                {
                    "reportId": 1,
                    "userId": 1,
                    "text": "Integration test komentar azuriran"
                }
                """;

        assertThat(
                mockMvcTester.put()
                        .uri("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .hasStatusOk()
                        .bodyJson()
                        .extractingPath("$.text")
                        .isEqualTo("Integration test komentar azuriran");

        // verifikacija baze
        Comment updated = commentRepository.findById(1L).orElseThrow();

        assertThat(updated.getText()).isEqualTo("Integration test komentar azuriran");
    }

    @Test
    void updateCommentErrorIntegrationTest() {

        String requestBody = """
                {
                    "reportId": 1,
                    "userId": 1,
                    "text": ""
                }
                """;

        assertThat(
                mockMvcTester.put()
                        .uri("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
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
