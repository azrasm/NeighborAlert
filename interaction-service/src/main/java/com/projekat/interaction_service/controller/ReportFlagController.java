package com.projekat.interaction_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projekat.interaction_service.model.ReportFlag;
import com.projekat.interaction_service.service.ReportFlagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flags")
@Tag(name = "Reports", description = "Report Flag managment APIs")
public class ReportFlagController {

    @Autowired
    private ReportFlagService flagService;

    // POST /api/flags - Kreiranje prijave
    @PostMapping
    @Operation(summary = "Makes a new report")
    public ResponseEntity<ReportFlag> createFlag(@Valid @RequestBody ReportFlag flag) {
        return new ResponseEntity<>(flagService.createFlag(flag), HttpStatus.CREATED);
    }

    // GET /api/flags - Sve prijave (za admina)
    @GetMapping
    @Operation(summary = "Returns all reports")
    public List<ReportFlag> getAllFlags() {
        return flagService.getAllFlags();
    }

    // GET /api/flags/unreviewed - Samo nepregledane
    @GetMapping("/unreviewed")
    @Operation(summary = "Returns all unreviewed reports")
    public List<ReportFlag> getUnreviewedFlags() {
        return flagService.getUnreviewedFlags();
    }

    // PATCH /api/flags/{id}/review - Oznacavanje kao pregledano
    @PatchMapping("/{id}/review")
    @Operation(summary = "Marks a report flag as reviewed")
    public ReportFlag markAsReviewed(@PathVariable Long id, @Valid @RequestBody Map<String, Boolean> body) {
        // Izvlacimo "reviewed" vrijednost iz JSON-a
        boolean status = body.getOrDefault("reviewed", true);
        return flagService.markAsReviewed(id, status);
    }
}