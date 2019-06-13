package com.microideation.app.dialogue.rsocket.config;

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
	

	@Bean(name = "miRSocketSubscribers")
	public ConcurrentHashMap<String,MiRSocketSubscriber> miRSocketSubscribers() {

		return new ConcurrentHashMap<>(0);
		
	}
	
	@Bean(name = "miRSocketPublishers")
	public ConcurrentHashMap<String,MiRSocketPublisher> miRSocketPublishers() {
		
		return new ConcurrentHashMap<>(0);
		
	}
}
