package com.doorfox.repository;

import com.doorfox.entity.Conversation;
import com.doorfox.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);

    List<Message> findTop20ByConversationOrderByCreatedAtDesc(Conversation conversation);
}
