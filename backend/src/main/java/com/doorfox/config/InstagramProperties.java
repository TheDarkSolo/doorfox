package com.doorfox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "instagram")
public record InstagramProperties(String verifyToken, String appSecret, String accessToken, String igUserId) {

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank();
    }
}
