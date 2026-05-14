package com.projekat.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleScoreStatDTO {
    private String roleName;
    private double averageScore;
}
