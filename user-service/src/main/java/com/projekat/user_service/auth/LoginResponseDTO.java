package com.projekat.user_service.auth;

/**
 * Odgovor na uspješan login — JWT token i osnovni podaci o korisniku.
 */
public class LoginResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String role;
    private long expiresIn; // sekunde

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, String username, String role, long expiresIn) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
