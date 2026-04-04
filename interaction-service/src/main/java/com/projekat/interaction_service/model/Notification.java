package com.projekat.interaction_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notifikacije")
public class Notification {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String message;

    // Konstruktori
    public Notification() {
    }

    public Notification(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    // Geteri i seteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}