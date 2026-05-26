package com.projekat.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * =========================================================
 *                    Zadatak 8. RabbitMQ
 * =========================================================
 * Opis:
 * Event koji se aktivira kada se prijava uspjesno
 * oznaci kao rijsena
 * Sluzi za pokretanje Saga koreografije i obavjestava
 * user-service da azurira user-service o dodjeli bodova
 * =========================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResolvedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long reportId;
    private Long userId;
    private int pointsToAward;
}