package com.projekat.user_service.controller;

import com.github.fge.jsonpatch.JsonPatch;
import com.projekat.user_service.dto.*;
import com.projekat.user_service.model.User;
import com.projekat.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "Users", description = "Upravljanje korisnicima")
public class UserController {

    private final UserService userService;

    // ---------------------------------------------------------------
    // GET /api/users  — lista svih korisnika
    // ---------------------------------------------------------------

    @GetMapping
    @Operation(summary = "Dohvati sve korisnike")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // ---------------------------------------------------------------
    // GET /api/users/paged  — paginacija i sortiranje
    // ---------------------------------------------------------------

    @GetMapping("/paged")
    @Operation(summary = "Paginisana lista korisnika",
               description = "Parametri: page (default 0), size (default 10), sort (npr. userScore,desc)")
    public ResponseEntity<Page<UserDTO>> getUsersPaged(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "id,asc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<UserDTO> result = userService.getUsersPaged(pageable)
                .map(userService::convertToDto);
        return ResponseEntity.ok(result);
    }

    // ---------------------------------------------------------------
    // GET /api/users/{id}
    // ---------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(summary = "Dohvati korisnika po ID-ju")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.convertToDto(userService.getUserById(id)));
    }

    // ---------------------------------------------------------------
    // POST /api/users  — kreiranje jednog korisnika
    // ---------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Kreiraj novog korisnika")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO dto) {
        User user = userService.createUser(dto);
        return new ResponseEntity<>(userService.convertToDto(user), HttpStatus.CREATED);
    }

    // ---------------------------------------------------------------
    // POST /api/users/batch  — batch unos korisnika
    // ---------------------------------------------------------------

    @PostMapping("/batch")
    @Operation(summary = "Batch unos korisnika (max 100 u jednom zahtjevu)")
    public ResponseEntity<BatchResultDTO> createUsersBatch(@Valid @RequestBody BatchCreateDTO dto) {
        BatchResultDTO result = userService.createUsersBatch(dto);
        HttpStatus status = result.getFailCount() == 0 ? HttpStatus.CREATED : HttpStatus.MULTI_STATUS;
        return new ResponseEntity<>(result, status);
    }

    // ---------------------------------------------------------------
    // PATCH /api/users/{id}  — JSON Patch (RFC 6902)
    // ---------------------------------------------------------------

    @PatchMapping(value = "/{id}", consumes = "application/json-patch+json")
    @Operation(summary = "Parcijalno ažuriranje korisnika (JSON Patch)",
               description = "Primjer tijela: [{\"op\":\"replace\",\"path\":\"/email\",\"value\":\"novo@email.com\"}]")
    public ResponseEntity<UserDTO> patchUser(@PathVariable Long id,
                                              @RequestBody JsonPatch patch) {
        User patched = userService.patchUser(id, patch);
        return ResponseEntity.ok(userService.convertToDto(patched));
    }

    // ---------------------------------------------------------------
    // DELETE /api/users/{id}
    // ---------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(summary = "Obriši korisnika")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------
    // GET /api/users/search?q=  — custom upit: pretraga po username-u
    // ---------------------------------------------------------------

    @GetMapping("/search")
    @Operation(summary = "Pretraga korisnika po username-u (custom JPQL upit)")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String q) {
        List<UserDTO> result = userService.searchByUsername(q).stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ---------------------------------------------------------------
    // GET /api/users/top?minScore=  — custom upit: top korisnici
    // ---------------------------------------------------------------

    @GetMapping("/top")
    @Operation(summary = "Korisnici sa score-om iznad praga, sortirani opadajuće")
    public ResponseEntity<List<UserDTO>> getTopUsers(
            @RequestParam(defaultValue = "0") @Min(0) int minScore) {
        List<UserDTO> result = userService.getTopUsers(minScore).stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ---------------------------------------------------------------
    // GET /api/users/by-role?role=  — custom upit + paginacija
    // ---------------------------------------------------------------

    @GetMapping("/by-role")
    @Operation(summary = "Korisnici po roli (paginisano)")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> result = userService.getUsersByRole(role, pageable)
                .map(userService::convertToDto);
        return ResponseEntity.ok(result);
    }

    // ---------------------------------------------------------------
    // GET /api/users/stats/score-by-role  — custom agregacioni upit
    // ---------------------------------------------------------------

    @GetMapping("/stats/score-by-role")
    @Operation(summary = "Statistika: prosječan score po roli")
    public ResponseEntity<List<RoleScoreStatDTO>> getScoreStatsByRole() {
        return ResponseEntity.ok(userService.getAverageScoreByRole());
    }

    // ---------------------------------------------------------------
    // POST /api/users/score/transfer  — transakciona metoda
    // ---------------------------------------------------------------

    @PostMapping("/score/transfer")
    @Operation(summary = "Prenos score bodova između korisnika (transakciona operacija)")
    public ResponseEntity<Map<String, String>> transferScore(@Valid @RequestBody ScoreTransferDTO dto) {
        userService.transferScore(dto);
        return ResponseEntity.ok(Map.of("message",
                "Uspješno preneseno " + dto.getAmount() + " bodova."));
    }

    // ---------------------------------------------------------------
    // POST /api/users/score/bonus  — bulk transakciona metoda
    // ---------------------------------------------------------------

    @PostMapping("/score/bonus")
    @Operation(summary = "Dodaj bonus score svim korisnicima određene role")
    public ResponseEntity<Map<String, Object>> addBonusScore(
            @RequestParam Long roleId,
            @RequestParam @Min(1) int bonus) {
        int updated = userService.addBonusScoreToRole(roleId, bonus);
        return ResponseEntity.ok(Map.of(
                "message", "Bonus score uspješno ažuriran.",
                "updatedCount", updated
        ));
    }
}
