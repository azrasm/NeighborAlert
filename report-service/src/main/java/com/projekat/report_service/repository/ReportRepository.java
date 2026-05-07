package com.projekat.report_service.repository;

import com.projekat.report_service.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    
    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.category LEFT JOIN FETCH r.status")
    List<Report> findAllWithDetails();
}