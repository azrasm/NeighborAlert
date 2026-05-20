package com.projekat.report_service.controller;

import com.projekat.report_service.dto.MediaDTO;
import com.projekat.report_service.service.MediaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @GetMapping
    public List<MediaDTO> getAllMedia() {
        return mediaService.getAllMedia();
    }

    @PostMapping
    public ResponseEntity<MediaDTO> addMedia(@Valid @RequestBody MediaDTO mediaDTO) {
        MediaDTO savedMedia = mediaService.addMedia(mediaDTO);
        return new ResponseEntity<>(savedMedia, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        if (mediaService.deleteMedia(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}