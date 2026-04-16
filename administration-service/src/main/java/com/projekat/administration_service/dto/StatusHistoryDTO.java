package com.projekat.administration_service.dto;

public class StatusHistoryDTO {
    private Long reportId;
    private Long adminId;
    private String newStatus;
    private String comment;

    public StatusHistoryDTO() {}

    public StatusHistoryDTO(Long reportId, Long adminId, String newStatus, String comment) {
        this.reportId = reportId;
        this.adminId = adminId;
        this.newStatus = newStatus;
        this.comment = comment;
    }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}