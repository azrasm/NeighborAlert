package com.projekat.interaction_service.client;

import com.projekat.interaction_service.dto.ReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback implementacija za {@link ReportServiceClient}.
 *
 * Aktivira se automatski kada report-service nije dostupan:
 * timeout, connection refused, ili HTTP 5xx greška.
 *
 * Na ovaj način interaction-service ostaje aktivan čak i kada
 * jedan od zavisnih mikroservisa padne (zahtjev 5f zadatka).
 *
 * Vraća null — servisni sloj (CommentService) prepoznaje null
 * kao nedostupnost servisa i baca ServiceUnavailableException,
 * što klijentu daje razumljiv 503 odgovor.
 */
@Component
public class ReportServiceFallback implements ReportServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceFallback.class);

    @Override
    public ReportDTO getReportById(Long id) {
        log.warn("[FALLBACK] report-service nije dostupan — reportId={}", id);
        return null;
    }
}
