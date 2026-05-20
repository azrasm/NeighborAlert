package com.projekat.report_service.service;

import com.projekat.report_service.dto.UpvoteDTO;
import com.projekat.report_service.model.Upvote;
import com.projekat.report_service.repository.ReportRepository;
import com.projekat.report_service.repository.UpvoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UpvoteService {

    @Autowired
    private UpvoteRepository upvoteRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<UpvoteDTO> getAllUpvotes() {
        return upvoteRepository.findAll().stream()
                .map(u -> modelMapper.map(u, UpvoteDTO.class))
                .collect(Collectors.toList());
    }

    public UpvoteDTO createUpvote(UpvoteDTO upvoteDTO) {
        Upvote upvote = modelMapper.map(upvoteDTO, Upvote.class);

        if (upvoteDTO.getReportId() != null) {
            reportRepository.findById(upvoteDTO.getReportId())
                    .ifPresent(upvote::setReport);
        }

        Upvote savedUpvote = upvoteRepository.save(upvote);
        return modelMapper.map(savedUpvote, UpvoteDTO.class);
    }
}