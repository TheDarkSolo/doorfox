package com.doorfox.service.channel;

import com.doorfox.config.InstagramProperties;
import com.doorfox.entity.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * TODO: requires Instagram Messaging API access (Meta App Review for
 * instagram_manage_messages) — INSTAGRAM_ACCESS_TOKEN, INSTAGRAM_IG_USER_ID.
 * Logs a no-op until those are configured.
 */
@Slf4j
@Component
public class InstagramChannelAdapter implements ChannelAdapter {

    private final InstagramProperties properties;
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://graph.facebook.com/v20.0")
            .build();

    public InstagramChannelAdapter(InstagramProperties properties) {
        this.properties = properties;
    }

    @Override
    public Channel channel() {
        return Channel.INSTAGRAM;
    }

    @Override
    public void sendMessage(String externalThreadId, String text) {
        if (!properties.isConfigured()) {
            log.info("[no-op] INSTAGRAM_ACCESS_TOKEN not set — would send to {}: {}", externalThreadId, text);
            return;
        }

        Map<String, Object> body = Map.of(
                "recipient", Map.of("id", externalThreadId),
                "message", Map.of("text", text)
        );

        restClient.post()
                .uri("/{igUserId}/messages", properties.igUserId())
                .header("Authorization", "Bearer " + properties.accessToken())
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
