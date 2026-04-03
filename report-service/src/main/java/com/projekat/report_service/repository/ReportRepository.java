package com.projekat.report_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.projekat.report_service.model.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}