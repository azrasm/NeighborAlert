package com.projekat.report_service.service;

import com.projekat.report_service.model.Report;
import com.projekat.report_service.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    public void testGetAllReports() {
        Report r1 = new Report();
        r1.setTitle("Rupa na putu");
        Report r2 = new Report();
        r2.setTitle("Kvar na rasvjeti");
        
        when(reportRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        List<Report> result = reportService.getAllReports();

        assertEquals(2, result.size());
        assertEquals("Rupa na putu", result.get(0).getTitle());
        verify(reportRepository, times(1)).findAll();
    }
}