package com.microideation.app.dialogue.rsocket.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.rsocket.integration.MiRSocketPublisher;
import com.microideation.app.dialogue.rsocket.integration.MiRSocketSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableScheduling
@PropertySource(value = "classpath:rsocket.properties")
public class MiRSocketConfig {
	
	@Bean(name = "rsocketObjectMapper")
	public ObjectMapper rsocketObjectMapper() {
		return new ObjectMapper()
				// Add mixin to avoid the issue with the DialogueEvent class
				// and the ObjectMapper class inside it.
				.addMixIn(DialogueEvent.class, DialogueEventMixin.class)
				.registerModule(new JavaTimeModule())
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Bean(name = "miRSocketSubscribers")
	public ConcurrentHashMap<String,MiRSocketSubscriber> miRSocketSubscribers() {

		return new ConcurrentHashMap<>(0);
		
	}
	
	@Bean(name = "miRSocketPublishers")
	public ConcurrentHashMap<String,MiRSocketPublisher> miRSocketPublishers() {
		
		return new ConcurrentHashMap<>(0);
		
	}
	
	// IMPORTANT : This is a workaround to avoid the issue with the DialogueEvent
	// class and the ObjectMapper class.
	public abstract class DialogueEventMixin {
		@JsonIgnore
		private ObjectMapper objectMapper;

		@JsonIgnore
		public abstract ObjectMapper getObjectMapper();

		@JsonIgnore
		public abstract void setObjectMapper(ObjectMapper objectMapper);
	}
}
