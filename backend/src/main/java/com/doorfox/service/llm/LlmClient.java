package com.doorfox.service.llm;

import java.util.List;

/**
 * Deliberately provider-agnostic: any backend that speaks the OpenAI-compatible
 * Chat Completions wire format (DeepSeek, Qwen/DashScope, OpenRouter, ...) plugs in
 * via config alone (llm.base-url / llm.api-key / llm.model) — no code change needed
 * to switch providers.
 */
public interface LlmClient {

    String complete(String systemPrompt, List<ChatMessage> history);
}
