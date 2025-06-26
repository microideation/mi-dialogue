package com.microideation.app.dialogue.rsocket.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.rsocket.utils.RSocketGeneralUtils;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
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
	private boolean isConnected = false;
	private boolean isShutdown = false;
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
	 * Method to check if the server is listening
	 * @return : Return the connect status
	 */
	public boolean isConnected() {
		return this.server != null && isConnected;
	}

	/**
	 * Method to dispose the socket connection
	 */
	public void dispose() {
		// Set the isShutdown to true
		isShutdown = true;
		
		// Dispose the server
		if (this.server != null) {
			this.server.dispose();
		}
	}

	/**
	 * Method to start listening on the specified hostname and port
	 * This will connect to the host and start subscribing
	 */
	public void startListening() {
		try {
			// Get the split address
			String[] splitAddress = RSocketGeneralUtils.splitAddress(addr);
			
			this.server = RSocketServer.create(SocketAcceptor.forFireAndForget(payload -> {
				// Read the data as String and try to parse to DialogueEvent
				try {
					// Map to Dialogue event
					DialogueEvent event = mapper.readValue(payload.getDataUtf8(), DialogueEvent.class);
					
					// Invoke the specified method using Reflection
					ReflectionUtils.invokeMethod(method, listenerClass, event);
					
				} catch (IOException e) {
					log.error("Error parsing DialogueEvent: " + e.getMessage(), e);
				}
				
				// Return empty
				return Mono.empty();
			}))
			.bind(TcpServerTransport.create(splitAddress[0], Integer.parseInt(splitAddress[1])))
			.subscribe();
			
			// Set the isConnected to true
			isConnected = true;
			
		} catch (Exception e) {
			// Log the error
			log.error("connectSocket -> Exception while connecting to " + addr + " Error : " + e.getMessage());
			
			// Set the isConnected to false;
			isConnected = false;
			
			// Print the stacktrace
			e.printStackTrace();
		}
	}

	/**
	 * This method will be used to refresh the connection is the
	 * connection is already disposed off unintentionally
	 */
	public void refreshConnection() {
		// If we are shutting down on request, we don't need to reconnect
		if (isShutdown) {
			// Log the warning
			log.trace("refreshConnection -> Shutdown is progress.. Refresh cancelled");
			
			// Return
			return;
		}

		// Check if disposed
		if (!isConnected()) {
			// Log the info
			log.info("refreshConnection -> Subscriber @ " + addr + " not listening, starting again");
			
			// Start listening
			startListening();
		}
	}
}
