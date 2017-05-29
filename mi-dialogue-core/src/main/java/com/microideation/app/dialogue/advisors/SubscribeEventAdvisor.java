package com.microideation.app.dialogue.advisors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.authority.DialogueEventAuthorityAuth;
import com.microideation.app.dialogue.authority.EventAuthority;
import com.microideation.app.dialogue.event.DialogueEvent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sandheepgr on 18/6/16.
 */
@Aspect
@Component
public class SubscribeEventAdvisor {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DialogueEventAuthorityAuth dialogueEventAuthorityAuth;


    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() {  }


    @Around(value = "anyPublicMethod() && @annotation(subscribeEvent) &&args(dialogueEvent,..)")
    public void subscribeEvent(ProceedingJoinPoint joinPoint,SubscribeEvent subscribeEvent,DialogueEvent dialogueEvent) throws Throwable {

        // Check if the subscribeEvent is null
        if ( subscribeEvent == null ) {

            // error logging
            return;

        }

        // Set the objectMapper
        dialogueEvent.setObjectMapper(objectMapper);

        // Check if the setAuthentication is true
        if ( subscribeEvent.isSetAuthentication() ) {

            // Check if the eventAuthority is present
            EventAuthority eventAuthority = dialogueEvent.getEventAuthority();

            // Check if the eventAuthority is null
            if ( eventAuthority != null ) {

                // Call the method in the dialogueEventAuthorityAuth
                dialogueEventAuthorityAuth.setEventAuthorityAuth(eventAuthority);

            }

        }

        // Create the arrya
        DialogueEvent dialogueEvents[] = {dialogueEvent};

        // Proceed with the new dialogueevents
        joinPoint.proceed(dialogueEvents);
    }

}
