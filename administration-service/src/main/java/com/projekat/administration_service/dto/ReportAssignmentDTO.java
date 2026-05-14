package com.projekat.administration_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportAssignmentDTO {

    @NotNull(message = "ID prijave je obavezan")
    private Long reportId;

    @NotEmpty(message = "Naziv službe ne može biti prazan")
    private String service;

    private String note; // Opcionalno polje

    @NotNull(message = "ID administratora je obavezan")
    private Long adminId;
}