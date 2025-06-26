package com.microideation.app.dialogue.rsocket.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.rsocket.utils.RSocketGeneralUtils;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiRSocketPublisher {

	private RSocket socket;

	private final String addr;
	
	private final ObjectMapper mapper;

	
	public MiRSocketPublisher(String addr, ObjectMapper mapper) {
		this.addr = addr;
		this.mapper = mapper;
	}
	
	
	/**
	 * Method to check if the socket is connected
	 * @return : Return the connect status
	 */
	public boolean isConnected() {
		
		return this.socket != null && !this.socket.isDisposed();
	}
	
	/**
	 * Method to connect the socket to the address specified
	 * If there is any exception only logging is done for now
	 */
	public void connectSocket() {
		
		try {
		
			// Get the split address
			String[] splitAddress = RSocketGeneralUtils.splitAddress(addr);
			
			// Try to connect to the specified address
			this.socket = RSocketConnector.create()
					.connect(TcpClientTransport.create(splitAddress[0],Integer.parseInt(splitAddress[1])))
					.block();
		} catch (Exception  e) {
			
			// Log the error
			log.error("connectSocket -> Exception while connecting to "+addr+ " Error : " + e.getMessage());
		
			// Print the stacktrace
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Method to dispose the connect
	 */
	public void dispose() {
		if (this.socket != null) {
			this.socket.dispose();
		}
	}
	
	/**
	 * Method to publish the data to the socket using fireAndForget
	 * The data is converted to JSON string using the mapper
	 *
	 * @param event : The DialogueEvent object to be sent over socket
	 */
	public void publishData(DialogueEvent event) {
	
		String strJson;
		
		try {
			
			// Create the json string for the event object
			strJson = mapper.writeValueAsString(event);
			
			// Send the data using fire and forget
			this.socket.fireAndForget(DefaultPayload.create(strJson)).block();
			
		} catch (Exception e) {
			
			// Log the error
			log.error("publishData -> Exception while publishing : " + e.getMessage());
			
			// Print the stack trace
			e.printStackTrace();
		}
		
	}
	

}
