package com.dinesh.springbootradieslistner;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
public class AlertController {

    StringRedisTemplate redisTemplate;
    ObjectMapper objectMapper;

    public AlertController(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/alert")
    public void sendAlert(@RequestBody Alert alert) {
        System.out.println("Publishing test alert to 'system-alerts'...");

        String json = objectMapper.writeValueAsString(alert);
        redisTemplate.convertAndSend("system-alerts", json);
    }
}
