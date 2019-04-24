
# Mi-Dialogue

Mi-Dialogue is set of spring based libraries that provides out-of-the-box features for microservices communication ( or dialogue for that matter). Following are the main features supported.

1. Event based communication over Redis, Kafka and RabbitMQ
2. Domain change replications on different services
3. Authentication carry forward with events
4. Event subscription failure handling with backoff-policy

## Table of Contents

- [Why Mi-Dialogue ?](#why-mi-dialogue)
- [Modules in Mi-Dialogue](#modules-in-mi-dialogue)
    - [mi-dialogue-core](#mi-dialogue-core)
        - [Publishing an Event](#publishing-an-event)
        - [Subscribing to an event](#subscribing-to-an-event)
    - [mi-dialogue-rabbit](#mi-dialogue-rabbit)
        - [Conventions](#conventions)
    - [mi-dialogue-redis](#mi-dialogue-redis)
    - [mi-dialogue-kafka](#mi-dialogue-kafka)

## Why Mi-Dialogue ?
Mi-Dialogue was created out requirement for a event based communication without the hassles of going through the store implementation or the spring-integration process. 

## Modules in Mi-Dialogue
Mi-dialogue has following modules and can be selectively enabled in a project based on the requirements. The mi-dialogue-core is a common module for all the modules.

1. mi-dialogue-core
2. mi-dialogue-rabbit
3. mi-dialogue-redis
4. mi-dialogue-kafka
5. mi-dialogue-domain

### mi-dialogue-core
Provides the basic and common support functionality to the other modules in the library. 

* Declares the DialogueEvent object which holds the payload and the header information for the event ( authority, any other user specific headers).
* The common AOP magic , advisors,authority and session handling, dictonary classes, shared objects and annotations are defined under this module. 
* This module is an injected dependency of all other modules in the project.
* The common integration interface that is implemented by respective channels are also defined in this module.
* Provides PublishEvent annotation support for publishing events to a specific store
* Provides SubscribeEvent annotation support for receiving events based on a event name and channel name for provided store.

#### Publishing an Event
Mi-Dialogue allows to publish an event to the channel using following methods:
1. @PublishEvent annotation
2. Using the dialogueEventBus service

**@PublishEvent annotation**
This is the most common method of publishing an event. Here a method will be annotated with @PublishEvent annotation. We can pass the following params:
* eventStore : RABBIT, REDIS or KAFA. Derives value from EventStore enum
* eventName  : Optional event name if we want to publish only to a event specific stream
* channelName: The name of channel. This can be the name for the channel holding or passing the events ( Used in building the queue names)
* publishType: The channel publish behaviour. Supports BROADCAST or EVENT_SPECIFIC
* isSetAuthority: Used to specify if we need to encode the authentication information in the event object. This requires DialogueAuthority bean to be defined

Example Usage:
``` java
@Service
public class CustomerService {
    @PublishEvent(eventStore=EventStore.RABBITMQ, channelName="com.microideation.customers")
    public Customer createCustomer(String mobile, String name) {
        Customer customer = new Customer(mobile,name);
        customer = customerRepository.save(customer);
        return customer;
    }
}
```
This would convert the Customer object into JSON payload and create a DialogueEvent object and pass to the provided channel.

> Please note that the channel is only created when there is a subscriber for the channel. 

**DialogueEventBus service**
In this method, the dialogueEventBus service can be autowired and varios methods for publish can be used to trigger an event publication.

Example Usage:
``` java
@Service
public class CustomerService {

    private final DialogueEventBus dialogueEventBus;

    public CustomerService(DialogueEventBus dialogueEventBus) {
        this.dialogueEventBus = dialogueEventBus;
    }

    public Customer createCustomer(String mobile, String name) {
        Customer customer = new Customer(mobile,name);
        customer = customerRepository.save(customer);

        // Post the event using dialogueEventBus
        dialogueEventBus.publish("com.microideation.customers",EventStore.RABBITMQ,customer);
        return customer;
    }
}
```
The event is published explicitly when we invoke the publish method on the dialogueEventBus service. There are different variations of publish method available which can be used based on the level of control required.

#### Subscribing to an event
An event can be subscribed by using the @SubscribeEvent annotation. For using this annotation, the subscribing class need be annotated with @DialogueEventListener 

Exaample Usage:
``` java
@DialogueEventListener
public class CustomerEventListener {

    @SubscribeEvent(eventStore=EventStore.RABBITMQ,channelName="com.microideation.customers")
    public void receiveCustomerEvents(DialogueEvent event) {

        Customer customer = event.getPayload(Customer.class);

    }
}
```
This would register the subscriber based on the event-store selected and the channel. Once a matching event is received, it will trigger the annotated method and pass the DialogueEvent object recevied.
> The SubscribeEvent requires an ObjectMapper bean defined in the spring context and this will be automatically set to the DialogueEvent object when event is received. 
There is a also an overloaded method for getPayload where you can pass your own ObjectMapper instance for serializing the payload.

### mi-dialogue-rabbit
Provides the implementation logic for the event transmission through rabbitmq as a channel.
* Implements the Integration interface and provides functionality w.r.t RabbitMQ
* Auto declaration of queue and exchanges based on the PublishEvent annotation
* Generation and registering of listener for the methods annotated with SubscribeEvent 
* Dead letter queue binding
* Backoff policy definition
* Routing based on event names as routing keys
* Consumer configuration based on annotation params
* Requeue of event to the end of queue on repeated failure to deliver avoiding blocking by a single entry in the queue.

#### Conventions
1. Queues are created with the name as : 
channel name (annotation value ) + "-sub-"+ service name ( spring.application.name ) + The annotated method class name in lowercase+method name in lower case.
2. Exchange name is defined as the channel name passed in the annotation
3. Creates a FanoutExchange if the event publish type is BROADCAST ( which is by default ). If the publish type is EVENT_SPECIFIC, then a DirectExchange is created with the event name as the routing key.

### mi-dialogue-redis
This module provides the implementation of the event transmission through redis as a channel.
* Implements the Integration interface and provides functionality w.r.t Redis
* The key for the channel is generated as  channel name + method name of the annotated class method
* Creates a ChannelTopic in Redis based on the channel name
* Events based on event names are not supported as Redis ChannelTopic does not support routing keys 
* Only broadcast publish is supported in redis integration

### mi-dialogue-kafka
This module provides the event communication over the Kafka cluster.
* Implements the Integration interface and provides functionality w.r.t Kafka
* Provides definition for the ProducerFactory and ConsumerFactory based on the serializers.
* Defines a DirectChannel using the channel name 
* Associates the channel subscribe method to invoke the annotated class method

## Passing authentication information in events
Most of the event transfer mechanisms are stateless and does not carry authority or the authentication. Mi-Dialogue allows to configure events to be enriched with authentication from source and then use the same authentication and authority at the subscriber. 

### Configuring authentication 
We need to configure a bean on the publisher side to have the authentication and principal to be passed in the event.

Define a bean of DialogueAuthorityManager  in a configuration class as below:

``` java
@Configuration
@Slf4j
public class DialogueAuthorityConfig {

    @Primary
    @Bean
    public DialogueAuthorityManager dialogueAuthorityManager() {

        return new DialogueAuthorityManagerImpl();
    }

    /**
     * Class implementing the DialogueAuthorityManager
     */
    private class DialogueAuthorityManagerImpl implements DialogueAuthorityManager {

        @Override
        public EventAuthority getEventAuthority() {

            // Overide the method and build the eventAuthority 
            // with the information required by your application.

            // Create the EventAuthority object
            EventAuthority eventAuthority = new EventAuthority();

            // Set the fields 
            eventAuthority.setPrincipal(yourSessionObj.getUsername());

            // Set the extra params
            eventAuthority.setExtraParam("ownerRef",yourSessionObj.getOwnerRef());
            eventAuthority.setExtraParam("userNo",yourSessionObj.getUserNo());
            eventAuthority.setExtraParam("userType",yourSessionObj.getUserType());

            // Return the eventAuthority object
            return eventAuthority;
        }

        @Override
        public void setEventAuthorityAuthContext(EventAuthority eventAuthority) {

            // Override this method and implement the logic to set the authentication  / security context from the eventAuthority received.
            // This method is called when the subscriber wants to set authentication when an event with authority is received.

            /** Sample setup **/    
            // Create the authentication using eventAuthority
            Authentication auth = new UsernamePasswordAuthenticationToken(eventAuthority, "", Arrays.asList(new SimpleGrantedAuthority("EVENT_AUTHORITY")));

            // Set the authentication
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}

```

In the above snippet, DialogueAuthorityManagerImpl provides implementation for the DialogueAuthorityManager by overriding the getEventAuthority() and setEventAuthorityAuthContext() methods.

When an event is being published, Mi-Dialogue will look for the bean of type DialogueAuthorityManager and if found, will call the getEventAuthority() to get the EventAuthority object. If a valid EventAuthority is returned, it will be added to the event while publishing.

Similarly, on the Subscriber side, we need to make sure that the isSetAuthentication field is set to true in the annotation. This will allow the subscriber to look for the eventAuthority object in the recepient event and then call the setEventAuthorityAuthContext of the DialogueAuthorityManager bean for setting the authentication based on your implementation. 
