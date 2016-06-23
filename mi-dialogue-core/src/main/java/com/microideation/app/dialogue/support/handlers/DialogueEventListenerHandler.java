package com.microideation.app.dialogue.support.handlers;

import com.microideation.app.dialogue.annotations.DialogueEventListener;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.integration.DialogueIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by sandheepgr on 18/6/16.
 */
@Component
public class DialogueEventListenerHandler {

    // Create the logger
    private Logger log = LoggerFactory.getLogger(DialogueEventListenerHandler.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DialogueIntegration dialogueIntegration;


    /**
     * Method called by the spring when the context is ready
     * This is based on the org.springframework.context.event.EventListener({ContextRefreshedEvent.class})
     */
    @EventListener({ContextRefreshedEvent.class})
    public void initializeEventListeners()  {


        // Get the list of listenerClasses that are annotated with EventListener annotation
        final Map<String,Object>  listenerClasses = applicationContext.getBeansWithAnnotation(DialogueEventListener.class);

        // If there are no listenerClasses,log the information
        if ( listenerClasses == null || listenerClasses.isEmpty() ) {

            // Log the information
            log.info("No classes tagged with EventListener annotation");

            // return
            return;

        }

        // Iterate through the listenerClasses
        for ( final Object listenerClass : listenerClasses.values() ) {

            // Get the base class for the item ( this is due to the proxying of spring )
            // If we directly call getClass on listenerClass, then it returns proxy class
            Class finalClass = AopProxyUtils.ultimateTargetClass(listenerClass);

            // Get the methods of the class
            Method[] methods = finalClass.getDeclaredMethods();

            // If there are no methods for the subscription of the event, then
            // we need to log the details
            if ( methods == null || methods.length == 0  ) {

                // Log the information
                log.info("No methods tagged with SubscribeEvent annotation for : "+finalClass.getName());

                // continue the loop
                continue;

            }

            // Get the methods on the class
            for ( Method m : methods) {

                // Check if the method has the annotation present
                if ( m.isAnnotationPresent(SubscribeEvent.class)) {

                    // Get the subscribeEvent object
                    SubscribeEvent subscribeEvent = m.getAnnotation(SubscribeEvent.class);

                    // Log the method
                    log.info("Adding subscriber event for : " + subscribeEvent + " on method : " + m.getName());

                    // Call the method to add thel listener to the method
                    dialogueIntegration.addListenerToSubscriberMethod(m, subscribeEvent, listenerClass);

                    // Log the method
                    log.info("Subscriber added for event : " + subscribeEvent + " on method : " + m.getName());

                }

            }
        }
    }
}
