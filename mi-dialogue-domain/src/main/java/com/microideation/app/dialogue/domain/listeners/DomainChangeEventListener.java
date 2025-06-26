package com.microideation.app.dialogue.domain.listeners;

import com.microideation.app.dialogue.dictionary.DomainChangeEventType;
import com.microideation.app.dialogue.domain.publisher.DomainChangePublisher;
import com.microideation.app.dialogue.domain.support.AutowireHelper;
import com.microideation.app.dialogue.service.DialogueEventBus;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by sandheepgr on 25/5/17.
 */
public class DomainChangeEventListener {



    @Autowired
    private DialogueEventBus dialogueEventBus;



    @PostPersist
    public void postPersist(Object target) {

        // Call the method with the eventType
        publishEvent(target,DomainChangeEventType.CREATE);

    }

    @PostUpdate
    public void postUpdate(Object target) {

        // Call the method with the eventType
        publishEvent(target,DomainChangeEventType.UPDATE);

    }

    @PostRemove
    public void postRemove(Object target) {

        // Call the method with the eventType
        publishEvent(target,DomainChangeEventType.DELETE);

    }




    /**
     * Method to publish the event for the target object and the event type
     *
     * @param target    : The target object generating the event
     * @param eventType : The event type
     */
    private void publishEvent(Object target, DomainChangeEventType eventType) {

        // Check if the targe is instance of the DomainEventPublisher
        if ( !(target instanceof DomainChangePublisher) ) {

            // Return
            return;

        }

        // Get the instance of the DomainChangePublisher
        DomainChangePublisher domainChangePublisher = (DomainChangePublisher) target;

        // Check if the channel and resource is not null
        if (    domainChangePublisher.getChannelName() == null ||
                domainChangePublisher.getChannelName().trim().equals("") ||
                domainChangePublisher.getResourceClass() == null ) {

            // return
            return;

        }

        // Autowire the eventbus using helper
        AutowireHelper.autowire(this,this.dialogueEventBus);

        // Publish the event
        // Publish the event
        dialogueEventBus.broadcastDomainChange( domainChangePublisher.getChannelName(),
                eventType,
                domainChangePublisher.getResourceClass(),
                target);

    }
}
