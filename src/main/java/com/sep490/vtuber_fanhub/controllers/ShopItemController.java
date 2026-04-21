package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateShopItemRequest;
import com.sep490.vtuber_fanhub.dto.requests.PurchaseItemRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.PurchaseResponse;
import com.sep490.vtuber_fanhub.dto.responses.ShopItemResponse;
import com.sep490.vtuber_fanhub.services.ShopItemService;
import com.sep490.vtuber_fanhub.services.UserItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/shop-items")
@RequiredArgsConstructor
public class ShopItemController {

    private final ShopItemService shopItemService;

    private final UserItemService userItemService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> createShopItem(
            @RequestPart("request") @Valid CreateShopItemRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(shopItemService.createShopItem(request, image))
                .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllShopItems(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {

        List<ShopItemResponse> items = shopItemService.getAllShopItems(pageNo, pageSize, sortBy);
        return ResponseEntity.ok().body(APIResponse.<List<ShopItemResponse>>builder()
                .success(true)
                .message("Success")
                .data(items)
                .build()
        );
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> purchaseItem(@RequestBody @Valid PurchaseItemRequest request,
                                          HttpServletRequest httpRequest) {
        PurchaseResponse response = userItemService.purchaseItem(request, httpRequest);
        return ResponseEntity.ok().body(APIResponse.<PurchaseResponse>builder()
                .success(true)
                .message("Item purchased successfully")
                .data(response)
                .build()
        );
    }
}
