package com.projekat.report_service.service;

import com.projekat.report_service.dto.UpvoteDTO;
import com.projekat.report_service.model.Upvote;
import com.projekat.report_service.repository.UpvoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpvoteServiceTest {

    @Mock
    private UpvoteRepository upvoteRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UpvoteService upvoteService;

    @Test
    public void testGetAllUpvotes() {
        Upvote upvote = new Upvote();
        UpvoteDTO dto = new UpvoteDTO();
        dto.setUserId(101L);

        when(upvoteRepository.findAll()).thenReturn(Collections.singletonList(upvote));
        when(modelMapper.map(upvote, UpvoteDTO.class)).thenReturn(dto);

        List<UpvoteDTO> result = upvoteService.getAllUpvotes();

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getUserId());
        verify(upvoteRepository, times(1)).findAll();
    }
}