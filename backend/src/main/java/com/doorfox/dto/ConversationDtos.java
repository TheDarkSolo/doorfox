package com.doorfox.dto;

import com.doorfox.entity.Channel;
import com.doorfox.entity.ConversationStatus;
import com.doorfox.entity.MessageSender;
import com.doorfox.entity.OwnerType;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public class ConversationDtos {

    public record ConversationSummary(
            UUID id,
            UUID leadId,
            String leadName,
            Channel channel,
            OwnerType ownerType,
            UUID ownerManagerId,
            ConversationStatus status,
            Instant updatedAt
    ) {
    }

    public record MessageResponse(
            UUID id,
            MessageSender sender,
            String content,
            Instant createdAt
    ) {
    }

    public record SendMessageRequest(@NotBlank String text) {
    }
}
