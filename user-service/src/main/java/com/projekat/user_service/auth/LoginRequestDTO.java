package com.projekat.user_service.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Zahtjev za login — username i password.
 */
public class LoginRequestDTO {

    @NotBlank(message = "Username je obavezan")
    private String username;

    @NotBlank(message = "Password je obavezan")
    private String password;

    public LoginRequestDTO() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
