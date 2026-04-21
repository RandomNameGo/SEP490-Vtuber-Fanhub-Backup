package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubStrikeRequest;
import com.sep490.vtuber_fanhub.dto.responses.FanHubStrikeResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubStrike;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubStrikeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FanHubStrikeServiceImplTest {

    @Mock
    private FanHubStrikeRepository fanHubStrikeRepository;

    @Mock
    private FanHubRepository fanHubRepository;

    @Mock
    private AuthService authService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private FanHubStrikeServiceImpl fanHubStrikeService;

    private SystemAccount adminAccount;
    private FanHub fanHub;
    private User hubOwner;

    @BeforeEach
    void setUp() {
        adminAccount = new SystemAccount();
        adminAccount.setId(1L);
        adminAccount.setUsername("admin");

        hubOwner = new User();
        hubOwner.setId(10L);

        fanHub = new FanHub();
        fanHub.setId(1L);
        fanHub.setHubName("Test Hub");
        fanHub.setOwnerUser(hubOwner);
        fanHub.setStrikeCount(0);
    }

    @Test
    void createStrike_Success() {
        // Arrange
        CreateFanHubStrikeRequest request = new CreateFanHubStrikeRequest();
        request.setFanHubId(1L);
        request.setReason("Inappropriate content");

        when(authService.getSystemAccountFromToken(httpServletRequest)).thenReturn(adminAccount);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        
        FanHubStrike savedStrike = new FanHubStrike();
        savedStrike.setId(100L);
        savedStrike.setHub(fanHub);
        savedStrike.setReason(request.getReason());
        savedStrike.setStrikeBy(adminAccount);
        savedStrike.setIsActive(true);

        when(fanHubStrikeRepository.save(any(FanHubStrike.class))).thenReturn(savedStrike);

        // Act
        FanHubStrikeResponse response = fanHubStrikeService.createStrike(request, httpServletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getFanHubId());
        assertEquals(1, fanHub.getStrikeCount());
        verify(fanHubRepository).save(fanHub);
        verify(notificationService).sendFanHubStrikeNotification(eq(10L), eq(1L), eq("Test Hub"), eq(1), eq("Inappropriate content"));
        verify(fanHubStrikeRepository).save(any(FanHubStrike.class));
    }

    @Test
    void createStrike_Failure_FanHubNotFound() {
        // Arrange
        CreateFanHubStrikeRequest request = new CreateFanHubStrikeRequest();
        request.setFanHubId(99L);

        when(authService.getSystemAccountFromToken(httpServletRequest)).thenReturn(adminAccount);
        when(fanHubRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            fanHubStrikeService.createStrike(request, httpServletRequest);
        });
        verify(fanHubStrikeRepository, never()).save(any());
    }

    @Test
    void createStrike_Success_StrikeCountNull() {
        // Arrange
        fanHub.setStrikeCount(null);
        CreateFanHubStrikeRequest request = new CreateFanHubStrikeRequest();
        request.setFanHubId(1L);
        request.setReason("Reason");

        when(authService.getSystemAccountFromToken(httpServletRequest)).thenReturn(adminAccount);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        
        FanHubStrike savedStrike = new FanHubStrike();
        savedStrike.setId(101L);
        when(fanHubStrikeRepository.save(any(FanHubStrike.class))).thenReturn(savedStrike);

        // Act
        fanHubStrikeService.createStrike(request, httpServletRequest);

        // Assert
        assertEquals(1, fanHub.getStrikeCount());
        verify(fanHubRepository).save(fanHub);
    }

    @Test
    void createStrike_Success_IncrementStrikeCount() {
        // Arrange
        fanHub.setStrikeCount(2);
        CreateFanHubStrikeRequest request = new CreateFanHubStrikeRequest();
        request.setFanHubId(1L);
        request.setReason("Third strike");

        when(authService.getSystemAccountFromToken(httpServletRequest)).thenReturn(adminAccount);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        
        FanHubStrike savedStrike = new FanHubStrike();
        savedStrike.setId(102L);
        when(fanHubStrikeRepository.save(any(FanHubStrike.class))).thenReturn(savedStrike);

        // Act
        fanHubStrikeService.createStrike(request, httpServletRequest);

        // Assert
        assertEquals(3, fanHub.getStrikeCount());
        verify(fanHubRepository).save(fanHub);
    }
}
