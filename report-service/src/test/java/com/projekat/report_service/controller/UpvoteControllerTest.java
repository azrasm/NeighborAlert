package com.projekat.report_service.controller;

import com.projekat.report_service.dto.UpvoteDTO;
import com.projekat.report_service.service.UpvoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UpvoteController.class)
public class UpvoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpvoteService upvoteService;

    @Test
    public void testGetAllUpvotes() throws Exception {
        UpvoteDTO dto = new UpvoteDTO();
        dto.setUserId(99L);

        when(upvoteService.getAllUpvotes()).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/upvotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(99));
    }
}