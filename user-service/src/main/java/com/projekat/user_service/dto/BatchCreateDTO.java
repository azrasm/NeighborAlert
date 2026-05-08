package com.projekat.user_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BatchCreateDTO {

    @NotEmpty(message = "Lista korisnika ne može biti prazna")
    @Size(max = 100, message = "Maksimalan broj korisnika u jednom batch-u je 100")
    @Valid
    private List<UserCreateDTO> users;
}
