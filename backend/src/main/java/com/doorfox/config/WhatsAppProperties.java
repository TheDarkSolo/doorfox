package com.doorfox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "whatsapp")
public record WhatsAppProperties(String verifyToken, String appSecret, String accessToken, String phoneNumberId) {

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank();
    }
}
