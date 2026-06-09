package com.projekat.administration_service.service;

import com.projekat.administration_service.dto.ReportAssignmentDTO;
import com.projekat.administration_service.dto.StatusHistoryDTO;
import com.projekat.administration_service.model.ReportAssignment;
import com.projekat.administration_service.model.StatusHistory;
import com.projekat.administration_service.rabbitmq.AdministrationEventProducer;
import com.projekat.administration_service.repository.ReportAssignmentRepository;
import com.projekat.administration_service.repository.StatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.time.LocalDateTime;

@Service
public class AdministrationService {

    @Autowired
    private ReportAssignmentRepository reportAssignmentRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private AdministrationEventProducer administrationEventProducer;

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
        
        // Automatski postavljamo trenutno vrijeme
        history.setChangeDate(LocalDateTime.now());
        
        // Za početak postavljamo fiksni stari status, 
        // kasnije ćemo ga izvlačiti iz baze
        history.setOldStatus("PENDING"); 

        return statusHistoryRepository.save(history);
    }

   // Izmijenjena metoda da koristi optimizaciju (N+1 rješenje)
    public List<ReportAssignment> getAllAssignments() {
        return reportAssignmentRepository.findAllOptimized();
    }

    public List<ReportAssignment> getAssignmentsByAdmin(Long adminId) {
        return reportAssignmentRepository.findByAdminId(adminId);
    }

    public void deleteAssignment(Long id) {
    if (!reportAssignmentRepository.existsById(id)) {
        throw new RuntimeException("Assignment with ID " + id + " not found.");
    }
    reportAssignmentRepository.deleteById(id);
    }

    
    public Page<ReportAssignment> getAllPaged(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return reportAssignmentRepository.findAll(pageable);
    }   

    @Transactional 
    public ReportAssignment assignAndLogStatus(ReportAssignmentDTO dto) {
        // 1. Kreiraj dodjelu
        ReportAssignment ra = new ReportAssignment();
        ra.setReportId(dto.getReportId());
        ra.setAdminId(dto.getAdminId());
        ReportAssignment saved = reportAssignmentRepository.save(ra);

        // 2. Automatski kreiraj zapis u istoriji (drugi repozitorij)
        StatusHistory history = new StatusHistory();
        history.setReportId(dto.getReportId());
        history.setNewStatus("ASSIGNED");
        statusHistoryRepository.save(history);

        return saved;
    }

     /**
     * Zadatak 8. RabbitMQ
     * Vrsi poziv lanca kada administrator oznaci 
     * prijavu kao rijesenu
     */
    @Transactional
    public StatusHistory updateStatusAndTriggerSaga(StatusHistoryDTO dto) {
        // Lokalna transakcija
        StatusHistory history = new StatusHistory();
        history.setReportId(dto.getReportId());
        history.setAdminId(dto.getAdminId());
        history.setNewStatus(dto.getNewStatus());
        history.setComment(dto.getComment());
        history.setChangeDate(LocalDateTime.now());
        history.setOldStatus("U toku"); 

        StatusHistory savedHistory = statusHistoryRepository.save(history);

        Long dummyAssignmentId = dto.getReportId();
            
        // Slanje poruke
        administrationEventProducer.produceAssignmentCompleted(
            dummyAssignmentId, 
            dto.getReportId(),
            dto.getNewStatus()
        );

        return savedHistory;
    }

    /**
     * Zadatak 8. RabbitMQ
     * Inverzna akcija
     * Ako ostatak sistema padne ponistava se status "Riješeno".
     */
    @Transactional
    public void rollbackStatusChange(Long reportId) {
        // Upisuje se novi history sa opisom greske rollback-a
        StatusHistory rollbackHistory = new StatusHistory();
        rollbackHistory.setReportId(reportId);
        rollbackHistory.setNewStatus("U toku");
        rollbackHistory.setOldStatus("Riješeno");
        rollbackHistory.setChangeDate(LocalDateTime.now());
        rollbackHistory.setComment("SAGA ROLLBACK: Automatsko vraćanje statusa zbog greške u sistemu bodovanja.");
        
        statusHistoryRepository.save(rollbackHistory);
        
        System.out.println("Baza uspješno vraćena u početno stanje (Inverzna akcija izvršena za report: " + reportId + ")");
    }
}