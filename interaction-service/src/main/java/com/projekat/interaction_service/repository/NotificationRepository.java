package com.projekat.interaction_service.repository;

import com.projekat.interaction_service.model.Notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Pronalazenje svih notifikacija za jednog korisnika
    List<Notification> findByUserId(Long userId);
}