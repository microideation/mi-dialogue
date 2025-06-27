# Mi-Dialogue

Mi-Dialogue is a set of Java Spring Boot-based libraries that provides a simple, unified, channel-agnostic microservices event communication mechanism over different channels. Following are the main features supported:

1. Event-based communication over Redis, Kafka, and RabbitMQ
2. Domain change replications on different services
3. Authentication carry-forward with events
4. Event subscription failure handling with backoff-policy

## Latest packages
- [mi-dialogue-core : 3.2.3](https://cloudsmith.io/~microideation/repos/mi-dialogue/packages/detail/maven/mi-dialogue-core/3.2.3/a=noarch;xg=com.microideation.app.dialogue/)
- [mi-dialogue-rabbit : 3.2.3](https://cloudsmith.io/~microideation/repos/mi-dialogue/packages/detail/maven/mi-dialogue-rabbit/3.2.3/a=noarch;xg=com.microideation.app.dialogue/)
- [mi-dialogue-domain : 3.2.3](https://cloudsmith.io/~microideation/repos/mi-dialogue/packages/detail/maven/mi-dialogue-domain/3.2.3/a=noarch;xg=com.microideation.app.dialogue/)
- [mi-dialogue-redis : 3.2.3](https://cloudsmith.io/~microideation/repos/mi-dialogue/packages/detail/maven/mi-dialogue-redis/3.2.3/a=noarch;xg=com.microideation.app.dialogue/)
- [mi-dialogue-kafka : 3.2.3](https://cloudsmith.io/~microideation/repos/mi-dialogue/packages/detail/maven/mi-dialogue-kafka/3.2.3/a=noarch;xg=com.microideation.app.dialogue/)
- [mi-dialogue-rsocket : 3.2.3](https://cloudsmith.io/~microideation/repos/mi-dialogue/packages/detail/maven/mi-dialogue-rsocket/3.2.3/a=noarch;xg=com.microideation.app.dialogue/)

See the slideshare link for basic introduction:

[![Introduction to Mi-dialogue](https://blog.microideation.com/wp-content/uploads/2019/06/mi-dialogue-slideshare-intro.png)](https://www.slideshare.net/MicroideationIdeatio/mi-dialogueintrov11)

Check out the below video for a live session of multi-channel event communication:

[![Demo of different channels](http://img.youtube.com/vi/rP2YZSZczAs/0.jpg)](http://www.youtube.com/watch?v=rP2YZSZczAs)

## Table of Contents

- [Mi-Dialogue](#Mi-Dialogue)
  - [Table of Contents](#Table-of-Contents)
  - [Why Mi-Dialogue?](#Why-Mi-Dialogue)
  - [Modules in Mi-Dialogue](#Modules-in-Mi-Dialogue)
    - [mi-dialogue-core](#mi-dialogue-core)
      - [Publishing an Event](#Publishing-an-Event)
      - [Subscribing to an Event](#Subscribing-to-an-Event)
    - [mi-dialogue-rabbit](#mi-dialogue-rabbit)
      - [Conventions](#Conventions)
    - [mi-dialogue-redis](#mi-dialogue-redis)
    - [mi-dialogue-kafka](#mi-dialogue-kafka)
    - [mi-dialogue-rsocket](#mi-dialogue-rsocket)
  - [Passing Authentication Information in Events](#Passing-authentication-information-in-events)
    - [Configuring Authentication](#Configuring-authentication)

## Why Mi-Dialogue?
Mi-Dialogue was created out of the requirement for event-based communication without the hassles of going through the store implementation or the Spring Integration process.

## Modules in Mi-Dialogue
Mi-Dialogue has the following modules and can be selectively enabled in a project based on the requirements. The mi-dialogue-core is a common module for all the modules.

1. mi-dialogue-core
2. mi-dialogue-rabbit
3. mi-dialogue-redis
4. mi-dialogue-kafka
5. mi-dialogue-domain

### mi-dialogue-core
Provides the basic and common support functionality to the other modules in the library.

* Declares the DialogueEvent object which holds the payload and the header information for the event (authority, any other user-specific headers).
* The common AOP magic, advisors, authority and session handling, dictionary classes, shared objects and annotations are defined under this module.
* This module is an injected dependency of all other modules in the project.
* The common integration interface that is implemented by respective channels is also defined in this module.
* Provides PublishEvent annotation support for publishing events to a specific store
* Provides SubscribeEvent annotation support for receiving events based on an event name and channel name for the provided store.

#### Publishing an Event
Mi-Dialogue allows publishing an event to the channel using the following methods:
1. @PublishEvent annotation
2. Using the dialogueEventBus service

**@PublishEvent annotation**
This is the most common method of publishing an event. Here a method will be annotated with @PublishEvent annotation. We can pass the following params:
* eventStore: RABBIT, REDIS, or KAFKA. Derives value from EventStore enum
* eventName: Optional event name if we want to publish only to an event-specific stream
* channelName: The name of the channel. This can be the name for the channel holding or passing the events (Used in building the queue names)
* publishType: The channel publish behavior. Supports BROADCAST or EVENT_SPECIFIC
* isSetAuthority: Used to specify if we need to encode the authentication information in the event object. This requires DialogueAuthority bean to be defined

Example Usage:
```java
@Service
public class CustomerService {
    @PublishEvent(eventStore=EventStore.RABBITMQ, channelName="com.microideation.customers")
    public Customer createCustomer(String mobile, String name) {
        Customer customer = new Customer(mobile, name);
        customer = customerRepository.save(customer);
        return customer;
    }
}
```
This would convert the Customer object into JSON payload and create a DialogueEvent object and pass it to the provided channel.

> Please note that the channel is only created when there is a subscriber for the channel.

**DialogueEventBus service**
In this method, the dialogueEventBus service can be autowired and various methods for publish can be used to trigger an event publication.

Example Usage:
```java
@Service
public class CustomerService {

    private final DialogueEventBus dialogueEventBus;

    public CustomerService(DialogueEventBus dialogueEventBus) {
        this.dialogueEventBus = dialogueEventBus;
    }

    public Customer createCustomer(String mobile, String name) {
        Customer customer = new Customer(mobile, name);
        customer = customerRepository.save(customer);

        // Post the event using dialogueEventBus
        dialogueEventBus.publish("com.microideation.customers", EventStore.RABBITMQ, customer);
        return customer;
    }
}
```
The event is published explicitly when we invoke the publish method on the dialogueEventBus service. There are different variations of publish method available which can be used based on the level of control required.

#### Subscribing to an Event
An event can be subscribed to by using the @SubscribeEvent annotation. For using this annotation, the subscribing class needs to be annotated with @DialogueEventListener.

Example Usage:
```java
@DialogueEventListener
public class CustomerEventListener {

    @SubscribeEvent(eventStore=EventStore.RABBITMQ, channelName="com.microideation.customers")
    public void receiveCustomerEvents(DialogueEvent event) {
        Customer customer = event.getPayload(Customer.class);
    }
}
```
This would register the subscriber based on the event-store selected and the channel. Once a matching event is received, it will trigger the annotated method and pass the DialogueEvent object received.
> The SubscribeEvent requires an ObjectMapper bean defined in the Spring context and this will be automatically set to the DialogueEvent object when the event is received.
There is also an overloaded method for getPayload where you can pass your own ObjectMapper instance for serializing the payload.

### mi-dialogue-rabbit
Provides the implementation logic for the event transmission through RabbitMQ as a channel.
* Implements the Integration interface and provides functionality w.r.t RabbitMQ
* Auto declaration of queue and exchanges based on the PublishEvent annotation
* Generation and registering of listener for the methods annotated with SubscribeEvent
* Dead letter queue binding
* Backoff policy definition
* Routing based on event names as routing keys
* Consumer configuration based on annotation params
* Requeue of event to the end of queue on repeated failure to deliver avoiding blocking by a single entry in the queue.
* Event store indicated using EventStore.RABBITMQ

#### Conventions
1. Queues are created with the name as:
   channel name (annotation value) + "-sub-" + service name (spring.application.name) + the annotated method class name in lowercase + method name in lowercase.
2. Exchange name is defined as the channel name passed in the annotation
3. Creates a FanoutExchange if the event publish type is BROADCAST (which is by default). If the publish type is EVENT_SPECIFIC, then a DirectExchange is created with the event name as the routing key.

### mi-dialogue-redis
This module provides the implementation of the event transmission through Redis as a channel.
* Implements the Integration interface and provides functionality w.r.t Redis
* The key for the channel is generated as channel name + method name of the annotated class method
* Creates a ChannelTopic in Redis based on the channel name
* Events based on event names are not supported as Redis ChannelTopic does not support routing keys
* Only broadcast publish is supported in Redis integration
* Event store indicated using EventStore.REDIS

#### IMPORTANT:
* Redis uses fire-and-forget method, there is no storage of events anywhere. If there are no subscribers, it will be lost.
* You cannot have duplicate subscribers on the same channel and same method.

### mi-dialogue-kafka
This module provides the event communication over the Kafka cluster.
* Implements the Integration interface and provides functionality w.r.t Kafka
* Provides definition for the ProducerFactory and ConsumerFactory based on the serializers
* Event store indicated using EventStore.KAFKA
* Automatic topic creation with configurable partitions and replication factors
* Per-subscription group ID configuration
* Externalized configuration through application.yml

#### Configuration
The Kafka integration can be configured through `application.yml` or `application.properties`. The following table shows all available configuration options with their default values. These properties can be overridden as and when required for your specific use case.

| Configuration Property | Default Value | Description |
|----------------------|---------------|-------------|
| `kafka.broker.address` | `192.168.0.104:9092` | Kafka broker address |
| `kafka.zookeeper.connect` | `192.168.0.104:2181` | Zookeeper connection string |
| `kafka.consumer.groupIdConfig` | `mi-dialogue-default-group` | Default consumer group ID |
| `kafka.consumer.enableAutoCommit` | `true` | Enable auto-commit for consumers |
| `kafka.consumer.autoCommitIntervalMs` | `100` | Auto-commit interval in milliseconds |
| `kafka.consumer.sessionTimeoutMs` | `15000` | Consumer session timeout in milliseconds |
| `kafka.producer.retries` | `0` | Number of retries for failed producer requests |
| `kafka.producer.batchSize` | `16384` | Producer batch size in bytes |
| `kafka.producer.lingerMs` | `1` | Time to wait before sending batches in milliseconds |
| `kafka.producer.bufferMemory` | `33554432` | Total memory for buffering records in bytes |
| `kafka.topic.defaultPartitionCount` | `3` | Default number of partitions for new topics |
| `kafka.topic.defaultReplicationFactor` | `1` | Default replication factor for new topics |

#### Publishing Events
Kafka topics are automatically created when publishing events. The topic configuration is taken from the `@PublishEvent` annotation:

```java
@Service
public class CustomerService {
    @PublishEvent(
        eventStore = EventStore.KAFKA, 
        channelName = "com.microideation.customers",
        partitionCount = 5,
        replicationFactor = 2
    )
    public Customer createCustomer(String mobile, String name) {
        Customer customer = new Customer(mobile, name);
        customer = customerRepository.save(customer);
        return customer;
    }
}
```

#### Subscribing to Events
Subscribers can specify custom group IDs for better control over message consumption:

```java
@DialogueEventListener
public class CustomerEventListener {

    @SubscribeEvent(
        eventStore = EventStore.KAFKA,
        channelName = "com.microideation.customers",
        groupId = "customer-processing-group"
    )
    public void receiveCustomerEvents(DialogueEvent event) {
        Customer customer = event.getPayload(Customer.class);
        // Process customer event
    }

    // Using default group ID (generated from class and method name)
    @SubscribeEvent(
        eventStore = EventStore.KAFKA,
        channelName = "com.microideation.customers"
    )
    public void handleCustomerUpdates(DialogueEvent event) {
        Customer customer = event.getPayload(Customer.class);
        // Process customer updates
    }
}
```

#### Features
* **Automatic Topic Creation**: Topics are created automatically with specified partition count and replication factor
* **Per-Subscription Group IDs**: Each subscription can have its own consumer group ID
* **Default Group ID Generation**: When no group ID is specified, a default is generated based on class and method name
* **Externalized Configuration**: All Kafka settings are configurable through application.yml
* **Topic Caching**: Topic existence is cached to avoid repeated checks
* **Error Handling**: Graceful handling of topic creation errors

#### Conventions
1. Topics are created with the name specified in the `channelName` parameter
2. Group IDs can be specified per subscription using the `groupId` parameter in `@SubscribeEvent`
3. Default group IDs follow the pattern: `{ClassName}-{methodName}-group`
4. Topics are created automatically when first accessed (publish or subscribe)
5. Event name specific listening is not supported in Kafka integration

### mi-dialogue-rsocket
This module provides communication of events using RSockets as channels.
* Implements the Integration interface and provides the functionality using RSockets
* Channel name is passed as host:port format. E.g.: 192.168.56.20:9001
* Channel name can be provided using IP or actual hostname
* One host can only consist of one subscriber (as only one server can listen to a given port)
* Event store indicated using EventStore.RSOCKET
* Any number of publishers can publish to the same channel.
* Reconnections at publisher side are done at the time of publish request
* Subscriber side reconnections and connection failures are handled using a job which is run every 5 minutes (Controlled using mi-dialogue.rsocket.subscriber.refresh.interval value. This is in the format of a Quartz cron)

```properties
# For running reconnect every minute
mi-dialogue.rsocket.subscriber.refresh.interval=0 0/1 * * * ?

# For running reconnect every 30 minutes
mi-dialogue.rsocket.subscriber.refresh.interval=0 0/30 * * * ?
```

* Event names are not supported in RSocket-based event communication as of now.

## Passing Authentication Information in Events
Most of the event transfer mechanisms are stateless and do not carry authority or authentication. Mi-Dialogue allows configuring events to be enriched with authentication from source and then use the same authentication and authority at the subscriber.

### Configuring Authentication
We need to configure a bean on the publisher side to have the authentication and principal to be passed in the event.

Define a bean of DialogueAuthorityManager in a configuration class as below:

```java
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
            // Override the method and build the eventAuthority 
            // with the information required by your application.

            // Create the EventAuthority object
            EventAuthority eventAuthority = new EventAuthority();

            // Set the fields 
            eventAuthority.setPrincipal(yourSessionObj.getUsername());

            // Set the extra params
            eventAuthority.setExtraParam("ownerRef", yourSessionObj.getOwnerRef());
            eventAuthority.setExtraParam("userNo", yourSessionObj.getUserNo());
            eventAuthority.setExtraParam("userType", yourSessionObj.getUserType());

            // Return the eventAuthority object
            return eventAuthority;
        }

        @Override
        public void setEventAuthorityAuthContext(EventAuthority eventAuthority) {
            // Override this method and implement the logic to set the authentication / security context from the eventAuthority received.
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

Similarly, on the subscriber side, we need to make sure that the isSetAuthentication field is set to true in the annotation. This will allow the subscriber to look for the eventAuthority object in the recipient event and then call the setEventAuthorityAuthContext of the DialogueAuthorityManager bean for setting the authentication based on your implementation.
