package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.requests.UpdateFanHubRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubAnalyticsResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubResponse;
import com.sep490.vtuber_fanhub.services.FanHubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/fan-hub")
@RequiredArgsConstructor
public class FanHubController {

    private final FanHubService fanHubService;

    @PostMapping("/create")
    public ResponseEntity<?> createSystemAccount(@RequestBody @Valid CreateFanHubRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.createFanHub(request))
                .build()
        );
    }

    @PostMapping("/create/v2")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> createFanHubV2(
            @RequestPart("request") @Valid CreateFanHubRequest request,
            @RequestParam(value = "banner", required = false) MultipartFile banner,
            @RequestParam(value = "backgrounds", required = false) List<MultipartFile> backgrounds,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) throws Exception {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.createFanHubV2(request, banner, backgrounds, avatar))
                .build()
        );
    }

    @PutMapping("/update/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> updateFanHub(@PathVariable Long fanHubId, @RequestBody @Valid UpdateFanHubRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.updateFanHub(fanHubId, request))
                .build()
        );
    }

    @PostMapping("/upload-images/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> uploadFanHubBannerBackgroundAvatar(@PathVariable Long fanHubId,
                                                                @RequestParam(value = "banner", required = false) MultipartFile banner,
                                                                @RequestParam(value = "backgrounds", required = false) List<MultipartFile> backgrounds,
                                                                @RequestParam(value = "avatar", required = false) MultipartFile avatar) throws Exception {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.uploadFanHubBannerBackGroundAvatar(fanHubId, banner, backgrounds, avatar))
                .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllFanHubs(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean includePrivate) {

        return ResponseEntity.ok().body(APIResponse.<List<FanHubResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.getAllFanHubs(pageNo, pageSize, sortBy, includePrivate))
                .build()
        );
    }

    @GetMapping("/top")
    public ResponseEntity<?> getTopFanHubs(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {

        return ResponseEntity.ok().body(APIResponse.<List<FanHubResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.getTopFanHubs(pageNo, pageSize, category))
                .build()
        );
    }

    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<?> getFanHubBySubdomain(@PathVariable String subdomain) {
        return ResponseEntity.ok().body(APIResponse.<FanHubResponse>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.getFanHubBySubdomain(subdomain))
                .build()
        );
    }

    @GetMapping("/my-hubs")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getMyJoinedFanHubs(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<FanHubResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.getJoinedFanHubs(pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/my-hub-as-owner")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> getMyHubAsOwner() {
        return ResponseEntity.ok().body(APIResponse.<FanHubResponse>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.getMyHubAsOwner())
                .build()
        );
    }

    @GetMapping("/analytics/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> getFanHubAnalytics(@PathVariable Long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<FanHubAnalyticsResponse>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.getFanHubAnalytics(fanHubId))
                .build()
        );
    }

    @DeleteMapping("/delete/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> deleteFanHub(@PathVariable Long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.deleteFanHub(fanHubId))
                .build()
        );
    }

    @PutMapping("/deactivate/{fanHubId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> deactivateFanHub(@PathVariable Long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.deactivateFanHub(fanHubId))
                .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFanHubs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<FanHubResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubService.searchFanHubs(keyword, pageNo, pageSize, sortBy))
                .build()
        );
    }
}
