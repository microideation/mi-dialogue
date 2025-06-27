package com.microideation.app.dialogue.kafka.integration;

import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.integration.Integration;
import com.microideation.app.dialogue.integration.IntegrationUtils;
import com.microideation.app.dialogue.kafka.config.DialogueKafkaConfiguration;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by sandheepgr on 28/6/16.
 */
@Component("dialogueKafkaIntegration")
public class DialogueKafkaIntegration implements Integration {

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${kafka.consumer.groupIdConfig:mi-dialogue-group}")
    private String defaultGroupId;

    @Autowired
    private DialogueKafkaConfiguration kafkaConfiguration;

    @Autowired
    private IntegrationUtils integrationUtils;

    @Autowired
    private KafkaTemplate<String, DialogueEvent> kafkaTemplate;

    @Resource
    private ConcurrentHashMap<String, KafkaMessageListenerContainer<String, DialogueEvent>> kafkaContainers;

    // Cache to track which topics we've already checked/created
    private final ConcurrentHashMap<String, Boolean> topicExistsCache = new ConcurrentHashMap<>();

    @Override
    public Object publishToChannel(PublishEvent publishEvent, DialogueEvent dialogueEvent) {
        // Get the property value for the channelName
        String channelName = integrationUtils.getEnvironmentProperty(publishEvent.channelName());

        // Ensure the topic exists before publishing
        ensureTopicExists(channelName, publishEvent.partitionCount(), publishEvent.replicationFactor());

        // Send to the channel
        kafkaTemplate.send(channelName, dialogueEvent);

        //  return dialogueEvent
        return dialogueEvent;
    }

    @Override
    public void registerSubscriber(Object listenerClass, String methodName, SubscribeEvent subscribeEvent) {
        // Check if the subscriber has got eventname specified, if yes, we need to
        // show error message as listening to specific key is not supported as of now
        if (subscribeEvent.eventName() != null && !subscribeEvent.eventName().equals("")) {
            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_EVENT_SPECIFIC_SUBSCRIBER_NOT_SUPPORTED,
                    "Event name specific listening is not supported in Kafka integration");
        }

        // Get the channelName
        String channelName = subscribeEvent.channelName();

        // Ensure the topic exists before subscribing (use default values for partitions and replication)
        ensureTopicExists(channelName, 3, 1);

        // Get the finalClass for the listenerClass
        Class<?> finalClass = AopProxyUtils.ultimateTargetClass(listenerClass);

        // Get the method by name
        Method method = ReflectionUtils.findMethod(finalClass, methodName, null);

        // Determine the group ID to use
        String groupId = determineGroupId(subscribeEvent, listenerClass, methodName);

        // Create a consumer factory with the specific group ID
        ConsumerFactory<String, DialogueEvent> consumerFactory = createConsumerFactory(groupId);

        System.out.println("Creating consumer factory for groupId: " + groupId);

        // Create container properties
        ContainerProperties containerProperties = new ContainerProperties(channelName);
        
        // Create a listener
        KafkaMessageListenerContainer<String, DialogueEvent> kafkaMessageListenerContainer =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        // Set the message listener
        kafkaMessageListenerContainer.setupMessageListener((MessageListener<String, DialogueEvent>) record -> {
            // IMPORTANT: This will throw error if the method is inside an inner class
            ReflectionUtils.invokeMethod(method, listenerClass, record.value());
        });

        // Start the container
        kafkaMessageListenerContainer.start();

        // Store the container for later cleanup
        kafkaContainers.put(channelName + "_" + methodName, kafkaMessageListenerContainer);
    }

    /**
     * Ensure that a Kafka topic exists, create it if it doesn't
     * @param topicName the name of the topic
     * @param partitionCount the number of partitions
     * @param replicationFactor the replication factor
     */
    private void ensureTopicExists(String topicName, int partitionCount, int replicationFactor) {
        // Check if we've already verified this topic exists
        if (topicExistsCache.containsKey(topicName)) {
            return;
        }

        try (AdminClient adminClient = AdminClient.create(createAdminConfig())) {
            // Check if topic exists
            boolean topicExists = adminClient.listTopics().names().get().contains(topicName);
            
            if (!topicExists) {
                System.out.println("Topic '" + topicName + "' does not exist. Creating with " + 
                                 partitionCount + " partitions and replication factor " + replicationFactor);
                
                // Create the topic
                NewTopic newTopic = new NewTopic(topicName, partitionCount, (short) replicationFactor);
                CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));
                
                // Wait for the topic creation to complete
                result.all().get();
                
                System.out.println("Topic '" + topicName + "' created successfully");
            } else {
                System.out.println("Topic '" + topicName + "' already exists");
            }
            
            // Mark this topic as checked
            topicExistsCache.put(topicName, true);
            
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error creating topic '" + topicName + "': " + e.getMessage());
            // Don't throw exception here as the topic might already exist
            // Just log the error and continue
        }
    }

    /**
     * Create admin client configuration
     * @return Map containing admin client configuration
     */
    private Map<String, Object> createAdminConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", brokerAddress);
        return config;
    }

    /**
     * Create a consumer factory with a specific group ID
     * @param groupId the group ID to use for this consumer
     * @return ConsumerFactory configured with the specified group ID
     */
    private ConsumerFactory<String, DialogueEvent> createConsumerFactory(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 15000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DialogueEventDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Determine the group ID to use for this subscription
     * @param subscribeEvent the subscription event annotation
     * @param listenerClass the listener class
     * @param methodName the method name
     * @return the group ID to use
     */
    private String determineGroupId(SubscribeEvent subscribeEvent, Object listenerClass, String methodName) {
        // If group ID is specified in the annotation, use it
        if (subscribeEvent.groupId() != null && !subscribeEvent.groupId().trim().isEmpty()) {
            return subscribeEvent.groupId();
        }
        
        // Otherwise, generate a default group ID based on class and method
        Class<?> finalClass = AopProxyUtils.ultimateTargetClass(listenerClass);
        return finalClass.getSimpleName() + "-" + methodName + "-group";
    }

    @Override
    public void stopListeners() {
        // Iterate through the containers and stop them
        for (KafkaMessageListenerContainer<String, DialogueEvent> container : kafkaContainers.values()) {
            container.stop();
        }
    }

    @Override
    public boolean isIntegrationAvailable() {
        // check if the beans are available
        if (kafkaConfiguration == null) {
            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE, "kafkaConfiguration bean is not available");
        } else if (kafkaTemplate == null) {
            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE, "kafkaTemplate bean is not available");
        }

        // Return true finally
        return true;
    }
}
