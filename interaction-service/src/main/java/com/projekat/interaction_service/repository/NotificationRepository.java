package com.projekat.interaction_service.repository;

import com.projekat.interaction_service.model.Notification;

import feign.Param;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Pronalazenje svih notifikacija za jednog korisnika
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    List<Notification> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}