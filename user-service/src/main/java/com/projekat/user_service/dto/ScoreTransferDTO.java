package com.projekat.user_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScoreTransferDTO {

    @NotNull(message = "ID pošiljaoca je obavezan")
    private Long fromUserId;

    @NotNull(message = "ID primaoca je obavezan")
    private Long toUserId;

    @Min(value = 1, message = "Iznos transfera mora biti najmanje 1")
    private int amount;
}
