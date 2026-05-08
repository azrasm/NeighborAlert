package com.projekat.user_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekat.user_service.dto.ScoreTransferDTO;
import com.projekat.user_service.dto.UserCreateDTO;
import com.projekat.user_service.exception.ResourceNotFoundException;
import com.projekat.user_service.model.User;
import com.projekat.user_service.model.UserRole;
import com.projekat.user_service.repository.UserRepository;
import com.projekat.user_service.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository roleRepository;
    @Spy  private ObjectMapper objectMapper;

    @InjectMocks
    private UserService userService;

    // ---------------------------------------------------------------
    // createUser
    // ---------------------------------------------------------------

    @Test
    void createUser_ValidniPodaci_KreiraKorisnika() {
        UserCreateDTO dto = makeCreateDto("testuser", "test@test.com", 1L);

        UserRole role = new UserRole(1L, "USER");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("USER", result.getRole().getName());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_NepostojecaRola_BacaException() {
        UserCreateDTO dto = makeCreateDto("testuser", "t@test.com", 99L);

        when(roleRepository.findById(99L)).thenReturn(Optional.empty());
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_DupliUsername_BacaException() {
        UserCreateDTO dto = makeCreateDto("duplikat", "d@test.com", 1L);

        when(userRepository.existsByUsername("duplikat")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // getUserById
    // ---------------------------------------------------------------

    @Test
    void getUserById_PostojeciId_VracaKorisnika() {
        UserRole role = new UserRole(1L, "USER");
        User user = User.builder().id(1L).username("u").email("u@test.com").role(role).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserById_NepostojeciId_BacaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    // ---------------------------------------------------------------
    // transferScore
    // ---------------------------------------------------------------

    @Test
    void transferScore_ValjanTransfer_AzuriraObaKorisnika() {
        UserRole role = new UserRole(1L, "USER");
        User from = User.builder().id(1L).username("from").email("f@t.com").userScore(100).role(role).build();
        User to   = User.builder().id(2L).username("to").email("t@t.com").userScore(10).role(role).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(from));
        when(userRepository.findById(2L)).thenReturn(Optional.of(to));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScoreTransferDTO dto = new ScoreTransferDTO();
        dto.setFromUserId(1L);
        dto.setToUserId(2L);
        dto.setAmount(30);

        userService.transferScore(dto);

        assertEquals(70, from.getUserScore());
        assertEquals(40, to.getUserScore());
        verify(userRepository, times(2)).save(any());
    }

    @Test
    void transferScore_NedovoljnoBodova_BacaException() {
        UserRole role = new UserRole(1L, "USER");
        User from = User.builder().id(1L).username("from").email("f@t.com").userScore(5).role(role).build();
        User to   = User.builder().id(2L).username("to").email("t@t.com").userScore(0).role(role).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(from));
        when(userRepository.findById(2L)).thenReturn(Optional.of(to));

        ScoreTransferDTO dto = new ScoreTransferDTO();
        dto.setFromUserId(1L);
        dto.setToUserId(2L);
        dto.setAmount(100);

        assertThrows(IllegalArgumentException.class, () -> userService.transferScore(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void transferScore_SamiSebi_BacaException() {
        ScoreTransferDTO dto = new ScoreTransferDTO();
        dto.setFromUserId(1L);
        dto.setToUserId(1L);
        dto.setAmount(10);

        assertThrows(IllegalArgumentException.class, () -> userService.transferScore(dto));
    }

    // ---------------------------------------------------------------
    // deleteUser
    // ---------------------------------------------------------------

    @Test
    void deleteUser_PostojeciKorisnik_BriseGa() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NepostojeciKorisnik_BacaException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(any());
    }

    // ---------------------------------------------------------------
    // Pomoćne metode
    // ---------------------------------------------------------------

    private UserCreateDTO makeCreateDto(String username, String email, Long roleId) {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername(username);
        dto.setPassword("lozinka123");
        dto.setEmail(email);
        dto.setUserScore(0);
        dto.setRoleId(roleId);
        return dto;
    }
}
