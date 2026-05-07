package com.projekat.report_service.controller;

import com.projekat.report_service.dto.MediaDTO;
import com.projekat.report_service.model.Media;
import com.projekat.report_service.repository.MediaRepository;
import com.projekat.report_service.repository.ReportRepository;

import jakarta.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ModelMapper modelMapper;

    // 1. GET - Svi medijski fajlovi
    @GetMapping
    public List<MediaDTO> getAllMedia() {
        return mediaRepository.findAll().stream()
                .map(m -> modelMapper.map(m, MediaDTO.class))
                .collect(Collectors.toList());
    }

    // 2. POST - Dodaj medij za određenu prijavu
    @PostMapping
    public ResponseEntity<MediaDTO> addMedia(@Valid @RequestBody MediaDTO mediaDTO) {
        Media media = modelMapper.map(mediaDTO, Media.class);

        // Povezivanje sa prijavom preko ID-a iz DTO-a
        if (mediaDTO.getReportId() != null) {
            reportRepository.findById(mediaDTO.getReportId())
                    .ifPresent(media::setReport);
        }

        Media savedMedia = mediaRepository.save(media);
        return new ResponseEntity<>(modelMapper.map(savedMedia, MediaDTO.class), HttpStatus.CREATED);
    }

    // 3. DELETE - Brisanje medija
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        if (mediaRepository.existsById(id)) {
            mediaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}