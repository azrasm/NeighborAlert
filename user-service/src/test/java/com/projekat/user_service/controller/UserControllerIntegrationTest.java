package com.projekat.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekat.user_service.dto.*;
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

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;

    private UserRole savedRole;
    private UserRole adminRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRoleRepository.deleteAll();
        savedRole = userRoleRepository.save(new UserRole("USER"));
        adminRole = userRoleRepository.save(new UserRole("ADMIN"));
    }

    // ================================================================
    // GET /api/users
    // ================================================================

    @Test
    void getAllUsers_PraznaLista() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllUsers_VracaSveKorisnike() throws Exception {
        userRepository.save(makeUser("marko", "marko@test.com", 10, savedRole));
        userRepository.save(makeUser("ana",   "ana@test.com",   20, savedRole));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ================================================================
    // GET /api/users/paged — paginacija i sortiranje
    // ================================================================

    @Test
    void getUsersPaged_PodrazumijevanaPaginacija() throws Exception {
        for (int i = 0; i < 15; i++) {
            userRepository.save(makeUser("user" + i, "user" + i + "@test.com", i * 10, savedRole));
        }

        mockMvc.perform(get("/api/users/paged?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void getUsersPaged_SortiranjePadajuce() throws Exception {
        userRepository.save(makeUser("low",  "low@test.com",  10, savedRole));
        userRepository.save(makeUser("high", "high@test.com", 90, savedRole));
        userRepository.save(makeUser("mid",  "mid@test.com",  50, savedRole));

        mockMvc.perform(get("/api/users/paged?sort=userScore,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("high"))
                .andExpect(jsonPath("$.content[2].username").value("low"));
    }

    @Test
    void getUsersPaged_DrugaStrana() throws Exception {
        for (int i = 0; i < 12; i++) {
            userRepository.save(makeUser("u" + i, "u" + i + "@test.com", 0, savedRole));
        }

        mockMvc.perform(get("/api/users/paged?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(1));
    }

    // ================================================================
    // GET /api/users/{id}
    // ================================================================

    @Test
    void getUserById_PostojeciKorisnik_VracaKorisnika() throws Exception {
        User user = userRepository.save(makeUser("marko", "marko@test.com", 10, savedRole));

        mockMvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("marko"))
                .andExpect(jsonPath("$.roleName").value("USER"));
    }

    @Test
    void getUserById_NepostojeciId_Vraca404() throws Exception {
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    // ================================================================
    // POST /api/users
    // ================================================================

    @Test
    void createUser_ValidniPodaci_VracaKreiranogKorisnika() throws Exception {
        UserCreateDTO dto = validDto("novi", "novi@test.com");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("novi"))
                .andExpect(jsonPath("$.roleName").value("USER"))
                .andReturn();

        UserDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserDTO.class);
        assertTrue(userRepository.existsById(response.getId()));
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
        UserCreateDTO dto = validDto("ab", "ab@test.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").exists());
    }

    @Test
    void createUser_NeispravanEmail_Vraca400() throws Exception {
        UserCreateDTO dto = validDto("user1", "nije-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void createUser_NegativniScore_Vraca400() throws Exception {
        UserCreateDTO dto = validDto("user1", "u@test.com");
        dto.setUserScore(-1);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.userScore").exists());
    }

    @Test
    void createUser_NepostojeciRoleId_Vraca404() throws Exception {
        UserCreateDTO dto = validDto("user1", "u@test.com");
        dto.setRoleId(9999L);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void createUser_DupliUsername_Vraca400() throws Exception {
        userRepository.save(makeUser("postojeci", "p@test.com", 0, savedRole));

        UserCreateDTO dto = validDto("postojeci", "drugiemail@test.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    // ================================================================
    // POST /api/users/batch — batch unos
    // ================================================================

    @Test
    void createUsersBatch_ValidniPodaci_KreiraSveKorisnike() throws Exception {
        BatchCreateDTO batch = new BatchCreateDTO();
        batch.setUsers(List.of(
                validDto("batch1", "b1@test.com"),
                validDto("batch2", "b2@test.com"),
                validDto("batch3", "b3@test.com")
        ));

        mockMvc.perform(post("/api/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successCount").value(3))
                .andExpect(jsonPath("$.failCount").value(0))
                .andExpect(jsonPath("$.createdUsers.length()").value(3));

        assertEquals(3, userRepository.count());
    }

    @Test
    void createUsersBatch_PraznaLista_Vraca400() throws Exception {
        BatchCreateDTO batch = new BatchCreateDTO();
        batch.setUsers(List.of());

        mockMvc.perform(post("/api/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUsersBatch_DioKorisnikaJeDuplikat_VracaMultiStatus() throws Exception {
        userRepository.save(makeUser("existing", "existing@test.com", 0, savedRole));

        BatchCreateDTO batch = new BatchCreateDTO();
        batch.setUsers(List.of(
                validDto("existing", "other@test.com"), // duplikat - preskočen
                validDto("newuser",  "newuser@test.com") // ok
        ));

        mockMvc.perform(post("/api/users/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().is(207)) // Multi-Status
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failCount").value(1));
    }

    // ================================================================
    // PATCH /api/users/{id} — JSON Patch
    // ================================================================

    @Test
    void patchUser_PromjenaEmaila_Uspjesno() throws Exception {
        User user = userRepository.save(makeUser("patchme", "old@test.com", 10, savedRole));

        String patch = "[{\"op\":\"replace\",\"path\":\"/email\",\"value\":\"new@test.com\"}]";

        mockMvc.perform(patch("/api/users/" + user.getId())
                        .contentType("application/json-patch+json")
                        .content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.username").value("patchme"));
    }

    @Test
    void patchUser_NepostojeciId_Vraca404() throws Exception {
        String patch = "[{\"op\":\"replace\",\"path\":\"/email\",\"value\":\"x@test.com\"}]";

        mockMvc.perform(patch("/api/users/99999")
                        .contentType("application/json-patch+json")
                        .content(patch))
                .andExpect(status().isNotFound());
    }

    // ================================================================
    // DELETE /api/users/{id}
    // ================================================================

    @Test
    void deleteUser_PostojeciKorisnik_Uspjesno() throws Exception {
        User user = userRepository.save(makeUser("tobedeleted", "del@test.com", 0, savedRole));

        mockMvc.perform(delete("/api/users/" + user.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    void deleteUser_NepostojeciId_Vraca404() throws Exception {
        mockMvc.perform(delete("/api/users/99999"))
                .andExpect(status().isNotFound());
    }

    // ================================================================
    // GET /api/users/search — custom upit
    // ================================================================

    @Test
    void searchUsers_PronadjePoDijeluUsernamea() throws Exception {
        userRepository.save(makeUser("markovic", "markovic@test.com", 0, savedRole));
        userRepository.save(makeUser("marko",    "marko@test.com",    0, savedRole));
        userRepository.save(makeUser("petar",    "petar@test.com",    0, savedRole));

        mockMvc.perform(get("/api/users/search?q=mark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchUsers_NemaRezultata_VracaPrazanNiz() throws Exception {
        mockMvc.perform(get("/api/users/search?q=nepostoji"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ================================================================
    // GET /api/users/top — custom upit
    // ================================================================

    @Test
    void getTopUsers_VracaKorisnkieSaDostaScora() throws Exception {
        userRepository.save(makeUser("low",  "low@test.com",  10, savedRole));
        userRepository.save(makeUser("mid",  "mid@test.com",  50, savedRole));
        userRepository.save(makeUser("high", "high@test.com", 90, savedRole));

        mockMvc.perform(get("/api/users/top?minScore=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userScore").value(90)); // sortirani opadajuce
    }

    // ================================================================
    // GET /api/users/by-role — custom upit + paginacija
    // ================================================================

    @Test
    void getUsersByRole_VracaKorisnikeOdredeneRole() throws Exception {
        userRepository.save(makeUser("admin1", "a1@test.com", 0, adminRole));
        userRepository.save(makeUser("user1",  "u1@test.com", 0, savedRole));
        userRepository.save(makeUser("user2",  "u2@test.com", 0, savedRole));

        mockMvc.perform(get("/api/users/by-role?role=USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    // ================================================================
    // GET /api/users/stats/score-by-role — agregacioni upit
    // ================================================================

    @Test
    void getScoreStatsByRole_VracaStatistiku() throws Exception {
        userRepository.save(makeUser("a1", "a1@test.com", 100, adminRole));
        userRepository.save(makeUser("u1", "u1@test.com", 20,  savedRole));
        userRepository.save(makeUser("u2", "u2@test.com", 40,  savedRole));

        mockMvc.perform(get("/api/users/stats/score-by-role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ================================================================
    // POST /api/users/score/transfer — transakciona metoda
    // ================================================================

    @Test
    void transferScore_UspjesanTransfer() throws Exception {
        User from = userRepository.save(makeUser("sender",   "sender@test.com",   100, savedRole));
        User to   = userRepository.save(makeUser("receiver", "receiver@test.com",  10, savedRole));

        ScoreTransferDTO dto = new ScoreTransferDTO();
        dto.setFromUserId(from.getId());
        dto.setToUserId(to.getId());
        dto.setAmount(30);

        mockMvc.perform(post("/api/users/score/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        User updatedFrom = userRepository.findById(from.getId()).orElseThrow();
        User updatedTo   = userRepository.findById(to.getId()).orElseThrow();
        assertEquals(70, updatedFrom.getUserScore());
        assertEquals(40, updatedTo.getUserScore());
    }

    @Test
    void transferScore_NedovoljnoBodova_Vraca400() throws Exception {
        User from = userRepository.save(makeUser("siromah", "s@test.com", 5, savedRole));
        User to   = userRepository.save(makeUser("bogatas", "b@test.com", 0, savedRole));

        ScoreTransferDTO dto = new ScoreTransferDTO();
        dto.setFromUserId(from.getId());
        dto.setToUserId(to.getId());
        dto.setAmount(100);

        mockMvc.perform(post("/api/users/score/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferScore_SamiSebi_Vraca400() throws Exception {
        User user = userRepository.save(makeUser("solo", "solo@test.com", 100, savedRole));

        ScoreTransferDTO dto = new ScoreTransferDTO();
        dto.setFromUserId(user.getId());
        dto.setToUserId(user.getId());
        dto.setAmount(10);

        mockMvc.perform(post("/api/users/score/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferScore_NepostojeciPosiljac_Vraca404() throws Exception {
        User to = userRepository.save(makeUser("receiver2", "recv2@test.com", 0, savedRole));

        ScoreTransferDTO dto = new ScoreTransferDTO();
        dto.setFromUserId(99999L);
        dto.setToUserId(to.getId());
        dto.setAmount(10);

        mockMvc.perform(post("/api/users/score/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ================================================================
    // POST /api/users/score/bonus — bulk transakcija
    // ================================================================

    @Test
    void addBonusScore_DodajeBodusSvimKorisnicimaRole() throws Exception {
        userRepository.save(makeUser("u1", "u1b@test.com", 10, savedRole));
        userRepository.save(makeUser("u2", "u2b@test.com", 20, savedRole));
        userRepository.save(makeUser("a1", "a1b@test.com", 50, adminRole));

        mockMvc.perform(post("/api/users/score/bonus?roleId=" + savedRole.getId() + "&bonus=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedCount").value(2));

        // Provjeri da su USER-i dobili bonus, ADMIN nije
        List<User> users = userRepository.findAll();
        users.stream().filter(u -> u.getRole().getId().equals(savedRole.getId()))
                .forEach(u -> assertTrue(u.getUserScore() >= 15));
    }

    @Test
    void addBonusScore_NepostojecaRola_Vraca404() throws Exception {
        mockMvc.perform(post("/api/users/score/bonus?roleId=99999&bonus=10"))
                .andExpect(status().isNotFound());
    }

    // ================================================================
    // Pomoćne metode
    // ================================================================

    private User makeUser(String username, String email, int score, UserRole role) {
        return User.builder()
                .username(username).password("pass123")
                .email(email).userScore(score).role(role).build();
    }

    private UserCreateDTO validDto(String username, String email) {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername(username);
        dto.setPassword("lozinka123");
        dto.setEmail(email);
        dto.setUserScore(0);
        dto.setRoleId(savedRole.getId());
        return dto;
    }
}
