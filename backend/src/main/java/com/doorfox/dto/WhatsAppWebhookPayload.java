package com.doorfox.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Mirrors the WhatsApp Cloud API webhook payload shape (Meta Graph API docs). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WhatsAppWebhookPayload(String object, List<Entry> entry) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(String id, List<Change> changes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Change(Value value, String field) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Value(List<Contact> contacts, List<InboundMessage> messages) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contact(Profile profile, String wa_id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profile(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InboundMessage(String from, String id, String timestamp, String type, Text text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Text(String body) {
    }
}
