package com.projekat.administration_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryDTO {

    @NotNull(message = "ID prijave ne smije biti null")
    private Long reportId;

    @NotNull(message = "ID administratora ne smije biti null")
    private Long adminId;

    @NotBlank(message = "Novi status je obavezan")
    private String newStatus;

    private String comment; 
}