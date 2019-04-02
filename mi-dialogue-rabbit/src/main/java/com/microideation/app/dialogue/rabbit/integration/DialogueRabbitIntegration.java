package com.microideation.app.dialogue.rabbit.integration;

import com.microideation.app.dialogue.annotations.PublishEvent;
import com.microideation.app.dialogue.annotations.SubscribeEvent;
import com.microideation.app.dialogue.event.DialogueEvent;
import com.microideation.app.dialogue.event.PublishType;
import com.microideation.app.dialogue.integration.Integration;
import com.microideation.app.dialogue.integration.IntegrationUtils;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sandheepgr on 18/6/16.
 */
@Component("dialogueRabbitIntegration")
@PropertySource("classpath:mi-dialogue-rabbitmq-.properties")
public class DialogueRabbitIntegration implements Integration {


    /**
     * NOTE : For handling the properties of library and for overriding by an implementing client
     * we need to have the specific properties put in a respective file and import using @PropertSource
     * After that this property can be overridden by the  implementing project using application.yml or
     * application.properties file.
     * If we try to put the properties inside the application.yml / application.properties of the library,
     * these will not be read in the implementing project ( No defaults taken from library )
     *
     * Refer to the mi-dialogue-rabbitmq-.propertie usage in this class
     */

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private IntegrationUtils integrationUtils;

    @Autowired
    private Environment environment;

    // Read the values for the rabbit retry settings


    @Value("${dialogue.rabbit.retry.dlxname}")
    private String dlxName;

    @Value("${dialogue.rabbit.retry.maxattempts}")
    private Integer maxAttempts;

    @Value("${dialogue.rabbit.retry.initialinterval}")
    private Long initialInterval;

    @Value("${dialogue.rabbit.retry.multiplier}")
    private Double multiplier;

    @Value("${dialogue.rabbit.retry.maxinterval}")
    private Long maxInterval;


    @Resource
    private ConcurrentHashMap<String,Exchange> rabbitChannels;

    @Resource
    private ConcurrentHashMap<String,Queue> rabbitQueues;

    @Resource
    private ConcurrentHashMap<String,SimpleMessageListenerContainer> rabbitContainers;


    /**
     * Method to build the queue using the autowired rabbit configuration
     *
     * @param queueName    : The name of the queue
     * @param publishEvent : The PublishEvent annotation object
     *
     * @return          : Return the queue object created
     */
    public Exchange buildExchange(String queueName, PublishEvent publishEvent) {

        // Check if the queue is already existing
        if ( rabbitChannels.containsKey(queueName) ) {

            // return the queue from the list
            return rabbitChannels.get(queueName);

        }

        // Check the event type and based on that declare the exchange
        // If the type is broadcast, we need to declare a Fanout exchange so that
        // the event is passed to all the queues irrespective of the routing key
        if ( publishEvent.publishType() == PublishType.BROADCAST ) {

            //add a fanout exchange for the queue
            FanoutExchange exchange = new FanoutExchange(queueName);

            //declare the exchange
            amqpAdmin.declareExchange(exchange);

            // Add the queue to the channels list
            rabbitChannels.put(queueName, exchange);

            // return exchange
            return exchange;

        } else {

            // Check if the eventName is not specified, we need to throw an error as eventName is not there
            if ( publishEvent.eventName() == null || publishEvent.eventName().equals("") ) {

                // Throw exception
                throw new DialogueException(ErrorCode.ERR_EVENT_NAME_NOT_SPECIFIED,"Eventname not specified for the eventType : LISTENER_ONLY : "+
                                            publishEvent.channelName()
                                           );


            }

            // Add a Direct Exchange
            DirectExchange directExchange = new DirectExchange(queueName);

            //declare the exchange
            amqpAdmin.declareExchange(directExchange);

            // Add the queue to the channels list
            rabbitChannels.put(queueName, directExchange);

            // Return the direct exchange
            return directExchange;

        }

    }

    /**
     * Method to build the queue using the autowired rabbit configuration
     *
     * @param channelName: The name of channel ( exchange )
     * @param queueName : The name of the queue
     * @param persist   : Flag showing whether the queue need to be persistent or not
     * @param eventName : The name of the event listening to ( routing key )
     *
     * @return          : Return the queue object created
     */
    public Queue buildQueue(String channelName,String queueName,boolean persist,String eventName) {

        // Check if the queue is already existing
        if ( rabbitQueues.containsKey(queueName) ) {

            // return the queue from the list
            return rabbitQueues.get(queueName);

        }

        // Declare the queue object
        Queue queue = new Queue(queueName,persist);

        //declare the queue
        amqpAdmin.declareQueue(queue);

        // Check if the eventName is specified, then use a direct exchange with
        // eventName as the routingKey
        if ( eventName != null && !eventName.equals("")) {

            // Create a directExchange
            DirectExchange directExchange = new DirectExchange(channelName);

            //declare the exchange
            amqpAdmin.declareExchange(directExchange);

            //add binding for queue and exchange
            amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(directExchange).with(eventName));

            // Add the queue to the channels list
            rabbitQueues.put(queueName,queue);


        } else {

            //add a topic exchange for the queue
            FanoutExchange exchange = new FanoutExchange(channelName);

            //declare the exchange
            amqpAdmin.declareExchange(exchange);

            //add binding for queue and exchange
            amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange));

            // Add the queue to the channels list
            rabbitQueues.put(queueName, queue);

        }

        // Define the dead-letter binding
        defineDeadletterBinding(queue,persist);

        // Return the queue
        return queue;

    }

    /**
     * Method to create the SimpleMessageListener object for the queue for subscribing
     *
     * @param listener      : The listener class object
     * @param methodName    : The name of the method for the listener
     * @param subscribeEvent: The subscribeEvent annotation object
     *
     * @return              : Return the SimpleMessageListenerContainer object
     */
    public SimpleMessageListenerContainer createListenerContainer(Object listener,String methodName,SubscribeEvent subscribeEvent) {

        // Set the channelName
        String channelName = subscribeEvent.channelName();

        // Get the property value for the channelName
        channelName = integrationUtils.getEnvironmentProperty(channelName);

        // if the channel name is greater than 250 characters, then reject it
        if ( channelName.length() > 250 ) {

            // Throw the DialogueException
            throw new DialogueException("Channel name exceeds 250 character limit for channel " + channelName);

        }

        // Set the queueName to be the
        String queueName = getQueueName(listener,methodName,channelName,subscribeEvent.eventName());

        // if the queueName is greater than 250 characters, then reject it
        if ( queueName.length() > 250 ) {

            // Throw the DialogueException
            throw new DialogueException("Queue name exceeds 250 character limit for queue " + queueName);

        }

        // If the queue already contains the listener, then return the instance
        if ( rabbitContainers.containsKey(queueName) ) {

            // Return the queue reference from the list
            return rabbitContainers.get(queueName);

        }


        // Build the queue
        // If the queue is already existing, this will fail.
        buildQueue(channelName,queueName,false,subscribeEvent.eventName());

        //add a message listener for the queue , receiver object is also created for each smsChannel
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter( listener ,methodName);

        //add a messageListenerContainer for the receiver , and set the parameters
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setQueueNames(queueName);
        simpleMessageListenerContainer.setConcurrentConsumers(subscribeEvent.concurrentConsumers());
        simpleMessageListenerContainer.setMessageListener(messageListenerAdapter);
        simpleMessageListenerContainer.setAdviceChain(new Advice[] {retryInterceptor(dlxName,queueName)});
        simpleMessageListenerContainer.afterPropertiesSet();

        //start the receiver container
        simpleMessageListenerContainer.start();

        // Add to the queue
        rabbitContainers.put(queueName,simpleMessageListenerContainer);

        // Return the listener
        return simpleMessageListenerContainer;

    }

    /**
     * Method to publish an item to the queue
     *
     * @param publishEvent : The instance of publishEvent annotation
     * @param dialogueEvent: The object to be sent
     *
     * @return          : Return the object if the publish was successful
     *                    Return null otherwise
     */
    @Override
    public Object publishToChannel(PublishEvent publishEvent,DialogueEvent dialogueEvent) {

        // Get the property value for the channelName
        String channelName = integrationUtils.getEnvironmentProperty(publishEvent.channelName());

        // if the channel name is greater than 250 characters, then reject it
        if ( channelName.length() > 250 ) {

            // Throw the DialogueException
            throw new DialogueException("Channel name exceeds 250 character limit for channel " + channelName);

        }

        // Get the exchange
        Exchange exchange = buildExchange(channelName,publishEvent);

        // If the exchange is null, return false
        if ( exchange == null ) return false;

        // Send to the queue using the rabbitTemplate
        rabbitTemplate.convertAndSend(channelName,publishEvent.eventName(),dialogueEvent);

        // return the object passed;
        return dialogueEvent;

    }

    /**
     * Overridden method to register the subscriber
     * @param listenerClass : The listener class object
     * @param methodName    : The name of the method for the listener
     * @param subscribeEvent: The subscribeEvent annotation object
     *
     */
    @Override
    public void registerSubscriber(Object listenerClass, String methodName, SubscribeEvent subscribeEvent) {

        // Call the method to create the listener
        createListenerContainer(listenerClass,methodName,subscribeEvent);

    }

    /**
     * Method to be called when the spring context is finishing
     * This will call the stop on the containers
     */
    @PreDestroy
    @Override
    public void stopListeners() {

        // Iterate the through the containers and stop them
        for ( SimpleMessageListenerContainer container : rabbitContainers.values() ) {

            container.stop();

        }

    }

    /**
     * Method to check if the integration components are available for this
     * integration
     *
     * @return  : return true if the integration components are available
     *          :  return false else and throw the exception
     */
    @Override
    public boolean isIntegrationAvailable() {

        // check if any of the beans are null
        if ( rabbitTemplate == null ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,"rabbitTemplate bean is not available");

        } else if ( amqpAdmin == null ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,"amqpAdmin bean is not available");

        } else if ( connectionFactory == null ) {

            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_INTEGRATION_NOT_AVAILABLE,"connectionFactory bean is not available");

        }


        // Return true finally
        return true;

    }
    
    
    /**
     * Method to create the queue name in the following format
     * {channelname}-sub-{servicename}.{class}.{method}-{event}
     *
     * @param listener      : The listener object subscribed to the event
     * @param methodName    : The name of the method that is annotated with
     * @param channelName   : The channel to which queue is listening
     * @param eventName     : The name of the event if subscribed to an event
     *
     * @return              : Return the queue name based on the format with passed fields
     *                        If the service name is not specified, this will throw an error.
     */
    private String getQueueName(Object listener,String methodName, String channelName,String eventName) {
        
        // Get the service name
        String serviceName = environment.getProperty("spring.application.name");
        
        // If the name is null or empty, then don't start the queue
        if ( serviceName == null || serviceName.isEmpty() || serviceName.equals("spring.application.name")) {
            
            // Throw the exception
            throw new DialogueException(ErrorCode.ERR_SERVICE_NAME_NOT_PROVIDED,
                    "The service name is not set. Please set the spring.application.name property in application.properties / application.yml file");
            
            
        }
        
        // Remove spaces from service name
        serviceName = serviceName.replaceAll(" ","");
        
        // Get the class name
        String className = AopUtils.getTargetClass(listener).getSimpleName();
        
        // Set the queueName to be the
        String queueName = channelName +"-sub-"+serviceName.toLowerCase()+"."+className.toLowerCase()+"."+methodName.toLowerCase();
        
        // If the eventName is present, then add it to the queuename
        if ( eventName != null && !eventName.equals("")) {
            
            // add it to the queuename
            queueName += "-listen-"+eventName.toLowerCase();
            
        }
        
        // Return the queueName
        return queueName;
        
    }
    
    /**
     * Method to define the deadletter binding for the queue
     * The idea is to bind the deadletter exchange with the same queue
     * using the queue name as the routing key
     * In this way we can route the messages to the same queue
     * If we route to the same exchange using original routing key, all other queues bound
     * with same criteria will also receive the republish
     *
     * @param queue     : The queue for which the deadletter need to be defined
     * @param persist   : Do we need to persist the queue ( durable queue )
     *
     * @return          : Return the queue
     */
    private Queue defineDeadletterBinding(Queue queue, boolean persist) {
        
        // Create a Direct binding with the routing key as the queue name
        // This is for the failure republish to specific queue
        DirectExchange directExchange = new DirectExchange(dlxName);
        
        //declare the exchange
        amqpAdmin.declareExchange(directExchange);
        
        //add binding for queue and exchange
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(directExchange).with(queue.getName()));
        
        //  return the queue
        return queue;
        
    }

    /**
     * Method to build the retry interceptor for the rabbitmq listener
     * If the listener throws and exception,the retry goes on based on the
     * settings specified in the mi-dialogue-rabbitmq-.properties file.
     *
     * When the retries are exhausted, the item in put back into the same queue
     * for processing. The routing key used would be the key name and this would
     * trigger the deadletter binding for the queue
     *
     * @param errorExchange : The name of the exchange ( deadletter exchange )
     * @param routingKey    : The routing key ( the name of the queue )
     * @return
     */
    private RetryOperationsInterceptor retryInterceptor(String errorExchange,String routingKey) {

        // Build and return the Interceptor
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(maxAttempts)
                .backOffOptions(initialInterval, multiplier, maxInterval)
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate,errorExchange,routingKey))
                .build();
    }
}
