package com.projekat.report_service.controller;

import com.projekat.report_service.dto.ReportDTO;
import com.projekat.report_service.model.Report;
import com.projekat.report_service.repository.ReportRepository;
import com.projekat.report_service.repository.CategoryRepository;
import com.projekat.report_service.repository.StatusRepository;
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
    private ReportRepository reportRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private ModelMapper modelMapper;

    // 1. GET - Svi izvještaji (Prijave)
    @GetMapping
    public List<ReportDTO> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return reports.stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .collect(Collectors.toList());
    }

    // 2. GET - Jedan izvještaj po ID-u
    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Long id) {
        return reportRepository.findById(id)
                .map(report -> ResponseEntity.ok(modelMapper.map(report, ReportDTO.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST - Kreiranje novog izvještaja 
    @PostMapping
    public ResponseEntity<ReportDTO> createReport(@RequestBody ReportDTO reportDTO) {
        Report report = modelMapper.map(reportDTO, Report.class);

        if (reportDTO.getCategoryId() != null) {
            categoryRepository.findById(reportDTO.getCategoryId())
                    .ifPresent(report::setCategory);
        }

        if (reportDTO.getStatusId() != null) {
            statusRepository.findById(reportDTO.getStatusId())
                    .ifPresent(report::setStatus);
        }

        Report savedReport = reportRepository.save(report);
        
        return new ResponseEntity<>(modelMapper.map(savedReport, ReportDTO.class), HttpStatus.CREATED);
    }

    // 4. DELETE - Brisanje izvještaja
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}