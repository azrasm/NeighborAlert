package com.projekat.report_service.dto;

import jakarta.validation.constraints.NotNull;

public class UpvoteDTO {
    private Long id;

    @NotNull(message = "Report ID is required")
    private Long reportId; // ID prijave za koju se glasa

    @NotNull(message = "User ID is mandatory")
    private Long userId;   // ID korisnika koji glasa

    public UpvoteDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}