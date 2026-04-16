package com.projekat.administration_service.controller;

import com.projekat.administration_service.dto.ReportAssignmentDTO;
import com.projekat.administration_service.dto.StatusHistoryDTO;
import com.projekat.administration_service.model.ReportAssignment;
import com.projekat.administration_service.model.StatusHistory;
import com.projekat.administration_service.service.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administration") // Osnovna putanja za ovaj kontroler
public class AdministrationController {

    @Autowired
    private AdministrationService administrationService;

    // Endpoint za dodelu prijave: POST http://localhost:8080/api/administration/assign
    @PostMapping("/assign")
    public ResponseEntity<ReportAssignment> assignReport(@RequestBody ReportAssignmentDTO dto) {
        ReportAssignment result = administrationService.assignReport(dto);
        return ResponseEntity.ok(result);
    }

    // Endpoint za promenu statusa: POST http://localhost:8080/api/administration/status
    @PostMapping("/status")
    public ResponseEntity<StatusHistory> updateStatus(@RequestBody StatusHistoryDTO dto) {
        StatusHistory result = administrationService.updateStatus(dto);
        return ResponseEntity.ok(result);
    }
}