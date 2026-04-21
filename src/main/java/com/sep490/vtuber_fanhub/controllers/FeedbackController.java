package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.CreateFeedbackRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.FeedbackResponse;
import com.sep490.vtuber_fanhub.models.FeedbackCategory;
import com.sep490.vtuber_fanhub.services.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> submitFeedback(@RequestBody @Valid CreateFeedbackRequest request) {
        return ResponseEntity.ok().body(APIResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(feedbackService.submitFeedback(request))
                .build()
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<?> getAllFeedback(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        List<FeedbackResponse> feedbackList = feedbackService.getAllFeedback(pageNo, pageSize, sortBy);
        return ResponseEntity.ok().body(APIResponse.<List<FeedbackResponse>>builder()
                .success(true)
                .message("Success")
                .data(feedbackList)
                .build()
        );
    }

    @GetMapping("/my-feedback")
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getMyFeedback(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        List<FeedbackResponse> feedbackList = feedbackService.getCurrentUserFeedback(pageNo, pageSize, sortBy);
        return ResponseEntity.ok().body(APIResponse.<List<FeedbackResponse>>builder()
                .success(true)
                .message("Success")
                .data(feedbackList)
                .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMyFeedbackDetail(@PathVariable Long id) {
        return ResponseEntity.ok().body(APIResponse.<FeedbackResponse>builder()
                .success(true)
                .message("Success")
                .data(feedbackService.getFeedbackDetail(id))
                .build()
        );
    }


    @GetMapping("/categories")
    public ResponseEntity<?> getAllFeedbackCategories() {
        List<FeedbackCategory> categories = feedbackService.getAllFeedbackCategories();
        return ResponseEntity.ok().body(APIResponse.<List<FeedbackCategory>>builder()
                .success(true)
                .message("Feedback categories retrieved successfully")
                .data(categories)
                .build()
        );
    }
}
