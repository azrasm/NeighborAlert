package com.projekat.report_service.controller;

import com.projekat.report_service.dto.ReportDTO;
import com.projekat.report_service.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public List<ReportDTO> getAllReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Long id) {
        ReportDTO reportDTO = reportService.getReportById(id);
        if (reportDTO != null) {
            return ResponseEntity.ok(reportDTO);
        }
        return ResponseEntity.notFound().build();
    }

    ///api/reports/paged?page=0&size=5&sortBy=title
    @GetMapping("/paged")
    public ResponseEntity<Page<ReportDTO>> getReportsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(reportService.getAllReportsPaged(pageable));
    }

    @PostMapping
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody ReportDTO reportDTO) {
        ReportDTO savedReport = reportService.saveReport(reportDTO);
        return new ResponseEntity<>(savedReport, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (reportService.deleteReport(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
}