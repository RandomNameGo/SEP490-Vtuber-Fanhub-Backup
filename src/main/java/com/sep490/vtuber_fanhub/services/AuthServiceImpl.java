package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;
import com.sep490.vtuber_fanhub.dto.responses.TokenValidationResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.SystemAccountRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JWTService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final SystemAccountRepository systemAccountRepository;

    private final HttpServletRequest httpServletRequest;

    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginResponse login(String username, String password) {
        Optional<User> user = userRepository.findByUsernameAndIsActive(username);
        if (user.isEmpty()) {
            throw new CustomAuthenticationException("Invalid username or password");
        }

        if(passwordEncoder.matches(password, user.get().getPasswordHash())){
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setId(user.get().getId());
            loginResponse.setUsername(user.get().getUsername());
            loginResponse.setToken(jwtService.generateToken(user.get()));
            loginResponse.setRefreshToken(refreshTokenService.createRefreshToken(user.get()));
            return loginResponse;
        } else {
            throw new CustomAuthenticationException("Invalid username or password");
        }
    }

    @Override
    public LoginResponse SystemAccountLogin(String username, String password) {

        Optional<SystemAccount> systemAccount = systemAccountRepository.findByUsername(username);

        if (systemAccount.isEmpty()) {
            throw new CustomAuthenticationException("Invalid username or password");
        }

        if(passwordEncoder.matches(password, systemAccount.get().getPasswordHash())){
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setId(systemAccount.get().getId());
            loginResponse.setUsername(systemAccount.get().getUsername());
            loginResponse.setToken(jwtService.generateTokenSystemAccount(systemAccount.get()));
            return loginResponse;
        } else {
            throw new CustomAuthenticationException("Invalid username or password");
        }

    }

    @Override
    public User getUserFromToken(HttpServletRequest httpServletRequest) {

        String token = jwtService.getCurrentToken(httpServletRequest);

        if (token == null) {
            throw new CustomAuthenticationException("Invalid token");
        }

        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<User> tokenUser = userRepository.findByUsernameAndIsActive(tokenUsername);
        if (tokenUser.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        return tokenUser.get();
    }

    @Override
    public SystemAccount getSystemAccountFromToken(HttpServletRequest httpServletRequest) {
        String token = jwtService.getCurrentToken(httpServletRequest);

        if (token == null) {
            throw new CustomAuthenticationException("Invalid token");
        }

        String tokenUsername = jwtService.getUsernameFromToken(token);

        Optional<SystemAccount> tokenSystemAccount = systemAccountRepository.findByUsername(tokenUsername);
        if (tokenSystemAccount.isEmpty()) {
            throw new CustomAuthenticationException("Authentication failed");
        }

        return tokenSystemAccount.get();
    }

    @Override
    public TokenValidationResponse validateToken() {
        String token = jwtService.getCurrentToken(httpServletRequest);

        if (token == null) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .expired(true)
                    .build();
        }

        boolean isValid = jwtService.isTokenValid(token);
        Date expirationTime = jwtService.getExpirationTimeFromToken(token);

        TokenValidationResponse.TokenValidationResponseBuilder builder = TokenValidationResponse.builder()
                .valid(isValid)
                .expired(!isValid)
                .expiresAt(expirationTime != null ? expirationTime.toInstant() : null);

        if (isValid) {
            try {
                String username = jwtService.getUsernameFromToken(token);
                Optional<User> user = userRepository.findByUsernameAndIsActive(username);
                user.ifPresent(u -> {
                    builder.userId(u.getId());
                    builder.username(u.getUsername());
                    builder.role(u.getRole());
                });
            } catch (Exception e) {
            }
        }

        return builder.build();
    }

    @Override
    public void logout() {
        String token = jwtService.getCurrentToken(httpServletRequest);
        if (token != null) {
            Date expirationTime = jwtService.getExpirationTimeFromToken(token);
            if (expirationTime != null) {
                long ttl = expirationTime.getTime() - System.currentTimeMillis();
                if (ttl > 0) {

                    redisTemplate.opsForValue().set(
                            "blacklist:" + token,
                            "blacklisted",
                            ttl + 60000,
                            TimeUnit.MILLISECONDS
                    );
                }
            }
        }
    }


}
