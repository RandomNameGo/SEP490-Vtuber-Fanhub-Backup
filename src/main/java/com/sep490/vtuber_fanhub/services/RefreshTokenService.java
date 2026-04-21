package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;
import com.sep490.vtuber_fanhub.exceptions.CustomAuthenticationException;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.RefreshToken;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    private final JWTService jwtService;

    public String createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));
        refreshTokenRepository.save(token);
        return token.getToken();
    }

    public LoginResponse createNewToken(String token){
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if(refreshTokenOpt.isEmpty()){
            throw new NotFoundException("Refresh token not found");
        }

        RefreshToken currentRefreshToken = refreshTokenOpt.get();

        if(isTokenExpired(currentRefreshToken)){
            throw new CustomAuthenticationException("Refresh token is expired");
        }

        User user = currentRefreshToken.getUser();
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(user.getId());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setToken(jwtService.generateToken(user));
        Instant now = Instant.now();
        Instant expiryDate = currentRefreshToken.getExpiryDate();
        if (now.plus(2, ChronoUnit.DAYS).isAfter(expiryDate)) {
            loginResponse.setRefreshToken(this.createRefreshToken(user));
            refreshTokenRepository.delete(currentRefreshToken);
        } else {
            loginResponse.setRefreshToken(currentRefreshToken.getToken());
        }

        return loginResponse;
    }

    public boolean isTokenExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }}
