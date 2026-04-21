package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubStrikeRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubStrikeResponse;
import com.sep490.vtuber_fanhub.services.FanHubStrikeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("vhub/api/v1/fan-hub-strike")
@RequiredArgsConstructor
public class FanHubStrikeController {

    private final FanHubStrikeService fanHubStrikeService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> createStrike(@RequestBody @Valid CreateFanHubStrikeRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok().body(APIResponse.<FanHubStrikeResponse>builder()
                .success(true)
                .message("Strike created successfully")
                .data(fanHubStrikeService.createStrike(request, httpServletRequest))
                .build()
        );
    }
}
