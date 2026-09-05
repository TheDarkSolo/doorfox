package com.doorfox.service.channel;

import com.doorfox.config.WhatsAppProperties;
import com.doorfox.entity.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * TODO: requires a real Meta Business app + WhatsApp Cloud API phone number
 * (WHATSAPP_ACCESS_TOKEN, WHATSAPP_PHONE_NUMBER_ID). Until those are set this
 * logs a no-op instead of throwing, so the rest of the system stays testable.
 */
@Slf4j
@Component
public class WhatsAppChannelAdapter implements ChannelAdapter {

    private final WhatsAppProperties properties;
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://graph.facebook.com/v20.0")
            .build();

    public WhatsAppChannelAdapter(WhatsAppProperties properties) {
        this.properties = properties;
    }

    @Override
    public Channel channel() {
        return Channel.WHATSAPP;
    }

    @Override
    public void sendMessage(String externalThreadId, String text) {
        if (!properties.isConfigured()) {
            log.info("[no-op] WHATSAPP_ACCESS_TOKEN not set — would send to {}: {}", externalThreadId, text);
            return;
        }

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", externalThreadId,
                "type", "text",
                "text", Map.of("body", text)
        );

        restClient.post()
                .uri("/{phoneNumberId}/messages", properties.phoneNumberId())
                .header("Authorization", "Bearer " + properties.accessToken())
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
