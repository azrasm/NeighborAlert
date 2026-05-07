package com.projekat.report_service.service;

import com.projekat.report_service.model.Report;
import com.projekat.report_service.repository.ReportRepository;
import com.projekat.report_service.repository.CategoryRepository;
import com.projekat.report_service.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StatusRepository statusRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAllWithDetails();
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public Report createReport(Report report, Long categoryId, Long statusId) {
        if (categoryId != null) {
            categoryRepository.findById(categoryId).ifPresent(report::setCategory);
        }
        if (statusId != null) {
            statusRepository.findById(statusId).ifPresent(report::setStatus);
        }
        return reportRepository.save(report);
    }

    public boolean deleteReport(Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }
}