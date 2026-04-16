package com.projekat.user_service.controller;

import com.projekat.user_service.dto.UserCreateDTO;
import com.projekat.user_service.dto.UserDTO;
import com.projekat.user_service.model.User;
import com.projekat.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users") // Osnovni URL za sve metode u ovom kontroleru
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        User user = userService.createUser(dto);
        return new ResponseEntity<>(convertToDto(user), HttpStatus.CREATED);
    }

    // Pomoćna metoda za pretvaranje Entity -> DTO
    // U pravom projektu ovdje bi koristili ModelMapper ili MapStruct
    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setUserScore(user.getUserScore());
        dto.setRoleName(user.getRole().getName());
        return dto;
    }
}