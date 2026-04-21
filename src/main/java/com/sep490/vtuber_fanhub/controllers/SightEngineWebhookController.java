package com.sep490.vtuber_fanhub.controllers;

import com.fasterxml.jackson.databind.JsonNode;

import com.sep490.vtuber_fanhub.services.PostValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/vhub/api/v1/webhooks/sightengine")
@RequiredArgsConstructor
public class SightEngineWebhookController {
    private final PostValidationService postValidationServiceImplAsync;

    @PostMapping("/video-result")
    public ResponseEntity<?> handleVideoResult(@RequestBody JsonNode payload) {
        postValidationServiceImplAsync.handleVideoCallback(payload);
        System.out.println("Callback received.");
        return ResponseEntity.ok("Received");
    }

    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestBody JsonNode payload) {
        System.out.println("test received.");
        return ResponseEntity.ok("Received");
    }

}
