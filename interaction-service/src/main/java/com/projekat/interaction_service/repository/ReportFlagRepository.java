package com.projekat.interaction_service.repository;

import com.projekat.interaction_service.model.ReportFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportFlagRepository extends JpaRepository<ReportFlag, Long> {
}