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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.Json;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@code RedisModule}.
 *
 * @author Thibault Meyer
 * @version 16.11.13
 * @see RedisModule
 * @since 16.03.09
 */
@Singleton
public class RedisModuleImpl implements RedisModule {

    /**
     * Logger instance.
     *
     * @since 16.05.07
     */
    private static final Logger.ALogger LOG = Logger.of(RedisModule.class);

    /**
     * @since 16.03.09
     */
    private static final String REDISPOOL_SERVER_HOST = "redis.default.host";

    /**
     * @since 16.03.09
     */
    private static final String REDISPOOL_SERVER_PORT = "redis.default.port";

    /**
     * @since 16.03.09
     */
    private static final String REDISPOOL_SERVER_PASSWORD = "redis.default.password";

    /**
     * @since 16.03.09
     */
    private static final String REDISPOOL_SERVER_DB_DEFAULT = "redis.default.db.default";

    /**
     * @since 16.03.09
     */
    private static final String REDISPOOL_SERVER_CONN_TIMEOUT = "redis.default.conn.timeout";

    /**
     * @since 16.03.09
     */
    private static final String REDISPOOL_SERVER_CONN_TOTAL = "redis.default.conn.maxtotal";

    /**
     * @since 16.03.09
     */
    private static final String REDISPOOL_SERVER_CONN_MAXIDLE = "redis.default.conn.maxidle";

    /**
     * @since 16.03.09
     */
    private static final String REDISPOOL_SERVER_CONN_MINIDLE = "redis.default.conn.minidle";

    /**
     * The Redis connections pool.
     *
     * @since 16.03.09
     */
    private JedisPool redisPool;

    /**
     * The database number to use by default.
     *
     * @since 16.03.09
     */
    private Integer redisDefaultDb;

    /**
     * Build a basic instance with injected dependency.
     *
     * @param lifecycle     The current application lifecyle
     * @param configuration The current application configuration
     * @since 16.03.09
     */
    @Inject
    public RedisModuleImpl(final ApplicationLifecycle lifecycle, final Configuration configuration) {
        final String redisHost = configuration.getString(RedisModuleImpl.REDISPOOL_SERVER_HOST, "127.0.0.1");
        final String redisPassword = configuration.getString(RedisModuleImpl.REDISPOOL_SERVER_PASSWORD);
        final Integer redisPort = configuration.getInt(RedisModuleImpl.REDISPOOL_SERVER_PORT, 6379);
        final Integer redisConnTimeout = configuration.getInt(RedisModuleImpl.REDISPOOL_SERVER_CONN_TIMEOUT, 0);
        final Integer redisConnTotal = configuration.getInt(RedisModuleImpl.REDISPOOL_SERVER_CONN_TOTAL, 64);
        final Integer redisConnMaxIdle = configuration.getInt(RedisModuleImpl.REDISPOOL_SERVER_CONN_MAXIDLE, 16);
        final Integer redisConnMinIdle = configuration.getInt(RedisModuleImpl.REDISPOOL_SERVER_CONN_MINIDLE, redisConnMaxIdle / 2);
        this.redisDefaultDb = configuration.getInt(RedisModuleImpl.REDISPOOL_SERVER_DB_DEFAULT, null);
        if (redisHost != null) {
            final JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMinIdle(redisConnMinIdle > 0 ? redisConnMinIdle : 1);
            poolConfig.setMaxIdle(redisConnMaxIdle > 0 ? redisConnMaxIdle : 1);
            poolConfig.setMaxTotal(redisConnTotal > 0 ? redisConnTotal : 1);
            if (redisPassword != null && !redisPassword.isEmpty()) {
                this.redisPool = new JedisPool(poolConfig, redisHost, redisPort, redisConnTimeout, redisPassword);
            } else {
                this.redisPool = new JedisPool(poolConfig, redisHost, redisPort, redisConnTimeout);
            }
            RedisModuleImpl.LOG.info("Redis connected at {}", String.format("redis://%s:%d", redisHost, redisPort));
        } else {
            throw new RuntimeException("Redis module is not properly configured");
        }
        if (lifecycle != null) {
            lifecycle.addStopHook(() -> {
                RedisModuleImpl.LOG.info("Shutting down Redis");
                this.redisPool.close();
                return CompletableFuture.completedFuture(null);
            });
        }
    }

    @Override
    public Jedis getConnection() {
        if (this.redisDefaultDb == null) {
            return this.redisPool.getResource();
        }
        final Jedis conn = this.redisPool.getResource();
        conn.select(this.redisDefaultDb);
        return conn;
    }

    @Override
    public Jedis getConnection(final int db) {
        final Jedis conn = this.redisPool.getResource();
        conn.select(db >= 0 ? db : 0);
        return conn;
    }

    @Override
    public <T> T get(final String key, final TypeReference<T> typeReference) {
        T object = null;
        try {
            final String rawData;
            try (final Jedis jedis = this.redisPool.getResource()) {
                if (this.redisDefaultDb != null) {
                    jedis.select(this.redisDefaultDb);
                }
                rawData = jedis.get(key);
            }
            if (rawData != null) {
                object = Json.mapper().readerFor(typeReference).readValue(rawData.getBytes());
            }
        } catch (IOException ex) {
            RedisModuleImpl.LOG.error("Can't get object", ex);
        }
        return object;
    }

    @Override
    public <T> void set(final String key, final TypeReference<T> typeReference, final Object value) {
        this.set(key, typeReference, value, 0);
    }

    @Override
    public <T> void set(final String key, final TypeReference<T> typeReference, final Object value, final int expiration) {
        try {
            final String data = Json.mapper().writerFor(typeReference).writeValueAsString(value);
            try (final Jedis jedis = this.redisPool.getResource()) {
                if (this.redisDefaultDb != null) {
                    jedis.select(this.redisDefaultDb);
                }
                jedis.set(key, data);
                if (expiration > 0) {
                    jedis.expire(key, expiration);
                }
            }
        } catch (IOException ex) {
            RedisModuleImpl.LOG.error("Can't set object", ex);
        }
    }

    @Override
    public <T> T getOrElse(final String key, final TypeReference<T> typeReference, final Callable<T> block) {
        return this.getOrElse(key, typeReference, block, 0);
    }

    @Override
    public <T> T getOrElse(final String key, final TypeReference<T> typeReference, final Callable<T> block, final int expiration) {
        T data = this.get(key, typeReference);
        if (data == null) {
            try {
                data = block.call();
                this.set(key, typeReference, data, expiration);
            } catch (Exception ex) {
                RedisModuleImpl.LOG.error("Something goes wrong during the Callable execution", ex);
            }
        }
        return data;
    }

    @Override
    public void remove(final String key) {
        try (final Jedis jedis = this.redisPool.getResource()) {
            if (this.redisDefaultDb != null) {
                jedis.select(this.redisDefaultDb);
            }
            jedis.del(key);
        }
    }

    @Override
    public void remove(final String... keys) {
        try (final Jedis jedis = this.redisPool.getResource()) {
            if (this.redisDefaultDb != null) {
                jedis.select(this.redisDefaultDb);
            }
            for (final String k : keys) {
                jedis.del(k);
            }
        }
    }

    @Override
    public boolean exists(final String key) {
        boolean exists;
        try (final Jedis jedis = this.redisPool.getResource()) {
            if (this.redisDefaultDb != null) {
                jedis.select(this.redisDefaultDb);
            }
            exists = jedis.exists(key);
        }
        return exists;
    }

    @Override
    public <T> void addInList(final String key, final TypeReference<T> typeReference, final Object value) {
        try {
            final String data = Json.mapper().writerFor(typeReference).writeValueAsString(value);
            try (final Jedis jedis = this.redisPool.getResource()) {
                if (this.redisDefaultDb != null) {
                    jedis.select(this.redisDefaultDb);
                }
                jedis.lpush(key, data);
            }
        } catch (IOException ex) {
            RedisModuleImpl.LOG.error("Something goes wrong with Redis module", ex);
        }
    }

    @Override
    public <T> void addInList(final String key, final TypeReference<T> typeReference, final Object value, final int maxItem) {
        try {
            final String data = Json.mapper().writerFor(typeReference).writeValueAsString(value);
            try (final Jedis jedis = this.redisPool.getResource()) {
                if (this.redisDefaultDb != null) {
                    jedis.select(this.redisDefaultDb);
                }
                jedis.lpush(key, data);
                jedis.ltrim(key, 0, maxItem > 0 ? maxItem - 1 : maxItem);
            }
        } catch (IOException ex) {
            RedisModuleImpl.LOG.error("Something goes wrong with Redis module", ex);
        }
    }

    @Override
    public <T> List<T> getFromList(final String key, final TypeReference<T> typeReference) {
        return this.getFromList(key, typeReference, 0, -1);
    }

    @Override
    public <T> List<T> getFromList(final String key, final TypeReference<T> typeReference, final int offset, final int count) {
        final List<T> objects = new ArrayList<>();
        try {
            final List<String> rawData;
            try (final Jedis jedis = this.redisPool.getResource()) {
                if (this.redisDefaultDb != null) {
                    jedis.select(this.redisDefaultDb);
                }
                rawData = jedis.lrange(key, offset, count > 0 ? count - 1 : count);
            }
            if (rawData != null) {
                final ObjectReader objectReader = Json.mapper().readerFor(typeReference);
                for (final String s : rawData) {
                    objects.add(objectReader.readValue(s));
                }
            }
        } catch (IOException | NullPointerException ex) {
            RedisModuleImpl.LOG.error("Something goes wrong with Redis module", ex);
        }
        return objects;
    }

    @Override
    public boolean tryLock(final String key, final int expiration) {
        long ret = 0;
        try (final Jedis jedis = this.redisPool.getResource()) {
            if (this.redisDefaultDb != null) {
                jedis.select(this.redisDefaultDb);
            }
            ret = jedis.setnx(key, "1");
            if (ret == 1) {
                jedis.expire(key, expiration);
            }
        } catch (JedisConnectionException ex) {
            RedisModuleImpl.LOG.error("Can't connect to Redis: {}", ex.getCause().getMessage());
        } catch (JedisDataException ex) {
            RedisModuleImpl.LOG.error("Can't connect to Redis: {}", ex.getMessage());
        }
        return ret == 1;
    }
}
