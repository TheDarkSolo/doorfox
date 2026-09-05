package com.doorfox.service;

import com.doorfox.entity.*;
import com.doorfox.repository.ConversationRepository;
import com.doorfox.repository.LeadRepository;
import com.doorfox.repository.MessageRepository;
import com.doorfox.service.channel.ChannelAdapterRegistry;
import com.doorfox.service.llm.ChatMessage;
import com.doorfox.service.llm.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Single entry point for inbound messages from any channel webhook. Creates the
 * Lead/Conversation on first contact, persists the message, and — unless a
 * manager has taken the conversation over — asks the LLM for a reply and sends
 * it back out through the right ChannelAdapter.
 */
@Slf4j
@Service
public class ConversationOrchestrator {

    private final LeadRepository leadRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final LlmClient llmClient;
    private final SystemPromptLoader systemPromptLoader;
    private final ChannelAdapterRegistry channelAdapterRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversationOrchestrator(LeadRepository leadRepository,
                                     ConversationRepository conversationRepository,
                                     MessageRepository messageRepository,
                                     LlmClient llmClient,
                                     SystemPromptLoader systemPromptLoader,
                                     ChannelAdapterRegistry channelAdapterRegistry,
                                     SimpMessagingTemplate messagingTemplate) {
        this.leadRepository = leadRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.llmClient = llmClient;
        this.systemPromptLoader = systemPromptLoader;
        this.channelAdapterRegistry = channelAdapterRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void handleInboundMessage(Channel channel, String externalThreadId, String senderDisplayName,
                                      String text, String externalMessageId) {
        Conversation conversation = conversationRepository.findByChannelAndExternalThreadId(channel, externalThreadId)
                .orElseGet(() -> createConversation(channel, externalThreadId, senderDisplayName));

        Message inbound = new Message();
        inbound.setConversation(conversation);
        inbound.setSender(MessageSender.LEAD);
        inbound.setContent(text);
        inbound.setExternalMessageId(externalMessageId);
        messageRepository.save(inbound);

        broadcastMessage(conversation, inbound);
        broadcastSummary(conversation);

        if (conversation.getOwnerType() != OwnerType.BOT) {
            log.info("Conversation {} is owned by a manager — skipping bot reply", conversation.getId());
            return;
        }

        List<ChatMessage> history = messageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(m -> m.getSender() == MessageSender.LEAD
                        ? ChatMessage.user(m.getContent())
                        : ChatMessage.assistant(m.getContent()))
                .toList();

        String reply;
        try {
            reply = llmClient.complete(systemPromptLoader.load(), history);
        } catch (Exception e) {
            log.error("LLM call failed for conversation {}", conversation.getId(), e);
            return;
        }

        Message botMessage = new Message();
        botMessage.setConversation(conversation);
        botMessage.setSender(MessageSender.BOT);
        botMessage.setContent(reply);
        messageRepository.save(botMessage);

        broadcastMessage(conversation, botMessage);
        broadcastSummary(conversation);

        channelAdapterRegistry.get(channel).sendMessage(externalThreadId, reply);
    }

    private Conversation createConversation(Channel channel, String externalThreadId, String senderDisplayName) {
        Lead lead = new Lead();
        lead.setName(senderDisplayName);
        if (channel == Channel.WHATSAPP) {
            lead.setPhone(externalThreadId);
        } else {
            lead.setInstagramHandle(externalThreadId);
        }
        leadRepository.save(lead);

        Conversation conversation = new Conversation();
        conversation.setLead(lead);
        conversation.setChannel(channel);
        conversation.setExternalThreadId(externalThreadId);
        return conversationRepository.save(conversation);
    }

    private void broadcastMessage(Conversation conversation, Message message) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getId(),
                ConversationMapper.toMessageResponse(message));
    }

    private void broadcastSummary(Conversation conversation) {
        messagingTemplate.convertAndSend("/topic/inbox", Collections.singletonList(ConversationMapper.toSummary(conversation)));
    }
}
