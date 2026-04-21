package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.services.FanHubService;
import com.sep490.vtuber_fanhub.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final PostService postService;

    private final FanHubService fanHubService;

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "Post") String type) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(APIResponse.<Object>builder()
                    .success(false)
                    .message("Search keyword cannot be empty")
                    .build()
            );
        }

        String searchType = type.trim().toLowerCase();

        if ("post".equals(searchType)) {
            List<PostResponse> results = postService.searchPosts(keyword.trim(), pageNo, pageSize, sortBy);
            return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                    .success(true)
                    .message("Posts search successful")
                    .data(results)
                    .build()
            );
        } else if ("fanhub".equals(searchType) || "fan hub".equals(searchType.toLowerCase()) || "fan_hub".equals(searchType)) {
            List<FanHubResponse> results = fanHubService.searchFanHubs(keyword.trim(), pageNo, pageSize, sortBy);
            return ResponseEntity.ok().body(APIResponse.<List<FanHubResponse>>builder()
                    .success(true)
                    .message("FanHubs search successful")
                    .data(results)
                    .build()
            );
        } else {
            return ResponseEntity.badRequest().body(APIResponse.<Object>builder()
                    .success(false)
                    .message("Invalid search type. Use 'Post' or 'FanHub'")
                    .build()
            );
        }
    }
}
