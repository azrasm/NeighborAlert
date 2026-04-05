package com.projekat.administration_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "report_assignments")
public class ReportAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private Long reportId;

    private String service;
    
    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "admin_id")
    private Long adminId;

    public ReportAssignment() {}

    public ReportAssignment(Long reportId, String service, String note, Long adminId) {
        this.reportId = reportId;
        this.service = service;
        this.note = note;
        this.adminId = adminId;
    }

    // geteri i seteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
}