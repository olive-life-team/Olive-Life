package com.ecommerce.chatdemo.global.config;

import com.ecommerce.chatdemo.global.listener.CacheSyncMessageListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class CacheSyncSubscriberConfig {
    public static final String CHANNEL_NAME = "cache:sync:searchCache";
    @Bean
    public RedisMessageListenerContainer container(
            RedisConnectionFactory connectionFactory,
            CacheSyncMessageListener listener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new ChannelTopic(CHANNEL_NAME));
        return container;
    }
}
