package com.projekat.report_service.service;

import com.projekat.report_service.dto.ReportDTO;
import com.projekat.report_service.model.Report;
import com.projekat.report_service.model.Category;
import com.projekat.report_service.model.Status;
import com.projekat.report_service.repository.CategoryRepository;
import com.projekat.report_service.repository.ReportRepository;
import com.projekat.report_service.repository.StatusRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull; 

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StatusRepository statusRepository;
    
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<ReportDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .collect(Collectors.toList());
    }

    public Page<ReportDTO> getAllReportsPaged(@NonNull Pageable pageable) {
        return reportRepository.findAll(pageable)
                .map(report -> modelMapper.map(report, ReportDTO.class));
    }

    public ReportDTO getReportById(@NonNull Long id) {
    return reportRepository.findById(id)
            .map(report -> modelMapper.map(report, ReportDTO.class))
            .orElse(null); 
    }

    @Transactional 
    public ReportDTO saveReport(ReportDTO reportDTO) {
        Report report = new Report();
        report.setTitle(reportDTO.getTitle());
        report.setDescription(reportDTO.getDescription());
        report.setAddress(reportDTO.getAddress());
        report.setUserId(reportDTO.getUserId());
    
        Category category = categoryRepository.findById(reportDTO.getCategoryId()).orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));
        report.setCategory(category);
    
        Status status = statusRepository.findById(reportDTO.getStatusId()).orElseThrow(() -> new RuntimeException("Status nije pronađen"));
        report.setStatus(status);
    
        Report savedReport = reportRepository.save(report);
        return modelMapper.map(savedReport, ReportDTO.class);
    }

    @Transactional 
    public boolean deleteReport(@NonNull Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }
}