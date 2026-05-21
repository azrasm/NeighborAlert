package com.projekat.user_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.projekat.user_service.dto.*;
import com.projekat.user_service.exception.ResourceNotFoundException;
import com.projekat.user_service.model.User;
import com.projekat.user_service.model.UserRole;
import com.projekat.user_service.repository.UserRepository;
import com.projekat.user_service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    // ---------------------------------------------------------------
    // Čitanje
    // ---------------------------------------------------------------

    /** Vraća sve korisnike koristeći entity graph (izbjegava N+1). */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Paginacija + sortiranje korisnika. */
    public Page<User> getUsersPaged(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /** Dohvata jednog korisnika ili baca 404. */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID-jem " + id + " ne postoji."));
    }

    /** Custom upit: pretraga po dijelu usernamea. */
    public List<User> searchByUsername(String query) {
        return userRepository.searchByUsername(query);
    }

    /** Custom upit: korisnici sa score-om iznad praga. */
    public List<User> getTopUsers(int minScore) {
        return userRepository.findTopUsersByMinScore(minScore);
    }

    /** Custom upit: korisnici po imenu role, paginisano. */
    public Page<User> getUsersByRole(String roleName, Pageable pageable) {
        return userRepository.findByRoleNamePaged(roleName, pageable);
    }

    /** Statistika: prosječan score po roli. */
    public List<RoleScoreStatDTO> getAverageScoreByRole() {
        return userRepository.getAverageScoreByRole().stream()
                .map(row -> new RoleScoreStatDTO((String) row[0], (Double) row[1]))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Kreiranje
    // ---------------------------------------------------------------

    @Transactional
    public User createUser(UserCreateDTO dto) {
        validateUniqueUsername(dto.getUsername(), null);
        validateUniqueEmail(dto.getEmail(), null);

        UserRole role = findRoleById(dto.getRoleId());

        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword()) // u produkciji: BCrypt
                .email(dto.getEmail())
                .userScore(dto.getUserScore())
                .role(role)
                .build();

        return userRepository.save(user);
    }

    // ---------------------------------------------------------------
    // Batch unos (više repository poziva u jednoj transakciji)
    // ---------------------------------------------------------------

    /**
     * Batch unos korisnika. Sve operacije su u jednoj transakciji.
     * Ako jedna operacija ne uspije, cijeli batch se poništava.
     */
    @Transactional
    public BatchResultDTO createUsersBatch(BatchCreateDTO dto) {
        List<UserDTO> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (UserCreateDTO userDto : dto.getUsers()) {
            try {
                if (userRepository.existsByUsername(userDto.getUsername())) {
                    errors.add("Username '" + userDto.getUsername() + "' već postoji - preskočeno.");
                    continue;
                }
                if (userRepository.existsByEmail(userDto.getEmail())) {
                    errors.add("Email '" + userDto.getEmail() + "' već postoji - preskočeno.");
                    continue;
                }
                UserRole role = findRoleById(userDto.getRoleId());
                User user = User.builder()
                        .username(userDto.getUsername())
                        .password(userDto.getPassword())
                        .email(userDto.getEmail())
                        .userScore(userDto.getUserScore())
                        .role(role)
                        .build();
                User saved = userRepository.save(user);
                created.add(convertToDto(saved));
            } catch (Exception e) {
                errors.add("Greška za '" + userDto.getUsername() + "': " + e.getMessage());
            }
        }

        log.info("Batch unos završen: {} uspješno, {} grešaka", created.size(), errors.size());
        return new BatchResultDTO(created.size(), errors.size(), created, errors);
    }

    // ---------------------------------------------------------------
    // PATCH (JSON Patch - RFC 6902)
    // ---------------------------------------------------------------

    /**
     * Parcijalno ažuriranje korisnika putem JSON Patch operacija.
     * Npr: [{"op":"replace","path":"/email","value":"novo@email.com"}]
     */
    @Transactional
    public User patchUser(Long id, JsonPatch patch) {
        User user = getUserById(id);

        try {
            JsonNode userNode = objectMapper.convertValue(user, JsonNode.class);
            JsonNode patchedNode = patch.apply(userNode);
            User patchedUser = objectMapper.treeToValue(patchedNode, User.class);

            // Provjera jedinstvenosti samo ako su se promijenili
            if (!user.getUsername().equals(patchedUser.getUsername())) {
                validateUniqueUsername(patchedUser.getUsername(), id);
            }
            if (!user.getEmail().equals(patchedUser.getEmail())) {
                validateUniqueEmail(patchedUser.getEmail(), id);
            }

            patchedUser.setId(id); // osiguraj ID
            return userRepository.save(patchedUser);

        } catch (Exception e) {
            throw new IllegalArgumentException("Greška pri primjeni JSON patch-a: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Transakcione metode (više repository poziva)
    // ---------------------------------------------------------------

    /**
     * Prenos score bodova između dva korisnika.
     * Atomična operacija: ili oba ažuriranja uspiju ili nijedan.
     */
    @Transactional
    public void transferScore(ScoreTransferDTO dto) {
        if (dto.getFromUserId().equals(dto.getToUserId())) {
            throw new IllegalArgumentException("Ne možete prenijeti score samom sebi.");
        }

        User from = getUserById(dto.getFromUserId());
        User to = getUserById(dto.getToUserId());

        if (from.getUserScore() < dto.getAmount()) {
            throw new IllegalArgumentException(
                    "Korisnik '" + from.getUsername() + "' nema dovoljno bodova. " +
                    "Dostupno: " + from.getUserScore() + ", traženo: " + dto.getAmount());
        }

        from.setUserScore(from.getUserScore() - dto.getAmount());
        to.setUserScore(to.getUserScore() + dto.getAmount());

        userRepository.save(from);
        userRepository.save(to);

        log.info("Score transfer: {} -> {} ({} bodova)", from.getUsername(), to.getUsername(), dto.getAmount());
    }

    /**
     * Dodaje bonus score svim korisnicima određene role.
     * Koristi bulk UPDATE upit.
     */
    @Transactional
    public int addBonusScoreToRole(Long roleId, int bonus) {
        if (!roleRepository.existsById(roleId)) {
            throw new ResourceNotFoundException("Rola sa ID-jem " + roleId + " ne postoji.");
        }
        int updated = userRepository.addBonusScoreByRole(roleId, bonus);
        log.info("Bonus score +{} dodat za {} korisnika sa roleId={}", bonus, updated, roleId);
        return updated;
    }

    // ---------------------------------------------------------------
    // Brisanje
    // ---------------------------------------------------------------

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Korisnik sa ID-jem " + id + " ne postoji.");
        }
        userRepository.deleteById(id);
    }

    // ---------------------------------------------------------------
    // Pomoćne metode
    // ---------------------------------------------------------------

    public UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setUserScore(user.getUserScore());
        dto.setRoleName(user.getRole().getName());
        return dto;
    }

    private UserRole findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rola sa ID-jem " + roleId + " ne postoji."));
    }

    private void validateUniqueUsername(String username, Long excludeId) {
        boolean exists = excludeId == null
                ? userRepository.existsByUsername(username)
                : userRepository.findAll().stream()
                    .anyMatch(u -> u.getUsername().equals(username) && !u.getId().equals(excludeId));
        if (exists) {
            throw new IllegalArgumentException("Username '" + username + "' je već zauzet.");
        }
    }

    private void validateUniqueEmail(String email, Long excludeId) {
        boolean exists = excludeId == null
                ? userRepository.existsByEmail(email)
                : userRepository.findAll().stream()
                    .anyMatch(u -> u.getEmail().equals(email) && !u.getId().equals(excludeId));
        if (exists) {
            throw new IllegalArgumentException("Email '" + email + "' je već zauzet.");
        }
    }

    /**
     * Zadatak 8. RabbitMQ
     * Saga transakcija: Dodavanje bodova korisniku za uspjesno rijesenu prijavu
     */
    @Transactional
    public void addPointsToUserScore(Long userId, int points) {
        log.info("Saga obrada: Pokušaj dodavanja {} bodova korisniku sa ID-jem: {}", points, userId);
        
        User user = getUserById(userId);
        
        // Povecavanje score-a
        int newScore = user.getUserScore() + points;
        user.setUserScore(newScore);
        
        userRepository.save(user);
        
        log.info("Uspješno ažuriran score za korisnika {}. Stari score -> Novi score: {} -> {}", 
                user.getUsername(), (newScore - points), newScore);
    }
}
