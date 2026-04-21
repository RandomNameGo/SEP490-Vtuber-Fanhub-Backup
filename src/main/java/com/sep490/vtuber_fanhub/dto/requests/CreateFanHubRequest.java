package com.sep490.vtuber_fanhub.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateFanHubRequest {

    @NotBlank(message = "Hub name must not be blank")
    @Size(max = 100, message = "Hub name must not exceed 100 characters")
    private String hubName;

    @NotBlank(message = "Subdomain must not be blank")
    @Size(max = 100, message = "Subdomain must not exceed 100 characters")
    private String subdomain;

    private String description;

    @Size(max = 20, message = "Theme color must not exceed 20 characters")
    private String themeColor;

    private List<String> category;

    private Boolean isPrivate;

    private Boolean requiresApproval;
}
