package com.projekat.administration_service.service;

import com.projekat.administration_service.dto.ReportAssignmentDTO;
import com.projekat.administration_service.dto.StatusHistoryDTO;
import com.projekat.administration_service.model.ReportAssignment;
import com.projekat.administration_service.model.StatusHistory;
import com.projekat.administration_service.repository.ReportAssignmentRepository;
import com.projekat.administration_service.repository.StatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdministrationService {

    @Autowired
    private ReportAssignmentRepository reportAssignmentRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    public ReportAssignment assignReport(ReportAssignmentDTO dto) {
        // Kreiramo novi objekat modela (Entity)
        ReportAssignment assignment = new ReportAssignment();
        
        // Ručno mapiramo (prepisujemo) podatke iz DTO u Model
        assignment.setReportId(dto.getReportId());
        assignment.setService(dto.getService());
        assignment.setNote(dto.getNote());
        assignment.setAdminId(dto.getAdminId());
        
        // Čuvamo u bazu i vraćamo spašeni objekat
        return reportAssignmentRepository.save(assignment);
    }
public StatusHistory updateStatus(StatusHistoryDTO dto) {
        StatusHistory history = new StatusHistory();
        
        history.setReportId(dto.getReportId());
        history.setAdminId(dto.getAdminId());
        history.setNewStatus(dto.getNewStatus());
        history.setComment(dto.getComment());
        
        // Automatski postavljamo trenutno vreme
        history.setChangeDate(LocalDateTime.now());
        
        // Za početak postavljamo fiksni stari status, 
        // kasnije ćemo ga izvlačiti iz baze
        history.setOldStatus("PENDING"); 

        return statusHistoryRepository.save(history);
    }
}