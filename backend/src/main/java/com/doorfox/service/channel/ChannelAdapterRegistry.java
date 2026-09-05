package com.doorfox.service.channel;

import com.doorfox.entity.Channel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ChannelAdapterRegistry {

    private final Map<Channel, ChannelAdapter> adapters;

    public ChannelAdapterRegistry(List<ChannelAdapter> adapters) {
        this.adapters = adapters.stream()
                .collect(Collectors.toMap(ChannelAdapter::channel, Function.identity()));
    }

    public ChannelAdapter get(Channel channel) {
        ChannelAdapter adapter = adapters.get(channel);
        if (adapter == null) {
            throw new IllegalStateException("No ChannelAdapter registered for " + channel);
        }
        return adapter;
    }
}
