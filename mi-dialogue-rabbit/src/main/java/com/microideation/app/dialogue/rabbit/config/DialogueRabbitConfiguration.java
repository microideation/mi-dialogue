package com.microideation.app.dialogue.rabbit.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sandheepgr on 23/6/16.
 */
@Configuration
@ComponentScan(basePackages = {"com.microideation.app.dialogue.rabbit.integration"})
public class DialogueRabbitConfiguration {

    @Value("${spring.amqp.deserialization.allowed-list-patterns:com.microideation.app.dialogue.event.*,com.microideation.app.dialogue.authority.*,com.microideation.app.dialogue.dictionary.*,java.util.*,java.lang.*}")
    private String allowedListPatterns;

    /** Rabbit configuration **/
    @Bean
    public ConcurrentHashMap<String,Queue> rabbitChannels() {

        return new ConcurrentHashMap<>(0);

    }

    @Bean
    public ConcurrentHashMap<String,SimpleMessageListenerContainer> rabbitContainers() {

        return new ConcurrentHashMap<>(0);

    }

    /**
     * Configure a custom message converter that allows DialogueEvent and related classes
     * for deserialization to fix the SecurityException
     */
    @Bean
    public MessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        
        // Set allowed list patterns for deserialization from properties
        String[] patterns = allowedListPatterns.split(",");
        converter.setAllowedListPatterns(Arrays.asList(patterns));
        
        return converter;
    }

}
