package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreateBanMemberRequest;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.BanMember;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.BanMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BanMemberServiceImplTest {

    @Mock
    private BanMemberRepository banMemberRepository;

    @Mock
    private FanHubMemberRepository fanHubMemberRepository;

    @Mock
    private FanHubRepository fanHubRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BanMemberServiceImpl banMemberService;

    private User currentUser;
    private FanHub fanHub;
    private FanHubMember targetMember;
    private CreateBanMemberRequest request;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setRole("VTUBER");
        currentUser.setUsername("vtuber_owner");

        fanHub = new FanHub();
        fanHub.setId(1L);
        fanHub.setHubName("Test FanHub");
        fanHub.setOwnerUser(currentUser);

        User memberUser = new User();
        memberUser.setId(2L);
        memberUser.setUsername("member_user");

        targetMember = new FanHubMember();
        targetMember.setId(10L);
        targetMember.setHub(fanHub);
        targetMember.setUser(memberUser);

        request = new CreateBanMemberRequest();
        request.setFanHubMemberId(10L);
        request.setReason("Inappropriate behavior");
        request.setBanType("COMMENT");
        request.setBannedUntil(Instant.now().plusSeconds(3600));
    }

    @Test
    void banFanHubMember_Success_ByOwner() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.of(targetMember));
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));

        String result = banMemberService.banFanHubMember(request);

        assertEquals("Member banned successfully", result);
        verify(banMemberRepository).save(any(BanMember.class));
        verify(notificationService).sendMemberBannedNotification(
                eq(2L), eq(1L), eq("Test FanHub"), eq("Inappropriate behavior")
        );
    }

    @Test
    void banFanHubMember_Success_ByModerator() {
        User moderatorUser = new User();
        moderatorUser.setId(3L);
        moderatorUser.setRole("USER");
        
        FanHubMember moderatorMember = new FanHubMember();
        moderatorMember.setRoleInHub("MODERATOR");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(moderatorUser);
        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.of(targetMember));
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 3L)).thenReturn(Optional.of(moderatorMember));

        String result = banMemberService.banFanHubMember(request);

        assertEquals("Member banned successfully", result);
        verify(banMemberRepository).save(any(BanMember.class));
    }

    @Test
    void banFanHubMember_Failure_MemberNotFound() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> banMemberService.banFanHubMember(request));
    }

    @Test
    void banFanHubMember_Failure_AccessDenied() {
        User regularUser = new User();
        regularUser.setId(4L);
        regularUser.setRole("USER");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(regularUser);
        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.of(targetMember));
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 4L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> banMemberService.banFanHubMember(request));
    }

    @Test
    void banFanHubMember_Failure_HubNotFound() {
        // In the code, it uses fanHubRepository.findById(fanHubId). If it returns empty, it returns false for isOwner
        // and then checks moderator status. If both are false, it throws AccessDeniedException.
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.of(targetMember));
        when(fanHubRepository.findById(1L)).thenReturn(Optional.empty());
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> banMemberService.banFanHubMember(request));
    }
}
