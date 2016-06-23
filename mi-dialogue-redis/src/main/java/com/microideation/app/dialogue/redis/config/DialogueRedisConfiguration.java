package com.microideation.app.dialogue.redis.config;

import com.microideation.app.dialogue.event.DialogueEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sandheepgr on 23/6/16.
 */
@Configuration
@ComponentScan(basePackages = {"com.microideation.app.dialogue.redis.integration"})
public class DialogueRedisConfiguration {

    @Bean
    public ConcurrentHashMap<String,RedisMessageListenerContainer> redisContainers() {

        return new ConcurrentHashMap<>(0);

    }


    /** Redis configuration **/
    @Bean
    public RedisTemplate<String,DialogueEvent> dialogueRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String,DialogueEvent> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(DialogueEvent.class));
        redisTemplate.afterPropertiesSet();
        return redisTemplate ;

    }

}
