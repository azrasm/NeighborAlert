package com.projekat.interaction_service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projekat.interaction_service.exception.ResourceNotFoundException;
import com.projekat.interaction_service.model.Notification;
import com.projekat.interaction_service.repository.NotificationRepository;
import com.projekat.interaction_service.service.NotificationService;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(10L);
        testNotification.setMessage("Vaša prijava je odobrena!");
    }

    // 1. Test za listanje notifikacija po korisniku
    @Test
    void getNotificationsByUserId_ShouldReturnList() {
        //Kada repozitorij upitamo za usera 10, vrati listu sa jednom notifikacijom
        when(notificationRepository.findByUserId(10L)).thenReturn(Arrays.asList(testNotification));

        // WHEN: Pozovemo servis
        List<Notification> result = notificationService.getNotificationsByUserId(10L);

        // THEN: Provjera
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Vaša prijava je odobrena!", result.get(0).getMessage());
        verify(notificationRepository, times(1)).findByUserId(10L);
    }

    // 2. Test za brisanje notifikacije
    @Test
    void deleteNotification_WhenExists_ShouldDelete() {
        // Notifikacija postoji u bazi
        when(notificationRepository.existsById(1L)).thenReturn(true);

        // Poziv ne bi trebao baciti gresku
        assertDoesNotThrow(() -> notificationService.deleteNotification(1L));

        // Provjeri da je deleteById pozvan
        verify(notificationRepository, times(1)).deleteById(1L);
    }

    // 3. Test za neuspješno brisanje (ID ne postoji)
    @Test
    void deleteNotification_WhenNotExists_ShouldThrowException() {
        // Notifikacija ne postoji (id 99)
        when(notificationRepository.existsById(99L)).thenReturn(false);

        // Provjeri da li baca ResourceNotFoundException
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.deleteNotification(99L);
        });

        // Provjeri poruku greske
        assertEquals("Notification with ID 99 not found.", exception.getMessage());
        
        // Provjeri da metoda deleteById nikada nije pozvana (jer ID ne postoji)
        verify(notificationRepository, never()).deleteById(anyLong());
    }
}