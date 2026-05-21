package com.projekat.administration_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data                   // Generiše gettere, settere, toString, equals i hashCode
@NoArgsConstructor      // Generiše prazan konstruktor (obavezan za Jackson)
@AllArgsConstructor     // Generiše konstruktor sa svim poljima
public class AssignmentCompletedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long assignmentId;
    private Long reportId;
    private Long userId;
}