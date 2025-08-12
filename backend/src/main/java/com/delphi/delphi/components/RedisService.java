package com.delphi.delphi.components;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Store a value with a key
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // Store a value with a key and expiration time
    public void setWithExpiration(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // Set the TTL for a given key
    public void expireKey(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    // Set a date for the key to expire at
    public void expireAt(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    // Retrieve a value by key
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // Delete a key
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // Check if a key exists
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /* List Operations */

    // Add an element to the end of a list
    public Long rightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    // Add an element to the beginning of a list
    public Long leftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    // Get all elements in a list
    public List<Object> getAllListElements(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    // Get elements in a range (inclusive)
    public List<Object> getListRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // Get the length of a list
    public Long getListSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // Remove and get the first element of a list
    public Object leftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    // Remove and get the last element of a list
    public Object rightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    // Get element at specific index
    public Object getListElement(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    // Remove elements from list
    public Long removeFromList(String key, Object value) {
        return redisTemplate.opsForList().remove(key, 0, value);
    }

    /* Transaction Operations */

    // Increment a key and return the new value
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    // Increment a key by a specific amount
    public Long incrementBy(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // Execute operations in a transaction (Redis MULTI/EXEC)
    public void executeInTransaction(Runnable operations) {
        redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            connection.multi();
            operations.run();
            connection.exec();
            return null;
        });
    }

    // Get the current value as Long (useful for counters)
    public Long getLong(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // Atomic rate limiting operation - implements the exact pseudocode MULTI/INCR/EXPIRE/EXEC
    public Long incrementAndExpire(String key, long expireSeconds) {
        return redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Long>) connection -> {
            // Start transaction
            connection.multi();
            // Queue INCR command
            connection.stringCommands().incr(key.getBytes());
            // Queue EXPIRE command
            connection.keyCommands().expire(key.getBytes(), expireSeconds);
            // Execute transaction
            java.util.List<Object> results = connection.exec();
            
            // Return the result of INCR (first command in the transaction)
            if (results != null && !results.isEmpty() && results.get(0) instanceof Long) {
                return (Long) results.get(0);
            }
            return 1L; // Default to 1 if something went wrong
        });
    }

    // User-specific caching methods
    // public void cacheUser(String email, Object user) {
    //     String key = "user:" + email;
    //     setWithExpiration(key, user, 30, TimeUnit.MINUTES);
    // }

    // public Object getCachedUser(String email) {
    //     String key = "user:" + email;
    //     return get(key);
    // }

    // public void removeCachedUser(String email) {
    //     String key = "user:" + email;
    //     delete(key);
    // }

    // // Clear all user cache entries
    // public void clearUserCache() {
    //     // Note: This is a simple implementation. In production, you might want to use Redis SCAN
    //     // to find and delete all user:* keys more efficiently
    //     redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
    //         Set<byte[]> keys = connection.keyCommands().keys("user:*".getBytes());
    //         if (keys != null && !keys.isEmpty()) {
    //             connection.keyCommands().del(keys.toArray(new byte[0][0]));
    //         }
    //         return null;
    //     });
    // }

    /**
     * Evict user candidates cache for a specific userId.
     * This method finds and deletes all Redis keys that match the pattern "cache:user_candidates:{userId}*"
     * 
     * @param userId The user ID for which to evict candidate cache entries
     */
    public void evictCache(String pattern) {
        //String pattern = "cache:user_candidates:" + userId + "*";
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            Set<byte[]> keys = connection.keyCommands().keys(pattern.getBytes());
            if (keys != null && !keys.isEmpty()) {
                connection.keyCommands().del(keys.toArray(new byte[0][0]));
            }
            return null;
        });
    }
} 