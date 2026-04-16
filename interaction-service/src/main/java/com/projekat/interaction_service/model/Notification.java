package com.projekat.interaction_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "notifikacije")
public class Notification {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is mandatory")
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "Notification message cannot be empty")
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