package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateReportPostRequest;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.Post;
import com.sep490.vtuber_fanhub.models.ReportPost;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.PostRepository;
import com.sep490.vtuber_fanhub.repositories.ReportPostRepository;
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
public class ReportPostServiceImplTest {

    @Mock
    private ReportPostRepository reportPostRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ReportPostServiceImpl reportPostService;

    private User currentUser;
    private Post post;
    private CreateReportPostRequest request;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        post = new Post();
        post.setId(100L);
        post.setTitle("Test Post");

        request = new CreateReportPostRequest();
        request.setPostId(100L);
        request.setReason("SPAM");
    }

    @Test
    void createReportPost_Success() {
        // Arrange
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        // Act
        String result = reportPostService.createReportPost(request);

        // Assert
        assertEquals("Report post sent successfully", result);
        verify(reportPostRepository, times(1)).save(any(ReportPost.class));
    }

    @Test
    void createReportPost_PostNotFound_ThrowsNotFoundException() {
        // Arrange
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postRepository.findById(100L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            reportPostService.createReportPost(request);
        });

        assertEquals("Post not found", exception.getMessage());
        verify(reportPostRepository, never()).save(any(ReportPost.class));
    }

    @Test
    void createReportPost_AuthServiceReturnsNull_ShouldStillAttemptIfUserIsNull() {
        // In some cases authService might return null if token is invalid but reached here
        // Arrange
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(null);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        // Act
        String result = reportPostService.createReportPost(request);

        // Assert
        assertEquals("Report post sent successfully", result);
        verify(reportPostRepository, times(1)).save(any(ReportPost.class));
    }

    @Test
    void createReportPost_VerifyReportPostFields() {
        // Arrange
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        // Act
        reportPostService.createReportPost(request);

        // Assert
        verify(reportPostRepository).save(argThat(report -> 
            report.getPost().equals(post) &&
            report.getReportedBy().equals(currentUser) &&
            report.getReason().equals("SPAM") &&
            "PENDING".equals(report.getStatus()) &&
            report.getCreatedAt() != null
        ));
    }

    @Test
    void createReportPost_WithDifferentReason() {
        // Arrange
        request.setReason("INAPPROPRIATE_CONTENT");
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(postRepository.findById(100L)).thenReturn(Optional.of(post));

        // Act
        String result = reportPostService.createReportPost(request);

        // Assert
        assertEquals("Report post sent successfully", result);
        verify(reportPostRepository).save(argThat(report -> 
            report.getReason().equals("INAPPROPRIATE_CONTENT")
        ));
    }
}
