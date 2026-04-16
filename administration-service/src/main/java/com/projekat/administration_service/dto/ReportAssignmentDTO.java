package com.projekat.administration_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ReportAssignmentDTO {

    @NotNull(message = "ID prijave je obavezan")
    private Long reportId;

    @NotEmpty(message = "Naziv službe ne može biti prazan")
    private String service;

    private String note; // Ovo ostavljamo bez anotacije jer note može biti prazan

    @NotNull(message = "ID administratora je obavezan")
    private Long adminId;


    public ReportAssignmentDTO() {}

    public ReportAssignmentDTO(Long reportId, String service, String note, Long adminId) {
        this.reportId = reportId;
        this.service = service;
        this.note = note;
        this.adminId = adminId;
    }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
}