package com.projekat.report_service.controller;

import com.projekat.report_service.dto.MediaDTO;
import com.projekat.report_service.service.MediaService;
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

@WebMvcTest(MediaController.class)
public class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaService mediaService;

    @Test
    public void testGetAllMedia() throws Exception {
        MediaDTO dto = new MediaDTO();
        dto.setUrl("http://test-slika.com/img.png");

        when(mediaService.getAllMedia()).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/media"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].url").value("http://test-slika.com/img.png"));
    }
}