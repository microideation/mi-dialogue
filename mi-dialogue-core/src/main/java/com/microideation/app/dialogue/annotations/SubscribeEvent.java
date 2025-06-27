package com.microideation.app.dialogue.annotations;

import com.microideation.app.dialogue.event.EventStore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sandheepgr on 18/6/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) //can use in method only.
public @interface SubscribeEvent {

	// The event store from which the event need to be subscribed to
    EventStore eventStore();
    
    // Name of the channel
    String channelName();
    
    // Name of the event
    String eventName() default "";
    
    // Flag whether authentication need be applied if available
    boolean isSetAuthentication() default true;
    
    // Concurrenct consumers in the case of rabbitmq
    int concurrentConsumers() default 5;
    
    // Group ID for Kafka consumers (optional, will use default if not specified)
    String groupId() default "";

}
