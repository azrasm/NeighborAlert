package com.projekat.administration_service.controller;

import com.projekat.administration_service.dto.ReportAssignmentDTO;
import com.projekat.administration_service.dto.StatusHistoryDTO;
import com.projekat.administration_service.model.ReportAssignment;
import com.projekat.administration_service.model.StatusHistory;
import com.projekat.administration_service.service.AdministrationService;
import jakarta.validation.Valid; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;


import java.util.List;

@RestController
@RequestMapping("/api/administration")
public class AdministrationController {

    @Autowired
    private AdministrationService administrationService;

    // Endpoint za dodjelu prijave
    @PostMapping("/assign")
    public ResponseEntity<ReportAssignment> assignReport(@Valid @RequestBody ReportAssignmentDTO dto) {
        ReportAssignment result = administrationService.assignAndLogStatus(dto);
        return ResponseEntity.ok(result);
    }

    // Endpoint za promjenu statusa
    @PostMapping("/status")
    public ResponseEntity<StatusHistory> updateStatus(@Valid @RequestBody StatusHistoryDTO dto) {
        StatusHistory result = administrationService.updateStatus(dto);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/assignments")
public ResponseEntity<List<ReportAssignment>> getAllAssignments() {
    List<ReportAssignment> assignments = administrationService.getAllAssignments();
    return ResponseEntity.ok(assignments);
}
    @GetMapping("/all")
public ResponseEntity<List<ReportAssignment>> getAll() {
    return ResponseEntity.ok(administrationService.getAllAssignments());
}
    @GetMapping("/admin/{adminId}")
public ResponseEntity<List<ReportAssignment>> getByAdmin(@PathVariable Long adminId) {
    return ResponseEntity.ok(administrationService.getAssignmentsByAdmin(adminId));
}
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    administrationService.deleteAssignment(id);
    return ResponseEntity.noContent().build(); // Vraća 204 status
}
//PAGINACIJA
@GetMapping("/paged")
public ResponseEntity<Page<ReportAssignment>> getAllPaged(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy) {
    return ResponseEntity.ok(administrationService.getAllPaged(page, size, sortBy));
}
    /**
    * Zadatak 8. RabbitMQ
    * Endpoint za promjenu statusa ili rollback
    */
    @PostMapping("/status/change")
    public ResponseEntity<StatusHistory> updateStatusAndRollack(@Valid @RequestBody StatusHistoryDTO dto) {
        StatusHistory result = administrationService.updateStatusAndTriggerSaga(dto);
        return ResponseEntity.ok(result);
    }
}
