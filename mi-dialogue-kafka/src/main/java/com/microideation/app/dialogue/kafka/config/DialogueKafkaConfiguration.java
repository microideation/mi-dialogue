package com.microideation.app.dialogue.kafka.config;

import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.kafka.integration.DialogueEventSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sandheepgr on 28/6/16.
 */
@Configuration
@ComponentScan(basePackages = {"com.microideation.app.dialogue.kafka.integration"})
public class DialogueKafkaConfiguration {

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${kafka.zookeeper.connect}")
    private String zookeeperConnect;

    @Value("${kafka.consumer.groupIdConfig:mi-dialogue-group}")
    private String defaultGroupId;

    @Value("${kafka.producer.retries:0}")
    private int producerRetries;

    @Value("${kafka.producer.batchSize:16384}")
    private int producerBatchSize;

    @Value("${kafka.producer.lingerMs:1}")
    private int producerLingerMs;

    @Value("${kafka.producer.bufferMemory:33554432}")
    private int producerBufferMemory;

    @Bean
    public KafkaTemplate<String, DialogueEvent> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    @Bean
    public ProducerFactory<String, DialogueEvent> kafkaProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddress);
        props.put(ProducerConfig.RETRIES_CONFIG, this.producerRetries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, this.producerBatchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, this.producerLingerMs);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, this.producerBufferMemory);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DialogueEventSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ConcurrentHashMap<String, KafkaMessageListenerContainer<String, DialogueEvent>> kafkaContainers() {
        return new ConcurrentHashMap<>(0);
    }
}
