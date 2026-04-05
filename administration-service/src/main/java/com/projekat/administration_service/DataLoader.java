package com.projekat.administration_service;

import com.projekat.administration_service.model.ReportAssignment;
import com.projekat.administration_service.model.StatusHistory;
import com.projekat.administration_service.repository.ReportAssignmentRepository;
import com.projekat.administration_service.repository.StatusHistoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class AdministrationDataLoader implements CommandLineRunner {

    private final ReportAssignmentRepository assignmentRepository;
    private final StatusHistoryRepository historyRepository;

    public AdministrationDataLoader(ReportAssignmentRepository assignmentRepository, StatusHistoryRepository historyRepository) {
        this.assignmentRepository = assignmentRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Dodajemo zaduzenja (ReportAssignments)
        // Pretpostavljamo da report_id 101 i 102 postoje u drugom servisu
        assignmentRepository.save(new ReportAssignment(101L, "Komunalna Služba", "Potrebno hitno izaći na teren.", 2L));
        assignmentRepository.save(new ReportAssignment(102L, "Vodovod", "Provjeriti cijevi u ulici.", 2L));

        // 2. Dodajemo historiju statusa (StatusHistory)
        historyRepository.save(new StatusHistory(
            101L, 2L, "PENDING", "IN_PROGRESS", 
            LocalDateTime.now().minusDays(1), "Započeta obrada prijave."
        ));

        historyRepository.save(new StatusHistory(
            101L, 2L, "IN_PROGRESS", "RESOLVED", 
            LocalDateTime.now(), "Problem uspješno riješen."
        ));

        System.out.println(">>> Administration Service: Podaci za zaduženja i historiju su učitani!");
    }
}