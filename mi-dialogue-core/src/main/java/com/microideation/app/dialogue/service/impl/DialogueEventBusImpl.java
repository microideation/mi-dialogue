package com.microideation.app.dialogue.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.dictionary.DialogueHeaderKeys;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.event.EventStore;
import com.microideation.app.dialogue.event.PublishEventImpl;
import com.microideation.app.dialogue.event.PublishType;
import com.microideation.app.dialogue.integration.DialogueIntegration;
import com.microideation.app.dialogue.service.DialogueEventBus;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by sandheepgr on 29/3/17.
 */
@Service
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
