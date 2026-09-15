# Spring Boot Redis Listener

A Spring Boot application demonstrating **Redis Pub/Sub message consumption** using **Spring Data Redis**.

The project demonstrates **both the traditional Redis listener configuration and the newer annotation-based `@RedisListener` approach** available in the latest Spring Data Redis.

The main purpose of this project is to show how Redis message listeners have become simpler to configure with the newer annotation-based programming model.

---

## Overview

This project demonstrates how a Spring Boot application can subscribe to a Redis topic/channel and automatically consume JSON messages as Java objects.

The example uses a system alert:

```text
Alert
 ├── level
 ├── serviceName
 └── message
```

Messages are published to the Redis channel:

```text
system-alerts
```

The application demonstrates two approaches:

### Old / Traditional Approach

```text
Redis
  ↓
RedisMessageListenerContainer
  ↓
MessageListenerAdapter
  ↓
AlertSubscriber.processAlert()
  ↓
Manual JSON conversion
```

### New Annotation-Based Approach

```text
Redis
  ↓
@RedisListener
  ↓
AlertSubscriber.processAlert(Alert alert)
  ↓
Automatic JSON conversion
```

---

# Technologies

* Java 21
* Spring Boot 4.1.0
* Spring Data Redis
* Redis
* Docker
* Docker Compose
* Maven

---

# Project Structure

```text
SpringBootRedisListener
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── dinesh
│   │   │           └── springbootradieslistner
│   │   │               ├── Alert.java
│   │   │               ├── AlertSubscriber.java
│   │   │               └── RedisAlertConfig.java
│   │   │
│   │   └── resources
│   │       └── application.properties
│   │
├── compose.yaml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

# Redis Pub/Sub

Redis Pub/Sub follows the Publisher/Subscriber pattern.

A publisher sends a message to a Redis channel:

```text
Publisher
    |
    | PUBLISH
    ↓
system-alerts
    |
    | SUBSCRIBE
    ↓
Spring Boot Application
    |
    ↓
AlertSubscriber
```

The subscriber does not need to continuously poll Redis.

Redis pushes published messages to subscribed consumers.

---

# Alert Message

The application uses an `Alert` object to represent the message.

Conceptually:

```java
public record Alert(
        String level,
        String serviceName,
        String message
) {
}
```

Example JSON:

```json
{
  "level": "CRITICAL",
  "serviceName": "payment-service",
  "message": "Payment service is unavailable"
}
```

This message is published to:

```text
system-alerts
```

---

# Traditional Redis Listener

The traditional Spring Data Redis approach requires multiple configuration components.

The application creates:

1. `MessageListenerAdapter`
2. `RedisMessageListenerContainer`
3. `ChannelTopic`

Example:

```java
@Configuration
public class RedisAlertConfig {

    @Bean
    public MessageListenerAdapter alertListenerAdapter(
            AlertSubscriber subscriber) {

        return new MessageListenerAdapter(
                subscriber,
                "processAlert"
        );
    }

    @Bean
    public RedisMessageListenerContainer alertContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter alertListenerAdapter) {

        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(
                alertListenerAdapter,
                new ChannelTopic("system-alerts")
        );

        return container;
    }
}
```

The listener method traditionally receives the raw message:

```java
public void processAlert(String rawMessage) {

    try {
        Alert alert =
                objectMapper.readValue(rawMessage, Alert.class);

        System.out.println(
                alert.level()
                + " ALERT from "
                + alert.serviceName()
                + ": "
                + alert.message()
        );

    } catch (JacksonException e) {
        System.err.println("Failed to parse alert");
    }
}
```

### Problems with the traditional approach

There is more configuration and boilerplate:

```text
MessageListenerAdapter
        +
RedisMessageListenerContainer
        +
ChannelTopic
        +
Method name
        +
ObjectMapper
        +
Manual JSON conversion
```

The application developer needs to explicitly configure how the message is connected to the listener method.

---

# New `@RedisListener` Approach

The newer approach allows the Redis topic/channel to be declared directly on the listener method.

Example:

```java
@Service
public class AlertSubscriber {

    @RedisListener(topic = "system-alerts")
    public void processAlert(Alert alert) {

        System.out.println(
                alert.level()
                + " ALERT from "
                + alert.serviceName()
                + ": "
                + alert.message()
        );
    }
}
```

The topic is now directly associated with the method:

```java
@RedisListener(topic = "system-alerts")
```

There is no need to explicitly create:

```java
MessageListenerAdapter
```

or:

```java
RedisMessageListenerContainer
```

in the application configuration for this listener.

---

# Automatic JSON Conversion

One of the major differences demonstrated by this project is message conversion.

### Traditional approach

The listener receives a raw message:

```java
public void processAlert(String rawMessage)
```

The developer needs to manually convert JSON:

```java
Alert alert =
        objectMapper.readValue(rawMessage, Alert.class);
```

### New approach

The listener can directly receive the Java object:

```java
public void processAlert(Alert alert)
```

The framework handles the conversion.

Therefore:

```text
Redis JSON
    ↓
Spring Data Redis
    ↓
Message Conversion
    ↓
Alert
    ↓
processAlert(Alert alert)
```

This significantly reduces application-level boilerplate.

---

# Old vs New

| Feature                         | Traditional Approach      | New Approach                        |
| ------------------------------- | ------------------------- | ----------------------------------- |
| Listener configuration          | Explicit                  | Annotation-based                    |
| `RedisMessageListenerContainer` | Required explicitly       | Framework manages it                |
| `MessageListenerAdapter`        | Required explicitly       | Not required                        |
| Topic configuration             | `ChannelTopic`            | `@RedisListener(topic = "...")`     |
| Listener method                 | Raw message commonly used | Java object can be used             |
| JSON conversion                 | Manual                    | Automatic                           |
| ObjectMapper code               | Application code          | Framework                           |
| Configuration                   | More verbose              | Much simpler                        |
| Readability                     | More infrastructure code  | Listener intent is visible directly |

---

# Complete New Listener

The new implementation can be as simple as:

```java
package com.dinesh.springbootradieslistner;

import org.springframework.data.redis.annotation.RedisListener;
import org.springframework.stereotype.Service;

@Service
public class AlertSubscriber {

    @RedisListener(topic = "system-alerts")
    public void processAlert(Alert alert) {

        System.out.println(
                alert.level()
                + " ALERT from "
                + alert.serviceName()
                + ": "
                + alert.message()
        );
    }
}
```

The important part is:

```java
@RedisListener(topic = "system-alerts")
```

and:

```java
public void processAlert(Alert alert)
```

---

# Configuration Comparison

## Old Approach

```text
RedisAlertConfig
       |
       +-- MessageListenerAdapter
       |
       +-- RedisMessageListenerContainer
       |
       +-- ChannelTopic
       |
       +-- AlertSubscriber.processAlert()
```

## New Approach

```text
AlertSubscriber
       |
       +-- @RedisListener
               |
               +-- system-alerts
               |
               +-- processAlert(Alert)
```

The new approach moves the listener configuration closer to the code that actually handles the message.

---

# Running Redis with Docker

The project uses Docker Compose to run Redis.

Start Redis:

```bash
docker compose up -d
```

Check running containers:

```bash
docker ps
```

Stop Redis:

```bash
docker compose down
```

View logs:

```bash
docker compose logs -f
```

---

# Verify Redis

Connect to Redis:

```bash
docker exec -it <redis-container-name> redis-cli
```

Test the Redis server:

```text
PING
```

Expected:

```text
PONG
```

---

# Publish an Alert

After starting the Spring Boot application, connect to Redis:

```bash
docker exec -it <redis-container-name> redis-cli
```

Publish an alert:

```text
PUBLISH system-alerts '{"level":"CRITICAL","serviceName":"payment-service","message":"Payment service is unavailable"}'
```

Redis publishes the message to:

```text
system-alerts
```

The Spring Boot application receives it through:

```java
@RedisListener(topic = "system-alerts")
```

The listener receives the converted `Alert` object.

Expected application output:

```text
CRITICAL ALERT from payment-service: Payment service is unavailable
```

---

# Message Flow

The complete flow is:

```text
             Redis Publisher
                    |
                    |
                    | PUBLISH
                    ↓
          ┌───────────────────┐
          │   system-alerts   │
          │   Redis Channel   │
          └─────────┬─────────┘
                    |
                    | Message
                    ↓
          ┌───────────────────┐
          │  Spring Data      │
          │      Redis        │
          └─────────┬─────────┘
                    |
                    | JSON → Alert
                    ↓
          ┌───────────────────┐
          │ @RedisListener    │
          │                   │
          │ processAlert()    │
          └─────────┬─────────┘
                    |
                    ↓
              Alert Object
```

---

# Why Use the New Approach?

The annotation-based approach provides a cleaner programming model.

Instead of configuring infrastructure separately:

```java
@Bean
public MessageListenerAdapter ...
```

and:

```java
@Bean
public RedisMessageListenerContainer ...
```

the listener can declare its subscription directly:

```java
@RedisListener(topic = "system-alerts")
```

This makes the code:

* Easier to understand
* Less verbose
* Easier to maintain
* Closer to the business logic
* Less dependent on infrastructure configuration

---

# When to Use the Traditional Approach

The traditional approach can still be useful when you need more explicit control over listener infrastructure.

For example:

* Custom listener container configuration
* Multiple listener containers
* Advanced Redis connection configuration
* Custom message listener adapters
* Custom subscription configuration
* Low-level infrastructure control

The annotation-based approach is preferable when you want a simple and declarative listener.

---

# When to Use `@RedisListener`

Use the annotation-based approach when your application mainly needs:

```text
Subscribe to Redis channel
        ↓
Receive message
        ↓
Convert message
        ↓
Process Java object
```

For example:

* System alerts
* Notifications
* Application events
* Cache invalidation events
* Real-time updates
* Lightweight event broadcasting

---

# Redis Pub/Sub vs Redis Streams

Redis Pub/Sub should not be confused with Redis Streams.

### Pub/Sub

```text
Publisher
    ↓
Redis Channel
    ↓
Subscribers
```

Messages are delivered to currently subscribed consumers.

### Redis Streams

```text
Producer
    ↓
Redis Stream
    ↓
Consumer Group
    ↓
Consumers
```

Redis Streams provide features such as message persistence, consumer groups, acknowledgements, and message replay.

For durable event processing, Redis Streams may be a better choice.

---

# Running the Application

Clone the repository:

```bash
git clone https://github.com/dinesh-veer/SpringBootRadiesListner.git
```

Navigate to the project:

```bash
cd SpringBootRadiesListner
```

Start Redis:

```bash
docker compose up -d
```

Run Spring Boot:

### macOS / Linux

```bash
./mvnw spring-boot:run
```

### Windows

```bash
mvnw.cmd spring-boot:run
```

---

# Build

```bash
./mvnw clean package
```

Run the generated JAR:

```bash
java -jar target/SpringBootRadiesListner-0.0.1-SNAPSHOT.jar
```

---

# Key Spring Data Redis Concepts Demonstrated

This project demonstrates:

* Redis Pub/Sub
* Redis channels
* Spring Data Redis
* Redis message listeners
* `@RedisListener`
* `RedisMessageListenerContainer`
* `MessageListenerAdapter`
* `ChannelTopic`
* JSON message conversion
* Java object message handling
* Docker Redis
* Docker Compose
* Publisher/Subscriber pattern

---

# Learning Path

A good way to understand this project is to study the implementations in this order:

### 1. Redis Pub/Sub

Understand:

```text
PUBLISH
SUBSCRIBE
CHANNEL
```

### 2. Traditional Spring Data Redis

Understand:

```text
RedisMessageListenerContainer
        +
MessageListenerAdapter
        +
ChannelTopic
```

### 3. New Annotation-Based Listener

Understand:

```java
@RedisListener(topic = "system-alerts")
```

### 4. Message Conversion

Understand how:

```text
JSON
 ↓
Alert
```

can be handled by the framework.

### 5. Compare Both Approaches

The key lesson of this project is:

```text
Traditional
More explicit configuration
        ↓
New
Declarative @RedisListener
```

---

# Repository

GitHub:

https://github.com/dinesh-veer/SpringBootRedisListener

# Author

**Dinesh Veer**

GitHub:

https://github.com/dinesh-veer

