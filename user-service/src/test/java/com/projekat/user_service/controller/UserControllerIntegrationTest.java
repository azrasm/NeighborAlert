package com.projekat.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekat.user_service.dto.UserCreateDTO;
import com.projekat.user_service.dto.UserDTO;
import com.projekat.user_service.model.User;
import com.projekat.user_service.model.UserRole;
import com.projekat.user_service.repository.UserRepository;
import com.projekat.user_service.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private UserRole savedRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRoleRepository.deleteAll();

        UserRole role = new UserRole("USER");
        savedRole = userRoleRepository.save(role);
    }

    // ----------------------------------------------------------------
    // GET /api/users
    // ----------------------------------------------------------------

    @Test
    void getAllUsers_KadaNemaKorisnika_VracaPrazanNiz() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllUsers_KadaPostojiKorisnik_VracaListuKorisnika() throws Exception {
        User user = new User("marko", "lozinka123", "marko@test.com", 10, savedRole);
        userRepository.save(user);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("marko"))
                .andExpect(jsonPath("$[0].email").value("marko@test.com"))
                .andExpect(jsonPath("$[0].userScore").value(10))
                .andExpect(jsonPath("$[0].roleName").value("USER"));
    }

    @Test
    void getAllUsers_KadaPostojeViseKorisnika_VracaIspravniBroj() throws Exception {
        userRepository.save(new User("ana", "lozinka123", "ana@test.com", 5, savedRole));
        userRepository.save(new User("pero", "lozinka456", "pero@test.com", 20, savedRole));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ----------------------------------------------------------------
    // POST /api/users
    // ----------------------------------------------------------------

    @Test
    void createUser_ValidniPodaci_VracaKreiranogKorisnika() throws Exception {
        UserCreateDTO dto = validDto();

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("novikorisnik"))
                .andExpect(jsonPath("$.email").value("novi@test.com"))
                .andExpect(jsonPath("$.userScore").value(0))
                .andExpect(jsonPath("$.roleName").value("USER"))
                .andReturn();

        UserDTO responseDto = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserDTO.class);
        assertTrue(userRepository.existsById(responseDto.getId()));
    }

    @Test
    void createUser_PrazanBody_Vraca400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserCreateDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void createUser_PreKratkiUsername_Vraca400() throws Exception {
        UserCreateDTO dto = validDto();
        dto.setUsername("ab");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").exists());
    }

    @Test
    void createUser_PredugiUsername_Vraca400() throws Exception {
        UserCreateDTO dto = validDto();
        dto.setUsername("ovajusernamejepredugacak123");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").exists());
    }

    @Test
    void createUser_NeispravanEmailFormat_Vraca400() throws Exception {
        UserCreateDTO dto = validDto();
        dto.setEmail("ovo-nije-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void createUser_PreKratkaLozinka_Vraca400() throws Exception {
        UserCreateDTO dto = validDto();
        dto.setPassword("krat");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void createUser_NegativniUserScore_Vraca400() throws Exception {
        UserCreateDTO dto = validDto();
        dto.setUserScore(-5);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.userScore").exists());
    }

    @Test
    void createUser_NepostojeciRoleId_Vraca500() throws Exception {
        UserCreateDTO dto = validDto();
        dto.setRoleId(9999L);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("server_error"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("9999")));
    }

    @Test
    void createUser_BezRoleId_Vraca400() throws Exception {
        UserCreateDTO dto = validDto();
        dto.setRoleId(null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.roleId").exists());
    }

    @Test
    void createUser_KorisnikJeSpremljenUBazu() throws Exception {
        long pocetniCount = userRepository.count();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto())))
                .andExpect(status().isCreated());

        assertEquals(pocetniCount + 1, userRepository.count());
        assertTrue(userRepository.findAll().stream()
                .anyMatch(u -> "novikorisnik".equals(u.getUsername())));
    }

    // ----------------------------------------------------------------
    // Pomoćna metoda
    // ----------------------------------------------------------------

    private UserCreateDTO validDto() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("novikorisnik");
        dto.setPassword("lozinka123");
        dto.setEmail("novi@test.com");
        dto.setUserScore(0);
        dto.setRoleId(savedRole.getId());
        return dto;
    }
}