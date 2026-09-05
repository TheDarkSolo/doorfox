package com.doorfox.repository;

import com.doorfox.entity.Channel;
import com.doorfox.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByChannelAndExternalThreadId(Channel channel, String externalThreadId);

    // JOIN FETCH avoids a LazyInitializationException on conversation.getLead() when mapping to
    // ConversationSummary — open-in-view is disabled, so there's no session left by the time the
    // controller maps the result unless the lead is loaded in this same query.
    @Query("SELECT c FROM Conversation c JOIN FETCH c.lead ORDER BY c.updatedAt DESC")
    List<Conversation> findAllByOrderByUpdatedAtDesc();
}
