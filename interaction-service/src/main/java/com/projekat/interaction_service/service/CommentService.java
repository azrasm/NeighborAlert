package com.projekat.interaction_service.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.projekat.interaction_service.client.ReportServiceClient;
import com.projekat.interaction_service.client.UserServiceClient;
import com.projekat.interaction_service.dto.ReportDTO;
import com.projekat.interaction_service.dto.UserDTO;
import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.exception.ServiceUnavailableException;
import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.repository.CommentRepository;
import feign.FeignException;

/**
 * Servisni sloj za upravljanje komentarima.
 *
 * Sinhronа komunikacija (Zadatak 5c, 5d):
 * Metoda saveComment() prije čuvanja komentara sinhronom komunikacijom
 * (Feign + Eureka) provjerava:
 *   1. Da prijava (reportId) postoji u report-service
 *   2. Da korisnik (userId) postoji u user-service
 *
 * Adrese servisa se NE hardkodiraju — Eureka razrješava "report-service"
 * i "user-service" u stvarne IP:port aktivnih instanci (Zadatak 5d).
 *
 * Dostupnost (Zadatak 5f):
 * Razlikujemo dva slučaja nedostupnosti:
 *   - FeignException.NotFound (404) → entitet ne postoji → 404 ResourceNotFoundException
 *   - FeignException ostale / null fallback → servis pao  → 503 ServiceUnavailableException
 */
@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ReportServiceClient reportServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private NotificationService notificationService;

    public List<Comment> getCommentsByReport(Long reportId) {
        return commentRepository.findByReportId(reportId);
    }

    /**
     * Kreira novi komentar uz sinhronom validacijom da reportId i userId postoje.
     *
     * Mogući ishodi:
     *   - report ili user ne postoje (404 od zavisnog servisa) → 404 Not Found
     *   - report-service ili user-service pali (connection error) → 503 Service Unavailable
     *   - sve ok → komentar snimljen → 200 OK
     */
    public Comment saveComment(Comment comment) {
        ReportDTO report = null;
        UserDTO user = null;

        // --- Korak 1: Provjeri da report postoji u report-service ---
        log.info("Provjera postojanja prijave reportId={} u report-service...", comment.getReportId());
        try {
            report = reportServiceClient.getReportById(comment.getReportId());
            if (report == null) {
                // null dolazi od fallbacka — servis nije dostupan
                throw new ServiceUnavailableException(
                    "report-service trenutno nije dostupan. Pokušajte ponovo kasnije.");
            }
            log.info("Prijava potvrđena: '{}' (id={})", report.getTitle(), report.getId());
        } catch (FeignException.NotFound e) {
            // report-service je dostupan, ali report s tim ID-em ne postoji
            log.warn("Prijava reportId={} ne postoji u report-service", comment.getReportId());
            throw new ResourceNotFoundException(
                "Prijava s ID-em " + comment.getReportId() + " ne postoji.");
        } catch (FeignException e) {
            // neki drugi Feign problem (5xx, timeout koji nije uhvatio fallback, itd.)
            log.error("Greška pri komunikaciji s report-service: {}", e.getMessage());
            throw new ServiceUnavailableException(
                "report-service trenutno nije dostupan. Pokušajte ponovo kasnije.");
        }

        // --- Korak 2: Provjeri da korisnik postoji u user-service ---
        log.info("Provjera postojanja korisnika userId={} u user-service...", comment.getUserId());
        try {
            user = userServiceClient.getUserById(comment.getUserId());
            if (user == null) {
                throw new ServiceUnavailableException(
                    "user-service trenutno nije dostupan. Pokušajte ponovo kasnije.");
            }
            log.info("Korisnik potvrđen: '{}' (id={})", user.getUsername(), user.getId());
        } catch (FeignException.NotFound e) {
            log.warn("Korisnik userId={} ne postoji u user-service", comment.getUserId());
            throw new ResourceNotFoundException(
                "Korisnik s ID-em " + comment.getUserId() + " ne postoji.");
        } catch (FeignException e) {
            log.error("Greška pri komunikaciji s user-service: {}", e.getMessage());
            throw new ServiceUnavailableException(
                "user-service trenutno nije dostupan. Pokušajte ponovo kasnije.");
        }

        // --- Korak 3: Oba servisa potvrdila — sačuvaj komentar ---
        Comment saved = commentRepository.save(comment);
        log.info("Komentar id={} uspješno kreiran za reportId={}, userId={}",
                saved.getId(), saved.getReportId(), saved.getUserId());

        //LOGIKA ZA NOTIFIKACIJU
        try {
            // OSIGURANJE: Nemoj slati notifikaciju korisniku ako je on sam komentarisao svoju prijavu
            if (!report.getUserId().equals(user.getId())) {
                
                notificationService.createNotification(
                    report.getUserId(),
                    "Novi komentar na vašoj prijavi",
                    "Korisnik @" + user.getUsername() + " je ostavio komentar na vašoj prijavi: " + report.getTitle());
            }
        } catch (Exception e) {
            // Loguj grešku, ali nemoj srušiti spašavanje komentara ako notifikacija zakže!
            log.error("Neuspješno slanje notifikacije za komentar", e);
        }
        return saved;
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment with ID " + id + " not found.");
        }
        commentRepository.deleteById(id);
    }

    public Comment updateComment(Long id, Comment commentDetails) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment with ID " + id + " not found."));
        comment.setText(commentDetails.getText());
        return commentRepository.save(comment);
    }

    public Comment patchComment(Long id, String patchJson) {
        try {
            Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

            JsonPatch patch = JsonPatch.fromJson(objectMapper.readTree(patchJson));

            JsonNode patched = patch.apply(objectMapper.convertValue(existingComment, JsonNode.class));

            Comment result = objectMapper.treeToValue(patched, Comment.class);

            return commentRepository.save(result);

        } catch (Exception e) {
            throw new RuntimeException("Patch failed: " + e.getMessage(), e);
        }   
    }   

    public Page<Comment> getCommentsByReport(Long reportId, Pageable pageable) {
        return commentRepository.findByReportId(reportId, pageable);
    }

    public List<Comment> searchComments(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        return commentRepository.searchByKeyword(keyword);
    }
}
