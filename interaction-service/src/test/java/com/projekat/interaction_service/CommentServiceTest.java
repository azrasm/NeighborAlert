package com.projekat.interaction_service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projekat.interaction_service.client.ReportServiceClient;
import com.projekat.interaction_service.client.UserServiceClient;
import com.projekat.interaction_service.dto.ReportDTO;
import com.projekat.interaction_service.dto.UserDTO;
import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.exception.ServiceUnavailableException;
import com.projekat.interaction_service.model.Comment;
import com.projekat.interaction_service.repository.CommentRepository;
import com.projekat.interaction_service.service.CommentService;

/**
 * Unit testovi za CommentService.
 *
 * Feign klijenti (ReportServiceClient, UserServiceClient) su mockovani —
 * testiramo logiku servisa izolovano od stvarne mrežne komunikacije.
 */
@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReportServiceClient reportServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;
    private ReportDTO mockReport;
    private UserDTO mockUser;

    @BeforeEach
    void setUp() {
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setReportId(10L);
        testComment.setUserId(5L);
        testComment.setText("Originalni tekst");

        mockReport = new ReportDTO();
        mockReport.setId(10L);
        mockReport.setTitle("Rupa na putu");

        mockUser = new UserDTO();
        mockUser.setId(5L);
        mockUser.setUsername("testuser");
    }

    // 1. Test za listanje komentara po reportu
    @Test
    @DisplayName("getCommentsByReport — vraća listu komentara")
    void getCommentsByReport_ShouldReturnList() {
        when(commentRepository.findByReportId(10L)).thenReturn(Arrays.asList(testComment));

        List<Comment> result = commentService.getCommentsByReport(10L);

        assertEquals(1, result.size());
        assertEquals("Originalni tekst", result.get(0).getText());
        verify(commentRepository, times(1)).findByReportId(10L);
    }

    // 2. Test za uspješno kreiranje komentara (oba servisa dostupna)
    @Test
    @DisplayName("saveComment — uspješno kreira kada report i user postoje")
    void saveComment_WhenBothServicesAvailable_ShouldSave() {
        when(reportServiceClient.getReportById(10L)).thenReturn(mockReport);
        when(userServiceClient.getUserById(5L)).thenReturn(mockUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment saved = commentService.saveComment(testComment);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        // Verifikacija: oba Feign klijenta pozvana po jednom
        verify(reportServiceClient, times(1)).getReportById(10L);
        verify(userServiceClient, times(1)).getUserById(5L);
        verify(commentRepository, times(1)).save(testComment);
    }

    // 3. Test — report-service nije dostupan (fallback vraca null)
    @Test
    @DisplayName("saveComment — baca ServiceUnavailableException kada report-service pao (fallback null)")
    void saveComment_WhenReportServiceDown_ShouldThrowServiceUnavailable() {
        when(reportServiceClient.getReportById(anyLong())).thenReturn(null);

        assertThrows(ServiceUnavailableException.class,
                () -> commentService.saveComment(testComment));

        verify(userServiceClient, never()).getUserById(anyLong());
        verify(commentRepository, never()).save(any());
    }

    // 4. Test — report ne postoji (report-service vraca 404 → FeignException.NotFound)
    @Test
    @DisplayName("saveComment — baca ResourceNotFoundException kada report ne postoji (404)")
    void saveComment_WhenReportNotFound_ShouldThrowResourceNotFoundException() {
        when(reportServiceClient.getReportById(anyLong()))
                .thenThrow(new feign.FeignException.NotFound(
                        "404", mock(feign.Request.class), null, null));

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.saveComment(testComment));

        verify(userServiceClient, never()).getUserById(anyLong());
        verify(commentRepository, never()).save(any());
    }

    // 5. Test — user-service nije dostupan (fallback vraca null)
    @Test
    @DisplayName("saveComment — baca ServiceUnavailableException kada user-service pao (fallback null)")
    void saveComment_WhenUserServiceDown_ShouldThrowServiceUnavailable() {
        when(reportServiceClient.getReportById(10L)).thenReturn(mockReport);
        when(userServiceClient.getUserById(anyLong())).thenReturn(null);

        assertThrows(ServiceUnavailableException.class,
                () -> commentService.saveComment(testComment));

        verify(commentRepository, never()).save(any());
    }

    // 6. Test — user ne postoji (user-service vraca 404 → FeignException.NotFound)
    @Test
    @DisplayName("saveComment — baca ResourceNotFoundException kada user ne postoji (404)")
    void saveComment_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(reportServiceClient.getReportById(10L)).thenReturn(mockReport);
        when(userServiceClient.getUserById(anyLong()))
                .thenThrow(new feign.FeignException.NotFound(
                        "404", mock(feign.Request.class), null, null));

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.saveComment(testComment));

        verify(commentRepository, never()).save(any());
    }

    // 5. Test za brisanje komentara
    @Test
    @DisplayName("deleteComment — uspješno briše postojeći komentar")
    void deleteComment_WhenExists_ShouldDelete() {
        when(commentRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> commentService.deleteComment(1L));

        verify(commentRepository, times(1)).deleteById(1L);
    }

    // 6. Test za neuspješno brisanje
    @Test
    @DisplayName("deleteComment — baca ResourceNotFoundException za nepostojeći ID")
    void deleteComment_WhenNotExists_ShouldThrowException() {
        when(commentRepository.existsById(99L)).thenReturn(false);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            commentService.deleteComment(99L);
        });

        assertEquals("Comment with ID 99 not found.", exception.getMessage());
        verify(commentRepository, never()).deleteById(anyLong());
    }

    // 7. Test za uspješan update
    @Test
    @DisplayName("updateComment — ažurira tekst postojećeg komentara")
    void updateComment_WhenExists_ShouldUpdateText() {
        Comment updatedDetails = new Comment();
        updatedDetails.setText("Novi, izmijenjeni tekst");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Comment result = commentService.updateComment(1L, updatedDetails);

        assertEquals("Novi, izmijenjeni tekst", result.getText());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    // 8. Test za neuspješan update
    @Test
    @DisplayName("updateComment — baca ResourceNotFoundException za nepostojeći ID")
    void updateComment_WhenNotExists_ShouldThrowException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.updateComment(99L, new Comment());
        });

        verify(commentRepository, never()).save(any(Comment.class));
    }
}