package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.FanHub;
import com.sep490.vtuber_fanhub.models.FanHubMember;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.FanHubMemberRepository;
import com.sep490.vtuber_fanhub.repositories.FanHubRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FanHubMemberServiceImplTest {

    @Mock
    private FanHubMemberRepository fanHubMemberRepository;

    @Mock
    private FanHubRepository fanHubRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AuthService authService;

    @Mock
    private BanMemberService banMemberService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FanHubMemberServiceImpl fanHubMemberService;

    private User currentUser;
    private FanHub fanHub;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setRole("USER");

        fanHub = new FanHub();
        fanHub.setId(1L);
        fanHub.setRequiresApproval(false);
        User owner = new User();
        owner.setId(2L);
        owner.setRole("VTUBER");
        fanHub.setOwnerUser(owner);
    }

    // --- joinFanHubMember ---

    @Test
    void joinFanHubMember_Success_AutoJoin() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        String result = fanHubMemberService.joinFanHubMember(1L);

        assertEquals("Joined FanHub successfully", result);
        verify(fanHubMemberRepository).save(any(FanHubMember.class));
    }

    @Test
    void joinFanHubMember_Success_PendingApproval() {
        fanHub.setRequiresApproval(true);
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        String result = fanHubMemberService.joinFanHubMember(1L);

        assertEquals("Join request submitted. Awaiting approval.", result);
        verify(fanHubMemberRepository).save(argThat(member -> "PENDING".equals(member.getStatus())));
    }

    @Test
    void joinFanHubMember_Failure_NotFound() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> fanHubMemberService.joinFanHubMember(1L));
    }

    @Test
    void joinFanHubMember_Failure_AlreadyMember() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.of(new FanHubMember()));

        String result = fanHubMemberService.joinFanHubMember(1L);

        assertEquals("User is already a member of this FanHub", result);
        verify(fanHubMemberRepository, never()).save(any());
    }

    @Test
    void joinFanHubMember_Failure_Banned() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        doThrow(new AccessDeniedException("Banned")).when(banMemberService).checkBanStatus(eq(1L), eq(1L), anyList());

        assertThrows(AccessDeniedException.class, () -> fanHubMemberService.joinFanHubMember(1L));
    }

    // --- addModerator ---

    @Test
    void addModerator_Success() {
        User owner = fanHub.getOwnerUser();
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(owner);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        
        FanHubMember member = new FanHubMember();
        member.setId(10L);
        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.of(member));

        String result = fanHubMemberService.addModerator(1L, List.of(10L));

        assertEquals("Set moderator successfully", result);
        assertEquals("MODERATOR", member.getRoleInHub());
        verify(fanHubMemberRepository).save(member);
    }

    @Test
    void addModerator_Failure_NotOwner() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));

        assertThrows(AccessDeniedException.class, () -> fanHubMemberService.addModerator(1L, List.of(10L)));
    }

    @Test
    void addModerator_Failure_HubNotFound() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> fanHubMemberService.addModerator(1L, List.of(10L)));
    }

    @Test
    void addModerator_Failure_MemberNotFound() {
        User owner = fanHub.getOwnerUser();
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(owner);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> fanHubMemberService.addModerator(1L, List.of(10L)));
    }

    // --- kickMember ---

    @Test
    void kickMember_Success_ByOwner() {
        User owner = fanHub.getOwnerUser();
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(owner);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));

        User targetUser = new User();
        targetUser.setId(3L);
        FanHubMember targetMember = new FanHubMember();
        targetMember.setId(10L);
        targetMember.setHub(fanHub);
        targetMember.setUser(targetUser);
        targetMember.setRoleInHub("MEMBER");

        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.of(targetMember));

        String result = fanHubMemberService.kickMember(1L, 10L);

        assertEquals("Member kicked successfully", result);
        verify(fanHubMemberRepository).delete(targetMember);
    }

    @Test
    void kickMember_Failure_NotAuthorized() {
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findByHubIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> fanHubMemberService.kickMember(1L, 10L));
    }

    @Test
    void kickMember_Failure_KickSelf() {
        User owner = fanHub.getOwnerUser();
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(owner);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));

        FanHubMember targetMember = new FanHubMember();
        targetMember.setId(10L);
        targetMember.setHub(fanHub);
        targetMember.setUser(owner); // Same as current user

        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.of(targetMember));

        assertThrows(AccessDeniedException.class, () -> fanHubMemberService.kickMember(1L, 10L));
    }

    @Test
    void kickMember_Failure_KickModerator() {
        User owner = fanHub.getOwnerUser();
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(owner);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));

        User targetUser = new User();
        targetUser.setId(3L);
        FanHubMember targetMember = new FanHubMember();
        targetMember.setId(10L);
        targetMember.setHub(fanHub);
        targetMember.setUser(targetUser);
        targetMember.setRoleInHub("MODERATOR");

        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.of(targetMember));

        assertThrows(AccessDeniedException.class, () -> fanHubMemberService.kickMember(1L, 10L));
    }

    @Test
    void kickMember_Failure_MemberNotFound() {
        User owner = fanHub.getOwnerUser();
        when(authService.getUserFromToken(httpServletRequest)).thenReturn(owner);
        when(fanHubRepository.findById(1L)).thenReturn(Optional.of(fanHub));
        when(fanHubMemberRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> fanHubMemberService.kickMember(1L, 10L));
    }
}
