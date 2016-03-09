package com.zero_x_baadf00d.play.module.redis;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.concurrent.Callable;

/**
 * Implementation of {@code RedisModule}.
 *
 * @author Thibault Meyer
 * @version 16.03.09
 * @see RedisModule
 * @since 16.03.09
 */
@Singleton
public class RedisModuleImpl implements RedisModule {

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
     * @param lifecycle The current application lifecyle
     * @since 16.03.09
     */
    @Inject
    public RedisModuleImpl(final ApplicationLifecycle lifecycle, final Configuration configuration) {
        final String redisHost = configuration.getString(RedisModuleImpl.REDISPOOL_SERVER_HOST);
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
            if (redisPassword != null) {
                this.redisPool = new JedisPool(poolConfig, redisHost, redisPort, redisConnTimeout, redisPassword);
            } else {
                this.redisPool = new JedisPool(poolConfig, redisHost, redisPort, redisConnTimeout);
            }
        } else {
            throw new RuntimeException("RedisModule is not properly configured");
        }
        lifecycle.addStopHook(() -> {
            this.redisPool.close();
            return F.Promise.pure(null);
        });
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
    @SuppressWarnings("unchecked")
    public <T> T get(final String key) {
        T object = null;
        try {
            final String rawData;
            try (final Jedis jedis = this.redisPool.getResource()) {
                if (this.redisDefaultDb != null) {
                    jedis.select(this.redisDefaultDb);
                }
                rawData = jedis.get(key);
            }
            final ByteArrayInputStream bais = new ByteArrayInputStream(Base64Coder.decode(rawData));
            final ObjectInputStream ois = new ObjectInputStream(bais);
            object = (T) ois.readObject();
            bais.close();
            ois.close();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.error("Can't deserialize object", ex);
        } catch (NullPointerException ignore) {
        }
        return object;
    }

    @Override
    public void set(final String key, final Object value) {
        this.set(key, value, 0);
    }

    @Override
    public void set(final String key, final Object value, final int expiration) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            try (final Jedis jedis = this.redisPool.getResource()) {
                if (this.redisDefaultDb != null) {
                    jedis.select(this.redisDefaultDb);
                }
                jedis.set(key, new String(Base64Coder.encode(baos.toByteArray())));
                if (expiration > 0) {
                    jedis.expire(key, expiration);
                }
            }
            baos.close();
            oos.close();
        } catch (IOException ex) {
            Logger.error("Can't serialize object", ex);
        }
    }

    @Override
    public <T> T getOrElse(final String key, final Callable<T> block) {
        return this.getOrElse(key, block, 0);
    }

    @Override
    public <T> T getOrElse(final String key, final Callable<T> block, final int expiration) {
        T data = this.get(key);
        if (data == null) {
            try {
                data = block.call();
                this.set(key, data, expiration);
            } catch (Exception ex) {
                Logger.error("Something goes wrong Callable execution", ex);
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
}
