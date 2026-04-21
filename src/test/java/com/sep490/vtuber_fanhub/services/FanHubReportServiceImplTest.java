package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.FanHubReport;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubReportRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FanHubReportServiceImplTest {

    @Mock
    private FanHubReportRepository fanHubReportRepository;

    @Mock
    private FanHubRepository fanHubRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private FanHubReportServiceImpl fanHubReportService;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole("ADMIN");

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole("USER");
    }

    @Test
    void bulkResolveFanHubReports_Success() {
        // Arrange
        List<Long> reportIds = Arrays.asList(1L, 2L);
        String resolveMessage = "Resolved";
        
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(adminUser);
        
        FanHubReport report1 = new FanHubReport();
        report1.setId(1L);
        report1.setStatus("PENDING");
        
        FanHubReport report2 = new FanHubReport();
        report2.setId(2L);
        report2.setStatus("PENDING");
        
        when(fanHubReportRepository.findById(1L)).thenReturn(Optional.of(report1));
        when(fanHubReportRepository.findById(2L)).thenReturn(Optional.of(report2));

        // Act
        String result = fanHubReportService.bulkResolveFanHubReports(reportIds, resolveMessage);

        // Assert
        assertEquals("Successfully resolved 2 report(s)", result);
        assertEquals("RESOLVED", report1.getStatus());
        assertEquals("RESOLVED", report2.getStatus());
        assertEquals(resolveMessage, report1.getResolveMessage());
        assertEquals(resolveMessage, report2.getResolveMessage());
        verify(fanHubReportRepository, times(2)).save(any(FanHubReport.class));
    }

    @Test
    void bulkResolveFanHubReports_Failure_NotAdmin() {
        // Arrange
        List<Long> reportIds = Arrays.asList(1L);
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(regularUser);

        // Act & Assert
        CustomAuthenticationException exception = assertThrows(CustomAuthenticationException.class, () -> {
            fanHubReportService.bulkResolveFanHubReports(reportIds, "Message");
        });
        assertEquals("Only ADMIN can resolve FanHub reports", exception.getMessage());
        verify(fanHubReportRepository, never()).save(any());
    }

    @Test
    void bulkResolveFanHubReports_Failure_EmptyIds() {
        // Arrange
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(adminUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fanHubReportService.bulkResolveFanHubReports(List.of(), "Message");
        });
        assertEquals("Report IDs cannot be empty", exception.getMessage());
    }

    @Test
    void bulkResolveFanHubReports_Success_PartialFound() {
        // Arrange
        List<Long> reportIds = Arrays.asList(1L, 3L);
        String resolveMessage = "Resolved";
        
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(adminUser);
        
        FanHubReport report1 = new FanHubReport();
        report1.setId(1L);
        
        when(fanHubReportRepository.findById(1L)).thenReturn(Optional.of(report1));
        when(fanHubReportRepository.findById(3L)).thenReturn(Optional.empty());

        // Act
        String result = fanHubReportService.bulkResolveFanHubReports(reportIds, resolveMessage);

        // Assert
        assertEquals("Successfully resolved 1 report(s)", result);
        assertEquals("RESOLVED", report1.getStatus());
        verify(fanHubReportRepository, times(1)).save(any(FanHubReport.class));
    }

    @Test
    void bulkResolveFanHubReports_Success_NoneFound() {
        // Arrange
        List<Long> reportIds = Arrays.asList(100L);
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(adminUser);
        when(fanHubReportRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        String result = fanHubReportService.bulkResolveFanHubReports(reportIds, "Message");

        // Assert
        assertEquals("Successfully resolved 0 report(s)", result);
        verify(fanHubReportRepository, never()).save(any());
    }
}
