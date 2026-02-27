package com.forgequeue.core.service;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisHealthService {

    private final StringRedisTemplate redisTemplate;

    public RedisHealthService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void verifyConnection() {
        try {
            redisTemplate.opsForValue().set("forgequeue:healthcheck", "ok");
            String value = redisTemplate.opsForValue().get("forgequeue:healthcheck");

            if (!"ok".equals(value)) {
                throw new IllegalStateException("Redis health check failed");
            }

        } catch (Exception e) {
            throw new IllegalStateException("Unable to connect to Redis", e);
        }
    }
}