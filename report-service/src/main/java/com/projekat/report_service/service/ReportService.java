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

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private ModelMapper modelMapper;

    private ReportDTO toDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setDescription(report.getDescription());
        dto.setAddress(report.getAddress());
        dto.setUserId(report.getUserId());
        if (report.getCategory() != null) {
            dto.setCategoryId(report.getCategory().getId());
        }
        if (report.getStatus() != null) {
            dto.setStatusId(report.getStatus().getId());
        }
        return dto;
    }

    public List<ReportDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<ReportDTO> getAllReportsPaged(@NonNull Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::toDTO);
    }

    public ReportDTO getReportById(@NonNull Long id) {
        return reportRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Transactional
    public ReportDTO saveReport(ReportDTO reportDTO) {
        Report report = new Report();
        report.setTitle(reportDTO.getTitle());
        report.setDescription(reportDTO.getDescription());
        report.setAddress(reportDTO.getAddress());
        report.setUserId(reportDTO.getUserId());

        Category category = categoryRepository.findById(reportDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategorija nije pronađena"));
        report.setCategory(category);

        Status status = statusRepository.findById(reportDTO.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status nije pronađen"));
        report.setStatus(status);

        return toDTO(reportRepository.save(report));
    }

    @Transactional
    public boolean deleteReport(@NonNull Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public ReportDTO updateReport(Long id, ReportDTO reportDTO) {
        return reportRepository.findById(id).map(existing -> {
            existing.setTitle(reportDTO.getTitle());
            existing.setDescription(reportDTO.getDescription());
            existing.setAddress(reportDTO.getAddress());

            if (reportDTO.getCategoryId() != null) {
                categoryRepository.findById(reportDTO.getCategoryId())
                        .ifPresent(existing::setCategory);
            }
            if (reportDTO.getStatusId() != null) {
                statusRepository.findById(reportDTO.getStatusId())
                        .ifPresent(existing::setStatus);
            }

            return toDTO(reportRepository.save(existing));
        }).orElse(null);
    /**
     * Zadatak 8. RabbitMQ
     * Saga transakcija: Postavlja status prijave na "Riješeno"
     */
    @Transactional
    public void updateReportStatusByName(Long reportId, String statusName) {
        // Nadji prijavu
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Prijava sa ID-jem " + reportId + " nije pronađena."));

        // Nadji id iz status tabele
        Status status = statusRepository.findByName(statusName)
                .orElseThrow(() -> new RuntimeException("Status '" + statusName + "' ne postoji u bazi podataka."));

        report.setStatus(status);
        reportRepository.save(report);
    }

    /**
     * Inverzna akcija: Vraca status ako user-service baci grešku.
     */
    @Transactional
    public void rollbackReportStatus(Long reportId) {
        // Trazi report
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Prijava za rollback nije pronađena."));

        // Nadji status u tabeli Status
        Status statusInProgress = statusRepository.findByName("U toku")
                .orElseThrow(() -> new RuntimeException("Status 'U toku' ne postoji u tvojoj bazi podataka."));

        report.setStatus(statusInProgress);
        reportRepository.save(report);
        
        System.out.println("SAGA ROLLBACK: Javna prijava " + reportId + " je uspješno vraćena na status U_TOKU.");
    }
}