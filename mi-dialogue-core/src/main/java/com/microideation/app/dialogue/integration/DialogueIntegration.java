package com.microideation.app.dialogue.integration;

import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.event.EventStore;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by sandheepgr on 23/6/16.
 */
@Component
public class DialogueIntegration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier(value = "dialogueRabbitIntegration")
    private Integration rabbitIntegration;

    @Autowired(required = false)
    @Qualifier(value = "dialogueRedisIntegration")
    private Integration redisIntegration;

    @Autowired(required = false)
    @Qualifier(value = "dialogueKafkaIntegration")
    private Integration kafkaIntegration;




    /**
     * Method to get the integration for the eventStore and call the publishToChannel method
     *
     * @param publishEvent      : The publishEvent annotation object
     * @param dialogueEvent     : The DialogueEvent object to be published
     */
    public void processPublishEvent(PublishEvent publishEvent,DialogueEvent dialogueEvent) {

        // Get the type
        EventStore eventStore = publishEvent.eventStore();

        // Get the integration
        Integration integration = getIntegration(eventStore);

        // Check if the integration is available
        integration.isIntegrationAvailable();

        // Call the publishToQueue method of the rabbitIntegration
        integration.publishToChannel(publishEvent, dialogueEvent);

    }


    /**
     * Method to add the listener for the subscriber method
     *
     * @param method        : The method object on which the subscribeEvent is annotated
     * @param subscribeEvent: The SubscribeEvent annotation object
     * @param listenerClass : The ListenerClass in which the method resides
     *
     */
    public void addListenerToSubscriberMethod(Method method,SubscribeEvent subscribeEvent, Object listenerClass) {

        // Get the type
        EventStore eventStore = subscribeEvent.eventStore();

        // Get the integration
        Integration integration = getIntegration(eventStore);

        // Check if the integration is available
        integration.isIntegrationAvailable();

        // Call the registerSubscriber method of the integraton
        integration.registerSubscriber(listenerClass, method.getName(), subscribeEvent.channelName());

    }


    /**
     * Method  to get the integration based on the event store
     *
     * @param eventStore : Eventstore parameter
     *
     * @return           : Return the integration based on the event store
     *                     throw exception if no integration is available for
     *                     the given event store.
     */
    private Integration getIntegration(EventStore eventStore) {

        // Switch the event type
        switch (eventStore) {

            // Processing for the rabbitmq
            case RABBITMQ:

                // Check if the integration is available
                if ( rabbitIntegration == null ) {

                    throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,
                                                "RabbitMQ integration is not available. Please make sure that the mi-dialogue-rabbit is added as dependency");

                }

                // Return the rabbit integration
                return rabbitIntegration;

            // Processing for the redis type
            case REDIS:

                // Check if the integration is available
                if ( redisIntegration == null ) {

                    throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,
                            "Redis integration is not available. Please make sure that the mi-dialogue-redis is added as dependency");

                }

                // Return the redis integration
                return redisIntegration;


            // Processing for the kafka type
            case KAFKA:

                // Check if the integration is available
                if ( kafkaIntegration == null ) {

                    throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,
                            "Kafka integration is not available. Please make sure that the mi-dialogue-kafka is added as dependency");

                }

                // Return the kafka integration
                return kafkaIntegration;


            default:

               throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE, "No integration for event store : " + eventStore.name());

        }

    }


}
