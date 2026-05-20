package com.projekat.report_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekat.report_service.dto.ReportDTO;
import com.projekat.report_service.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper; 

    @Test
    public void testGetAllReports_Success() throws Exception {
        ReportDTO dto = new ReportDTO();
        dto.setTitle("Oštećena klupa");

        when(reportService.getAllReports()).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Oštećena klupa"));
    }

    @Test
    public void testGetReportsPaged_Success() throws Exception {
        ReportDTO dto = new ReportDTO();
        dto.setTitle("Puknuta cijev");
        Page<ReportDTO> page = new PageImpl<>(Collections.singletonList(dto));

        when(reportService.getAllReportsPaged(any())).thenReturn(page);

        mockMvc.perform(get("/api/reports/paged")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Puknuta cijev"));
    }

    @Test
    public void testCreateReport_Success() throws Exception {
        ReportDTO inputDto = new ReportDTO();
        inputDto.setTitle("Nova prijava");
        inputDto.setDescription("Opis prijave koji je dovoljno dug");
        inputDto.setAddress("Zmaja od Bosne 15");
        inputDto.setUserId(5L);
        inputDto.setCategoryId(1L);  
        inputDto.setStatusId(1L);

        when(reportService.saveReport(any(ReportDTO.class))).thenReturn(inputDto);

        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nova prijava"));
    }

    @Test
    public void testCreateReport_InvalidData_ReturnsBadRequest() throws Exception {
        ReportDTO invalidDto = new ReportDTO();
        invalidDto.setTitle(""); 

        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteReport_Success() throws Exception {
        when(reportService.deleteReport(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/reports/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteReport_NotFound() throws Exception {
        when(reportService.deleteReport(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/reports/999"))
                .andExpect(status().isNotFound());
    }
}