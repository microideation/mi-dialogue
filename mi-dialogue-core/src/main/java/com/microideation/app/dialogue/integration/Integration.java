package com.microideation.app.dialogue.integration;

import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.event.DialogueEvent;

import javax.annotation.PreDestroy;

/**
 * Created by sandheepgr on 20/6/16.
 */
public interface Integration {

    public Object publishToChannel(PublishEvent publishEvent,DialogueEvent dialogueEvent);
    public void registerSubscriber(Object listenerClass, String methodName, SubscribeEvent subscribeEvent);
    @PreDestroy
    public void stopListeners();
    public boolean isIntegrationAvailable();

}
