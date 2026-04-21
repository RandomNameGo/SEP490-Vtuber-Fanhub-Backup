package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateBannerItemRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreateBannerRequest;
import com.sep490.vtuber_fanhub.dto.requests.GachaBannerItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.BannerItemResponse;
import com.sep490.vtuber_fanhub.dto.responses.BannerResponse;
import com.sep490.vtuber_fanhub.dto.responses.GachaResultResponse;
import com.sep490.vtuber_fanhub.services.BannerItemService;
import com.sep490.vtuber_fanhub.services.BannerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    private final BannerItemService bannerItemService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> createBanner(
            @RequestPart("request") @Valid CreateBannerRequest request,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(bannerService.createBanner(request, bannerImage))
                .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBanners(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {

        List<BannerResponse> banners = bannerService.getAllBanners(pageNo, pageSize, sortBy);
        return ResponseEntity.ok().body(APIResponse.<List<BannerResponse>>builder()
                .success(true)
                .message("Success")
                .data(banners)
                .build()
        );
    }

    //only one banner active in the time
    @GetMapping("/active")
    public ResponseEntity<?> getActiveBanner() {
        return ResponseEntity.ok().body(APIResponse.<BannerResponse>builder()
                .success(true)
                .message("Success")
                .data(bannerService.getActiveBanner())
                .build()
        );
    }

    @PostMapping("/items/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> createBannerItem(
            @RequestPart("request") @Valid CreateBannerItemRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(bannerItemService.createBannerItem(request, image))
                .build()
        );
    }

    @GetMapping("/items/all")
    public ResponseEntity<?> getBannerItemsByBannerId(
            @RequestParam Long bannerId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {

        List<BannerItemResponse> items = bannerItemService.getBannerItemsByBannerId(bannerId, pageNo, pageSize, sortBy);
        return ResponseEntity.ok().body(APIResponse.<List<BannerItemResponse>>builder()
                .success(true)
                .message("Success")
                .data(items)
                .build()
        );
    }

    @PostMapping("/gacha")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> gachaBannerItem(@RequestBody @Valid GachaBannerItemRequest request,
                                              HttpServletRequest httpRequest) {
        GachaResultResponse response = bannerItemService.gachaBannerItem(request, httpRequest);
        return ResponseEntity.ok().body(APIResponse.<GachaResultResponse>builder()
                .success(true)
                .message("Gacha successful!")
                .data(response)
                .build()
        );
    }
}
