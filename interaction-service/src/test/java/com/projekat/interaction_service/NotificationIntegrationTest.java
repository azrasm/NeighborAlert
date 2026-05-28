package com.projekat.interaction_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import com.projekat.interaction_service.model.Notification;
import com.projekat.interaction_service.repository.NotificationRepository;

import org.springframework.http.HttpStatus;

import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class NotificationIntegrationTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    void getNotificationsByUserIntegrationTest() {

        notificationRepository.save(new Notification(4L, "Hello message", "Hello user 4"));

        assertThat(
                mockMvcTester.get().uri("/api/notifications/user/4"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.length()")
                .isEqualTo(1);
    }

    @Test
    void deleteNotificationIntegrationTest() {

        Notification saved = notificationRepository.save(
                new Notification(1L, "Delete message", "To be deleted")
        );

        assertThat(
            mockMvcTester.delete()
            .uri("/api/notifications/" + saved.getId()))
            .hasStatus(HttpStatus.NO_CONTENT);

        assertThat(notificationRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void deleteNotificationNotFoundIntegrationTest() {

        assertThat(
                mockMvcTester.delete().uri("/api/notifications/444"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .isLenientlyEqualTo("""
                    {
                        "error": "not_found",
                        "message": "Notification with ID 444 not found."
                    }
                    """);
    }
    
}
