package com.sep490.vtuber_fanhub.controllers;


import com.sep490.vtuber_fanhub.dto.requests.CreateVTuberApplication;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.VTuberApplicationResponse;
import com.sep490.vtuber_fanhub.services.VTuberApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/vtuber-application")
@RequiredArgsConstructor
public class VTuberApplicationController {

    private final VTuberApplicationService vtuberApplicationService;

    @PostMapping("/register-vtuber")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> registerVTuber(@RequestBody @Valid CreateVTuberApplication request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(vtuberApplicationService.createVTuberApplication(request))
                .build()
        );
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getMyVTuberApplications() {
        return ResponseEntity.ok().body(APIResponse.<List<VTuberApplicationResponse>>builder()
                .success(true)
                .message("Success")
                .data(vtuberApplicationService.getMyVTuberApplications())
                .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> getAllVTuberApplications(@RequestParam(defaultValue = "0") int pageNo,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok().body(APIResponse.<List<VTuberApplicationResponse>>builder()
                .success(true)
                .message("Success")
                .data(vtuberApplicationService.getAllVTuberApplications(pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PutMapping("/review-application")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> reviewVTuber(@RequestParam long vTuberApplicationId,
                                          @RequestParam String status,
                                          @RequestParam String reason) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(vtuberApplicationService.reviewVTuberApplication(vTuberApplicationId, status, reason))
                .build()
        );

    }
}
