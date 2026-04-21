package com.sep490.vtuber_fanhub.dto.responses;

import lombok.Data;

@Data
public class LoginResponse {
    private long id;
    private String username;
    private String token;
    private String refreshToken;
}
