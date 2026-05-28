package com.projekat.interaction_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.model.Notification;
import com.projekat.interaction_service.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Pronalazenje svih notifikacija za korisnika
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    // Brisanje notifikacije uz provjeru postojanja
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification with ID " + id + " not found.");
        }
        notificationRepository.deleteById(id);
    }

    // Oznacava notifikaciju kao procitanu
    public Notification markAsRead(Long id) {
        // Pronađi notifikaciju u bazi, ako ne postoji baci izuzetak
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification with ID " + id + " not found"));

        // Promijeni status u pročitano
        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    // Napravi novu notifikaciju
    public Notification createNotification(Long userId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        
        // Eksplicitno postavljamo na false jer je u pitanju novo, nepročitano obavještenje
        notification.setRead(false); 
        
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }
}