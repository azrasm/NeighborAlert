package com.projekat.interaction_service;

import com.projekat.interaction_service.client.ReportServiceClient;
import com.projekat.interaction_service.client.UserServiceClient;
import com.projekat.interaction_service.dto.ReportDTO;
import com.projekat.interaction_service.dto.UserDTO;
import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.repository.CommentRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Integracijski testovi za Comment funkcionalnost.
 *
 * Feign klijenti (ReportServiceClient, UserServiceClient) su mockovani
 * putem @MockitoBean — u test okruženju nema stvarnih mikroservisa.
 *
 * @ActiveProfiles("test") aktivira application-test.properties
 * koji koristi H2 in-memory bazu i isključuje Eureka registraciju.
 */
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CommentIntegrationTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    CommentRepository commentRepository;

    // Mockovani Feign klijenti — zamjenjuju stvarnu sinhronu komunikaciju u testovima
    @MockitoBean
    ReportServiceClient reportServiceClient;

    @MockitoBean
    UserServiceClient userServiceClient;

    // ── Pomoćne metode za setup mockova ──────────────────────────

    private void mockServicesAvailable() {
        ReportDTO report = new ReportDTO();
        report.setId(1L);
        report.setTitle("Test prijava");

        UserDTO user = new UserDTO();
        user.setId(5L);
        user.setUsername("testuser");

        when(reportServiceClient.getReportById(anyLong())).thenReturn(report);
        when(userServiceClient.getUserById(anyLong())).thenReturn(user);
    }

    private void mockReportNotFound() {
        when(reportServiceClient.getReportById(anyLong()))
                .thenThrow(feign.FeignException.NotFound.class);
    }

    private void mockReportServiceDown() {
        when(reportServiceClient.getReportById(anyLong())).thenReturn(null);
    }

    private void mockUserServiceDown() {
        ReportDTO report = new ReportDTO();
        report.setId(1L);
        report.setTitle("Test prijava");
        when(reportServiceClient.getReportById(anyLong())).thenReturn(report);
        when(userServiceClient.getUserById(anyLong())).thenReturn(null);
    }

    // ── Testovi ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/comments — 200 OK kada su oba servisa dostupna")
    void createCommentIntegrationTest() {
        mockServicesAvailable();

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

        // verifikacija baze — DataLoader ucita 3, dodali smo 1 = 4
        assertThat(commentRepository.findAll())
                .hasSize(4);
    }

    @Test
    @DisplayName("POST /api/comments — 503 kada report-service nije dostupan (Zadatak 5f)")
    void createComment_WhenReportServiceDown_Returns503() {
        mockReportServiceDown();

        String requestBody = """
                {
                    "reportId": 1,
                    "userId": 5,
                    "text": "Komentar koji ne treba biti sacuvan"
                }
                """;

        assertThat(
                mockMvcTester.post()
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

        // komentar NE smije biti sačuvan u bazu
        assertThat(commentRepository.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("POST /api/comments — 503 kada user-service nije dostupan (Zadatak 5f)")
    void createComment_WhenUserServiceDown_Returns503() {
        mockUserServiceDown();

        String requestBody = """
                {
                    "reportId": 1,
                    "userId": 5,
                    "text": "Komentar koji ne treba biti sacuvan"
                }
                """;

        assertThat(
                mockMvcTester.post()
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

        assertThat(commentRepository.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("DELETE /api/comments/{id} — 204 No Content")
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

        assertThat(commentRepository.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/comments/{id} — 404 za nepostojeći ID")
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
    @DisplayName("PUT /api/comments/{id} — ažurira tekst komentara")
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

        Comment updated = commentRepository.findById(1L).orElseThrow();
        assertThat(updated.getText()).isEqualTo("Integration test komentar azuriran");
    }

    @Test
    @DisplayName("PUT /api/comments/{id} — 400 za prazan tekst")
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
