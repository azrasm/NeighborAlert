package com.projekat.report_service.service;

import com.projekat.report_service.dto.MediaDTO;
import com.projekat.report_service.model.Media;
import com.projekat.report_service.repository.MediaRepository;
import com.projekat.report_service.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MediaService mediaService;

    @Test
    public void testGetAllMedia() {
        Media m1 = new Media();
        m1.setUrl("http://slika1.jpg");
        
        MediaDTO dto1 = new MediaDTO();
        dto1.setUrl("http://slika1.jpg");

        when(mediaRepository.findAll()).thenReturn(Arrays.asList(m1));
        when(modelMapper.map(m1, MediaDTO.class)).thenReturn(dto1);

        List<MediaDTO> result = mediaService.getAllMedia();

        assertEquals(1, result.size());
        assertEquals("http://slika1.jpg", result.get(0).getUrl());
        verify(mediaRepository, times(1)).findAll();
    }
}