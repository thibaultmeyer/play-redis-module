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

import com.fasterxml.jackson.core.type.TypeReference;
import com.zero_x_baadf00d.play.module.redis.PlayRedis;
import play.cache.SyncCacheApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Implementation of {@code PlayRedis}.
 *
 * @author Felipe Bonezi
 * @version 20.11
 * @see PlayRedis
 * @since 20.11.05
 */
@Singleton
public class SyncCaheRedisImpl implements SyncCacheApi {

    /**
     * The {@link PlayRedis} injected implementation.
     *
     * @since 20.11.05
     */
    private final PlayRedis playRedis;

    @Inject
    public SyncCaheRedisImpl(final PlayRedis playRedis) {
        this.playRedis = playRedis;
    }

    /**
     * Get the {@link PlayRedis} implementation class.
     *
     * @return A {@link PlayRedis} implementation.
     * @since 20.11.05
     */
    public PlayRedis asPlayRedis() {
        return this.playRedis;
    }

    @Override
    public <T> Optional<T> get(final String key) {
        return Optional.ofNullable(this.playRedis.get(key, new TypeReference<T>() {
        }));
    }

    @Override
    public <T> T getOrElseUpdate(final String key, final Callable<T> callable, final int expiration) {
        final Optional<T> optional = this.get(key);
        return optional.orElseGet(() -> {
            try {
                final T obj = callable.call();
                this.set(key, obj, expiration);
                return obj;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public <T> T getOrElseUpdate(final String key, final Callable<T> callable) {
        final Optional<T> optional = this.get(key);
        return optional.orElseGet(() -> {
            try {
                final T obj = callable.call();
                this.set(key, obj);
                return obj;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public void set(final String key, final Object o, final int expiration) {
        if (o instanceof Long) {
            this.playRedis.set(key, Long.class, (Long) o, expiration);
        } else {
            this.playRedis.set(key, new TypeReference<Object>() {
            }, o, expiration);
        }
    }

    @Override
    public void set(final String key, final Object o) {
        this.set(key, o, 0);
    }

    @Override
    public void remove(final String key) {
        this.playRedis.remove(key);
    }

}
