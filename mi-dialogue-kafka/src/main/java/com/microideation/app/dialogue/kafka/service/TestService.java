package com.microideation.app.dialogue.kafka.service;

import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.event.TestType;

/**
 * Created by sandheepgr on 28/6/16.
 */
public interface TestService {

    public TestType publishToKafka();
    public void subscribeKafka(DialogueEvent dialogueEvent);

}
