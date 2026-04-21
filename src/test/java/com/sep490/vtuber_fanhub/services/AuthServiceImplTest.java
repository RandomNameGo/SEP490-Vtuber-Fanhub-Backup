package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.SystemAccountRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private SystemAccountRepository systemAccountRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private SystemAccount systemAccount;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("encodedPassword");
        user.setIsActive(true);

        systemAccount = new SystemAccount();
        systemAccount.setId(1L);
        systemAccount.setUsername("admin");
        systemAccount.setPasswordHash("encodedAdminPassword");
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsernameAndIsActive("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refreshToken");

        LoginResponse response = authService.login("testuser", "password");

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("jwtToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByUsernameAndIsActive("nonexistent")).thenReturn(Optional.empty());

        assertThrows(CustomAuthenticationException.class, () -> authService.login("nonexistent", "password"));
    }

    @Test
    void login_InvalidPassword() {
        when(userRepository.findByUsernameAndIsActive("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(CustomAuthenticationException.class, () -> authService.login("testuser", "wrongpassword"));
    }

    @Test
    void login_UserInactive() {
        // findByUsernameAndIsActive returns empty if inactive
        when(userRepository.findByUsernameAndIsActive("inactive")).thenReturn(Optional.empty());

        assertThrows(CustomAuthenticationException.class, () -> authService.login("inactive", "password"));
    }

    @Test
    void systemAccountLogin_Success() {
        when(systemAccountRepository.findByUsername("admin")).thenReturn(Optional.of(systemAccount));
        when(passwordEncoder.matches("adminPass", "encodedAdminPassword")).thenReturn(true);
        when(jwtService.generateTokenSystemAccount(systemAccount)).thenReturn("systemToken");

        LoginResponse response = authService.SystemAccountLogin("admin", "adminPass");

        assertNotNull(response);
        assertEquals("admin", response.getUsername());
        assertEquals("systemToken", response.getToken());
    }

    @Test
    void systemAccountLogin_NotFound() {
        when(systemAccountRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(CustomAuthenticationException.class, () -> authService.SystemAccountLogin("unknown", "pass"));
    }

    @Test
    void systemAccountLogin_InvalidPassword() {
        when(systemAccountRepository.findByUsername("admin")).thenReturn(Optional.of(systemAccount));
        when(passwordEncoder.matches("wrong", "encodedAdminPassword")).thenReturn(false);

        assertThrows(CustomAuthenticationException.class, () -> authService.SystemAccountLogin("admin", "wrong"));
    }

    @Test
    void systemAccountLogin_EmptyUsername() {
        assertThrows(CustomAuthenticationException.class, () -> authService.SystemAccountLogin("", "pass"));
    }

    @Test
    void logout_Success() {
        String token = "validToken";
        Date expiration = new Date(System.currentTimeMillis() + 3600000); // 1 hour later
        when(jwtService.getCurrentToken(httpServletRequest)).thenReturn(token);
        when(jwtService.getExpirationTimeFromToken(token)).thenReturn(expiration);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.logout();

        verify(valueOperations).set(eq("blacklist:" + token), eq("blacklisted"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void logout_NoToken() {
        when(jwtService.getCurrentToken(httpServletRequest)).thenReturn(null);

        authService.logout();

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void logout_ExpiredToken() {
        String token = "expiredToken";
        Date expiration = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
        when(jwtService.getCurrentToken(httpServletRequest)).thenReturn(token);
        when(jwtService.getExpirationTimeFromToken(token)).thenReturn(expiration);

        authService.logout();

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void logout_NoExpiration() {
        String token = "tokenNoExp";
        when(jwtService.getCurrentToken(httpServletRequest)).thenReturn(token);
        when(jwtService.getExpirationTimeFromToken(token)).thenReturn(null);

        authService.logout();

        verify(redisTemplate, never()).opsForValue();
    }
}
