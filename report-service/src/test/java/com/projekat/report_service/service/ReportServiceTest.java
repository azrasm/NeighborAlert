package com.projekat.report_service.service;

import com.projekat.report_service.dto.ReportDTO;
import com.projekat.report_service.model.Report;
import com.projekat.report_service.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Spy
    private ModelMapper modelMapper; 

    @InjectMocks
    private ReportService reportService;

    private Report report;
    private ReportDTO reportDTO;

    @BeforeEach
    void setUp() {
        report = new Report();
        report.setId(1L);
        report.setTitle("Test Report");

        reportDTO = new ReportDTO();
        reportDTO.setTitle("Test Report");
    }

    @Test
    void testGetAllReports() {
        when(reportRepository.findAll()).thenReturn(Arrays.asList(report));

        List<ReportDTO> result = reportService.getAllReports();

        assertFalse(result.isEmpty());
        assertEquals(report.getTitle(), result.get(0).getTitle());
    }

    @Test
    void testGetAllReportsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(Arrays.asList(report));

        when(reportRepository.findAll(any(Pageable.class))).thenReturn(reportPage);

        Page<ReportDTO> result = reportService.getAllReportsPaged(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(report.getTitle(), result.getContent().get(0).getTitle());
    }

    @Test
    void testSaveReport() {
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        ReportDTO savedDto = reportService.saveReport(reportDTO);

        assertNotNull(savedDto);
        assertEquals("Test Report", savedDto.getTitle());
    }
}