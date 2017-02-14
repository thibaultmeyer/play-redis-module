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

import com.fasterxml.jackson.core.type.TypeReference;
import com.zero_x_baadf00d.play.module.redis.RedisModule;
import com.zero_x_baadf00d.play.module.redis.RedisModuleImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.Application;
import play.inject.ApplicationLifecycle;
import play.test.Helpers;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.HashMap;

import static org.mockito.Mockito.mock;

/**
 * RedisFailureTest.
 *
 * @author Thibault Meyer
 * @version 17.02.14
 * @since 17.02.14
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RedisFailureTest {

    /**
     * Handle to the Redis module
     *
     * @since 17.02.14
     */
    private RedisModule redisModule;

    /**
     * Initialize Redis module.
     *
     * @since 17.02.14
     */
    @Before
    public void initializeRedisModule() {
        if (this.redisModule == null) {
            final Application application = Helpers.
                    fakeApplication(new HashMap<String, Object>() {{
                        put("redis.default.db.default", 0);
                        put("redis.default.host", "127.0.0.1");
                        put("redis.default.port", 6380);
                    }});
            this.redisModule = new RedisModuleImpl(
                    mock(ApplicationLifecycle.class),
                    application.configuration()
            );
            Assert.assertNotEquals(null, this.redisModule);
        }
    }

    /**
     * @since 17.02.14
     */
    @Test
    public void redisFailureTest_001_tryLock() {
        boolean isLockAcquired = this.redisModule.tryLock("junit.lock", 900);
        Assert.assertFalse(isLockAcquired);
    }

    /**
     * @since 17.02.14
     */
    @Test
    public void redisFailureTest_002_set_get() {
        try {
            this.redisModule.set("junit.item", new TypeReference<String>() {
            }, "Hello World!");
            Assert.fail();
        } catch (JedisConnectionException ignore) {
        }
    }
}
