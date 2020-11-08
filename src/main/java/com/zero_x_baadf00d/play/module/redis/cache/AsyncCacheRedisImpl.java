/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 - 2017 Thibault Meyer
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
package com.zero_x_baadf00d.play.module.redis.cache;

import akka.Done;
import com.zero_x_baadf00d.play.module.redis.PlayRedis;
import play.cache.AsyncCacheApi;
import play.cache.SyncCacheApi;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Implementation of {@code PlayRedis} as Async Cache Api.
 *
 * @author Felipe Bonezi
 * @version 20.11
 * @see PlayRedis
 * @since 20.11.05
 */
@Singleton
public class AsyncCacheRedisImpl implements AsyncCacheApi {

    /**
     * Default prefix mapping content saved in Redis.
     *
     * @since 20.11.05
     */
    private static final String DEFAULT_PREFIX = "play.cache.";

    /**
     * The {@link PlayRedis} injected implementation.
     *
     * @since 20.11.05
     */
    private final SyncCacheRedisImpl syncCacheApi;

    @Inject
    public AsyncCacheRedisImpl(final SyncCacheRedisImpl syncCacheApi) {
        this.syncCacheApi = syncCacheApi;
    }

    /**
     * Get the {@link PlayRedis} implementation class.
     *
     * @return A {@link PlayRedis} implementation.
     * @since 20.11.05
     */
    public PlayRedis asPlayRedis() {
        return this.syncCacheApi.asPlayRedis();
    }

    @Override
    public <T> CompletionStage<Optional<T>> get(final String key) {
        return supplyAsync(() -> this.sync().get(this.prepareKey(key)));
    }

    @Override
    public <T> CompletionStage<T> getOrElseUpdate(final String key, final Callable<CompletionStage<T>> callable,
                                                  final int expiration) {
        return CompletableFuture.supplyAsync(() -> this.sync().<T>get(this.prepareKey(key)))
            .thenCompose(optional -> {
                if (optional.isPresent())
                    return completedFuture(optional.get());

                try {
                    return callable.call()
                        .thenCompose(t ->
                            this.set(key, t).thenApply(done -> t));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CompletionException(e);
                }
            });
    }

    @Override
    public <T> CompletionStage<T> getOrElseUpdate(final String key, final Callable<CompletionStage<T>> callable) {
        return this.getOrElseUpdate(key, callable, 0);
    }

    @Override
    public CompletionStage<Done> set(final String key, final Object o, final int expiration) {
        return supplyAsync(() -> {
            this.sync().set(this.prepareKey(key), o, expiration);
            return Done.done();
        });
    }

    @Override
    public CompletionStage<Done> set(final String key, final Object o) {
        return this.set(key, o, 0);
    }

    @Override
    public CompletionStage<Done> remove(final String key) {
        return supplyAsync(() -> {
            this.sync().remove(this.prepareKey(key));
            return Done.done();
        });
    }

    @Override
    public CompletionStage<Done> removeAll() {
        return supplyAsync(() -> {
            try (final Jedis connection = this.asPlayRedis().getConnection(0)) {
                // Search all keys in Redis.
                // https://redis.io/commands/keys
                connection.keys(DEFAULT_PREFIX + "*").forEach(this::remove);
            }
            return Done.done();
        });
    }

    @Override
    public SyncCacheApi sync() {
        return this.syncCacheApi;
    }

    /**
     * Return the prepared key for be used on redis with a default prefix.
     *
     * @param key Content key.
     * @return Key with prefix.
     * @since 20.11.05
     */
    private String prepareKey(final String key) {
        return DEFAULT_PREFIX + key;
    }

}
