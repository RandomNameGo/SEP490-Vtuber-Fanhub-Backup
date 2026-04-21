package com.sep490.vtuber_fanhub.controllers;

import com.sep490.vtuber_fanhub.dto.requests.SendMessageRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.MessageResponse;
import com.sep490.vtuber_fanhub.dto.responses.PaginatedResponse;
import com.sep490.vtuber_fanhub.services.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("vhub/api/v1/message")
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> sendMessage(@RequestBody @Valid SendMessageRequest sendMessageRequest,
                                         @AuthenticationPrincipal Jwt jwt){
        String username = jwt.getSubject();

        return ResponseEntity.ok().body(APIResponse.<MessageResponse>builder()
                .success(true)
                .message("Message sent successfully")
                .data(chatMessageService.sendMessage(sendMessageRequest, username))
                .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'VTUBER')")
    public ResponseEntity<?> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        String username = jwt.getSubject();

        return ResponseEntity.ok().body(APIResponse.<PaginatedResponse<MessageResponse>>builder()
                .success(true)
                .message("Messages fetched successfully")
                .data(chatMessageService.getMessagesPaginated(username, page, size))
                .build()
        );
    }
}
