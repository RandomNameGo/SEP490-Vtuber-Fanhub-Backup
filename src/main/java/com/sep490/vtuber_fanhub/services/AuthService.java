package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.responses.LoginResponse;
import com.sep490.vtuber_fanhub.dto.responses.TokenValidationResponse;
import com.sep490.vtuber_fanhub.models.SystemAccount;
import com.sep490.vtuber_fanhub.models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginResponse login(String username, String password);

    LoginResponse SystemAccountLogin(String username, String password);

    User getUserFromToken(HttpServletRequest httpServletRequest);

    SystemAccount getSystemAccountFromToken(HttpServletRequest httpServletRequest);

    TokenValidationResponse validateToken();

    void logout();
}
