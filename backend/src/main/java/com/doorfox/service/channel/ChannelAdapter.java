package com.doorfox.service.channel;

import com.doorfox.entity.Channel;

public interface ChannelAdapter {

    Channel channel();

    /** Sends an outbound message to the given platform-specific thread id (wa_id or IGSID). */
    void sendMessage(String externalThreadId, String text);
}
