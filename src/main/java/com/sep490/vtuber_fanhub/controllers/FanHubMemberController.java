package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateBanMemberRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreateReportMemberRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.BanMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubMembershipResponse;
import com.sep490.vtuber_fanhub.dto.responses.FanHubMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberDetailResponse;
import com.sep490.vtuber_fanhub.dto.responses.ReportMemberResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberWithReportsResponse;
import com.sep490.vtuber_fanhub.dto.responses.MemberWithBansResponse;
import com.sep490.vtuber_fanhub.services.BanMemberService;
import com.sep490.vtuber_fanhub.services.FanHubMemberService;
import com.sep490.vtuber_fanhub.services.ReportMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/fan-hub-member")
@RequiredArgsConstructor
public class FanHubMemberController {

    private final FanHubMemberService fanHubMemberService;

    private final ReportMemberService reportMemberService;

    private final BanMemberService banMemberService;

    @PostMapping("/join/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> joinFanHub(@PathVariable long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.joinFanHubMember(fanHubId))
                .build()
        );
    }

    @GetMapping("/members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getFanHubMembers(
            @PathVariable long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(required = false) String username) {
        return ResponseEntity.ok().body(APIResponse.<List<FanHubMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getFanHubMembers(fanHubId, pageNo, pageSize, sortBy, username))
                .build()
        );
    }

    @GetMapping("/pending-members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getPendingFanHubMembers(
            @PathVariable long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "joinedAt") String sortBy) {
        return ResponseEntity.ok().body(APIResponse.<List<FanHubMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getPendingFanHubMembers(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PostMapping("/set-moderator/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> setModerator(@PathVariable long fanHubId, @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.addModerator(fanHubId, memberIds))
                .build()
        );
    }

    @PostMapping("/remove-moderator/{fanHubId}")
    @PreAuthorize("hasRole('VTUBER')")
    public ResponseEntity<?> removeModerator(@PathVariable long fanHubId, @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.removeModerator(fanHubId, memberIds))
                .build()
        );
    }

    @PutMapping("/review")
    @PreAuthorize("hasAnyRole('VTUBER', 'MODERATOR')")
    public ResponseEntity<?> reviewFanHubMember(
            @RequestParam long fanHubMemberId,
            @RequestParam String status) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.reviewFanHubMember(fanHubMemberId, status))
                .build()
        );
    }

    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> reportMember(@RequestBody CreateReportMemberRequest createReportMemberRequest) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.createReportMember(createReportMemberRequest))
                .build()
        );
    }

    @GetMapping("/reports/members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getReportMembersByFanHubId(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.getReportMembersByFanHubId(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PutMapping("/report/resolve")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> resolveReportMember(
            @RequestParam Long reportId,
            @RequestParam(required = false) String resolveMessage) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.resolveReportMember(reportId, resolveMessage))
                .build()
        );
    }

    @PostMapping("/ban")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> banMember(@RequestBody CreateBanMemberRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(banMemberService.banFanHubMember(request))
                .build()
        );
    }

    @GetMapping("/bans/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getActiveBansByHubId(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<BanMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(banMemberService.getActiveBansByHubId(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PutMapping("/ban/revoke")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> revokeBan(@RequestParam Long banId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(banMemberService.revokeBan(banId))
                .build()
        );
    }

    @PutMapping("/{fanHubId}/leave")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> leaveFanHub(@PathVariable long fanHubId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.leaveFanHub(fanHubId))
                .build()
        );
    }

    @PutMapping("/{fanHubId}/kick/{memberId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> kickMember(@PathVariable long fanHubId, @PathVariable long memberId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.kickMember(fanHubId, memberId))
                .build()
        );
    }

    @GetMapping("/members/{fanHubMemberId}/detail")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getMemberDetail(@PathVariable long fanHubMemberId) {
        return ResponseEntity.ok().body(APIResponse.<MemberDetailResponse>builder()
                .success(true)
                .message("Success")
                .data(fanHubMemberService.getMemberDetail(fanHubMemberId))
                .build()
        );
    }


    @GetMapping("/{fanHubId}/is-member")
    public ResponseEntity<?> checkIsUserMemberOfFanHub(@PathVariable Long fanHubId) {
        FanHubMembershipResponse membership = fanHubMemberService.checkUserMembership(fanHubId);
        return ResponseEntity.ok().body(APIResponse.<FanHubMembershipResponse>builder()
                .success(true)
                .message("Success")
                .data(membership)
                .build()
        );
    }

    @GetMapping("/reports/my-members-report")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getMyReportMembers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.getReportMembersByCurrentUser(pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/reports/pending-members/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getPendingReportMembers(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportMemberResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.getPendingReportMembersByFanHubId(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PutMapping("/reports/bulk-resolve")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> bulkResolveReportMembers(
            @RequestParam List<Long> reportIds,
            @RequestParam(required = false) String resolveMessage) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.bulkResolveReportMembers(reportIds, resolveMessage))
                .build()
        );
    }

    @GetMapping("/reports/members-with-reports/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getAllMembersWithReports(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<MemberWithReportsResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportMemberService.getAllMembersWithReports(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/bans/members-with-bans/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getAllMembersWithBans(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<MemberWithBansResponse>>builder()
                .success(true)
                .message("Success")
                .data(banMemberService.getAllMembersWithBans(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }
}
