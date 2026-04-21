package com.sep490.vtuber_fanhub.exceptions;

import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<APIResponse<String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(APIResponse.<String>builder()
                .success(false)
                .message("Error")
                .data(e.getMessage())
                .error("500")
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(APIResponse.<String>builder()
                .success(false)
                .message("Error")
                .data(e.getFieldError().getDefaultMessage())
                .error("400")
                .build());
    }

    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<APIResponse<String>> handleCustomAuthenticationException(CustomAuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(APIResponse.<String>builder()
                .success(false)
                .message("Unauthorized")
                .data(e.getMessage())
                .error("401")
                .build());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<APIResponse<String>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(APIResponse.<String>builder()
                .success(false)
                .message("Unauthorized")
                .data(e.getMessage())
                .error("401")
                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<String>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(APIResponse.<String>builder()
                .success(false)
                .message("Forbidden")
                .data(e.getMessage())
                .error("403")
                .build());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<APIResponse<String>> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(APIResponse.<String>builder()
                .success(false)
                .message("Not Found")
                .data(e.getMessage())
                .error("404")
                .build());
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<APIResponse<String>> handleRateLimitException(RateLimitException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(APIResponse.<String>builder()
                .success(false)
                .message("Too Many Requests")
                .data(e.getMessage())
                .error("429")
                .build());
    }

    @ExceptionHandler(CooldownException.class)
    public ResponseEntity<APIResponse<String>> handleCooldownException(CooldownException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.<String>builder()
                .success(false)
                .message("Sending AI Validation request for this post is on cooldown.")
                .data(e.getMessage())
                .error("400")
                .build());
    }

}
