package com.microideation.app.dialogue.service;

import com.microideation.app.dialogue.dictionary.DomainChangeEventType;
import com.microideation.app.dialogue.event.EventStore;
import com.microideation.app.dialogue.event.PublishType;

/**
 * Created by sandheepgr on 29/3/17.
 */
public interface DialogueEventBus {

    /**
     * Method to call the publish on the channel based on the minimum fields
     *
     * The following values are taken as default
     * eventName : ""
     * eventStore: RABBITMQ
     * isPersistent: false
     * publishType : BROADCAST
     *
     * @param channelName  : Name of the channel
     * @param payload      : The payload object
     */
    void publish(String channelName, Object payload) ;

    /**
     * Method to call the publish on the channel based on the minimum fields
     *
     * The following values are taken as default
     * eventName : ""
     * isPersistent: false
     * publishType : BROADCAST
     *
     * @param channelName  : Name of the channel
     * @param eventStore   : The event store to be used.
     * @param payload      : The payload object
     */
    void publish(String channelName, EventStore eventStore, Object payload);

    /**
     * Method to call the publish on the channel based on the minimum fields
     *
     * The following values are taken as default
     * eventStore  : Rabbitmq
     * isPersistent: false
     * publishType : EVENT_SPECIFIC
     *
     * @param channelName  : Name of the channel
     * @param eventName    : The eventName
     * @param payload      : The payload object
     */
    void publish(String channelName, String eventName, Object payload);

    /**
     * Method to call the publish on the channel with all the fields
     *
     * @param channelName   : Channel name to which the event need to be published
     * @param eventName     : Name of the event
     * @param eventStore    : Eventstore
     * @param publishType   : Event publish type
     * @param isPersistent  : Whether the channel is persistent or not
     * @param isSetAuthority: Whether the authority need to be set or not
     * @param payload       : The payload for the event
     */
    void publish(String channelName, String eventName, EventStore eventStore, PublishType publishType, boolean isPersistent,boolean isSetAuthority, Object payload);

    /**
     * Method to broadcast the event change for the Domain
     * This will convert the domain to resource class specified and
     * broadcast that Resource object
     *
     * @param channelName    : The name of the channel using which to send
     * @param eventType      : The type of the event
     * @param resourceClass  : The resource class
     * @param entity         : The entity to be converted to Resource
     * @param <T>            : The Resrouce class
     */
    <T> void broadcastDomainChange(String channelName, DomainChangeEventType eventType, Class<T> resourceClass, Object entity);

    /**
     * Method to broadcast the event change for the Domain
     * This will convert the domain to resource class specified and
     * broadcast that Resource object
     *
     * @param channelName    : Name of the channel
     * @param eventType      : EventType
     * @param eventStore     : EventStore to use for events
     * @param isPersistent   : IS the event store persistent
     * @param isSetAuthority : Do we need to set the authority
     * @param resourceClass  : The Resource class to which the entity need to be converted
     * @param entity         : The actual entity object
     * @param <T>            : Resource class
     */
    <T> void broadcastDomainChange(String channelName, DomainChangeEventType eventType, EventStore eventStore, boolean isPersistent, boolean isSetAuthority, Class<T> resourceClass, Object entity);
}
