package com.microideation.app.dialogue.redis.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.integration.Integration;import com.microideation.app.dialogue.integration.IntegrationUtils;import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.Object;import java.lang.Override;import java.lang.String;import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sandheepgr on 20/6/16.
 */
@Component("dialogueRedisIntegration")
public class RedisIntegration implements Integration {

    @Autowired
    private RedisTemplate<String,DialogueEvent> dialogueRedisTemplate;

    @Autowired
    RedisConnectionFactory connectionFactory;

    @Autowired
    private IntegrationUtils integrationUtils;

    @Resource
    private  ConcurrentHashMap<String,RedisMessageListenerContainer> redisContainers;



    /**
     * Method to publish an item to the queue
     *
     * @param publishEvent : The instance of publishEvent annotation
     * @param dialogueEvent: The object to be sent
     *
     * @return          : Return the object if the publish was successful
     *                    Return null otherwise
     */
    @Override
    public Object publishToChannel(PublishEvent publishEvent, DialogueEvent dialogueEvent) {

        // Get the property value for the channelName
        String channelName = integrationUtils.getEnvironmentProperty(publishEvent.channelName());

        // Send to the channel
        dialogueRedisTemplate.convertAndSend(channelName,dialogueEvent);

        // return the object
        return dialogueEvent;

    }

    /**
     * Overridden method to register the subscriber
     * @param listenerClass : The listener class object
     * @param methodName    : The name of the method for the listener
     * @param subscribeEvent: The subscribeEvent annotation object
     *
     */
    @Override
    public void registerSubscriber(Object listenerClass, String methodName, SubscribeEvent subscribeEvent) {

        // Check if the subscriber has got eventname specified, if yes, we need to
        // show error message as listening to specific key is not supported as of now
        if ( subscribeEvent.eventName() != null && !subscribeEvent.eventName().equals("") ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_EVENT_SPECIFIC_SUBSCRIBER_NOT_SUPPORTED,
                    "Event name specific listening is not supported in Redis integration");

        }

        // Get the channelName
        String channelName = subscribeEvent.channelName();

        // Get the property value for the channelName
        channelName = integrationUtils.getEnvironmentProperty(channelName);

        // Create the key
        String key = channelName+"#"+methodName;

        // If the queue already contains the listener, then return the instance
        if ( redisContainers.containsKey(key) ) {

            // TO-DO : Throw the execption that this container cannot have more than
            // one subscriber
            return;

        }

        // Create the MessageListenerAdapter
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(listenerClass,methodName);
        messageListenerAdapter.setSerializer(getRedisSerializer());
        messageListenerAdapter.afterPropertiesSet();

        // Create the listener container
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListenerAdapter, new ChannelTopic(channelName));
        container.afterPropertiesSet();

        // Start the receiver container
        container.start();

        // Add to the containers list
        redisContainers.put(key,container);


    }

    /**
     * Method to be called when the spring context is finishing
     * This will call the stop on the containers
     */
    @PreDestroy
    @Override
    public void stopListeners() {

        // Iterate the through the containers and stop them
        for ( RedisMessageListenerContainer container : redisContainers.values() ) {

            container.stop();

        }


    }



    /**
     * Method to check if the integration components are available for this
     * integration
     *
     * @return  : return true if the integration components are available
     *          :  return false else and throw the exception
     */
    @Override
    public boolean isIntegrationAvailable() {

        // check if any of the beans are null
        if ( dialogueRedisTemplate == null ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,"dialogueRedisTemplate bean is not available");

        } else if ( connectionFactory == null ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,"connectionFactory bean is not available");

        }


        // Return true finally
        return true;

    }
    
    
    /**
     * Method to build and return the Jackson2JsonRedisSerializer
     * This will build a object mapper with properties required
     * for the redis integration
     *
     * @return : Return the serializer with the required properties
     */
    private Jackson2JsonRedisSerializer<DialogueEvent> getRedisSerializer() {
    
        ObjectMapper mapper= new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Create the serialized
        Jackson2JsonRedisSerializer<DialogueEvent> serializer = new Jackson2JsonRedisSerializer<>(DialogueEvent.class);
        
        // Set the properties
        serializer.setObjectMapper(mapper);
        
        // return the serializer
        return serializer;
    }


}
