package com.jarapplication.kiranastore.redis;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * REDIS STORAGE SERVICE: Low-Level Redis Key-Value Operations
 *
 * WHAT IT DOES:
 * ├─ Provides basic Redis operations: SET (with TTL), GET, DELETE
 * ├─ Wraps Spring Data Redis's RedisTemplate for type-safe operations
 * └─ Acts as the base class for CacheService (inheritance)
 *
 * WHY IT'S NEEDED:
 * ├─ Abstraction: Hides RedisTemplate complexity behind simple methods
 * ├─ TTL support: setValueToRedis() stores values with automatic expiration
 * │   └─ After TTL expires → key is auto-deleted by Redis (no manual cleanup)
 * ├─ Reusable: Any feature needing Redis caching can use this service
 * └─ Inheritance: CacheService extends this for specific caching use cases
 *
 * REDIS TEMPLATE:
 * ├─ RedisTemplate<String, String>: Type parameters define key/value types
 * │   ├─ First String: Keys are always Strings (e.g., "USD_INR")
 * │   └─ Second String: Values are Strings (e.g., "0.012")
 * ├─ opsForValue(): Returns ValueOperations<K,V> for simple key-value ops
 * │   ├─ .set(key, value, timeout, unit): SET with TTL
 * │   ├─ .get(key): GET → returns value or null
 * │   └─ Internally: Uses Redis SET and GET commands
 * ├─ .delete(key): Removes key-value pair from Redis
 * └─ Auto-configured by Spring Boot when spring-data-redis is on classpath
 *    └─ Connection details from application.properties: spring.redis.host, spring.redis.port
 *
 * TTL (Time-To-Live):
 * ├─ Redis auto-deletes keys after TTL expires
 * ├─ TimeUnit.MILLISECONDS: TTL specified in milliseconds
 * ├─ Example: set("USD_INR", "0.012", 30000, MILLISECONDS) → expires in 30 seconds
 * ├─ Used for: Exchange rate cache (ConversionServiceImp)
 * │   └─ TTL = milliseconds until end of current minute (see DateUtil.getEndOfMinute())
 * └─ Why TTL? → Fresh exchange rates without manual cache invalidation
 *
 * ARCHITECTURE:
 * ├─ RedisStorageService (THIS CLASS): Generic Redis operations
 * │   └─ CacheService extends this (adds @Primary, named "cache")
 * │       └─ ConversionServiceImp uses CacheService for exchange rate caching
 *
 * @Service: Registers as Spring bean (auto-discovered by @ComponentScan)
 */
@Service // ← Spring bean for Redis operations
public class RedisStorageService {

    // Spring Data Redis template → auto-configured from application.properties
    // Handles: Connection management, serialization, connection pooling
    private final RedisTemplate<String, String> redisKVTemplate;

    // Constructor injection: Spring auto-injects the RedisTemplate bean
    public RedisStorageService(RedisTemplate<String, String> redisKVTemplate) {
        this.redisKVTemplate = redisKVTemplate;
    }

    /**
     * Stores a key-value pair in Redis with automatic expiration.
     *
     * Redis command equivalent: SET key value PX ttl
     * Example: SET "USD_INR" "0.012" PX 30000  → expires in 30 seconds
     *
     * @param key   ← Redis key (e.g., "USD_INR")
     * @param value ← Value to store (e.g., "0.012")
     * @param ttl   ← Time-to-live in milliseconds (auto-deletes after this)
     */
    public void setValueToRedis(String key, String value, long ttl) {
        redisKVTemplate.opsForValue().set(key, value, ttl, TimeUnit.MILLISECONDS);
    }

    /**
     * Retrieves the value for a given key from Redis.
     *
     * Redis command equivalent: GET key
     *
     * @param key ← Redis key to look up
     * @return Value string if key exists and hasn't expired, null otherwise
     */
    public String getValueFromRedis(String key) {
        return redisKVTemplate.opsForValue().get(key);
    }

    /**
     * Deletes a key-value pair from Redis (manual cache invalidation).
     *
     * Redis command equivalent: DEL key
     * Typically not needed if TTL is set (keys auto-expire).
     * Useful for: Immediate cache invalidation when data changes.
     *
     * @param key ← Redis key to delete
     */
    public void deleteCachedData(String key) {
        redisKVTemplate.delete(key);
    }
}
