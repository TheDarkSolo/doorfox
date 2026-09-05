package com.doorfox.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Verifies Meta's X-Hub-Signature-256 header (HMAC-SHA256 over the raw request
 * body, keyed with the app secret) used by both WhatsApp Cloud API and
 * Instagram Messaging API webhooks. Without this, anyone who discovers the
 * webhook URL could POST forged messages straight into the CRM.
 */
@Slf4j
@Component
public class WebhookSignatureVerifier {

    private static final String PREFIX = "sha256=";

    /** Returns true if the app secret isn't configured yet (logged, not enforced) — see channel adapter TODOs. */
    public boolean isValid(String rawBody, String signatureHeader, String appSecret) {
        if (appSecret == null || appSecret.isBlank()) {
            log.warn("App secret not configured — skipping webhook signature verification (do not run like this in production)");
            return true;
        }
        if (signatureHeader == null || !signatureHeader.startsWith(PREFIX)) {
            return false;
        }
        String expectedHex = signatureHeader.substring(PREFIX.length());
        String computedHex = hmacSha256Hex(rawBody, appSecret);
        return MessageDigest.isEqual(
                expectedHex.getBytes(StandardCharsets.US_ASCII),
                computedHex.getBytes(StandardCharsets.US_ASCII));
    }

    private String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new IllegalStateException("HmacSHA256 unavailable", e);
        }
    }
}
