package com.doorfox.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Component
public class SystemPromptLoader {

    private static final String PROMPT_PATH = "prompts/sales-agent-system-prompt.txt";

    private final String cachedPrompt;

    public SystemPromptLoader() {
        try {
            this.cachedPrompt = new ClassPathResource(PROMPT_PATH)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + PROMPT_PATH, e);
        }
    }

    public String load() {
        return cachedPrompt;
    }
}
