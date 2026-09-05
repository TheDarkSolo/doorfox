package com.doorfox.service;

import com.doorfox.dto.ConversationDtos.ConversationSummary;
import com.doorfox.dto.ConversationDtos.MessageResponse;
import com.doorfox.entity.Conversation;
import com.doorfox.entity.Message;

public final class ConversationMapper {

    private ConversationMapper() {
    }

    public static ConversationSummary toSummary(Conversation conversation) {
        return new ConversationSummary(
                conversation.getId(),
                conversation.getLead().getId(),
                conversation.getLead().getName(),
                conversation.getChannel(),
                conversation.getOwnerType(),
                conversation.getOwnerManager() != null ? conversation.getOwnerManager().getId() : null,
                conversation.getStatus(),
                conversation.getUpdatedAt()
        );
    }

    public static MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
