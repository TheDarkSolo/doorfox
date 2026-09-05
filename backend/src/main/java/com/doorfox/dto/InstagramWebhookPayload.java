package com.doorfox.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Mirrors the Instagram Messaging API webhook payload shape (Meta Graph API docs). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InstagramWebhookPayload(String object, List<Entry> entry) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(String id, List<Messaging> messaging) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Messaging(Participant sender, Participant recipient, InboundMessage message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Participant(String id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InboundMessage(String mid, String text) {
    }
}
