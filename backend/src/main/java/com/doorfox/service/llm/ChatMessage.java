package com.doorfox.service.llm;

/** role is one of "system" | "user" | "assistant", matching the OpenAI-compatible wire format. */
public record ChatMessage(String role, String content) {

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }
}
