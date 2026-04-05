package com.projekat.administration_service.repository;

import com.projekat.administration_service.model.ReportAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportAssignmentRepository extends JpaRepository<ReportAssignment, Long> {
}