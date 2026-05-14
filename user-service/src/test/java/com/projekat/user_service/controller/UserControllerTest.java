package com.projekat.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekat.user_service.dto.UserCreateDTO;
import com.projekat.user_service.dto.UserDTO;
import com.projekat.user_service.model.User;
import com.projekat.user_service.model.UserRole;
import com.projekat.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean  private UserService userService;

    @Test
    void createUser_ValidniPodaci_Vraca201() throws Exception {
        UserRole role = new UserRole(1L, "USER");
        User mockUser = User.builder().id(1L).username("testuser")
                .email("test@test.com").userScore(0).role(role).build();

        UserDTO mockDto = new UserDTO();
        mockDto.setId(1L);
        mockDto.setUsername("testuser");
        mockDto.setEmail("test@test.com");
        mockDto.setRoleName("USER");

        when(userService.createUser(any())).thenReturn(mockUser);
        when(userService.convertToDto(any())).thenReturn(mockDto);

        UserCreateDTO dto = validCreateDto();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void createUser_NedostajuObaveznaPolja_Vraca400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserCreateDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }

    @Test
    void createUser_KratkiPassword_Vraca400() throws Exception {
        UserCreateDTO dto = validCreateDto();
        dto.setPassword("abc");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void createUser_NullRoleId_Vraca400() throws Exception {
        UserCreateDTO dto = validCreateDto();
        dto.setRoleId(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.roleId").exists());
    }

    private UserCreateDTO validCreateDto() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setPassword("lozinka123");
        dto.setEmail("test@test.com");
        dto.setUserScore(0);
        dto.setRoleId(1L);
        return dto;
    }
}
