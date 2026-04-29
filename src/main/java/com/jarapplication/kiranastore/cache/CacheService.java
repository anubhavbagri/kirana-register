package com.jarapplication.kiranastore.cache;

import com.jarapplication.kiranastore.redis.RedisStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Primary
@Service("cache")
public class CacheService extends RedisStorageService {

    @Autowired
    public CacheService(RedisTemplate<String, String> redisKVTemplate) {
        super(redisKVTemplate);
    }
}
