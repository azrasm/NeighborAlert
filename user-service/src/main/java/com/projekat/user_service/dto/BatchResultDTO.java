package com.projekat.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BatchResultDTO {
    private int successCount;
    private int failCount;
    private List<UserDTO> createdUsers;
    private List<String> errors;
}
