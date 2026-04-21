package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.SystemAnalyticResponse;
import com.sep490.vtuber_fanhub.services.SystemAnalyticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("vhub/api/v1/admin/analytics")
@RequiredArgsConstructor
public class SystemAnalyticController {

    private final SystemAnalyticService systemAnalyticService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<SystemAnalyticResponse>> getSystemAnalytics() {
        return ResponseEntity.ok(APIResponse.<SystemAnalyticResponse>builder()
                .success(true)
                .message("Successfully fetched system analytics")
                .data(systemAnalyticService.getSystemAnalytics())
                .build());
    }
}
