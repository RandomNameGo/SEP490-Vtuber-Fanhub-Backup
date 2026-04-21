package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.LoginRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;
import com.sep490.vtuber_fanhub.dto.responses.TokenValidationResponse;
import com.sep490.vtuber_fanhub.services.AuthService;
import com.sep490.vtuber_fanhub.services.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("vhub/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok().body(APIResponse.<LoginResponse>builder()
                .success(true)
                .message("Success")
                .data(authService.login(request.getUsername(), request.getPassword()))
                .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam("refresh-token") String token) {
        return ResponseEntity.ok().body(APIResponse.<LoginResponse>builder()
                .success(true)
                .message("Success")
                .data(refreshTokenService.createNewToken(token))
                .build()
        );
    }

    @PostMapping("/system-account-login")
    public ResponseEntity<?> systemAccountLogin(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok().body(APIResponse.<LoginResponse>builder()
                .success(true)
                .message("Success")
                .data(authService.SystemAccountLogin(request.getUsername(), request.getPassword()))
                .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        TokenValidationResponse validationResponse = authService.validateToken();

        return ResponseEntity.ok().body(APIResponse.<TokenValidationResponse>builder()
                .success(true)
                .message("Token validation successful")
                .data(validationResponse)
                .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Logout successful")
                .data("Token has been blacklisted")
                .build()
        );
    }
}
