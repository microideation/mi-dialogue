package com.microideation.app.dialogue.domain.processor;

import com.microideation.app.dialogue.dictionary.DialogueHeaderKeys;
import com.microideation.app.dialogue.dictionary.DomainChangeEventType;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sandheepgr on 7/4/17.
 */
@Slf4j
public  abstract  class DomainChangeEventProcessor<T>  {


    /**
     * Method to update the domain object
     *
     * @param domain    : The domain object parsed
     */
    public abstract void update(T domain);

    /**
     * Method to insert the domain object
     *
     * @param domain    : The domain object parsed
     */
    public abstract void insert(T domain);

    /**
     * Method to delete the domain object
     *
     * @param domain    : The domain object parsed
     */
    public abstract void delete(T domain);




    /**
     * Method to process an unrecognized event
     * This method just logs the event and the domain received
     *
     * @param domain        : The domain object parsed
     * @param eventName     : The eventName received
     */
    public void processUnrecognizedEvent(T domain, String eventName) {

        // Log the error
        log.info("Received an unknown event : " + eventName +  " for object : " + domain);

    }


    /**
     * Method to process the replication event recevied
     *
     * @param dialogueEvent : The DialogueEvent object containing the payload and event name
     * @param type          : The class of the domain
     */
    public void processDomainChangeEvent(DialogueEvent dialogueEvent,Class<T> type) {

        // Check if the header is present
        if ( dialogueEvent.getHeaders() == null ||
                dialogueEvent.getHeaders().size() == 0 ||
                    !dialogueEvent.getHeaders().containsKey(DialogueHeaderKeys.EVENT_NAME)){

            // Log the error
            log.error("No event headers set for the event " + dialogueEvent);

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_EVENT_NAME_NOT_SPECIFIED,"No event name header found");

        }


        // Try to map the object
        T domain = dialogueEvent.getPayload(type);

        // The Replication event type
        DomainChangeEventType eventType = null;

        try {
            // Get the eventType
            eventType = DomainChangeEventType.valueOf(dialogueEvent.getHeaders().get(DialogueHeaderKeys.EVENT_NAME).toString());

        } catch (IllegalArgumentException e) {

            // Get the eventName
            String eventName = dialogueEvent.getHeaders().get(DialogueHeaderKeys.EVENT_NAME).toString();

            // Log the error
            log.error("Unrecognized domain event " + eventName);

            // Process it with the unrecognzied method
            processUnrecognizedEvent(domain,eventName);

        } catch (NullPointerException ex) {

            // Log the error
            log.error("Domaing event passed as null ");

            // Process it with the unrecognzied method
            processUnrecognizedEvent(domain,null);

        }

        // Check the event and call the appropriate method
        switch (eventType) {

            case CREATE:
                insert(domain);
                break;

            case UPDATE:
                update(domain);
                break;

            case DELETE:
                delete(domain);
                break;

            default:
                log.error("Unrecognized event type " + eventType);
                break;

        }

    }



}
