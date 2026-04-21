package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubReportRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubWithReportsResponse;
import com.sep490.vtuber_fanhub.services.FanHubReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/fan-hub-report")
@RequiredArgsConstructor
public class FanHubReportController {

    private final FanHubReportService fanHubReportService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> createFanHubReport(@RequestBody @Valid CreateFanHubReportRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubReportService.createFanHubReport(request))
                .build()
        );
    }

    @GetMapping("/with-reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> getAllFanHubsWithReports(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<FanHubWithReportsResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubReportService.getAllFanHubsWithReports(pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PutMapping("/bulk-resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> bulkResolveFanHubReports(
            @RequestParam List<Long> reportIds,
            @RequestParam(required = false) String resolveMessage) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubReportService.bulkResolveFanHubReports(reportIds, resolveMessage))
                .build()
        );
    }
}
