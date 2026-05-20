package com.projekat.report_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekat.report_service.dto.UpvoteDTO;
import com.projekat.report_service.service.UpvoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UpvoteController.class)
public class UpvoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpvoteService upvoteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllUpvotes_Success() throws Exception {
        UpvoteDTO dto = new UpvoteDTO();
        dto.setUserId(123L);

        when(upvoteService.getAllUpvotes()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/upvotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(123));
    }

    @Test
    public void testCreateUpvote_MissingUserId_ReturnsBadRequest() throws Exception {
        UpvoteDTO invalidDto = new UpvoteDTO();

        mockMvc.perform(post("/api/upvotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}