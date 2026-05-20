package com.projekat.report_service.controller;

import com.projekat.report_service.dto.UpvoteDTO;
import com.projekat.report_service.service.UpvoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/upvotes")
public class UpvoteController {

    @Autowired
    private UpvoteService upvoteService;

    @GetMapping
    public List<UpvoteDTO> getAllUpvotes() {
        return upvoteService.getAllUpvotes();
    }

    @PostMapping
    public ResponseEntity<UpvoteDTO> createUpvote(@Valid @RequestBody UpvoteDTO upvoteDTO) {
        UpvoteDTO savedUpvoteDTO = upvoteService.createUpvote(upvoteDTO);
        return new ResponseEntity<>(savedUpvoteDTO, HttpStatus.CREATED);
    }
}