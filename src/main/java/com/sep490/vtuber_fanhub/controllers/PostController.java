package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreatePostCommentRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreatePostRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreatePollPostRequest;
import com.sep490.vtuber_fanhub.dto.requests.CreateReportPostRequest;
import com.sep490.vtuber_fanhub.dto.requests.EditPostCommentRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostCommentResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostWithMediaResponse;
import com.sep490.vtuber_fanhub.dto.responses.SummarizePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.TranslatePostResponse;
import com.sep490.vtuber_fanhub.dto.responses.ReportPostResponse;
import com.sep490.vtuber_fanhub.dto.responses.PostWithReportsResponse;
import com.sep490.vtuber_fanhub.services.PostCommentService;
import com.sep490.vtuber_fanhub.services.PostService;
import com.sep490.vtuber_fanhub.services.ReportPostService;
import com.sep490.vtuber_fanhub.services.UserBookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final UserBookmarkService userBookmarkService;

    private final PostCommentService postCommentService;

    private final ReportPostService reportPostService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> createPost(
            @RequestPart("request") @Valid CreatePostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "video", required = false) MultipartFile video) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Post created successfully")
                .data(postService.createPost(request, images, video))
                .build()
        );
    }

    @PostMapping("/poll")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> createPollPost(@RequestBody @Valid CreatePollPostRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Poll post created successfully")
                .data(postService.createPollPost(request))
                .build()
        );
    }

    @GetMapping("/fan-hub/{fanHubId}/pending")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getPendingPosts(@PathVariable long fanHubId,
                                             @RequestParam(defaultValue = "0") int pageNo,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostWithMediaResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPendingPosts(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> sendAiValidate(@RequestParam Long postId) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.sendAiValidate(postId))
                .build()
        );
    }

    @PostMapping("/ai-validation/approve-all")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> approveAllAiSafePosts(@RequestParam Long fanHubId) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.approveAiSafePosts(fanHubId))
                .build()
        );
    }

    @PostMapping("/ai-validation/reject-all")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> rejectAllAiUnsafePosts(@RequestParam Long fanHubId) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.rejectAiUnsafePosts(fanHubId))
                .build()
        );
    }

    @GetMapping("/translate")
    public ResponseEntity<?> translatePost(@RequestParam Long postId) {

        return ResponseEntity.ok().body(APIResponse.<TranslatePostResponse>builder()
                .success(true)
                .message("Success")
                .data(postService.translatePost(postId))
                .build()
        );
    }

    @GetMapping("/summarize")
    public ResponseEntity<?> summarizePost(@RequestParam Long postId) {

        return ResponseEntity.ok().body(APIResponse.<SummarizePostResponse>builder()
                .success(true)
                .message("Success")
                .data(postService.summarizePost(postId))
                .build()
        );
    }

    @GetMapping("/fan-hub/{fanHubId}")
    public ResponseEntity<?> getPosts(@PathVariable long fanHubId,
                                      @RequestParam(defaultValue = "0") int pageNo,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(defaultValue = "createdAt") String sortBy,
                                      @RequestParam(required = false) String postHashtag,
                                      @RequestParam(required = false) String authorUsername) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPosts(fanHubId, pageNo, pageSize, sortBy, postHashtag, authorUsername))
                .build()
        );
    }

    @GetMapping("/fan-hub/{fanHubId}/announcements-events")
    public ResponseEntity<?> getAnnouncementAndEventPosts(@PathVariable long fanHubId,
                                                          @RequestParam(defaultValue = "0") int pageNo,
                                                          @RequestParam(defaultValue = "10") int pageSize,
                                                          @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getAnnouncementAndEventPosts(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PutMapping("/review")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> reviewPost(
            @RequestParam Long postId,
            @RequestParam String status) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message(postService.reviewPost(postId, status))
                .build()
        );
    }

    @PutMapping("/review/bulk")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> reviewPosts(
            @RequestParam List<Long> postIds,
            @RequestParam String status) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message(postService.reviewPosts(postIds, status))
                .build()
        );
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getPersonalizedFeed(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPersonalizedFeed(pageNo, pageSize, sortBy))
                .build()
        );
    }


    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingPublicPost() {
        return ResponseEntity.ok().body(APIResponse.<PostResponse>builder()
                .success(true)
                .message("Success")
                .data(postService.getTrendingPublicPost())
                .build());
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestPublicApprovedPost() {
        return ResponseEntity.ok().body(APIResponse.<PostResponse>builder()
                .success(true)
                .message("Success")
                .data(postService.getLatestPublicApprovedPost())
                .build());
    }

    @PostMapping("/like")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> likePost(@RequestParam long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.likePost(postId))
                .build()
        );
    }

    @PostMapping("/unlike")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> unlikePost(@RequestParam long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.unlikePost(postId))
                .build()
        );
    }

    @PutMapping("/{postId}/pin")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> pinPost(@PathVariable long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.pinPost(postId))
                .build()
        );
    }

    @PutMapping("/{postId}/unpin")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> unpinPost(@PathVariable long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.unpinPost(postId))
                .build()
        );
    }

    @PutMapping("/{postId}/reject")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> rejectPost(@PathVariable long postId,
                                        @RequestParam(required = false) String reason) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.rejectPost(postId, reason))
                .build()
        );
    }

    @PutMapping("delete/{postId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> deletePost(@PathVariable long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.deletePost(postId))
                .build()
        );
    }

    @PostMapping("/bookmark")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> bookmarkPost(@RequestParam long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(userBookmarkService.createUserBookmark(postId))
                .build()
        );
    }

    @GetMapping("/bookmark")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getBookmarkPosts(@RequestParam(defaultValue = "0") int pageNo,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostWithMediaResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getBookmarkPosts(pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> commentPost(@RequestBody @Valid CreatePostCommentRequest createPostCommentRequest) {
        return ResponseEntity.ok().body(APIResponse.<Boolean>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.createPostComment(createPostCommentRequest))
                .build()
        );
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getPostComments(@PathVariable long postId) {
        return ResponseEntity.ok().body(APIResponse.<List<PostCommentResponse>>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.getPostCommentsByPostId(postId))
                .build()
        );
    }

    @GetMapping("/comments/{parentCommentId}/replies")
    public ResponseEntity<?> getCommentsByParentId(@PathVariable Long parentCommentId) {
        return ResponseEntity.ok().body(APIResponse.<List<PostCommentResponse>>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.getCommentsByParentId(parentCommentId))
                .build()
        );
    }

    @PutMapping("/comment/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> editComment(@PathVariable Long commentId, @RequestBody @Valid EditPostCommentRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.editComment(commentId, request))
                .build()
        );
    }

    @DeleteMapping("/comment/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.deleteComment(commentId))
                .build()
        );
    }

    @PostMapping("/comment/like/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> likeComment(@PathVariable long commentId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.likeComment(commentId))
                .build()
        );
    }

    @PostMapping("/comment/unlike/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> unlikeComment(@PathVariable long commentId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.unlikeComment(commentId))
                .build()
        );
    }

    @PostMapping("/comment/gift/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> sendCommentGift(@PathVariable Long commentId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.sendCommentGift(commentId))
                .build()
        );
    }

    @PutMapping("/comment/{commentId}/hide")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> hideComment(@PathVariable Long commentId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postCommentService.hideComment(commentId))
                .build()
        );
    }

    @PostMapping("/vote")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> votePost(@RequestParam long postId, @RequestParam long optionId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.votePost(postId, optionId))
                .build()
        );
    }

    @PostMapping("/un-vote")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> unVotePost(@RequestParam long postId) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(postService.unVotePost(postId))
                .build()
        );
    }

    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> reportPost(@RequestBody CreateReportPostRequest createReportPostRequest) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.createReportPost(createReportPostRequest))
                .build()
        );
    }

    @GetMapping("/reports/posts/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getReportPostsByFanHubId(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportPostResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.getReportPostsByFanHubId(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PutMapping("/report/resolve")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> resolveReportPost(
            @RequestParam Long reportId,
            @RequestParam(required = false) String resolveMessage) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.resolveReportPost(reportId, resolveMessage))
                .build()
        );
    }

    @GetMapping("/fan-hub/subdomain/{subdomain}")
    public ResponseEntity<?> getPostsBySubDomain(@PathVariable String subdomain,
                                      @RequestParam(defaultValue = "0") int pageNo,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(defaultValue = "createdAt") String sortBy,
                                      @RequestParam(required = false) String postHashtag,
                                      @RequestParam(required = false) String authorUsername) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPostsBySubdomain(subdomain, pageNo, pageSize, sortBy, postHashtag, authorUsername))
                .build()
        );
    }

    @GetMapping("/fan-hub/subdomain/{subdomain}/pending")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getPendingPostsBySubdomain(@PathVariable String subdomain,
                                             @RequestParam(defaultValue = "0") int pageNo,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostWithMediaResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPendingPostsBySubdomain(subdomain, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getPostsByUsername(@RequestParam(defaultValue = "0") int pageNo,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostWithMediaResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getPostsByUsername(pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/fan-hub/{fanHubId}/all")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getAllPostsByFanHubId(@PathVariable long fanHubId,
                                                   @RequestParam(defaultValue = "0") int pageNo,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostWithMediaResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getAllPostsByFanHubId(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PostMapping("/share/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok().body(APIResponse.<PostResponse>builder()
                .success(true)
                .message("Success")
                .data(postService.getPostDetail(postId))
                .build()
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getApprovedPostById(@PathVariable Long postId) {
        return ResponseEntity.ok().body(APIResponse.<PostResponse>builder()
                .success(true)
                .message("Success")
                .data(postService.getApprovedPostById(postId))
                .build()
        );
    }

    @GetMapping("/fan-hub/subdomain/{subdomain}/all")
    @PreAuthorize("hasAnyRole('VTUBER', 'USER')")
    public ResponseEntity<?> getAllPostsBySubdomain(@PathVariable String subdomain,
                                                    @RequestParam(defaultValue = "0") int pageNo,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Success")
                .data(postService.getAllPostsBySubdomain(subdomain, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/reports/my-posts-report")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getMyReportPosts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportPostResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.getReportPostsByCurrentUser(pageNo, pageSize, sortBy))
                .build()
        );
    }

    @GetMapping("/reports/pending-posts/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getPendingReportPosts(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<ReportPostResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.getPendingReportPostsByFanHubId(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }

    @PutMapping("/reports/bulk-resolve")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> bulkResolveReportPosts(
            @RequestParam List<Long> reportIds,
            @RequestParam(required = false) String resolveMessage) {

        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.bulkResolveReportPosts(reportIds, resolveMessage))
                .build()
        );
    }

    @GetMapping("/reports/posts-with-reports/{fanHubId}")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getAllPostsWithReports(
            @PathVariable Long fanHubId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        return ResponseEntity.ok().body(APIResponse.<List<PostWithReportsResponse>>builder()
                .success(true)
                .message("Success")
                .data(reportPostService.getAllPostsWithReports(fanHubId, pageNo, pageSize, sortBy))
                .build()
        );
    }
}
