package com.projekat.administration_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report_assignments")
@Data               // Zamjenjuje sve getere, setere, toString, equals i hashCode
@NoArgsConstructor  // Zamjenjuje prazan konstruktor public ReportAssignment() {}
@AllArgsConstructor // Zamjenjuje konstruktor sa svim poljima
@Builder
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
}