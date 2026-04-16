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

@RestController
@RequestMapping("/api/flags")
public class ReportFlagController {

    @Autowired
    private ReportFlagService flagService;

    // POST /api/flags - Kreiranje prijave
    @PostMapping
    public ResponseEntity<ReportFlag> createFlag(@RequestBody ReportFlag flag) {
        return new ResponseEntity<>(flagService.createFlag(flag), HttpStatus.CREATED);
    }

    // GET /api/flags - Sve prijave (za admina)
    @GetMapping
    public List<ReportFlag> getAllFlags() {
        return flagService.getAllFlags();
    }

    // GET /api/flags/unreviewed - Samo nepregledane
    @GetMapping("/unreviewed")
    public List<ReportFlag> getUnreviewedFlags() {
        return flagService.getUnreviewedFlags();
    }

    // PATCH /api/flags/{id}/review - Oznacavanje kao pregledano
    @PatchMapping("/{id}/review")
    public ReportFlag markAsReviewed(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        // Izvlacimo "reviewed" vrijednost iz JSON-a
        boolean status = body.getOrDefault("reviewed", true);
        return flagService.markAsReviewed(id, status);
    }
}