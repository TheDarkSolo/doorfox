package com.doorfox.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.doorfox.config.WhatsAppProperties;
import com.doorfox.dto.WhatsAppWebhookPayload;
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
@RequestMapping("/webhooks/whatsapp")
public class WhatsAppWebhookController {

    private final WhatsAppProperties properties;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ConversationOrchestrator orchestrator;
    private final ObjectMapper objectMapper;

    public WhatsAppWebhookController(WhatsAppProperties properties,
                                      WebhookSignatureVerifier signatureVerifier,
                                      ConversationOrchestrator orchestrator,
                                      ObjectMapper objectMapper) {
        this.properties = properties;
        this.signatureVerifier = signatureVerifier;
        this.orchestrator = orchestrator;
        this.objectMapper = objectMapper;
    }

    /** Meta's one-time webhook subscription verification (App Dashboard "Verify and save"). */
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
            log.warn("Rejected WhatsApp webhook call with invalid signature");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WhatsAppWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawBody, WhatsAppWebhookPayload.class);
        } catch (Exception e) {
            log.error("Failed to parse WhatsApp webhook payload", e);
            return ResponseEntity.badRequest().build();
        }

        for (WhatsAppWebhookPayload.Entry entry : orEmpty(payload.entry())) {
            for (WhatsAppWebhookPayload.Change change : orEmpty(entry.changes())) {
                WhatsAppWebhookPayload.Value value = change.value();
                if (value == null || value.messages() == null) {
                    continue;
                }
                String contactName = value.contacts() != null && !value.contacts().isEmpty()
                        ? value.contacts().get(0).profile().name()
                        : null;
                for (WhatsAppWebhookPayload.InboundMessage message : value.messages()) {
                    if (message.text() == null) {
                        continue; // media/interactive messages not handled yet
                    }
                    orchestrator.handleInboundMessage(Channel.WHATSAPP, message.from(), contactName,
                            message.text().body(), message.id());
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list != null ? list : List.of();
    }
}
