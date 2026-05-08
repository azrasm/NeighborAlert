package com.projekat.user_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserCreateDTO {

    @NotBlank(message = "Username je obavezan")
    @Size(min = 3, max = 20, message = "Username mora biti između 3 i 20 karaktera")
    private String username;

    @NotBlank(message = "Password je obavezan")
    @Size(min = 6, message = "Password mora imati najmanje 6 karaktera")
    private String password;

    @Email(message = "Email format nije ispravan")
    @NotBlank(message = "Email je obavezan")
    private String email;

    @Min(value = 0, message = "Score ne može biti negativan")
    private int userScore;

    @NotNull(message = "Role ID je obavezan")
    private Long roleId;
}
