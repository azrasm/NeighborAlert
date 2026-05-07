package com.projekat.interaction_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.model.ReportFlag;
import com.projekat.interaction_service.repository.CommentRepository;
import com.projekat.interaction_service.repository.ReportFlagRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ReportFlagIntegrationTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    ReportFlagRepository reportFlagRepository;

    @Test
    void createFlagIntegrationTest() {

        String requestBody = """
                {
                    "reason": "Spam sadrzaj",
                    "reportId": 1,
                    "userId": 2,
                    "reviewed": false
                }
                """;

        assertThat(
                mockMvcTester.post()
                        .uri("/api/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.reason")
                .isEqualTo("Spam sadrzaj");

        assertThat(reportFlagRepository.findAll())
                .hasSize(4);
    }

    @Test
    void createFlagErrorIntegrationTest() {

        String requestBody = """
                {
                    "reason": "",
                    "reportId": 1,
                    "userId": 2,
                    "reviewed": false
                }
                """;

        assertThat(
                mockMvcTester.post()
                        .uri("/api/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .isLenientlyEqualTo("""
                        {
                            "error": "validation_error",
                            "message": "Reason for flagging cannot be empty"
                        }
                        """);
    }

    @Test
    void getUnreviewedFlagsIntegrationTest() {

        reportFlagRepository.save(new ReportFlag("Spam", 1L, 2L, false));

        assertThat(
                mockMvcTester.get().uri("/api/flags/unreviewed")
        )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.length()")
                .isEqualTo(2); // zajedno sa dataloader imaju 2
    }

    @Test
    void markAsReviewedIntegrationTest() {

        ReportFlag flag = reportFlagRepository.save(
                new ReportFlag("Spam", 1L, 2L, false)
        );

        String requestBody = """
                {
                    "reviewed": true
                }
                """;

        assertThat(
                mockMvcTester.patch()
                        .uri("/api/flags/" + flag.getId() + "/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.reviewed")
                .isEqualTo(true);

        ReportFlag updated = reportFlagRepository.findById(flag.getId()).orElseThrow();
        assertThat(updated.getReviewed()).isTrue();
    }

    @Test
    void markAsReviewedErrorIntegrationTest() {

        String requestBody = """
                {
                    "reviewed": true
                }
                """;

        assertThat(
                mockMvcTester.patch()
                        .uri("/api/flags/444/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .hasStatus(HttpStatus.NOT_FOUND)
                        .bodyJson()
                        .isLenientlyEqualTo("""
                            {
                                "error": "not_found",
                                "message": "Flag with ID 444 not found."
                            }
                            """);
    }
}
