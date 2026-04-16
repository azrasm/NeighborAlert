package com.projekat.administration_service.dto;

public class ReportAssignmentDTO {
    private Long reportId;
    private String service;
    private String note;
    private Long adminId;

    // Prazan konstruktor (neophodan za Java framework-e)
    public ReportAssignmentDTO() {}

    // Konstruktor sa svim poljima
    public ReportAssignmentDTO(Long reportId, String service, String note, Long adminId) {
        this.reportId = reportId;
        this.service = service;
        this.note = note;
        this.adminId = adminId;
    }

    // Getteri i Setteri
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
}