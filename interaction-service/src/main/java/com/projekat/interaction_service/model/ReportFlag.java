package com.projekat.interaction_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "report")
public class ReportFlag {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Reason for flagging cannot be empty")
    private String reason;

    @NotNull(message = "Report ID is mandatory")
    @Column(name = "report_id")
    private Long reportId;

    @NotNull(message = "User ID is mandatory")
    @Column(name = "user_id")
    private Long userId;

    private Boolean reviewed;

    // Konstruktori
    public ReportFlag() {
    }

    public ReportFlag(String reason, Long reportId, Long userId, Boolean reviewed) {
        this.reason = reason;
        this.reportId = reportId;
        this.userId = userId;
        this.reviewed = reviewed;
    }

    // Geteri i seteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Boolean getReviewed() { return reviewed; }
    public void setReviewed(Boolean reviewed) { this.reviewed = reviewed; }
}