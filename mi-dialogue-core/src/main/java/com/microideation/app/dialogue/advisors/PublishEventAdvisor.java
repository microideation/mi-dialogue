package com.microideation.app.dialogue.advisors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.dictionary.DialogueHeaderKeys;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.integration.DialogueIntegration;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sandheepgr on 17/6/16.
 */
@Aspect
@Component
public class PublishEventAdvisor {


    @Autowired
    private DialogueIntegration dialogueIntegration;

    @Autowired
    private ObjectMapper objectMapper;





    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() {  }


    @AfterReturning(value = "anyPublicMethod() && @annotation(publishEvent)",returning = "returnValue")
    public void publishEvent(JoinPoint joinPoint,Object returnValue,PublishEvent publishEvent) throws Throwable {

        // Check if the publishEvent is null or the return value is null
        if (publishEvent == null || returnValue == null) {

            // error logging
            return;

        }

        // Set the data
        String json = objectMapper.writeValueAsString(returnValue);

        // Create the DialogueEvent
        DialogueEvent dialogueEvent = new DialogueEvent(json);

        // Set the event name
        dialogueEvent.getHeaders().put(DialogueHeaderKeys.EVENT_NAME,publishEvent.eventName());

        // call the processPublishEvent method for processing
        dialogueIntegration.processPublishEvent(publishEvent, dialogueEvent);

    }

}
