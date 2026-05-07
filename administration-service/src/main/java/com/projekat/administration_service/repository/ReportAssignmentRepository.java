package com.projekat.administration_service.repository;

import com.projekat.administration_service.model.ReportAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportAssignmentRepository extends JpaRepository<ReportAssignment, Long> {

    // Ova metoda rješava N+1 problem i služi za tvoj "2. GET /all" endpoint
    @Query("SELECT ra FROM ReportAssignment ra")
    List<ReportAssignment> findAllOptimized();

    List<ReportAssignment> findByAdminId(Long adminId);
}