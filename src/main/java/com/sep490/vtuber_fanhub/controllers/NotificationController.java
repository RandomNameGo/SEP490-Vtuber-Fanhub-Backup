package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.NotificationEventResponse;
import com.sep490.vtuber_fanhub.services.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        List<NotificationEventResponse> notifications = notificationService.getUserNotifications(
                request, pageNo, pageSize, sortBy);

        return ResponseEntity.ok()
                .body(APIResponse.<List<NotificationEventResponse>>builder()
                        .success(true)
                        .message("Success")
                        .data(notifications)
                        .build());
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        List<NotificationEventResponse> notifications = notificationService.getUnreadNotifications(
                request, pageNo, pageSize, sortBy);

        return ResponseEntity.ok()
                .body(APIResponse.<List<NotificationEventResponse>>builder()
                        .success(true)
                        .message("Success")
                        .data(notifications)
                        .build());
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadNotificationCount(HttpServletRequest request) {

        Long count = notificationService.getUnreadNotificationCount(request);

        return ResponseEntity.ok()
                .body(APIResponse.<Long>builder()
                        .success(true)
                        .message("Success")
                        .data(count)
                        .build());
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            HttpServletRequest request,
            @PathVariable Long notificationId) {

        String result = notificationService.markAsRead(notificationId, request);

        return ResponseEntity.ok()
                .body(APIResponse.<String>builder()
                        .success(true)
                        .message("Success")
                        .data(result)
                        .build());
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {

        int count = notificationService.markAllAsRead(request);

        return ResponseEntity.ok()
                .body(APIResponse.<Integer>builder()
                        .success(true)
                        .message("Success")
                        .data(count)
                        .build());
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            HttpServletRequest request,
            @PathVariable Long notificationId) {

        String result = notificationService.deleteNotification(notificationId, request);

        return ResponseEntity.ok()
                .body(APIResponse.<String>builder()
                        .success(true)
                        .message("Success")
                        .data(result)
                        .build());
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(HttpServletRequest request) {

        String result = notificationService.deleteAllNotifications(request);

        return ResponseEntity.ok()
                .body(APIResponse.<String>builder()
                        .success(true)
                        .message("Success")
                        .data(result)
                        .build());
    }
}
