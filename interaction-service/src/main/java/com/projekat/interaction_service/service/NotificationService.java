package com.projekat.interaction_service.service;

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
}