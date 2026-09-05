package com.doorfox.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.doorfox.config.InstagramProperties;
import com.doorfox.dto.InstagramWebhookPayload;
import com.doorfox.entity.Channel;
import com.doorfox.service.ConversationOrchestrator;
import com.doorfox.util.WebhookSignatureVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/webhooks/instagram")
public class InstagramWebhookController {

    private final InstagramProperties properties;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ConversationOrchestrator orchestrator;
    private final ObjectMapper objectMapper;

    public InstagramWebhookController(InstagramProperties properties,
                                       WebhookSignatureVerifier signatureVerifier,
                                       ConversationOrchestrator orchestrator,
                                       ObjectMapper objectMapper) {
        this.properties = properties;
        this.signatureVerifier = signatureVerifier;
        this.orchestrator = orchestrator;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
                                          @RequestParam("hub.verify_token") String verifyToken,
                                          @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && properties.verifyToken() != null && properties.verifyToken().equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> receive(@RequestBody String rawBody,
                                         @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
        if (!signatureVerifier.isValid(rawBody, signature, properties.appSecret())) {
            log.warn("Rejected Instagram webhook call with invalid signature");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        InstagramWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, InstagramWebhookPayload.class);
        } catch (Exception e) {
            log.error("Failed to parse Instagram webhook payload", e);
            return ResponseEntity.badRequest().build();
        }

        for (InstagramWebhookPayload.Entry entry : orEmpty(payload.entry())) {
            for (InstagramWebhookPayload.Messaging messaging : orEmpty(entry.messaging())) {
                if (messaging.message() == null || messaging.message().text() == null || messaging.sender() == null) {
                    continue; // media/echo events not handled yet
                }
                orchestrator.handleInboundMessage(Channel.INSTAGRAM, messaging.sender().id(), null,
                        messaging.message().text(), messaging.message().mid());
            }
        }
        return ResponseEntity.ok().build();
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list != null ? list : List.of();
    }
}
