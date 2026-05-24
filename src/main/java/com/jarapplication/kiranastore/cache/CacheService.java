package com.jarapplication.kiranastore.cache;

import com.jarapplication.kiranastore.redis.RedisStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * CACHE SERVICE: Application-Level Cache Abstraction (Extends RedisStorageService)
 *
 * WHAT IT DOES:
 * ├─ Inherits ALL Redis operations from RedisStorageService (set, get, delete)
 * ├─ Acts as the application's primary cache service bean
 * └─ Currently adds no extra methods (pure inheritance for Spring bean resolution)
 *
 * WHY IT EXISTS (if it just inherits without overriding?):
 * ├─ @Primary: Resolves Spring bean ambiguity
 * │   ├─ Problem: Both RedisStorageService AND CacheService are @Service beans
 * │   ├─ When another bean auto-wires RedisStorageService → which one to inject?
 * │   ├─ @Primary: Spring injects CacheService (this one) by default
 * │   └─ Without @Primary: "NoUniqueBeanDefinitionException" at startup
 * │
 * ├─ Named bean: @Service("cache") gives this bean the name "cache"
 * │   └─ Can be explicitly injected with @Qualifier("cache")
 * │   └─ Useful when you need to inject SPECIFICALLY this bean (not parent)
 * │
 * ├─ Abstraction layer: Separates "caching concern" from "Redis concern"
 * │   ├─ ConversionServiceImp injects CacheService (caching abstraction)
 * │   ├─ Not RedisStorageService (infrastructure detail)
 * │   └─ If you switch from Redis to Memcached: change CacheService, not consumers
 * │
 * └─ Future extensibility:
 *    ├─ Add cache-specific logic (cache warming, hit/miss metrics)
 *    ├─ Add @CachePut, @Cacheable, @CacheEvict support
 *    └─ Add fallback logic if Redis is unavailable
 *
 * DESIGN PATTERN: TEMPLATE METHOD / STRATEGY via INHERITANCE
 * ├─ Parent: RedisStorageService → provides the "how" (Redis operations)
 * ├─ Child: CacheService → provides the "what" (application-level caching)
 * └─ Could be replaced with: interface-based strategy (more flexible)
 *    └─ interface CacheService { get(), set(), delete() }
 *    └─ class RedisCacheService implements CacheService { ... }
 *    └─ class InMemoryCacheService implements CacheService { ... }
 *
 * SPRING BEAN RESOLUTION ORDER:
 * ├─ autowire by type: RedisStorageService → found 2 beans (parent + child)
 * ├─ @Primary wins: CacheService is injected
 * ├─ @Qualifier("cache") → explicitly selects CacheService bean
 * └─ @Qualifier("redisStorageService") → explicitly selects parent bean
 *
 * @Primary: Makes this the default bean when multiple candidates exist
 * @Service("cache"): Registers as Spring bean with name "cache"
 */
@Primary        // ← DEFAULT bean when autowiring RedisStorageService type
@Service("cache") // ← Bean name = "cache" (injectable via @Qualifier("cache"))
public class CacheService extends RedisStorageService {

    @Autowired
    public CacheService(RedisTemplate<String, String> redisKVTemplate) {
        super(redisKVTemplate); // ← Pass RedisTemplate to parent constructor
    }
    // Inherits from RedisStorageService:
    // ├─ setValueToRedis(key, value, ttl) → Redis SET with TTL
    // ├─ getValueFromRedis(key) → Redis GET
    // └─ deleteCachedData(key) → Redis DEL
}
