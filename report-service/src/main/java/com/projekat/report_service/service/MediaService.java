package com.projekat.report_service.service;

import com.projekat.report_service.dto.MediaDTO;
import com.projekat.report_service.model.Media;
import com.projekat.report_service.repository.MediaRepository;
import com.projekat.report_service.repository.ReportRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MediaService {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<MediaDTO> getAllMedia() {
        return mediaRepository.findAll().stream()
                .map(m -> modelMapper.map(m, MediaDTO.class))
                .collect(Collectors.toList());
    }

    public MediaDTO addMedia(MediaDTO mediaDTO) {
        Media media = modelMapper.map(mediaDTO, Media.class);

        if (mediaDTO.getReportId() != null) {
            reportRepository.findById(mediaDTO.getReportId())
                    .ifPresent(media::setReport);
        }

        Media savedMedia = mediaRepository.save(media);
        return modelMapper.map(savedMedia, MediaDTO.class);
    }

    public boolean deleteMedia(Long id) {
        if (mediaRepository.existsById(id)) {
            mediaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}