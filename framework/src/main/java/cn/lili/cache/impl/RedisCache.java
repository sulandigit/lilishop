package cn.lili.cache.impl;

import cn.lili.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis cache implementation
 *
 * @author Chopepr
 */
@Slf4j
@Component
public class RedisCache implements Cache {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    public RedisCache() {

    }

    /**
     * Get the cached value by key
     *
     * @param key cache key
     * @return the cached value, or null if not found
     */
    @Override
    public Object get(Object key) {

        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Get the cached value as a string by key
     *
     * @param key cache key
     * @return the cached value as a string, or null if not found or on exception
     */
    @Override
    public String getString(Object key) {
        try {
            return redisTemplate.opsForValue().get(key).toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get multiple cached values in batch
     *
     * @param keys collection of cache keys
     * @return list of values corresponding to the keys, null for missing keys
     */
    @Override
    public List multiGet(Collection keys) {
        return redisTemplate.opsForValue().multiGet(keys);

    }

    /**
     * Set multiple key-value pairs in batch
     *
     * @param map key-value pairs to set
     */
    @Override
    public void multiSet(Map map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    /**
     * Delete multiple cache entries in batch
     *
     * @param keys collection of keys to delete
     */
    @Override
    public void multiDel(Collection keys) {
        redisTemplate.delete(keys);
    }

    /**
     * Set a cache entry with no expiration
     *
     * @param key   cache key
     * @param value cache value
     */
    @Override
    public void put(Object key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Set a cache entry with an expiration time in seconds
     *
     * @param key   cache key
     * @param value cache value
     * @param exp   expiration time in seconds
     */
    @Override
    public void put(Object key, Object value, Long exp) {
        put(key, value, exp, TimeUnit.SECONDS);
    }

    /**
     * Set a cache entry with an expiration time in the specified time unit
     *
     * @param key      cache key
     * @param value    cache value
     * @param exp      expiration time
     * @param timeUnit time unit for expiration
     */
    @Override
    public void put(Object key, Object value, Long exp, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, exp, timeUnit);
    }

    /**
     * Delete a cache entry by key
     *
     * @param key cache key
     * @return true if the key was successfully deleted
     */
    @Override
    public Boolean remove(Object key) {

        return redisTemplate.delete(key);
    }

    /**
     * Delete cache entries matching a key prefix (fuzzy delete)
     *
     * @param key key prefix for fuzzy deletion
     */
    @Override
    public void vagueDel(Object key) {
        List keys = this.keys(key + "*");
        redisTemplate.delete(keys);
    }

    /**
     * Clear all cache entries
     */
    @Override
    public void clear() {
        List keys = this.keys("*");
        redisTemplate.delete(keys);
    }

    /**
     * Put a single field-value pair into a Hash structure
     *
     * @param key       cache key
     * @param hashKey   field name in the Hash
     * @param hashValue field value in the Hash
     */
    @Override
    public void putHash(Object key, Object hashKey, Object hashValue) {
        redisTemplate.opsForHash().put(key, hashKey, hashValue);
    }

    /**
     * Put multiple field-value pairs into a Hash structure
     *
     * @param key cache key
     * @param map field-value pairs to store
     */
    @Override
    public void putAllHash(Object key, Map map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * Get the value of a specific field from a Hash structure
     *
     * @param key     cache key
     * @param hashKey field name in the Hash
     * @return the value of the specified field
     */
    @Override
    public Object getHash(Object key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Get all field-value pairs from a Hash structure
     *
     * @param key cache key
     * @return all field-value pairs in the Hash
     */
    @Override
    public Map<Object, Object> getHash(Object key) {
        return this.redisTemplate.opsForHash().entries(key);
    }

    /**
     * Check whether a key exists in the cache
     *
     * @param key cache key
     * @return true if the key exists, false otherwise
     */
    @Override
    public boolean hasKey(Object key) {
        return this.redisTemplate.opsForValue().get(key) != null;
    }

    /**
     * Get keys matching the given pattern using SCAN (non-blocking)
     *
     * @param pattern match pattern
     * @return list of keys matching the pattern
     */
    @Override
    public List<Object> keys(String pattern) {
        List<Object> keys = new ArrayList<>();
        this.scan(pattern, item -> {
            // key matching the pattern
            String key = new String(item, StandardCharsets.UTF_8);
            keys.add(key);
        });
        return keys;
    }

    /**
     * Get keys matching the given pattern using the KEYS command (blocking).
     * Note: May cause Redis to block with large datasets. Prefer {@link #keys(String)} instead.
     *
     * @param pattern match pattern
     * @return list of keys matching the pattern
     */
    @Override
    public List<Object> keysBlock(String pattern) {
        Set<Object> set = redisTemplate.keys(pattern);
        List<Object> list = new ArrayList<>();
        list.addAll(set);
        return list;
    }

    /**
     * SCAN implementation for iterating over keys matching a pattern
     *
     * @param pattern  match pattern
     * @param consumer callback to process each matched key
     */
    private void scan(String pattern, Consumer<byte[]> consumer) {
        this.redisTemplate.execute((RedisConnection connection) -> {
            try (Cursor<byte[]> cursor =
                         connection.scan(ScanOptions.scanOptions()
                                 .count(Long.MAX_VALUE)
                                 .match(pattern).build())) {
                cursor.forEachRemaining(consumer);
                return null;

            } catch (IOException e) {
                log.error("scan error", e);
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * Add a value to a HyperLogLog for cardinality estimation (uses PFADD command)
     *
     * @param key   cache key
     * @param value value to add
     * @return 1 if at least one internal register was altered, 0 otherwise
     */
    @Override
    public Long cumulative(Object key, Object value) {
        HyperLogLogOperations<Object, Object> operations = redisTemplate.opsForHyperLogLog();
        // PFADD command
        return operations.add(key, value);

    }

    /**
     * Get the estimated cardinality of a HyperLogLog (uses PFCOUNT command)
     *
     * @param key cache key
     * @return estimated cardinality
     */
    @Override
    public Long counter(Object key) {
        HyperLogLogOperations<Object, Object> operations = redisTemplate.opsForHyperLogLog();

        // PFCOUNT command
        return operations.size(key);
    }

    /**
     * Get estimated cardinalities for multiple HyperLogLog keys in batch
     *
     * @param keys collection of cache keys
     * @return list of estimated cardinalities, empty list if keys is null
     */
    @Override
    public List multiCounter(Collection keys) {
        if (keys == null) {
            return new ArrayList();
        }
        List<Long> result = new ArrayList<>();
        for (Object key : keys) {
            result.add(counter(key));
        }
        return result;
    }

    /**
     * Merge multiple HyperLogLogs into the first key (uses PFMERGE command)
     *
     * @param key first argument is the destination key, followed by source keys to merge
     * @return estimated cardinality after merging
     */
    @Override
    public Long mergeCounter(Object... key) {
        HyperLogLogOperations<Object, Object> operations = redisTemplate.opsForHyperLogLog();
        // merge and accumulate counters
        return operations.union(key[0], key);
    }

    /**
     * Atomic increment with expiration time (set only on first creation).
     * Commonly used for rate limiting and counter scenarios.
     *
     * @param key      cache key
     * @param liveTime expiration time in seconds, only applied when the counter is first created
     * @return the value before increment
     */
    @Override
    public Long incr(String key, long liveTime) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        Long increment = entityIdCounter.getAndIncrement();
        // set expiration on initial creation
        if (increment == 0 && liveTime > 0) {
            entityIdCounter.expire(liveTime, TimeUnit.SECONDS);
        }

        return increment;
    }

    /**
     * Atomic increment without expiration
     *
     * @param key cache key
     * @return the value before increment
     */
    @Override
    public Long incr(String key) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return entityIdCounter.getAndIncrement();
    }

    /**
     * Increment the score of a keyword in a Sorted Set by 1 (ZINCRBY command).
     * If the Sorted Set does not exist, it will be created automatically.
     * If the member does not exist, it will be added with a score of 1.
     *
     * @param sortedSetName name of the Sorted Set (created automatically if not present)
     * @param keyword       the keyword member
     */
    @Override
    public void incrementScore(String sortedSetName, String keyword) {
        // increment the score of the zset member
        redisTemplate.opsForZSet().incrementScore(sortedSetName, keyword, 1);
    }

    /**
     * Increment the score of a keyword in a Sorted Set by a specified value (ZINCRBY command).
     * If the member does not exist, it will be created with the given score.
     *
     * @param sortedSetName name of the Sorted Set
     * @param keyword       the keyword member
     * @param score         the score increment value
     */
    @Override
    public void incrementScore(String sortedSetName, String keyword, Integer score) {
        redisTemplate.opsForZSet().incrementScore(sortedSetName, keyword, score);
    }

    /**
     * Query a range of values from a Sorted Set in descending order by score (ZREVRANGE command).
     * No out-of-bounds error will occur if start/end exceed the set size.
     *
     * @param sortedSetName name of the Sorted Set
     * @param start         start index of the range
     * @param end           end index of the range
     * @return set of typed tuples sorted by score in descending order
     */
    @Override
    public Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String sortedSetName, Integer start, Integer end) {
        return this.redisTemplate.opsForZSet().reverseRangeWithScores(sortedSetName, start, end);
    }

    /**
     * Query top N values from a Sorted Set in descending order by score (ZREVRANGE command).
     * No out-of-bounds error will occur if count exceeds the set size.
     *
     * @param sortedSetName name of the Sorted Set
     * @param count         number of entries to retrieve
     * @return set of typed tuples sorted by score in descending order
     */
    @Override
    public Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String sortedSetName, Integer count) {
        return this.redisTemplate.opsForZSet().reverseRangeWithScores(sortedSetName, 0, count);
    }


    /**
     * Add a member to a Sorted Set with a given score
     *
     * @param key   cache key
     * @param score score value, typically used for sorting
     * @param value member value
     * @return true if the member was added successfully
     */
    @Override
    public boolean zAdd(String key, long score, String value) {
        return redisTemplate.opsForZSet().add(key, value, score);

    }


    /**
     * Get members within a score range from a Sorted Set
     *
     * @param key  cache key
     * @param from minimum score (inclusive)
     * @param to   maximum score (inclusive)
     * @return set of typed tuples within the score range
     */
    @Override
    public Set<ZSetOperations.TypedTuple<Object>> zRangeByScore(String key, int from, long to) {
        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet().rangeByScoreWithScores(key, from, to);
        return set;
    }

    /**
     * Remove members from a Sorted Set
     *
     * @param key   cache key
     * @param value members to remove
     * @return the number of members removed
     */
    @Override
    public Long zRemove(String key, String... value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }
}
