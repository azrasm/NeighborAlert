package com.projekat.user_service.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Username je obavezan")
    private String username;

    @NotBlank(message = "Lozinka je obavezna")
    private String password;

    @Email(message = "Email nije ispravan")
    @NotBlank(message = "Email je obavezan")
    private String email;
}