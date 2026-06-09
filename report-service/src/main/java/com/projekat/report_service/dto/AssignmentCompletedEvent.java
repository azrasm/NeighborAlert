package com.projekat.report_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * =========================================================
 *                    Zadatak 8. RabbitMQ
 * =========================================================
 * Opis:
 * (Kopija iz admistration-service)
 * Event koji se aktivira kada administrator uspjesno
 * oznaci prijavu kao rijsenu 
 * Sluzi za pokretanje Saga koreografije i obavjestava
 * report-service da azurira status prijave
 * =========================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCompletedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long assignmentId;
    private Long reportId;
    private String newStatus;
}