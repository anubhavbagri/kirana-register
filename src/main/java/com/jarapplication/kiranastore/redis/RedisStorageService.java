package com.jarapplication.kiranastore.redis;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisStorageService {

    private final RedisTemplate<String, String> redisKVTemplate;

    public RedisStorageService(RedisTemplate<String, String> redisKVTemplate) {
        this.redisKVTemplate = redisKVTemplate;
    }

    /**
     * Stores the key values pair for Time to live duration
     *
     * @param key
     * @param value
     * @param ttl
     */
    public void setValueToRedis(String key, String value, long ttl) {
        redisKVTemplate.opsForValue().set(key, value, ttl, TimeUnit.MILLISECONDS);
    }

    /**
     * Retrieves the value for a key
     *
     * @param key
     * @return
     */
    public String getValueFromRedis(String key) {
        return redisKVTemplate.opsForValue().get(key);
    }

    /**
     * Deletes the data based on key
     *
     * @param key
     */
    public void deleteCachedData(String key) {
        redisKVTemplate.delete(key);
    }
}
