package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.models.Enum.BanMemberType;
import com.sep490.vtuber_fanhub.models.Enum.HubMemberStatus;
import com.sep490.vtuber_fanhub.models.Enum.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    @GetMapping("/post-types")
    public ResponseEntity<?> getPostTypes() {
        List<PostType> postTypes = Arrays.asList(PostType.values());
        return ResponseEntity.ok().body(APIResponse.<List<PostType>>builder()
                .success(true)
                .message("Post types retrieved successfully")
                .data(postTypes)
                .build()
        );
    }

    @GetMapping("/hub-member-statuses")
    public ResponseEntity<?> getHubMemberStatuses() {
        List<HubMemberStatus> statuses = Arrays.asList(HubMemberStatus.values());
        return ResponseEntity.ok().body(APIResponse.<List<HubMemberStatus>>builder()
                .success(true)
                .message("Hub member statuses retrieved successfully")
                .data(statuses)
                .build()
        );
    }

    @GetMapping("/ban-member-types")
    public ResponseEntity<?> getBanMemberTypes() {
        List<BanMemberType> banTypes = Arrays.asList(BanMemberType.values());
        return ResponseEntity.ok().body(APIResponse.<List<BanMemberType>>builder()
                .success(true)
                .message("Ban member types retrieved successfully")
                .data(banTypes)
                .build()
        );
    }
}
