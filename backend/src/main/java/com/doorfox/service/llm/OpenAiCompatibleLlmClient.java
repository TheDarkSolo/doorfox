package com.doorfox.service.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.doorfox.config.LlmProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Talks to any OpenAI-compatible Chat Completions endpoint. Swapping providers
 * (DeepSeek, Qwen/DashScope, OpenRouter, ...) is a config change (llm.base-url /
 * llm.api-key / llm.model), never a code change.
 */
@Slf4j
@Component
public class OpenAiCompatibleLlmClient implements LlmClient {

    private final LlmProperties properties;
    private final RestClient restClient;

    public OpenAiCompatibleLlmClient(LlmProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public String complete(String systemPrompt, List<ChatMessage> history) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            log.warn("LLM_API_KEY is not set — returning a placeholder reply instead of calling {}", properties.baseUrl());
            return "[LLM не настроен: задайте LLM_API_KEY, чтобы бот отвечал реальными сообщениями]";
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPrompt));
        messages.addAll(history);

        ChatCompletionRequest request = new ChatCompletionRequest(properties.model(), messages);

        ChatCompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + properties.apiKey())
                .body(request)
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("LLM provider returned no choices");
        }
        return response.choices().get(0).message().content();
    }

    private record ChatCompletionRequest(String model, List<ChatMessage> messages) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(ChatMessage message) {
    }
}
