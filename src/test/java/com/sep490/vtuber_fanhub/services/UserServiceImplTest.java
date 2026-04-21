package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.ChangePasswordRequest;
import com.sep490.vtuber_fanhub.dto.requests.SelectUserBadgeRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateUserRequest;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.models.UserBadge;
import com.sep490.vtuber_fanhub.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private UserDailyMissionRepository userDailyMissionRepository;

    @Mock
    private AuthService authService;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private FanHubMemberRepository fanHubMemberRepository;

    @Mock
    private PostCommentGiftRepository postCommentGiftRepository;

    @Mock
    private UserBadgeService userBadgeService;

    @InjectMocks
    private UserServiceImpl userService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");
        currentUser.setPasswordHash("encodedOldPassword");
        currentUser.setDisplayName("Old Name");
        currentUser.setBio("Old Bio");
    }

    @Test
    void updateUser_SuccessAllFields() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDisplayName("New Name");
        request.setBio("New Bio");
        request.setTranslateLanguage("en");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);

        String result = userService.updateUser(request);

        assertEquals("Updated user successfully", result);
        assertEquals("New Name", currentUser.getDisplayName());
        assertEquals("New Bio", currentUser.getBio());
        assertEquals("en", currentUser.getTranslateLanguage());
        verify(userRepository).save(currentUser);
    }

    @Test
    void updateUser_SuccessPartialFields() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setDisplayName("New Name");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);

        String result = userService.updateUser(request);

        assertEquals("Updated user successfully", result);
        assertEquals("New Name", currentUser.getDisplayName());
        assertEquals("Old Bio", currentUser.getBio());
        verify(userRepository).save(currentUser);
    }

    @Test
    void updateUser_NoFields() {
        UpdateUserRequest request = new UpdateUserRequest();

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);

        String result = userService.updateUser(request);

        assertEquals("Updated user successfully", result);
        verify(userRepository).save(currentUser);
    }

    @Test
    void updateUser_TokenInvalid() {
        when(authService.getUserFromToken(httpServletRequest)).thenThrow(new CustomAuthenticationException("Invalid token"));

        assertThrows(CustomAuthenticationException.class, () -> userService.updateUser(new UpdateUserRequest()));
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");
        request.setConfirmPassword("newPass123");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPassword");

        String result = userService.changePassword(request);

        assertEquals("Changed password successfully", result);
        assertEquals("encodedNewPassword", currentUser.getPasswordHash());
        verify(userRepository).save(currentUser);
    }

    @Test
    void changePassword_IncorrectOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPass");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(passwordEncoder.matches("wrongPass", "encodedOldPassword")).thenReturn(false);

        String result = userService.changePassword(request);

        assertEquals("Old password is incorrect", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_NewPasswordMismatch() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");
        request.setConfirmPassword("differentPass");

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(passwordEncoder.matches("oldPass", "encodedOldPassword")).thenReturn(true);

        String result = userService.changePassword(request);

        assertEquals("New password and confirm password do not match", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_TokenInvalid() {
        when(authService.getUserFromToken(httpServletRequest)).thenThrow(new CustomAuthenticationException("Invalid token"));

        assertThrows(CustomAuthenticationException.class, () -> userService.changePassword(new ChangePasswordRequest()));
    }

    @Test
    void updateUserBadgeDisplay_Success() {
        SelectUserBadgeRequest request = new SelectUserBadgeRequest();
        request.setUserBadgeIds(Arrays.asList(10L, 20L));

        UserBadge ub1 = new UserBadge(); ub1.setId(10L); ub1.setIsDisplay(false);
        UserBadge ub2 = new UserBadge(); ub2.setId(20L); ub2.setIsDisplay(false);
        UserBadge ub3 = new UserBadge(); ub3.setId(30L); ub3.setIsDisplay(true);

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(userBadgeRepository.findByUserId(1L)).thenReturn(Arrays.asList(ub1, ub2, ub3));

        String result = userService.updateUserBadgeDisplay(request);

        assertEquals("Updated badge display successfully", result);
        assertTrue(ub1.getIsDisplay());
        assertTrue(ub2.getIsDisplay());
        assertFalse(ub3.getIsDisplay());
        verify(userBadgeRepository).saveAll(any());
    }

    @Test
    void updateUserBadgeDisplay_TooManyBadges() {
        SelectUserBadgeRequest request = new SelectUserBadgeRequest();
        request.setUserBadgeIds(Arrays.asList(1L, 2L, 3L, 4L));

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);

        String result = userService.updateUserBadgeDisplay(request);

        assertEquals("Maximum 3 badges can be displayed", result);
        verify(userBadgeRepository, never()).saveAll(any());
    }

    @Test
    void updateUserBadgeDisplay_EmptyList() {
        SelectUserBadgeRequest request = new SelectUserBadgeRequest();
        request.setUserBadgeIds(new ArrayList<>());

        UserBadge ub1 = new UserBadge(); ub1.setId(1L); ub1.setIsDisplay(true);

        when(authService.getUserFromToken(httpServletRequest)).thenReturn(currentUser);
        when(userBadgeRepository.findByUserId(1L)).thenReturn(Arrays.asList(ub1));

        String result = userService.updateUserBadgeDisplay(request);

        assertEquals("Updated badge display successfully", result);
        assertFalse(ub1.getIsDisplay());
        verify(userBadgeRepository).saveAll(any());
    }

    @Test
    void updateUserBadgeDisplay_TokenInvalid() {
        when(authService.getUserFromToken(httpServletRequest)).thenThrow(new CustomAuthenticationException("Invalid token"));

        assertThrows(CustomAuthenticationException.class, () -> userService.updateUserBadgeDisplay(new SelectUserBadgeRequest()));
    }
}
