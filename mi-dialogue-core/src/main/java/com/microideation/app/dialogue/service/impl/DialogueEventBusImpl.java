package com.microideation.app.dialogue.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.dictionary.DialogueHeaderKeys;
import com.microideation.app.dialogue.dictionary.DomainChangeEventType;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.event.EventStore;
import com.microideation.app.dialogue.event.PublishEventImpl;
import com.microideation.app.dialogue.event.PublishType;
import com.microideation.app.dialogue.integration.DialogueIntegration;
import com.microideation.app.dialogue.service.DialogueEventBus;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by sandheepgr on 29/3/17.
 */
@Service
@Slf4j
public class DialogueEventBusImpl implements DialogueEventBus {


    @Autowired
    private DialogueIntegration dialogueIntegration;

    @Autowired
    private ObjectMapper objectMapper;



    @Override
    public void publish(String channelName, Object payload) {

        // Create the PublishEventImpl with the fields pased and defaults for others
        PublishEventImpl publishEvent = new PublishEventImpl();
        publishEvent.setAnnotationType(PublishEvent.class);
        publishEvent.setChannelName(channelName);
        publishEvent.setEventName("");
        publishEvent.setEventStore(EventStore.RABBITMQ);
        publishEvent.setPersistent(false);
        publishEvent.setPublishType(PublishType.BROADCAST);

        // Call the publishToChannel
        publishToChannel(publishEvent,payload);

    }

    @Override
    public void publish(String channelName, EventStore eventStore, Object payload) {

        // Create the PublishEventImpl with the fields pased and defaults for others
        PublishEventImpl publishEvent = new PublishEventImpl();
        publishEvent.setAnnotationType(PublishEvent.class);
        publishEvent.setChannelName(channelName);
        publishEvent.setEventName("");
        publishEvent.setEventStore(eventStore);
        publishEvent.setPersistent(false);
        publishEvent.setPublishType(PublishType.BROADCAST);

        // Call the publishToChannel
        publishToChannel(publishEvent,payload);

    }

    @Override
    public void publish(String channelName, String eventName, Object payload) {

        // Create the PublishEventImpl with the fields pased and defaults for others
        PublishEventImpl publishEvent = new PublishEventImpl();
        publishEvent.setAnnotationType(PublishEvent.class);
        publishEvent.setChannelName(channelName);
        publishEvent.setEventName(eventName);
        publishEvent.setEventStore(EventStore.RABBITMQ);
        publishEvent.setPersistent(false);
        publishEvent.setPublishType(PublishType.EVENT_SPECIFIC);

        // Call the publishToChannel
        publishToChannel(publishEvent,payload);

    }

    @Override
    public void publish(String channelName, String eventName, EventStore eventStore, PublishType publishType, boolean isPersistent, Object payload) {

        // Create the PublishEventImpl with the fields pased and defaults for others
        PublishEventImpl publishEvent = new PublishEventImpl();
        publishEvent.setAnnotationType(PublishEvent.class);
        publishEvent.setChannelName(channelName);
        publishEvent.setEventName(eventName);
        publishEvent.setEventStore(eventStore);
        publishEvent.setPersistent(isPersistent);
        publishEvent.setPublishType(publishType);

        // Call the publishToChannel
        publishToChannel(publishEvent,payload);

    }



    @Override
    public <T> void broadcastDomainChange(String channelName, DomainChangeEventType eventType, Class<T> resourceClass, Object entity) {

        // Create the PublishEventImpl with the fields pased and defaults for others
        PublishEventImpl publishEvent = new PublishEventImpl();
        publishEvent.setAnnotationType(PublishEvent.class);
        publishEvent.setChannelName(channelName);
        publishEvent.setEventName(eventType.name());
        publishEvent.setEventStore(EventStore.RABBITMQ);
        publishEvent.setPersistent(false);
        publishEvent.setPublishType(PublishType.BROADCAST);

        // Get the Resource
        T resource = convertToResource(entity,resourceClass);

        // Call the publishToChannel
        publishToChannel(publishEvent,resource);

    }




    @Override
    public <T> void broadcastDomainChange(String channelName, DomainChangeEventType eventType, EventStore eventStore, boolean isPersistent, Class<T> resourceClass, Object entity) {

        // Create the PublishEventImpl with the fields pased and defaults for others
        PublishEventImpl publishEvent = new PublishEventImpl();
        publishEvent.setAnnotationType(PublishEvent.class);
        publishEvent.setChannelName(channelName);
        publishEvent.setEventName(eventType.name());
        publishEvent.setEventStore(eventStore);
        publishEvent.setPersistent(isPersistent);
        publishEvent.setPublishType(PublishType.BROADCAST);

        // Get the Resource
        T resource = convertToResource(entity,resourceClass);

        // Call the publishToChannel
        publishToChannel(publishEvent,resource);

    }


    /**
     * Method to convert the entity to resource with the specified class
     *
     * @param entity    : The entity to be converted
     * @param type      : The Resource class type ( Class )
     * @param <T>       : The Resouce Type
     *
     * @return          : On success, return the Resource ( T ) object
     *                    On error
     */
    private <T> T convertToResource(Object entity, Class<T> type ) {

        try {

            // Store the source object as json string
            String json = objectMapper.writeValueAsString(entity);

            // Create the saleResource object
            T  resource = objectMapper.readValue(json, type);

            // return the resource
            return resource;

        } catch (JsonProcessingException ex) {

            // Log the error
            log.error("Error processing for json : "+ex.getMessage() + " Object : " + entity);

            // Print the stack trace
            ex.printStackTrace();

        } catch ( IOException e) {

            // Log the error
            log.error("IOException while writing to resource : "+e.getMessage() + " Object : " + entity);

            // Print the stack trace
            e.printStackTrace();

        }

        // Return null
        return null;

    }

    /**
     * Method to call the publishToChannel on the dialog integration
     *
     * @param publishEvent     : The PublishEvent annotation instance
     * @param payload          : The payload for the dialogue event
     */
    private void publishToChannel(PublishEvent publishEvent, Object payload) {

        // Set the data
        String json = "";

        try {

            // Create the json object
            json = objectMapper.writeValueAsString(payload);

        } catch (JsonProcessingException e) {

            // Print the stack trace
            e.printStackTrace();

            // Throw exception
            throw new DialogueException(ErrorCode.ERR_JSON_MAPPING_EXCEPTION,"Error during mapping of json");
        }

        // Create the DialogueEvent
        DialogueEvent dialogueEvent = new DialogueEvent(json);

        // Set the event name
        dialogueEvent.getHeaders().put(DialogueHeaderKeys.EVENT_NAME,publishEvent.eventName());

        // call the processPublishEvent method for processing
        dialogueIntegration.processPublishEvent(publishEvent, dialogueEvent);


    }



}
