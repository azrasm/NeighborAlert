package com.projekat.report_service.controller;

import com.projekat.report_service.dto.UpvoteDTO;
import com.projekat.report_service.model.Upvote;
import com.projekat.report_service.repository.UpvoteRepository;

import jakarta.validation.Valid;

import com.projekat.report_service.repository.ReportRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/upvotes")
public class UpvoteController {

    @Autowired
    private UpvoteRepository upvoteRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public List<UpvoteDTO> getAllUpvotes() {
        return upvoteRepository.findAll().stream()
                .map(u -> modelMapper.map(u, UpvoteDTO.class))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<UpvoteDTO> createUpvote(@Valid @RequestBody UpvoteDTO upvoteDTO) {
        Upvote upvote = modelMapper.map(upvoteDTO, Upvote.class);

        // Povezivanje sa Report entitetom
        if (upvoteDTO.getReportId() != null) {
            reportRepository.findById(upvoteDTO.getReportId())
                    .ifPresent(upvote::setReport);
        }

        Upvote savedUpvote = upvoteRepository.save(upvote);
        return new ResponseEntity<>(modelMapper.map(savedUpvote, UpvoteDTO.class), HttpStatus.CREATED);
    }
}