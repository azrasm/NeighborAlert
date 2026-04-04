package com.projekat.interaction_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.projekat.interaction_service.model.*;
import com.projekat.interaction_service.repository.*;

@Component
public class DataLoader implements CommandLineRunner {

    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;
    private final ReportFlagRepository reportFlagRepository;

    public DataLoader(CommentRepository commentRepository,
                      NotificationRepository notificationRepository,
                      ReportFlagRepository reportFlagRepository) {

        this.commentRepository = commentRepository;
        this.notificationRepository = notificationRepository;
        this.reportFlagRepository = reportFlagRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // Dodavanje komentara
        Comment komentar = new Comment();
        komentar.setText("Test komentar...");
        commentRepository.save(komentar);

        // Dodavanje notifikacije
        Notification notifikacija = new Notification();
        notifikacija.setMessage("Test notifikacija...");
        notificationRepository.save(notifikacija);

        // Dodavanje repot flag
        ReportFlag rf = new ReportFlag();
        rf.setReason("Test razlog");
        rf.setReviewed(Boolean.TRUE);
        reportFlagRepository.save(rf);

        System.out.println("Podaci su ucitani u db");
    }
}