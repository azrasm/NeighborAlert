package com.projekat.administration_service;

import com.projekat.administration_service.controller.AdministrationController;
import com.projekat.administration_service.service.AdministrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdministrationController.class)
class AdministrationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdministrationService service;

    @Test
    void testInvalidInputReturns400() throws Exception {
        String invalidJson = "{\"reportId\": null}";

        mockMvc.perform(post("/api/administration/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}