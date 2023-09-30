package com.concurrency.sql.global;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void setValue(String key, String data) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    public Optional<String> getValues(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return Optional.ofNullable(values.get(key));
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    // Lock기능 활용 (SpinLock)
    public boolean lock(String key) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(key+"_lock", "lock", Duration.ofMillis(10000));
    }

    public void deleteLock(String key) {
        redisTemplate.delete(key+"_lock");
    }
}