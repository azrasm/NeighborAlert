package com.projekat.administration_service;

import com.projekat.administration_service.model.ReportAssignment;
import com.projekat.administration_service.model.StatusHistory;
import com.projekat.administration_service.repository.ReportAssignmentRepository;
import com.projekat.administration_service.repository.StatusHistoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final ReportAssignmentRepository assignmentRepository;
    private final StatusHistoryRepository historyRepository;

    public DataLoader(ReportAssignmentRepository assignmentRepository, StatusHistoryRepository historyRepository) {
        this.assignmentRepository = assignmentRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Dodajemo zaduženja koristeći Builder (Lombok)
        assignmentRepository.save(ReportAssignment.builder()
                .reportId(101L)
                .service("Komunalna Služba")
                .note("Potrebno hitno izaći na teren.")
                .adminId(2L)
                .build());

        assignmentRepository.save(ReportAssignment.builder()
                .reportId(102L)
                .service("Vodovod")
                .note("Provjeriti cijevi u ulici.")
                .adminId(2L)
                .build());

        // 2. Dodajemo historiju statusa koristeći Builder (Lombok)
        historyRepository.save(StatusHistory.builder()
                .reportId(101L)
                .adminId(2L)
                .oldStatus("PENDING")
                .newStatus("IN_PROGRESS")
                .changeDate(LocalDateTime.now().minusDays(1))
                .comment("Započeta obrada prijave.")
                .build());

        historyRepository.save(StatusHistory.builder()
                .reportId(101L)
                .adminId(2L)
                .oldStatus("IN_PROGRESS")
                .newStatus("RESOLVED")
                .changeDate(LocalDateTime.now())
                .comment("Problem uspješno riješen.")
                .build());
        
        historyRepository.save(StatusHistory.builder()
                .reportId(1L)
                .adminId(2L)
                .oldStatus("Prijavljeno")
                .newStatus("U toku")
                .changeDate(LocalDateTime.now())
                .comment("")
                .build());

        System.out.println(">>> Administration Service: Podaci za zaduženja i historiju su učitani!");
    }
}