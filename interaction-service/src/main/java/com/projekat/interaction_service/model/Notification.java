package com.projekat.interaction_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "notifikacije")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is mandatory")
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "Title cannot be empty")
    private String title; 

    @NotBlank(message = "Notification message cannot be empty")
    private String message;

    @Column(name = "is_read")
    private boolean isRead = false; // Po defaultu je svaka nova notifikacija nepročitana

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Konstruktori
    public Notification() {
    }

    public Notification(Long userId, String title, String message) {
        this.userId = userId;
        this.title = title;
        this.message = message;
    }

    // Automatski postavi vrijeme kreiranja prije nego što se spasi u bazu
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Geteri i seteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}