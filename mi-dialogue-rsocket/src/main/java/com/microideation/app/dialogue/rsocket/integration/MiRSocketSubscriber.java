package com.microideation.app.dialogue.rsocket.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.rsocket.utils.RSocketGeneralUtils;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.TcpServerTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Method;

@Slf4j
public class MiRSocketSubscriber {

	
	private Disposable server;

	private String addr;
	
	private ObjectMapper mapper;
	
	private Object listenerClass;
	
	private Method method;
	

	public MiRSocketSubscriber(String addr, ObjectMapper mapper, Object listenerClass, Method method) {
		this.addr = addr;
		this.mapper = mapper;
		this.listenerClass = listenerClass;
		this.method = method;
	}
	
	
	
	/**
	 * Method to dispose the socket connection
	 */
	public void dispose() {
		this.server.dispose();
	}

	
	/**
	 * Method to start listening on the specified hostname and port
	 * This will connect to the host and start subscribing
	 */
	public void startListening() {
		
		try {
			
			// Get the split address
			String[] splitAddress = RSocketGeneralUtils.splitAddress(addr);
			
			this.server = RSocketFactory
					.receive()
					.acceptor((payload,reactiveSocket) -> Mono.just(new MiRSocketImpl()))
					.transport(TcpServerTransport.create(splitAddress[0], Integer.parseInt(splitAddress[1])))
					.start()
					.subscribe();
		} catch (Exception e) {
			
			// Log the error
			log.error("connectSocket -> Exception while connecting to "+addr+ " Error : " + e.getMessage());
			
			// Print the stacktrace
			e.printStackTrace();
		}
	}

	
	/**
	 * Inner class extending the AbstractRocket
	 * we will be overriding the fireAndForget method to receive the payload
	 * and then parse the string to DialogueEvent
	 */
	private class MiRSocketImpl extends AbstractRSocket {
		
		@Override
		public Mono<Void> fireAndForget(Payload payload) {
			
			// Read the data as String and try to parse to DialogueEvent
			DialogueEvent event;
			
			try {
				
				// Map to Dialogue event
				event = mapper.readValue(payload.getDataUtf8(),DialogueEvent.class);
				
				// Invoke the specified method using Reflection
				ReflectionUtils.invokeMethod(method,listenerClass,event);
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		
			// Return empty
			return Mono.empty();
		}
	}
}
