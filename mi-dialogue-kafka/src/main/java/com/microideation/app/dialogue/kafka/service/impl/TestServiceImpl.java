package com.microideation.app.dialogue.kafka.service.impl;

import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.event.EventStore;
import com.microideation.app.dialogue.event.TestType;
import com.microideation.app.dialogue.kafka.service.TestService;
import org.springframework.stereotype.Service;

/**
 * Created by sandheepgr on 28/6/16.
 */
@Service
public class TestServiceImpl implements TestService {

    @PublishEvent(channelName = "com.test.publish",eventStore = EventStore.REDIS)
    public TestType publishToKafka() {

        return  new TestType("this is read by REDIS");

    }


    @SubscribeEvent(channelName = "com.test.publish",eventStore = EventStore.REDIS)
    public void subscribeKafka(DialogueEvent dialogueEvent) {

        TestType  testType = dialogueEvent.getPayload(TestType.class);
        System.out.println("read from REDIS" + testType);

    }

}
