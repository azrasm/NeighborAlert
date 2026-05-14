package com.projekat.administration_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
@Data               // Automatski pravi getere, setere, equals, hashCode i toString
@NoArgsConstructor  // Pravi prazan konstruktor public StatusHistory() {}
@AllArgsConstructor // Pravi konstruktor sa svim poljima
@Builder
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "change_date")
    private LocalDateTime changeDate;

    private String comment;
}