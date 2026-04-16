package com.projekat.interaction_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "komentari")
public class Comment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Report ID is mandatory")
    @Column(name = "report_id")
    private Long reportId;
    
    @NotNull(message = "User ID is mandatory")
    @Column(name = "user_id")
    private Long userId;
    
    @NotBlank(message = "Comment message cannot be empty")
    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String text;

    // Konstruktori
    public Comment() {
    }

    public Comment(Long reportId, Long userId, String text) {
        this.reportId = reportId;
        this.userId = userId;
        this.text = text;
    }

    // Geteri i seteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}