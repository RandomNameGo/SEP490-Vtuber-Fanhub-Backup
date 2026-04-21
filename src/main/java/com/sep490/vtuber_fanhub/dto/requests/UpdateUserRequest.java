package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Size(max = 512, message = "Translate language must not exceed 512 characters")
    private String translateLanguage;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;
}
