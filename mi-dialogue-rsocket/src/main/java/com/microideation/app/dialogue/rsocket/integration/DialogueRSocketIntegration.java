package com.microideation.app.dialogue.rsocket.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.integration.Integration;
import com.microideation.app.dialogue.integration.IntegrationUtils;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Component("dialogueRSocketIntegration")
@Slf4j
public class DialogueRSocketIntegration implements Integration {

	
	@Resource
	private ConcurrentHashMap<String,MiRSocketSubscriber> miRSocketSubscribers;
	
	@Resource
	private ConcurrentHashMap<String,MiRSocketPublisher> miRSocketPublishers;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private IntegrationUtils integrationUtils;
	
	
	public DialogueRSocketIntegration( ) {
	}
	
	
	@Override
	public Object publishToChannel(PublishEvent publishEvent, DialogueEvent dialogueEvent) {
	
		// Get the channel name ( hostname:port )
		String channelName = integrationUtils.getEnvironmentProperty(publishEvent.channelName());
		
		// Check if there is an existing publisher in the map
		MiRSocketPublisher publisher = miRSocketPublishers.get(channelName);
		
		// Check if null
		if ( publisher == null ) {
			
			// Log the info
			log.info("publishToChannel -> No publisher for " + channelName + " , creating new ");
			
			// Create a new publisher
			publisher = new MiRSocketPublisher(channelName,mapper);
			
			// Connect
			publisher.connectSocket();
			
			// Add to the map
			miRSocketPublishers.put(channelName,publisher);
		
			// Log
			log.info("publishToChannel -> Created new publisher for : " + channelName);
			
		}
		
		// Check if the publisher is connected
		if ( !publisher.isConnected() ) {
			
			// Log
			log.info("publishToChannel -> Reconnecting to " + channelName);
			
			// connect
			publisher.connectSocket();
			
		}
		
		// Send to the socket
		publisher.publishData(dialogueEvent);
		
		// Return the event
		return dialogueEvent;
		
	}
	
	@Override
	public void registerSubscriber(Object listenerClass, String methodName, SubscribeEvent subscribeEvent) {

		// We are not currently supporting event names ( routing ). So lets reject the
		// subscribers with event names
		if ( subscribeEvent.eventName() != null && !subscribeEvent.eventName().equals("") ) {
			
			// Throw the exception
			throw new DialogueException(ErrorCode.ERR_EVENT_SPECIFIC_SUBSCRIBER_NOT_SUPPORTED,
					"Event name specific listening is not supported in RSocket integration");
			
		}
		
		// Get the channel name ( hostname:port )
		String channelName = integrationUtils.getEnvironmentProperty(subscribeEvent.channelName());
	
		// Check if the channel is already being listened. We can only have
		// one subscriber ( server ) running for given address
		if ( miRSocketSubscribers.containsKey(channelName) ) {
			
			// Throw exception
			throw new DialogueException(ErrorCode.ERR_DUPLICATE_SUBSCRIBER_NOT_SUPPORTED,
					"RSocket only allows single subscriber instance. " +channelName + " has multiple declaration");
		}
	
		// Log the info
		log.info("registerSubscriber -> Registering subscriber for address: " + channelName);
		
		// Get the finalClass for the listenerClass
		Class finalClass = AopProxyUtils.ultimateTargetClass(listenerClass);
		
		// Get the method by name
		Method method = ReflectionUtils.findMethod(finalClass,methodName,null);
		
		// Create the subscriber
		MiRSocketSubscriber subscriber = new MiRSocketSubscriber(channelName,mapper,listenerClass,method);
	
		// Start listening
		subscriber.startListening();
	
		// log
		log.info("registerSubscriber -> Subscriber listening for : " + channelName);
		
		// Add to the map
		miRSocketSubscribers.put(channelName,subscriber);
		
	}
	
	@Override
	public void stopListeners() {

		// Iterate through the publishers and stop
		miRSocketPublishers
				.entrySet()
				.stream()
				.forEach(e -> e.getValue().dispose());
		
		// Iterate through the subscribers and stop
		miRSocketSubscribers
				.entrySet()
				.stream()
				.forEach(e -> e.getValue().dispose());
	}
	
	@Override
	public boolean isIntegrationAvailable() {
	
		// Check if the mapper is defined
		if ( mapper == null ) {
			
			// Throw the exception
			throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,
					"Jackson object mapper is a dependency and is not available");
			
		}
	
		// Return
		return true;
	}

	@PreDestroy
	public void destroy() {

		// Log
		log.info("RSocketIntegration unloading - stopping listeners");
	
		// Stop the listeners
		stopListeners();
		
	}
	
}
