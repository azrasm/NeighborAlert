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
        commentRepository.save(new Comment(1L, 1L, "Komentar 1"));
        commentRepository.save(new Comment(1L, 2L, "Komentar 2"));
        commentRepository.save(new Comment(2L, 1L, "Komentar 3"));

        // Dodavanje notifikacije
        notificationRepository.save(new Notification(
        1L, 
        "Promjena statusa", 
        "Vaša prijava (Oštećena klupa) je prebačena u status: U toku."));

        notificationRepository.save(new Notification(
            1L, 
            "Novi komentar", 
            "Administrator je dodao komentar na vašu prijavu: 'Ekipa izlazi na teren u petak.'"));
            
        notificationRepository.save(new Notification(
        1L, 
        "Prijava uspješno riješena!", 
        "Služba za komunalne poslove je označila vašu prijavu #88 kao 'Riješeno'. Hvala na prijavi!"));

        // Dodavanje repot flag
        reportFlagRepository.save(new ReportFlag("Reason 1", 1L, 1L, true));
        reportFlagRepository.save(new ReportFlag("Reason 2", 2L, 2L, true));
        reportFlagRepository.save(new ReportFlag("Reason 3", 3L, 3L, false));

        System.out.println("Podaci su ucitani u db");
    }
}