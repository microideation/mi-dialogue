package com.microideation.app.dialogue.kafka.integration;

import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.integration.Integration;
import com.microideation.app.dialogue.integration.IntegrationUtils;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sandheepgr on 28/6/16.
 */
@Component("dialogueKafkaIntegration")
public class DialogueKafkaIntegration implements Integration {

    @Autowired
    private ConsumerFactory<String, DialogueEvent> kafkaConsumerFactory;

    @Autowired
    private IntegrationUtils integrationUtils;

    @Autowired
    private KafkaTemplate<String, DialogueEvent> kafkaTemplate;

    @Resource
    private ConcurrentHashMap<String, KafkaMessageListenerContainer<String, DialogueEvent>> kafkaContainers;

    @Override
    public Object publishToChannel(PublishEvent publishEvent, DialogueEvent dialogueEvent) {
        // Get the property value for the channelName
        String channelName = integrationUtils.getEnvironmentProperty(publishEvent.channelName());

        // Send to the channel
        kafkaTemplate.send(channelName, dialogueEvent);

        //  return dialogueEvent
        return dialogueEvent;
    }

    @Override
    public void registerSubscriber(Object listenerClass, String methodName, SubscribeEvent subscribeEvent) {
        // Check if the subscriber has got eventname specified, if yes, we need to
        // show error message as listening to specific key is not supported as of now
        if (subscribeEvent.eventName() != null && !subscribeEvent.eventName().equals("")) {
            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_EVENT_SPECIFIC_SUBSCRIBER_NOT_SUPPORTED,
                    "Event name specific listening is not supported in Kafka integration");
        }

        // Get the channelName
        String channelName = subscribeEvent.channelName();

        // Get the finalClass for the listenerClass
        Class<?> finalClass = AopProxyUtils.ultimateTargetClass(listenerClass);

        // Get the method by name
        Method method = ReflectionUtils.findMethod(finalClass, methodName, null);

        // Create container properties
        ContainerProperties containerProperties = new ContainerProperties(new TopicPartitionOffset(channelName, 0));
        
        // Create a listener
        KafkaMessageListenerContainer<String, DialogueEvent> kafkaMessageListenerContainer =
                new KafkaMessageListenerContainer<>(kafkaConsumerFactory, containerProperties);

        // Set the message listener
        kafkaMessageListenerContainer.setupMessageListener((MessageListener<String, DialogueEvent>) record -> {
            // IMPORTANT: This will throw error if the method is inside an inner class
            ReflectionUtils.invokeMethod(method, listenerClass, record.value());
        });

        // Start the container
        kafkaMessageListenerContainer.start();

        // Store the container for later cleanup
        kafkaContainers.put(channelName + "_" + methodName, kafkaMessageListenerContainer);
    }

    @Override
    public void stopListeners() {
        // Iterate through the containers and stop them
        for (KafkaMessageListenerContainer<String, DialogueEvent> container : kafkaContainers.values()) {
            container.stop();
        }
    }

    @Override
    public boolean isIntegrationAvailable() {
        // check if the beans are available
        if (kafkaConsumerFactory == null) {
            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE, "kafkaConsumerFactory bean is not available");
        } else if (kafkaTemplate == null) {
            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE, "kafkaTemplate bean is not available");
        }

        // Return true finally
        return true;
    }
}
