package com.projekat.report_service.controller;

import com.projekat.report_service.dto.ReportDTO;
import com.projekat.report_service.model.Report;
import com.projekat.report_service.service.ReportService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public List<ReportDTO> getAllReports() {
        return reportService.getAllReports().stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Long id) {
        return reportService.getReportById(id)
                .map(report -> ResponseEntity.ok(modelMapper.map(report, ReportDTO.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody ReportDTO reportDTO) {
        Report reportRequest = modelMapper.map(reportDTO, Report.class);
        
        Report savedReport = reportService.createReport(reportRequest, reportDTO.getCategoryId(), reportDTO.getStatusId());
        
        return new ResponseEntity<>(modelMapper.map(savedReport, ReportDTO.class), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (reportService.deleteReport(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}