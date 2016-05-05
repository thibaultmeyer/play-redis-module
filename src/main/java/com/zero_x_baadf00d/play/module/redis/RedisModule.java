/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Thibault Meyer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zero_x_baadf00d.play.module.redis;

import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Redis module give access to methods to easily use
 * a Redis database.
 *
 * @author Thibault Meyer
 * @version 16.05.05
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
     * @param key   Item key
     * @param clazz The object type
     * @param <T>   Generic type of something implementing {@code java.io.Serializable}
     * @return object or {@code null}
     * @since 16.03.09
     */
    <T> T get(final String key, final Class<T> clazz);

    /**
     * Retrieves an object by key.
     *
     * @param key  Item key
     * @param type The object type
     * @param <T>  Generic type of something implementing {@code java.io.Serializable}
     * @return object or {@code null}
     * @since 16.04.05
     */
    <T> T get(final String key, final Type type);

    /**
     * Sets a value without expiration.
     *
     * @param key   Item key
     * @param value The value to set
     * @since 16.03.09
     */
    @Deprecated
    void set(final String key, final Object value);

    /**
     * Sets a value without expiration.
     *
     * @param key   Item key
     * @param clazz Object type
     * @param value The value to set
     * @param <T>   Generic type of something implementing {@code java.io.Serializable}
     * @since 16.03.31
     */
    <T> void set(final String key, final Class<T> clazz, final Object value);

    /**
     * Sets a value without expiration.
     *
     * @param key   Item key
     * @param type  Object type
     * @param value The value to set
     * @since 16.04.05
     */
    void set(final String key, final Type type, final Object value);

    /**
     * Sets a value with expiration.
     *
     * @param key        Item key
     * @param value      The value to set
     * @param expiration expiration in seconds
     * @since 16.03.09
     */
    @Deprecated
    void set(final String key, final Object value, final int expiration);

    /**
     * Sets a value with expiration.
     *
     * @param key        Item key
     * @param clazz      Object type
     * @param value      The value to set
     * @param expiration expiration in seconds
     * @param <T>        Generic type of something implementing {@code java.io.Serializable}
     * @since 16.03.31
     */
    <T> void set(final String key, final Class<T> clazz, final Object value, final int expiration);

    /**
     * Sets a value with expiration.
     *
     * @param key        Item key
     * @param type       Object type
     * @param value      The value to set
     * @param expiration expiration in seconds
     * @since 16.04.05
     */
    void set(final String key, final Type type, final Object value, final int expiration);

    /**
     * Retrieve a value from the cache, or set it from a default
     * Callable function. The value has no expiration.
     *
     * @param key   Item key
     * @param clazz Object type
     * @param block block returning value to set if key does not exist
     * @param <T>   Generic type of something implementing {@code java.io.Serializable}
     * @return value
     * @since 16.03.31
     */
    <T> T getOrElse(final String key, final Class<T> clazz, final Callable<T> block);

    /**
     * Retrieve a value from the cache, or set it from a default
     * Callable function. The value has no expiration.
     *
     * @param key   Item key
     * @param type  Object type
     * @param block block returning value to set if key does not exist
     * @param <T>   Generic type of something implementing {@code java.io.Serializable}
     * @return value
     * @since 16.04.05
     */
    <T> T getOrElse(final String key, final Type type, final Callable<T> block);

    /**
     * Retrieve a value from the cache, or set it from a default
     * Callable function.
     *
     * @param key        Item key
     * @param clazz      Object type
     * @param block      block returning value to set if key does not exist
     * @param expiration expiration period in seconds
     * @param <T>        Generic type of something implementing {@code java.io.Serializable}
     * @return value
     * @since 16.03.31
     */
    <T> T getOrElse(final String key, final Class<T> clazz, final Callable<T> block, final int expiration);

    /**
     * Retrieve a value from the cache, or set it from a default
     * Callable function.
     *
     * @param key        Item key
     * @param type       Object type
     * @param block      block returning value to set if key does not exist
     * @param expiration expiration period in seconds
     * @param <T>        Generic type of something implementing {@code java.io.Serializable}
     * @return value
     * @since 16.04.05
     */
    <T> T getOrElse(final String key, final Type type, final Callable<T> block, final int expiration);

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

    /**
     * Check if key is present on Redis database.
     *
     * @param key The key to test
     * @return {@code true} if the key is present on Redis database
     * @since 16.04.11
     */
    boolean exists(final String key);

    /**
     * Add a value in a list.
     *
     * @param key   The list key
     * @param clazz Object type
     * @param value The value to add in the list
     * @param <T>   Generic type of something implementing {@code java.io.Serializable}
     * @since 16.05.05
     */
    <T> void addInList(final String key, final Class<T> clazz, final Object value);

    /**
     * Add a value in a list.
     *
     * @param key     The list key
     * @param clazz   Object type
     * @param value   The value to add in list
     * @param maxItem The maximum number of items to keep in the list
     * @param <T>     Generic type of something implementing {@code java.io.Serializable}
     * @since 16.05.05
     */
    <T> void addInList(final String key, final Class<T> clazz, final Object value, final int maxItem);

    /**
     * Get values from a list.
     *
     * @param key   The list key
     * @param clazz Object type
     * @param <T>   Generic type of something implementing {@code java.io.Serializable}
     * @return The values list
     * @since 16.05.05
     */
    <T> List<T> getFromList(final String key, final Class<T> clazz);
}
