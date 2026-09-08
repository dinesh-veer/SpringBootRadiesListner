package com.dinesh.springbootradieslistner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * This is old way to consume message in Topic.
 */

@Configuration
public class RedisAlertConfig {

    // 1. Create the adapter and hardcode the target method name ("processAlert")
    @Bean
    public MessageListenerAdapter alertListenerAdapter(AlertSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "processAlert");
    }

    // 2. Create the container, inject the connection, and bind the topic
    @Bean
    public RedisMessageListenerContainer alertContainer(RedisConnectionFactory connectionFactory,
                                                        MessageListenerAdapter alertListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(alertListenerAdapter, new ChannelTopic("system-alerts"));
        return container;
    }
}