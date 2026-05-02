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
import java.util.List;

@RestController
@RequestMapping("/api/administration")
public class AdministrationController {

    @Autowired
    private AdministrationService administrationService;

    // Endpoint za dodjelu prijave
    @PostMapping("/assign")
    public ResponseEntity<ReportAssignment> assignReport(@Valid @RequestBody ReportAssignmentDTO dto) {
        ReportAssignment result = administrationService.assignReport(dto);
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
}