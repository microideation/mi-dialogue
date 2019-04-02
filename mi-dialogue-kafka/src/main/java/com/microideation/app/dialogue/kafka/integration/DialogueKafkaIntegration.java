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
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.TopicPartitionInitialOffset;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sandheepgr on 28/6/16.
 */
@Component("dialogueKafkaIntegration")
public class DialogueKafkaIntegration implements Integration {


    @Autowired
    private ConsumerFactory<String,DialogueEvent> consumerFactory;

    @Autowired
    private IntegrationUtils integrationUtils;

    @Autowired
    private KafkaTemplate<String,DialogueEvent> kafkaTemplate;


    @Resource
    private ConcurrentHashMap<String,KafkaMessageListenerContainer> kafkaContainers;


    @Override
    public Object publishToChannel(PublishEvent publishEvent, DialogueEvent dialogueEvent) {

        // Get the property value for the channelName
        String channelName = integrationUtils.getEnvironmentProperty(publishEvent.channelName());

        // Send to the channel
        kafkaTemplate.send(channelName,dialogueEvent);

        //  return dialogueEvent
        return dialogueEvent;

    }

    @Override
    public void registerSubscriber(Object listenerClass, String methodName, SubscribeEvent subscribeEvent) {

        // Check if the subscriber has got eventname specified, if yes, we need to
        // show error message as listening to specific key is not supported as of now
        if ( subscribeEvent.eventName() != null || !subscribeEvent.eventName().equals("") ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_EVENT_SPECIFIC_SUBSCRIBER_NOT_SUPPORTED,
                    "Event name specific listening is not supported in Kafka integration");

        }

        // Get the channelName
        String channelName = subscribeEvent.channelName();

        // Get the finalClass for the listenerClass
        Class finalClass = AopProxyUtils.ultimateTargetClass(listenerClass);

        // Get the method by name
        Method method = ReflectionUtils.findMethod(finalClass,methodName,null);
        
        // Create a listener
        KafkaMessageListenerContainer<String,DialogueEvent> kafkaMessageListenerContainer =
                new KafkaMessageListenerContainer<>(consumerFactory,
                        new ContainerProperties(new TopicPartitionInitialOffset(channelName,0)));
        
        // Create the adapter
        KafkaMessageDrivenChannelAdapter<String,DialogueEvent> kafkaMessageDrivenChannelAdapter =
                new KafkaMessageDrivenChannelAdapter<>(kafkaMessageListenerContainer);

        // Create the channel
        SubscribableChannel receiverChannel = getReceiverChannel();
        
        // Set the handler
        receiverChannel.subscribe(new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {

                ReflectionUtils.invokeMethod(method,listenerClass,message.getPayload());

            }
        });

        // Set the output channel
        kafkaMessageDrivenChannelAdapter.setOutputChannel(receiverChannel);

        // Call the after properties set
        kafkaMessageDrivenChannelAdapter.afterPropertiesSet();

        // Start the container
        kafkaMessageListenerContainer.start();


    }

    @Override
    public void stopListeners() {

        // Iterate the through the containers and stop them
        for ( KafkaMessageListenerContainer container : kafkaContainers.values() ) {

            container.stop();

        }

    }

    @Override
    public boolean isIntegrationAvailable() {

        // check if the beans are available
        if ( consumerFactory == null ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,"consumerFactory bean is not available");

        } else if ( kafkaTemplate == null ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,"consumerFactory bean is not available");

        }

        // Return true finally
        return true;

    }
   
    
    protected SubscribableChannel getReceiverChannel() {
        
        return new DirectChannel();
        
    }

}
