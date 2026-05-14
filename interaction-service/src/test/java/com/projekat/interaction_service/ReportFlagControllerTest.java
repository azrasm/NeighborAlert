package com.projekat.interaction_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import com.projekat.interaction_service.controller.ReportFlagController;
import com.projekat.interaction_service.service.ReportFlagService;

import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = ReportFlagController.class)
class ReportFlagControllerTest {

    @MockitoBean
    ReportFlagService reportFlagService;
    
    @Autowired
    MockMvcTester mockMvcTester;

    @Test
    void createReportFlagError() {
        
        String invalidRequest = """
        {
        "reason": "",
        "reportId": 4,
        "userId": 2,
        "reviewed": true
        }
        """;
        
        assertThat(mockMvcTester.post()
        .uri("/api/flags")
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidRequest))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .isLenientlyEqualTo("""
        {
            "error": "validation_error",
            "message": "Reason for flagging cannot be empty"
        }
        """);
    }
}