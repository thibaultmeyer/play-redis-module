package com.zero_x_baadf00d.play.module.redis;

import redis.clients.jedis.Jedis;

import java.util.concurrent.Callable;

/**
 * Redis module give access to methods to easily use
 * a Redis database.
 *
 * @author Thibault Meyer
 * @version 16.03.09
 * @since 16.03.09
 */
public interface RedisModule {

    /**
     * Get a Redis connection from the pool.
     *
     * @return A redis connection
     * @see Jedis
     * @since 16.03.09
     */
    Jedis getConnection();

    /**
     * Retrieves an object by key.
     *
     * @return object
     * @since 16.03.09
     */
    <T> T get(final String key);

    /**
     * Sets a value without expiration.
     *
     * @param key   Item key
     * @param value The value to set
     * @since 16.03.09
     */
    void set(final String key, final Object value);

    /**
     * Sets a value with expiration.
     *
     * @param key        Item key
     * @param value      The value to set
     * @param expiration expiration in seconds
     * @since 16.03.09
     */
    void set(final String key, final Object value, final int expiration);

    /**
     * Retrieve a value from the cache, or set it from a default
     * Callable function. The value has no expiration.
     *
     * @param key   Item key
     * @param block block returning value to set if key does not exist
     * @return value
     * @since 16.03.09
     */
    <T> T getOrElse(final String key, final Callable<T> block);

    /**
     * Retrieve a value from the cache, or set it from a default
     * Callable function.
     *
     * @param key        Item key
     * @param block      block returning value to set if key does not exist
     * @param expiration expiration period in seconds
     * @return value
     * @since 16.03.09
     */
    <T> T getOrElse(final String key, final Callable<T> block, final int expiration);

    /**
     * Removes a value from the cache.
     *
     * @param key The key to remove the value for
     * @since 16.03.09
     */
    void remove(final String key);

    /**
     * Removes a value from the cache.
     *
     * @param keys Keys to remove from redis
     * @since 16.03.09
     */
    void remove(final String... keys);
}
