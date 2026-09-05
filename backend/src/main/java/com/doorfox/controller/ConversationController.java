package com.doorfox.controller;

import com.doorfox.dto.ConversationDtos.ConversationSummary;
import com.doorfox.dto.ConversationDtos.MessageResponse;
import com.doorfox.dto.ConversationDtos.SendMessageRequest;
import com.doorfox.entity.Conversation;
import com.doorfox.entity.Message;
import com.doorfox.entity.MessageSender;
import com.doorfox.entity.OwnerType;
import com.doorfox.repository.ConversationRepository;
import com.doorfox.repository.ManagerRepository;
import com.doorfox.repository.MessageRepository;
import com.doorfox.service.ConversationMapper;
import com.doorfox.service.channel.ChannelAdapterRegistry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ManagerRepository managerRepository;
    private final ChannelAdapterRegistry channelAdapterRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public ConversationController(ConversationRepository conversationRepository,
                                   MessageRepository messageRepository,
                                   ManagerRepository managerRepository,
                                   ChannelAdapterRegistry channelAdapterRegistry,
                                   SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.managerRepository = managerRepository;
        this.channelAdapterRegistry = channelAdapterRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public List<ConversationSummary> list() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(ConversationMapper::toSummary)
                .toList();
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(@PathVariable UUID id) {
        Conversation conversation = getOrThrow(id);
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(ConversationMapper::toMessageResponse)
                .toList();
    }

    @PostMapping("/{id}/takeover")
    @Transactional
    public ConversationSummary takeover(@PathVariable UUID id, @AuthenticationPrincipal UUID managerId) {
        Conversation conversation = getOrThrow(id);
        conversation.setOwnerType(OwnerType.MANAGER);
        conversation.setOwnerManager(managerRepository.getReferenceById(managerId));
        conversationRepository.save(conversation);
        ConversationSummary summary = ConversationMapper.toSummary(conversation);
        messagingTemplate.convertAndSend("/topic/inbox", List.of(summary));
        return summary;
    }

    @PostMapping("/{id}/release")
    @Transactional
    public ConversationSummary release(@PathVariable UUID id) {
        Conversation conversation = getOrThrow(id);
        conversation.setOwnerType(OwnerType.BOT);
        conversation.setOwnerManager(null);
        conversationRepository.save(conversation);
        ConversationSummary summary = ConversationMapper.toSummary(conversation);
        messagingTemplate.convertAndSend("/topic/inbox", List.of(summary));
        return summary;
    }

    @PostMapping("/{id}/messages")
    @Transactional
    public MessageResponse sendMessage(@PathVariable UUID id, @Valid @RequestBody SendMessageRequest request) {
        Conversation conversation = getOrThrow(id);
        if (conversation.getOwnerType() != OwnerType.MANAGER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conversation is still owned by the bot — call /takeover first");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(MessageSender.MANAGER);
        message.setContent(request.text());
        messageRepository.save(message);

        MessageResponse response = ConversationMapper.toMessageResponse(message);
        messagingTemplate.convertAndSend("/topic/conversations/" + conversation.getId(), response);

        channelAdapterRegistry.get(conversation.getChannel())
                .sendMessage(conversation.getExternalThreadId(), request.text());

        return response;
    }

    private Conversation getOrThrow(UUID id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }
}
