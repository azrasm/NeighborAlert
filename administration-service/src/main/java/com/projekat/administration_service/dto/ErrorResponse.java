package com.projekat.administration_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Dodatna pogodnost za kreiranje objekata greške
public class ErrorResponse {
    private String error;
    private String message;
}