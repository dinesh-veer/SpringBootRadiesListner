package com.dinesh.springbootradieslistner;

import org.springframework.data.redis.annotation.RedisListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class AlertSubscriber {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * This is old way to consume message in Topic.
     */
     //The method name MUST match the string in the configuration above
//    public void processAlert(String rawMessage) {
//        try {
//            // Manually parse the JSON string back into an Alert object
//            Alert alert = objectMapper.readValue(rawMessage, Alert.class);
//            System.out.println(alert.level() + " ALERT from " + alert.serviceName() + ": " + alert.message());
//        } catch (JacksonException e) {
//            System.err.println("Failed to parse alert");
//        }
//    }

    // The topic is now bound directly on the method using the annotation
    @RedisListener(topic = "system-alerts")
    public void processAlert(Alert alert) {
        // The framework automatically parses the JSON into your Alert record!
        System.out.println(alert.level() + " ALERT from " + alert.serviceName() + ": " + alert.message());
    }
}