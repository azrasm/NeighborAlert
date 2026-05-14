package com.projekat.user_service.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private int userScore;
    private String roleName;
}
